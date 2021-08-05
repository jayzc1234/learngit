package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClient;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageClientMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.diagramtransfer.LineChartDataTransfer;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ClientDataCurveLineVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageClientOrderDataStatisticsListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageSaleClientNumStatisticsVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  客户数据统计公共类
 * </p>
 * @author zc
 * @since 2019-10-21
 */
@Service
@Slf4j
public class ProductionManageClientDataStatisticsService {


    @Autowired
    private ProductManageClientMapper clientMapper;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageSaleClientNumStatisticsService saleClientNumStatisticsService;

    @Autowired
    private ProductionManageClientOrderDataStatisticsService clientOrderDataStatisticsService;

    /**
     * 订单客户数
     * @param dateIntervalDTO
     * @return
     */
    public ClientDataCurveLineVO orderClientNum(DateIntervalDTO dateIntervalDTO) {
        List<LineChartVO> lineChartVOS = new ArrayList<>(1);
        String startQueryDate = dateIntervalDTO.getStartQueryDate();
        String endQueryDate = dateIntervalDTO.getEndQueryDate();
        List<String> dateInterval = LocalDateTimeUtil.getDateInterval(startQueryDate, endQueryDate);

        QueryWrapper<ProductionManageClient> clientQueryWrapper = new QueryWrapper<>();
        clientQueryWrapper.ge(StringUtils.isNotBlank(startQueryDate), "date_format(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d')", startQueryDate);
        clientQueryWrapper.le(StringUtils.isNotBlank(endQueryDate), "date_format(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d')", endQueryDate);
        clientQueryWrapper.isNotNull("o." + ProductionManageOrder.COL_CLIENT_ID);
        clientQueryWrapper.eq("c." + ProductionManageOrder.COL_ORGANIZATION_ID, commonUtil.getOrganizationId());
        clientQueryWrapper.eq("c." + ProductionManageOrder.COL_SYS_ID, commonUtil.getSysId());

        Integer num = clientMapper.countOrderClientNum(clientQueryWrapper);

        clientQueryWrapper.groupBy("date_format(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d')");
        List<Map<String, Object>> dataList = clientMapper.orderClientNum(clientQueryWrapper);
        LineChartVO transfer = LineChartDataTransfer.transfer(dateInterval, dataList, "orderDate", "clientNum", "订单客户数");
        lineChartVOS.add(transfer);

        ClientDataCurveLineVO curveLineVO = new ClientDataCurveLineVO();
        curveLineVO.setValues(lineChartVOS);
        curveLineVO.setClientTotalNum(num);
        return curveLineVO;
    }


    public ClientDataCurveLineVO potentialClientNum(DateIntervalDTO dateIntervalDTO) {
        List<LineChartVO> lineChartVOS = new ArrayList<>(1);
        String startQueryDate = dateIntervalDTO.getStartQueryDate();
        String endQueryDate = dateIntervalDTO.getEndQueryDate();
        List<String> dateInterval = LocalDateTimeUtil.getDateInterval(startQueryDate, endQueryDate);
        QueryWrapper<ProductionManageClient> clientQueryWrapper = commonUtil.queryTemplate(ProductionManageClient.class);
        clientQueryWrapper.eq(ProductionManageClient.COL_CLIENT_TYPE, 0);
        clientQueryWrapper.ge(StringUtils.isNotBlank(startQueryDate), "date_format(" + ProductionManageClient.COL_CREATE_DATE + ",'%Y-%m-%d')", startQueryDate);
        clientQueryWrapper.le(StringUtils.isNotBlank(endQueryDate), "date_format(" + ProductionManageClient.COL_CREATE_DATE + ",'%Y-%m-%d')", endQueryDate);
        clientQueryWrapper.groupBy("date_format(" + ProductionManageClient.COL_CREATE_DATE + ",'%Y-%m-%d')");
        List<Map<String, Object>> dataList = clientMapper.potentialClientNum(clientQueryWrapper);
        LineChartVO transfer = LineChartDataTransfer.transfer(dateInterval, dataList, "createDate", "clientNum", "潜在客户数");
        lineChartVOS.add(transfer);

        ClientDataCurveLineVO curveLineVO = new ClientDataCurveLineVO();
        curveLineVO.setValues(lineChartVOS);
        curveLineVO.setClientTotalNum(transfer.getTotalValue().intValue());
        return curveLineVO;
    }

    public IPage<ProductionManageSaleClientNumStatisticsVO> potentialClientList(DateIntervalListDTO dateIntervalDTO) {
        return  saleClientNumStatisticsService.pageList(dateIntervalDTO);
    }

    public IPage<ProductionManageClientOrderDataStatisticsListVO> orderClientList(DateIntervalListDTO dateIntervalDTO) {
        return clientOrderDataStatisticsService.pageList(dateIntervalDTO);
    }

    /**
     * 导出潜在客户
     * @param dateIntervalDTO
     * @param response
     * @throws Exception
     */
    public void exportPotentialClient(DateIntervalListDTO dateIntervalDTO, HttpServletResponse response) throws Exception {
        String dataList = dateIntervalDTO.getDataList();
        String exportMetadata = dateIntervalDTO.getExportMetadata();
        log.info("导出部分潜在客户订单数据："+dataList+",exportMetadata="+exportMetadata);
        saleClientNumStatisticsService.exportExcelList(dateIntervalDTO,commonUtil.getExportNumber(),"潜在客户转化率",ProductionManageSaleClientNumStatisticsVO.class,response);
    }

    /**
     * 导出订单客户
     * @param dateIntervalDTO
     * @param response
     * @throws Exception
     */
    public void exportOrderClient(DateIntervalListDTO dateIntervalDTO, HttpServletResponse response) throws Exception {
        String dataList = dateIntervalDTO.getDataList();
        String exportMetadata = dateIntervalDTO.getExportMetadata();
        log.info("导出部分潜在客户订单数据："+dataList+",exportMetadata="+exportMetadata);
        clientOrderDataStatisticsService.exportExcelList(dateIntervalDTO,commonUtil.getExportNumber(),"订单客户数",ProductionManageClientOrderDataStatisticsListVO.class,response);
    }
}
