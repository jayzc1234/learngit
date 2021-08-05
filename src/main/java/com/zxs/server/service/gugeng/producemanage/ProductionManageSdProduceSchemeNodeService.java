package com.zxs.server.service.gugeng.producemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.ProductionManageSdProduceSchemeNodeListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManageSdProduceSchemeNode;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductionManageSdProduceSchemeNodeMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.ProductionManageSdProduceSchemeNodeVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 * 小飞飞测试
 * @author shixiongfei
 * @since 2019-06-14
 */
@Service
public class ProductionManageSdProduceSchemeNodeService extends ServiceImpl<ProductionManageSdProduceSchemeNodeMapper, ProductionManageSdProduceSchemeNode> implements BaseService<ProductionManageSdProduceSchemeNodeVO> {

	@Autowired
	private ProductionManageSdProduceSchemeNodeMapper dao;

	public void deleteBySchemeId(Long sdProduceSchemeId) {
		Map<String, Object> columnMap = new HashMap<>();
		columnMap.put(ProductionManageSdProduceSchemeNode.COL_SD_PRODUCE_SCHEME_ID, sdProduceSchemeId);
		dao.deleteByMap(columnMap);
	}

	@Override
	public PageResults<List<ProductionManageSdProduceSchemeNodeVO>> page(DaoSearch e) throws SuperCodeException {
		ProductionManageSdProduceSchemeNodeListDTO pNodeListDTO=(ProductionManageSdProduceSchemeNodeListDTO) e;
		Page<Integer> page = new Page<>(pNodeListDTO.getDefaultCurrent(), pNodeListDTO.getDefaultPageSize());
		IPage<Integer> iPage=dao.list(page,pNodeListDTO);
		List<Integer> dayNums=iPage.getRecords();
		List<ProductionManageSdProduceSchemeNodeVO> nodeVOs=new ArrayList<>();
		
		if (null!=dayNums && !dayNums.isEmpty()) {
			Map<Integer, ProductionManageSdProduceSchemeNodeVO>dataMap=new LinkedHashMap<>();
			List<ProductionManageSdProduceSchemeNode> nodes=dao.selectByDayNumsAndSchemeId(dayNums,pNodeListDTO.getSdProduceSchemeId());
			for (ProductionManageSdProduceSchemeNode productionManageSdProduceSchemeNode : nodes) {
				int dayNumer=productionManageSdProduceSchemeNode.getDayNumber();
				ProductionManageSdProduceSchemeNodeVO sdProduceSchemeNodeVO=dataMap.get(dayNumer);
				if (null==sdProduceSchemeNodeVO) {
					sdProduceSchemeNodeVO=new ProductionManageSdProduceSchemeNodeVO();
					sdProduceSchemeNodeVO.setIndex(dayNumer);
					sdProduceSchemeNodeVO.setNum(1);
					List<ProductionManageSdProduceSchemeNode> nodeInfo=new ArrayList<ProductionManageSdProduceSchemeNode>();
					if (StringUtils.isNotBlank(productionManageSdProduceSchemeNode.getName())) {
						nodeInfo.add(productionManageSdProduceSchemeNode);
					}
					sdProduceSchemeNodeVO.setNodeInfo(nodeInfo);
					dataMap.put(dayNumer, sdProduceSchemeNodeVO);
				}else {
					if (StringUtils.isNotBlank(productionManageSdProduceSchemeNode.getName())) {
						sdProduceSchemeNodeVO.setNum(sdProduceSchemeNodeVO.getNum()+1);
						List<ProductionManageSdProduceSchemeNode> nodeInfo=sdProduceSchemeNodeVO.getNodeInfo();
						nodeInfo.add(productionManageSdProduceSchemeNode);
					}
				}
			}
			for(Integer key:dataMap.keySet()) {
				nodeVOs.add(dataMap.get(key));
			}
		}
		PageResults<List<ProductionManageSdProduceSchemeNodeVO>> pageResults = new PageResults();
        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        pageResults.setPagination(pagination);
        pageResults.setList(nodeVOs);
        return pageResults;
	}

	public List<ProductionManageSdProduceSchemeNode> selectBySchemeId(Long sdProduceSchemeId) {
		QueryWrapper<ProductionManageSdProduceSchemeNode> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(ProductionManageSdProduceSchemeNode.COL_SD_PRODUCE_SCHEME_ID, sdProduceSchemeId);
		return dao.selectList(queryWrapper);
	}

	
	public List<ProductionManageSdProduceSchemeNode> selectBySchemeIdAndDayNumber(Long sdProduceSchemeId,
			Integer dayNumber) {
		QueryWrapper<ProductionManageSdProduceSchemeNode> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(ProductionManageSdProduceSchemeNode.COL_SD_PRODUCE_SCHEME_ID, sdProduceSchemeId);
		queryWrapper.eq(ProductionManageSdProduceSchemeNode.COL_DAY_NUMBER, dayNumber);
		queryWrapper.orderByAsc(ProductionManageSdProduceSchemeNode.COL_ID);
		return dao.selectList(queryWrapper);
	}

	public List<Map<String, Object>> selectDayNumberCount(Long schemeId) {
		return dao.selectDayNumberCount(schemeId);
	}

	public void deleteByIds(List<Long> deleteSchemeNodeIds) {
      dao.deleteBatchIds(deleteSchemeNodeIds);		
	}

}
