package com.zxs.server.service.base.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.base.MaterialSortDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialSortListDTO;
import net.app315.hydra.intelligent.planting.enums.EnableOrDisEnum;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialSort;
import net.app315.hydra.intelligent.planting.server.mapper.base.MaterialSortMapper;
import net.app315.hydra.intelligent.planting.server.service.AbstractPageSearchService;
import net.app315.hydra.intelligent.planting.server.service.base.MaterialSortService;
import net.app315.hydra.intelligent.planting.server.util.BaseMapperUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2020-08-20
 */
@Service
public class MaterialSortServiceImpl extends AbstractPageSearchService<MaterialSortMapper, MaterialSort> implements MaterialSortService {

    @Autowired
    private CommonUtil commonUtil;

    @Override
    public void updateStatus(Integer id, Integer status) {
        MaterialSort materialSort = baseMapper.selectById(id);
        boolean nonNull = Objects.nonNull(materialSort);
        if (!nonNull){
            CommonUtil.throwSuperCodeExtException(500,"物料分类不存在");
        }
        EnableOrDisEnum value = EnableOrDisEnum.getValue(status);
        if (null == value){
            CommonUtil.throwSuperCodeExtException(500,"禁用启用状态非法，status："+status);
        }
        materialSort.setStatus(status);
        baseMapper.updateById(materialSort);
    }

    @Override
    public void add(MaterialSortDTO materialSortDTO) {
        MaterialSort materialSort = new MaterialSort();
        BeanUtils.copyProperties(materialSortDTO,materialSort);
        baseMapper.insert(materialSort);
    }

    @Override
    public void update(MaterialSortDTO materialSortDTO) {
        BaseMapperUtil.singleTableUniqueFieldCheck(materialSortDTO.getId(),MaterialSort.COL_SORT_NAME,materialSortDTO.getSortName(),commonUtil.getOrganizationId(),commonUtil.getSysId(),null,baseMapper);
        MaterialSort materialSort = new MaterialSort();
        BeanUtils.copyProperties(materialSortDTO,materialSort);
        baseMapper.updateById(materialSort);
    }

    @Override
    public IPage<MaterialSort> pageList(MaterialSortListDTO daoSearch) {
        QueryWrapper<MaterialSort> materialSortQueryWrapper = commonUtil.queryTemplate(MaterialSort.class);
        Integer integer = baseMapper.selectCount(materialSortQueryWrapper);
        if (Objects.isNull(integer) || integer == 0){
            materialSortQueryWrapper =  new QueryWrapper<>();
            materialSortQueryWrapper.isNull(MaterialSort.COL_ORGANIZATION_ID);
            materialSortQueryWrapper.isNull(MaterialSort.COL_SYS_ID);
            List<MaterialSort> materialSorts = baseMapper.selectList(materialSortQueryWrapper);
            if (CollectionUtils.isNotEmpty(materialSorts)){
                for (MaterialSort materialSort : materialSorts) {
                    materialSort.setId(null);
                }
                saveBatch(materialSorts);
            }
        }
        return listPage(daoSearch);
    }

    @Override
    protected <P extends DaoSearch> void additionalQuerySet(QueryWrapper<MaterialSort> queryWrapper, P daoSearch) {
        MaterialSortListDTO sortListDTO = (MaterialSortListDTO) daoSearch;
        queryWrapper.orderByAsc(MaterialSort.COL_SORT_WEIGHT,MaterialSort.COL_ID);
        queryWrapper.eq(!Objects.isNull(sortListDTO.getStatus()),MaterialSort.COL_STATUS,sortListDTO.getStatus());
    }
}
