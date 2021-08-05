package com.zxs.server.service.gugeng.producemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.QueryMapperUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.ProductionManageHarvestDamageListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManageHarvestDamage;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductionManageHarvestDamageMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 * 小飞飞测试
 * 
 * @author ZC
 * @since 2019-06-14
 */
@Service
public class ProductionManageHarvestDamageService
		extends ServiceImpl<ProductionManageHarvestDamageMapper, ProductionManageHarvestDamage> implements BaseService<ProductionManageHarvestDamage> {
	@Autowired
	private ProductionManageHarvestDamageMapper dao;

	@Autowired
	private CommonUtil commonUtil;

	public void add(ProductionManageHarvestDamage pManageHarvestDamage) throws SuperCodeException, ParseException {

		Employee employee = commonUtil.getEmployee();
		pManageHarvestDamage.setCreateUserId(employee.getEmployeeId());
		pManageHarvestDamage.setOrganizationId(commonUtil.getOrganizationId());
		pManageHarvestDamage.setSysId(commonUtil.getSysId());
		pManageHarvestDamage.setCreateDate(CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
		pManageHarvestDamage.setCreateUserName(employee.getName());
		pManageHarvestDamage.setAuthDepartmentId(employee.getDepartmentId());
		dao.insert(pManageHarvestDamage);
	}

	public void update(ProductionManageHarvestDamage pManageHarvestDamage) throws SuperCodeException {
        Long id = pManageHarvestDamage.getId();
        if (null == id) {
            throw new SuperCodeException("更新id不能为空", 500);
        }
        Map<String, Object> columnMap = QueryMapperUtil.singleQueryColumnMap(ProductionManageHarvestDamage.COL_ID, id, commonUtil.getOrganizationId(),
                commonUtil.getSysId());
        List<ProductionManageHarvestDamage> list = dao.selectByMap(columnMap);
        if (null == list) {
            throw new SuperCodeException("该报损不存在", 500);
        }
        dao.updateById(pManageHarvestDamage);
    }
	
	@Override
    public PageResults<List<ProductionManageHarvestDamage>> page(DaoSearch e) throws SuperCodeException {
        ProductionManageHarvestDamageListDTO pHarvestDamageListDTO = (ProductionManageHarvestDamageListDTO) e;
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        pHarvestDamageListDTO.setSysId(sysId);
        pHarvestDamageListDTO.setOrganizationId(organizationId);

        Page<ProductionManageHarvestDamage> page = new Page<>(pHarvestDamageListDTO.getDefaultCurrent(),
                pHarvestDamageListDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageHarvestDamage> queryWrapper = new QueryWrapper();
        // 查询指定的任务类型数据
        queryWrapper.eq(StringUtils.isNotBlank(organizationId), ProductionManageHarvestDamage.COL_ORGANIZATION_ID, organizationId)
                .eq(StringUtils.isNotBlank(sysId), ProductionManageHarvestDamage.COL_SYS_ID, sysId);
        // 判断搜索框是否为空,为空，则进行高级搜索，不为空，则普通搜索

        String search = pHarvestDamageListDTO.getSearch();
        if (StringUtils.isBlank(search)) {
            String[] dateInterval = LocalDateTimeUtil.substringDate(pHarvestDamageListDTO.getCreateDate());
            queryWrapper
                    .like(StringUtils.isNotBlank(pHarvestDamageListDTO.getCreateUserName()), ProductionManageHarvestDamage.COL_CREATE_USER_NAME,
                            pHarvestDamageListDTO.getCreateUserName())
                    .like(StringUtils.isNotBlank(pHarvestDamageListDTO.getGreenhouseName()), ProductionManageHarvestDamage.COL_GREENHOUSE_NAME,
                            pHarvestDamageListDTO.getGreenhouseName())
                    .eq(StringUtils.isNotBlank(pHarvestDamageListDTO.getHarvertBatchId()), ProductionManageHarvestDamage.COL_HARVEST_BATCH_ID,
                            pHarvestDamageListDTO.getHarvertBatchId())
                    .ge(StringUtils.isNotBlank(dateInterval[0]), ProductionManageHarvestDamage.COL_CREATE_DATE, dateInterval[0])
                    .le(StringUtils.isNotBlank(dateInterval[1]), ProductionManageHarvestDamage.COL_CREATE_DATE, dateInterval[1])
                    .like(StringUtils.isNotBlank(pHarvestDamageListDTO.getProductName()), ProductionManageHarvestDamage.COL_PRODUCT_NAME,
                            pHarvestDamageListDTO.getProductName());
        } else {
            queryWrapper.and(outorder -> outorder.like(ProductionManageHarvestDamage.COL_HARVEST_BATCH_NAME, search).or().like(ProductionManageHarvestDamage.COL_PRODUCT_NAME, search).or()
                    .like(ProductionManageHarvestDamage.COL_GREENHOUSE_NAME, search).or().like(ProductionManageHarvestDamage.COL_CREATE_USER_NAME, search));
        }
        commonUtil.roleDataAuthFilter("recoveryLoseList", queryWrapper, ProductionManageHarvestDamage.COL_CREATE_USER_ID, null);
        queryWrapper.orderByDesc(ProductionManageHarvestDamage.COL_CREATE_DATE);
        IPage<ProductionManageHarvestDamage> ipage = dao.selectPage(page, queryWrapper);
        return CommonUtil.iPageToPageResults(ipage, null);
    }

	/**
	 * 导出采收报损
	 * @param pHarvestDamageListDTO
	 * @param response
	 * @throws SuperCodeException
	 */
    public void exportDamageExcel(ProductionManageHarvestDamageListDTO pHarvestDamageListDTO, HttpServletResponse response) throws SuperCodeException {
		ArrayList<String> idList = pHarvestDamageListDTO.getIdList();
		// 如果ids为空则全部导出, 不为空，则选中导出
		List<ProductionManageHarvestDamage> list;
		if (CollectionUtils.isEmpty(idList)) {
			pHarvestDamageListDTO.setCurrent(1);
			pHarvestDamageListDTO.setPageSize(commonUtil.getExportNumber());
			PageResults<List<ProductionManageHarvestDamage>> pageResults = page(pHarvestDamageListDTO);
			list = pageResults.getList();
		} else {
            QueryWrapper<ProductionManageHarvestDamage> queryWrapper = commonUtil.queryTemplate(ProductionManageHarvestDamage.class);
            queryWrapper.and(query -> query.in(ProductionManageHarvestDamage.COL_ID, idList));
            list = dao.selectList(queryWrapper);
        }
		ExcelUtils.listToExcel(list, pHarvestDamageListDTO.exportMetadataToMap(), "采收报损", response);

	}
}
