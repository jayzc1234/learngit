package com.zxs.server.service.gugeng.salemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.ProductManageConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.QueryMapperUtil;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClient;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClientCategory;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ClientCategoryMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageClientMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.CLIENT_CLASSIFY;

@Service
public class ClientCategoryService extends ServiceImpl<ClientCategoryMapper, ProductionManageClientCategory> {
	@Autowired
	private ClientCategoryMapper dao;

	@Autowired
	private ProductManageClientMapper clientDao;
	
	@Autowired
	private CommonUtil commonUtil;

	public void add(String categoryName, Integer sortWeight) throws SuperCodeException {
		
		Map<String, Object> columnMap= QueryMapperUtil.singleQueryColumnMap(ProductionManageClientCategory.COL_CATEGORY_NAME, categoryName, commonUtil.getOrganizationId(), commonUtil.getSysId());
		List<ProductionManageClientCategory> list=dao.selectByMap(columnMap);
		if (null!=list && !list.isEmpty()) {
			throw new SuperCodeException("该类目已存在", 500);
		}
		ProductionManageClientCategory category = new ProductionManageClientCategory();
		Employee employee = commonUtil.getEmployee();
		category.setCategoryName(categoryName);
		category.setCreateUserId(employee.getEmployeeId());
		category.setAuthDepartmentId(employee.getDepartmentId());
		category.setOrganizationId(commonUtil.getOrganizationId());
		category.setSortWeight(sortWeight);
		category.setSysId(commonUtil.getSysId());
		dao.insert(category);

	}

	public void update( Long id,String categoryName, Integer sortWeight) throws SuperCodeException {
		ProductionManageClientCategory category = dao.selectById(id);
		if (null == category) {
			throw new SuperCodeException("该类目不存在", 500);
		}
		String organizationId = commonUtil.getOrganizationId();
		String sysId = commonUtil.getSysId();
		ProductionManageClientCategory existcategory = dao.selectByNameInOtherSysId(categoryName, id, organizationId, sysId);
		if (null != existcategory) {
			throw new SuperCodeException("该类目名称已在当前组织系统中存在", 500);
		}
		category.setSortWeight(sortWeight);
		category.setCategoryName(categoryName);
		dao.updateById(category);
	}

	public void sort(Long id, int moveStep, int direction) throws SuperCodeException {
		ProductionManageClientCategory category = dao.selectById(id);
		if (null == category) {
			throw new SuperCodeException("该类目不存在", 500);
		}
		String organizationId = commonUtil.getOrganizationId();
		String sysId = commonUtil.getSysId();
		
		List<ProductionManageClientCategory> exchangeCategoryList = dao.selectMoveStep(category.getSortWeight(),moveStep,direction,organizationId,sysId);
		if (null!=exchangeCategoryList && !exchangeCategoryList.isEmpty()) {
			if (moveStep>0) {
				ProductionManageClientCategory echangecategory =exchangeCategoryList.get(exchangeCategoryList.size()-1);
				int sortWeight=category.getSortWeight();
				category.setSortWeight(echangecategory.getSortWeight());
				echangecategory.setSortWeight(sortWeight);
				dao.updateById(echangecategory);
				dao.updateById(category);
			}
		}
	}

	public List<ProductionManageClientCategory> selectAll() throws SuperCodeException {
		String organizationId = commonUtil.getOrganizationId();
		String sysId = commonUtil.getSysId();
		List<ProductionManageClientCategory> list=dao.selectAndSortByOrgIdAndSysId(organizationId,sysId);
		if (null==list) {
			list=new ArrayList<>();
		}
		return list;
	}

	public PageResults<List<ProductionManageClientCategory>> page(DaoSearch daoSearch) throws Exception {
		   Page<ProductionManageClientCategory> page = new Page<>(daoSearch.getDefaultCurrent(), daoSearch.getDefaultPageSize());
	       QueryWrapper<ProductionManageClientCategory> queryWrapper = new QueryWrapper();
	       queryWrapper.eq(ProductManageConstant.SYS_ORGANIZATIONID,  commonUtil.getOrganizationId());
	       queryWrapper.eq(ProductManageConstant.SYS_SYSID,  commonUtil.getSysId());
	       if (StringUtils.isNotBlank(daoSearch.getSearch())) {
	    	   queryWrapper.and(name ->name.like(ProductionManageClientCategory.COL_CATEGORY_NAME, daoSearch.getSearch()));
		   }
	       queryWrapper.orderByAsc(ProductionManageClientCategory.COL_SORT_WEIGHT);
		   queryWrapper.orderByDesc(ProductionManageClientCategory.COL_ID);
		   commonUtil.roleDataAuthFilter(CLIENT_CLASSIFY,queryWrapper,ProductionManageClientCategory.COL_CREATE_USER_ID,null);

	       IPage<ProductionManageClientCategory> ipage=dao.selectPage(page, queryWrapper);
	       PageResults<List<ProductionManageClientCategory>> pageResults=CommonUtil.iPageToPageResults(ipage,null);
		return pageResults;
	}

	public void delete(Long id) throws SuperCodeException {
		 Map<String, Object> columMap=QueryMapperUtil.singleQueryColumnMap("Id",id, commonUtil.getOrganizationId(), commonUtil.getSysId());
		 List<ProductionManageClientCategory> list=dao.selectByMap(columMap);
		 if (null==list || list.isEmpty()) {
			 throw new SuperCodeException("该类目不存在", 500);
		 }
		 QueryWrapper<ProductionManageClient> queryWrapper=QueryMapperUtil.singleAndWrapQuery(ProductionManageClient.COL_CATEGORY_ID,id, commonUtil.getOrganizationId(), commonUtil.getSysId());
		 Integer count=clientDao.selectCount(queryWrapper);
		 if (null!=count && count>0) {
			 throw new SuperCodeException("该类目正在使用无法删除", 500);
		 }
		 dao.deleteById(id);
	}


	public PageResults<List<ProductionManageClientCategory>> dropDownPage(DaoSearch daoSearch) {
		Page<ProductionManageClientCategory> page = new Page<>(daoSearch.getDefaultCurrent(), daoSearch.getDefaultPageSize());
		QueryWrapper<ProductionManageClientCategory> queryWrapper = new QueryWrapper();
		queryWrapper.eq(ProductManageConstant.SYS_ORGANIZATIONID,  commonUtil.getOrganizationId());
		queryWrapper.eq(ProductManageConstant.SYS_SYSID,  commonUtil.getSysId());
		if (StringUtils.isNotBlank(daoSearch.getSearch())) {
			queryWrapper.and(name ->name.like(ProductionManageClientCategory.COL_CATEGORY_NAME, daoSearch.getSearch()));
		}
		queryWrapper.orderByAsc(ProductionManageClientCategory.COL_SORT_WEIGHT);
		IPage<ProductionManageClientCategory> ipage=dao.selectPage(page, queryWrapper);
		PageResults<List<ProductionManageClientCategory>> pageResults=CommonUtil.iPageToPageResults(ipage,null);
		return pageResults;
	}
}
