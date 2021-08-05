package com.zxs.server.service.fuding.impl.teagreen;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.utils.http.SuperCodeRequests;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.exception.gugeng.TeaException;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionStatisticsDO;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.teagreen.TeaGreenAcquisitionFarmerMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.teagreen.TeaGreenAcquisitionStatisticsMapper;
import net.app315.hydra.intelligent.planting.server.service.fuding.teagreen.ITeaGreenAcquisitionStatisticsService;
import net.app315.hydra.intelligent.planting.utils.fuding.TimeUtil;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.common.Histogram;
import net.app315.hydra.intelligent.planting.vo.fuding.common.LineChart;
import net.app315.hydra.intelligent.planting.vo.fuding.common.Piechart;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenAcquisitionHourDataVO;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenAcquisitionStatisticsVO;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenAndHairyTeaStatisticsQuery;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TimeRangeVO;
import net.app315.hydra.user.sdk.provide.context.UserContextHelper;
import net.app315.nail.common.utils.DateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 茶青收购统计 服务实现类
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@Service
@Slf4j
public class TeaGreenAcquisitionStatisticsServiceImpl extends ServiceImpl<TeaGreenAcquisitionStatisticsMapper, TeaGreenAcquisitionStatisticsDO> implements ITeaGreenAcquisitionStatisticsService {

    @Autowired
    private TeaGreenAcquisitionStatisticsMapper teaGreenAcquisitionStatisticsMapper;
    @Autowired
    private TeaGreenAcquisitionFarmerMapper teaGreenAcquisitionFarmerMapper;
    @Autowired
    private UserContextHelper userContextHelper;

    @Autowired
    protected SuperCodeRequests superCodeRequests;


    private static final int CURRENT_ONE_MONTH = 0;
    private static final int CURRENT_SIX_MONTH = 1;
    private static final int CURRENT_ONE_YEAR = 2;

    private TimeRangeVO getTimeRange(TeaGreenAndHairyTeaStatisticsQuery model) {
        String startTime = "";
        String endTime = "";
        if (model.getTimeScope() == null) {
            startTime = DateUtil.format(model.getStartDate(), DateUtil.monthDatePattern);
            endTime = DateUtil.format(model.getEndDate(), DateUtil.monthDatePattern);
        } else {
            endTime = LocalDate.now().format(DateTimeFormatter.ofPattern(DateUtil.monthDatePattern));
            //0-最近1个月 1-最近六个月 2-最近一年
            if (model.getTimeScope() == CURRENT_ONE_MONTH) {
                startTime = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern(DateUtil.monthDatePattern));
            } else if (model.getTimeScope() == CURRENT_SIX_MONTH) {
                startTime = LocalDate.now().minusMonths(6).format(DateTimeFormatter.ofPattern(DateUtil.monthDatePattern));
            } else if (model.getTimeScope() == CURRENT_ONE_YEAR) {
                startTime = LocalDate.now().minusYears(1).format(DateTimeFormatter.ofPattern(DateUtil.monthDatePattern));
            } else {
                throw new TeaException("请选择时间区间");
            }
        }
        return new TimeRangeVO(startTime, endTime);
    }

    @Override
    public TeaGreenAcquisitionHourDataVO statisticsQuery(TeaGreenAndHairyTeaStatisticsQuery model) {
        TimeRangeVO timeRange = getTimeRange(model);
        String startTime = timeRange.getStartTime();
        String endTime = timeRange.getEndTime();
        TeaGreenAcquisitionHourDataVO result = new TeaGreenAcquisitionHourDataVO();
        String orgId = userContextHelper.getOrganizationId();
        //统计品类数据
        List<TeaGreenAcquisitionStatisticsDO> categorys = teaGreenAcquisitionStatisticsMapper.categoryStstisByOrg(orgId, startTime, endTime);
        if (CollectionUtils.isNotEmpty(categorys)) {
            List<Piechart> piecharts = new ArrayList<>();
            List<TeaGreenAcquisitionStatisticsVO> teaGreenAcquisitionStatisticsVOS = CopyUtil.copyList(categorys, TeaGreenAcquisitionStatisticsVO::new, (src, target) -> {
                Piechart piechart = new Piechart();
                piechart.setName(src.getProductName());
                piechart.setValue(src.getQuantity());
                piecharts.add(piechart);
                //设置总金额
                result.setAmount(result.getAmount().add(src.getAmount()));
                //收购总数量
                result.setQuantity(result.getQuantity().add(src.getQuantity()));
                if (src.getQuantity().doubleValue() > 0) {
                    //设置单条激励的平均值
                    target.setAveragePrice(src.getAmount().divide(src.getQuantity(), 2, RoundingMode.FLOOR));
                    target.setAmount(src.getAmount());
                    target.setQuantity(src.getQuantity());
                }
            });
            if(result.getQuantity().doubleValue() > 0){
                result.setAveragePrice(result.getAmount().divide(result.getQuantity(), 2,RoundingMode.FLOOR));
            }
            //设置饼图数据
            result.setPiecharts(piecharts);
            // 设置每个品类是数据
            result.setStatisticsTeaCategorys(teaGreenAcquisitionStatisticsVOS);
            // 设置茶农数量
            result.setFarmers(teaGreenAcquisitionFarmerMapper.farmerCountByOrg(orgId,startTime,endTime));
        }

        // 柱状图数据
        List<TeaGreenAcquisitionStatisticsDO> cooperatives = teaGreenAcquisitionStatisticsMapper.cooperativeStstisByOrg(orgId, startTime, endTime);
        if(CollectionUtils.isNotEmpty(cooperatives)){
            Histogram histogram = new Histogram();
            // x轴
            List<String> xAxis = new ArrayList<>();
            List<Object> data = new ArrayList<>();
            for (TeaGreenAcquisitionStatisticsDO cooperative : cooperatives) {
                xAxis.add(cooperative.getCooperativeName());
                data.add(cooperative.getQuantity());
                //设置交易笔数
                result.setTradingVolume(result.getTradingVolume() + cooperative.getTradingVolume());
            }
            histogram.setData(data);
            histogram.setXAxis(xAxis);
            result.setHistogram(histogram);
        }
        List<TeaGreenAcquisitionStatisticsDO> dayStatics = teaGreenAcquisitionStatisticsMapper.dayStstisByOrg(orgId, startTime, endTime);
        List<String> days = TimeUtil.getDayRange(DateUtil.parse(startTime,DateUtil.monthDatePattern),DateUtil.parse(endTime,DateUtil.monthDatePattern)
                ,DateUtil.monthDatePattern);
        int daySize = days.size();
        //平均值数据
        List<Object> avgData = new ArrayList<>(daySize);
        //交易金额
        List<Object> amountData = new ArrayList<>(daySize);
        //交易量
        List<Object> quantityData = new ArrayList<>(daySize);
        //交易笔数
        List<Object> tradingVolumeData = new ArrayList<>(daySize);
        //x轴
        List<String> xAxis = new ArrayList<>(daySize);
        Map<String, TeaGreenAcquisitionStatisticsDO> doMap = dayStatics.stream().collect(Collectors.toMap(TeaGreenAcquisitionStatisticsDO::getStatisticsTime, Function.identity()));
        for (String day : days) {
            TeaGreenAcquisitionStatisticsDO dayStatic = doMap.get(day);
            xAxis.add(day);
            if (dayStatic != null) {
                amountData.add(dayStatic.getAmount());
                quantityData.add(dayStatic.getQuantity());
                tradingVolumeData.add(dayStatic.getTradingVolume());
                if (dayStatic.getQuantity().doubleValue() > 0) {
                    avgData.add(dayStatic.getAmount().divide(dayStatic.getQuantity(), 2, RoundingMode.FLOOR));
                } else {
                    avgData.add(0);
                }
            } else {
                amountData.add(0);
                quantityData.add(0);
                tradingVolumeData.add(0);
                avgData.add(0);
            }
        }
        result.getLineCharts().add(new LineChart("交易均价", avgData, xAxis));
        result.getLineCharts().add(new LineChart("交易金额", amountData, xAxis));
        result.getLineCharts().add(new LineChart("交易重量", quantityData, xAxis));
        result.getLineCharts().add(new LineChart("交易笔数", tradingVolumeData, xAxis));
        return result;
    }
}
