package com.zxs.server.facade;

import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.BaseMassifbaseView;
import net.app315.hydra.user.common.web.base.FeignConfig;
import net.app315.nail.common.result.RichResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(value = "${rest.user.url}",
        path = "/mass",
        configuration = FeignConfig.class)
public interface MassifClient {

    @GetMapping("/enable/list")
    RichResult<AbstractPageService.PageResults<List<BaseMassifbaseView>>> list();


}
