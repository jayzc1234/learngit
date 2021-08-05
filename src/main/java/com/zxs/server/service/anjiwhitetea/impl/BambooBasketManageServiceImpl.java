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
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BambooBasketListDTO;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.BambooBasketAssociate;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.BambooBasketManage;
import net.app315.hydra.intelligent.planting.server.mapper.anjiwhitetea.BambooBasketManageMapper;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.BambooBasketAssociateService;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.BambooBasketManageService;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.ExportExcelBaseService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 竹筐管理 服务实现类
 * </p>
 * @author shixiongfei
 * @since 2020-04-14
 */
@Service
public class BambooBasketManageServiceImpl extends ServiceImpl<BambooBasketManageMapper, BambooBasketManage> implements BambooBasketManageService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ExportExcelBaseService excelBaseService;

    @Autowired
    private BambooBasketAssociateService bambooBasketAssociateService;

    @Override
    public void add(String serialNumber) {
        QueryWrapper<BambooBasketManage> queryWrapper = commonUtil.queryTemplate(BambooBasketManage.class);
        queryWrapper.eq(BambooBasketManage.COL_SERIAL_NUMBER, serialNumber);
        BambooBasketManage bambooBasketManage = baseMapper.selectOne(queryWrapper);
        CustomAssert.notNull(bambooBasketManage, "已存在当前编号");
        bambooBasketManage = new BambooBasketManage();
        bambooBasketManage.setSerialNumber(serialNumber);
        //TODO 设置二维码地址
        baseMapper.insert(bambooBasketManage);
    }

    @Override
    public void update(Long id, String serialNumber) {
        QueryWrapper<BambooBasketManage> queryWrapper = commonUtil.queryTemplate(BambooBasketManage.class);
        queryWrapper.eq(BambooBasketManage.COL_SERIAL_NUMBER, serialNumber);
        queryWrapper.ne(BambooBasketManage.COL_ID, id);
        BambooBasketManage bambooBasketManage = baseMapper.selectOne(queryWrapper);
        CustomAssert.notNull(bambooBasketManage, "已存在当前编号");

        BambooBasketManage bambooBasketManage1 = baseMapper.selectById(id);
        bambooBasketManage1.setSerialNumber(serialNumber);

        BambooBasketAssociate bambooBasketAssociate = new BambooBasketAssociate();
        bambooBasketAssociate.setSerialNumber(serialNumber);
        QueryWrapper<BambooBasketAssociate> associateQueryWrapper = new QueryWrapper<>();
        associateQueryWrapper.eq(BambooBasketAssociate.COL_BASKET_ID, id);
        bambooBasketAssociateService.update(bambooBasketAssociate, associateQueryWrapper);

        baseMapper.updateById(bambooBasketManage1);
    }

    @Override
    public AbstractPageService.PageResults<List<BambooBasketManage>> pageList(BambooBasketListDTO basketListDTO) {
        String search = basketListDTO.getSearch();
        int current = Optional.ofNullable(basketListDTO.getCurrent()).orElse(1);
        int pageSize = Optional.ofNullable(basketListDTO.getPageSize()).orElse(10);
        Page<BambooBasketManage> page=new Page<>(current,pageSize);
        QueryWrapper<BambooBasketManage> queryWrapper=commonUtil.queryTemplate(BambooBasketManage.class);
        queryWrapper.orderByDesc(BambooBasketManage.COL_ID);
        queryWrapper.and(StringUtils.isNotBlank(search), q -> q.or().like(BambooBasketManage.COL_SERIAL_NUMBER, search));
        queryWrapper.eq(basketListDTO.getUseStatus() != null, BambooBasketManage.COL_USE_STATUS, basketListDTO.getUseStatus());
        IPage<BambooBasketManage> iPage1 = baseMapper.selectPage(page, queryWrapper);

        List<BambooBasketManage> records = iPage1.getRecords();
        records.stream().forEach(e->{
            String useStatusName= e.getUseStatus()==0?"禁用":"启用";
            e.setUseStatusName(useStatusName);
        });

        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page(
                pageSize,  current, (int) page.getTotal());
        return new AbstractPageService.PageResults<List<BambooBasketManage>>(records, pagination);
    }

    @Override
    public void exportExcelList(BambooBasketListDTO request, Integer maxExportNum, Class<BambooBasketManage> laborManageListVOClass, HttpServletResponse response) throws Exception
    {
        List<BambooBasketManage> list;
        // idList为空导出全部，不为空导出指定数据
        if (StringUtils.isNotBlank(request.getDataList())) {
            list = JSONObject.parseArray(request.getDataList(), BambooBasketManage.class);
        } else {
            request.setCurrent(1);
            request.setPageSize(commonUtil.getExportNumber());
            list = pageList(request).getList();
        }

        try {
            ExcelUtils.listToExcel(list, request.exportMetadataToMap(), "列表", response);
        } catch (Exception e) {
            throw new SuperCodeExtException(e.getMessage());
        }
    }

    @Override
    public void changeUseStatus(Long id, Integer useStatus) {
        BambooBasketManage bambooBasketManage1 = baseMapper.selectById(id);
        bambooBasketManage1.setUseStatus(useStatus);
        baseMapper.updateById(bambooBasketManage1);
    }


}
