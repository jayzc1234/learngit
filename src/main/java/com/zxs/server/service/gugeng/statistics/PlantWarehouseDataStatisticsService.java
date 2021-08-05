package com.zxs.server.service.gugeng.statistics;

import com.jgw.supercodeplatform.exception.SuperCodeException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.bo.gugeng.InboundMessageBO;
import net.app315.hydra.intelligent.planting.bo.gugeng.OutboundMessageBO;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.PlantWarehouseDataStatisticsMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.PlantWarehouseDataDetailResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchPlantBatchResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchPlantWarehouseDataStatisticsResponseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author shixiongfei
 * @date 2019-09-22
 * @since
 */
@Slf4j
@Service
public class PlantWarehouseDataStatisticsService {


    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private PlantWarehouseDataStatisticsMapper mapper;

    /**
     * 通过批次id获取种植仓储数据统计信息
     *
     * @param plantBatchId 种植批次id
     * @return 仓储数据响应vo
     * @author shixiongfei
     * @date 2019-09-22
     * @updateDate 2019-09-22
     * @updatedBy shixiongfei
     */
    public SearchPlantWarehouseDataStatisticsResponseVO list(String plantBatchId) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        SearchPlantWarehouseDataStatisticsResponseVO responseVO = new SearchPlantWarehouseDataStatisticsResponseVO();

        // 获取分拣入库相关信息
        List<InboundMessageBO> inboundMessageBOS = mapper.getSortingStorageMessage(plantBatchId, sysId, organizationId);
        CustomAssert.isNotEmpty(inboundMessageBOS, "分拣入库信息为空，请检查");
        InboundMessageBO inboundMessageBO = inboundMessageBOS.get(0);
        // 获取公有参数值
        BeanUtils.copyProperties(inboundMessageBO, responseVO);
        // 获取批次类型， 如果为种植批次则从溯源中获取采收时间集合
        if (inboundMessageBO.getPlantBatchType() == 1) {
            try {
                responseVO.setHarvestDate(commonUtil.getHarvestDatesByPlantBatchId(plantBatchId));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        List<PlantWarehouseDataDetailResponseVO> vos = inboundMessageBOS.stream().map(bo -> {
            PlantWarehouseDataDetailResponseVO vo = new PlantWarehouseDataDetailResponseVO();
            BeanUtils.copyProperties(bo, vo);
            String productLevelCode = bo.getProductLevelCode();
            String now = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            //  获取数值相关信息
            List<OutboundMessageBO> outboundMessages = mapper.getOutboundMessage(plantBatchId, productLevelCode, now, sysId, organizationId);
            // 1. 获取总入库数值信息
            OutboundMessageBO checkInboundMessage = outboundMessages.stream().filter(message -> message.getSorting() == 1).findFirst().get();
            vo.setInboundBoxNum(checkInboundMessage.getOutboundBoxNum());
            vo.setInboundQuantity(checkInboundMessage.getOutboundQuantity());
            vo.setInboundWeight(checkInboundMessage.getOutboundWeight());

            // 2. 获取总出库数值信息
            OutboundMessageBO outMessage = outboundMessages.stream().filter(message -> message.getSorting() == 3).findFirst().get();
            vo.setOutboundBoxNum(outMessage.getOutboundBoxNum());
            vo.setOutboundQuantity(outMessage.getOutboundQuantity());
            vo.setOutboundWeight(outMessage.getOutboundWeight());

            // 3. 获取今日出库数值信息（今日包装出库 + 今日盘点出库 + 今日库存报损 + 今日换箱 + 箱子报损 + 空余箱子处理）
            List<OutboundMessageBO> todayOutMessages = outboundMessages.stream().filter(message -> message.getSorting() == 2 || message.getSorting() == 4
                    || message.getSorting() == 6 || message.getSorting() == 7 || message.getSorting() == 8
                    || message.getSorting() == 9).collect(Collectors.toList());
            vo.setTodayOutboundBoxNum(todayOutMessages.stream().mapToInt(OutboundMessageBO::getOutboundBoxNum).sum());
            vo.setTodayOutboundQuantity(todayOutMessages.stream().mapToInt(OutboundMessageBO::getOutboundQuantity).sum());
            vo.setTodayOutboundWeight(todayOutMessages.stream().map(OutboundMessageBO::getOutboundWeight).reduce(BigDecimal.ZERO, BigDecimal::add));

            // 4. 今日出库数值扣除退货入库的出库数值, 扣减是否会存在为负数的情况
            OutboundMessageBO refundOutboundMessage = outboundMessages.stream().filter(message -> message.getSorting() == 10).findFirst().get();
            vo.setTodayOutboundBoxNum(vo.getTodayOutboundBoxNum() - refundOutboundMessage.getOutboundBoxNum());
            vo.setTodayOutboundQuantity(vo.getTodayOutboundQuantity() - refundOutboundMessage.getOutboundQuantity());
            vo.setTodayOutboundWeight(vo.getTodayOutboundWeight().subtract(refundOutboundMessage.getOutboundWeight()));

            // 5. 获取实际库存数值信息
            OutboundMessageBO stock = outboundMessages.stream().filter(message -> message.getSorting() == 5).findFirst().get();
            vo.setStockBoxNum(stock.getOutboundBoxNum());
            vo.setStockQuantity(stock.getOutboundQuantity());
            vo.setStockWeight(stock.getOutboundWeight());
            return vo;
        }).collect(Collectors.toList());
        // 6. 获取非商品产品重量
        BigDecimal nonProductWeight = mapper.getNonProductWeight(plantBatchId, sysId, organizationId);
        responseVO.setNonCommercialWeight(nonProductWeight);

        responseVO.setPlantWarehouseDataDetails(vos);
        return responseVO;
    }

    /**
     * 获取种植批次列表
     *
     * @return
     * @author shixiongfei
     * @date 2019-09-22
     * @updateDate 2019-09-22
     * @updatedBy shixiongfei
     */
    public List<SearchPlantBatchResponseVO> listByPlantBatchId(String search) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        return mapper.listByPlantBatchId(search, sysId, organizationId);
    }

    /**
     * 获取默认的种植批次信息
     *
     * @return
     * @author shixiongfei
     * @date 2019-09-22
     * @updateDate 2019-09-22
     * @updatedBy shixiongfei
     */
    public SearchPlantBatchResponseVO getDefault() throws SuperCodeException {
        List<SearchPlantBatchResponseVO> list = listByPlantBatchId(null);
        CustomAssert.isNotEmpty(list, "暂无任何种植批次进行过分拣入库");
        return list.get(0);
    }

    /**
     * 导出种植批次数据统计
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-09-22
     * @updateDate 2019-09-22
     * @updatedBy shixiongfei
     */
    public void export(String plantBatchId, HttpServletResponse response) throws SuperCodeException {
        SearchPlantWarehouseDataStatisticsResponseVO responseVO = list(plantBatchId);

        Map<String, String> fieldMap = new HashMap<>(1);
        fieldMap.put("plantBatchId", "种植批次");
        ExcelUtils.listToExcel(Stream.of(responseVO).collect(Collectors.toList()), fieldMap, "种植批次数据统计", response, (sheet) -> {
            try {
                // 插入表头
                addHeaderLabel(sheet);
                // 添加数据
                addData(sheet, responseVO);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 添加表头相关信息
     *
     * @author shixiongfei
     * @date 2019-09-24
     * @updateDate 2019-09-24
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void addHeaderLabel(WritableSheet sheet) {
        // 插入表头
        try {
            sheet.addCell(new Label(0, 0, "种植批次"));
            sheet.addCell(new Label(1, 0, "区域名称"));
            sheet.addCell(new Label(2, 0, "采收时间"));
            sheet.addCell(new Label(3, 0, "分拣入库时间"));
            sheet.addCell(new Label(4, 0, "分拣类型"));
            sheet.addCell(new Label(5, 0, "产品等级"));
            sheet.addCell(new Label(6, 0, "产品规格"));
            sheet.addCell(new Label(7, 0, "分拣规格"));

            sheet.addCell(new Label(8, 0, "总入库"));
            sheet.addCell(new Label(8, 1, "入库重量（斤）"));
            sheet.addCell(new Label(9, 1, "入库箱数（箱）"));
            sheet.addCell(new Label(10, 1, "入库个数（个）"));

            sheet.addCell(new Label(11, 0, "总出库"));
            sheet.addCell(new Label(11, 1, "出库重量（斤）"));
            sheet.addCell(new Label(12, 1, "出库箱数（箱）"));
            sheet.addCell(new Label(13, 1, "出库个数（个）"));

            sheet.addCell(new Label(14, 0, "今日出库"));
            sheet.addCell(new Label(14, 1, "出库重量（斤）"));
            sheet.addCell(new Label(15, 1, "出库箱数（箱）"));
            sheet.addCell(new Label(16, 1, "出库个数（个）"));

            sheet.addCell(new Label(17, 0, "实际库存"));
            sheet.addCell(new Label(17, 1, "库存重量（斤）"));
            sheet.addCell(new Label(18, 1, "库存箱数（箱）"));
            sheet.addCell(new Label(19, 1, "库存个数（个）"));

            sheet.addCell(new Label(20, 0, "已入库天数"));
            sheet.addCell(new Label(21, 0, "非商品产品重量（斤）"));

            // 合并行
            sheet.mergeCells(0, 0, 0, 1);
            sheet.mergeCells(1, 0, 1, 1);
            sheet.mergeCells(2, 0, 2, 1);
            sheet.mergeCells(3, 0, 3, 1);
            sheet.mergeCells(4, 0, 4, 1);
            sheet.mergeCells(5, 0, 5, 1);
            sheet.mergeCells(6, 0, 6, 1);
            sheet.mergeCells(7, 0, 7, 1);

            // 合并列
            sheet.mergeCells(8, 0, 10, 0);
            sheet.mergeCells(11, 0, 13, 0);
            sheet.mergeCells(14, 0, 16, 0);
            sheet.mergeCells(17, 0, 19, 0);

            // 合并行
            sheet.mergeCells(20, 0, 20, 1);
            sheet.mergeCells(21, 0, 21, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加excel数据相关信息
     *
     * @author shixiongfei
     * @date 2019-09-24
     * @updateDate 2019-09-24
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void addData(WritableSheet sheet, SearchPlantWarehouseDataStatisticsResponseVO responseVO) throws WriteException {
        // 定义保留2位小数
        DecimalFormat df = new DecimalFormat("0.00");
        // 添加通用数据
        sheet.addCell(new Label(0, 2, responseVO.getPlantBatchName()));
        sheet.addCell(new Label(1, 2, responseVO.getGreenhouseName()));
        sheet.addCell(new Label(2, 2, responseVO.getHarvestDate()));
        sheet.addCell(new Label(3, 2, responseVO.getSortingInventoryDate()));
        sheet.addCell(new Label(21, 2, df.format(responseVO.getNonCommercialWeight().doubleValue())));
        // 添加特有属性
        List<PlantWarehouseDataDetailResponseVO> details = responseVO.getPlantWarehouseDataDetails();
        for (int i = 0; i < details.size(); i++) {
            int index = i + 2;
            PlantWarehouseDataDetailResponseVO vo = details.get(i);
            // 添加分拣类型, 产品等级，产品规格，分拣规格
            sheet.addCell(new Label(4, index, ""));
            sheet.addCell(new Label(5, index, vo.getProductLevelName()));
            sheet.addCell(new Label(6, index, vo.getProductSpecName()));
            sheet.addCell(new Label(7, index, vo.getSortingSpecName()));
            // 添加总入库
            sheet.addCell(new Label(8, index, df.format(vo.getInboundWeight())));
            sheet.addCell(new Label(9, index, vo.getInboundBoxNum().toString()));
            sheet.addCell(new Label(10, index, vo.getInboundQuantity().toString()));
            // 添加总出库
            sheet.addCell(new Label(11, index, df.format(vo.getOutboundWeight())));
            sheet.addCell(new Label(12, index, vo.getOutboundBoxNum().toString()));
            sheet.addCell(new Label(13, index, vo.getOutboundQuantity().toString()));
            // 添加今日出库
            sheet.addCell(new Label(14, index, df.format(vo.getTodayOutboundWeight())));
            sheet.addCell(new Label(15, index, vo.getTodayOutboundBoxNum().toString()));
            sheet.addCell(new Label(16, index, vo.getTodayOutboundQuantity().toString()));
            // 添加实际库存
            sheet.addCell(new Label(17, index, df.format(vo.getStockWeight())));
            sheet.addCell(new Label(18, index, vo.getStockBoxNum().toString()));
            sheet.addCell(new Label(19, index, vo.getStockQuantity().toString()));
            // 添加以入库天数
            sheet.addCell(new Label(20, index, vo.getInboundDays() >= 5 ? vo.getInboundDays() + "(预警)" : vo.getInboundDays().toString()));
        }
    }
}