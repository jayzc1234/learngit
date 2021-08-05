package com.zxs.server.service.common.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.pojo.common.Dictionary;
import net.app315.hydra.intelligent.planting.pojo.common.SystemSetting;
import net.app315.hydra.intelligent.planting.server.mapper.common.SystemSettingMapper;
import net.app315.hydra.intelligent.planting.server.service.common.SystemSettingService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2020-09-29
 */
@Service
public class SystemSettingServiceImpl extends ServiceImpl<SystemSettingMapper, SystemSetting> implements SystemSettingService {

    @Autowired
    private CommonUtil commonUtil;

    @Override
    public SystemSetting get(){
        QueryWrapper<SystemSetting> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(Dictionary.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());

        //1.根据组织id查重量单位数据
        List<SystemSetting> dictionaries = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(dictionaries)){
            QueryWrapper<SystemSetting> queryWrapper2=new QueryWrapper<>();
            queryWrapper2.and(q->q.isNull(SystemSetting.COL_ORGANIZATION_ID).or().eq(SystemSetting.COL_ORGANIZATION_ID,""));
            dictionaries = baseMapper.selectList(queryWrapper2);
            //2.根据组织id为空查到数据则同步到当前企业
            if (CollectionUtils.isNotEmpty(dictionaries)){
                for (SystemSetting dictionary : dictionaries) {
                    dictionary.setId(null);
                    dictionary.setOrganizationId(commonUtil.getOrganizationId());
                }
                saveBatch(dictionaries);
            }
        }
        return dictionaries.get(0);
    }

    @Override
    public void update(SystemSetting setting) {
        baseMapper.updateById(setting);
    }

}
