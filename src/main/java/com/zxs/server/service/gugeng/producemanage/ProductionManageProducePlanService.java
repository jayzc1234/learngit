package com.zxs.server.service.gugeng.producemanage;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import lombok.val;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.util.*;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.*;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManagePlanInformDepartment;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManageProducePlan;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductionManagePlanInformDepartmentMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductionManageProducePlanMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.common.MessageInformService;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.ObjectUniqueValueResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.ProducePlanResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.SearchPlanInformDepartmentResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.SearchProducePlanWithInformResponseVO;
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

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.PRODUCE_PLAN;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.OLD_CREATE_USER_ID;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-06-13
 */
@Service
public class ProductionManageProducePlanService extends ServiceImpl<ProductionManageProducePlanMapper, ProductionManageProducePlan> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManagePlanInformDepartmentMapper informDepartmentMapper;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private MessageInformService messageInformService;


    /**
     * 通过生产计划编号获取生产计划信息
     *
     * @author shixiongfei
     * @date 2019-11-06
     * @updateDate 2019-11-06
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public List<ProducePlanResponseVO> listByProductionNo(String productionNo) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        List<ProductionManageProducePlan> list = query().eq(StringUtils.isNotBlank(sysId), ProductionManageProducePlan.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageProducePlan.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageProducePlan.COL_PRODUCTION_NO, productionNo)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.stream().map(this::parseGreenhouseInfo2Names).collect(Collectors.toList());
    }

    /**
     * 通过计划编号来查询指定生产计划的详细信息
     *
     * @return
     */
    public SearchProducePlanWithInformResponseVO getByProductionNo(Long id) throws SuperCodeException {

        QueryWrapper<ProductionManageProducePlan> queryWrapper = commonUtil.queryTemplate(ProductionManageProducePlan.class);
        queryWrapper.eq(ProductionManageProducePlan.COL_ID, id);
        ProductionManageProducePlan producePlan = baseMapper.selectOne(queryWrapper);
        if (Objects.isNull(producePlan)) {
            throw new SuperCodeException("不存在此生产任务信息", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        SearchProducePlanWithInformResponseVO responseVO = new SearchProducePlanWithInformResponseVO();
        BeanUtils.copyProperties(producePlan, responseVO);

        responseVO.setStartDate(LocalDateTimeUtil.formatDate2Str(producePlan.getStartDate()));
        responseVO.setEndDate(LocalDateTimeUtil.formatDate2Str(producePlan.getEndDate()));

        // 反序列化为对象
        String greenhouseInfo = producePlan.getGreenhouseInfo();
        AddBaseRequestDTO baseMessage = JSONObject.parseObject(greenhouseInfo, AddBaseRequestDTO.class);
        responseVO.setBaseMessage(baseMessage);

        QueryWrapper<ProductionManagePlanInformDepartment> informQueryWrapper = new QueryWrapper<>();
        informQueryWrapper.eq(ProductionManagePlanInformDepartment.COL_PLAN_UNIQUE_ID, producePlan.getPlanUniqueId());
        List<ProductionManagePlanInformDepartment> informDepartments = informDepartmentMapper.selectList(informQueryWrapper);

        List<SearchPlanInformDepartmentResponseVO> informList = new ArrayList<>(informDepartments.size());
        if (CollectionUtils.isNotEmpty(informDepartments)) {
            informDepartments.forEach(inform -> {
                SearchPlanInformDepartmentResponseVO informDepartmentResponseVO = new SearchPlanInformDepartmentResponseVO();
                BeanUtils.copyProperties(inform, informDepartmentResponseVO);
                informList.add(informDepartmentResponseVO);
            });
        }

        responseVO.setPlanInformDepartments(informList);
        return responseVO;
    }

    /**
     * 获取生产计划列表
     *
     * @param requestDTO
     * @author shixiongfei
     * @updateDate 2019-09-05
     * @since V1.1.1 添加了未删除的限制
     */
    public PageResults<List<ProducePlanResponseVO>> list(ProducePlanRequestDTO requestDTO) throws Exception {
        Page<ProductionManageProducePlan> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageProducePlan> queryWrapper = commonUtil.queryTemplate(ProductionManageProducePlan.class);
        // 获取未被删除的生产计划
        queryWrapper.eq(ProductionManageProducePlan.COL_DELETE_OR_NOT, DeleteOrNotEnum.NOT_DELETED.getKey());
        // search为空，则普通查询，不为空，则高级检索
        if (StringUtils.isNotBlank((requestDTO.getSearch()))) {
            queryWrapper.lambda().and(wrapper -> wrapper.or(plan -> plan.like(ProductionManageProducePlan::getCreateUserName, requestDTO.getSearch()))
                    .or(plan -> plan.like(ProductionManageProducePlan::getProductName, requestDTO.getSearch()))
                    .or(plan -> plan.like(ProductionManageProducePlan::getProductionNo, requestDTO.getSearch())));
        } else {
            String[] startInterval = LocalDateTimeUtil.substringDate(requestDTO.getStartDate());
            String[] endInterval = LocalDateTimeUtil.substringDate(requestDTO.getEndDate());
            String[] createInterval = LocalDateTimeUtil.substringDate(requestDTO.getCreateDate());
            queryWrapper.eq(StringUtils.isNotBlank(requestDTO.getProductName()), ProductionManageProducePlan.COL_PRODUCT_NAME, requestDTO.getProductName())
                    .eq(StringUtils.isNotBlank(requestDTO.getUserName()), ProductionManageProducePlan.COL_CREATE_USER_NAME, requestDTO.getUserName())
                    .eq(StringUtils.isNotBlank(requestDTO.getProductionNo()), ProductionManageProducePlan.COL_PRODUCTION_NO, requestDTO.getProductionNo())
                    // 这里因为区域信息为json字符串
                    .like(StringUtils.isNotBlank(requestDTO.getGreenhouseInfo()), ProductionManageProducePlan.COL_GREENHOUSE_INFO, requestDTO.getGreenhouseInfo())
                    .ge(StringUtils.isNotBlank(startInterval[0]), ProductionManageProducePlan.COL_START_DATE, startInterval[0])
                    .lt(StringUtils.isNotBlank(startInterval[1]), ProductionManageProducePlan.COL_START_DATE, LocalDateTimeUtil.addOneDay(startInterval[1]))
                    .ge(StringUtils.isNotBlank(endInterval[0]), ProductionManageProducePlan.COL_END_DATE, endInterval[0])
                    .lt(StringUtils.isNotBlank(endInterval[1]), ProductionManageProducePlan.COL_END_DATE, LocalDateTimeUtil.addOneDay(endInterval[1]))
                    .ge(StringUtils.isNotBlank(createInterval[0]), ProductionManageProducePlan.COL_CREATE_DATE, createInterval[0])
                    .le(StringUtils.isNotBlank(createInterval[1]), ProductionManageProducePlan.COL_CREATE_DATE, createInterval[1]);
        }
        queryWrapper.orderByDesc(ProductionManageProducePlan.COL_CREATE_DATE);

        // 添加数据权限
        commonUtil.roleDataAuthFilter(PRODUCE_PLAN, queryWrapper, OLD_CREATE_USER_ID, StringUtils.EMPTY);

        IPage<ProductionManageProducePlan> iPage = baseMapper.selectPage(page, queryWrapper);

        PageResults<List<ProducePlanResponseVO>> pageResult = new PageResults<>();
        com.jgw.supercodeplatform.common.pojo.common.Page newPage =
                new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        pageResult.setPagination(newPage);

        List<ProductionManageProducePlan> records = Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());

        List<ProducePlanResponseVO> list = records.stream().map(this::parseGreenhouseInfo2Names).collect(Collectors.toList());

        pageResult.setList(list);

        return pageResult;
    }

    /**
     * 新增生产计划
     *
     * @param requestDTO
     */
    @Transactional
    public void save(AddProducePlanRequestDTO requestDTO) throws SuperCodeException {
        if (StringUtils.isBlank(requestDTO.getProductionNo())) {
            val planNumber = numberGenerator.getSerialNumber(3, RedisKey.PRODUCE_PLAN_NO_KEY);
            // 计划编号为产品名称+年月日+3位流水号
            requestDTO.setProductionNo(requestDTO.getProductName() + planNumber);
        }

        // 校验编号是否重复
        QueryWrapper<ProductionManageProducePlan> queryWrapper = commonUtil.queryTemplate(ProductionManageProducePlan.class);
        queryWrapper.eq(ProductionManageProducePlan.COL_PRODUCTION_NO, requestDTO.getProductionNo());
        CustomAssert.isNotGreaterThanCustomNumber(baseMapper.selectCount(queryWrapper), 0, "该计划编号已经存在，请重新输入");

        // 校验选择时间和区域是否正确
        validProductPlan(requestDTO);

        // 获取通知部门列表
        List<AddPlanInformDepartmentRequestDTO> informDepartments = requestDTO.getPlanInformDepartments();
        // 校验通知部门消息内容是否为空
        if (CollectionUtils.isNotEmpty(informDepartments)) {
            long count = informDepartments.stream().filter(inform -> StringUtils.isBlank(inform.getInformContent())).count();
            if (count > 0) {
                throw new SuperCodeException("通知部门消息内容不可为空，请填写", HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
        }

        ProductionManageProducePlan producePlan = new ProductionManageProducePlan();
        BeanUtils.copyProperties(requestDTO, producePlan);

        Employee employee = commonUtil.getEmployee();
        producePlan.setCreateUserId(employee.getEmployeeId());
        producePlan.setCreateUserName(employee.getName());
        producePlan.setUpdateUserId(employee.getEmployeeId());
        producePlan.setUpdateUserName(employee.getName());
        producePlan.setAuthDepartmentId(employee.getDepartmentId());
        producePlan.setCreateDate(LocalDateTime.now());
        producePlan.setUpdateDate(LocalDateTime.now());
        producePlan.setStartDate(LocalDateTimeUtil.parseStr2LocalDateTime(requestDTO.getStartDate()));
        producePlan.setEndDate(LocalDateTimeUtil.parseStr2LocalDateTime(requestDTO.getEndDate()));

        producePlan.setSysId(commonUtil.getSysId());
        producePlan.setOrganizationId(commonUtil.getOrganizationId());

        // 设置唯一id
        String uuid = commonUtil.getUUID();
        producePlan.setPlanUniqueId(uuid);

        // 将基地信息进行json序列化为json字符串
        String jsonBaseMessage = JSONObject.toJSONString(requestDTO.getBaseMessage());
        producePlan.setGreenhouseInfo(jsonBaseMessage);
        baseMapper.insert(producePlan);

        // 添加部门消息通知
        savePlanInformDepartment(informDepartments, uuid);

    }

    /**
     * 编辑生产计划
     *
     * @updateDate 2019-09-05
     * @since V1.1.1版本中加入了生产计划未被删除的限制
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(UpdateProducePlanRequestDTO requestDTO) throws SuperCodeException {
        // 获取当前生产计划
        QueryWrapper<ProductionManageProducePlan> queryWrapper = commonUtil.queryTemplate(ProductionManageProducePlan.class);
        queryWrapper.eq(ProductionManageProducePlan.COL_ID, requestDTO.getId())
                .eq(ProductionManageProducePlan.COL_DELETE_OR_NOT, DeleteOrNotEnum.NOT_DELETED.getKey());
        ProductionManageProducePlan manageProducePlan = baseMapper.selectOne(queryWrapper);
        if (Objects.isNull(manageProducePlan)) {
            throw new SuperCodeException("不存在此生产计划信息", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        if (StringUtils.isBlank(requestDTO.getProductionNo())) {
            requestDTO.setProductionNo(manageProducePlan.getProductionNo());
        } else {
            // 判断计划编号是否不同， 不同查询是否包含相同编号
            if (!manageProducePlan.getProductionNo().equals(requestDTO.getProductionNo())) {
                QueryWrapper<ProductionManageProducePlan> queryWrapper1 = commonUtil.queryTemplate(ProductionManageProducePlan.class);
                queryWrapper1.eq(ProductionManageProducePlan.COL_PRODUCTION_NO, requestDTO.getProductionNo());
                Integer count = baseMapper.selectCount(queryWrapper1);
                if (count > 0) {
                    throw new SuperCodeException("该计划编号已存在，请重新输入", HttpStatus.SC_INTERNAL_SERVER_ERROR);
                }
            }
        }

        // 校验选择时间和区域是否正确
        validProductPlan(requestDTO);

        ProductionManageProducePlan producePlan = new ProductionManageProducePlan();
        BeanUtils.copyProperties(requestDTO, producePlan);

        producePlan.setUpdateUserId(commonUtil.getEmployee().getEmployeeId());
        producePlan.setUpdateUserName(commonUtil.getEmployee().getName());
        producePlan.setUpdateDate(LocalDateTime.now());

        producePlan.setStartDate(LocalDateTimeUtil.parseStr2LocalDateTime(requestDTO.getStartDate()));
        producePlan.setEndDate(LocalDateTimeUtil.parseStr2LocalDateTime(requestDTO.getEndDate()));

        // 将基地信息进行json序列化为json字符串
        String jsonBaseMessage = JSONObject.toJSONString(requestDTO.getBaseMessage());
        producePlan.setGreenhouseInfo(jsonBaseMessage);

        baseMapper.updateById(producePlan);

        // 移除部门消息通知
        QueryWrapper<ProductionManagePlanInformDepartment> informWrapper = new QueryWrapper<>();
        informWrapper.eq(ProductionManagePlanInformDepartment.COL_PLAN_UNIQUE_ID, manageProducePlan.getPlanUniqueId());
        List<ProductionManagePlanInformDepartment> departments = informDepartmentMapper.selectList(informWrapper);
        informDepartmentMapper.delete(informWrapper);

        // 添加部门消息通知
        savePlanInformDepartment(requestDTO.getPlanInformDepartments(), manageProducePlan.getPlanUniqueId());

        List<AddPlanInformDepartmentRequestDTO> informs = requestDTO.getPlanInformDepartments();
        //新增部门成功后，如果新增部门或者修改部门消息则发送消息
        if (null != departments && departments.isEmpty()) {
            Map<String, String> mapDepartment = new HashMap<String, String>();
            for (ProductionManagePlanInformDepartment department : departments) {
                mapDepartment.put(department.getDepartmentId(), department.getInformContent());
            }
            if (null != informs && !informs.isEmpty()) {
                for (AddPlanInformDepartmentRequestDTO departmentRequestDTO : informs) {
                    String oldcontent = mapDepartment.get(departmentRequestDTO.getDepartmentId());
                    if (StringUtils.isBlank(oldcontent)) {
                        messageInformService.sendOrgMessageToAllPartmentUser(departmentRequestDTO.getDepartmentId(), departmentRequestDTO.getDepartmentName(), departmentRequestDTO.getInformContent(), 2, 1, 1);
                    } else {
                        if (!oldcontent.equals(departmentRequestDTO.getInformContent())) {
                            messageInformService.sendOrgMessageToAllPartmentUser(departmentRequestDTO.getDepartmentId(), departmentRequestDTO.getDepartmentName(), departmentRequestDTO.getInformContent(), 2, 1, 1);
                        }
                    }
                }
            }
        }
    }

    /**
     * 新增和编辑下校验时间和分区选择是否正确
     *
     * @param requestDTO
     * @throws SuperCodeException
     */
    private void validProductPlan(AddProducePlanRequestDTO requestDTO) throws SuperCodeException {
        // 校验开始时间是否小于结束时间
        LocalDateTime startDate = LocalDateTimeUtil.parseStr2LocalDateTime(requestDTO.getStartDate());
        LocalDateTime endDate = LocalDateTimeUtil.parseStr2LocalDateTime(requestDTO.getEndDate());

        if (startDate.isAfter(endDate)) {
            throw new SuperCodeException("开始时间必须不大于结束时间", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        // 校验选择的分区/区域格式是否正确
        List<AddAreaRequestDTO> areas = requestDTO.getBaseMessage().getAreas();
        for (AddAreaRequestDTO area : areas) {
            if (area.getResultType() == 0 && CollectionUtils.isEmpty(area.getChilds())) {
                throw new SuperCodeException("分区下没有选择区域, 请选择", HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    /**
     * 添加部门消息通知
     */
    private void savePlanInformDepartment(List<AddPlanInformDepartmentRequestDTO> informDepartments, String uuid) {
        // 添加部门消息通知
        if (CollectionUtils.isNotEmpty(informDepartments)) {
            informDepartments.forEach(inform -> {
                ProductionManagePlanInformDepartment informDepartment = new ProductionManagePlanInformDepartment();
                BeanUtils.copyProperties(inform, informDepartment);
                informDepartment.setReceiverType(0);
                informDepartment.setPlanUniqueId(uuid);
                informDepartmentMapper.insert(informDepartment);
                messageInformService.sendOrgMessageToAllPartmentUser(informDepartment.getDepartmentId(), informDepartment.getDepartmentName(), informDepartment.getInformContent(), 2, 1, 1);
            });
        }
    }

    /**
     * 删除指定的生产计划
     *
     * @param id
     * @return void
     * @author shixiongfei
     * @date 2019-09-05
     * @updateDate 2019-09-05
     * @updatedBy shixiongfei
     */
    public void delete(Long id) throws SuperCodeException {
        boolean isSuccess = update().set(ProductionManageProducePlan.COL_DELETE_OR_NOT, DeleteOrNotEnum.DELETED.getKey())
                .eq(ProductionManageProducePlan.COL_ID, id)
                .update();
        CustomAssert.isSuccess(isSuccess, "删除生产计划失败");
    }

    /**
     * 通过主键id集合获取生产计划信息列表
     *
     * @return
     */
    public List<ProducePlanResponseVO> excelByIds(List<String> idList) {
        List<ProductionManageProducePlan> list = query().eq(ProductionManageProducePlan.COL_SYS_ID, commonUtil.getSysId())
                .eq(ProductionManageProducePlan.COL_ORGANIZATION_ID, commonUtil.getOrganizationId())
                .in(ProductionManageProducePlan.COL_ID, idList)
                .list();
        return list.stream().map(plan -> parseGreenhouseInfo2Names(plan)).collect(Collectors.toList());
    }

    /**
     * 解析区域信息json字符串为区域信息名称
     *
     * @param plan 生产计划实体类
     * @return ProducePlanResponseVO 生产计划响应vo
     * @author shixiongfei
     * @date 2019-09-06
     * @updateDate 2019-09-06
     * @updatedBy shixiongfei
     */
    public ProducePlanResponseVO parseGreenhouseInfo2Names(ProductionManageProducePlan plan) {
        ProducePlanResponseVO responseVO = new ProducePlanResponseVO();
        BeanUtils.copyProperties(plan, responseVO);
        // 设置对应的时间类型值
        responseVO.setStartDate(LocalDateTimeUtil.formatDate2Str(plan.getStartDate()));
        responseVO.setEndDate(LocalDateTimeUtil.formatDate2Str(plan.getEndDate()));
        responseVO.setCreateDate(LocalDateTimeUtil.formatDateTime2Str(plan.getCreateDate()));

        // 反序列化为对象
        String greenhouseInfo = plan.getGreenhouseInfo();
        AddBaseRequestDTO baseMessage = JSONObject.parseObject(greenhouseInfo, AddBaseRequestDTO.class);
        List<AddAreaRequestDTO> areas = baseMessage.getAreas();

        // 设置种植区域信息
        String greenhouse = "";
        String greenhouseIdInfo = "";
        List<AddAreaRequestDTO> stream1 = areas.stream().filter(area -> area.getResultType()!=null && area.getResultType() == 1).collect(Collectors.toList());
        List<List<AddAreaRequestDTO>> listStream = areas.stream().filter(area ->area.getResultType()!=null && area.getResultType() == 0).map(child -> child.getChilds()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(listStream)) {
            if (CollectionUtils.isNotEmpty(stream1)) {
                listStream.add(stream1);
            }
            greenhouse = listStream.stream().flatMap(stream -> stream.stream().map(AddAreaRequestDTO::getAreaName)).collect(Collectors.joining(","));
            greenhouseIdInfo = listStream.stream().flatMap(stream -> stream.stream().map(AddAreaRequestDTO::getAreaId)).collect(Collectors.joining(","));
        } else {
            if (CollectionUtils.isNotEmpty(stream1)) {
                greenhouse = stream1.stream().map(AddAreaRequestDTO::getAreaName).collect(Collectors.joining(","));
                greenhouseIdInfo = stream1.stream().map(AddAreaRequestDTO::getAreaId).collect(Collectors.joining(","));
            }
        }

        responseVO.setGreenhouseInfo(greenhouse);
        responseVO.setGreenhouseIdInfo(greenhouseIdInfo);
        return responseVO;
    }

    /**
     * 生产计划导出
     *
     * @param requestDTO
     * @param response
     */
    public void export(ProducePlanRequestDTO requestDTO, HttpServletResponse response) throws Exception {
        ArrayList<String> idList = requestDTO.getIdList();
        List<ProducePlanResponseVO> list;
        // idList如果为空， 则导出全部数据，不为空，则导出指定数据
        if (CollectionUtils.isEmpty(requestDTO.getIdList())) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = list(requestDTO).getList();
        } else {
            list = excelByIds(idList);
        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "生产计划列表", response);
    }

    public PageResults<List<ObjectUniqueValueResponseVO>> listPlanfield(ProducePlanRequestDTO requestDTO) throws Exception {
        PageResults<List<ObjectUniqueValueResponseVO>> pageResult = new PageResults<>();

        PageResults<List<ProducePlanResponseVO>> pageResults = list(requestDTO);
        List<ProducePlanResponseVO> list = pageResults.getList();

        pageResult.setPagination(pageResults.getPagination());
        pageResult.setList(list.stream().map(e->new ObjectUniqueValueResponseVO(e.getPlanUniqueId(), e.getProductionNo())).collect(Collectors.toList()));

        return pageResult;
    }
}