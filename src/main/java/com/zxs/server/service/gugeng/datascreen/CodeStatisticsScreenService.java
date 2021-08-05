package com.zxs.server.service.gugeng.datascreen;

import com.alibaba.fastjson.JSONObject;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.pojo.gugeng.datascreen.ProductionManageCodeScanStatisticsVo;
import net.app315.hydra.intelligent.planting.pojo.gugeng.datascreen.ProductionManageRundaCodeStatisticsVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CodeStatisticsScreenService extends CommonUtil {

    @Value("${rest.codemanager.url}")
    public String codeManageDomain;

    public ProductionManageRundaCodeStatisticsVo selectData() throws SuperCodeException {

        String url = codeManageDomain + "/code/scanstatistics/getScanStatistics";

        Map<String, String> headerMap = new HashMap<>(1);
        headerMap.put("super-token", getSuperToken() );

        Map<String, Object> params = new HashMap<>(2);
        params.put("organizationId", getOrganizationId());

        ProductionManageRundaCodeStatisticsVo statisticsVo=null;

        try {
            String result = codeRequests.getAndGetResultBySpring(url, params, headerMap, String.class, true);
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (jsonObject.getIntValue("state") == 200) {
                JSONObject resultsObject = jsonObject.getJSONObject("results");
                String data= resultsObject.toString();
                statisticsVo= JSONObject.parseObject(data, ProductionManageRundaCodeStatisticsVo.class);

                List<ProductionManageCodeScanStatisticsVo> scanStatisticsVos= resultsObject.getJSONArray("productScanStatisticsList").toJavaList(ProductionManageCodeScanStatisticsVo.class);
                statisticsVo.setTraceProducts( scanStatisticsVos);

                statisticsVo.setProvinceStatisticsList(statisticsVo.getProvinceStatisticsList().stream().limit(15).collect(Collectors.toList()));
                statisticsVo.setCityStatisticsList( statisticsVo.getCityStatisticsList().stream().limit(15).collect(Collectors.toList()));
                statisticsVo.setProductRelationStatisticsList( statisticsVo.getProductRelationStatisticsList().stream().limit(15).collect(Collectors.toList()));
                statisticsVo.setProductScanStatisticsList(statisticsVo.getProductScanStatisticsList().stream().limit(15).collect(Collectors.toList()));
                statisticsVo.setTraceProducts( statisticsVo.getTraceProducts().stream().limit(15).collect(Collectors.toList()));

            } else {
                CustomAssert.throwException("获取采收重量统计信息失败");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            CustomAssert.throwException("获取采收重量统计信息失败");
        }

        return statisticsVo;
    }

}
