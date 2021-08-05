package com.zxs.server.service.gugeng.statistics;


import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalDTO;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageSortInstorageMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class ProductionManageStorageStatisticsService {
    @Autowired
    private ProductionManageSortInstorageMapper sortInstorageMapper;

    @Autowired
    private ProductionManageOutboundMapper outboundMapper;

    @Autowired
    private CommonUtil commonUtil;

    public RestResult<LineChartVO> outInWeightStatistics(DateIntervalDTO dateIntervalDTO) {
        RestResult<LineChartVO> restResult = new RestResult<>();
        dateIntervalDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalDTO.setSysId(commonUtil.getSysId());
        //分拣入库
        Double allInWeight = Optional.ofNullable(sortInstorageMapper.statisticsSortInWeight(dateIntervalDTO)).orElse(0D);

        //盘点出库
        Double outWeight = outboundMapper.statisticsOutBoundWeight(dateIntervalDTO);
        Double stackLossOutWeight = outboundMapper.statisticsStackAndLossOutWeight(dateIntervalDTO);

        Double allOutWeight = (outWeight == null ? 0 : outWeight) + (stackLossOutWeight == null ? 0 : stackLossOutWeight);
        LineChartVO lineChartVO = new LineChartVO();
        List<LineChartVO.NameAndValueVO> values = new ArrayList<>();
        LineChartVO.NameAndValueVO sortinNameAndValueVO = lineChartVO.new NameAndValueVO();
        sortinNameAndValueVO.setName("入库");
        sortinNameAndValueVO.setValue(new BigDecimal(allInWeight).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        values.add(sortinNameAndValueVO);
        LineChartVO.NameAndValueVO outboundNameAndValueVO = lineChartVO.new NameAndValueVO();
        outboundNameAndValueVO.setName("出库");
        outboundNameAndValueVO.setValue(new BigDecimal(allOutWeight).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        values.add(outboundNameAndValueVO);
        lineChartVO.setValues(values);
        restResult.setState(200);
        restResult.setResults(lineChartVO);
        return restResult;
    }
}
