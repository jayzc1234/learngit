package com.zxs.server.service.gugeng.repairmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ObjectConverter;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairmanage.ProductionManageWorkshopInspectionContentDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairmanage.ProductionManageWorkshopInspectionDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairmanage.ProductionManageWorkshopInspectionListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageWorkshopInspection;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageWorkshopInspectionContent;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageWorkshopInspectionContentMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageWorkshopInspectionMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairmanage.ProductionManageWorkshopInspectionContentListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairmanage.ProductionManageWorkshopInspectionListVO;
import net.app315.hydra.user.data.auth.sdk.model.InterceptorUserRoleDataAuth;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.DataPermissionConstants.AUTH_DEPARTMENT_ID;


/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-11-12
 */
@Service
public class ProductionManageWorkshopInspectionService extends ServiceImpl<ProductionManageWorkshopInspectionMapper, ProductionManageWorkshopInspection> implements BaseService<ProductionManageWorkshopInspection> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageWorkshopInspectionContentMapper contentMapper;

    @Autowired
    private ObjectConverter<ProductionManageWorkshopInspection, ProductionManageWorkshopInspectionListVO> objectConverter;

    @Autowired
    private ObjectConverter<ProductionManageWorkshopInspectionContent, ProductionManageWorkshopInspectionContentListVO> contentConverter;

    @Transactional
    public void add(ProductionManageWorkshopInspectionDTO inspectionDTO) throws SuperCodeException {
        ProductionManageWorkshopInspection entity = new ProductionManageWorkshopInspection();
        BeanUtils.copyProperties(inspectionDTO, entity);
        entity.setOrganizationId(commonUtil.getOrganizationId());
        entity.setSysId(commonUtil.getSysId());
        entity.setCreateUserId(commonUtil.getEmployee().getEmployeeId());
        entity.setCreateDate(new Date());
        baseMapper.insert(entity);

        for(ProductionManageWorkshopInspectionContentDTO contentDTO: inspectionDTO.getContentDTOList()){
            ProductionManageWorkshopInspectionContent content=new ProductionManageWorkshopInspectionContent();
            BeanUtils.copyProperties(contentDTO, content);
            content.setWorkshopInspectionId(entity.getId());
            contentMapper.insert(content);
        }
    }

    @Transactional
    public void update(ProductionManageWorkshopInspectionDTO inspectionDTO) throws SuperCodeException {
        ProductionManageWorkshopInspection entity = new ProductionManageWorkshopInspection();
        BeanUtils.copyProperties(inspectionDTO, entity);
        baseMapper.updateById(entity);

        for(ProductionManageWorkshopInspectionContentDTO contentDTO: inspectionDTO.getContentDTOList()){
            ProductionManageWorkshopInspectionContent content=new ProductionManageWorkshopInspectionContent();
            BeanUtils.copyProperties(contentDTO, content);
            contentMapper.updateById(content);
        }
    }

    @Override
    public AbstractPageService.PageResults list(DaoSearch daoSearch) throws SuperCodeException  {
        ProductionManageWorkshopInspectionListDTO inspectionManageListDTO= (ProductionManageWorkshopInspectionListDTO) daoSearch;
        if(StringUtils.isNotEmpty(inspectionManageListDTO.getInspectionDate())){
            inspectionManageListDTO.setInspectionDate(inspectionManageListDTO.getInspectionDate().replace(" ~ ","~"));
        }
        Page<ProductionManageWorkshopInspection> page = new Page<>(inspectionManageListDTO.getDefaultCurrent(), inspectionManageListDTO.getPageSize());
        QueryWrapper<ProductionManageWorkshopInspection> queryWrapper = commonUtil.advanceSearchQueryWrapperSet(inspectionManageListDTO, "ProductionManageInspectionManageService.list", ProductionManageWorkshopInspection.class);
        queryWrapper.eq(StringUtils.isNotBlank(inspectionManageListDTO.getProductBatchId()), ProductionManageWorkshopInspection.COL_PRODUCT_BATCH_ID, inspectionManageListDTO.getProductBatchId());

        String userIdField="CreateUserId";
        InterceptorUserRoleDataAuth roleDataAuth= commonUtil.getRoleFunAuthWithAuthCode("workShopInspectionList");
        commonUtil.setAuthFilter(queryWrapper, roleDataAuth, userIdField, AUTH_DEPARTMENT_ID, ProductionManageWorkshopInspection.class);

        queryWrapper.orderByDesc(ProductionManageWorkshopInspection.COL_ID);
        IPage<ProductionManageWorkshopInspection> iPage = baseMapper.selectPage(page, queryWrapper);

        List<ProductionManageWorkshopInspectionContentListVO> contentVOSList=new ArrayList<>();
        List<ProductionManageWorkshopInspectionListVO> listVOS= objectConverter.convert(iPage.getRecords(), ProductionManageWorkshopInspectionListVO.class);
        if(CollectionUtils.isNotEmpty(listVOS)){
            for(ProductionManageWorkshopInspectionListVO inspectionListVO: listVOS){
                Long id= inspectionListVO.getId();
                QueryWrapper<ProductionManageWorkshopInspectionContent> contentQueryWrapper = new QueryWrapper<>();
                contentQueryWrapper.eq(ProductionManageWorkshopInspectionContent.COL_WORKSHOP_INSPECTION_ID, id);
                List<ProductionManageWorkshopInspectionContent> contentList= contentMapper.selectList(contentQueryWrapper);

                List<ProductionManageWorkshopInspectionContentListVO> contentlistVOS= contentConverter.convert(contentList, ProductionManageWorkshopInspectionContentListVO.class);
                if(daoSearch.getPageSize()==1){
                    for(ProductionManageWorkshopInspectionContentListVO contentListVO: contentlistVOS){
                        BeanUtils.copyProperties(inspectionListVO, contentListVO);
                    }
                } else {
                    BeanUtils.copyProperties(inspectionListVO, contentlistVOS.get(0));
                }
                contentVOSList.addAll(contentlistVOS);
            }
        }

        return CommonUtil.iPageToPageResults(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), contentVOSList, null);
    }

    public ProductionManageWorkshopInspectionListVO getById(Long id) throws SuperCodeException {
        ProductionManageWorkshopInspection inspection= baseMapper.selectById(id);
        ProductionManageWorkshopInspectionListVO listVO=new ProductionManageWorkshopInspectionListVO();
        BeanUtils.copyProperties(inspection, listVO);

        QueryWrapper<ProductionManageWorkshopInspectionContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProductionManageWorkshopInspectionContent.COL_WORKSHOP_INSPECTION_ID, id);
        List<ProductionManageWorkshopInspectionContent> contentList= contentMapper.selectList(queryWrapper);
        List<ProductionManageWorkshopInspectionContentListVO> listVOS= contentConverter.convert(contentList, ProductionManageWorkshopInspectionContentListVO.class);
        listVO.setContentDTOList(listVOS);

        return listVO;
    }

    @Transactional
    public void delete(Long id){
        baseMapper.deleteById(id);

        QueryWrapper<ProductionManageWorkshopInspectionContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProductionManageWorkshopInspectionContent.COL_WORKSHOP_INSPECTION_ID, id);
        contentMapper.delete(queryWrapper);
    }
}
