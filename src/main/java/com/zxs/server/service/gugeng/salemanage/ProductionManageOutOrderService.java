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
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.ProductionManageOutOrderDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.ProductionManageOutOrderListDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderTypeEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOutOrder;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductionManageOutOrderMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductionManageOutOrderListVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.OUTSIDE_ORDER;


@Service
public class ProductionManageOutOrderService extends ServiceImpl<ProductionManageOutOrderMapper, ProductionManageOutOrder> implements BaseService {
@Autowired
private ProductionManageOutOrderMapper dao;

@Autowired
private CommonUtil commonUtil;

@Autowired
private SerialNumberGenerator serialNumberGenerator;


private static String BATCH_PREFIX="外采-";


public void add(ProductionManageOutOrderDTO pOrderDTO) throws SuperCodeException, ParseException {
		String organizationId = commonUtil.getOrganizationId();
		String sysId = commonUtil.getSysId();
		ProductionManageOutOrder pOutOrder = new ProductionManageOutOrder();
		String outOrderBatchId = pOrderDTO.getOutOrderBatchId();
		QueryWrapper<ProductionManageOutOrder> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(ProductManageConstant.SYS_ORGANIZATIONID, organizationId);
		queryWrapper.eq(ProductManageConstant.SYS_SYSID, sysId);
		String productName=pOrderDTO.getProductName();
		if (StringUtils.isBlank(outOrderBatchId)) {
			outOrderBatchId = BATCH_PREFIX + productName + serialNumberGenerator.getSerialNumber(3, RedisKey.OUT_ORDER_NO_KEY, organizationId + sysId, CommonUtil.getSecondsNextEarlyMorning());
			queryWrapper.eq(ProductionManageOutOrder.COL_OUT_ORDER_BATCH_ID, outOrderBatchId);
			Integer count = dao.selectCount(queryWrapper);
			while (null != count && count > 0) {
				outOrderBatchId = BATCH_PREFIX + productName +serialNumberGenerator.getSerialNumber(3, RedisKey.OUT_ORDER_NO_KEY,organizationId + sysId, CommonUtil.getSecondsNextEarlyMorning());
				queryWrapper.eq(ProductionManageOutOrder.COL_OUT_ORDER_BATCH_ID, outOrderBatchId);
				count = dao.selectCount(queryWrapper);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			queryWrapper.eq(ProductionManageOutOrder.COL_OUT_ORDER_BATCH_ID, outOrderBatchId);
			Integer count = dao.selectCount(queryWrapper);
			if (null != count && count > 0) {
				throw new SuperCodeException("订单号已存在", 500);
			}
		}
		BeanUtils.copyProperties(pOrderDTO, pOutOrder);
		pOutOrder.setOutOrderBatchId(outOrderBatchId);
		Employee employee = commonUtil.getEmployee();
		pOutOrder.setCreateUserId(employee.getEmployeeId());
		pOutOrder.setSysId(sysId);
	    pOutOrder.setAuthDepartmentId(employee.getDepartmentId());
		pOutOrder.setOrganizationId(organizationId);
		pOutOrder.setCreateDate(CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
		dao.insert(pOutOrder);
    
}

public void update(ProductionManageOutOrderDTO pOrderDTO) throws ParseException, SuperCodeException {
	if (null==pOrderDTO.getId()) {
		throw new SuperCodeException("订单id不能为空", 500);
	}
	ProductionManageOutOrder existOutOrder=baseMapper.selectById(pOrderDTO.getId());
	if (null==existOutOrder){
		throw new SuperCodeException("外采订单不存在", 500);
	}
	ProductionManageOutOrder pOutOrder=new ProductionManageOutOrder();
	BeanUtils.copyProperties(pOrderDTO, pOutOrder);
	String organizationId = commonUtil.getOrganizationId();
	String sysId = commonUtil.getSysId();
	String outOrderBatchId = pOrderDTO.getOutOrderBatchId();
	String productName=pOrderDTO.getProductName();
	QueryWrapper<ProductionManageOutOrder> queryWrapper = new QueryWrapper<>();
	queryWrapper.eq(ProductManageConstant.SYS_ORGANIZATIONID, organizationId);
	queryWrapper.eq(ProductManageConstant.SYS_SYSID, sysId);

	if (StringUtils.isNotBlank(outOrderBatchId)) {
		queryWrapper.ne(ProductionManageOutOrder.COL_ID,pOrderDTO.getId());
		queryWrapper.eq(ProductionManageOutOrder.COL_OUT_ORDER_BATCH_ID, outOrderBatchId);
		Integer count = dao.selectCount(queryWrapper);
		if (null != count && count > 0) {
			throw new SuperCodeException("批次号已存在", 500);
		}
	}else{
		pOutOrder.setOutOrderBatchId(existOutOrder.getOutOrderBatchId());
	}
	OrderTypeEnum byStatus = OrderTypeEnum.getByStatus(pOrderDTO.getOrderType());
	switch (byStatus){
		case WEIGHT:
			pOutOrder.setOutBoxNum(null);
			pOutOrder.setOutNum(null);
			break;
		case BOX_NUN:
			pOutOrder.setOutOrderWeight(null);
			pOutOrder.setOutNum(null);
			break;
		case NUM:
			pOutOrder.setOutBoxNum(null);
			pOutOrder.setOutOrderWeight(null);
			break;
		default:break;
	}
    Employee employee=commonUtil.getEmployee();
    pOutOrder.setUpdateDate(CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
    pOutOrder.setUpdateUserId(employee.getEmployeeId());
	pOutOrder.setOrganizationId(organizationId);
	pOutOrder.setSysId(sysId);
	pOutOrder.setCreateUserId(existOutOrder.getCreateUserId());
	pOutOrder.setCreateDate(existOutOrder.getCreateDate());
    dao.updateById(pOutOrder);
}

	@Override
	public PageResults<List<ProductionManageOutOrderListVO>> page(DaoSearch e) throws SuperCodeException {

		String sysId = commonUtil.getSysId();
		String organizationId = commonUtil.getOrganizationId();
		ProductionManageOutOrderListDTO orderListDTO = (ProductionManageOutOrderListDTO) e;
		orderListDTO.setSysId(sysId);
		orderListDTO.setOrganizationId(organizationId);

		Page<ProductionManageOutOrderListVO> page = new Page<>(orderListDTO.getDefaultCurrent(), orderListDTO.getDefaultPageSize());
		QueryWrapper<ProductionManageOutOrder> queryWrapper = new QueryWrapper();
		// 查询指定的任务类型数据
		queryWrapper.eq(StringUtils.isNotBlank(organizationId), ProductionManageOutOrder.COL_ORGANIZATION_ID, organizationId)
				.eq(StringUtils.isNotBlank(sysId), ProductionManageOutOrder.COL_SYS_ID, sysId);
		// 判断搜索框是否为空,为空，则进行高级搜索，不为空，则普通搜索

		String search = orderListDTO.getSearch();
		if (StringUtils.isBlank(search)) {
			String[] dateInteral = LocalDateTimeUtil.substringDate(orderListDTO.getPurchaseDate());
			queryWrapper.like(StringUtils.isNotBlank(orderListDTO.getBaseName()), ProductionManageOutOrder.COL_BASE_NAME, orderListDTO.getBaseName())
					.like(StringUtils.isNotBlank(orderListDTO.getPurchaseUserName()), ProductionManageOutOrder.COL_PURCHASE_USER_NAME, orderListDTO.getPurchaseUserName())
					.eq(StringUtils.isNotBlank(orderListDTO.getOutOrderBatchId()), ProductionManageOutOrder.COL_OUT_ORDER_BATCH_ID, orderListDTO.getOutOrderBatchId())
					.ge(StringUtils.isNotBlank(dateInteral[0]), ProductionManageOutOrder.COL_PURCHASE_DATE, dateInteral[0])
					.le(StringUtils.isNotBlank(dateInteral[1]), ProductionManageOutOrder.COL_PURCHASE_DATE, dateInteral[1])
					.eq(StringUtils.isNotBlank(orderListDTO.getPrincipalName()), ProductionManageOutOrder.COL_PRINCIPAL_NAME, orderListDTO.getPrincipalName())
					.eq(StringUtils.isNotBlank(orderListDTO.getContactPhone()), ProductionManageOutOrder.COL_CONTACT_PHONE, orderListDTO.getContactPhone())
					.like(StringUtils.isNotBlank(orderListDTO.getProductName()), ProductionManageOutOrder.COL_PRODUCT_NAME, orderListDTO.getProductName());
		} else {
			queryWrapper.and(outorder -> outorder.like(ProductionManageOutOrder.COL_OUT_ORDER_BATCH_ID, search)
					.or().like(ProductionManageOutOrder.COL_PRODUCT_NAME, search)
					.or().like(ProductionManageOutOrder.COL_BASE_NAME, search)
					.or().like(ProductionManageOutOrder.COL_PRINCIPAL_NAME, search)
					.or().like(ProductionManageOutOrder.COL_PURCHASE_USER_NAME, search)
					.or().like(ProductionManageOutOrder.COL_CONTACT_PHONE, search)
			);
		}
		queryWrapper.orderByDesc(ProductionManageOutOrder.COL_CREATE_DATE);
		// 添加数据权限
		commonUtil.roleDataAuthFilter(OUTSIDE_ORDER, queryWrapper, ProductionManageOutOrder.COL_CREATE_USER_ID, StringUtils.EMPTY);
		IPage<ProductionManageOutOrderListVO> ipage = dao.pageList(page, queryWrapper);
		return CommonUtil.iPageToPageResults(ipage, null);
	}

	/**
	 * 导出外采订单excel
	 * @param orderListDTO
	 * @param response
	 */
    public void exportOutOrderExcel(ProductionManageOutOrderListDTO orderListDTO, HttpServletResponse response) throws SuperCodeException {
		ArrayList<String> idList = orderListDTO.getIdList();
		List<ProductionManageOutOrderListVO> list;
		if (CollectionUtils.isEmpty(idList)) {
			orderListDTO.setCurrent(1);
			orderListDTO.setPageSize(commonUtil.getExportNumber());
			PageResults<List<ProductionManageOutOrderListVO>> page = page(orderListDTO);
			list = page.getList();
		} else {
			QueryWrapper<ProductionManageOutOrderListVO> queryWrapper = commonUtil.queryTemplate(ProductionManageOutOrderListVO.class);
			queryWrapper.and(query -> query.in(ProductionManageOutOrder.COL_ID, idList));
			list = dao.selectByIds(queryWrapper);
		}
		ExcelUtils.listToExcel(list, orderListDTO.exportMetadataToMap(), "外采订单", response);
	}
}
