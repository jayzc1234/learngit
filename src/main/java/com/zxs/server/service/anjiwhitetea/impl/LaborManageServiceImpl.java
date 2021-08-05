package com.zxs.server.service.anjiwhitetea.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.LaborManageAddDTO;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.LaborManageDTO;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.LaborManageListDTO;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.LaborManageUpdateDTO;
import net.app315.hydra.intelligent.planting.enums.anjiwhitetea.SettlementTypeEnum;
import net.app315.hydra.intelligent.planting.enums.anjiwhitetea.TeaLevelEnum;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.BodyTemperature;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.LaborManage;
import net.app315.hydra.intelligent.planting.server.mapper.anjiwhitetea.LaborManageMapper;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.ExportExcelBaseService;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.LaborManageService;
import net.app315.hydra.intelligent.planting.vo.anjiwhitetea.LaborManageListVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 用工管理 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-04-14
 */
@Service
public class LaborManageServiceImpl extends ServiceImpl<LaborManageMapper, LaborManage> implements LaborManageService {

    @Autowired
    private ExportExcelBaseService excelBaseService;

    @Autowired
    private CommonUtil commonUtil;

    @Override
    public void add(LaborManageAddDTO laborManageAddDTO) {
        List<LaborManageDTO> laborManageDTOS = laborManageAddDTO.getLaborManageDTOS();
        CustomAssert.empty2Error(laborManageDTOS, "用工信息不能为空");
        Date pickingTeaTime = commonUtil.formatToDate(laborManageAddDTO.getPickingTeaTime(), "yyyy-MM-dd HH:mm");
        Integer settlementType = laborManageAddDTO.getSettlementType();
        Date date = new Date();
        List<LaborManage> laborManages = new ArrayList<>();
        for (LaborManageDTO laborManageDTO : laborManageDTOS) {
            LaborManage laborManage = new LaborManage();
            BeanUtils.copyProperties(laborManageDTO, laborManage);
            laborManage.setPickingTeaTime(pickingTeaTime);
            laborManage.setSettlementType(settlementType);
            laborManages.add(laborManage);
        }
        this.saveBatch(laborManages);
        System.out.println(11);
    }

    @Override
    public void update(LaborManageUpdateDTO updateDTO) {
        Long id = updateDTO.getId();
        LaborManage laborManage = baseMapper.selectById(id);
        CustomAssert.isNull(laborManage, "不存在当前记录");
        BeanUtils.copyProperties(updateDTO, laborManage);
        baseMapper.updateById(laborManage);
    }

    @Override
    public PageResults<List<LaborManageListVO>> pageList(LaborManageListDTO laborManageListDTO) {
        Integer current = Optional.ofNullable(laborManageListDTO.getCurrent()).orElse(1);
        Integer pageSize = Optional.ofNullable(laborManageListDTO.getPageSize()).orElse(10);
        Page<LaborManageListVO> page = new Page<>(current, pageSize);
        IPage<LaborManageListVO> iPage1 = baseMapper.pageList(page, getQueryWrapper(laborManageListDTO));
        List<LaborManageListVO> records = iPage1.getRecords();
        records.forEach(record -> {
            record.setSettlementTypeName(SettlementTypeEnum.getDesc(record.getSettlementType()));
            record.setTeaLevelName(TeaLevelEnum.getDesc(record.getTeaLevel()));
        });
        return CommonUtil.iPageToPageResults(iPage1, null);
    }

    private QueryWrapper<LaborManage> getQueryWrapper(LaborManageListDTO daoSearch) {
        QueryWrapper<LaborManage> queryWrapper = new QueryWrapper<>();
        if (null !=daoSearch.getFlag() && daoSearch.getFlag() == 0) {
            queryWrapper.eq(StringUtils.isNotEmpty(daoSearch.getSerialNumber()), LaborManage.COL_SERIAL_NUMBER, daoSearch.getSerialNumber());
            queryWrapper.eq(StringUtils.isNotEmpty(daoSearch.getPickingTeaUserName()), LaborManage.COL_PICKING_TEA_USER_NAME, daoSearch.getPickingTeaUserName());
            queryWrapper.eq(daoSearch.getTeaLevel() != null, LaborManage.COL_TEA_LEVEL, daoSearch.getTeaLevel());
            queryWrapper.eq(daoSearch.getSettlementType() != null, LaborManage.COL_SETTLEMENT_TYPE, daoSearch.getSettlementType());
            queryWrapper.eq(StringUtils.isNotEmpty(daoSearch.getOperatorName()), LaborManage.COL_OPERATOR_NAME, daoSearch.getOperatorName());
            if (StringUtils.isNotEmpty(daoSearch.getCreateTime()) && daoSearch.getCreateTime().length() > 1) {
                String[] startDates = daoSearch.getCreateTime().split("~");
                daoSearch.setStartTime(startDates[0].trim());
                daoSearch.setEndTime(startDates[1].trim());

            }
            queryWrapper.ge(StringUtils.isNotBlank(daoSearch.getStartTime()), LaborManage.COL_CREATE_TIME, daoSearch.getStartTime());
            queryWrapper.lt(StringUtils.isNotBlank(daoSearch.getEndTime()), LaborManage.COL_CREATE_TIME, daoSearch.getEndTime());

            if (StringUtils.isNotEmpty(daoSearch.getPickingTeaTime()) && daoSearch.getPickingTeaTime().length() > 1) {
                String[] pickingTeaDates = daoSearch.getPickingTeaTime().split("~");
                daoSearch.setPickingTeaStartTime(pickingTeaDates[0].trim());
                daoSearch.setPickingTeaEndTime(pickingTeaDates[1].trim());

            }
            queryWrapper.ge(StringUtils.isNotBlank(daoSearch.getPickingTeaStartTime()), LaborManage.COL_PICKING_TEA_TIME, daoSearch.getPickingTeaStartTime());
            queryWrapper.lt(StringUtils.isNotBlank(daoSearch.getPickingTeaEndTime()), LaborManage.COL_PICKING_TEA_TIME, daoSearch.getPickingTeaEndTime());
        } else if (StringUtils.isNotEmpty(daoSearch.getSearch())) {
            queryWrapper.and(wrapper -> wrapper
                    .or().like(LaborManage.COL_PICKING_TEA_USER_NAME, daoSearch.getSearch())
                    .or().like(LaborManage.COL_SERIAL_NUMBER, daoSearch.getSearch()));
        }
        queryWrapper.orderByDesc(BodyTemperature.COL_ID);
        return queryWrapper;
    }

    @Override
    public void exportExcelList(LaborManageListDTO listDTO, Integer maxExportNum, Class<LaborManageListVO> laborManageListVOClass, HttpServletResponse response) throws Exception {
        List<LaborManageListVO> list;
        // idList为空导出全部，不为空导出指定数据
        if (StringUtils.isNotBlank(listDTO.getDataList())) {
            list = JSONObject.parseArray(listDTO.getDataList(), LaborManageListVO.class);
        } else {
            listDTO.setCurrent(1);
            listDTO.setPageSize(commonUtil.getExportNumber());
            list = pageList(listDTO).getList();
        }

        try {
            ExcelUtils.listToExcel(list, listDTO.exportMetadataToMap(), "用工管理", response);
        } catch (Exception e) {
            throw new SuperCodeExtException(e.getMessage());
        }
    }

}
