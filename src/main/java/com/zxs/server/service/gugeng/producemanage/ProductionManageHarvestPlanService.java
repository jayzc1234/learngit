package com.zxs.server.service.gugeng.producemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.util.*;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.*;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManageHarvestPlan;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManagePlanInformDepartment;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductionManageHarvestPlanMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductionManagePlanInformDepartmentMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.common.MessageInformService;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.SearchHarvestPlanResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.SearchHarvestPlanWithInformResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.SearchPlanInformDepartmentResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.SearchProductionForecastResponseVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.HARVEST_PLAN;
import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.YIELD_FORECAST;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.OLD_CREATE_USER_ID;


/**
 * <p>
 * 采收计划服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-06-13
 */
@Service
public class ProductionManageHarvestPlanService extends ServiceImpl<ProductionManageHarvestPlanMapper, ProductionManageHarvestPlan> {


    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private ProductionManagePlanInformDepartmentMapper informDepartmentMapper;

    @Autowired
    private MessageInformService messageInformService;

    /**
     * 新增采收计划
     *
     * @param requestDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(AddHarvestPlanAndPlanInformRequestDTO requestDTO) throws SuperCodeException {
        if (StringUtils.isBlank(requestDTO.getHarvestNo())) {
            requestDTO.setHarvestNo(numberGenerator.getSerialNumber(3, RedisKey.HARVEST_PLAN_NO_KEY));
        } else {
            // TODO 是否有必要校验是否包含相同批次信息
        }

        // 校验计划编号是否重复
        Integer count = query()
                .eq(ProductionManageHarvestPlan.COL_HARVEST_NO, requestDTO.getHarvestNo())
                .eq(ProductionManageHarvestPlan.COL_SYS_ID, commonUtil.getSysId())
                .eq(ProductionManageHarvestPlan.COL_ORGANIZATION_ID, commonUtil.getOrganizationId())
                .count();
        CustomAssert.isNotGreaterThanCustomNumber(count, 0, "采收计划编号已存在，请重新输入");
        // 判断是否选择了通知部门，如果选择，通知部门信息是否为空
        List<AddPlanInformDepartmentRequestDTO> informs = validRequestDTO(requestDTO);

        // 新增采收计划
        Employee employee = commonUtil.getEmployee();
        List<AddHarvestPlanRequestDTO> planRequestDTOS = requestDTO.getHarvestPlans();

        // 生成唯一id
        String uuid = commonUtil.getUUID();

        for (AddHarvestPlanRequestDTO request : planRequestDTOS) {
            ProductionManageHarvestPlan harvestPlan = new ProductionManageHarvestPlan();
            BeanUtils.copyProperties(request, harvestPlan);

            harvestPlan.setHarvestNo(requestDTO.getHarvestNo());
            harvestPlan.setCreateUserId(employee.getEmployeeId());
            harvestPlan.setCreateUserName(employee.getName());
            harvestPlan.setUpdateUserId(employee.getEmployeeId());
            harvestPlan.setUpdateUserName(employee.getName());

            harvestPlan.setCreateDate(LocalDateTime.now());
            harvestPlan.setUpdateDate(LocalDateTime.now());
            harvestPlan.setHarvestDate(LocalDateTimeUtil.parseStr2LocalDateTime(request.getHarvestDate()));
            harvestPlan.setSysId(commonUtil.getSysId());
            harvestPlan.setOrganizationId(commonUtil.getOrganizationId());
            harvestPlan.setPlanUniqueId(uuid);
            harvestPlan.setAuthDepartmentId(employee.getDepartmentId());
            baseMapper.insert(harvestPlan);
        }

        // 新增通知部门信息
        addPlanInformDepartment(informs, uuid);
    }

    /**
     * 编辑采收计划
     *
     * @param requestDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(UpdateHarvestPlanAndPlanInformRequestDTO requestDTO) throws SuperCodeException {
        // 判断是否选择了通知部门，如果选择，通知部门信息是否为空
        List<AddPlanInformDepartmentRequestDTO> informs = validRequestDTO(requestDTO);

        // 编辑采收计划
        ProductionManageHarvestPlan harvestPlan = new ProductionManageHarvestPlan();
        AddHarvestPlanRequestDTO requestPlan = requestDTO.getHarvestPlans().get(0);
        BeanUtils.copyProperties(requestPlan, harvestPlan);

        harvestPlan.setUpdateUserId(commonUtil.getEmployee().getEmployeeId());
        harvestPlan.setUpdateUserName(commonUtil.getEmployee().getName());
        harvestPlan.setUpdateDate(LocalDateTime.now());
        harvestPlan.setHarvestDate(LocalDateTimeUtil.parseStr2LocalDateTime(requestPlan.getHarvestDate()));
        harvestPlan.setId(requestDTO.getId());

        baseMapper.updateById(harvestPlan);

        // 获取计划唯一id
        String planUniqueId = requestDTO.getPlanUniqueId();

        // 移除原先所有通知消息内容
        QueryWrapper<ProductionManagePlanInformDepartment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProductionManagePlanInformDepartment.COL_PLAN_UNIQUE_ID, planUniqueId);
        List<ProductionManagePlanInformDepartment> departments = informDepartmentMapper.selectList(queryWrapper);

        informDepartmentMapper.delete(queryWrapper);

        // 添加部门消息
        addPlanInformDepartment(informs, planUniqueId);

        // 新增部门成功后，新增或者修改部门消息时进行消息通知
        if (CollectionUtils.isEmpty(departments)) {
            return;
        }

        Map<String, String> departmentMap = departments.stream()
                .collect(Collectors.toMap(ProductionManagePlanInformDepartment::getDepartmentId,
                        ProductionManagePlanInformDepartment::getInformContent));
        informs.forEach(inform -> {
            String departmentId = inform.getDepartmentId();
            String departmentName = inform.getDepartmentName();
            String informContent = inform.getInformContent();
            String oldContent = departmentMap.get(departmentId);
            boolean isTrue = StringUtils.isBlank(oldContent) || !oldContent.equals(informContent);
            if (isTrue) {
                messageInformService.sendOrgMessageToAllPartmentUser(departmentId,
                        departmentName, informContent, 2, 1, 1);
            }
        });
    }

    /**
     * 校验请求dto是否满足条件
     *
     * @param requestDTO
     * @return
     * @throws SuperCodeException
     */
    private List<AddPlanInformDepartmentRequestDTO> validRequestDTO(AddHarvestPlanAndPlanInformRequestDTO requestDTO) throws SuperCodeException {
        List<AddPlanInformDepartmentRequestDTO> informs = Optional.ofNullable(requestDTO.getPlanInformDepartments()).orElse(Collections.emptyList());

        long count = informs.stream()
                .filter(inform -> StringUtils.isBlank(inform.getInformContent()) && StringUtils.isNotBlank(inform.getDepartmentId())).count();
        if (count > 0) {
            throw new SuperCodeException("消息通知内容不可为空，请填写");
        }

        return informs;
    }

    /**
     * 添加部门消息内容
     *
     * @param informs
     */
    private void addPlanInformDepartment(List<AddPlanInformDepartmentRequestDTO> informs, String uuid) {
        // 添加通知消息内容
        if (CollectionUtils.isNotEmpty(informs)) {
            informs.forEach(inform -> {
                ProductionManagePlanInformDepartment informDepartment = new ProductionManagePlanInformDepartment();
                BeanUtils.copyProperties(inform, informDepartment);
                informDepartment.setPlanUniqueId(uuid);
                informDepartmentMapper.insert(informDepartment);
                messageInformService.sendOrgMessageToAllPartmentUser(informDepartment.getDepartmentId(), informDepartment.getDepartmentName(), informDepartment.getInformContent(), 2, 1, 1);
            });
        }
    }

    public PageResults<List<SearchHarvestPlanResponseVO>> list(SearchHarvestPlanRequestDTO requestDTO) throws SuperCodeException {
        Page<ProductionManageHarvestPlan> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());

        QueryWrapper<ProductionManageHarvestPlan> queryWrapper = commonUtil.queryTemplate(ProductionManageHarvestPlan.class);
        queryWrapper.eq(ProductionManageHarvestPlan.COL_DELETE_OR_NOT, DeleteOrNotEnum.NOT_DELETED.getKey());
        // 如果search为空则进行高级检索，不为空则普通检索
        if (StringUtils.isNotBlank(requestDTO.getSearch())) {
            queryWrapper.lambda().and(wrapper -> wrapper.or(plan -> plan.like(ProductionManageHarvestPlan::getCreateUserName, requestDTO.getSearch()))
                    .or(plan -> plan.like(ProductionManageHarvestPlan::getHarvestNo, requestDTO.getSearch()))
                    .or(plan -> plan.like(ProductionManageHarvestPlan::getPlantBatchName, requestDTO.getSearch()))
                    .or(plan -> plan.like(ProductionManageHarvestPlan::getProductName, requestDTO.getSearch()))
                    .or(plan -> plan.like(ProductionManageHarvestPlan::getGreenhouseName, requestDTO.getSearch())));
        } else {
            String[] dateInterval = LocalDateTimeUtil.substringDate(requestDTO.getHarvestDate());

            queryWrapper.like(StringUtils.isNotBlank(requestDTO.getPlantBatchName()), ProductionManageHarvestPlan.COL_PLANT_BATCH_NAME, requestDTO.getPlantBatchName())
                    .like(StringUtils.isNotBlank(requestDTO.getGreenhouseName()), ProductionManageHarvestPlan.COL_GREENHOUSE_NAME, requestDTO.getGreenhouseName())
                    .ge(StringUtils.isNotBlank(dateInterval[0]), ProductionManageHarvestPlan.COL_HARVEST_DATE, dateInterval[0])
                    .le(StringUtils.isNotBlank(dateInterval[1]), ProductionManageHarvestPlan.COL_HARVEST_DATE, dateInterval[1])
                    .like(StringUtils.isNotBlank(requestDTO.getProductName()), ProductionManageHarvestPlan.COL_PRODUCT_NAME, requestDTO.getProductName())
                    .like(StringUtils.isNotBlank(requestDTO.getCreateUserName()), ProductionManageHarvestPlan.COL_CREATE_USER_NAME, requestDTO.getCreateUserName());
        }

        queryWrapper.orderByDesc(ProductionManageHarvestPlan.COL_CREATE_DATE);

        // 添加数据权限
        commonUtil.roleDataAuthFilter(HARVEST_PLAN, queryWrapper, OLD_CREATE_USER_ID, StringUtils.EMPTY);

        IPage<ProductionManageHarvestPlan> iPage = baseMapper.selectPage(page, queryWrapper);

        List<ProductionManageHarvestPlan> records = Optional.ofNullable(iPage.getRecords())
                .orElse(Collections.emptyList());

        com.jgw.supercodeplatform.common.pojo.common.Page newPage =
                new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());

        List<SearchHarvestPlanResponseVO> list = records.stream().map(plan -> {
            SearchHarvestPlanResponseVO harvestPlan = new SearchHarvestPlanResponseVO();
            BeanUtils.copyProperties(plan, harvestPlan);
            harvestPlan.setCreateDate(LocalDateTimeUtil.formatDateTime2Str(plan.getCreateDate()));
            harvestPlan.setHarvestDate(LocalDateTimeUtil.formatDate2Str(plan.getHarvestDate()));
            return harvestPlan;
        }).collect(Collectors.toList());

        return new PageResults<>(list, newPage);
    }

    /**
     * 获取采收计划详情数据
     *
     * @param id
     * @return
     */
    public SearchHarvestPlanWithInformResponseVO getByPrimaryKey(Long id) throws SuperCodeException {
        QueryWrapper<ProductionManageHarvestPlan> queryWrapper = commonUtil.queryTemplate(ProductionManageHarvestPlan.class);
        queryWrapper.eq(ProductionManageHarvestPlan.COL_ID, id);
        ProductionManageHarvestPlan harvestPlan = baseMapper.selectOne(queryWrapper);

        SearchHarvestPlanWithInformResponseVO responseVO = new SearchHarvestPlanWithInformResponseVO();
        if (Objects.isNull(harvestPlan)) {
            throw new SuperCodeException("不存在此采收计划信息", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        BeanUtils.copyProperties(harvestPlan, responseVO);
        responseVO.setHarvestDate(LocalDateTimeUtil.formatDate2Str(harvestPlan.getHarvestDate()));

        // 获取部门消息通知列表
        QueryWrapper<ProductionManagePlanInformDepartment> wrapper = new QueryWrapper<>();
        wrapper.eq(ProductionManagePlanInformDepartment.COL_PLAN_UNIQUE_ID, harvestPlan.getPlanUniqueId());
        List<ProductionManagePlanInformDepartment> informDepartments = informDepartmentMapper.selectList(wrapper);
        List<SearchPlanInformDepartmentResponseVO> informs = new ArrayList<>(informDepartments.size());
        if (CollectionUtils.isNotEmpty(informDepartments)) {
            informDepartments.forEach(inform -> {
                SearchPlanInformDepartmentResponseVO searchPlanInform = new SearchPlanInformDepartmentResponseVO();
                BeanUtils.copyProperties(inform, searchPlanInform);
                informs.add(searchPlanInform);
            });
        }
        responseVO.setPlanInformDepartments(informs);

        return responseVO;
    }

    /**
     * 获取产量预测列表
     *
     * @param requestDTO
     * @return
     * @throws SuperCodeException
     */
    public PageResults<List<SearchProductionForecastResponseVO>> listForProductionForecast(SearchProductionForecastRequestDTO requestDTO) throws SuperCodeException {
        Page<ProductionManageHarvestPlan> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());

        QueryWrapper<ProductionManageHarvestPlan> queryWrapper = commonUtil.queryTemplate(ProductionManageHarvestPlan.class);
        queryWrapper.eq(ProductionManageHarvestPlan.COL_DELETE_OR_NOT, DeleteOrNotEnum.NOT_DELETED.getKey());
        // 如果search为空则进行高级检索，不为空则普通检索
        if (StringUtils.isNotBlank(requestDTO.getSearch())) {
            queryWrapper.lambda().and(wrapper -> wrapper.or(plan -> plan.like(ProductionManageHarvestPlan::getPlantBatchName, requestDTO.getSearch()))
                    .or(plan -> plan.like(ProductionManageHarvestPlan::getProductName, requestDTO.getSearch()))
                    .or(plan -> plan.like(ProductionManageHarvestPlan::getGreenhouseName, requestDTO.getSearch())));
        } else {
            String[] harvestInterval = LocalDateTimeUtil.substringDate(requestDTO.getHarvestDate());
            queryWrapper.eq(StringUtils.isNotBlank(requestDTO.getPlantBatchName()), ProductionManageHarvestPlan.COL_PLANT_BATCH_NAME, requestDTO.getPlantBatchName())
                    .eq(StringUtils.isNotBlank(requestDTO.getGreenhouseName()), ProductionManageHarvestPlan.COL_GREENHOUSE_NAME, requestDTO.getGreenhouseName())
                    .eq(StringUtils.isNotBlank(requestDTO.getProductName()), ProductionManageHarvestPlan.COL_PRODUCT_NAME, requestDTO.getProductName())
                    .ge(StringUtils.isNotBlank(harvestInterval[0]), ProductionManageHarvestPlan.COL_HARVEST_DATE, harvestInterval[0])
                    .le(StringUtils.isNotBlank(harvestInterval[1]), ProductionManageHarvestPlan.COL_HARVEST_DATE, harvestInterval[1]);
        }

        queryWrapper.orderByDesc(ProductionManageHarvestPlan.COL_HARVEST_DATE);

        // 添加数据权限
        commonUtil.roleDataAuthFilter(YIELD_FORECAST, queryWrapper, OLD_CREATE_USER_ID, StringUtils.EMPTY);

        IPage<ProductionManageHarvestPlan> iPage = baseMapper.selectPage(page, queryWrapper);

        PageResults<List<SearchProductionForecastResponseVO>> pageResult = new PageResults<>();
        List<ProductionManageHarvestPlan> records = iPage.getRecords();

        com.jgw.supercodeplatform.common.pojo.common.Page newPage =
                new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        pageResult.setPagination(newPage);

        List<SearchProductionForecastResponseVO> list = new ArrayList<>(records.size());

        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(plan -> {
                SearchProductionForecastResponseVO harvestPlan = new SearchProductionForecastResponseVO();
                BeanUtils.copyProperties(plan, harvestPlan);
                harvestPlan.setHarvestDate(LocalDateTimeUtil.formatDate2Str(plan.getHarvestDate()));
                list.add(harvestPlan);
            });
        }

        pageResult.setList(list);

        return pageResult;
    }

    /**
     * 采收计划导出
     *
     * @param requestDTO
     * @param response
     */
    public void export(SearchHarvestPlanRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        ArrayList<String> idList = requestDTO.getIdList();
        List<SearchHarvestPlanResponseVO> list;
        // idList如果为空， 则导出全部数据，不为空，则导出指定数据
        if (CollectionUtils.isEmpty(requestDTO.getIdList())) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = list(requestDTO).getList();
        } else {
            QueryWrapper<ProductionManageHarvestPlan> queryWrapper = commonUtil.queryTemplate(ProductionManageHarvestPlan.class);
            queryWrapper.and(query -> query.in(ProductionManageHarvestPlan.COL_ID, idList));
            List<ProductionManageHarvestPlan> plans = baseMapper.selectList(queryWrapper);
            list = new ArrayList<>(plans.size());
            plans.forEach(plan -> {
                SearchHarvestPlanResponseVO harvestPlan = new SearchHarvestPlanResponseVO();
                BeanUtils.copyProperties(plan, harvestPlan);
                harvestPlan.setCreateDate(LocalDateTimeUtil.formatDateTime2Str(plan.getCreateDate()));
                harvestPlan.setHarvestDate(LocalDateTimeUtil.formatDate2Str(plan.getHarvestDate()));
                list.add(harvestPlan);
            });
        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "采收计划", response);
    }

    /**
     * 通过id删除指定的采收计划
     *
     * @param id
     * @return void
     * @author shixiongfei
     * @date 2019-09-05
     * @updateDate 2019-09-05
     * @updatedBy shixiongfei
     */
    public void delete(Long id) throws SuperCodeException {
        boolean isSuccess = update().set(ProductionManageHarvestPlan.COL_DELETE_OR_NOT, DeleteOrNotEnum.DELETED.getKey())
                .eq(ProductionManageHarvestPlan.COL_ID, id)
                .update();
        CustomAssert.isSuccess(isSuccess, "删除采收计划失败");
    }
}
