package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DepartmentRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ExportStockLossStatisticsPageRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchStockLossPageRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchStockLossStatisticsRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStockLoss;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageStatisticsReportLossMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.StockLossStatisticsMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchStockLossPageResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchStockLossStatisticsResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 库存报损分析服务类
 * @author shixiongfei
 * @date 2019-09-18
 * @since
 */
@Service
public class StockLossStatisticsService extends ServiceImpl<StockLossStatisticsMapper, ProductionManageStockLoss> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageStatisticsReportLossService reportLossService;

    @Autowired
    private ProductionManageStatisticsReportLossMapper reportLossMapper;

    /**
     * 获取库存报损
     *
     * @author shixiongfei
     * @date 2019-09-18
     * @updateDate 2019-09-18
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Deprecated
    public AbstractPageService.PageResults<List<SearchStockLossPageResponseVO>> list(SearchStockLossPageRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        IPage<ProductionManageStockLoss> iPage = baseMapper.list(requestDTO, sysId, organizationId);

        AbstractPageService.PageResults<List<SearchStockLossPageResponseVO>> pageResults = new AbstractPageService.PageResults<>();
        com.jgw.supercodeplatform.common.pojo.common.Page pagination =
                new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());

        pageResults.setPagination(pagination);

        List<ProductionManageStockLoss> records = Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());

        List<SearchStockLossPageResponseVO> result = records.stream().map(record -> {
            SearchStockLossPageResponseVO responseVO = new SearchStockLossPageResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            // 设置当前行数据的唯一标识id
            // responseVO.setId(record.getPlantBatchId() + ":::" + record.getDepartmentId());
            // 通过分拣类型获取批次类型
            responseVO.setType(record.getType().toString());
            return responseVO;
        }).collect(Collectors.toList());

        pageResults.setList(result);

        return pageResults;
    }

    /**
     * 获取库存b报损数据统计列表
     *
     * @author shixiongfei
     * @date 2019-09-18
     * @updateDate 2019-09-18
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Deprecated
    public SearchStockLossStatisticsResponseVO listStatistics(SearchStockLossStatisticsRequestDTO requestDTO) throws SuperCodeException {
        // 校验开始时间是否小于结束时间
        boolean isTrue = LocalDate.parse(requestDTO.getStartQueryDate()).isAfter(LocalDate.parse(requestDTO.getEndQueryDate()));
        if (isTrue) {
            CustomAssert.throwException("开始时间不可大于结束时间");
        }

        // 获取完整的时间段
        List<String> dateInterval = LocalDateTimeUtil.getDateInterval(requestDTO.getStartQueryDate(), requestDTO.getEndQueryDate());

        // 获取时间区间内的库存报损数据列表
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        // 将sql和业务逻辑代码进行分离
        List<ProductionManageStockLoss> list = baseMapper.listStatistics(requestDTO, sysId, organizationId);

        // 获取所有部门集合
        List<DepartmentRequestDTO> departments = requestDTO.getDepartments();
        // 初始化响应体
        SearchStockLossStatisticsResponseVO responseVO = initResponseVO(departments, dateInterval);

        // 结果集不为空时做过滤垃圾数据处理
        if (CollectionUtils.isNotEmpty(list)) {
            list = list.stream()
                    .filter(stockLoss -> StringUtils.isNotBlank(stockLoss.getDepartmentId()) || !"0".equals(stockLoss.getDepartmentId()))
                    .collect(Collectors.toList());
        }

        // 结果集为空则处理空的数据统计结果响应体
        if (CollectionUtils.isEmpty(list)) {
            departments.forEach(department -> handleEmptyDepartmentData(responseVO, department.getDepartmentName(), dateInterval));
            return responseVO;
        }

        // 处理非空下的数据统计
        for (DepartmentRequestDTO department : departments) {
            List<ProductionManageStockLoss> lossList = list.stream().filter(stockLoss -> department.getDepartmentId().equals(stockLoss.getDepartmentId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(lossList)) {
                handleEmptyDepartmentData(responseVO, department.getDepartmentName(), dateInterval);
            } else {
                handleDepartmentData(responseVO, department.getDepartmentName(), dateInterval, lossList);
            }
        }

        return responseVO;
    }


    /**
     * 初始化响应体
     *
     * @author shixiongfei
     * @date 2019-09-18
     * @updateDate 2019-09-18
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Deprecated
    public SearchStockLossStatisticsResponseVO initResponseVO(List<DepartmentRequestDTO> departments, List<String> dateInterval) {
        SearchStockLossStatisticsResponseVO responseVO = new SearchStockLossStatisticsResponseVO();
        List<LineChartVO> vos = new ArrayList<>(departments.size());
        departments.forEach(department -> {
            LineChartVO vo = new LineChartVO();
            LineChartVO.Option option = vo.new Option();
            option.setName(department.getDepartmentName());
            vo.setOption(option);
            List<LineChartVO.NameAndValueVO> valueVOS = new ArrayList<>(dateInterval.size());
            vo.setValues(valueVOS);
            vos.add(vo);
        });

        responseVO.setValues(vos);
        return responseVO;
    }
    /**
     * 处理库存报损的每个部门的统计数据
     *
     * @author shixiongfei
     * @date 2019-09-18
     * @updateDate 2019-09-18
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Deprecated
    public void handleDepartmentData(SearchStockLossStatisticsResponseVO responseVO, String departmentName,
                                                              List<String> dateInterval, List<ProductionManageStockLoss> lossList) {

        List<LineChartVO> values = responseVO.getValues();
        LineChartVO vo = values.stream().filter(value -> value.getOption().getName().equals(departmentName)).findFirst().get();
        List<LineChartVO.NameAndValueVO> valueVOS = vo.getValues();

        Map<String, BigDecimal> maps = lossList.stream().collect(Collectors.toMap(loss -> DateFormatUtils.format(loss.getDamageDate(), "yyyy-MM-dd"),
                loss -> Optional.ofNullable(loss.getDamageWeight()).orElse(BigDecimal.ZERO)));
        dateInterval.forEach(date -> {
            LineChartVO.NameAndValueVO nameAndValueVO = vo.new NameAndValueVO();
            nameAndValueVO.setName(date);
            nameAndValueVO.setValue(maps.getOrDefault(date, BigDecimal.ZERO));
            valueVOS.add(nameAndValueVO);
        });
    }

    /**
     * 处理为空时的部门统计数据
     *
     * @author shixiongfei
     * @date 2019-09-18
     * @updateDate 2019-09-18
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Deprecated
    public void handleEmptyDepartmentData(SearchStockLossStatisticsResponseVO responseVO, String departmentName,
                                                                   List<String> dateInterval) {

        List<LineChartVO> values = responseVO.getValues();
        LineChartVO vo = values.stream().filter(value -> value.getOption().getName().equals(departmentName)).findFirst().get();
        List<LineChartVO.NameAndValueVO> valueVOS = vo.getValues();
        dateInterval.forEach(date -> {
            LineChartVO.NameAndValueVO nameAndValueVO = vo.new NameAndValueVO();
            nameAndValueVO.setName(date);
            nameAndValueVO.setValue(BigDecimal.ZERO);
            valueVOS.add(nameAndValueVO);
        });
    }

    /**
     *
     *
     * @author shixiongfei
     * @date 2019-09-19
     * @updateDate 2019-09-19
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Deprecated
    public List<SearchStockLossPageResponseVO> excelByIds(List<String> ids, ExportStockLossStatisticsPageRequestDTO requestDTO ) {

        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        List<ProductionManageStockLoss> list = baseMapper.excelByIds(ids, requestDTO, sysId, organizationId);

        return list.stream().map(stockLoss -> {
            SearchStockLossPageResponseVO responseVO = new SearchStockLossPageResponseVO();
            BeanUtils.copyProperties(stockLoss, responseVO);
            // 通过分拣类型获取批次类型
            responseVO.setType(stockLoss.getType().toString());
            return responseVO;
        }).collect(Collectors.toList());
    }

    /**
     * 库存报损数据分析导出
     *
     * @author shixiongfei
     * @date 2019-09-19
     * @updateDate 2019-09-19
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Deprecated
    public void export(ExportStockLossStatisticsPageRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<String> idList = requestDTO.getIdList();
        requestDTO.setCurrent(1);
        requestDTO.setPageSize(commonUtil.getExportNumber());
        SearchStockLossPageRequestDTO lossPageRequestDTO = new SearchStockLossPageRequestDTO();
        BeanUtils.copyProperties(requestDTO, lossPageRequestDTO);
        // 如果id为空则导出全部，否则导出指定
        List<SearchStockLossPageResponseVO> list = CollectionUtils.isEmpty(idList) ?
                list(lossPageRequestDTO).getList() : excelByIds(idList, requestDTO);
        // 将批次类型转换为指定类型
        list.forEach(responseVO -> responseVO.setType("1".equals(responseVO.getType()) ? "种植批次" : "外采批次"));
        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "库存报损分析列表", response);
    }
}