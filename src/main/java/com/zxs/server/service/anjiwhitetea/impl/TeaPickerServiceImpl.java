package com.zxs.server.service.anjiwhitetea.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.TeaPickerListDTO;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.*;
import net.app315.hydra.intelligent.planting.server.mapper.anjiwhitetea.TeaPickerMapper;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.BambooBasketAssociateService;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.BodyTemperatureService;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.LaborManageService;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.TeaPickerService;
import net.app315.hydra.user.data.auth.sdk.utils.AreaUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-04-13
 */
@Service
public class TeaPickerServiceImpl extends ServiceImpl<TeaPickerMapper, TeaPicker> implements TeaPickerService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private AreaUtil<PlantingArea> areaUtil;

    @Autowired
    private BambooBasketAssociateService bambooBasketAssociateService;
    @Autowired
    private BodyTemperatureService bodyTemperatureService;

    @Autowired
    private LaborManageService laborManageService;

    public AbstractPageService.PageResults<List<TeaPicker>> list(TeaPickerListDTO daoSearch) {
        long pageNum = daoSearch.getCurrent().longValue();
        long pageSize = daoSearch.getPageSize().longValue();
        IPage<TeaPicker> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), getQueryWrapper(daoSearch));
        List<TeaPicker> records = page.getRecords();

        records.forEach(e -> {
            e.updateCensusText();
            String genderText = e.getSex() == 1 ? "男" : "女";
            e.setGenderText(genderText);
        });

        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page(
                (int) pageSize, (int) pageNum, (int) page.getTotal());
        return new AbstractPageService.PageResults<>(records, pagination);
    }

    public <T> QueryWrapper<T> getQueryWrapper(TeaPickerListDTO daoSearch) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        if (daoSearch.getFlag()!=null && daoSearch.getFlag() == 0) {
            queryWrapper.eq(StringUtils.isNotEmpty(daoSearch.getName()), TeaPicker.COL_NAME, daoSearch.getName());
            queryWrapper.eq(StringUtils.isNotEmpty(daoSearch.getSex()), TeaPicker.COL_SEX, daoSearch.getSex());
            queryWrapper.eq(StringUtils.isNotEmpty(daoSearch.getTelephone()), TeaPicker.COL_TELEPHONE, daoSearch.getTelephone());
            queryWrapper.eq(daoSearch.getLabourCompanyId() != null, TeaPicker.COL_LABOUR_COMPANY_ID, daoSearch.getLabourCompanyId());
            queryWrapper.eq(StringUtils.isNotEmpty(daoSearch.getProvinceName()), TeaPicker.COL_PROVINCE_NAME, daoSearch.getProvinceName());
            queryWrapper.eq(StringUtils.isNotEmpty(daoSearch.getCityName()), TeaPicker.COL_CITY_NAME, daoSearch.getCityName());
            queryWrapper.eq(StringUtils.isNotEmpty(daoSearch.getCountyName()), TeaPicker.COL_COUNTY_NAME, daoSearch.getCountyName());
        } else if(StringUtils.isNotEmpty(daoSearch.getSearch())) {
            queryWrapper.and(wrapper -> wrapper
                    .or().like(TeaPicker.COL_NAME, daoSearch.getSearch())
                    .or().like(TeaPicker.COL_TELEPHONE, daoSearch.getSearch()));
        }
        queryWrapper.orderByDesc(TeaPicker.COL_ID);
        return queryWrapper;
    }

    public void exportList(TeaPickerListDTO request, HttpServletResponse response) {
        List<TeaPicker> list;
        // idList为空导出全部，不为空导出指定数据
        if (StringUtils.isNotBlank(request.getDataList())) {
            list = JSONObject.parseArray(request.getDataList(), TeaPicker.class);
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

    @Override
    public void add(TeaPicker obj) {
        QueryWrapper<TeaPicker> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(TeaPicker.COL_TELEPHONE, obj.getTelephone());
        List<TeaPicker> teaPickers = baseMapper.selectList(queryWrapper);
        CustomAssert.isEmpty(teaPickers, "已存在当前手机号");
        setAreaInfoByAreaCode(obj, obj.getCensus());
        baseMapper.insert(obj);
    }

    @Override
    public void update(TeaPicker obj) {
        QueryWrapper<TeaPicker> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(TeaPicker.COL_TELEPHONE, obj.getTelephone());
        queryWrapper.ne(TeaPicker.COL_ID, obj.getId());
        List<TeaPicker> teaPickers = baseMapper.selectList(queryWrapper);
        CustomAssert.isEmpty(teaPickers, "已存在当前手机号");
        setAreaInfoByAreaCode(obj, obj.getCensus());

        BodyTemperature bodyTemperature = new BodyTemperature();
        bodyTemperature.setTeaPickerName(obj.getName());
        QueryWrapper<BodyTemperature> bodyTemperatureQueryWrapper = new QueryWrapper<>();
        bodyTemperatureQueryWrapper.eq(BodyTemperature.COL_TEA_PICKER_ID, obj.getId());
        bodyTemperatureService.update(bodyTemperature, bodyTemperatureQueryWrapper);

        BambooBasketAssociate bambooBasketAssociate = new BambooBasketAssociate();
        bambooBasketAssociate.setPickingTeaUserName(obj.getName());
        QueryWrapper<BambooBasketAssociate> bambooBasketAssociateQueryWrapper = new QueryWrapper<>();
        bambooBasketAssociateQueryWrapper.eq(BambooBasketAssociate.COL_PICKING_TEA_USER_ID, obj.getId());
        bambooBasketAssociateService.update(bambooBasketAssociate, bambooBasketAssociateQueryWrapper);

        LaborManage laborManage = new LaborManage();
        laborManage.setPickingTeaUserName(obj.getName());
        QueryWrapper<LaborManage> laborManageQueryWrapper = new QueryWrapper<>();
        laborManageQueryWrapper.eq(LaborManage.COL_PICKING_TEA_USER_ID, obj.getId());
        laborManageService.update(laborManage, laborManageQueryWrapper);
        baseMapper.updateById(obj);
    }

    @Override
    public void delete(Integer id) {
        QueryWrapper<BambooBasketAssociate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BambooBasketAssociate.COL_PICKING_TEA_USER_ID, id);
        List<BambooBasketAssociate> list = bambooBasketAssociateService.list(queryWrapper);
        CustomAssert.isEmpty(list, "该采茶工已经被关联，不能删除哦");

        QueryWrapper<BodyTemperature> bodyTemperatureQueryWrapper = new QueryWrapper<>();
        bodyTemperatureQueryWrapper.eq(BodyTemperature.COL_TEA_PICKER_ID, id);
        List<BodyTemperature> bodyTemperatureList = bodyTemperatureService.list(bodyTemperatureQueryWrapper);
        CustomAssert.isEmpty(bodyTemperatureList, "该采茶工已经被关联，不能删除哦");

        baseMapper.deleteById(id);
    }

    public void setAreaInfoByAreaCode(TeaPicker t, String areaCode) {
        if (StringUtils.isNotBlank(areaCode)){
            Map<String, String> re = areaUtil.getAreaInfoByAreaCode(areaCode);
            t.setProvinceName(re.get("provinceName"));
            t.setCityName(re.get("cityName"));
            t.setCountyName(re.get("countyName"));
            t.setTownShipName(re.get("townShipName"));
        }

    }
}
