package com.zxs.server.service.anjiwhitetea;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 导出excel工具类
 * @author zc
 */
@Component
public class ExportExcelBaseService {

	/**
	 * 反射导出excel
	 * @param daoSearch
	 * @param exportNum
	 * @param text
	 * @param classVO:部分导出json字符串序列化类型
	 * @param response
	 * @throws Exception
	 */
	public   void exportExcelReflect(DaoSearch daoSearch, int exportNum, String text, Class classVO, IService service, String reflectMethodName, HttpServletResponse response) throws Exception {
		List list;
		if (StringUtils.isNotBlank(daoSearch.getDataList())){
			list = JSONObject.parseArray(daoSearch.getDataList(),classVO);
		}else {
			daoSearch.setCurrent(1);
			daoSearch.setPageSize(exportNum);
			Class<? extends ExportExcelBaseService> aClass = this.getClass();
			Method method = service.getClass().getDeclaredMethod(reflectMethodName, daoSearch.getClass());
			PageResults eiPage = (PageResults) method.invoke(service, daoSearch);
			list = (List) eiPage.getList();
		}

		ExcelUtils.listToExcel(list, daoSearch.exportMetadataToMap(), text, response);
	}

	private String toFirstCharLowerCase(String simpleName) {
		char[] chars = simpleName.toCharArray();
		chars[0] += 32;
		return new String(chars);
	}

	private void dataTransfer(List list) {

	}

}
