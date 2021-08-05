package com.zxs.server.service.gugeng;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Component
public interface BaseService<E> {
	public default PageResults<List<E>> page(DaoSearch e) throws SuperCodeException {
		return null;
	}
	
	public  default PageResults<List<E>> list(DaoSearch e) throws SuperCodeException {
		return null;
	}

	public default IPage<E> pageList(DaoSearch e) throws SuperCodeException {
		return null;
	}

	public default List<?>  listExcelByIds(List<? extends Serializable> ids) throws SuperCodeException {
		return null;
	}

	public default List<E>  listExcelByIdsWithSpecificMethod(List<? extends Serializable> ids) throws SuperCodeException {
		return null;
	}

	public default void  dataTransfer(List<E> list) throws SuperCodeException {
	}

	/**
	 * 信息导出
	 * @param daoSearch
	 * @param response
	 */
	public  default void exportExcel(DaoSearch daoSearch, int exportNum, String text, HttpServletResponse response) throws Exception {
		ArrayList<String> idList = daoSearch.getIdList();
		List list;
		// idList为空导出全部，不为空导出指定数据
		if (CollectionUtils.isEmpty(idList)) {
			daoSearch.setCurrent(1);
			daoSearch.setPageSize(exportNum);
			PageResults<List<E>> pageResults=this.page(daoSearch);
			if (null==pageResults) {
				pageResults=this.list(daoSearch);
			}
			list = pageResults.getList();
		} else  {
			list = listExcelByIds(idList);
		}
		try {
			dataTransfer(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ExcelUtils.listToExcel(list, daoSearch.exportMetadataToMap(), text, response);
	}

	/**
	 *
	 * @param daoSearch
	 * @param exportNum
	 * @param text
	 * @param claz:部分导出json字符串序列化类型
	 * @param response
	 * @throws Exception
	 */
	public  default void exportExcelList(DaoSearch daoSearch, int exportNum, String text, Class claz, HttpServletResponse response) throws Exception {
		ArrayList<String> idList = daoSearch.getIdList();
		List list;
		// idList为空导出全部，不为空导出指定数据
       if (StringUtils.isNotBlank(daoSearch.getDataList())){
			 list = JSONObject.parseArray(daoSearch.getDataList(),claz);
		}else {
			daoSearch.setCurrent(1);
			daoSearch.setPageSize(exportNum);
			IPage eiPage = this.pageList(daoSearch);
			list=eiPage.getRecords();
		}
		try {
			dataTransfer(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ExcelUtils.listToExcel(list, daoSearch.exportMetadataToMap(), text, response);
	}

	/**
	 *
	 * @param daoSearch
	 * @param exportNum
	 * @param text
	 * @param claz:部分导出json字符串序列化类型
	 * @param response
	 * @throws Exception
	 */
	public  default void exportExcelReflect(DaoSearch daoSearch, int exportNum, String text, Class claz, String reflectMethodName, HttpServletResponse response) throws Exception {
		ArrayList<String> idList = daoSearch.getIdList();
		List list;
		if (StringUtils.isNotBlank(daoSearch.getDataList())){
			list = JSONObject.parseArray(daoSearch.getDataList(),claz);
		}else {
			daoSearch.setCurrent(1);
			daoSearch.setPageSize(exportNum);
			Class<? extends BaseService> aClass = this.getClass();
			Method method = aClass.getDeclaredMethod(reflectMethodName,daoSearch.getClass());
			IPage eiPage = (IPage) method.invoke(this, daoSearch);
			list=eiPage.getRecords();
		}
		try {
			dataTransfer(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ExcelUtils.listToExcel(list, daoSearch.exportMetadataToMap(), text, response);
	}

}
