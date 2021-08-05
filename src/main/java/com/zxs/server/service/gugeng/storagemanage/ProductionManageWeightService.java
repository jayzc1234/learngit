package com.zxs.server.service.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.bo.gugeng.HarvestPlanBO;
import net.app315.hydra.intelligent.planting.bo.gugeng.WeighingBO;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.AddProductWeightRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ProductWeightPageDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.UpdateProductWeightRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.ProductWeightOperateTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageWeight;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductionManageHarvestPlanMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageWeightMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchWeightResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.WeighingVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.PRODUCT_WEIGHING;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.*;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-16
 */
@Service
public class ProductionManageWeightService extends ServiceImpl<ProductionManageWeightMapper, ProductionManageWeight> implements BaseService<SearchWeightResponseVO> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageHarvestPlanMapper harvestPlanMapper;

    /**
     * 分页
     *
     * @param daosearch
     * @return
     * @throws SuperCodeException
     */
    @Override
    public PageResults<List<SearchWeightResponseVO>> page(DaoSearch daosearch) throws SuperCodeException {
        ProductWeightPageDTO param = (ProductWeightPageDTO) daosearch;
        Page<ProductionManageWeight> page = new Page<>(param.getDefaultCurrent(), param.getDefaultPageSize());
        QueryWrapper<ProductionManageWeight> queryWrapper = getQueryWrapper(page, param, commonUtil.getOrganizationId(), commonUtil.getSysId());

        // 添加数据权限
        commonUtil.roleDataAuthFilter(PRODUCT_WEIGHING, queryWrapper, OLD_CREATE_USER_ID, StringUtils.EMPTY);

        IPage<ProductionManageWeight> iPage = baseMapper.selectPage(page, queryWrapper);
        PageResults<List<SearchWeightResponseVO>> pageResults = new PageResults<>();
        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        pageResults.setPagination(pagination);
        List<ProductionManageWeight> records = iPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return pageResults;
        }

        List<SearchWeightResponseVO> list = new ArrayList<>(records.size());

        records.forEach(record -> {
            SearchWeightResponseVO responseVO = new SearchWeightResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            responseVO.setType(record.getType() + "");
            list.add(responseVO);
        });

        pageResults.setList(list);
        return pageResults;
    }


    /**
     * 分页查询
     *
     * @param page
     * @param param
     * @return
     */
    QueryWrapper<ProductionManageWeight> getQueryWrapper(Page<ProductionManageWeight> page, ProductWeightPageDTO param, String organizationId, String sysId) {
        QueryWrapper<ProductionManageWeight> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(organizationId), ProductionManageWeight.COL_ORGANIZATION_ID, organizationId)
                .eq(StringUtils.isNotBlank(sysId), ProductionManageWeight.COL_SYS_ID, sysId);

        String search = param.getSearch();
        if (StringUtils.isBlank(search)) {
            /**
             * 称重类型（种植称重、外采称重）、批次名称、产品名称、区域/基地名称（外采基地）、称重时间（时间段）、操作人
             * // todo 精准还是模糊等测试提
             */
            String[] weighingInterval = LocalDateTimeUtil.substringDate(param.getWeighingDate());
            queryWrapper.eq((param.getType() != null), ProductionManageWeight.COL_TYPE, param.getType())
                    .like(Objects.nonNull((param.getWeight())), ProductionManageWeight.COL_WEIGHT, param.getWeight())
                    .like(StringUtils.isNotBlank(param.getPlantBatchId()), ProductionManageWeight.COL_PLANT_BATCH_ID, param.getPlantBatchId())
                    .like(StringUtils.isNotBlank(param.getPlantBatchName()), ProductionManageWeight.COL_PLANT_BATCH_NAME, param.getPlantBatchName())
                    .like(StringUtils.isNotBlank(param.getProductName()), ProductionManageWeight.COL_PRODUCT_NAME, param.getProductName())
                    .like(StringUtils.isNotBlank(param.getBaseName()), ProductionManageWeight.COL_BASE_NAME, param.getBaseName())
                    .like(StringUtils.isNotBlank(param.getGreenhouseName()), ProductionManageWeight.COL_GREENHOUSE_NAME, param.getGreenhouseName())
                    .ge(StringUtils.isNotBlank(weighingInterval[0]), ProductionManageWeight.COL_WEIGHING_DATE, weighingInterval[0])
                    .le(StringUtils.isNotBlank(weighingInterval[1]), ProductionManageWeight.COL_WEIGHING_DATE, weighingInterval[1])
                    .like(StringUtils.isNotBlank(param.getCreateUserName()), ProductionManageWeight.COL_CREATE_USER_NAME, param.getCreateUserName());
        } else {
            queryWrapper.and(wrapper -> wrapper
                    .or(condition -> condition.like(ProductionManageWeight.COL_WEIGHT, search))
                    .or(condition -> condition.like(ProductionManageWeight.COL_PLANT_BATCH_ID, search))
                    .or(condition -> condition.like(ProductionManageWeight.COL_PLANT_BATCH_NAME, search))
                    .or(condition -> condition.like(ProductionManageWeight.COL_PRODUCT_NAME, search))
                    .or(condition -> condition.like(ProductionManageWeight.COL_BASE_NAME, search))
                    .or(condition -> condition.like(ProductionManageWeight.COL_GREENHOUSE_NAME, search))
                    .or(condition -> condition.like(ProductionManageWeight.COL_CREATE_USER_NAME, search)));
        }
        queryWrapper.orderByDesc(ProductionManageWeight.COL_WEIGHING_DATE);

        return queryWrapper;
    }

    @Override
    public List<SearchWeightResponseVO> listExcelByIds(List<? extends Serializable> ids) throws SuperCodeException {
        List<SearchWeightResponseVO> weightResponseVOs = baseMapper.listExcelByIds(ids);
        dataTransfer(weightResponseVOs);
        return weightResponseVOs;
    }


    @Override
    public void dataTransfer(List<SearchWeightResponseVO> list) throws SuperCodeException {
        if (null != list && !list.isEmpty()) {
            try {
                for (SearchWeightResponseVO searchWeightResponseVO : list) {
                    searchWeightResponseVO.setType(ProductWeightOperateTypeEnum.getValue(searchWeightResponseVO.getType()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 详情
     *
     * @param id
     * @return
     * @throws SuperCodeException
     */
    public ProductionManageWeight getById(String id) throws SuperCodeException {
        ProductionManageWeight productWeight = baseMapper.selectById(id);
        if (productWeight == null) {
            throw new SuperCodeException("数据不存在");
        }
        return productWeight;
    }

    /**
     * 新增产品称重
     *
     * @param requestDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(AddProductWeightRequestDTO requestDTO) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        ProductionManageWeight weight = new ProductionManageWeight();
        BeanUtils.copyProperties(requestDTO, weight);
        weight.setSysId(sysId);
        weight.setOrganizationId(organizationId);

        Employee employee = commonUtil.getEmployee();
        Date now = new Date();
        weight.setUpdateDate(now);
        weight.setUpdateUserId(employee.getEmployeeId());
        weight.setUpdateUserName(employee.getName());

        weight.setCreateDate(now);
        weight.setCreateUserId(employee.getEmployeeId());
        weight.setCreateUserName(employee.getName());

        int count = baseMapper.insert(weight);
        if (count < 1) {
            throw new SuperCodeException("新增产品称重失败");
        }

        // 更新生产产量数据统计数据,这里将统计数据代码嵌入进了业务代码中是不合理的，
        // 开采用消息队列的方式实现解耦和消息重试，但是对于一体化应用不推荐使用此方式
        // 这里采用线程池的方式来做异步处理
        // jobExecutor.execute(() -> yieldDataService.updateAreaYield(requestDTO.getPlantBatchId(), requestDTO.getWeight(), true));
    }

    /**
     * 编辑产品称重
     *
     * @param requestDTO
     * @throws SuperCodeException
     */

    @Transactional(rollbackFor = Exception.class)
    public void update(UpdateProductWeightRequestDTO requestDTO) throws SuperCodeException {

        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        ProductionManageWeight manageWeight = query()
                .eq(StringUtils.isNotBlank(sysId), ProductionManageWeight.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageWeight.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageWeight.COL_ID, requestDTO.getId())
                .one();
        CustomAssert.isNull(manageWeight, "不存在此产品称重信息，请检查");
        ProductionManageWeight weight = new ProductionManageWeight();
        BeanUtils.copyProperties(requestDTO, weight);

        Employee employee = commonUtil.getEmployee();
        weight.setUpdateDate(new Date());
        weight.setUpdateUserId(employee.getEmployeeId());
        weight.setUpdateUserName(employee.getName());

        int count = baseMapper.updateById(weight);
        if (count < 1) {
            throw new SuperCodeException("产品称重编辑失败");
        }

        // 更新生产产量数据统计数据,这里将统计数据代码嵌入进了业务代码中是不合理的，
        // 开采用消息队列的方式实现解耦和消息重试，但是对于一体化应用不推荐使用此方式
        // 这里采用线程池的方式来做异步处理
//        jobExecutor.execute(() -> {
//            BigDecimal oldWeight = manageWeight.getWeight();
//            BigDecimal newWeight = requestDTO.getWeight();
//            BigDecimal updateWeight = newWeight.subtract(oldWeight).setScale(2, BigDecimal.ROUND_HALF_UP);
//            yieldDataService.updateAreaYield(requestDTO.getPlantBatchId(), updateWeight, true);
//        });


        // 种植批次重量累加
        // 目前更新逻辑：更新产品重量, 然后该产品重量与原重量差价与其他种植批次一个产品重量相加  =最终叠加结果
//        Map<String, Object> plantBatchIdMap = new HashMap<>();
//        plantBatchIdMap.put("OrganizationId",commonUtil.getOrganizationId());
//        plantBatchIdMap.put("SysId",commonUtil.getSysId());
//        plantBatchIdMap.put("PlantBatchId",updateVO.getPlantBatchId());
//        plantBatchIdMap.put("Id ! ",updateVO.getId());
//        QueryWrapper<ProductionManageWeight> query = new QueryWrapper<>();
//        query.select("PlantBatchId","Weight").apply(" OrganizationId = {0} and SysId = {1} and PlantBatchId = {2} and Id != {3} ",commonUtil.getOrganizationId() ,commonUtil.getSysId(),updateVO.getPlantBatchId() , updateVO.getId());
//        List<ProductionManageWeight> productWeights = baseMapper.selectList(query);
//
//        // 需要做当前种植批次是否为空的逻辑判断
//        if (CollectionUtils.isNotEmpty(productWeights)) {
//            BigDecimal finalWeight = toDB.getWeight().subtract(productWeights.get(0).getWeight());
//            updateWeightWhenProductWeight(productWeights.get(0).getPlantBatchId(), finalWeight);
//        }
    }

    /**
     * 通过种植批次id来获取称重总重量，该方法仅用于定时任务
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-16
     * @updateDate 2019-10-16
     * @updatedBy shixiongfei
     */
    public BigDecimal getWeighingWeight(String plantBatchId, String sysId, String organizationId) {
        ProductionManageWeight weight = query().select("SUM(IFNULL(Weight, 0)) AS weight")
                .eq(ProductionManageWeight.COL_WEIGHT, sysId)
                .eq(ProductionManageWeight.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageWeight.COL_PLANT_BATCH_ID, plantBatchId)
                .eq(ProductionManageWeight.COL_TYPE, ProductWeightOperateTypeEnum.HARVEST_WEIGHT.getKey())
                .one();

        return Objects.isNull(weight) ? BigDecimal.ZERO :
                Objects.isNull(weight.getWeight()) ? BigDecimal.ZERO : weight.getWeight();
    }

    /**
     * 通过批次获取产品称重信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-29
     * @updateDate 2019-10-29
     * @updatedBy shixiongfei
     */
    public BigDecimal getByBatchId(String plantBatchId, String sysId, String organizationId) {
        ProductionManageWeight weigh = query().select("SUM(IFNULL(" + ProductionManageWeight.COL_WEIGHT + ", 0)) AS weight")
                .eq(StringUtils.isNotBlank(sysId), ProductionManageWeight.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageWeight.COL_ORGANIZATION_ID, organizationId)
                .eq(StringUtils.isNotBlank(plantBatchId), ProductionManageWeight.COL_PLANT_BATCH_ID, plantBatchId)
                .one();

        return Objects.isNull(weigh) ? BigDecimal.ZERO : weigh.getWeight();
    }

    /**
     * 获取最近半年的产品称重信息
     *
     * @author shixiongfei
     * @date 2019-12-18
     * @updateDate 2019-12-18
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public List<WeighingVO> listByHalfYearMsg() {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        // 获取最近半年的时间区间
        List<String> months = LocalDateTimeUtil.listHalfYearAndMonth();

        QueryWrapper<ProductionManageWeight> wrapper = new QueryWrapper<>();
        wrapper.eq(OLD_SYS_ID, sysId)
                .eq(OLD_ORGANIZATION_ID, organizationId)
                .in("DATE_FORMAT(" + ProductionManageWeight.COL_WEIGHING_DATE + ",'%Y-%m')", months)
                .groupBy("month");

        // 获取称重数据
        List<WeighingBO> weighingBOS = net.app315.hydra.intelligent.planting.common.gugeng.util.CollectionUtils.elementIsNull(baseMapper.listByHalfYearMsg(wrapper));

        // 获取采收计划的预测值数据
        QueryWrapper<ProductionManageWeight> planeWrapper = new QueryWrapper<>();
        planeWrapper.eq(OLD_SYS_ID, sysId)
                .eq(OLD_ORGANIZATION_ID, organizationId)
                .in("DATE_FORMAT("+ProductionManageWeight.COL_CREATE_DATE+",'%Y-%m')", months)
                .groupBy("month");
        List<HarvestPlanBO> planBOS = net.app315.hydra.intelligent.planting.common.gugeng.util.CollectionUtils.elementIsNull(harvestPlanMapper.listByHalfYearMsg(planeWrapper));

        List<WeighingVO> vos = months.stream().map(month -> {
            WeighingVO vo = new WeighingVO();
            vo.setMonth(month);
            WeighingBO weighingBO = weighingBOS.stream().filter(t -> month.equals(t.getMonth())).findFirst().orElse(null);
            vo.setHarvestQuantity(Objects.isNull(weighingBO) ? BigDecimal.ZERO : weighingBO.getHarvestQuantity());
            HarvestPlanBO planBO = planBOS.stream().filter(t -> month.equals(t.getMonth())).findFirst().orElse(null);
            vo.setExpectQuantity(Objects.isNull(planBO) ? BigDecimal.ZERO : planBO.getExpectQuantity());
            return vo;
        }).collect(Collectors.toList());

        return vos;
    }
}