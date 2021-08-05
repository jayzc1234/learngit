package com.zxs.server.service.gugeng;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.RankVO;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public interface StatisticsExcelBaseService<E> {
	public default void  dataTransfer(List<E> list) throws SuperCodeException {
	}
	public  default void exportExcelWithMethod(DaoSearch daoSearch, int exportNum, String text, String methodName, String selectIdsMethodName, BaseMapper baseMapper, HttpServletResponse response) throws Exception {
		ArrayList<String> idList = daoSearch.getIdList();
		List<E> list=null;
		if (CollectionUtils.isEmpty(idList)) {
			daoSearch.setCurrent(1);
			daoSearch.setPageSize(exportNum);

			try {
				Class claz=this.getClass();
				Method[] methods=claz.getDeclaredMethods();
				Method declaredMethod=null;
				for (Method method:methods) {
					boolean hs=method.getName().equals(methodName);
					if (hs){
						declaredMethod=method;
						break;
					}
				}
				PageResults<List<E>> pageResults= (PageResults<List<E>>) declaredMethod.invoke(this,daoSearch);
				list=pageResults.getList();
			} catch (Exception e) {
				CommonUtil.throwSupercodeException(500,"导出失败"+e.getLocalizedMessage());
			}
		}else {
			try {
				//默认排序导出
				list= (List<E>) baseListByIdsExcel(daoSearch,selectIdsMethodName,baseMapper);
			}catch (Exception e){
				//不排序导出
				list= (List<E>) baseListByNotNumIdsAndNotRankExcel(daoSearch,selectIdsMethodName,baseMapper);
			}
		}
		dataTransfer(list);
		ExcelUtils.listToExcel(list, daoSearch.exportMetadataToMap(), text, response);
	}

	public default <E extends RankVO> List<E> baseListByNotNumIdsAndNotRankExcel(DaoSearch daoSearch, String selectIdsMethodName, BaseMapper baseMapper) throws SuperCodeException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		List<String> ids=daoSearch.getIdList();
		if (null==ids || ids.isEmpty()){
			return null;
		}
		Class claz=baseMapper.getClass();
		Method[] methods=claz.getDeclaredMethods();
		Method declaredMethod=null;
		for (Method method:methods) {
			boolean hs=method.getName().equals(selectIdsMethodName);
			if (hs){
				declaredMethod=method;
			}
		}
		List<E> rankingVOList= (List<E>) declaredMethod.invoke(baseMapper,daoSearch);
		return rankingVOList;
	}
	public default <E extends RankVO> List<E> baseListByIdsExcel(DaoSearch daoSearch, String selectIdsMethodName, BaseMapper orderdao) throws SuperCodeException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		List<String> ids=daoSearch.getIdList();
		if (null==ids || ids.isEmpty()){
			return null;
		}
		Map<Integer,String> idsMap=new HashMap<>();
		for (String id:ids) {
			idsMap.put(Integer.valueOf(id),id);
		}
		int max=Integer.valueOf(ids.get(ids.size()-1));
		daoSearch.setPageSize(max);
		List<E> realPersonList=new ArrayList<>();
		Class claz=orderdao.getClass();
		Method[] methods=claz.getDeclaredMethods();
		Method declaredMethod=null;
		for (Method method:methods) {
			boolean hs=method.getName().equals(selectIdsMethodName);
			if (hs){
				declaredMethod=method;
			}
		}
		List<E> rankingVOList= (List<E>) declaredMethod.invoke(orderdao,daoSearch);
		if (null!=rankingVOList && !rankingVOList.isEmpty()){
	        int current=daoSearch.getCurrent();
	        int rankstart=(current-1)*daoSearch.getPageSize()+1;

			for (E personRankingVO:rankingVOList) {
				boolean rank=idsMap.containsKey(rankstart);
				if (rank) {
					personRankingVO.setRank(rankstart);
					realPersonList.add(personRankingVO);
				}
				rankstart++;
			}
		}
		return realPersonList;
	}
}
