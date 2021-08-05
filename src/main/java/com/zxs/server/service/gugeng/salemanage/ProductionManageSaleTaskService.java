package com.zxs.server.service.gugeng.salemanage;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.*;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.*;
import net.app315.hydra.intelligent.planting.dto.gugeng.trace.EmployeeMsgDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.enums.gugeng.SaleTaskTimeEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.SaleTaskTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageSaleTask;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsSaleTargetData;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductionManageOrderProductMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductionManageSaleTaskMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.SaleManageConstants.MID_LINE;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.SaleManageConstants.QUARTER;
import static net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil.YEAR;


/**
 * 销售管理业务类
 *
 * @author shixiongfei
 */
@Service
public class ProductionManageSaleTaskService extends CommonUtil {

    @Autowired
    private ProductionManageSaleTaskMapper saleTaskMapper;

    @Autowired
    private ProductionManageOrderProductMapper orderProductMapper;

    @Autowired
    private ProductionManageSaleTaskComparisonService comparisonService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;


    /**
     * 制定部门销售任务
     *
     * @param requestVO
     */
    @Transactional(rollbackFor = Exception.class)
    public void createDepartment(MakingDepartmentSaleTaskRequestDTO requestVO) throws SuperCodeException {
        create(requestVO, SaleTaskTypeEnum.DEPARTMENT);
    }

    /**
     * 制定个人销售任务
     *
     * @param requestVO
     */
    @Transactional
    public void createPersonal(MakingPersonalSaleTaskRequestDTO requestVO) throws SuperCodeException {
        create(requestVO, SaleTaskTypeEnum.PERSONAL);
    }

    /**
     * 制定计划
     *
     * @param requestVO
     */
    private void create(MakingDepartmentSaleTaskRequestDTO requestVO, SaleTaskTypeEnum typeEnum) throws SuperCodeException {
        ProductionManageSaleTask saleTask = new ProductionManageSaleTask();
        // 对象属性复制
        BeanUtils.copyProperties(requestVO, saleTask);

        // 获取销售时间并处理为年月类型
        Date saleDate;
        try {
            saleDate = DateUtils.parseDate(requestVO.getSaleDate(), "yyyy-MM");
        } catch (ParseException e) {
            throw new SuperCodeException("时间类型转换错误", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        // 获取当前用户信息
        Employee employee = getEmployee();
        // 判断任务编号是否被填写
        if (StringUtils.isEmpty(saleTask.getTaskId())) {
            // 默认生成任务编号：年月日+ 6位流水号
            saleTask.setTaskId(numberGenerator.getSerialNumber(6, RedisKey.SALE_TASK_NO_KEY, getSysId() + getOrganizationId(), getSecondsNextEarlyMorning()));
        }
        // 判断是否包含此任务编号
        QueryWrapper<ProductionManageSaleTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProductionManageSaleTask.COL_TASK_ID, saleTask.getTaskId())
                .eq(ProductionManageSaleTask.COL_SYS_ID, getSysId())
                .eq(ProductionManageSaleTask.COL_ORGANIZATION_ID, getOrganizationId());
        CustomAssert.isNotGreaterThanCustomNumber(saleTaskMapper.selectCount(queryWrapper), 0, "任务编号已经存在，请重新输入");

        MakingPersonalSaleTaskRequestDTO personalRequestVO = new MakingPersonalSaleTaskRequestDTO();
        // 判断当前类型是否为个人类型
        if (SaleTaskTypeEnum.PERSONAL == typeEnum) {
            personalRequestVO = (MakingPersonalSaleTaskRequestDTO) requestVO;
            saleTask.setSalesPersonnelId(personalRequestVO.getSalesPersonnelId());
            saleTask.setSalesPersonnelName(personalRequestVO.getSalesPersonnelName());
            // 获取销售人员的部门信息
            EmployeeMsgDTO msg = commonUtil.getEmployeeMsg(getSuperToken(), personalRequestVO.getSalesPersonnelId());
            CustomAssert.isNull(msg, "此销售人员不隶属任何部门, 请检查");
            saleTask.setDepartmentId(msg.getDepartmentId());
            saleTask.setDepartmentName(msg.getDepartmentName());
        }

        /* 判断当前用户的任务的销售日期和产品名称是否重复,
            1. 如果是部门销售任务，则根据销售人员id为空和销售日期及产品名称判定唯一性
            2. 如果是个人销售任务， 则根据销售人员ID的值和销售日期及产品名称判定唯一性
         */

        QueryWrapper<ProductionManageSaleTask> wrapper = new QueryWrapper<>();
        wrapper.eq(ProductionManageSaleTask.COL_SALE_DATE, saleDate)
                .eq(ProductionManageSaleTask.COL_PRODUCT_ID, requestVO.getProductId())
                .isNull(typeEnum == SaleTaskTypeEnum.DEPARTMENT, ProductionManageSaleTask.COL_SALES_PERSONNEL_ID)
                .eq((typeEnum == SaleTaskTypeEnum.PERSONAL), ProductionManageSaleTask.COL_SALES_PERSONNEL_ID, personalRequestVO.getSalesPersonnelId())
                .eq(typeEnum == SaleTaskTypeEnum.DEPARTMENT, ProductionManageSaleTask.COL_DEPARTMENT_ID, requestVO.getDepartmentId())
                .eq(ProductionManageSaleTask.COL_SYS_ID, getSysId())
                .eq(ProductionManageSaleTask.COL_ORGANIZATION_ID, getOrganizationId());
        if (saleTaskMapper.selectCount(wrapper) > 0) {
            String errorMessage = typeEnum == SaleTaskTypeEnum.DEPARTMENT
                    ? "销售日期, 部门, 产品不可重复, 请重新填写"
                    : "销售日期, 产品, 销售人员不可重复, 请重新填写";
            throw new SuperCodeException(errorMessage, 500);
        }
        // 判断是否选择了重量单位，默认为斤，对应的code为006003
        if (StringUtils.isEmpty(saleTask.getWeightUnitCode())) {
            // TODO 暂时硬编码，后续可能需要修改
            saleTask.setWeightUnitCode("006003");
        }


        saleTask.setCreateUserId(employee.getEmployeeId());
        saleTask.setCreateUserName(employee.getName());

        // 获取组织信息
        saleTask.setOrganizationId(getOrganizationId());
        saleTask.setOrganizationName(getOrganizationName());

        // 获取当前时间
        Date now = new Date();
        saleTask.setCreateDate(now);

        saleTask.setUpdateUserId(employee.getEmployeeId());
        saleTask.setUpdateDate(now);

        // 设置销售时间
        saleTask.setSaleDate(saleDate);

        // 设置系统id
        saleTask.setSysId(commonUtil.getSysId());

        // 设置类型
        saleTask.setTaskType(typeEnum.getKey());

        saleTaskMapper.insert(saleTask);

        // 添加或更新销售任务对比数据
        comparisonService.addOrUpdate(saleTask, typeEnum);
    }

    /**
     * 获取部门销售任务列表
     *
     * @param requestVO
     * @return
     * @throws Exception
     */
    public RestResult<PageResults<List<DepartmentSaleTaskResponseVO>>> listForDepartment(ProductManageSaleTaskRequestDTO requestVO) throws SuperCodeException {

        RestResult<PageResults<List<DepartmentSaleTaskResponseVO>>> restResult = new RestResult<>();
        IPage<ProductionManageSaleTask> iPage = list(requestVO, SaleTaskTypeEnum.DEPARTMENT,"departmentSalesGoals");
        PageResults<List<DepartmentSaleTaskResponseVO>> pageInfo = setListPagination(iPage);
        List<ProductionManageSaleTask> list = iPage.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            pageInfo.setList(Collections.EMPTY_LIST);
            restResult.setState(200);
            restResult.setMsg("success");
            return restResult;
        }
        List<DepartmentSaleTaskResponseVO> tasks = new ArrayList<>(list.size());
        list.forEach(task -> {
            DepartmentSaleTaskResponseVO responseVO = new DepartmentSaleTaskResponseVO();
            BeanUtils.copyProperties(task, responseVO);
            // 设置任务制定时间为最近修改时间
            responseVO.setCreateDate(DateFormatUtils.format(task.getUpdateDate(), "yyyy-MM-dd HH:mm:ss"));
            // 设置销售时间
            responseVO.setSaleDate(DateFormatUtils.format(task.getSaleDate(), "yyyy-MM"));
            // 统计
            getStatistics(task, responseVO);
            tasks.add(responseVO);
        });
        pageInfo.setList(tasks);

        restResult.setState(200);
        restResult.setMsg("success");
        restResult.setResults(pageInfo);
        return restResult;
    }

    /**
     * 获取个人销售任务列表
     *
     * @param requestVO
     * @return
     * @throws Exception
     */
    public RestResult<PageResults<List<PersonalSaleTaskResponseVO>>> listForPersonal(ProductManageSaleTaskRequestDTO requestVO) throws SuperCodeException {

        RestResult<PageResults<List<PersonalSaleTaskResponseVO>>> restResult = new RestResult<>();
        // 获取查询结果集合
        IPage<ProductionManageSaleTask> iPage = list(requestVO, SaleTaskTypeEnum.PERSONAL,"unitSalesGoals");
        PageResults<List<PersonalSaleTaskResponseVO>> pageInfo = setListPagination(iPage);
        List<ProductionManageSaleTask> list = iPage.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            pageInfo.setList(Collections.EMPTY_LIST);
            restResult.setState(200);
            restResult.setMsg("success");
            restResult.setResults(pageInfo);
            return restResult;
        }

        List<PersonalSaleTaskResponseVO> tasks = new ArrayList<>(list.size());
        list.forEach(task -> {
            PersonalSaleTaskResponseVO responseVO = new PersonalSaleTaskResponseVO();
            BeanUtils.copyProperties(task, responseVO);
            // 设置任务制定时间为最近修改时间
            responseVO.setCreateDate(DateFormatUtils.format(task.getUpdateDate(), "yyyy-MM-dd HH:mm:ss"));
            // 设置销售时间
            responseVO.setSaleDate(DateFormatUtils.format(task.getSaleDate(), "yyyy-MM"));
            // 统计
            getStatistics(task, responseVO);
            tasks.add(responseVO);
        });

        pageInfo.setList(tasks);
        restResult.setState(200);
        restResult.setMsg("success");
        restResult.setResults(pageInfo);
        return restResult;
    }


    /**
     * 查询指定的任务列表
     *
     * @param requestVO
     * @return
     * @throws Exception
     */
    private IPage<ProductionManageSaleTask> list(ProductManageSaleTaskRequestDTO requestVO, SaleTaskTypeEnum typeEnum, String authCode) throws SuperCodeException {
        // 测试使用mybatisPlus分页插件
        Page<ProductionManageSaleTask> page = new Page<>(requestVO.getDefaultCurrent(), requestVO.getDefaultPageSize());
        QueryWrapper<ProductionManageSaleTask> queryWrapper = new QueryWrapper<>();

        // 获取系统id
        String sysId = getSessionSysId();
        // 获取组织id
        String organizationId = getSessionOrganizationId();

        // 查询指定的任务类型数据
        queryWrapper.eq(ProductionManageSaleTask.COL_TASK_TYPE, typeEnum.getKey())
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageSaleTask.COL_ORGANIZATION_ID, organizationId)
                .eq(StringUtils.isNotBlank(sysId), ProductionManageSaleTask.COL_SYS_ID, sysId);
        // 判断搜索框是否为空,为空，则进行高级搜索，不为空，则普通搜索
        if (StringUtils.isBlank(requestVO.getSearch())) {
            String[] dateInterval = LocalDateTimeUtil.substringDate(requestVO.getSaleDate());
            String[] createDateInterval = LocalDateTimeUtil.substringDate(requestVO.getCreateDate());
            boolean isTrue = SaleTaskTypeEnum.DEPARTMENT == typeEnum && StringUtils.isNotBlank(requestVO.getDepartmentName());
            queryWrapper.ge(StringUtils.isNotBlank(dateInterval[0]), ProductionManageSaleTask.COL_SALE_DATE, dateInterval[0])
                    .le(StringUtils.isNotBlank(dateInterval[1]), ProductionManageSaleTask.COL_SALE_DATE, dateInterval[1])
                    .eq(StringUtils.isNotBlank(requestVO.getTaskId()), ProductionManageSaleTask.COL_TASK_ID, requestVO.getTaskId())
                    .eq(StringUtils.isNotBlank(requestVO.getSalesPersonnelName()), ProductionManageSaleTask.COL_SALES_PERSONNEL_NAME, requestVO.getSalesPersonnelName())
                    .eq(StringUtils.isNotBlank(requestVO.getProductName()), ProductionManageSaleTask.COL_PRODUCT_NAME, requestVO.getProductName())
                    .eq(isTrue, ProductionManageSaleTask.COL_DEPARTMENT_NAME, requestVO.getDepartmentName())
                    .ge(StringUtils.isNotBlank(createDateInterval[0]), ProductionManageSaleTask.COL_CREATE_DATE, createDateInterval[0])
                    .le(StringUtils.isNotBlank(createDateInterval[1]), ProductionManageSaleTask.COL_CREATE_DATE, createDateInterval[1]);
        } else {
            queryWrapper.lambda().and(wrapper -> wrapper.or(task -> task.like(ProductionManageSaleTask::getTaskId, requestVO.getSearch()))
                    .or(task -> task.like(ProductionManageSaleTask::getProductName, requestVO.getSearch()))
                    .or(task -> task.like(ProductionManageSaleTask::getTargetSales, requestVO.getSearch()))
                    .or(task -> task.like(ProductionManageSaleTask::getTargetSaleAmount, requestVO.getSearch()))
                    .or(task -> task.like(SaleTaskTypeEnum.DEPARTMENT == typeEnum, ProductionManageSaleTask::getDepartmentName, requestVO.getSearch()))
                    .or(task -> task.like(ProductionManageSaleTask::getTaskRemark, requestVO.getSearch())));
        }

        queryWrapper.orderByDesc(ProductionManageSaleTask.COL_CREATE_DATE);

        // 暂不添加销售任务数据权限，因为涉及到季度和年度任务的数据无法进行数据权限
        // commonUtil.roleDataAuthFilter(authCode, queryWrapper,"createUserId",null);
        return saleTaskMapper.selectPage(page, queryWrapper);
    }

    /**
     * 设置分页参数
     *
     * @param iPage
     * @param <T>
     * @return
     * @throws Exception
     */
    private <T> PageResults<List<T>> setListPagination(IPage iPage) throws SuperCodeException {
        PageResults<List<T>> pageResults = new PageResults();
        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        pageResults.setPagination(pagination);
        return pageResults;
    }

    /**
     * 修改部门任务
     *
     * @param requestVO
     */
    @Transactional
    public void updateDepartment(UpdateDepartmentSaleTaskRequestDTO requestVO) throws SuperCodeException {
        update(requestVO, SaleTaskTypeEnum.DEPARTMENT);
    }

    /**
     * 修改个人任务
     *
     * @param requestVO
     */
    @Transactional
    public void updatePersonal(UpdatePersonalSaleTaskRequestDTO requestVO) throws SuperCodeException {
        update(requestVO, SaleTaskTypeEnum.PERSONAL);
    }

    /**
     * 更新销售任务
     *
     * @param requestVO
     */
    private void update(UpdateDepartmentSaleTaskRequestDTO requestVO, SaleTaskTypeEnum typeEnum) throws SuperCodeException {
        ProductionManageSaleTask saleTask = new ProductionManageSaleTask();

        // 查询当前销售任务是否存在
        QueryWrapper<ProductionManageSaleTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", requestVO.getId())
                .eq(ProductionManageSaleTask.COL_SYS_ID, getSysId())
                .eq(ProductionManageSaleTask.COL_ORGANIZATION_ID, getOrganizationId());
        ProductionManageSaleTask task = saleTaskMapper.selectOne(queryWrapper);
        if (Objects.isNull(task)) {
            throw new SuperCodeException("当前销售任务不存在", 500);
        }

        // 获取销售时间并处理为年月类型
        Date saleDate;
        try {
            saleDate = DateUtils.parseDate(requestVO.getSaleDate(), "yyyy-MM");
        } catch (ParseException e) {
            throw new SuperCodeException("时间类型转换错误", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        UpdatePersonalSaleTaskRequestDTO personalRequestVO = new UpdatePersonalSaleTaskRequestDTO();
        // 判断当前类型是否为个人类型
        if (SaleTaskTypeEnum.PERSONAL == typeEnum) {
            personalRequestVO = (UpdatePersonalSaleTaskRequestDTO) requestVO;
            // v1.7新加内容
            // 判断销售人员是否发生了变更
            String personnelId = personalRequestVO.getSalesPersonnelId();
            if (!task.getSalesPersonnelId().equals(personnelId)) {
                // 获取最新的部门信息
                EmployeeMsgDTO msg = commonUtil.getEmployeeMsg(commonUtil.getSuperToken(), personalRequestVO.getSalesPersonnelId());
                if (Objects.isNull(msg)) {
                    throw new SuperCodeExtException("当前销售人员不隶属任何部门，请检查");
                }
                personalRequestVO.setDepartmentId(Optional.ofNullable(msg.getDepartmentId()).orElse(StringUtils.EMPTY));
                personalRequestVO.setDepartmentName(Optional.of(msg.getDepartmentName()).orElse(StringUtils.EMPTY));
            }
        }

        // 判断当前用户的任务的销售日期和产品名称是否重复, 排除自身
        QueryWrapper<ProductionManageSaleTask> wrapper = new QueryWrapper<>();
        wrapper.eq(ProductionManageSaleTask.COL_SALE_DATE, saleDate)
                .eq(ProductionManageSaleTask.COL_PRODUCT_ID, requestVO.getProductId())
                .eq(typeEnum == SaleTaskTypeEnum.DEPARTMENT, ProductionManageSaleTask.COL_DEPARTMENT_ID, requestVO.getDepartmentId())
                .isNull(typeEnum == SaleTaskTypeEnum.DEPARTMENT, ProductionManageSaleTask.COL_SALES_PERSONNEL_ID)
                .eq((typeEnum == SaleTaskTypeEnum.PERSONAL), ProductionManageSaleTask.COL_SALES_PERSONNEL_ID, personalRequestVO.getSalesPersonnelId())
                .eq(ProductionManageSaleTask.COL_SYS_ID, getSysId())
                .eq(ProductionManageSaleTask.COL_ORGANIZATION_ID, getOrganizationId())
                .ne("id", requestVO.getId());

        Integer count = saleTaskMapper.selectCount(wrapper);
        if (count > 0) {
            String errorMessage = typeEnum == SaleTaskTypeEnum.DEPARTMENT
                    ? "销售日期, 部门, 产品不可重复, 请重新填写"
                    : "销售日期, 产品, 销售人员不可重复, 请重新填写";
            throw new SuperCodeException(errorMessage, 500);
        }

        // TODO 判断是否选择了重量单位，这里暂时不做任何判断

        // 判断任务类型是否为个人类型
        if (SaleTaskTypeEnum.PERSONAL == typeEnum) {
            BeanUtils.copyProperties(personalRequestVO, saleTask);
        } else {
            BeanUtils.copyProperties(requestVO, saleTask);
        }

        try {
            saleTask.setSaleDate(DateUtils.parseDate(requestVO.getSaleDate(), "yyyy-MM"));
        } catch (ParseException e) {
            throw new SuperCodeException("时间转换错误", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        Date now = new Date();
        saleTask.setId(task.getId());
        saleTask.setTaskId(task.getTaskId());
        saleTask.setUpdateDate(now);
        saleTask.setUpdateUserId(getEmployee().getEmployeeId());

        saleTaskMapper.updateById(saleTask);

        // 同步更新销售任务对比数据统计
        comparisonService.update(requestVO, task, typeEnum, saleTask);

    }

    /**
     * 通过任务编号获取任务的相关信息
     *
     * @param id
     * @return
     */
    private ProductionManageSaleTask getById(Long id, SaleTaskTypeEnum typeEnum) throws SuperCodeException {
        // 查询当前销售任务是否存在
        QueryWrapper<ProductionManageSaleTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)
                .eq(ProductionManageSaleTask.COL_TASK_TYPE, typeEnum.getKey())
                .eq(StringUtils.isNotBlank(getSysId()), ProductionManageSaleTask.COL_SYS_ID, getSysId())
                .eq(StringUtils.isNotBlank(getOrganizationId()), ProductionManageSaleTask.COL_ORGANIZATION_ID, getOrganizationId());
        ProductionManageSaleTask task = saleTaskMapper.selectOne(queryWrapper);
        if (Objects.isNull(task)) {
            throw new SuperCodeException("当前销售任务不存在", 500);
        }

        return task;
    }

    /**
     * 获取个人销售任务详细信息
     *
     * @param id
     * @return
     * @throws SuperCodeException
     */
    public PersonalSaleTaskResponseVO getPersonalById(Long id) throws SuperCodeException {
        ProductionManageSaleTask task = getById(id, SaleTaskTypeEnum.PERSONAL);
        PersonalSaleTaskResponseVO responseVO = new PersonalSaleTaskResponseVO();
        BeanUtils.copyProperties(task, responseVO);
        if (Objects.nonNull(task.getSaleDate())) {
            responseVO.setSaleDate(DateFormatUtils.format(task.getSaleDate(), "yyyy-MM"));
            responseVO.setCreateDate(DateFormatUtils.format(task.getCreateDate(), "yyyy-MM-dd HH:mm:ss"));
        }
        getStatistics(task, responseVO);
        return responseVO;
    }

    /**
     * 获取部门销售任务详细信息
     *
     * @param id
     * @return
     * @throws SuperCodeException
     */
    public DepartmentSaleTaskResponseVO getDepartmentById(Long id) throws SuperCodeException {
        ProductionManageSaleTask task = getById(id, SaleTaskTypeEnum.DEPARTMENT);
        DepartmentSaleTaskResponseVO responseVO = new DepartmentSaleTaskResponseVO();
        BeanUtils.copyProperties(task, responseVO);
        if (Objects.nonNull(task.getSaleDate())) {
            responseVO.setCreateDate(DateFormatUtils.format(task.getCreateDate(), "yyyy-MM-dd HH:mm:ss"));
            responseVO.setSaleDate(DateFormatUtils.format(task.getSaleDate(), "yyyy-MM"));
        }
        getStatistics(task, responseVO);
        return responseVO;
    }


    /**
     * 销售任务统计
     *
     * @param resultSet 查询结果集
     * @param requestVO 请求vo
     * @param clazz     返回的结果接受类
     * @param timeEnum  时间枚举
     * @param typeEnum  类型枚举
     * @param <T>
     * @return
     * @throws Exception
     */
    private <T> PageResults<List<T>> listStatistics(String[] resultSet, SaleTaskStatisticsRequestDTO requestVO, Class<T> clazz, SaleTaskTimeEnum timeEnum, SaleTaskTypeEnum typeEnum) throws SuperCodeException {
        // 设置分页
        Page<ProductionManageSaleTask> page = new Page<>(requestVO.getDefaultCurrent(), requestVO.getDefaultPageSize());

        QueryWrapper<ProductionManageSaleTask> queryWrapper = new QueryWrapper<>();

        // 设置查询结果集
        queryWrapper.select(resultSet);

        // 查询指定系统和指定组织下的信息
        queryWrapper.eq(StringUtils.isNotBlank(getSysId()), ProductionManageSaleTask.COL_SYS_ID, getSysId())
                .eq(StringUtils.isNotBlank(getOrganizationId()), ProductionManageSaleTask.COL_ORGANIZATION_ID, getOrganizationId())
                .eq(ProductionManageSaleTask.COL_TASK_TYPE, typeEnum.getKey());

        // 判断search是否为空，如果为空，则进行高级检索
        if (StringUtils.isNotBlank(requestVO.getSearch())) {
            queryWrapper.lambda().and(wrapper ->
                    wrapper.or(task -> task.like(ProductionManageSaleTask::getProductName, requestVO.getSearch()))
                            .or(typeEnum == SaleTaskTypeEnum.PERSONAL, task -> task.like(ProductionManageSaleTask::getSalesPersonnelName, requestVO.getSearch())));
        } else {
            queryWrapper.eq(StringUtils.isNotBlank(requestVO.getYear()), "YEAR(sale_date)", requestVO.getYear())
                    .like(StringUtils.isNotBlank(requestVO.getProductName()), ProductionManageSaleTask.COL_PRODUCT_NAME, requestVO.getProductName())
                    .eq(StringUtils.isNotBlank(requestVO.getQuarter()), "QUARTER(sale_date)", requestVO.getQuarter())
                    .eq(ProductionManageSaleTask.COL_TASK_TYPE, typeEnum.getKey());
        }

        /*
         * 分组规则: 按部门，产品进行分组
         * 排序规则: 排序按照年度，季度降序排序
         */
        List<String> groupBy = new ArrayList<>(5);
        // v1.7新家内容
        groupBy.add(ProductionManageSaleTask.COL_DEPARTMENT_ID);
        groupBy.add(ProductionManageSaleTask.COL_PRODUCT_ID);
        if (timeEnum == SaleTaskTimeEnum.QUARTERLY) {
            groupBy.add("YEAR(sale_date)");
            groupBy.add("QUARTER(sale_date)");
        } else {
            groupBy.add("YEAR(sale_date)");
        }

        String[] orderBy = timeEnum == SaleTaskTimeEnum.QUARTERLY ? new String[]{"YEAR(sale_date)", "QUARTER(sale_date)"} : new String[]{"YEAR(sale_date)"};

        if (typeEnum == SaleTaskTypeEnum.PERSONAL) {
            groupBy.add(3, ProductionManageSaleTask.COL_SALES_PERSONNEL_ID);
        }
        queryWrapper.groupBy(groupBy.toArray(new String[0])).orderByDesc(orderBy);

        IPage<Map<String, Object>> mapIPage = saleTaskMapper.selectMapsPage(page, queryWrapper);

        PageResults<List<T>> pageResult = setListPagination(mapIPage);

        // 判断查询结果集是否为空
        if (CollectionUtils.isEmpty(mapIPage.getRecords())) {
            pageResult.setList(Collections.EMPTY_LIST);
            return pageResult;
        }

        List<T> list = JSONObject.parseArray(JSONObject.toJSONString(mapIPage.getRecords()), clazz);

        // 返回结果数据集
        pageResult.setList(list);

        return pageResult;
    }

    /**
     * 统计部门季度销售任务列表
     */
    public PageResults<List<DepartmentSaleTaskStatisticsVO>> listDepartmentQuarterlyStatistics(SaleTaskStatisticsRequestDTO requestVO) throws SuperCodeException {

        String[] resultSet = {"SUM(target_sale_amount) AS targetSaleAmount", ProductionManageSaleTask.COL_PRODUCT_ID,
                ProductionManageSaleTask.COL_PRODUCT_NAME, ProductionManageSaleTask.COL_DEPARTMENT_ID, ProductionManageSaleTask.COL_DEPARTMENT_NAME, "YEAR(sale_date) AS year", "QUARTER(sale_date) AS quarter"};

        PageResults<List<DepartmentSaleTaskStatisticsVO>> pageResult = listStatistics(resultSet, requestVO, DepartmentSaleTaskStatisticsVO.class, SaleTaskTimeEnum.QUARTERLY, SaleTaskTypeEnum.DEPARTMENT);
        if (pageResult.getPagination().getTotal() == 0) {
            return pageResult;
        }
        List<DepartmentSaleTaskStatisticsVO> list = pageResult.getList();
        list.forEach(statistics -> {
            try {
                statisticYearOrQuarterOrderProduct(statistics, null);
            } catch (SuperCodeException e) {
                e.printStackTrace();
            }

            StringBuilder sb = new StringBuilder();
            sb.append(statistics.getYear())
                    .append(MID_LINE)
                    .append(statistics.getQuarter())
                    .append(MID_LINE)
                    .append(statistics.getProductId())
                    .append(MID_LINE)
                    .append(statistics.getDepartmentId());

            statistics.setId(sb.toString());
            statistics.setQuarter(String.format(YEAR, statistics.getYear()) + String.format(QUARTER, statistics.getQuarter()));
        });

        return pageResult;
    }

    /**
     * 统计部门年度销售任务列表
     */
    public PageResults<List<DepartmentSaleTaskStatisticsVO>> listDepartmentYearStatistics(SaleTaskStatisticsRequestDTO requestVO) throws SuperCodeException {
        String[] resultSet = {"SUM(target_sale_amount) AS targetSaleAmount", ProductionManageSaleTask.COL_PRODUCT_ID, ProductionManageSaleTask.COL_PRODUCT_NAME,
                ProductionManageSaleTask.COL_DEPARTMENT_ID, ProductionManageSaleTask.COL_DEPARTMENT_NAME, "YEAR(sale_date) AS year"};
        PageResults<List<DepartmentSaleTaskStatisticsVO>> pageResult = listStatistics(resultSet, requestVO, DepartmentSaleTaskStatisticsVO.class, SaleTaskTimeEnum.YEAR, SaleTaskTypeEnum.DEPARTMENT);
        if (pageResult.getPagination().getTotal() == 0) {
            return pageResult;
        }
        List<DepartmentSaleTaskStatisticsVO> list = pageResult.getList();
        list.forEach(statistics -> {
            try {
                statisticYearOrQuarterOrderProduct(statistics, null);
            } catch (SuperCodeException e) {
                e.printStackTrace();
            }

            StringBuilder sb = new StringBuilder();
            sb.append(statistics.getYear())
                    .append(MID_LINE)
                    .append(statistics.getProductId())
                    .append(MID_LINE)
                    .append(statistics.getDepartmentId());
            statistics.setId(sb.toString());
            statistics.setYear(String.format(YEAR, statistics.getYear()));
        });

        return pageResult;
    }

    /**
     * 统计个人季度销售任务列表
     */
    public PageResults<List<PersonalSaleTaskStatisticsVO>> listPersonalQuarterlyStatistics(SaleTaskStatisticsRequestDTO requestVO) throws SuperCodeException {
        String[] resultSet = {"SUM(target_sale_amount) AS targetSaleAmount", ProductionManageSaleTask.COL_PRODUCT_ID,
                ProductionManageSaleTask.COL_PRODUCT_NAME, ProductionManageSaleTask.COL_DEPARTMENT_ID, ProductionManageSaleTask.COL_DEPARTMENT_NAME,
                "YEAR(sale_date) AS year", "QUARTER(sale_date) AS quarter",
                ProductionManageSaleTask.COL_SALES_PERSONNEL_ID, ProductionManageSaleTask.COL_SALES_PERSONNEL_NAME};
        PageResults<List<PersonalSaleTaskStatisticsVO>> pageResult = listStatistics(resultSet, requestVO, PersonalSaleTaskStatisticsVO.class, SaleTaskTimeEnum.QUARTERLY, SaleTaskTypeEnum.PERSONAL);

        if (pageResult.getPagination().getTotal() == 0) {
            return pageResult;
        }
        List<PersonalSaleTaskStatisticsVO> list = pageResult.getList();

        list.forEach(statistics -> {
            try {
                statisticYearOrQuarterOrderProduct(statistics, statistics.getSalesPersonnelId());
            } catch (SuperCodeException e) {
                e.printStackTrace();
            }

            StringBuilder sb = new StringBuilder();
            sb.append(statistics.getYear())
                    .append(MID_LINE)
                    .append(statistics.getQuarter())
                    .append(MID_LINE)
                    .append(statistics.getSalesPersonnelId())
                    .append(MID_LINE)
                    .append(statistics.getProductId())
                    .append(MID_LINE)
                    .append(statistics.getDepartmentId());
            statistics.setId(sb.toString());
            statistics.setQuarter(String.format(YEAR, statistics.getYear()) + String.format(QUARTER, statistics.getQuarter()));
        });

        return pageResult;
    }

    /**
     * 统计个人年度销售任务列表
     */
    public PageResults<List<PersonalSaleTaskStatisticsVO>> listPersonalYearStatistics(SaleTaskStatisticsRequestDTO requestVO) throws SuperCodeException {

        String[] resultSet = {"SUM(target_sale_amount) AS targetSaleAmount", ProductionManageSaleTask.COL_PRODUCT_ID,
                ProductionManageSaleTask.COL_PRODUCT_NAME, ProductionManageSaleTask.COL_DEPARTMENT_ID, ProductionManageSaleTask.COL_DEPARTMENT_NAME,
                "YEAR(sale_date) AS year", ProductionManageSaleTask.COL_SALES_PERSONNEL_ID, ProductionManageSaleTask.COL_SALES_PERSONNEL_NAME};

        PageResults<List<PersonalSaleTaskStatisticsVO>> pageResult = listStatistics(resultSet, requestVO, PersonalSaleTaskStatisticsVO.class, SaleTaskTimeEnum.YEAR, SaleTaskTypeEnum.PERSONAL);

        if (pageResult.getPagination().getTotal() == 0) {
            return pageResult;
        }

        List<PersonalSaleTaskStatisticsVO> list = pageResult.getList();
        list.forEach(statistics -> {
            try {
                statisticYearOrQuarterOrderProduct(statistics, statistics.getSalesPersonnelId());
            } catch (SuperCodeException e) {
                e.printStackTrace();
            }

            StringBuilder sb = new StringBuilder();
            sb.append(statistics.getYear())
                    .append(MID_LINE)
                    .append(statistics.getSalesPersonnelId())
                    .append(MID_LINE)
                    .append(statistics.getProductId())
                    .append(MID_LINE)
                    .append(statistics.getDepartmentId());
            statistics.setId(sb.toString());

            statistics.setYear(String.format(YEAR, statistics.getYear()));
        });

        return pageResult;
    }

    /**
     * 获取部门销售任务月度列表
     *
     * @param requestVO
     * @return
     */
    public PageResults<List<DepartmentSaleTaskMonthResponseVO>> listForDepartmentMonth(ProductManageSaleTaskRequestDTO requestVO) throws SuperCodeException {
        IPage<ProductionManageSaleTask> iPage = list(requestVO, SaleTaskTypeEnum.DEPARTMENT,"departmentSalesGoals");
        PageResults<List<DepartmentSaleTaskMonthResponseVO>> pageResult = setListPagination(iPage);
        List<ProductionManageSaleTask> list = iPage.getRecords();

        List<DepartmentSaleTaskMonthResponseVO> result;
        if (CollectionUtils.isEmpty(list)) {
            result = Collections.EMPTY_LIST;
        } else {
            result = new ArrayList<>(list.size());
            list.forEach(task -> {
                DepartmentSaleTaskMonthResponseVO taskMonthResponseVO = new DepartmentSaleTaskMonthResponseVO();
                BeanUtils.copyProperties(task, taskMonthResponseVO);
                taskMonthResponseVO.setCreateDate(DateFormatUtils.format(task.getCreateDate(), "yyyy-MM-dd HH:mm:ss"));
                taskMonthResponseVO.setSaleDate(DateFormatUtils.format(task.getSaleDate(), "yyyy年MM月"));
                result.add(taskMonthResponseVO);
            });
        }
        pageResult.setList(result);

        return pageResult;
    }

    /**
     * 编辑部门任务反馈信息
     *
     * @param requestDTO
     */
    public void updateRemark(UpdateSaleTaskRemarkRequestDTO requestDTO) throws SuperCodeException {
        QueryWrapper<ProductionManageSaleTask> queryWrapper = queryTemplate(ProductionManageSaleTask.class);
        queryWrapper.eq("id", requestDTO.getId());
        ProductionManageSaleTask productionManageSaleTask = saleTaskMapper.selectOne(queryWrapper);
        if (Objects.isNull(productionManageSaleTask)) {
            throw new SuperCodeException("不存在此销售任务信息", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        ProductionManageSaleTask saleTask = new ProductionManageSaleTask();
        saleTask.setId(requestDTO.getId());
        saleTask.setTaskRemark(requestDTO.getTaskRemark());

        saleTaskMapper.updateById(saleTask);
    }

    private <T extends SaleTaskResponseVO> void getStatistics(ProductionManageSaleTask task, T responseVO) {
        // 按产品id, 销售时间, 部门id获取实际销量和销售额
        OrderProductStatisticsVO orderProductStatisticsVO = orderProductMapper.statisticsOrderProduct(task);
        // 获取目标销量
        Float targetSaleAmount = task.getTargetSaleAmount();
        // 获取完成销售额
        Float completedSaleAmount = orderProductStatisticsVO.getCompletedSaleAmount();
        // 获取完成销量
        Integer completedSales = orderProductStatisticsVO.getCompletedSales();

        // 计算百分比
        float percent = computedPercent(completedSaleAmount.floatValue(), targetSaleAmount.floatValue(), 2);
        responseVO.setCompletedSaleAmount(completedSaleAmount);
        responseVO.setCompletionPercentage(percent);
        responseVO.setCompletedSales(completedSales);
    }

    /**
     * 计算百分比， 后续可抽离成工具方法
     *
     * @param num1
     * @param num2
     * @param scale
     * @return
     */
    private float computedPercent(float num1, float num2, int scale) {
        BigDecimal b1 = new BigDecimal(num1);
        BigDecimal b2 = new BigDecimal(num2);

        return b1.multiply(BigDecimal.valueOf(100)).divide(b2, scale, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * 统计年度/季度的完成金额
     *
     * @param statistics
     * @throws SuperCodeException
     */
    private void statisticYearOrQuarterOrderProduct(DepartmentSaleTaskStatisticsVO statistics, String salesPersonnelId) throws SuperCodeException {
        String sysId = getSysId();
        String organizationId = getOrganizationId();
        OrderProductStatisticsVO orderProduct = orderProductMapper.statisticYearOrQuarterOrderProduct(statistics, sysId, organizationId, salesPersonnelId);
        // 获取目标销售额
        Float targetSaleAmount = statistics.getTargetSaleAmount();
        // 获取完成销售额
        Float completedSaleAmount = orderProduct.getCompletedSaleAmount();
        // 计算百分比
        float percent = computedPercent(completedSaleAmount.floatValue(), targetSaleAmount.floatValue(), 2);

        statistics.setCompletedSaleAmount(completedSaleAmount);
        statistics.setCompletionPercentage(percent);
    }

    /**
     * 部门销售任务导出
     *
     * @param requestDTO
     * @param response
     */
    public void departmentExport(ProductManageSaleTaskRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        ArrayList<String> idList = requestDTO.getIdList();
        List<DepartmentSaleTaskResponseVO> list;
        // 如果idList为空则全部导出，不为空，则导出指定
        if (CollectionUtils.isEmpty(idList)) {
            setPage(requestDTO);
            RestResult<PageResults<List<DepartmentSaleTaskResponseVO>>> pageResultsRestResult = listForDepartment(requestDTO);
            list = pageResultsRestResult.getResults().getList();
        } else {
            QueryWrapper<ProductionManageSaleTask> queryWrapper = commonUtil.queryTemplate(ProductionManageSaleTask.class);
            queryWrapper.and(query -> query.in("Id", idList));

            List<ProductionManageSaleTask> saleTasks = saleTaskMapper.selectList(queryWrapper);
            list = new ArrayList<>(saleTasks.size());
            saleTasks.forEach(task -> {
                DepartmentSaleTaskResponseVO responseVO = new DepartmentSaleTaskResponseVO();
                BeanUtils.copyProperties(task, responseVO);
                // 设置任务制定时间为最近修改时间
                responseVO.setCreateDate(DateFormatUtils.format(task.getUpdateDate(), "yyyy-MM-dd HH:mm:ss"));
                // 设置销售时间
                responseVO.setSaleDate(DateFormatUtils.format(task.getSaleDate(), "yyyy-MM"));
                // 统计
                getStatistics(task, responseVO);
                list.add(responseVO);
            });

        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "部门销售任务列表", response);
    }


    /**
     * 部门季度销售任务导出
     *
     * @param requestDTO
     * @param response
     */
    public void departmentQuarterExport(SaleTaskStatisticsRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<String> idList = requestDTO.getIdList();
        List<DepartmentSaleTaskStatisticsVO> list;
        // idList为空则全部导出，不为空则导出指定数据
        if (CollectionUtils.isEmpty(idList)) {
            setPage(requestDTO);
            PageResults<List<DepartmentSaleTaskStatisticsVO>> listPageResults = listDepartmentQuarterlyStatistics(requestDTO);
            list = listPageResults.getList();
        } else {
            list = departmentQuarterExportByUniqueIds(idList);
        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "部门季度销售任务列表", response);
    }

    /**
     * 部门年度销售任务导出
     *
     * @param requestDTO
     * @param response
     * @throws Exception
     */
    public void departmentYearExport(SaleTaskStatisticsRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<String> idList = requestDTO.getIdList();
        List<DepartmentSaleTaskStatisticsVO> list;
        // idList为空则全部导出，不为空则导出指定数据
        if (CollectionUtils.isEmpty(idList)) {
            setPage(requestDTO);
            PageResults<List<DepartmentSaleTaskStatisticsVO>> listPageResults = listDepartmentYearStatistics(requestDTO);
            list = listPageResults.getList();
        } else {
            list = departmentYearExportByUniqueIds(idList);
        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "部门年度销售任务列表", response);
    }

    /**
     * 个人销售任务导出
     *
     * @param requestDTO
     * @param response
     */
    public void personalExport(ProductManageSaleTaskRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        ArrayList<String> idList = requestDTO.getIdList();
        List<PersonalSaleTaskResponseVO> list;
        // idList为空时，导出全部，不为空，导出指定
        if (CollectionUtils.isEmpty(idList)) {
            setPage(requestDTO);
            RestResult<PageResults<List<PersonalSaleTaskResponseVO>>> restResult = listForPersonal(requestDTO);
            list = restResult.getResults().getList();
        } else {
            QueryWrapper<ProductionManageSaleTask> queryWrapper = commonUtil.queryTemplate(ProductionManageSaleTask.class);
            queryWrapper.and(query -> query.in("Id", idList));
            List<ProductionManageSaleTask> saleTasks = saleTaskMapper.selectList(queryWrapper);
            list = new ArrayList<>(saleTasks.size());
            saleTasks.forEach(task -> {
                PersonalSaleTaskResponseVO responseVO = new PersonalSaleTaskResponseVO();
                BeanUtils.copyProperties(task, responseVO);
                // 设置任务制定时间为最近修改时间
                responseVO.setCreateDate(DateFormatUtils.format(task.getUpdateDate(), "yyyy-MM-dd HH:mm:ss"));
                // 设置销售时间
                responseVO.setSaleDate(DateFormatUtils.format(task.getSaleDate(), "yyyy-MM"));
                // 统计
                getStatistics(task, responseVO);
                list.add(responseVO);
            });
        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "个人销售任务", response);
    }

    /**
     * 设置导出分页大小
     *
     * @param requestDTO
     * @param <T>
     * @return
     */
    private <T extends DaoSearch> T setPage(T requestDTO) {
        requestDTO.setCurrent(1);
        requestDTO.setPageSize(commonUtil.getExportNumber());
        return requestDTO;
    }

    /**
     * 个人季度销售任务导出
     *
     * @param requestDTO
     * @param response
     * @throws SuperCodeException
     */
    public void personalQuarterExport(SaleTaskStatisticsRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<String> idList = requestDTO.getIdList();
        List<PersonalSaleTaskStatisticsVO> list;

        if (CollectionUtils.isEmpty(idList)) {
            setPage(requestDTO);
            PageResults<List<PersonalSaleTaskStatisticsVO>> listPageResults = listPersonalQuarterlyStatistics(requestDTO);
            list = listPageResults.getList();
        } else {
            list = personalQuarterExportByUniqueIds(idList);
        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "个人季度销售任务列表", response);
    }

    /**
     * 个人年度销售任务导出
     *
     * @param requestDTO
     * @param response
     */
    public void personalYearExport(SaleTaskStatisticsRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<String> idList = requestDTO.getIdList();
        List<PersonalSaleTaskStatisticsVO> list;

        if (CollectionUtils.isEmpty(idList)) {
            setPage(requestDTO);
            PageResults<List<PersonalSaleTaskStatisticsVO>> listPageResults = listPersonalYearStatistics(requestDTO);
            list = listPageResults.getList();
        } else {
            list = personalYearExportByUniqueIds(idList);
        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "个人年度销售任务列表", response);
    }

    /**
     * 通过唯一id来获取指定导出的部门季度销售任务数据
     * id的组成格式为 year + "-" + quarter + "-" + productId + "-" + departmentId, 例如：2019-1
     */
    public List<DepartmentSaleTaskStatisticsVO> departmentQuarterExportByUniqueIds(List<String> uniqueIds) throws SuperCodeException {
        QueryWrapper<DepartmentSaleTaskStatisticsVO> queryWrapper = commonUtil.queryTemplate(DepartmentSaleTaskStatisticsVO.class);
        queryWrapper.and(condition -> {
            uniqueIds.forEach(id -> {
                String[] ids = CustomArrayUtil.str2Arrays(id, MID_LINE, 4);
                condition.or(wrapper -> wrapper
                        .eq("YEAR(sale_date)", ids[0])
                        .eq("QUARTER(sale_date)", ids[1])
                        .eq(ProductionManageSaleTask.COL_PRODUCT_ID, ids[2])
                        .eq(ProductionManageSaleTask.COL_DEPARTMENT_ID, ids[3])
                );
            });
            return condition;
        });
        queryWrapper.groupBy(ProductionManageSaleTask.COL_DEPARTMENT_ID, ProductionManageSaleTask.COL_PRODUCT_ID, "YEAR(sale_date)", "QUARTER(sale_date)");
        queryWrapper.orderByDesc("YEAR(sale_date)", "QUARTER(sale_date)");

        List<DepartmentSaleTaskStatisticsVO> list = saleTaskMapper.lisDepartmentByUniqueIds(queryWrapper);
        for (DepartmentSaleTaskStatisticsVO statistics : list) {
            statisticYearOrQuarterOrderProduct(statistics, null);
            statistics.setQuarter(String.format(YEAR, statistics.getYear()) + String.format(QUARTER, statistics.getQuarter()));
        }

        return list;
    }

    /**
     * 通过唯一id来获取指定导出的部门年度销售任务数据
     * id的组成格式为 year + "-" + productId + "-" + departmentId,例如：2019
     */
    public List<DepartmentSaleTaskStatisticsVO> departmentYearExportByUniqueIds(List<String> uniqueIds) throws SuperCodeException {
        QueryWrapper<DepartmentSaleTaskStatisticsVO> queryWrapper = commonUtil.queryTemplate(DepartmentSaleTaskStatisticsVO.class);
        queryWrapper.and(condition -> {
            uniqueIds.forEach(id -> {
                String[] ids = CustomArrayUtil.str2Arrays(id, MID_LINE, 4);

                condition.or(wrapper -> wrapper
                        .eq("YEAR(sale_date)", ids[0])
                        .eq(ProductionManageSaleTask.COL_PRODUCT_ID, ids[1])
                        .eq(ProductionManageSaleTask.COL_DEPARTMENT_ID, ids[2])
                );
            });
            return condition;
        });

        queryWrapper.groupBy(ProductionManageSaleTask.COL_DEPARTMENT_ID, ProductionManageSaleTask.COL_PRODUCT_ID, "YEAR(sale_date)");
        queryWrapper.orderByDesc("YEAR(sale_date)");

        List<DepartmentSaleTaskStatisticsVO> list = saleTaskMapper.lisDepartmentByUniqueIds(queryWrapper);
        for (DepartmentSaleTaskStatisticsVO statistics : list) {
            statisticYearOrQuarterOrderProduct(statistics, null);
            statistics.setYear(String.format(YEAR, statistics.getYear()));
        }

        return list;
    }

    /**
     * 通过唯一id来获取指定导出的个人季度销售任务数据
     * id的组成格式为 year + "-" + quarter + "-" + salesPersonnelId, 例如：2019-1-123456789876asda
     */
    public List<PersonalSaleTaskStatisticsVO> personalQuarterExportByUniqueIds(List<String> uniqueIds) throws SuperCodeException {
        QueryWrapper<PersonalSaleTaskStatisticsVO> queryWrapper = commonUtil.queryTemplate(PersonalSaleTaskStatisticsVO.class);
        queryWrapper.and(condition -> {
            uniqueIds.forEach(id -> {
                String[] ids = CustomArrayUtil.str2Arrays(id, MID_LINE, 4);
                condition.or(wrapper -> wrapper
                        .eq("YEAR(sale_date)", ids[0])
                        .eq("QUARTER(sale_date)", ids[1])
                        .eq(ProductionManageSaleTask.COL_SALES_PERSONNEL_ID, ids[2])
                        .eq(ProductionManageSaleTask.COL_PRODUCT_ID, ids[3])
                        .eq(ProductionManageSaleTask.COL_DEPARTMENT_ID, ids[4])
                );
            });
            return condition;
        });

        queryWrapper.groupBy(ProductionManageSaleTask.COL_DEPARTMENT_ID, ProductionManageSaleTask.COL_PRODUCT_ID, ProductionManageSaleTask.COL_SALES_PERSONNEL_ID, "YEAR(sale_date)", "QUARTER(sale_date)");
        queryWrapper.orderByDesc("YEAR(sale_date)", "QUARTER(sale_date)");

        List<PersonalSaleTaskStatisticsVO> list = saleTaskMapper.listPersonalByUniqueIds(queryWrapper);

        for (PersonalSaleTaskStatisticsVO statistics : list) {
            statisticYearOrQuarterOrderProduct(statistics, statistics.getSalesPersonnelId());
            statistics.setQuarter(String.format(YEAR, statistics.getYear()) + String.format(QUARTER, statistics.getQuarter()));
        }

        return list;
    }

    /**
     * 通过唯一id来获取指定导出的个人年度销售任务数据
     * id的组成格式为 year + "-" + salesPersonnelId,例如：2019-1234567892134cs
     */
    public List<PersonalSaleTaskStatisticsVO> personalYearExportByUniqueIds(List<String> uniqueIds) throws SuperCodeException {
        QueryWrapper<PersonalSaleTaskStatisticsVO> queryWrapper = commonUtil.queryTemplate(PersonalSaleTaskStatisticsVO.class);
        queryWrapper.and(condition -> {
            uniqueIds.forEach(id -> {
                String[] ids = CustomArrayUtil.str2Arrays(id, MID_LINE, 4);
                condition.or(wrapper -> wrapper
                        .eq("YEAR(sale_date)", ids[0])
                        .eq(ProductionManageSaleTask.COL_SALES_PERSONNEL_ID, ids[1])
                        .eq(ProductionManageSaleTask.COL_PRODUCT_ID, ids[2])
                        .eq(ProductionManageSaleTask.COL_DEPARTMENT_ID, ids[3])
                );
            });
            return condition;
        });

        queryWrapper.groupBy(ProductionManageSaleTask.COL_DEPARTMENT_ID, ProductionManageSaleTask.COL_PRODUCT_ID, ProductionManageSaleTask.COL_SALES_PERSONNEL_ID, "YEAR(sale_date)");
        queryWrapper.orderByDesc("YEAR(sale_date)");

        List<PersonalSaleTaskStatisticsVO> list = saleTaskMapper.listPersonalByUniqueIds(queryWrapper);

        for (PersonalSaleTaskStatisticsVO statistics : list) {
            statisticYearOrQuarterOrderProduct(statistics, statistics.getSalesPersonnelId());
            statistics.setYear(String.format(YEAR, statistics.getYear()));
        }

        return list;
    }

    public List<DepartmentSaleTaskResponseVO> listTasksForDepartment(ProductManageSaleTaskRequestDTO requestVO) throws SuperCodeException {
        IPage<ProductionManageSaleTask> iPage = list(requestVO, SaleTaskTypeEnum.DEPARTMENT,"departmentSalesGoals");

        List<ProductionManageSaleTask> list = iPage.getRecords();

        List<DepartmentSaleTaskResponseVO> tasks = new ArrayList<>(list.size());
        list.forEach(task -> {
            DepartmentSaleTaskResponseVO responseVO = new DepartmentSaleTaskResponseVO();
            BeanUtils.copyProperties(task, responseVO);
            // 设置任务制定时间为最近修改时间
            responseVO.setCreateDate(DateFormatUtils.format(task.getUpdateDate(), "yyyy-MM-dd HH:mm:ss"));
            // 设置销售时间
            responseVO.setSaleDate(DateFormatUtils.format(task.getSaleDate(), "yyyy-MM"));
            // 统计
            getStatistics(task, responseVO);
            tasks.add(responseVO);
        });
        return tasks;
    }

    /**
     * 获取指定时间区间内的部门销售任务信息, 过滤掉部门为空或为''的脏数据
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     */
    public List<ProductionManageStatisticsSaleTargetData> listDepBySAEDate(String sysId, String organizationId) {
        QueryWrapper<ProductionManageSaleTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), ProductionManageSaleTask.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageSaleTask.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageSaleTask.COL_TASK_TYPE, SaleTaskTypeEnum.DEPARTMENT.getKey())
                .isNotNull(ProductionManageSaleTask.COL_DEPARTMENT_ID)
                .ne(ProductionManageSaleTask.COL_DEPARTMENT_ID, "")
                .groupBy("DATE_FORMAT(sale_date, '%Y-%m')", ProductionManageSaleTask.COL_DEPARTMENT_ID, ProductionManageSaleTask.COL_PRODUCT_ID);
        return saleTaskMapper.listByStartAndEndDate(queryWrapper);
    }

    /**
     * 获取指定时间区间内的个人销售任务信息, 过滤掉销售人员为空或为''的脏数据
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     */
    public List<ProductionManageStatisticsSaleTargetData> listPerBySAEDate(String sysId, String organizationId) {
        QueryWrapper<ProductionManageSaleTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), ProductionManageSaleTask.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageSaleTask.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageSaleTask.COL_TASK_TYPE, SaleTaskTypeEnum.PERSONAL.getKey())
                .isNotNull(ProductionManageSaleTask.COL_SALES_PERSONNEL_ID)
                .ne(ProductionManageSaleTask.COL_SALES_PERSONNEL_ID, "")
                .groupBy("DATE_FORMAT(sale_date, '%Y-%m')", ProductionManageSaleTask.COL_PRODUCT_ID, ProductionManageSaleTask.COL_SALES_PERSONNEL_ID);
        return saleTaskMapper.listByStartAndEndDate(queryWrapper);
    }
}