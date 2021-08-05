package com.zxs.server.service.anjiwhitetea.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BodyTemperatureAddDTO;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BodyTemperatureDTO;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BodyTemperatureListDTO;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.BodyTemperature;
import net.app315.hydra.intelligent.planting.server.mapper.anjiwhitetea.BodyTemperatureMapper;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.BodyTemperatureService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-04-13
 */
@Service
public class BodyTemperatureServiceImpl extends ServiceImpl<BodyTemperatureMapper, BodyTemperature> implements BodyTemperatureService {

    @Autowired
    private CommonUtil commonUtil;

    public void add(BodyTemperatureAddDTO addDTO) {
        for (BodyTemperatureDTO temperatureDTO : addDTO.getList()) {
            BodyTemperature temperature = new BodyTemperature();
            BeanUtils.copyProperties(temperatureDTO, temperature);
            baseMapper.insert(temperature);
        }
    }

    public AbstractPageService.PageResults<List<BodyTemperature>> list(BodyTemperatureListDTO daoSearch) {
        long pageNum = daoSearch.getCurrent().longValue();
        long pageSize = daoSearch.getPageSize().longValue();
        IPage<BodyTemperature> page = baseMapper.selectPage(new Page<BodyTemperature>(pageNum, pageSize), getQueryWrapper(daoSearch));
        List<BodyTemperature> records = page.getRecords();

        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page(
                (int) pageSize, (int) pageNum, (int) page.getTotal());
        return new AbstractPageService.PageResults<List<BodyTemperature>>(records, pagination);
    }

    private <T> QueryWrapper<T> getQueryWrapper(BodyTemperatureListDTO daoSearch) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        if (null != daoSearch.getFlag() && daoSearch.getFlag() == 0) {
            queryWrapper.eq(daoSearch.getTeaPickerId() != null, BodyTemperature.COL_TEA_PICKER_ID, daoSearch.getTeaPickerId());
            queryWrapper.eq(StringUtils.isNotEmpty(daoSearch.getOperatorName()), BodyTemperature.COL_OPERATOR_NAME, daoSearch.getOperatorName());

            if (StringUtils.isNotEmpty(daoSearch.getCreateTime()) && daoSearch.getCreateTime().length() > 1) {
                String[] startDates = daoSearch.getCreateTime().split("~");
                daoSearch.setStartTime(startDates[0].trim());
                daoSearch.setEndTime(startDates[1].trim());

            }

            queryWrapper.ge(StringUtils.isNotBlank(daoSearch.getStartTime()), BodyTemperature.COL_CREATE_TIME, daoSearch.getStartTime());
            queryWrapper.lt(StringUtils.isNotBlank(daoSearch.getEndTime()), BodyTemperature.COL_CREATE_TIME, daoSearch.getEndTime());
        } else if(StringUtils.isNotEmpty(daoSearch.getSearch())) {
            queryWrapper.and(wrapper -> wrapper
                    .or().like(BodyTemperature.COL_TEA_PICKER_NAME, daoSearch.getSearch()));
        }
        queryWrapper.orderByDesc(BodyTemperature.COL_ID);
        return queryWrapper;
    }

    public void exportList(BodyTemperatureListDTO request, HttpServletResponse response) {
        List<BodyTemperature> list;
        // idList为空导出全部，不为空导出指定数据
        if (StringUtils.isNotBlank(request.getDataList())) {
            list = JSONObject.parseArray(request.getDataList(), BodyTemperature.class);
        } else {
            request.setCurrent(1);
            request.setPageSize(commonUtil.getExportNumber());
            list = list(request).getList();
        }

        try {
            ExcelUtils.listToExcel(list, request.exportMetadataToMap(), "列表", response);
        } catch (Exception e) {
            throw new SuperCodeExtException(e.getMessage());
        }
    }

}
