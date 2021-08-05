package com.zxs.server.service.fuding.impl.teagreen;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.enums.EnableOrDisEnum;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenProductLevelDO;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.teagreen.TeaGreenProductLevelMapper;
import net.app315.hydra.intelligent.planting.server.service.fuding.teagreen.TeaGreenProductLevelService;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenProductLevelAddModel;
import net.app315.nail.common.utils.BeanCopyUtil;
import net.app315.nail.common.utils.UUIDUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author caihong
 * @since 2021-07-05
 */
@Service
public class TeaGreenProductLevelServiceImpl extends ServiceImpl<TeaGreenProductLevelMapper, TeaGreenProductLevelDO> implements TeaGreenProductLevelService {

    @Autowired
    private CommonUtil commonUtil;

    @Override
    public void add(TeaGreenProductLevelAddModel obj) {
        TeaGreenProductLevelDO one = getOne(new QueryWrapper<TeaGreenProductLevelDO>().lambda().eq(TeaGreenProductLevelDO::getProductLevelName, obj.getProductLevelName()));
        CustomAssert.false2Error(one==null||!one.getProductLevelName().equals(obj.getProductLevelName()),
                "产品等级名称已存在");

        TeaGreenProductLevelDO teaGreenProductLevelDO = new TeaGreenProductLevelDO();
        BeanCopyUtil.copy(obj, teaGreenProductLevelDO);
        teaGreenProductLevelDO.setProductLevel(UUIDUtil.getUUID());
        save(teaGreenProductLevelDO);
    }

    @Override
    public void update(TeaGreenProductLevelAddModel obj, Integer id) {
        TeaGreenProductLevelDO one =
                getOne(new QueryWrapper<TeaGreenProductLevelDO>().lambda().ne(TeaGreenProductLevelDO::getId, id).eq(TeaGreenProductLevelDO::getProductLevelName, obj.getProductLevelName()));
        CustomAssert.false2Error(one==null||!one.getProductLevelName().equals(obj.getProductLevelName()),  "产品等级名称已存在");


        TeaGreenProductLevelDO teaGreenProductLevelDO = new TeaGreenProductLevelDO();
        BeanCopyUtil.copy(obj, teaGreenProductLevelDO);
        teaGreenProductLevelDO.setId(id.longValue());
        updateById(teaGreenProductLevelDO);
    }

    @Override
    public void delete(Integer id) {
        update(new UpdateWrapper<TeaGreenProductLevelDO>()
                .lambda()
                .set(TeaGreenProductLevelDO::getDeleted, EnableOrDisEnum.DIS_ENABLE.getStatus())
                .eq(TeaGreenProductLevelDO::getId, id));
    }

    @Override
    public IPage<TeaGreenProductLevelDO> listPage(DaoSearch daoSearch) {
        QueryWrapper<TeaGreenProductLevelDO> queryWrapper = commonUtil.queryTemplate(TeaGreenProductLevelDO.class);
        queryWrapper.eq(TeaGreenProductLevelDO.COL_DELETED,
                EnableOrDisEnum.ENABLE.getStatus());
        List<TeaGreenProductLevelDO> productLevelDOList = list(queryWrapper);
        if (CollectionUtils.isEmpty(productLevelDOList)) {
            productLevelDOList = list(new QueryWrapper<TeaGreenProductLevelDO>().lambda().eq(TeaGreenProductLevelDO::getSysId, StringUtils.EMPTY).eq(TeaGreenProductLevelDO::getOrganizationId, StringUtils.EMPTY));
            for (TeaGreenProductLevelDO productLevelDO : productLevelDOList) {
                productLevelDO.setId(null);
                productLevelDO.setSysId(commonUtil.getSysId());
                productLevelDO.setOrganizationId(commonUtil.getOrganizationId());
            }
            saveBatch(productLevelDOList);
        }
        Page<TeaGreenProductLevelDO> page = new Page<>(daoSearch.getDefaultCurrent(), daoSearch.getDefaultPageSize());
        IPage<TeaGreenProductLevelDO> productLevelMaintainIPage = page(page, queryWrapper);
        return productLevelMaintainIPage;
    }

    @Override
    public IPage<TeaGreenProductLevelDO> selectByOrgAndSysId(DaoSearch daoSearch) {
        QueryWrapper<TeaGreenProductLevelDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(TeaGreenProductLevelDO.COL_ORGANIZATION_ID, commonUtil.getOrganizationId());
        queryWrapper.eq(TeaGreenProductLevelDO.COL_SYS_ID, commonUtil.getSysId());
        queryWrapper.eq(TeaGreenProductLevelDO.COL_DELETED, EnableOrDisEnum.ENABLE.getStatus());
        queryWrapper.eq(StringUtils.isNotBlank(daoSearch.getSearch()), TeaGreenProductLevelDO.COL_PRODUCT_LEVEL_NAME, daoSearch.getSearch());
        return page(new Page<>(daoSearch.getDefaultCurrent(), daoSearch.getDefaultPageSize()), queryWrapper);
    }
}
