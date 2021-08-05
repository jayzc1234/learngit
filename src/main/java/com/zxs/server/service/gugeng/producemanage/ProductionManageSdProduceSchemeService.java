package com.zxs.server.service.gugeng.producemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import jxl.CellView;
import jxl.write.*;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.ProductManageConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.model.excel.EXProductionManageSdProduceScheme;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.QueryMapperUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.ProductionManageSdProduceSchemeDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.ProductionManageSdProduceSchemeListDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.ProductionManageSdProduceSchemeNodeListDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManageSdProduceScheme;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManageSdProduceSchemeNode;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductionManageSdProduceSchemeMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.*;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.STANDARD_PRODUCE_PLAN;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.OLD_CREATE_USER_ID;


/**
 * <p>
 *  服务实现类
 * </p>
 * @author zc
 * @since 2019-06-14
 */
@Service
public class ProductionManageSdProduceSchemeService extends ServiceImpl<ProductionManageSdProduceSchemeMapper, ProductionManageSdProduceScheme> implements BaseService<ProductionManageSdProduceScheme> {
	private static final String SCHEME_NO_PREFIX="FA";

	@Autowired
	private ProductionManageSdProduceSchemeMapper dao;

	@Autowired
	private ProductionManageSdProduceSchemeNodeService pSchemeNodeService;

	@Autowired
	private SerialNumberGenerator serialNumberGenerator;

	@Autowired
	private CommonUtil commonUtil;

	@Transactional
	public void add(ProductionManageSdProduceSchemeDTO sdProduceSchemeDTO) throws SuperCodeException, ParseException {
		List<ProductionManageSdProduceSchemeNode> pSdProduceSchemeNodes=sdProduceSchemeDTO.getpSdProduceSchemeNodes();
		if (null==pSdProduceSchemeNodes || pSdProduceSchemeNodes.isEmpty()) {
			throw new SuperCodeException("方案节点信息不能为空", 500);
		}
		Integer schemeCycle=sdProduceSchemeDTO.getSchemeCycle();
		Set<Integer> days=new HashSet<>();
		for (ProductionManageSdProduceSchemeNode productionManageSdProduceSchemeNode : pSdProduceSchemeNodes) {
			Integer dayNum=productionManageSdProduceSchemeNode.getDayNumber();
			if (null==dayNum || dayNum<1) {
				throw new SuperCodeException("方案节点工作日不合法："+dayNum, 500);
			}
			days.add(productionManageSdProduceSchemeNode.getDayNumber());
		}
		if (schemeCycle.intValue()!=days.size()) {
			throw new SuperCodeException("方案节点数与周期值不匹配，周期为："+schemeCycle+",方案节点数为："+days.size(), 500);
		}

		String organizationId=commonUtil.getOrganizationId();
		String sysId=commonUtil.getSysId();
		String schemeNo=sdProduceSchemeDTO.getSchemeNo();

		if (StringUtils.isBlank(schemeNo)) {
			QueryWrapper<ProductionManageSdProduceScheme> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq(ProductManageConstant.SYS_ORGANIZATIONID, organizationId);
			queryWrapper.eq(ProductManageConstant.SYS_SYSID, sysId);
			schemeNo = SCHEME_NO_PREFIX + serialNumberGenerator.getSerialNumber(3, RedisKey.SD_PRODUCE_SCHEME_NO_KEY, organizationId + sysId, CommonUtil.getSecondsNextEarlyMorning());
			queryWrapper.eq(ProductionManageSdProduceScheme.COL_SCHEME_NO, schemeNo);
			Integer count = dao.selectCount(queryWrapper);
			while (null != count && count > 0) {
				schemeNo = SCHEME_NO_PREFIX + serialNumberGenerator.getSerialNumber(3, RedisKey.SD_PRODUCE_SCHEME_NO_KEY, organizationId + sysId, CommonUtil.getSecondsNextEarlyMorning());
				queryWrapper.eq(ProductionManageSdProduceScheme.COL_SCHEME_NO, schemeNo);
				count = dao.selectCount(queryWrapper);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}else {
			Map<String, Object> columMap = QueryMapperUtil.singleQueryColumnMap(ProductionManageSdProduceScheme.COL_SCHEME_NO, schemeNo, commonUtil.getOrganizationId(), commonUtil.getSysId());
			List<ProductionManageSdProduceScheme> listByNo = dao.selectByMap(columMap);
			if (null != listByNo && !listByNo.isEmpty()) {
				throw new SuperCodeException("该方案编号已存在", 500);
			}

			Map<String, Object> columMap2 = QueryMapperUtil.singleQueryColumnMap(ProductionManageSdProduceScheme.COL_SCHEME_NAME, sdProduceSchemeDTO.getSchemeName(), commonUtil.getOrganizationId(), commonUtil.getSysId());
			List<ProductionManageSdProduceScheme> listByName = dao.selectByMap(columMap2);
			if (null != listByName && !listByName.isEmpty()) {
				throw new SuperCodeException("该方案名称已存在", 500);
			}
		}
		Employee employee=commonUtil.getEmployee();
		ProductionManageSdProduceScheme pSdProduceScheme=new ProductionManageSdProduceScheme();
		pSdProduceScheme.setProductionForecast(sdProduceSchemeDTO.getProductionForecast());
		pSdProduceScheme.setSchemeName(sdProduceSchemeDTO.getSchemeName());
		pSdProduceScheme.setSchemeCycle(schemeCycle);
		pSdProduceScheme.setSchemeNo(schemeNo);
		pSdProduceScheme.setOrganizationId(organizationId);
		pSdProduceScheme.setSysId(sysId);
		pSdProduceScheme.setCreateDate(CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
		pSdProduceScheme.setCreateUserId(employee.getEmployeeId());
		pSdProduceScheme.setAuthDepartmentId(employee.getDepartmentId());
		dao.insert(pSdProduceScheme);
		Long sdProduceSchemeId=pSdProduceScheme.getId();
		for (ProductionManageSdProduceSchemeNode productionManageSdProduceSchemeNode : pSdProduceSchemeNodes) {
			productionManageSdProduceSchemeNode.setSdProduceSchemeId(sdProduceSchemeId);
//		  pSchemeNodeService.save(productionManageSdProduceSchemeNode);
		}
		pSchemeNodeService.saveBatch(pSdProduceSchemeNodes);
	}
	/**
	 * 只编辑
	 * @param sdProduceSchemeDTO
	 * @throws SuperCodeException
	 * @throws ParseException
	 */
	@Transactional
	public void update(ProductionManageSdProduceSchemeDTO sdProduceSchemeDTO) throws SuperCodeException, ParseException {
		Integer schemeCycle=sdProduceSchemeDTO.getSchemeCycle();
		if (null==schemeCycle || schemeCycle<1) {
			throw new SuperCodeException("方案周期不合法", 500);
		}
		Long schemeId=sdProduceSchemeDTO.getId();
		if (null==schemeId) {
			throw new SuperCodeException("方案节点主键不能为空", 500);
		}
		List<ProductionManageSdProduceSchemeNode> pSdProduceSchemeNodes=sdProduceSchemeDTO.getpSdProduceSchemeNodes();
		if (null==pSdProduceSchemeNodes || pSdProduceSchemeNodes.isEmpty()) {
			throw new SuperCodeException("方案节点信息不能为空", 500);
		}
		String organizationId= commonUtil.getOrganizationId();
		String sysId=commonUtil.getSysId();
		List<ProductionManageSdProduceScheme> schemes=dao.selectByMap(QueryMapperUtil.singleQueryColumnMap("Id", schemeId, organizationId, sysId));
		if (null==schemes || schemes.isEmpty()) {
			throw new SuperCodeException("该方案不存在", 500);
		}

		for (ProductionManageSdProduceSchemeNode productionManageSdProduceSchemeNode : pSdProduceSchemeNodes) {
			Integer dayNumer=productionManageSdProduceSchemeNode.getDayNumber();
			if (null == dayNumer || dayNumer < 1 || dayNumer > schemeCycle) {
				throw new SuperCodeException("方案节点工作日不合法：" + dayNumer, 500);
			}
		}

		//校验方案名称
		String schemeName = sdProduceSchemeDTO.getSchemeName();
		QueryWrapper<ProductionManageSdProduceScheme> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(ProductManageConstant.SYS_ORGANIZATIONID, organizationId);
		queryWrapper.eq(ProductManageConstant.SYS_SYSID, sysId);
		queryWrapper.ne(ProductionManageSdProduceScheme.COL_ID, schemeId);
		queryWrapper.eq(ProductionManageSdProduceScheme.COL_SCHEME_NAME, schemeName);
		int count = dao.selectCount(queryWrapper);
		if (count > 0) {
			throw new SuperCodeException("该方案名称已存在", 500);
		}
		List<Long> deleteSchemeNodeIds = sdProduceSchemeDTO.getDeleteSchemeNodeIds();
		if (null != deleteSchemeNodeIds && !deleteSchemeNodeIds.isEmpty()) {
			pSchemeNodeService.deleteByIds(deleteSchemeNodeIds);
		}
		Employee employee = commonUtil.getEmployee();
		ProductionManageSdProduceScheme pSdProduceScheme = schemes.get(0);
		pSdProduceScheme.setProductionForecast(sdProduceSchemeDTO.getProductionForecast());
		pSdProduceScheme.setSchemeName(sdProduceSchemeDTO.getSchemeName());
		pSdProduceScheme.setSchemeCycle(schemeCycle);
		pSdProduceScheme.setUpdateDate(CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
		pSdProduceScheme.setUpdateUserId(employee.getEmployeeId());
		dao.updateById(pSdProduceScheme);
		for (ProductionManageSdProduceSchemeNode productionManageSdProduceSchemeNode : pSdProduceSchemeNodes) {
			productionManageSdProduceSchemeNode.setSdProduceSchemeId(schemeId);
		}
		pSchemeNodeService.saveOrUpdateBatch(pSdProduceSchemeNodes);
	}

	@Override
	public PageResults<List<ProductionManageSdProduceScheme>> page(DaoSearch daoSearch) {
		ProductionManageSdProduceSchemeListDTO pSchemeListDTO = (ProductionManageSdProduceSchemeListDTO) daoSearch;
		QueryWrapper<ProductionManageSdProduceScheme> queryWrapper = commonUtil.queryTemplate(null);
		String search = pSchemeListDTO.getSearch();
		if (StringUtils.isNotBlank(search)) {
			queryWrapper.and(scheme -> scheme.like(ProductionManageSdProduceScheme.COL_SCHEME_NO, search)
					.or().like(ProductionManageSdProduceScheme.COL_SCHEME_NAME, search)
					.or().like(ProductionManageSdProduceScheme.COL_SCHEME_CYCLE, search)
					.or().like(ProductionManageSdProduceScheme.COL_PRODUCTION_FORECAST, search)
			);
		} else {
			queryWrapper.eq(StringUtils.isNotBlank(pSchemeListDTO.getSchemeName()), ProductionManageSdProduceScheme.COL_SCHEME_NAME, pSchemeListDTO.getSchemeName()).
					eq(StringUtils.isNotBlank(pSchemeListDTO.getSchemeNo()), ProductionManageSdProduceScheme.COL_SCHEME_NO, pSchemeListDTO.getSchemeNo());
		}

		queryWrapper.orderByDesc(ProductionManageSdProduceScheme.COL_CREATE_DATE);

		// 添加数据权限
		commonUtil.roleDataAuthFilter(STANDARD_PRODUCE_PLAN, queryWrapper, OLD_CREATE_USER_ID, StringUtils.EMPTY);
		Page<ProductionManageSdProduceScheme> page = new Page<>(pSchemeListDTO.getDefaultCurrent(), pSchemeListDTO.getDefaultPageSize());
		IPage<ProductionManageSdProduceScheme> ipage = dao.selectPage(page, queryWrapper);
		return CommonUtil.iPageToPageResults(ipage, null);
	}

	@Override
	public List<ProductionManageSdProduceScheme> listExcelByIds(List<? extends Serializable> ids) throws SuperCodeException {
		return baseMapper.selectBatchIds(ids);
	}
	@Transactional
	public void delete(Long id) throws SuperCodeException {
		Map<String, Object> columMap = QueryMapperUtil.singleQueryColumnMap(ProductionManageSdProduceScheme.COL_ID, id, commonUtil.getOrganizationId(), commonUtil.getSysId());
		List<ProductionManageSdProduceScheme> list = dao.selectByMap(columMap);
		if (null == list || list.isEmpty()) {
			throw new SuperCodeException("该方案不存在", 500);
		}
		dao.deleteById(id);
		pSchemeNodeService.deleteBySchemeId(id);

	}

	public void exportSchemeExcelService(ProductionManageSdProduceSchemeNodeListDTO pSchemeNodeListDTO,
										 HttpServletResponse response) throws SuperCodeException, IOException {
		String organizationId = commonUtil.getOrganizationId();
		String sysId = commonUtil.getSysId();
		QueryWrapper<ProductionManageSdProduceScheme> queryWrapper = QueryMapperUtil.singleAndWrapQuery(ProductionManageSdProduceScheme.COL_ID, pSchemeNodeListDTO.getSdProduceSchemeId(), organizationId, sysId);
		ProductionManageSdProduceScheme pSdProduceScheme = dao.selectOne(queryWrapper);
		if (null == pSdProduceScheme) {
			throw new SuperCodeException("该方案不存在", 500);
		}

		List<ProductionManageSdProduceSchemeNode> nodes = pSchemeNodeService.selectBySchemeId(pSchemeNodeListDTO.getSdProduceSchemeId());
		ExcelUtils.WriteSheet writeSheet = new ExcelUtils.WriteSheet() {
			@Override
			public void write(WritableSheet sheet) {
				try {
					for (int i = 0; i < nodes.size(); i++) {
						ProductionManageSdProduceSchemeNode pSchemeNode = nodes.get(i);
						Integer dayNum = pSchemeNode.getDayNumber();

						Label lblDayNum = new Label(i, 3, String.format("第%s天", dayNum));
						String workDetail = pSchemeNode.getWorkDetail();
						WritableFont writableFont = new WritableFont(WritableFont.createFont("宋体"), 11, WritableFont.NO_BOLD, false);
						WritableCellFormat writableCellFormat = new WritableCellFormat(writableFont);
						writableCellFormat.setVerticalAlignment(VerticalAlignment.TOP);
						writableCellFormat.setWrap(true);

						Label lblContent = new Label(i, 4, workDetail, writableCellFormat);
						try {
							sheet.addCell(lblDayNum);
							sheet.addCell(lblContent);
							CellView cellView = new CellView();
							cellView.setAutosize(true); //设置自动大小
							sheet.setColumnView(i, 35);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					sheet.setRowView(4, 2200, false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		ExcelUtils.listToExcel(Arrays.asList(pSdProduceScheme), ExcelUtils.beanToFieldMap(EXProductionManageSdProduceScheme.class), "生产详情",response, null);//导出文件

	}

	public ProductionManageSdProduceScheme selectById(Long id) throws SuperCodeException {
		Map<String, Object> columnMap = QueryMapperUtil.singleQueryColumnMap(ProductionManageSdProduceScheme.COL_ID, id, commonUtil.getOrganizationId(), commonUtil.getSysId());
		List<ProductionManageSdProduceScheme> list = dao.selectByMap(columnMap);
		if (null == list || list.isEmpty()) {
			throw new SuperCodeException("该方案不存在", 500);
		}
		return list.get(0);
	}

	/**
	 * 导出excel
	 * @param pSchemeNodeListDTO
	 * @param response
	 * @throws SuperCodeException
	 * @throws IOException
	 */
	public void exportSchemeListExcel(ProductionManageSdProduceSchemeListDTO pSchemeNodeListDTO, HttpServletResponse response) throws SuperCodeException {
		ArrayList<String> idList = pSchemeNodeListDTO.getIdList();
		List<ProductionManageSdProduceScheme> list;
		if (CollectionUtils.isEmpty(idList)) {
			pSchemeNodeListDTO.setCurrent(1);
			pSchemeNodeListDTO.setPageSize(commonUtil.getExportNumber());
			list= page(pSchemeNodeListDTO).getList();
		} else {
			QueryWrapper<ProductionManageSdProduceScheme> queryWrapper = commonUtil.queryTemplate(ProductionManageSdProduceScheme.class);
			queryWrapper.and(query -> query.in(ProductionManageSdProduceScheme.COL_ID, idList));
			list = dao.selectList(queryWrapper);
		}
		ExcelUtils.listToExcel(list, pSchemeNodeListDTO.exportMetadataToMap(),"生产档案列表", response);
	}

}
