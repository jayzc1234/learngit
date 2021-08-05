package com.zxs.server.facade;

import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductPlantingYieldStatisticVO;
import net.app315.hydra.user.common.web.base.FeignConfig;
import net.app315.nail.common.result.RichResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "${rest.trace.url}",
        path = "/trace/hainanrunda/massifinfostatistic",
        configuration = FeignConfig.class)
public interface HarvestYieldClient {

    @GetMapping("/getPlantingInfoStatistic")
    RichResult<ProductPlantingYieldStatisticVO> getPlantingInfoStatistic();
}
