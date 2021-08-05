package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchSDRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.statistics.ShippingDataEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsShippingData;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageStatisticsShippingDataMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchSDLCResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchSDResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-23
 */
@Service
public class ProductionManageStatisticsShippingDataService extends ServiceImpl<ProductionManageStatisticsShippingDataMapper, ProductionManageStatisticsShippingData> {

    @Autowired
    private CommonUtil commonUtil;


    /**
     * 获取发货数据统计列表
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     */
    public PageResults<List<SearchSDResponseVO>> list(SearchSDRequestDTO requestDTO) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        IPage<ProductionManageStatisticsShippingData> iPage = baseMapper.list(requestDTO, sysId, organizationId);

        Page pagination = new Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());

        List<ProductionManageStatisticsShippingData> records =
                Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());

        List<SearchSDResponseVO> list = records.stream().map(record -> {
            SearchSDResponseVO responseVO = new SearchSDResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            responseVO.setShippingDate(DateFormatUtils.format(record.getShippingDate(), LocalDateTimeUtil.DATE_PATTERN));
            return responseVO;
        }).collect(Collectors.toList());

        return new PageResults<>(list, pagination);
    }


    /**
     * 获取发货数据统计折线图相关信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     */
    public SearchSDLCResponseVO listLineChart(SearchSDRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        SearchSDLCResponseVO responseVO = new SearchSDLCResponseVO();
        // 获取时间区间
        List<String> dateInterval = LocalDateTimeUtil.getDateInterval(requestDTO.getStartQueryDate(), requestDTO.getEndQueryDate());
        // 获取总个数
        ProductionManageStatisticsShippingData totalNumber = baseMapper.getTotalNumber(requestDTO, sysId, organizationId);
        responseVO.setTotalDeliveryNumber(totalNumber.getDeliveryNumber());
        responseVO.setTotalExpressDeliveryNumber(totalNumber.getExpressDeliveryNumber());
        responseVO.setTotalSelfAcquiredNumber(totalNumber.getSelfAcquiredNumber());
        responseVO.setTotalWaitShipNumber(totalNumber.getWaitShipNumber());

        List<ProductionManageStatisticsShippingData> list = baseMapper.listLineChart(requestDTO, sysId, organizationId);

        return CollectionUtils.isEmpty(list) ? handleEmptyData(responseVO, dateInterval) : handleData(responseVO, dateInterval, list);
    }

    /**
     * 处理空数据集合
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     */
    private SearchSDLCResponseVO handleEmptyData(SearchSDLCResponseVO responseVO, List<String> dateInterval) {
        List<LineChartVO> lineChartVOS = Stream.of(ShippingDataEnum.values()).map(dataEnum -> {
            LineChartVO lineChartVO = new LineChartVO();
            // 设置折线图标题
            LineChartVO.Option option = lineChartVO.new Option();
            option.setName(dataEnum.getValue());
            lineChartVO.setOption(option);

            // 设置内容数据
            List<LineChartVO.NameAndValueVO> nameAndValueVOS = dateInterval.stream().map(date -> {
                LineChartVO.NameAndValueVO nameAndValueVO = lineChartVO.new NameAndValueVO();
                nameAndValueVO.setName(date);
                nameAndValueVO.setValue(0);
                return nameAndValueVO;
            }).collect(Collectors.toList());

            lineChartVO.setValues(nameAndValueVOS);
            return lineChartVO;
        }).collect(Collectors.toList());

       responseVO.setValues(lineChartVOS);

       return responseVO;
    }

    /**
     * 处理非空数据集合
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     */
    private SearchSDLCResponseVO handleData(SearchSDLCResponseVO responseVO, List<String> dateInterval, List<ProductionManageStatisticsShippingData> list) {
        Map<String, ProductionManageStatisticsShippingData> map = list.stream().collect(Collectors.toMap(data ->
                        DateFormatUtils.format(data.getShippingDate(), LocalDateTimeUtil.DATE_PATTERN),
                data -> data));

        List<LineChartVO> lineChartVOS = Stream.of(ShippingDataEnum.values()).map(dataEnum -> {
            LineChartVO lineChartVO = new LineChartVO();

            // 设置折线图标题
            LineChartVO.Option option = lineChartVO.new Option();
            option.setName(dataEnum.getValue());
            lineChartVO.setOption(option);

            // 设置折线图内容
            List<LineChartVO.NameAndValueVO> nameAndValueVOS = dateInterval.stream().map(date -> {
                LineChartVO.NameAndValueVO nameAndValueVO = lineChartVO.new NameAndValueVO();
                nameAndValueVO.setName(date);

                ProductionManageStatisticsShippingData shippingData = map.get(date);
                if (Objects.isNull(shippingData)) {
                    nameAndValueVO.setValue(0);
                    return nameAndValueVO;
                }
                int value;
                switch (dataEnum) {
                    case EXPRESS_DELIVERY:
                        value = shippingData.getExpressDeliveryNumber();
                        break;
                    case DELIVERY:
                        value = shippingData.getDeliveryNumber();
                        break;
                    case PICK_UP:
                        value = shippingData.getSelfAcquiredNumber();
                        break;
                    case TO_BE_DELIVERY:
                        value = shippingData.getWaitShipNumber();
                        break;
                    default:
                        value = 0;
                }
                nameAndValueVO.setValue(value);

                return nameAndValueVO;
            }).collect(Collectors.toList());

            lineChartVO.setValues(nameAndValueVOS);

            return lineChartVO;
        }).collect(Collectors.toList());

        responseVO.setValues(lineChartVOS);

        return responseVO;
    }


    /**
     * 发货数据统计导出
     *
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void export(SearchSDRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<SearchSDResponseVO> list = requestDTO.parseStr2List(SearchSDResponseVO.class);
        if (CollectionUtils.isEmpty(list)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = list(requestDTO).getList();
        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "发货数据统计列表", response);
    }
}