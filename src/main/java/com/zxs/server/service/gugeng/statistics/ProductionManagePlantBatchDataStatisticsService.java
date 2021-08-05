package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.PlantingBatchDataStatisticsDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.BatchTypesEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageSortInstorage;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductionManageHarvestPlanMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageOrderMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundPackageMessageMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageSortInstorageMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageStockLossMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.PlantingBatchDataStatisticsListVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 生产数据统计表 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-08-20
 */
@Slf4j
@Service
public class ProductionManagePlantBatchDataStatisticsService implements BaseService<PlantingBatchDataStatisticsListVO> {


    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageHarvestPlanMapper harvestPlanMapper;

    @Autowired
    private ProductionManageStockLossMapper stockLossMapper;

    @Autowired
    private ProductionManageSortInstorageMapper sortInstorageMapper;

    @Autowired
    private ProductionManageOutboundMapper outboundMapper;

    @Autowired
    private ProductionManageOutboundPackageMessageMapper outboundPackageMessageMapper;

    @Autowired
    private ProductManageOrderMapper orderMapper;

    private static String defaultNameKey="dateTime";
    private static String defaultValueKey="totalValue";

    @Override
    public AbstractPageService.PageResults<List<PlantingBatchDataStatisticsListVO>> page(DaoSearch daoSearch) throws SuperCodeException {
        PlantingBatchDataStatisticsDTO plantingBatchDataStatisticsDTO = (PlantingBatchDataStatisticsDTO) daoSearch;
        Page<ProductionManageSortInstorage> page = new Page<>(plantingBatchDataStatisticsDTO.getDefaultCurrent(), plantingBatchDataStatisticsDTO.getDefaultPageSize());
        Integer batchType = plantingBatchDataStatisticsDTO.getBatchType();
        //查询批次信息
        QueryWrapper<ProductionManageSortInstorage> sortInstorageQueryWrapper = commonUtil.queryTemplate(ProductionManageSortInstorage.class);
        if (null != batchType) {
            if (batchType.intValue() == 1) {
                sortInstorageQueryWrapper.in(ProductionManageSortInstorage.COL_TYPE, 1, 3);
            } else if (batchType.intValue() == 2) {
                sortInstorageQueryWrapper.in(ProductionManageSortInstorage.COL_TYPE, 0, 2);
            } else {
                CommonUtil.throwSupercodeException(500, "批次类型不合法");
            }
        }
        sortInstorageQueryWrapper.eq(StringUtils.isNotBlank(plantingBatchDataStatisticsDTO.getBatchId()), ProductionManageSortInstorage.COL_PLANT_BATCH_ID, plantingBatchDataStatisticsDTO.getBatchId());
        sortInstorageQueryWrapper.groupBy(ProductionManageSortInstorage.COL_PLANT_BATCH_ID);
        IPage<ProductionManageSortInstorage> instorageIPage = sortInstorageMapper.selectPage(page, sortInstorageQueryWrapper);
        List<ProductionManageSortInstorage> instorageList = instorageIPage.getRecords();
        //封装批次统计数据
        List<PlantingBatchDataStatisticsListVO> batchDataStatisticsListVOList = getPlantingBatchDataStatisticsListVOS(instorageList);
        //重新构建分页对象
        AbstractPageService.PageResults<List<PlantingBatchDataStatisticsListVO>> pageResults = new AbstractPageService.PageResults<>();
        pageResults.setList(batchDataStatisticsListVOList);
        com.jgw.supercodeplatform.common.pojo.common.Page page1 = new com.jgw.supercodeplatform.common.pojo.common.Page((int) instorageIPage.getSize(), (int) instorageIPage.getCurrent(), (int) instorageIPage.getTotal());
        pageResults.setPagination(page1);
        return pageResults;
    }

    @Override
    public List<PlantingBatchDataStatisticsListVO> listExcelByIds(List<? extends Serializable> ids) throws SuperCodeException {
        QueryWrapper<ProductionManageSortInstorage> sortInstorageQueryWrapper = commonUtil.queryTemplate(ProductionManageSortInstorage.class);
        sortInstorageQueryWrapper.in(ProductionManageSortInstorage.COL_PLANT_BATCH_ID, ids);
        sortInstorageQueryWrapper.groupBy(ProductionManageSortInstorage.COL_PLANT_BATCH_ID);
        List<ProductionManageSortInstorage> instorageList = sortInstorageMapper.selectList(sortInstorageQueryWrapper);
        return getPlantingBatchDataStatisticsListVOS(instorageList);
    }

    @Override
    public void dataTransfer(List<PlantingBatchDataStatisticsListVO> list) throws SuperCodeException {
        if (CollectionUtils.isNotEmpty(list)){
            for (PlantingBatchDataStatisticsListVO statisticsListVO:list ) {
                statisticsListVO.setBatchType(BatchTypesEnum.getDesc(statisticsListVO.getBatchType()));
            }
        }
    }

    /**
     * 根据批次集合转化统计批次数据
     * @param instorageList
     * @return
     * @throws SuperCodeException
     */
    private List<PlantingBatchDataStatisticsListVO> getPlantingBatchDataStatisticsListVOS(List<ProductionManageSortInstorage> instorageList) throws SuperCodeException {
        List<PlantingBatchDataStatisticsListVO> batchDataStatisticsListVOList=new ArrayList<>();
        if (CollectionUtils.isNotEmpty(instorageList)){
            for (ProductionManageSortInstorage sortInstorage:instorageList) {
                PlantingBatchDataStatisticsListVO batchDataStatisticsListVO=new PlantingBatchDataStatisticsListVO();
                String plantBatchId=sortInstorage.getPlantBatchId();
                int plantBatchType=sortInstorage.getType();
                batchDataStatisticsListVO.setBatchId(plantBatchId);
                batchDataStatisticsListVO.setBatchName(sortInstorage.getPlantBatchName());
                batchDataStatisticsListVO.setBatchType((plantBatchType==1||plantBatchType==3)?"1":"2");
                //批次入库重量
                BigDecimal inStorageWeight=statiticsBatchInStorageWeight(plantBatchId).setScale(2,BigDecimal.ROUND_HALF_UP);
                batchDataStatisticsListVO.setInStorageWeight(inStorageWeight);
                //报损重量
                BigDecimal lossWeight=statiticsBatchLossWeight(plantBatchId);
                batchDataStatisticsListVO.setLossesWeight(lossWeight);
                //订单数
                Integer batchOrderNum=statiticsBatchOrderNum(plantBatchId);
                batchDataStatisticsListVO.setOrderNum(batchOrderNum);
                //设置销售额,实收额差额
                setBatchOrderMoneyReceivedMoney(plantBatchId,batchDataStatisticsListVO);


                Map<String,Object> returnMap=statiticsBatchReturnWeight(plantBatchId);
                if (null==returnMap || returnMap.isEmpty()){
                    batchDataStatisticsListVO.setReturnOrderNum(0);
                    batchDataStatisticsListVO.setReturnBoxNum(0);
                    batchDataStatisticsListVO.setReturnWeight(new BigDecimal(0));
                    batchDataStatisticsListVO.setReturnNum(0);
                }else {
                    //退货订单数
                    Number batchRetunOrderNum=(Number) returnMap.get("returnOrderNum");
                    batchDataStatisticsListVO.setReturnOrderNum(batchRetunOrderNum.intValue());

                    //退货重量
                    Number batchRetunWeight=(Number) returnMap.get("returnWeight");
                    batchDataStatisticsListVO.setReturnWeight(new BigDecimal(batchRetunWeight.doubleValue()).setScale(2,BigDecimal.ROUND_HALF_UP));

                    //退货数量
                    Number returnQuantity=(Number) returnMap.get("returnQuantity");
                    batchDataStatisticsListVO.setReturnNum(returnQuantity.intValue());

                    //退货箱数
                    Number returnBoxQuantity=(Number) returnMap.get("returnBoxQuantity");
                    batchDataStatisticsListVO.setReturnBoxNum(returnBoxQuantity.intValue());
                }
                //拒收订单数
                Integer batchRejectOrderNum=statiticsBatchRejectOrderNum(plantBatchId);
                batchDataStatisticsListVO.setRejectOrderNum(batchRejectOrderNum);
                batchDataStatisticsListVOList.add(batchDataStatisticsListVO);
            }
        }
        return batchDataStatisticsListVOList;
    }

    /**
     * 设置销售额，实收额，差额
     * @param plantBatchId
     * @param batchDataStatisticsListVO
     */
    private void setBatchOrderMoneyReceivedMoney(String plantBatchId, PlantingBatchDataStatisticsListVO batchDataStatisticsListVO) throws SuperCodeException {
        List<Long> orderIds=outboundPackageMessageMapper.selectOrderIdsByBatchId(plantBatchId);
        if (CollectionUtils.isEmpty(orderIds)){
            batchDataStatisticsListVO.setOrderMoney(new BigDecimal(0));
            batchDataStatisticsListVO.setReceivedOrderMoney(new BigDecimal(0));
            batchDataStatisticsListVO.setDifferenceOrderMoney(new BigDecimal(0));
        }else {
            for (Long orderId:orderIds) {
                Map<String,Object> weightAndNumMap=outboundPackageMessageMapper.statiticsBatchPcakWeightAndNumByBatchId(plantBatchId,orderId);
                Number batchpackingWeight= (Number) weightAndNumMap.get("packingWeight");
                Number batchpackingNum= (Number) weightAndNumMap.get("packingNum");

                Map<String,Object> allweightAndNumAndMonenyMap=outboundPackageMessageMapper.statiticsBatchPcakWeightAndNumByOrderId(orderId);
                Number allPackingWeight= (Number) allweightAndNumAndMonenyMap.get("packingWeight");
                Number allPackingNum= (Number) allweightAndNumAndMonenyMap.get("packingNum");
                Double orderMoney= (Double) allweightAndNumAndMonenyMap.get("orderMoney");
                Double receivedOrderMoney= (Double) allweightAndNumAndMonenyMap.get("receivedOrderMoney");
                //算比例
                BigDecimal weightBili=new BigDecimal(0);
                BigDecimal numBili=new BigDecimal(0);
                if (allPackingWeight.doubleValue()!=0 && batchpackingWeight.doubleValue()!=0){
                    weightBili=new BigDecimal(batchpackingWeight.doubleValue()).divide(new BigDecimal(allPackingWeight.doubleValue()),2,BigDecimal.ROUND_HALF_UP);
                }
                if (allPackingNum.doubleValue()!=0 && batchpackingNum.doubleValue()!=0){
                    numBili=new BigDecimal(batchpackingNum.doubleValue()).divide(new BigDecimal(allPackingNum.doubleValue()),2,BigDecimal.ROUND_HALF_UP);
                }

                BigDecimal ordermoneyNumDecimal=new BigDecimal(orderMoney).multiply(numBili);
                BigDecimal ordermoneyWeightDecimal=new BigDecimal(orderMoney).multiply(weightBili);

                BigDecimal receivedordermoneyNumDecimal=new BigDecimal(receivedOrderMoney).multiply(numBili);
                BigDecimal receivedordermoneyDecimal=new BigDecimal(receivedOrderMoney).multiply(weightBili);

                batchDataStatisticsListVO.setOrderMoney(CommonUtil.bigDecimalAdd(batchDataStatisticsListVO.getOrderMoney(),CommonUtil.bigDecimalAdd(ordermoneyNumDecimal,ordermoneyWeightDecimal)).setScale(2,BigDecimal.ROUND_HALF_UP));
                batchDataStatisticsListVO.setReceivedOrderMoney(CommonUtil.bigDecimalAdd(batchDataStatisticsListVO.getReceivedOrderMoney(),CommonUtil.bigDecimalAdd(receivedordermoneyNumDecimal,receivedordermoneyDecimal)).setScale(2,BigDecimal.ROUND_HALF_UP));
            }
            batchDataStatisticsListVO.setDifferenceOrderMoney(CommonUtil.bigDecimalSub(batchDataStatisticsListVO.getReceivedOrderMoney(),batchDataStatisticsListVO.getOrderMoney()).setScale(2,BigDecimal.ROUND_HALF_UP));
        }
    }

    /**
     * 统计批次退货数量，重量，箱数，退货订单数
     * @param plantBatchId
     * @return
     * @throws SuperCodeException
     */
    private Map<String,Object> statiticsBatchReturnWeight(String plantBatchId) throws SuperCodeException {
        return outboundPackageMessageMapper.statiticsBatchReturnInfoByBatchId(plantBatchId);
    }

    /**
     * 统计批次拒收订单数
     * @param plantBatchId
     * @return
     */
    private Integer statiticsBatchRejectOrderNum(String plantBatchId) {
        return outboundPackageMessageMapper.statiticsBatchRejectOrderNumByBatchId(plantBatchId);
    }

    /**
     * 根据批次统计所有订单数
     * @param plantBatchId
     * @return
     */
    private Integer statiticsBatchOrderNum(String plantBatchId) {
       Integer orderNum= outboundPackageMessageMapper.statiticsBatchOrderNumByBatchId(plantBatchId);
       return orderNum;
    }

    /**
     * 根据批次统计报损重量
     * @param plantBatchId
     * @return
     * @throws SuperCodeException
     */
    private BigDecimal statiticsBatchLossWeight(String plantBatchId) {
        BigDecimal stockLossWeight = stockLossMapper.sumWeightByPlantBatchId(plantBatchId);
        return stockLossWeight.setScale(2,BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 根据批次统计入库重量
     * @param plantBatchId
     * @return
     * @throws SuperCodeException
     */
    private BigDecimal statiticsBatchInStorageWeight(String plantBatchId) {
        BigDecimal sortInstorageWeight=sortInstorageMapper.sumWeightByPlantBatchId(plantBatchId);
        return sortInstorageWeight.setScale(2,BigDecimal.ROUND_HALF_UP);
    }
}