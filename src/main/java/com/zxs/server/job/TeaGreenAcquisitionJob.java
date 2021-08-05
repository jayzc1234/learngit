package com.zxs.server.job;

import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSuperToken;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageSuperTokenMapper;
import net.app315.hydra.intelligent.planting.server.service.fuding.teagreen.ITeaGreenBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 茶青收购生成批次job
 */
@Slf4j
@Component
public class TeaGreenAcquisitionJob {
    @Autowired
    private ITeaGreenBatchService teaGreenBatchService;


    @Autowired
    private ProductionManageSuperTokenMapper superTokenMapper;

    @Autowired
    private CommonUtil commonUtil;

    public void execute() {
        log.info("===========================茶青收购生成批次==============================");
        List<ProductionManageSuperToken> superTokenList = superTokenMapper.getSuperTokenList();
        superTokenList.forEach(superToken -> {
            String token = commonUtil.getToken(superToken.getOrganizationId(), superToken.getSysId());
            XxlJobLogger.log("当前茶青批次的组织id="+superToken.getOrganizationId()+",sysId="+superToken.getSysId()+",token="+token);
            if (token!=null) {
                teaGreenBatchService.saveTeaBatch(superToken.getOrganizationId(), token);
            }
        });
        log.info("===========================茶青收购生成批次=============================");
    }
}