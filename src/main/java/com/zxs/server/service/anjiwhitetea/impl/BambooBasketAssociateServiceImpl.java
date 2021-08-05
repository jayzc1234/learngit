package com.zxs.server.service.anjiwhitetea.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BambooBasketAssociateAddDTO;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BambooBasketAssociateDTO;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BambooBasketAssociateListDTO;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.BambooBasketAssociate;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.BambooBasketManage;
import net.app315.hydra.intelligent.planting.server.mapper.anjiwhitetea.BambooBasketAssociateMapper;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.BambooBasketAssociateService;
import net.app315.hydra.intelligent.planting.vo.anjiwhitetea.BambooBasketAssociateListVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-04-14
 */
@Service
public class BambooBasketAssociateServiceImpl extends ServiceImpl<BambooBasketAssociateMapper, BambooBasketAssociate> implements BambooBasketAssociateService {

    @Autowired
    private CommonUtil commonUtil;

    @Override
    @Transactional
    public void add(BambooBasketAssociateAddDTO basketAssociateAddDTO) {
        List<BambooBasketAssociateDTO> associateList = basketAssociateAddDTO.getAssociateList();
        CustomAssert.empty2Error(associateList, "关联数据不能为空");
        List<BambooBasketAssociate> basketAssociateList = new ArrayList<>();

        for (BambooBasketAssociateDTO bambooBasketAssociateDTO : associateList) {
            QueryWrapper<BambooBasketAssociate> queryWrapper = new QueryWrapper();
            queryWrapper.eq(BambooBasketAssociate.COL_SERIAL_NUMBER, bambooBasketAssociateDTO.getSerialNumber());
            if (baseMapper.selectCount(queryWrapper) > 0) {
                CustomAssert.throwException("竹筐" + bambooBasketAssociateDTO.getSerialNumber() + "已经关联采茶工");
            }

            BambooBasketAssociate bambooBasketAssociate = new BambooBasketAssociate();
            BeanUtils.copyProperties(bambooBasketAssociateDTO, bambooBasketAssociate);
            basketAssociateList.add(bambooBasketAssociate);
            baseMapper.insert(bambooBasketAssociate);
        }
    }

    @Override
    public void update(BambooBasketAssociateDTO associateDTO) {
        Long id = associateDTO.getId();
        BambooBasketAssociate bambooBasketAssociate = baseMapper.selectById(id);
        CustomAssert.isNull(bambooBasketAssociate, "不存在当前关联记录");
        BeanUtils.copyProperties(associateDTO, bambooBasketAssociate);

        QueryWrapper<BambooBasketAssociate> queryWrapper = new QueryWrapper();
        queryWrapper.eq(BambooBasketAssociate.COL_SERIAL_NUMBER, associateDTO.getSerialNumber());
        queryWrapper.ne(BambooBasketAssociate.COL_ID, id);
        if (baseMapper.selectCount(queryWrapper) > 0) {
            CustomAssert.throwException("竹筐" + associateDTO.getSerialNumber() + "已经关联采茶工");
        }

        baseMapper.updateById(bambooBasketAssociate);
    }

    @Override
    public AbstractPageService.PageResults<List<BambooBasketAssociateListVO>> pageList(BambooBasketAssociateListDTO associateListDTO) {
        Integer current = Optional.ofNullable(associateListDTO.getCurrent()).orElse(1);
        Integer pageSize = Optional.ofNullable(associateListDTO.getPageSize()).orElse(10);
        Page<BambooBasketAssociateListVO> page = new Page<>(current, pageSize);
        IPage<BambooBasketAssociateListVO> iPage1 = baseMapper.pageList(page, getQueryWrapper(associateListDTO));
        List<BambooBasketAssociateListVO> records = iPage1.getRecords();
        return CommonUtil.iPageToPageResults(iPage1, null);
    }

    private QueryWrapper<BambooBasketAssociate> getQueryWrapper(BambooBasketAssociateListDTO daoSearch) {
        QueryWrapper<BambooBasketAssociate> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(BambooBasketManage.COL_ID);
        if (null !=daoSearch.getFlag() && daoSearch.getFlag() == 0) {
            queryWrapper.eq(StringUtils.isNotEmpty(daoSearch.getPickingTeaUserName()), BambooBasketAssociate.COL_PICKING_TEA_USER_NAME, daoSearch.getPickingTeaUserName());
            queryWrapper.eq(StringUtils.isNotEmpty(daoSearch.getSerialNumber()), BambooBasketAssociate.COL_SERIAL_NUMBER, daoSearch.getSerialNumber());

            if (StringUtils.isNotEmpty(daoSearch.getCreateTime()) && daoSearch.getCreateTime().length() > 1) {
                String[] startDates = daoSearch.getCreateTime().split("~");
                daoSearch.setStartTime(startDates[0].trim());
                daoSearch.setEndTime(startDates[1].trim());
            }
            queryWrapper.ge(StringUtils.isNotBlank(daoSearch.getStartTime()), BambooBasketAssociate.COL_CREATE_TIME, daoSearch.getStartTime());
            queryWrapper.lt(StringUtils.isNotBlank(daoSearch.getEndTime()), BambooBasketAssociate.COL_CREATE_TIME, daoSearch.getEndTime());
        } else  if(StringUtils.isNotEmpty(daoSearch.getSearch()))  {
            queryWrapper.and(wrapper -> wrapper
                    .or().like(BambooBasketAssociate.COL_PICKING_TEA_USER_NAME, daoSearch.getSearch())
                    .or().like(BambooBasketAssociate.COL_SERIAL_NUMBER, daoSearch.getSearch()));
        }
        return queryWrapper;
    }

}
