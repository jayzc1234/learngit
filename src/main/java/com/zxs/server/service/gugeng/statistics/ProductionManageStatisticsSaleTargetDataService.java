package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchSTDRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.SaleTaskTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsSaleTargetData;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageStatisticsSaleTargetDataMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchSTDHResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchSTDResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-23
 */
@Service
public class ProductionManageStatisticsSaleTargetDataService extends ServiceImpl<ProductionManageStatisticsSaleTargetDataMapper, ProductionManageStatisticsSaleTargetData> {

    @Autowired
    private CommonUtil commonUtil;

    /**
     * 获取销售目标列表
     *
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     */
    public PageResults<List<SearchSTDResponseVO>> list(SearchSTDRequestDTO requestDTO) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        String departmentId = requestDTO.getDepartmentId();
        String productId = requestDTO.getProductId();
        String salesPersonnelId = requestDTO.getSalesPersonnelId();

        // 设置时间
        List<String> startAndEndDate = LocalDateTimeUtil.getStartAndEndDate(requestDTO.getStartQueryDate(), requestDTO.getEndQueryDate());
        requestDTO.setStartQueryDate(startAndEndDate.get(0));
        requestDTO.setEndQueryDate(startAndEndDate.get(1));

        // 情况1 => 部门 + 产品 + 销售人员都为空
        boolean conditionOne = StringUtils.isBlank(departmentId) && StringUtils.isBlank(productId) && StringUtils.isBlank(salesPersonnelId);

        // 情况2 => 部门不为空, 产品 + 销售人员为空
        boolean conditionTwo = StringUtils.isNotBlank(departmentId) && StringUtils.isBlank(productId) && StringUtils.isBlank(salesPersonnelId);

        // 情况3 => 部门 + 产品不为空, 销售人员为空
        boolean conditionThree = StringUtils.isNotBlank(departmentId) && StringUtils.isNotBlank(productId) && StringUtils.isBlank(salesPersonnelId);

        // 情况4 => 部门 + 销售人员不为空, 产品为空
        boolean conditionFour = StringUtils.isNotBlank(departmentId) && StringUtils.isBlank(productId) && StringUtils.isNotBlank(salesPersonnelId);

        // 情况5 => 部门 + 产品 + 销售人员都不为空
        boolean conditionFive = StringUtils.isNotBlank(departmentId) && StringUtils.isNotBlank(productId) && StringUtils.isNotBlank(salesPersonnelId);

        // 情况6 => 产品不为空，部门 + 销售人员为空
        boolean conditionSix = StringUtils.isNotBlank(productId) && StringUtils.isBlank(departmentId) && StringUtils.isBlank(salesPersonnelId);

        IPage<ProductionManageStatisticsSaleTargetData> iPage = null;

        if (conditionOne) { iPage = baseMapper.listConditionOne(requestDTO, sysId, organizationId); }

        if (conditionTwo){ iPage = baseMapper.listConditionTwo(requestDTO, sysId, organizationId); }

        if (conditionThree) { iPage = baseMapper.listConditionThree(requestDTO, sysId, organizationId); }

        if (conditionFour) { iPage = baseMapper.listConditionFour(requestDTO, sysId, organizationId); }

        if (conditionFive) { iPage = baseMapper.listConditionFive(requestDTO, sysId, organizationId); }

        if (conditionSix) { iPage = baseMapper.listConditionSix(requestDTO, sysId, organizationId); }

        Page pagination = new Page((int) iPage.getSize(), (int)iPage.getCurrent(), (int)iPage.getTotal());
        List<ProductionManageStatisticsSaleTargetData> records =
                Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());

        List<SearchSTDResponseVO> list = records.stream().map(record -> {
            SearchSTDResponseVO responseVO = new SearchSTDResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            responseVO.setSaleTargetDate(DateFormatUtils.format(record.getSaleTargetDate(), LocalDateTimeUtil.YEAR_AND_MONTH));
            // 计算目标达成率
            BigDecimal actualSaleAmount = record.getActualSaleAmount();
            BigDecimal targetSaleAmount = record.getTargetSaleAmount();
            boolean isTrue = actualSaleAmount.signum() == 0 || (actualSaleAmount.signum() == 0 && targetSaleAmount.signum() == 0);
            // 目标销售额为空则目标达成率为100%, 实际销售额为空则为0%， 都为空则为0%
            if (isTrue) {
                responseVO.setTargetAchievementRate(BigDecimal.ZERO);
            } else if (targetSaleAmount.signum() == 0) {
                responseVO.setTargetAchievementRate(BigDecimal.valueOf(100));
            } else {
                responseVO.setTargetAchievementRate(actualSaleAmount.multiply(BigDecimal.valueOf(100))
                        .divide(targetSaleAmount, 2, BigDecimal.ROUND_HALF_UP));
            }
            return responseVO;
        }).collect(Collectors.toList());

        return new PageResults<>(list, pagination);
    }

    /**
     * 获取销售目标数据统计柱状图
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     */
    public SearchSTDHResponseVO listHistogram(SearchSTDRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        // 设置时间
        List<String> startAndEndDate = LocalDateTimeUtil.getStartAndEndDate(requestDTO.getStartQueryDate(), requestDTO.getEndQueryDate());
        requestDTO.setStartQueryDate(startAndEndDate.get(0));
        requestDTO.setEndQueryDate(startAndEndDate.get(1));

        // 获取时间区间
        List<LocalDate> dateInterval = LocalDateTimeUtil.getStartAndEndMonth(requestDTO.getStartQueryDate(), requestDTO.getEndQueryDate());

        SearchSTDHResponseVO responseVO = initResponseVO(dateInterval.size());

        List<BigDecimal> actualSaleAmounts = responseVO.getActualSaleAmounts();
        List<String> saleTargetDates = responseVO.getSaleTargetDates();
        List<BigDecimal> targetAchievementRates = responseVO.getTargetAchievementRates();
        List<BigDecimal> targetSaleAmounts = responseVO.getTargetSaleAmounts();

        Byte taskType = StringUtils.isNotBlank(requestDTO.getSalesPersonnelId())
                ? SaleTaskTypeEnum.PERSONAL.getKey()
                : SaleTaskTypeEnum.DEPARTMENT.getKey();


        List<ProductionManageStatisticsSaleTargetData> list =
                Optional.ofNullable(baseMapper.listHistogram(requestDTO, sysId, organizationId, taskType))
                .orElse(Collections.emptyList());

        Map<String, ProductionManageStatisticsSaleTargetData> map = list.stream()
                .collect(Collectors.toMap(data -> DateFormatUtils.format(data.getSaleTargetDate(),
                        LocalDateTimeUtil.YEAR_AND_MONTH), data -> data));

        dateInterval.forEach(date -> {
            String time = date.format(DateTimeFormatter.ofPattern(LocalDateTimeUtil.YEAR_AND_MONTH));
            saleTargetDates.add(time);
            if (map.containsKey(time)) {
                ProductionManageStatisticsSaleTargetData data = map.get(time);
                actualSaleAmounts.add(Optional.ofNullable(data.getActualSaleAmount()).orElse(BigDecimal.ZERO));
                targetSaleAmounts.add(Optional.ofNullable(data.getTargetSaleAmount()).orElse(BigDecimal.ZERO));

                // 计算目标达成率
                BigDecimal actualSaleAmount = data.getActualSaleAmount();
                BigDecimal targetSaleAmount = data.getTargetSaleAmount();
                boolean isTrue = actualSaleAmount.signum() == 0 || (actualSaleAmount.signum() == 0 && targetSaleAmount.signum() == 0);
                // 目标销售额为空则目标达成率为100%, 实际销售额为空则为0%， 都为空则为0%
                if (isTrue) {
                    targetAchievementRates.add(BigDecimal.ZERO);
                } else if(targetSaleAmount.signum() == 0) {
                    targetAchievementRates.add(BigDecimal.valueOf(100));
                } else {
                    targetAchievementRates.add(actualSaleAmount.multiply(BigDecimal.valueOf(100))
                            .divide(targetSaleAmount, 2, BigDecimal.ROUND_HALF_UP));
                }
            } else {
                actualSaleAmounts.add(BigDecimal.ZERO);
                targetAchievementRates.add(BigDecimal.ZERO);
                targetSaleAmounts.add(BigDecimal.ZERO);
            }
        });


        return responseVO;
    }

    /**
     * 初始化销售目标柱状图响应体
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     */
    private SearchSTDHResponseVO initResponseVO(Integer size) {
        SearchSTDHResponseVO responseVO = new SearchSTDHResponseVO();
        List<String> saleTargetDates = new ArrayList<>(size);
        List<BigDecimal> targetSaleAmounts = new ArrayList<>(size);
        List<BigDecimal> actualSaleAmounts = new ArrayList<>(size);
        List<BigDecimal> targetAchievementRates = new ArrayList<>(size);
        responseVO.setSaleTargetDates(saleTargetDates);
        responseVO.setTargetSaleAmounts(targetSaleAmounts);
        responseVO.setActualSaleAmounts(actualSaleAmounts);
        responseVO.setTargetAchievementRates(targetAchievementRates);
        return responseVO;
    }

    /**
     * 销售目标数据导出
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     */
    public void export(SearchSTDRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<SearchSTDResponseVO> list = requestDTO.parseStr2List(SearchSTDResponseVO.class);
        if (CollectionUtils.isEmpty(list)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = list(requestDTO).getList();
        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "销售目标数据列表", response);
    }
}
