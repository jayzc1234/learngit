package com.zxs.server.service.gugeng.purchasing;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.AddSupplierRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.SearchSupplierRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.UpdateSupplierRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.enums.gugeng.procurement.SupplierInvoiceTypeEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.procurement.SupplierTaxTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.purchasing.ProductionManageProcurementSupplier;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.purchasing.ProductionManageProcurementSupplierMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.purchasing.SearchSupplierDetailResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.purchasing.SearchSupplierResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-08
 */
@Service
public class ProductionManageProcurementSupplierService extends ServiceImpl<ProductionManageProcurementSupplierMapper, ProductionManageProcurementSupplier> {

    /**
     * 采购管理流水号前缀
     */
    private static final String PROCUREMENT_PREFIX = "CG";

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private ProductionManagePurchaseProductsService purchaseProductsService;


    /**
     * 添加采购供应商
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-08
     * @updateDate 2019-10-08
     * @updatedBy shixiongfei
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(AddSupplierRequestDTO requestDTO) throws SuperCodeException {

        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        // 校验供应商名称是否重复，重复则报错, 过滤掉前后空格
        requestDTO.setSupplierName(requestDTO.getSupplierName().trim());
        validSupplierNameIsRepeat(requestDTO.getSupplierName(), sysId, organizationId);

        // 判断编号是否为空，为空则自动生成
        if (StringUtils.isBlank(requestDTO.getSupplierNumber())) {
            String procurementNumber;
            while (true) {
                procurementNumber = numberGenerator.getSerialNumber(PROCUREMENT_PREFIX, 6,
                        RedisKey.PROCUREMENT_NO_KEY, organizationId + sysId, CommonUtil.getSecondsNextEarlyMorning());
                if (countBySerialNumber(procurementNumber) == 0) {
                    break;
                }
            }
            requestDTO.setSupplierNumber(procurementNumber);
        } else {
            requestDTO.setSupplierNumber(requestDTO.getSupplierNumber().trim());
            // 校验供应商编号是否重复，重复则报错
            Integer count = countBySerialNumber(requestDTO.getSupplierNumber());
            CustomAssert.isNotGreaterThanCustomNumber(count, 0, "供应商编号已存在，请重新输入");
        }

        ProductionManageProcurementSupplier entity = new ProductionManageProcurementSupplier();
        BeanUtils.copyProperties(requestDTO, entity);
        CustomAssert.isGreaterThan0(baseMapper.insert(entity), "添加采购供应商失败");
    }

    /**
     * 通过编号获取供应商个数
     *
     * @author shixiongfei
     * @date 2019-11-21
     * @updateDate 2019-11-21
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private Integer countBySerialNumber(String serialNumber) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        return query().eq(ProductionManageProcurementSupplier.COL_SUPPLIER_NUMBER, serialNumber)
                .eq(StringUtils.isNotBlank(sysId), ProductionManageProcurementSupplier.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageProcurementSupplier.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageProcurementSupplier.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .count();
    }

    /**
     * 编辑采购供应商
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(UpdateSupplierRequestDTO requestDTO) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        // 校验供应商名称是否重复，重复则报错
        validSupplierNameIsRepeat(requestDTO.getSupplierName(), sysId, organizationId, requestDTO.getId());

        // 这里查询主要是为了确保数据的正确性
        ProductionManageProcurementSupplier supplier = getNotDeletedById(requestDTO.getId());

        BeanUtils.copyProperties(requestDTO, supplier);

        CustomAssert.isGreaterThan0(baseMapper.updateById(supplier), "编辑采购供应商失败");

        // 校验采购供应商名称是否发生改变，改变则同步更新采购商品中的采购供应商名称
        if (!supplier.getSupplierName().equals(requestDTO.getSupplierName())) {
            purchaseProductsService.updateSupplierName(requestDTO.getId(), requestDTO.getSupplierName());
        }
    }

    /**
     * 获取采购供应商列表信息
     *
     * @param requestDTO 请求体对象
     * @param needAuth
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    public PageResults<List<SearchSupplierResponseVO>> list(SearchSupplierRequestDTO requestDTO, boolean needAuth) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        IPage<ProductionManageProcurementSupplier> iPage = baseMapper.list(requestDTO, sysId, organizationId, commonUtil, needAuth);

        Page pagination = new Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());

        List<ProductionManageProcurementSupplier> records = Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());
        List<SearchSupplierResponseVO> list = records.stream().map(record -> {
            SearchSupplierResponseVO responseVO = new SearchSupplierResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            responseVO.setRate(record.getRate().toString());
            responseVO.setSupplierTaxType(record.getSupplierTaxType().toString());
            responseVO.setSupplierInvoiceType(record.getSupplierInvoiceType().toString());
            return responseVO;
        }).collect(Collectors.toList());

        return new PageResults<>(list, pagination);
    }

    /**
     * 通过id获取未删除的供应商详情信息， 为空则报错
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    private ProductionManageProcurementSupplier getNotDeletedById(Long id) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        ProductionManageProcurementSupplier supplier = query().eq(ProductionManageProcurementSupplier.COL_ID, id)
                .eq(StringUtils.isNotBlank(sysId), ProductionManageProcurementSupplier.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageProcurementSupplier.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageProcurementSupplier.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .one();
        // 这里以后尽量采用运行时异常，检查异常实在是痛苦，获取捕获处理成运行时异常
        CustomAssert.isNull(supplier, "不存在此供应商详情信息，请检查");
        return supplier;
    }

    /**
     * 移除指定的采购供应商
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    public void remove(Long id) throws SuperCodeException {
        // 这里是为了防止地址栏被乱输入id导致错误的修改了其他系统下的数据信息
        getNotDeletedById(id);

        boolean isSuccess = update()
                .set(ProductionManageProcurementSupplier.COL_IS_DELETED, DeleteOrNotEnum.DELETED.getKey())
                .eq(ProductionManageProcurementSupplier.COL_ID, id)
                .update();
        CustomAssert.isSuccess(isSuccess, "删除采购供应商失败");

        // 移除供应商旗下的所有录入的商品信息
        purchaseProductsService.removeBySupplierId(id);
    }

    /**
     * 通过供应商名称来获取供应商的相关信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    public List<SearchSupplierResponseVO> listBySupplierName(String supplierName) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        List<ProductionManageProcurementSupplier> list = query()
                // 这里采用模糊搜索
                .like(StringUtils.isNotBlank(supplierName), ProductionManageProcurementSupplier.COL_SUPPLIER_NAME, supplierName)
                .eq(StringUtils.isNotBlank(sysId), ProductionManageProcurementSupplier.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageProcurementSupplier.COL_ORGANIZATION_ID, organizationId)
                .list();

        // 防止出现npe
        list = Optional.ofNullable(list).orElse(Collections.emptyList());

        return list.stream().map(supplier -> {
            SearchSupplierResponseVO responseVO = new SearchSupplierResponseVO();
            BeanUtils.copyProperties(supplier, responseVO);
            return responseVO;
        }).collect(Collectors.toList());
    }

    /**
     * 校验供应商名称是否重复，重复则报错
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    private void validSupplierNameIsRepeat(String supplierName, String sysId, String organizationId, Long... ids) throws SuperCodeException {
        // 校验供应商名称是否重复，重复则报错
        Long id = null;
        if (Objects.nonNull(ids) && ids.length > 0) {
            id = ids[0];
        }
        Integer count = query().eq(ProductionManageProcurementSupplier.COL_SUPPLIER_NAME, supplierName)
                .eq(StringUtils.isNotBlank(sysId), ProductionManageProcurementSupplier.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageProcurementSupplier.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageProcurementSupplier.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .ne(Objects.nonNull(id), ProductionManageProcurementSupplier.COL_ID, id)
                .count();
        CustomAssert.isNotGreaterThanCustomNumber(count, 0, "该采购供应商已存在，不可重复录入");
    }

    /**
     * 通过id获取供应商的详情信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    public SearchSupplierDetailResponseVO getDetailById(Long id) throws SuperCodeException {
        ProductionManageProcurementSupplier supplier = getNotDeletedById(id);

        SearchSupplierDetailResponseVO responseVO = new SearchSupplierDetailResponseVO();
        BeanUtils.copyProperties(supplier, responseVO);
        responseVO.setRate(supplier.getRate().toString());
        responseVO.setSupplierTaxType(supplier.getSupplierTaxType().toString());
        responseVO.setSupplierInvoiceType(supplier.getSupplierInvoiceType().toString());

        return responseVO;
    }

    /**
     * 采购供应商列表导出
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-10
     * @updateDate 2019-10-10
     * @updatedBy shixiongfei
     */
    public void export(SearchSupplierRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<SearchSupplierResponseVO> list = requestDTO.parseStr2List(SearchSupplierResponseVO.class);

        if (CollectionUtils.isEmpty(list)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = list(requestDTO,true).getList();
        }

        // 处理供应商税务类型和供应商开票类型及税率
        list.forEach(responseVO -> {
            responseVO.setSupplierTaxType(SupplierTaxTypeEnum.getValue(Integer.valueOf(responseVO.getSupplierTaxType())));
            responseVO.setSupplierInvoiceType(SupplierInvoiceTypeEnum.getValue(Integer.valueOf(responseVO.getSupplierInvoiceType())));
        });

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "采购供应商列表", response);
    }

    /**
     * 校验供应商是否失效，失效则提示错误信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-12
     * @updateDate 2019-10-12
     * @updatedBy shixiongfei
     */
    public void validBatchSupplierIsNotDeleted(List<Long> ids) {
        String organizationId = commonUtil.getOrganizationId();
        String sysId = commonUtil.getSysId();

        List<ProductionManageProcurementSupplier> suppliers = query().eq(StringUtils.isNotBlank(sysId), ProductionManageProcurementSupplier.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageProcurementSupplier.COL_ORGANIZATION_ID, organizationId)
                .in(ProductionManageProcurementSupplier.COL_ID, ids)
                .eq(ProductionManageProcurementSupplier.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .list();

        suppliers = Optional.ofNullable(suppliers).orElse(Collections.emptyList());

        if (ids.size() == suppliers.size()) {
            return;
        }

        Map<Long, String> map = suppliers.stream()
                .collect(Collectors.toMap(ProductionManageProcurementSupplier::getId, ProductionManageProcurementSupplier::getSupplierName));
        for (Long id : ids) {
            if (!map.containsKey(id)) {
                throw new SuperCodeExtException(String.format("[%s]不存在或已被移除, 请检查", map.get(id)));
            }
        }
    }
}