package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchPPDRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsNonProductProductionData;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsProductProductionData;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageStatisticsNonProductProductionDataMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageStatisticsProductProductionDataMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchPPDPageResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchPPDResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * <p>产品产量服务实现类</p>
 * 该类主要用来处理产品等级存在时的产品产量相关数据
 *
 * @author shixiongfei
 * @since 2019-10-29
 */
@Service
public class ProductionManageStatisticsProductProductionDataService extends ServiceImpl<ProductionManageStatisticsProductProductionDataMapper, ProductionManageStatisticsProductProductionData> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageStatisticsNonProductProductionDataMapper nonMapper;

    /**
     * 获取产品产量数据列表
     *
     * @author shixiongfei
     * @date 2019-10-29
     * @updateDate 2019-10-29
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public PageResults<List<SearchPPDPageResponseVO>> list(SearchPPDRequestDTO requestDTO) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        String date = LocalDateTimeUtil.contactDates(requestDTO.getStartQueryDate(), requestDTO.getEndQueryDate());

        IPage<ProductionManageStatisticsProductProductionData> iPage = baseMapper.list(requestDTO, sysId, organizationId);
        Page pagination = new Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());

        List<ProductionManageStatisticsProductProductionData> records = Optional.ofNullable(iPage.getRecords())
                .orElse(Collections.emptyList());

        List<SearchPPDPageResponseVO> list = new ArrayList<>(records.size());
        records.forEach(record -> {
            if (Objects.isNull(record)) {
                return;
            }
            SearchPPDPageResponseVO responseVO = new SearchPPDPageResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            // 设置产量 => 取自分拣入库的重量
            responseVO.setYield(record.getInboundWeight());
            // v1.9 设置商品产品产量 => 取自分拣入库的重量
            responseVO.setCommodityProductWeight(record.getInboundWeight());
            // 设置商品率
            BigDecimal commodityRates = getCommodityRates(requestDTO, sysId, organizationId);
            responseVO.setCommodityRate(commodityRates.toString());
            // 设置时间
            responseVO.setHarvestDate(date);
            list.add(responseVO);
        });
        return new PageResults<>(list, pagination);
    }


    /**
     * 获取产品产量柱状图相关数据
     *
     * @author shixiongfei
     * @date 2019-10-29
     * @updateDate 2019-10-29
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public SearchPPDResponseVO histogram(SearchPPDRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        List<ProductionManageStatisticsProductProductionData> list =
                Optional.ofNullable(baseMapper.histogram(requestDTO, sysId, organizationId))
                .orElse(Collections.emptyList());

        SearchPPDResponseVO responseVO = new SearchPPDResponseVO();
        List<String> productNames = new ArrayList<>(list.size());
        List<BigDecimal> commodityProductWeights = new ArrayList<>(list.size());
        List<BigDecimal> yields = new ArrayList<>(list.size());
        List<BigDecimal> commodityRates = new ArrayList<>(list.size());
        // 以产品id作为唯一标识数据
        list.forEach(data -> {
            if (Objects.isNull(data)) {
                return;
            }
            productNames.add(data.getProductName());
            // 产品等级存在时，其产量取自分拣入库的重量
            yields.add(data.getInboundWeight());
            // 商品产品产量取自非看入库的重量
            commodityProductWeights.add(data.getInboundWeight());
            // 设置商品率
            BigDecimal commodityRate = getCommodityRates(requestDTO, sysId, organizationId);
            commodityRates.add(commodityRate);
        });

        responseVO.setProductNames(productNames);
        responseVO.setYields(yields);
        responseVO.setCommodityProductWeights(commodityProductWeights);
        responseVO.setCommodityRates(commodityRates);

        return responseVO;
    }

    /**
     * 获取商品率
     *
     * @author shixiongfei
     * @date 2019-10-30
     * @updateDate 2019-10-30
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private BigDecimal getCommodityRates(SearchPPDRequestDTO requestDTO, String sysId, String organizationId) {
        // 获取入库总重量，产品称重总重量
        ProductionManageStatisticsNonProductProductionData nonData =
                nonMapper.getByProductId(requestDTO, sysId, organizationId);

        BigDecimal commodityRate;
        BigDecimal yield = nonData.getYield();
        BigDecimal inboundWeight = nonData.getInboundWeight();
        if (yield.signum() == 0 && inboundWeight.signum() == 0) {
            commodityRate = BigDecimal.ZERO;
        } else if (yield.signum() == 0) {
            commodityRate = BigDecimal.valueOf(100);
        } else {
            commodityRate = inboundWeight.multiply(BigDecimal.valueOf(100)).divide(yield, 2, BigDecimal.ROUND_HALF_UP);
        }

        return commodityRate;
    }

    /**
     * 产品产量数据导出
     *
     * @author shixiongfei
     * @date 2019-10-29
     * @updateDate 2019-10-29
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