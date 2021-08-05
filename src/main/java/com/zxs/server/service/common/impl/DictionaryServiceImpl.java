package com.zxs.server.service.common.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.common.DictionaryDTO;
import net.app315.hydra.intelligent.planting.pojo.common.Dictionary;
import net.app315.hydra.intelligent.planting.server.mapper.common.DictionaryMapper;
import net.app315.hydra.intelligent.planting.server.service.common.DictionaryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2020-09-04
 */
@Service
public class DictionaryServiceImpl extends ServiceImpl<DictionaryMapper, Dictionary> implements DictionaryService {


    @Autowired
    private CommonUtil commonUtil;

    /**
     * 1.先根据组织id和类型查数据
     * 2.根据组织id查不到则查组织id为空的数据，查到则同步到这个企业
     * @return
     */
    @Override
    public List<Dictionary> listWeightUnit() {
        QueryWrapper<Dictionary> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(Dictionary.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
        queryWrapper.eq(Dictionary.COL_TYPE,1);
        //1.根据组织id查重量单位数据
        List<Dictionary> dictionaries = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(dictionaries)){
            QueryWrapper<Dictionary> queryWrapper2=new QueryWrapper<>();
            queryWrapper2.eq(Dictionary.COL_TYPE,1);
            queryWrapper2.and(q->q.isNull(Dictionary.COL_ORGANIZATION_ID).or().eq(Dictionary.COL_ORGANIZATION_ID,""));
            dictionaries = baseMapper.selectList(queryWrapper2);
            //2.根据组织id为空查到数据则同步到当前企业
            if (CollectionUtils.isNotEmpty(dictionaries)){
                for (Dictionary dictionary : dictionaries) {
                    dictionary.setId(null);
                    dictionary.setOrganizationId(commonUtil.getOrganizationId());
                }
                saveBatch(dictionaries);
            }
        }
        return dictionaries;
    }

    @Override
    public List<Dictionary> selectWeightUnit(String organizationId) {
        QueryWrapper<Dictionary> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(Dictionary.COL_ORGANIZATION_ID,organizationId);
        queryWrapper.eq(Dictionary.COL_TYPE,1);
        //1.根据组织id查重量单位数据
        List<Dictionary> dictionaries = baseMapper.selectList(queryWrapper);
        return dictionaries;
    }

    @Override
    public void updateStatus(DictionaryDTO dictionaryDTO) {
        Long id = dictionaryDTO.getId();
        Integer type = dictionaryDTO.getType();
        CustomAssert.isNull(type,"type不能为空");
        CustomAssert.isNull(id,"主键不能为空");

        UpdateWrapper<Dictionary> updateWrapper=new UpdateWrapper<>();
        updateWrapper.set(Dictionary.COL_ENABLE,0)
                .eq(Dictionary.COL_TYPE,type)
                .eq(Dictionary.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
        baseMapper.update(null,updateWrapper);


        Dictionary dictionary = new Dictionary();
        dictionary.setId(dictionaryDTO.getId());
        dictionary.setEnable(1);
        baseMapper.updateById(dictionary);
    }

    @Override
    public List<Dictionary> listYieldType(){
        QueryWrapper<Dictionary> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(Dictionary.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
        queryWrapper.eq(Dictionary.COL_TYPE,2);
        //1.根据组织id查重量单位数据
        List<Dictionary> dictionaries = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(dictionaries)){
            QueryWrapper<Dictionary> queryWrapper2=new QueryWrapper<>();
            queryWrapper2.eq(Dictionary.COL_TYPE,2);
            queryWrapper2.and(q->q.isNull(Dictionary.COL_ORGANIZATION_ID).or().eq(Dictionary.COL_ORGANIZATION_ID,""));
            dictionaries = baseMapper.selectList(queryWrapper2);
            //2.根据组织id为空查到数据则同步到当前企业
            if (CollectionUtils.isNotEmpty(dictionaries)){
                for (Dictionary dictionary : dictionaries) {
                    dictionary.setId(null);
                    dictionary.setOrganizationId(commonUtil.getOrganizationId());
                }
                saveBatch(dictionaries);
            }
        }
        return dictionaries;
    }

    @Override
    public void updateYieldType(List<String> ids) {
        UpdateWrapper<Dictionary> updateWrapper=new UpdateWrapper<>();
        updateWrapper.set(Dictionary.COL_ENABLE,0)
                .eq(Dictionary.COL_TYPE,2)
                .eq(Dictionary.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
        baseMapper.update(null,updateWrapper);


        updateWrapper=new UpdateWrapper<>();
        updateWrapper.in(Dictionary.COL_ID, ids);
        Dictionary dictionary = new Dictionary();
        dictionary.setEnable(1);
        baseMapper.update(dictionary, updateWrapper);
    }

    public List<Dictionary> getPlantingRemind(){
        QueryWrapper<Dictionary> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(Dictionary.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
        queryWrapper.eq(Dictionary.COL_TYPE,3);
        //1.根据组织id查重量单位数据
        List<Dictionary> dictionaries = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(dictionaries)){
            QueryWrapper<Dictionary> queryWrapper2=new QueryWrapper<>();
            queryWrapper2.eq(Dictionary.COL_TYPE,3);
            queryWrapper2.and(q->q.isNull(Dictionary.COL_ORGANIZATION_ID).or().eq(Dictionary.COL_ORGANIZATION_ID,""));
            dictionaries = baseMapper.selectList(queryWrapper2);
            //2.根据组织id为空查到数据则同步到当前企业
            if (CollectionUtils.isNotEmpty(dictionaries)){
                for (Dictionary dictionary : dictionaries) {
                    dictionary.setId(null);
                    dictionary.setOrganizationId(commonUtil.getOrganizationId());
                }
                saveBatch(dictionaries);
            }
        }

        return dictionaries;
    }

}
