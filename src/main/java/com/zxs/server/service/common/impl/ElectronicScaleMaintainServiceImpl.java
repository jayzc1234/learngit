package com.zxs.server.service.common.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.bo.common.ElectronicPushBO;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.common.ElectronicPushDTO;
import net.app315.hydra.intelligent.planting.dto.common.ElectronicScaleMaintainDTO;
import net.app315.hydra.intelligent.planting.pojo.common.ElectronicScaleMaintain;
import net.app315.hydra.intelligent.planting.server.electronicscale.listener.AppPusherListener;
import net.app315.hydra.intelligent.planting.server.mapper.common.ElectronicScaleMaintainMapper;
import net.app315.hydra.intelligent.planting.server.service.AbstractPageSearchService;
import net.app315.hydra.intelligent.planting.server.service.common.ElectronicScaleMaintainService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2021-02-02
 */
@Service
public class ElectronicScaleMaintainServiceImpl extends AbstractPageSearchService<ElectronicScaleMaintainMapper, ElectronicScaleMaintain> implements ElectronicScaleMaintainService {


    @Override
    public void update(Long id, ElectronicScaleMaintainDTO scaleMaintainDTO) {
        ElectronicScaleMaintain electronicScaleMaintain = baseMapper.selectById(id);
        CustomAssert.isNull(electronicScaleMaintain,"不存在该电子秤");
        ElectronicScaleMaintain electronicScaleMaintain1 = baseMapper.selectOne(
                Wrappers.<ElectronicScaleMaintain>lambdaQuery().ne(ElectronicScaleMaintain::getId,id).and(w->w.eq(ElectronicScaleMaintain::getSerialNum, scaleMaintainDTO.getSerialNum())
                        .or().eq(ElectronicScaleMaintain::getUniqueId, scaleMaintainDTO.getUniqueId()))
                );
        CustomAssert.notNull(electronicScaleMaintain1,"唯一id或编号已存在");
        electronicScaleMaintain.setUniqueId(scaleMaintainDTO.getUniqueId()).setSerialNum(scaleMaintainDTO.getSerialNum());
        baseMapper.updateById(electronicScaleMaintain);
    }

    @Override
    public ElectronicScaleMaintain add(ElectronicScaleMaintainDTO scaleMaintainDTO) {
        synchronized (this){
            ElectronicScaleMaintain electronicScaleMaintain = baseMapper.selectOne(
                    Wrappers.<ElectronicScaleMaintain>lambdaQuery()
                            .eq(!Objects.isNull(scaleMaintainDTO.getSerialNum()),ElectronicScaleMaintain::getSerialNum, scaleMaintainDTO.getSerialNum())
                            .eq(ElectronicScaleMaintain::getUniqueId, scaleMaintainDTO.getUniqueId())
                            .eq(ElectronicScaleMaintain::getOrganizationId, commonUtil.getOrganizationId()));
            CustomAssert.notNull(electronicScaleMaintain,"唯一id或编号已存在");

            electronicScaleMaintain = new ElectronicScaleMaintain();
            if (Objects.isNull(scaleMaintainDTO.getSerialNum())){
                ElectronicScaleMaintain electronicScaleMaintain2 = baseMapper.selectOne(
                        Wrappers.<ElectronicScaleMaintain>lambdaQuery()
                                .eq(ElectronicScaleMaintain::getOrganizationId, commonUtil.getOrganizationId())
                                .orderByDesc(ElectronicScaleMaintain::getId)
                                .last(" limit 1"));
                if (null == electronicScaleMaintain2){
                    scaleMaintainDTO.setSerialNum(1+"");
                }else {
                    scaleMaintainDTO.setSerialNum(String.valueOf(Long.parseLong(electronicScaleMaintain2.getSerialNum())+1));
                }
            }
            electronicScaleMaintain.setUniqueId(scaleMaintainDTO.getUniqueId()).setSerialNum(scaleMaintainDTO.getSerialNum());
            baseMapper.insert(electronicScaleMaintain);
            return electronicScaleMaintain;
        }
    }

    @Override
    public IPage<ElectronicScaleMaintain> pageList(DaoSearch daoSearch) {
        QueryWrapper<ElectronicScaleMaintain> electronicScaleMaintainQueryWrapper = buildWrapper(daoSearch, true);
        Page<ElectronicScaleMaintain> page = new Page<>(daoSearch.getDefaultCurrent(), daoSearch.getDefaultPageSize());
        IPage iPage = baseMapper.selectPage(page, electronicScaleMaintainQueryWrapper);
        return iPage;
    }

    @Override
    public void pushInfo(ElectronicPushDTO electronicPushDTO) {
        /**
         * 无需使用双检锁，就算被覆盖也是正常的业务覆盖，由于网络延时导致后发送的请求先到达情况忽略。
         */
        synchronized (electronicPushDTO.getSerialNum()){
            ElectronicPushBO electronicPushBO = AppPusherListener.PUSH_INFO_MAP.get(electronicPushDTO.getSerialNum());
            if (Objects.isNull(electronicPushBO)){
                electronicPushBO = new ElectronicPushBO();
                electronicPushBO.setPushId(electronicPushDTO.getPushId()).setCreateTime(System.currentTimeMillis());
            }else {
                electronicPushBO.setPushId(electronicPushDTO.getPushId()).setCreateTime(System.currentTimeMillis());
            }
            AppPusherListener.PUSH_INFO_MAP.put(electronicPushDTO.getSerialNum(),electronicPushBO);
        }
    }


}
