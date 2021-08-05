package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.NumberUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ProductionManageFunctionUseDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SaleUserLineChartDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageFunctionUse;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageFunctionUseMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.FunctionUseResponseVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-10-28
 */
@Service
public class ProductionManageFunctionUseService extends ServiceImpl<ProductionManageFunctionUseMapper, ProductionManageFunctionUse> {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private ProduceSummaryStatisticsService summaryStatisticsService;


    @Transactional
    public void add(ProductionManageFunctionUseDTO dto) throws SuperCodeException {
        ProductionManageFunctionUse entity = new ProductionManageFunctionUse();
        entity.setFunctionName(dto.getFunctionName());
        entity.setCreateUserId(commonUtil.getUserId());
        entity.setCreateDate(new Date());
        entity.setOrganizationId(commonUtil.getOrganizationId());
        entity.setSysId(commonUtil.getSysId());

        // TODO 添加相应的业务逻辑
        baseMapper.insert(entity);
    }

    @Transactional
    public void update(Object obj) throws SuperCodeException {
        ProductionManageFunctionUse entity = new ProductionManageFunctionUse();
        // TODO 添加相应的业务逻辑
        baseMapper.updateById(entity);
    }

    public PageResults list(Object obj) throws SuperCodeException {
        Page<ProductionManageFunctionUse> page = new Page<>(1, 5);
        QueryWrapper<ProductionManageFunctionUse> queryWrapper = commonUtil.queryTemplate(ProductionManageFunctionUse.class);
        // TODO 添加相应的业务逻辑
        return null;
    }

    public ProductionManageFunctionUse getById(String id) throws SuperCodeException {
        QueryWrapper<ProductionManageFunctionUse> queryWrapper = commonUtil.queryTemplate(ProductionManageFunctionUse.class);
        // TODO 添加相应的业务逻辑
        return null;
    }

    public PageResults<List<FunctionUseResponseVO>> page(SaleUserLineChartDTO dateIntervalDTO) throws Exception {
        List<FunctionUseResponseVO> voList= selectUseCount(dateIntervalDTO);
        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page((int) dateIntervalDTO.getPageSize(), (int) dateIntervalDTO.getCurrent(), (int) voList.size());
        PageResults<List<FunctionUseResponseVO>> pageResults = new PageResults<>();
        pageResults.setPagination(pagination);
        voList= voList.stream().skip(pagination.getStartNumber()).limit(pagination.getPageSize()).collect(Collectors.toList());

        pageResults.setList(voList);
        return pageResults;
    }

    public List<FunctionUseResponseVO> selectUseCount(SaleUserLineChartDTO dateIntervalDTO) throws Exception {
        QueryWrapper<ProductionManageFunctionUse> wrapper = commonUtil.queryTemplate(ProductionManageFunctionUse.class);

        wrapper.ge(StringUtils.isNotBlank(dateIntervalDTO.getStartQueryDate()), ProductionManageFunctionUse.COL_CREATE_DATE, dateIntervalDTO.getStartQueryDate())
                .le(StringUtils.isNotBlank(dateIntervalDTO.getEndQueryDate()), ProductionManageFunctionUse.COL_CREATE_DATE, getNextDay(dateIntervalDTO.getEndQueryDate()));

        List<FunctionUseResponseVO> list = baseMapper.selectUseCount(wrapper);
        double total = list.stream().mapToDouble(FunctionUseResponseVO::getUseCount).sum();

        List<FunctionUseResponseVO> listNew = new ArrayList<>();
        listNew.addAll(list.stream().limit(9).collect(Collectors.toList()));
        if (list.size() > 9) {
            Double elseCount = list.stream().skip(9).mapToDouble(FunctionUseResponseVO::getUseCount).sum();
            FunctionUseResponseVO responseVO = new FunctionUseResponseVO();
            responseVO.setFunctionName("其他");
            responseVO.setUseCount(elseCount.intValue());
            listNew.add(responseVO);
        }

        for (int i = 0; i < listNew.size(); i++) {
            FunctionUseResponseVO useResponseVO = listNew.get(i);
            useResponseVO.setRankNum(i + 1);
            String useCountRadio = NumberUtil.retainTwoDecimal(useResponseVO.getUseCount() * 100 / total);
            useResponseVO.setRadio(useCountRadio + "%");
        }

        return listNew;
    }

    public LineChartVO selectLineChartVO(SaleUserLineChartDTO dateIntervalDTO) throws Exception {
        LineChartVO chart0=summaryStatisticsService.createLineChartVO("");
        List<LineChartVO> values= new ArrayList<>();
        values.add(chart0);

        List<FunctionUseResponseVO> responseVOS= selectUseCount(dateIntervalDTO);
        for(FunctionUseResponseVO responseVO: responseVOS){
            chart0.getValues().add(summaryStatisticsService.createNameAndValueVO(chart0, responseVO.getFunctionName(),  responseVO.getUseCount()));
        }
        return chart0;
    }

    public String getNextDay(String dateTime) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse(dateTime);
        long endDateLong = date.getTime();
        date = new Date(endDateLong + 3600 * 24 * 1000);
        return sdf.format(date);
    }
}
