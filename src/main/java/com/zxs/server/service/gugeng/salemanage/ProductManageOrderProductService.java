package com.zxs.server.service.gugeng.salemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.ProductManageOrderProductListDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderStatusEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProduct;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductionManageOrderProductMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ClientSaleOrderStatisticsVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductionManageOrderProductListVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Service
public class ProductManageOrderProductService extends ServiceImpl<BaseMapper<ProductionManageOrderProduct>, ProductionManageOrderProduct> implements BaseService<ProductionManageOrderProductListVO> {
@Autowired
private ProductionManageOrderProductMapper dao;

	public List<ProductionManageOrderProduct> selectByMap(Map<String,Object>columnMap) {
		return dao.selectByMap(columnMap);
	}

    @Override
	public PageResults<List<ProductionManageOrderProductListVO>> page(DaoSearch daoSearch) throws SuperCodeException {
    	ProductManageOrderProductListDTO pOrderProductListDTO=(ProductManageOrderProductListDTO) daoSearch;
		String orderDate=pOrderProductListDTO.getOrderDate();
		if (StringUtils.isNotBlank(orderDate)){
			String[] lossInterval = LocalDateTimeUtil.substringDate(orderDate);
			pOrderProductListDTO.setStartOrderDate(lossInterval[0]);
			pOrderProductListDTO.setEndOrderDate(lossInterval[1]);
		}
		Integer pcRequest = pOrderProductListDTO.getPcRequest();
		if (null==pcRequest){
			pcRequest=1;
		}
		QueryWrapper<ProductionManageOrderProduct> productQueryWrapper=new QueryWrapper<>();
		productQueryWrapper.eq(null!=pOrderProductListDTO.getClientId(),"mo."+ ProductionManageOrder.COL_CLIENT_ID,pOrderProductListDTO.getClientId());
		String orderFart="mo."+ProductionManageOrder.COL_ORDER_DATE;
		//2表示小程序
		if (pcRequest==2){
			orderFart="DATE_FORMAT(mo."+ProductionManageOrder.COL_ORDER_DATE+",'%Y-%m-%d')";
		}
		productQueryWrapper.ge(StringUtils.isNotBlank(pOrderProductListDTO.getStartOrderDate()),orderFart,pOrderProductListDTO.getStartOrderDate());
		productQueryWrapper.le(StringUtils.isNotBlank(pOrderProductListDTO.getEndOrderDate()),orderFart,pOrderProductListDTO.getEndOrderDate());
		productQueryWrapper.eq(StringUtils.isNotBlank(pOrderProductListDTO.getProductId()),"mop."+ProductionManageOrderProduct.COL_PRODUCT_ID,pOrderProductListDTO.getProductId());

		Page<ProductionManageOrderProduct> page = new Page<>(pOrderProductListDTO.getDefaultCurrent(), pOrderProductListDTO.getDefaultPageSize());
		IPage<ProductionManageOrderProductListVO> iPage=dao.list(page, productQueryWrapper);
		ClientSaleOrderStatisticsVO cStatisticsVO = dao.statistics(pOrderProductListDTO.getClientId(),pOrderProductListDTO.getProductId());
		if (CollectionUtils.isEmpty(iPage.getRecords()) ) {
			cStatisticsVO=null;
		}
		return CommonUtil.iPageToPageResults(iPage,cStatisticsVO);
	}

	
	@Override
	public void dataTransfer(List<ProductionManageOrderProductListVO> list) throws SuperCodeException {
		if (null!=list && !list.isEmpty()) {
			for (ProductionManageOrderProductListVO productionManageOrderProductListVO : list) {
				String orderStatus=productionManageOrderProductListVO.getOrderStatus();
				try {
					productionManageOrderProductListVO.setOrderStatus(OrderStatusEnum.getDesc(orderStatus));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	@Override
	public List<ProductionManageOrderProductListVO> listExcelByIds(List<? extends Serializable> ids)
			throws SuperCodeException {
		
		return dao.listExcelByIds(ids);
	}

	public ClientSaleOrderStatisticsVO statistics(Long clientId, String productId) {
		return dao.statistics(clientId,productId);
	}

	public void deleteByIds(List<Long> deleteOrderProductIds) {
       dao.deleteBatchIds(deleteOrderProductIds);		
	}

	public void deleteByOrderId(Long orderId) {
	   dao.deleteByOrderId(orderId);
	}

    public List<ProductionManageOrderProduct> selectByOrderId(Long orderId) {
		return dao.selectByOrderId(orderId);
    }
}
