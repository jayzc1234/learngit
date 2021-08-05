package com.zxs.server.service.gugeng.repairmanage;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ObjectConverter;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairmanage.ProductManageQualityControlInspectionDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairmanage.ProductManageQualityControlInspectionListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageQualityControlInspection;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageQualityControlInspectionMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairmanage.ProductManageQualityControlInspectionListVO;
import net.app315.hydra.user.data.auth.sdk.model.InterceptorUserRoleDataAuth;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.DataPermissionConstants.AUTH_DEPARTMENT_ID;


@Service
public class ProductManageQualityControlInspectionService  extends ServiceImpl<ProductionManageQualityControlInspectionMapper, ProductionManageQualityControlInspection> implements BaseService<ProductionManageQualityControlInspection> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ObjectConverter<ProductionManageQualityControlInspection, ProductManageQualityControlInspectionListVO> objectConverter;

    public void add(ProductManageQualityControlInspectionDTO inspectionManageDTO){
        ProductionManageQualityControlInspection entity=new ProductionManageQualityControlInspection();
        BeanUtils.copyProperties(inspectionManageDTO, entity);
        entity.setOrganizationId(commonUtil.getOrganizationId());
        entity.setSysId(commonUtil.getSysId());
        entity.setInspectionStatus(1);
        entity.setCreateUserId(commonUtil.getEmployee().getEmployeeId());
        entity.setCreateDate(new Date());
        baseMapper.insert(entity);
    }

    public void update(ProductManageQualityControlInspectionDTO inspectionManageDTO)
    {
        ProductionManageQualityControlInspection entity=new ProductionManageQualityControlInspection();
        BeanUtils.copyProperties(inspectionManageDTO, entity);
        baseMapper.updateById(entity);
    }

    @Override
    public AbstractPageService.PageResults list(DaoSearch daoSearch) throws SuperCodeException  {
        ProductManageQualityControlInspectionListDTO requestDto= (ProductManageQualityControlInspectionListDTO) daoSearch;
        Page<ProductionManageQualityControlInspection> page = new Page<>(requestDto.getDefaultCurrent(), requestDto.getDefaultPageSize());
        QueryWrapper<ProductionManageQualityControlInspection> queryWrapper = commonUtil.advanceSearchQueryWrapperSet(requestDto, "ProductionManageInspectionManageService.list", ProductionManageQualityControlInspection.class);

        String userIdField="CreateUserId";
        InterceptorUserRoleDataAuth roleDataAuth= commonUtil.getRoleFunAuthWithAuthCode("viewQualityControlInspection");
        commonUtil.setAuthFilter(queryWrapper, roleDataAuth, userIdField, AUTH_DEPARTMENT_ID, ProductionManageQualityControlInspection.class);

        queryWrapper.orderByDesc(ProductionManageQualityControlInspection.COL_ID);
        IPage<ProductionManageQualityControlInspection> iPage = baseMapper.selectPage(page, queryWrapper);

        List<ProductManageQualityControlInspectionListVO> listVOS= objectConverter.convert(iPage.getRecords(), ProductManageQualityControlInspectionListVO.class);
        return CommonUtil.iPageToPageResults(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), listVOS, null);
    }

    @Override
    public List<ProductionManageQualityControlInspection> listExcelByIds(List<? extends Serializable> ids) throws SuperCodeException {
        return null;
    }

    @Override
    public  void exportExcelList(DaoSearch daoSearch, int exportNum, String text, Class claz, HttpServletResponse response) throws Exception {
        ArrayList<String> idList = daoSearch.getIdList();
        List list;
        // idList为空导出全部，不为空导出指定数据
        if (StringUtils.isNotBlank(daoSearch.getDataList())){
            list = JSONObject.parseArray(daoSearch.getDataList(),claz);
        }else {
            daoSearch.setCurrent(1);
            daoSearch.setPageSize(exportNum);
            AbstractPageService.PageResults pageResults=list(daoSearch);
            list=(List<ProductManageQualityControlInspectionListVO>)pageResults.getList();
        }
        ExcelUtils.listToExcel(list, daoSearch.exportMetadataToMap(), text, response);
    }

}
