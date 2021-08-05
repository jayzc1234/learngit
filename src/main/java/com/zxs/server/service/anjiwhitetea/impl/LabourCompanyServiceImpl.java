package com.zxs.server.service.anjiwhitetea.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.LabourCompany;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.TeaPicker;
import net.app315.hydra.intelligent.planting.server.mapper.anjiwhitetea.LabourCompanyMapper;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.LabourCompanyService;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.TeaPickerService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class LabourCompanyServiceImpl extends ServiceImpl<LabourCompanyMapper, LabourCompany> implements LabourCompanyService {

    @Autowired
    private CommonUtil commonUtil;
    @Autowired
    private TeaPickerService teaPickerService;

    public AbstractPageService.PageResults<List<LabourCompany>> list(DaoSearch daoSearch) {
        long pageNum = daoSearch.getCurrent().longValue();
        long pageSize = daoSearch.getPageSize().longValue();
        QueryWrapper<LabourCompany> queryWrapper = commonUtil.queryTemplate(LabourCompany.class);
        queryWrapper.orderByAsc(LabourCompany.COL_SORT);
        queryWrapper.like(StringUtils.isNotEmpty(daoSearch.getSearch()), LabourCompany.COL_NAME, daoSearch.getSearch());
        IPage<LabourCompany> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        List<LabourCompany> records = page.getRecords();

        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page(
                (int) pageSize, (int) pageNum, (int) page.getTotal());
        return new AbstractPageService.PageResults<>(records, pagination);
    }

    @Override
    public void add(LabourCompany obj) {
        QueryWrapper<LabourCompany> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(LabourCompany.COL_NAME, obj.getName());
        List<LabourCompany> labourCompany = baseMapper.selectList(queryWrapper);
        CustomAssert.isEmpty(labourCompany, "已存在当前劳务公司名称");
        baseMapper.insert(obj);
    }

    @Override
    public void update(LabourCompany obj) {
        QueryWrapper<LabourCompany> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(LabourCompany.COL_NAME, obj.getName());
        queryWrapper.ne(LabourCompany.COL_ID, obj.getId());
        List<LabourCompany> labourCompany = baseMapper.selectList(queryWrapper);
        CustomAssert.isEmpty(labourCompany, "已存在当前劳务公司名称");

        TeaPicker teaPicker = new TeaPicker();
        teaPicker.setLabourCompanyName(obj.getName());
        QueryWrapper<TeaPicker> teaPickerQueryWrapper = new QueryWrapper<>();
        teaPickerQueryWrapper.eq(TeaPicker.COL_LABOUR_COMPANY_ID, obj.getId());
        teaPickerService.update(teaPicker, teaPickerQueryWrapper);

        baseMapper.updateById(obj);
    }

    @Override
    public void delete(Integer id) {
        QueryWrapper<TeaPicker> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(TeaPicker.COL_LABOUR_COMPANY_ID, id);
        List<TeaPicker> teaPickers = teaPickerService.list(queryWrapper);
        CustomAssert.isEmpty(teaPickers, "该劳务公司已经被采茶工关联，不能删除哦");
        baseMapper.deleteById(id);
    }
}
