package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.DataStatisticsConstants;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchPPDRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsNonProductProductionData;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageStatisticsNonProductProductionDataMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchPPDPageResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchPPDResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * 该类主要用来处理产品等级不存在时的产品产量相关数据
 *
 * @author shixiongfei
 * @since 2019-10-29
 */
@Service
public class ProductionManageStatisticsNonProductProductionDataService extends ServiceImpl<ProductionManageStatisticsNonProductProductionDataMapper, ProductionManageStatisticsNonProductProductionData> {

    @Autowired
    private CommonUtil commonUtil;

    /**
     * 获取非产品等级下的柱状图信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-29
     * @updateDate 2019-10-29
     * @updatedBy shixiongfei
     */
    public SearchPPDResponseVO histogram(SearchPPDRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        List<ProductionManageStatisticsNonProductProductionData> list =
                Optional.ofNullable(baseMapper.histogram(requestDTO, sysId, organizationId))
                        .orElse(Collections.emptyList());

        SearchPPDResponseVO responseVO = new SearchPPDResponseVO();

        List<BigDecimal> commodityProductWeights = new ArrayList<>(list.size());
        List<String> productNames = new ArrayList<>(list.size());
        List<BigDecimal> yields = new ArrayList<>(list.size());
        List<BigDecimal> commodityRates = new ArrayList<>(list.size());

        // 以产品id作为唯一标识数据
        list.forEach(data -> {
            if (Objects.isNull(data)) {
                return;
            }
            productNames.add(data.getProductName());
            yields.add(data.getYield());
            // 商品产品产量取自分拣入库的重量
            commodityProductWeights.add(data.getInboundWeight());
            // 设置商品率 商品率 = 分拣入库的重量 / 产品称重的重量
            BigDecimal commodityRate = getCommodityRate(data.getYield(), data.getInboundWeight());
            commodityRates.add(commodityRate);
        });

        responseVO.setProductNames(productNames);
        responseVO.setYields(yields);
        responseVO.setCommodityProductWeights(commodityProductWeights);
        responseVO.setCommodityRates(commodityRates);

        Double totalCommodityRates = null;
        if(StringUtils.isEmpty(requestDTO.getProductId())){
            Double totalInboundWeight= list.stream().mapToDouble(e->e.getInboundWeight().doubleValue()).sum();
            Double totalYield= list.stream().mapToDouble(e->e.getYield().doubleValue()).sum();
            totalCommodityRates = getCommodityRate(new BigDecimal(totalYield), new BigDecimal(totalInboundWeight)).doubleValue();
            /*totalCommodityRates= totalInboundWeight*100/totalYield;
            totalCommodityRates=NumberUtil.retainTwoDecimal(totalCommodityRates.toString());*/
        } else {
            if(CollectionUtils.isNotEmpty(commodityRates)){
                totalCommodityRates=commodityRates.get(0).doubleValue();
            }
        }
        responseVO.setTotalCommodityRates(totalCommodityRates);

        return responseVO;
    }

    /**
     * 获取非产品等级下的产品产量数据统计列表
     *
     * @author shixiongfei
     * @date 2019-10-29
     * @updateDate 2019-10-29
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public AbstractPageService.PageResults<List<SearchPPDPageResponseVO>> list(SearchPPDRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        String date = LocalDateTimeUtil.contactDates(requestDTO.getStartQueryDate(), requestDTO.getEndQueryDate());

        IPage<ProductionManageStatisticsNonProductProductionData> iPage = baseMapper.list(requestDTO, sysId, organizationId);

        Page pagination = new Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());

        List<ProductionManageStatisticsNonProductProductionData> records =
                Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());

        List<SearchPPDPageResponseVO> result = new ArrayList<>(records.size());
        records.forEach(record -> {
            if (Objects.isNull(record)) {
                return;
            }
            SearchPPDPageResponseVO responseVO = new SearchPPDPageResponseVO();
            BeanUtils.copyProperties(record, responseVO);

            // v1.9 设置商品产品产量 => 取自分拣入库的重量
            responseVO.setCommodityProductWeight(record.getInboundWeight());

            // 设置商品率
            BigDecimal commodityRate = getCommodityRate(record.getYield(), record.getInboundWeight());
            responseVO.setCommodityRate(commodityRate.toString());

            // 设置产品等级为全部
            responseVO.setProductLevelCode(DataStatisticsConstants.ALL);
            responseVO.setProductLevelName(DataStatisticsConstants.ALL);

            // 设置时间
            responseVO.setHarvestDate(date);
            result.add(responseVO);
        });

        return new AbstractPageService.PageResults<>(result, pagination);
    }

    /**
     * 获取商品率
     * 商品率 = 分拣入库的重量 / 产品称重的重量
     *
     * @author shixiongfei
     * @date 2019-10-30
     * @updateDate 2019-10-30
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private BigDecimal getCommodityRate(BigDecimal yield, BigDecimal inboundWeight) {
        BigDecimal commodityRate;
        if (yield.signum() == 0 && inboundWeight.signum() == 0) {
            commodityRate = BigDecimal.ZERO;
        } else if (yield.signum() == 0) {
            commodityRate = BigDecimal.valueOf(100);
        } else {
            commodityRate = inboundWeight.multiply(BigDecimal.valueOf(100))
                    .divide(yield, 2, BigDecimal.ROUND_HALF_UP);
        }
        return commodityRate;
    }

    /**
     * 非产品等级产品产量数据统计导出
     *
     * @author shixiongfei
     * @date 2019-10-30
     * @updateDate 2019-10-30
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void export(SearchPPDRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<SearchPPDPageResponseVO> list = requestDTO.parseStr2List(SearchPPDPageResponseVO.class);
        if (CollectionUtils.isEmpty(list)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = list(requestDTO).getList();
        }
        ExcelUtils.listToExcel(list,requestDTO.exportMetadataToMap(),"产品产量数据统计列表", response);
    }
}