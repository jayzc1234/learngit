package com.zxs.server.service.gugeng.hydra.operationsmanagement;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.hydra.operationsmanagement.GuGengCommonTypeDTO;
import net.app315.hydra.intelligent.planting.dto.hydra.operationsmanagement.GuGengCommonTypeListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.hydra.operationsmanagement.GuGengCommonType;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.hydra.operationsmanagement.GuGengCommonTypeMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-12-02
 */
@Service
public class GuGengCommonTypeServiceImpl extends ServiceImpl<GuGengCommonTypeMapper, GuGengCommonType> implements BaseService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Transactional(rollbackFor = Exception.class)
    public void add(GuGengCommonTypeDTO commonTypeDTO){
        duplicateNameCheck(commonTypeDTO);

        Integer type = commonTypeDTO.getType();
        Integer sequence = commonTypeDTO.getSequence();
        GuGengCommonType entity = new GuGengCommonType();
        BeanUtils.copyProperties(commonTypeDTO,entity);
        boolean needUpdate = needUpdateOtherSequence(sequence, type,null);
        //如果不需要更新其它数据则表示新增数据排序值大于最大值，则设置排序值为最大值加1
        if (!needUpdate){
            Integer maxSequence = getMaxSequence(type);
            if (null!=maxSequence){
                entity.setSequence(maxSequence+1);
            }
        }
        baseMapper.insert(entity);
    }

    /**
     * 批量更新比当前添加的排序小的数
     * @param sequence
     * @param type
     * @return
     */
    private boolean needUpdateOtherSequence(Integer sequence, Integer type,Long updateId) {
        QueryWrapper<GuGengCommonType> queryWrapper=commonUtil.queryTemplate(GuGengCommonType.class);
        queryWrapper.eq(GuGengCommonType.COL_TYPE,type);
        queryWrapper.ge(GuGengCommonType.COL_SEQUENCE,sequence);
        queryWrapper.ne(null!=updateId,GuGengCommonType.COL_ID,updateId);
        queryWrapper.orderByAsc(GuGengCommonType.COL_SEQUENCE);
        List<GuGengCommonType> guGengCommonTypes = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(guGengCommonTypes)){

            Integer originSequence=sequence;

            int size = guGengCommonTypes.size();
            //如果数据总量超过10000则分批更新
            if (size > 10000){
                List<GuGengCommonType> commonTypes =new ArrayList<>(1000);
                for (int i=0; i<size;i++) {
                    GuGengCommonType guGengCommonType = guGengCommonTypes.get(i);
                    if (guGengCommonType.getSequence().equals(originSequence) ){
                        ++originSequence;
                        guGengCommonType.setSequence(originSequence);
                    }else {
                        break;
                    }
                    commonTypes.add(guGengCommonType);
                    if (i%1000==0){
                        saveOrUpdateBatch(commonTypes);
                        commonTypes.clear();
                    }
                }
                //如果总数不是1000的整倍数那最后还有未更新的数据
                if (size % 1000 != 0){
                    saveOrUpdateBatch(commonTypes);
                }
            }else {
                List<GuGengCommonType> needUpdateList =new ArrayList<>(1000);
                for (GuGengCommonType guGengCommonType : guGengCommonTypes) {
                    Integer sequence1 = guGengCommonType.getSequence();
                    if (sequence1.equals(originSequence) ){
                        ++originSequence;
                        guGengCommonType.setSequence(originSequence);
                        needUpdateList.add(guGengCommonType);
                    }else {
                        break;
                    }
                }
                if (CollectionUtils.isNotEmpty(needUpdateList)){
                    saveOrUpdateBatch(needUpdateList);
                }
            }
            return true;
        }
        return false;
    }

    private Integer getMaxSequence(Integer type) {
        QueryWrapper<GuGengCommonType> queryWrapper=commonUtil.queryTemplate(GuGengCommonType.class);
        queryWrapper.eq(GuGengCommonType.COL_TYPE,type);
        return baseMapper.selectMaxSequence(queryWrapper);
    }

    public void update(GuGengCommonTypeDTO commonTypeDTO) throws SuperCodeException {
        Long id = commonTypeDTO.getId();
        if (null==id){
            CommonUtil.throwSuperCodeExtException(500,"主键不能为空");
        }
        GuGengCommonType guGengCommonType = baseMapper.selectById(id);
        if (null==guGengCommonType){
            CommonUtil.throwSuperCodeExtException(500,"记录不存在");
        }

        duplicateNameCheck(commonTypeDTO);

        if (!guGengCommonType.getSequence().equals(commonTypeDTO.getSequence())){
            boolean needUpdate = needUpdateOtherSequence(commonTypeDTO.getSequence(), commonTypeDTO.getType(),id);
            if (!needUpdate){
                Integer maxSequence = getMaxSequence(commonTypeDTO.getType());
                if (null!=maxSequence){
                    guGengCommonType.setSequence(maxSequence+1);
                }
            }
        }
        guGengCommonType.setSequence(commonTypeDTO.getSequence());
        guGengCommonType.setName(commonTypeDTO.getName());
        baseMapper.updateById(guGengCommonType);
    }

    private void duplicateNameCheck(GuGengCommonTypeDTO commonTypeDTO) {
        @NotNull String name = commonTypeDTO.getName();
        Integer type = commonTypeDTO.getType();
        QueryWrapper<GuGengCommonType> queryWrapper=commonUtil.queryTemplate(GuGengCommonType.class);
        queryWrapper.eq(GuGengCommonType.COL_TYPE,type);
        queryWrapper.eq(GuGengCommonType.COL_NAME,name);
        queryWrapper.ne(null!=commonTypeDTO.getId(),GuGengCommonType.COL_ID,commonTypeDTO.getId());
        GuGengCommonType gengCommonType = baseMapper.selectOne(queryWrapper);
        if (null!=gengCommonType){
            CommonUtil.throwSuperCodeExtException(500,"名称已存在");
        }
    }

    @Override
    public IPage<GuGengCommonType> pageList(DaoSearch e) {
        return getGuGengCommonTypeIpage(e);
    }

    public IPage<GuGengCommonType> dropDown(GuGengCommonTypeListDTO daoSearch) {
        return getGuGengCommonTypeIpage(daoSearch);
    }

    private IPage<GuGengCommonType> getGuGengCommonTypeIpage(DaoSearch daoSearch) {
        GuGengCommonTypeListDTO guGengCommonTypeListDTO = (GuGengCommonTypeListDTO) daoSearch;
        QueryWrapper<GuGengCommonType> queryWrapper = commonUtil.queryTemplate(GuGengCommonType.class);
        queryWrapper.eq(null != guGengCommonTypeListDTO.getType(), GuGengCommonType.COL_TYPE, guGengCommonTypeListDTO.getType());
        queryWrapper.like(StringUtils.isNotBlank(guGengCommonTypeListDTO.getSearch()), GuGengCommonType.COL_NAME, guGengCommonTypeListDTO.getSearch());
        queryWrapper.orderByAsc(GuGengCommonType.COL_SEQUENCE);

        // 通过类型来获取权限啊吗
        String authCode = guGengCommonTypeListDTO.getType() == 1 ? ROOM_TYPE : ENTERTAINMENT_SHOW;
        commonUtil.roleDataAuthFilter(authCode, queryWrapper, GuGengCommonType.COL_CREATE_USER_ID, StringUtils.EMPTY);
        Page<GuGengCommonType> page = new Page<>(daoSearch.getDefaultCurrent(), daoSearch.getDefaultPageSize());
        return baseMapper.selectPage(page, queryWrapper);
    }

    public void deleteById(Long id) {
        baseMapper.deleteById(id);
    }
}
