package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchGreenhouseStatisticsRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsGreenhouse;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageStatisticsGreenhouseMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.NameValueVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.StatisticsGreenhouseLineChartResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.StatisticsGreenhouseResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-20
 */
@Service
public class ProductionManageStatisticsGreenhouseService extends ServiceImpl<ProductionManageStatisticsGreenhouseMapper, ProductionManageStatisticsGreenhouse> {


    @Autowired
    private CommonUtil commonUtil;

    /**
     * 采收量
     */
    private static final String HARVEST_WEIGHT = "采收量";

    /**
     * 获取区域数据统计
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-20
     * @updateDate 2019-10-20
     * @updatedBy shixiongfei
     */
    public StatisticsGreenhouseLineChartResponseVO list(SearchGreenhouseStatisticsRequestDTO requestDTO) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        // 获取区域数据统计列表
        List<ProductionManageStatisticsGreenhouse> list = Optional
                .ofNullable(baseMapper.list(requestDTO, sysId, organizationId))
                .orElse(Collections.emptyList());

        // 获取采收总重量
        BigDecimal totalHarvestWeight = baseMapper.getTotalHarvestWeight(requestDTO, sysId, organizationId);

        StatisticsGreenhouseLineChartResponseVO responseVO = initResponseVO();
        responseVO.setTotalHarvestWeight(totalHarvestWeight);

        // 获取时间段
        List<String> dateInterval = LocalDateTimeUtil.getDateInterval(requestDTO.getStartQueryDate(), requestDTO.getEndQueryDate());
        initLineChart(responseVO, dateInterval.size());

        Map<String, BigDecimal> map = list.stream().collect(Collectors.toMap(
                harvest -> DateFormatUtils.format(harvest.getHarvestDate(), LocalDateTimeUtil.DATE_PATTERN),
                ProductionManageStatisticsGreenhouse::getHarvestWeight));

        // 填充值
        dateInterval.forEach(date -> {
            LineChartVO vo = responseVO.getValues().get(0);
            LineChartVO.NameAndValueVO nameAndValueVO = vo.new NameAndValueVO();
            List<LineChartVO.NameAndValueVO> nameAndValues = vo.getValues();
            nameAndValueVO.setName(date);
            nameAndValueVO.setValue(map.getOrDefault(date, BigDecimal.ZERO));
            nameAndValues.add(nameAndValueVO);
        });

        return responseVO;
    }

    /**
     * 初始化返回的响应vo
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-20
     * @updateDate 2019-10-20
     * @updatedBy shixiongfei
     */
    private StatisticsGreenhouseLineChartResponseVO initResponseVO() {
        StatisticsGreenhouseLineChartResponseVO responseVO = new StatisticsGreenhouseLineChartResponseVO();
        List<LineChartVO> values = new ArrayList<>(1);
        responseVO.setValues(values);
        return responseVO;
    }

    /**
     * 初始化折线图所需要的参数值
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-20
     * @updateDate 2019-10-20
     * @updatedBy shixiongfei
     */
    private LineChartVO initLineChart(StatisticsGreenhouseLineChartResponseVO responseVO, Integer dateLength) {
        // 初始化相关数据
        List<LineChartVO> values = responseVO.getValues();
        LineChartVO vo = new LineChartVO();
        List<LineChartVO.NameAndValueVO> list = new ArrayList<>(dateLength);
        LineChartVO.Option option = vo.new Option();
        option.setName(HARVEST_WEIGHT);
        vo.setOption(option);
        vo.setValues(list);
        values.add(vo);
        return vo;

    }

    /**
     * 获取区域数据统计列表
     *
     * @author shixiongfei
     * @date 2019-10-20
     * @updateDate 2019-10-20
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public PageResults<List<StatisticsGreenhouseResponseVO>> listGreenhouse(SearchGreenhouseStatisticsRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        IPage<ProductionManageStatisticsGreenhouse> iPage = baseMapper.listGreenhouse(requestDTO, sysId, organizationId);
        Page pagination = new Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());

        List<ProductionManageStatisticsGreenhouse> records = Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());
        List<StatisticsGreenhouseResponseVO> list = records.stream().map(record -> {
            StatisticsGreenhouseResponseVO responseVO = new StatisticsGreenhouseResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            responseVO.setHarvestDate(DateFormatUtils.format(record.getHarvestDate(), LocalDateTimeUtil.DATE_PATTERN));
            return responseVO;
        }).collect(Collectors.toList());

        return new PageResults<>(list, pagination);
    }

    /**
     * 获取总条数
     *
     * @author shixiongfei
     * @date 2019-10-21
     * @updateDate 2019-10-21
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Deprecated
    public Integer getTotalCount(String sysId, String organizationId) {
        return query().eq(StringUtils.isNotBlank(sysId), ProductionManageStatisticsGreenhouse.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageStatisticsGreenhouse.COL_ORGANIZATION_ID, organizationId)
                .count();
    }

    /**
     * 区域数据统计导出
     *
     * @author shixiongfei
     * @date 2019-10-21
     * @updateDate 2019-10-21
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void export(SearchGreenhouseStatisticsRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<StatisticsGreenhouseResponseVO> responseVOS = requestDTO.parseStr2List(StatisticsGreenhouseResponseVO.class);
        if (CollectionUtils.isEmpty(responseVOS)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            responseVOS = listGreenhouse(requestDTO).getList();
        }
        ExcelUtils.listToExcel(responseVOS, requestDTO.exportMetadataToMap(), "区域数据统计列表", response);
    }

    public NameValueVO<String, String> getLastestGreenhouse() {
        QueryWrapper<ProductionManageStatisticsGreenhouse> wrapper = new QueryWrapper<>();
        wrapper.eq(ProductionManageStatisticsGreenhouse.COL_ORGANIZATION_ID, commonUtil.getOrganizationId())
                .eq(ProductionManageStatisticsGreenhouse.COL_SYS_ID, commonUtil.getSysId())
                .orderByDesc(ProductionManageStatisticsGreenhouse.COL_HARVEST_DATE);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProductionManageStatisticsGreenhouse> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(0, 1);
        IPage<ProductionManageStatisticsGreenhouse> iPage = baseMapper.selectPage(page, wrapper);
        List<ProductionManageStatisticsGreenhouse> list = iPage.getRecords();

        NameValueVO<String, String> data = new NameValueVO<String, String>();
        if (CollectionUtils.isNotEmpty(list)) {
            data.setName(list.get(0).getGreenhouseName());
            data.setValue(list.get(0).getGreenhouseId());
        }
        return data;
    }

    public NameValueVO<String,String> getDefaultProduct(HttpServletRequest request){
        NameValueVO<String,String> data=new NameValueVO<String,String>();
        String serverName= request.getServerName();
        if(serverName.contains("jgwcjm")){
            data.setName("美月西瓜");
            data.setValue("0b2d4ee67c984d838cea64bafb64179d");
        } else {
            data.setName("蓝莓");
            data.setValue("9fe7a81cd3ba4f0ca7f84b642d78a671");
        }
        return data;
    }

}