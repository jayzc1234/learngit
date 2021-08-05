package com.zxs.server.service.gugeng.repairmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffSortDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuffSort;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageStuffSortMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffSortVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-10-08
 */
@Service
public class ProductionManageStuffSortService extends ServiceImpl<ProductionManageStuffSortMapper, ProductionManageStuffSort> {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    public void add(ProductionManageStuffSortDTO stuffSortDTO) throws SuperCodeException, ParseException {
        ProductionManageStuffSort entity = new ProductionManageStuffSort();
        entity.setCreateDate(CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
        entity.setSortName(stuffSortDTO.getSortName());
        QueryWrapper<ProductionManageStuffSort> queryWrapper=commonUtil.queryTemplate(ProductionManageStuffSort.class);
        queryWrapper.eq(ProductionManageStuffSort.COL_SORT_NAME, stuffSortDTO.getSortName());
        Integer count = baseMapper.selectCount(queryWrapper);
        if (null!=count && count>0){
            CommonUtil.throwSuperCodeExtException(500,"分类名称已存在");
        }
        Long previousSortId=stuffSortDTO.getPreviousSortId();
        //添加一级分类
        if (null==previousSortId){
            entity.setLevelNum(1);
        }else {
            ProductionManageStuffSort parent =baseMapper.selectById(previousSortId);
            if (null==parent){
                CommonUtil.throwSuperCodeExtException(500,"父分类不存在");
            }
            Integer levelNum=parent.getLevelNum();
            if (levelNum>=5){
                CommonUtil.throwSuperCodeExtException(500,"分类最多支持5层级");
            }
            entity.setPreviousSortId(previousSortId);
            entity.setLevelNum(++levelNum);
        }
        baseMapper.insert(entity);
    }

    public void update(ProductionManageStuffSortDTO stuffSortDTO) throws SuperCodeException {
        Long id = stuffSortDTO.getId();
        ProductionManageStuffSort existentity = baseMapper.selectById(id);
        if (null == existentity) {
            CommonUtil.throwSuperCodeExtException(500, "该分类不存在");
        }
        QueryWrapper<ProductionManageStuffSort> queryWrapper = commonUtil.queryTemplate(ProductionManageStuffSort.class);
        queryWrapper.eq(ProductionManageStuffSort.COL_SORT_NAME, stuffSortDTO.getSortName());
        queryWrapper.ne(ProductionManageStuffSort.COL_ID, id);
        Integer count = baseMapper.selectCount(queryWrapper);
        if (null != count && count > 0) {
            CommonUtil.throwSuperCodeExtException(500, "分类名称已存在");
        }
        existentity.setSortName(stuffSortDTO.getSortName());
        baseMapper.updateById(existentity);
    }

    public PageResults list(Object obj) throws SuperCodeException {
        Page<ProductionManageStuffSort> page = new Page<>(1, 5);
        QueryWrapper<ProductionManageStuffSort> queryWrapper = commonUtil.queryTemplate(ProductionManageStuffSort.class);
        // TODO 添加相应的业务逻辑
        return null;
    }

    public ProductionManageStuffSort getById(String id) throws SuperCodeException {
        QueryWrapper<ProductionManageStuffSort> queryWrapper = commonUtil.queryTemplate(ProductionManageStuffSort.class);
        // TODO 添加相应的业务逻辑
        return null;
    }

    public void deleteOne(Long id) {
        baseMapper.deleteById(id);
    }

    public List<ProductionManageStuffSortVO> first() throws SuperCodeException {
        QueryWrapper<ProductionManageStuffSortVO> queryWrapper=commonUtil.queryTemplate(ProductionManageStuffSortVO.class);
        queryWrapper.isNull(ProductionManageStuffSort.COL_PREVIOUS_SORT_ID);
        List<ProductionManageStuffSortVO> productionManageStuffSortVOS = baseMapper.selectVoList(queryWrapper);
        if (CollectionUtils.isNotEmpty(productionManageStuffSortVOS)){
            for (ProductionManageStuffSortVO productionManageStuffSortVO : productionManageStuffSortVOS) {
                QueryWrapper<ProductionManageStuffSort> queryWrapper2=commonUtil.queryTemplate(ProductionManageStuffSort.class);
                queryWrapper2.eq(ProductionManageStuffSort.COL_PREVIOUS_SORT_ID, productionManageStuffSortVO.getId());
                Integer integer = baseMapper.selectCount(queryWrapper2);
                if (integer>0){
                    productionManageStuffSortVO.setChild(true);
                }else {
                    productionManageStuffSortVO.setChild(false);
                }
            }
        }
        return productionManageStuffSortVOS;
    }

    public List<ProductionManageStuffSortVO> children(Long previousSortId) throws SuperCodeException {
        QueryWrapper<ProductionManageStuffSortVO> queryWrapper=commonUtil.queryTemplate(ProductionManageStuffSortVO.class);
        queryWrapper.eq(ProductionManageStuffSort.COL_PREVIOUS_SORT_ID, previousSortId);
        List<ProductionManageStuffSortVO> productionManageStuffSortVOS = baseMapper.selectVoList(queryWrapper);
        if (CollectionUtils.isNotEmpty(productionManageStuffSortVOS)){
            for (ProductionManageStuffSortVO productionManageStuffSortVO : productionManageStuffSortVOS) {
                QueryWrapper<ProductionManageStuffSort> queryWrapper2=commonUtil.queryTemplate(ProductionManageStuffSort.class);
                queryWrapper2.eq(ProductionManageStuffSort.COL_PREVIOUS_SORT_ID, productionManageStuffSortVO.getId());
                Integer integer = baseMapper.selectCount(queryWrapper2);
                if (integer>0){
                    productionManageStuffSortVO.setChild(true);
                }else {
                    productionManageStuffSortVO.setChild(false);
                }
            }
        }
        return productionManageStuffSortVOS;
    }
}
