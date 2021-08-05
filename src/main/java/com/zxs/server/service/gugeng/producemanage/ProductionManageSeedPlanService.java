package com.zxs.server.service.gugeng.producemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.util.*;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.AddSeedPlanRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.SearchSeedPlanRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.UpdateSeedPlanRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManageSeedPlan;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductionManageSeedPlanMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.SeedPlanResponseVO;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.SEED_PLAN;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.OLD_CREATE_USER_ID;


/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-06-14
 */
@Slf4j
@Service
public class ProductionManageSeedPlanService extends ServiceImpl<ProductionManageSeedPlanMapper, ProductionManageSeedPlan> {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;


    /**
     * 新增育苗计划
     * @param requestDTO
     * @throws SuperCodeException
     */
    @Transactional
    public void add(AddSeedPlanRequestDTO requestDTO) throws SuperCodeException {
        ProductionManageSeedPlan entity = new ProductionManageSeedPlan();
        // 对于不同类型无法复制其属性，这个需要注意
        BeanUtils.copyProperties(requestDTO, entity);
        if (StringUtils.isBlank(requestDTO.getTaskNo())) {
            entity.setTaskNo(numberGenerator.getSerialNumber(3, RedisKey.SEED_PLAN_NO_KEY));
        }

        // 判断任务编号是否重复
        QueryWrapper<ProductionManageSeedPlan> queryWrapper = commonUtil.queryTemplate(ProductionManageSeedPlan.class);
        queryWrapper.eq(ProductionManageSeedPlan.COL_TASK_NO, requestDTO.getTaskNo());
        CustomAssert.isNotGreaterThanCustomNumber(baseMapper.selectCount(queryWrapper), 0, "任务编号重复，请重新填写");

        entity.setStartDate(LocalDateTimeUtil.parseStr2LocalDateTime(requestDTO.getStartDate()));
        entity.setEndDate(LocalDateTimeUtil.parseStr2LocalDateTime(requestDTO.getEndDate()));
        // 判断时间格式是否正确
        if (entity.getStartDate().isAfter(entity.getEndDate())) {
            throw new SuperCodeException("开始时间不可大于结束时间", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        Employee employee = commonUtil.getEmployee();
        String greenhouseIds = requestDTO.getGreenhouseIds().stream().collect(Collectors.joining(","));
        String greenhouseNames = requestDTO.getGreenhouseNames().stream().collect(Collectors.joining(","));
        entity.setGreenhouseIds(greenhouseIds);
        entity.setGreenhouseNames(greenhouseNames);
        entity.setCreateDate(LocalDateTime.now());
        entity.setSysId(commonUtil.getSysId());
        entity.setOrganizationId(commonUtil.getOrganizationId());
        entity.setCreateUserId(employee.getEmployeeId());
        entity.setCreateUserName(employee.getName());
        entity.setAuthDepartmentId(employee.getDepartmentId());
        baseMapper.insert(entity);
    }

    /**
     * 编辑育苗计划
     * @param requestDTO
     * @throws SuperCodeException
     */
    @Transactional
    public void update(UpdateSeedPlanRequestDTO requestDTO) throws SuperCodeException {
        ProductionManageSeedPlan entity = new ProductionManageSeedPlan();
        // 获取育苗计划信息
        QueryWrapper<ProductionManageSeedPlan> queryWrapper = commonUtil.queryTemplate(ProductionManageSeedPlan.class);
        queryWrapper.eq(ProductionManageSeedPlan.COL_ID, requestDTO.getId());
        ProductionManageSeedPlan seedPlan = baseMapper.selectOne(queryWrapper);
        if (Objects.isNull(seedPlan)) {
            log.error("育苗计划信息不存在");
            throw new SuperCodeException("计划编号为: " + requestDTO.getTaskNo() + "育苗计划信息不存在", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        BeanUtils.copyProperties(requestDTO, entity);

        entity.setStartDate(LocalDateTimeUtil.parseStr2LocalDateTime(requestDTO.getStartDate()));
        entity.setEndDate(LocalDateTimeUtil.parseStr2LocalDateTime(requestDTO.getEndDate()));

        // 判断时间格式是否正确
        if (entity.getStartDate().isAfter(entity.getEndDate())) {
            throw new SuperCodeException("开始时间不可大于结束时间", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        String greenhouseIds = requestDTO.getGreenhouseIds().stream().collect(Collectors.joining(","));
        String greenhouseNames = requestDTO.getGreenhouseNames().stream().collect(Collectors.joining(","));

        entity.setTaskNo(seedPlan.getTaskNo());
        entity.setId(requestDTO.getId());
        entity.setGreenhouseIds(greenhouseIds);
        entity.setGreenhouseNames(greenhouseNames);


        baseMapper.updateById(entity);
    }

    /**
     * 获取育苗计划列表
     * @param requestDTO
     * @return
     * @throws SuperCodeException
     */
    public PageResults<List<SeedPlanResponseVO>> list(SearchSeedPlanRequestDTO requestDTO) throws SuperCodeException {
        Page<ProductionManageSeedPlan> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageSeedPlan> queryWrapper = commonUtil.queryTemplate(ProductionManageSeedPlan.class);
        queryWrapper.eq(ProductionManageSeedPlan.COL_DELETE_OR_NOT, DeleteOrNotEnum.NOT_DELETED.getKey());
        // search不为空则进行普通检索，为空则进行高级检索
        String search = requestDTO.getSearch();
        if (StringUtils.isNotBlank(search)) {
            queryWrapper.lambda()
                    .and(wrapper -> wrapper.or(seedPlan -> seedPlan.like(ProductionManageSeedPlan::getTaskNo, requestDTO.getSearch()))
                    .or(seedPlan -> seedPlan.like(ProductionManageSeedPlan::getSeedNum, requestDTO.getSearch()))
                    .or(seedPlan -> seedPlan.like(ProductionManageSeedPlan::getGreenhouseNames, requestDTO.getSearch()))
                    .or(seedPlan -> seedPlan.like(ProductionManageSeedPlan::getProductName, requestDTO.getSearch()))
                    .or(seedPlan -> seedPlan.like(ProductionManageSeedPlan::getCreateUserName, requestDTO.getSearch())));
        } else {
            String[] startInterval = LocalDateTimeUtil.substringDate(requestDTO.getStartDate());
            String[] endInterval = LocalDateTimeUtil.substringDate(requestDTO.getEndDate());
            queryWrapper.eq(StringUtils.isNotBlank(requestDTO.getTaskNo()), ProductionManageSeedPlan.COL_TASK_NO, requestDTO.getTaskNo())
                    .eq(StringUtils.isNotBlank(requestDTO.getProductName()), ProductionManageSeedPlan.COL_PRODUCT_NAME, requestDTO.getProductName())
                    .eq(StringUtils.isNotBlank(requestDTO.getProductionNo()), ProductionManageSeedPlan.COL_PRODUCTION_NO, requestDTO.getProductionNo())
                    .like(StringUtils.isNotBlank(requestDTO.getGreenhouseNames()), ProductionManageSeedPlan.COL_GREENHOUSE_NAMES, requestDTO.getGreenhouseNames())
                    .eq(StringUtils.isNotBlank(requestDTO.getCreateUserName()), ProductionManageSeedPlan.COL_CREATE_USER_NAME, requestDTO.getCreateUserName())
                    .ge(StringUtils.isNotBlank(startInterval[0]), ProductionManageSeedPlan.COL_START_DATE, startInterval[0])
                    .le(StringUtils.isNotBlank(startInterval[1]), ProductionManageSeedPlan.COL_START_DATE, startInterval[1])
                    .ge(StringUtils.isNotBlank(endInterval[0]), ProductionManageSeedPlan.COL_END_DATE, endInterval[0])
                    .le(StringUtils.isNotBlank(endInterval[1]), ProductionManageSeedPlan.COL_END_DATE, endInterval[1]);
        }

        queryWrapper.orderByDesc(ProductionManageSeedPlan.COL_END_DATE);

        // 添加数据权限
        commonUtil.roleDataAuthFilter(SEED_PLAN, queryWrapper, OLD_CREATE_USER_ID, StringUtils.EMPTY);

        IPage<ProductionManageSeedPlan> iPage = baseMapper.selectPage(page, queryWrapper);

        PageResults<List<SeedPlanResponseVO>> pageResult = new PageResults<>();
        com.jgw.supercodeplatform.common.pojo.common.Page newPage =
                new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        pageResult.setPagination(newPage);

        List<ProductionManageSeedPlan> records = iPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            pageResult.setList(Collections.EMPTY_LIST);
            return pageResult;
        }

        List<SeedPlanResponseVO> list = new ArrayList<>(records.size());
        records.forEach(seedPlan -> {
            SeedPlanResponseVO plan = new SeedPlanResponseVO();
            BeanUtils.copyProperties(seedPlan, plan);
            plan.setCreateDate(LocalDateTimeUtil.formatDateTime2Str(seedPlan.getCreateDate()));
            plan.setStartDate(LocalDateTimeUtil.formatDate2Str(seedPlan.getStartDate()));
            plan.setEndDate(LocalDateTimeUtil.formatDate2Str(seedPlan.getEndDate()));
            list.add(plan);
        });

        pageResult.setList(list);
        return pageResult;
    }

    /**
     * 通过育苗计划id删除指定的育苗计划
     *
     * @author shixiongfei
     * @date 2019-09-05
     * @updateDate 2019-09-05
     * @updatedBy shixiongfei
     * @param id 育苗计划主键id
     * @return void
     */
    public void delete(Long id) throws SuperCodeException {
        boolean isSuccess = update().set(ProductionManageSeedPlan.COL_DELETE_OR_NOT, DeleteOrNotEnum.DELETED.getKey())
                .eq(ProductionManageSeedPlan.COL_ID, id)
                .update();
        CustomAssert.isSuccess(isSuccess, "删除该育苗计划失败");
    }

    /**
     * 导出育苗计划
     *
     * @author shixiongfei
     * @date 2019-09-06
     * @updateDate 2019-09-06
     * @updatedBy shixiongfei
     * @param requestDTO 请求体
     * @param response 返回响应流
     * @return void
     */
    public void export(SearchSeedPlanRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<String> idList = requestDTO.getIdList();
        List<SeedPlanResponseVO> list;
        // 如果idList为空则导出全部，否则导出指定
        if (CollectionUtils.isEmpty(idList)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = list(requestDTO).getList();
        } else {
            list = excelByIds(idList);
        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "育苗计划列表", response);
    }

    /**
     * 通过主键id集合获取育苗计划信息列表
     * @return
     */
    private List<SeedPlanResponseVO> excelByIds(List<String> idList) {
        List<ProductionManageSeedPlan> list = query().eq(ProductionManageSeedPlan.COL_SYS_ID, commonUtil.getSysId())
                .eq(ProductionManageSeedPlan.COL_ORGANIZATION_ID, commonUtil.getOrganizationId())
                .in(ProductionManageSeedPlan.COL_ID, idList)
                .list();
        return list.stream().map(seedPlan -> {
            SeedPlanResponseVO plan = new SeedPlanResponseVO();
            BeanUtils.copyProperties(seedPlan, plan);
            plan.setCreateDate(LocalDateTimeUtil.formatDateTime2Str(seedPlan.getCreateDate()));
            plan.setStartDate(LocalDateTimeUtil.formatDate2Str(seedPlan.getStartDate()));
            plan.setEndDate(LocalDateTimeUtil.formatDate2Str(seedPlan.getEndDate()));
            return plan;
        }).collect(Collectors.toList());
    }

    /**
     * 通过生产计划编号获取育苗计划列表
     * v1.7版本新加内容
     *
     * @author shixiongfei
     * @date 2019-11-06
     * @updateDate 2019-11-06
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public List<SeedPlanResponseVO> listByProductionNo(String productionNo) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        List<ProductionManageSeedPlan> list = query().eq(StringUtils.isNotBlank(sysId), ProductionManageSeedPlan.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageSeedPlan.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageSeedPlan.COL_PRODUCTION_NO, productionNo)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.stream().map(plan -> {
            SeedPlanResponseVO responseVO = new SeedPlanResponseVO();
            BeanUtils.copyProperties(plan, responseVO);
            responseVO.setStartDate(LocalDateTimeUtil.formatDate2Str(plan.getStartDate()));
            responseVO.setEndDate(LocalDateTimeUtil.formatDate2Str(plan.getEndDate()));
            responseVO.setCreateDate(LocalDateTimeUtil.formatDateTime2Str(plan.getCreateDate()));
            return responseVO;
        }).collect(Collectors.toList());
    }
}