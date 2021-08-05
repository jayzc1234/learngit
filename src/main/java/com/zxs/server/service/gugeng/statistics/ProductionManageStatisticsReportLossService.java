package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ExportStockLossStatisticsPageRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchStockLossPageRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.BatchTypesEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsReportLoss;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageStatisticsReportLossMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchStockLossPageResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchStockLossStatisticsResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.DataStatisticsConstants.STOCK_LOSS;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.export.ExportSheetNameConstants.INVENTORY_LOSS_DATA_STATISTICS_LIST;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-21
 */
@Service
public class ProductionManageStatisticsReportLossService extends ServiceImpl<ProductionManageStatisticsReportLossMapper, ProductionManageStatisticsReportLoss> {

    @Autowired
    private CommonUtil commonUtil;


    /**
     * 获取库存报损数据统计折线图信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-21
     * @updateDate 2019-10-21
     * @updatedBy shixiongfei
     */
    public SearchStockLossStatisticsResponseVO listStockLossLineChart(SearchStockLossPageRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        // 获取完整的时间段
        List<String> dateInterval = LocalDateTimeUtil.getDateInterval(requestDTO.getStartQueryDate(), requestDTO.getEndQueryDate());
        SearchStockLossStatisticsResponseVO responseVO = initResponseVO(dateInterval);
        // 获取报损总重量
        BigDecimal totalWeight = baseMapper.getTotalStockLossWeight(requestDTO, sysId, organizationId);
        responseVO.setTotalStockLossWeight(totalWeight);
        // 获取库存报损数据统计列表
        List<ProductionManageStatisticsReportLoss> reportLosses = baseMapper.listStockLossLineChart(requestDTO, sysId, organizationId);
        // 集合为空则直接返回默认数据集
        if (CollectionUtils.isEmpty(reportLosses)) {
            handleEmptyData(responseVO, dateInterval);
            return responseVO;
        }

        handleDepartmentData(responseVO, dateInterval, reportLosses);

        return responseVO;
    }

    /**
     * 获取库存报损数据统计列表信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-21
     * @updateDate 2019-10-21
     * @updatedBy shixiongfei
     */
    public AbstractPageService.PageResults<List<SearchStockLossPageResponseVO>> listStockLossStatistics(SearchStockLossPageRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        IPage<ProductionManageStatisticsReportLoss> iPage = baseMapper.listStockLossStatistics(requestDTO, sysId, organizationId);
        Page pagination = new Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        List<ProductionManageStatisticsReportLoss> records = Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());
        List<SearchStockLossPageResponseVO> list = records.stream().map(record -> {
            SearchStockLossPageResponseVO responseVO = new SearchStockLossPageResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            responseVO.setType(record.getSortingType().toString());
            responseVO.setDamageWeight(record.getReportLossWeight());
            return responseVO;
        }).collect(Collectors.toList());
        return new AbstractPageService.PageResults<>(list, pagination);
    }

    /**
     * 数据结果集为空，则对日期的数据做置0处理
     *
     * @author shixiongfei
     * @date 2019-12-25
     * @updateDate 2019-12-25
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private void handleEmptyData(SearchStockLossStatisticsResponseVO responseVO, List<String> dateInterval) {
        List<LineChartVO> values = responseVO.getValues();
        LineChartVO vo = values.get(0);
        List<LineChartVO.NameAndValueVO> valueVOS = vo.getValues();
        dateInterval.forEach(date -> {
            LineChartVO.NameAndValueVO nameAndValueVO = vo.new NameAndValueVO();
            nameAndValueVO.setName(date);
            nameAndValueVO.setValue(BigDecimal.ZERO);
            valueVOS.add(nameAndValueVO);
        });
    }

    /**
     * 初始化响应体
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-09-18
     * @updateDate 2019-09-18
     * @updatedBy shixiongfei
     */
    private SearchStockLossStatisticsResponseVO initResponseVO(List<String> dateInterval) {
        SearchStockLossStatisticsResponseVO responseVO = new SearchStockLossStatisticsResponseVO();
        List<LineChartVO> vos = new ArrayList<>(1);
        LineChartVO vo = new LineChartVO();
        LineChartVO.Option option = vo.new Option();
        option.setName(STOCK_LOSS);
        vo.setOption(option);
        List<LineChartVO.NameAndValueVO> valueVOS = new ArrayList<>(dateInterval.size());
        vo.setValues(valueVOS);
        vos.add(vo);

        responseVO.setValues(vos);
        return responseVO;
    }

    /**
     * 处理库存报损的统计数据
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-09-18
     * @updateDate 2019-09-18
     * @updatedBy shixiongfei
     */
    private void handleDepartmentData(SearchStockLossStatisticsResponseVO responseVO,
                                      List<String> dateInterval, List<ProductionManageStatisticsReportLoss> lossList) {

        List<LineChartVO> values = responseVO.getValues();
        LineChartVO vo = values.get(0);
        List<LineChartVO.NameAndValueVO> valueVOS = vo.getValues();
        Map<String, BigDecimal> maps = lossList.stream().collect(Collectors.toMap(loss -> DateFormatUtils.format(loss.getReportLossDate(), LocalDateTimeUtil.DATE_PATTERN),
                loss -> Optional.ofNullable(loss.getReportLossWeight()).orElse(BigDecimal.ZERO)));
        dateInterval.forEach(date -> {
            LineChartVO.NameAndValueVO nameAndValueVO = vo.new NameAndValueVO();
            nameAndValueVO.setName(date);
            nameAndValueVO.setValue(maps.getOrDefault(date, BigDecimal.ZERO));
            valueVOS.add(nameAndValueVO);
        });
    }

    /**
     * 库存报损数据统计excel导出
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-22
     * @updateDate 2019-10-22
     * @updatedBy shixiongfei
     */
    public void exportStockLoss(ExportStockLossStatisticsPageRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        requestDTO.setCurrent(1);
        requestDTO.setPageSize(commonUtil.getExportNumber());
        SearchStockLossPageRequestDTO lossPageRequestDTO = new SearchStockLossPageRequestDTO();
        BeanUtils.copyProperties(requestDTO, lossPageRequestDTO);

        List<SearchStockLossPageResponseVO> list = requestDTO.parseStr2List(SearchStockLossPageResponseVO.class);

        if (CollectionUtils.isEmpty(list)) {
            list = listStockLossStatistics(lossPageRequestDTO).getList();
        }

        list.forEach(vo -> vo.setType(BatchTypesEnum.getDesc(vo.getType())));
        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), INVENTORY_LOSS_DATA_STATISTICS_LIST, response);
    }
}