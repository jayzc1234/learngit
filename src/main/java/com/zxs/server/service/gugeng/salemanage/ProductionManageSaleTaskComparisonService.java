package com.zxs.server.service.gugeng.salemanage;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CollectionUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.SearchSTCRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.UpdateDepartmentSaleTaskRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.trace.EmployeeMsgDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.SaleTaskTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageSaleTask;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageSaleTaskComparison;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductionManageSaleTaskComparisonMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductionManageSaleTaskMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.SearchSTCResponseVO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 部门和个人销售任务对比服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-11-04
 */
@Service
public class ProductionManageSaleTaskComparisonService extends ServiceImpl<ProductionManageSaleTaskComparisonMapper, ProductionManageSaleTaskComparison> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageSaleTaskMapper saleTaskMapper;

    /**
     * 新增或更新销售任务对比数据
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-05
     * @updateDate 2019-11-05
     * @updatedBy shixiongfei
     */
    public void addOrUpdate(ProductionManageSaleTask saleTask, SaleTaskTypeEnum typeEnum) {
        // 过滤为空的情况
        if (Objects.isNull(saleTask.getDepartmentId())) {
            saleTask.setDepartmentId(StringUtils.EMPTY);
        }
        ProductionManageSaleTaskComparison comparison = getByDepAndProAndDate(saleTask.getDepartmentId(),
                saleTask.getProductId(), DateFormatUtils.format(saleTask.getSaleDate(), LocalDateTimeUtil.YEAR_AND_MONTH));
        // 如果对比数据为空，则新增，否则更新
        if (Objects.isNull(comparison)) {
            comparison = new ProductionManageSaleTaskComparison();
            BeanUtils.copyProperties(saleTask, comparison, "id", "createDate", "updateDate");

            // 如果为个人销售任务，需要获取个人所在的部门信息
            if (SaleTaskTypeEnum.PERSONAL == typeEnum) {
                // 获取部门信息
                EmployeeMsgDTO msg = commonUtil.getEmployeeMsg(commonUtil.getSuperToken(), saleTask.getSalesPersonnelId());
                if (Objects.isNull(msg)) {
                    throw new SuperCodeExtException("此销售人员不隶属任何部门，请检查");
                }
                // 线上环境可能出现部门信息为null的情况
                saleTask.setDepartmentId(Optional.ofNullable(msg.getDepartmentId()).orElse(StringUtils.EMPTY));
                saleTask.setDepartmentName(Optional.ofNullable(msg.getDepartmentName()).orElse(StringUtils.EMPTY));
            }

            // 设置部门id和部门名称
            comparison.setDepSaleTargetAmount(SaleTaskTypeEnum.DEPARTMENT == typeEnum ? BigDecimal.valueOf(saleTask.getTargetSaleAmount()) : BigDecimal.ZERO);
            comparison.setPerSaleTargetAmount(SaleTaskTypeEnum.PERSONAL == typeEnum ? BigDecimal.valueOf(saleTask.getTargetSaleAmount()) : BigDecimal.ZERO);
            comparison.setDepartmentId(saleTask.getDepartmentId());
            comparison.setDepartmentName(saleTask.getDepartmentName());
            comparison.setSysId(commonUtil.getSysId());
            comparison.setOrganizationId(commonUtil.getOrganizationId());

            save(comparison);
        } else {
            BigDecimal saleTargetAmount = SaleTaskTypeEnum.DEPARTMENT == typeEnum
                    ? comparison.getDepSaleTargetAmount().add(BigDecimal.valueOf(saleTask.getTargetSaleAmount()))
                    : comparison.getPerSaleTargetAmount().add(BigDecimal.valueOf(saleTask.getTargetSaleAmount()));

            update().set(SaleTaskTypeEnum.DEPARTMENT == typeEnum, "dep_sale_target_amount", saleTargetAmount)
                    .set(SaleTaskTypeEnum.PERSONAL == typeEnum, "per_sale_target_amount", saleTargetAmount)
                    .eq("id", comparison.getId())
                    .update();
        }
    }

    /**
     * 更新销售任务对比数据
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-05
     * @updateDate 2019-11-05
     * @updatedBy shixiongfei
     */
    public void update(UpdateDepartmentSaleTaskRequestDTO requestDTO, ProductionManageSaleTask oldTask, SaleTaskTypeEnum typeEnum, ProductionManageSaleTask saleTask) {
        /*
        1.如果部门 + 产品 + 销售时间都未发生改变时，则直接更新相关数值
        2.部门或产品或销售时间发生改变时，则扣减原先的数值，
          然后校验新的产品和部门是否在对比数据中存在，不存在则新增，存在则更新
         */
        boolean isTrue = !(requestDTO.getDepartmentId().equals(oldTask.getDepartmentId())
                && requestDTO.getProductId().equals(oldTask.getProductId())
                && requestDTO.getSaleDate().equals(DateFormatUtils.format(oldTask.getSaleDate(), LocalDateTimeUtil.YEAR_AND_MONTH)));
        if (isTrue) {
            ProductionManageSaleTaskComparison oldComparison = getByDepAndProAndDate(oldTask.getDepartmentId(), oldTask.getProductId(),
                    DateFormatUtils.format(oldTask.getSaleDate(), LocalDateTimeUtil.YEAR_AND_MONTH));
            // 计算扣减后的值，TODO 扣减的值存在为负值，是否需要将其清0
            BigDecimal updateAmount;
            if (SaleTaskTypeEnum.PERSONAL == typeEnum) {
                updateAmount = oldComparison.getPerSaleTargetAmount().subtract(BigDecimal.valueOf(oldTask.getTargetSaleAmount()));
            } else {
                updateAmount = oldComparison.getDepSaleTargetAmount().subtract(BigDecimal.valueOf(oldTask.getTargetSaleAmount()));
            }
            updateAmountById(oldComparison.getId(), typeEnum, updateAmount);
            // 校验修改的部门信息是否存在于销售任务对比中，不存在则新增，存在则更新
            addOrUpdate(saleTask, typeEnum);
        } else {
            // 获取销售任务对比数据信息
            ProductionManageSaleTaskComparison comparison = getByDepAndProAndDate(requestDTO.getDepartmentId(), requestDTO.getProductId(), requestDTO.getSaleDate());
            BigDecimal updateAmount;
            if (SaleTaskTypeEnum.PERSONAL == typeEnum) {
                updateAmount = comparison.getPerSaleTargetAmount().add(BigDecimal.valueOf(requestDTO.getTargetSaleAmount())).subtract(BigDecimal.valueOf(oldTask.getTargetSaleAmount()));
            } else {
                updateAmount = comparison.getDepSaleTargetAmount().add(BigDecimal.valueOf(requestDTO.getTargetSaleAmount())).subtract(BigDecimal.valueOf(oldTask.getTargetSaleAmount()));
            }
            updateAmountById(comparison.getId(), typeEnum, updateAmount);
        }
    }

    /**
     * 通过id,更新的金额来修改指定字段的值
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-07
     * @updateDate 2019-11-07
     * @updatedBy shixiongfei
     */
    private void updateAmountById(Long id, SaleTaskTypeEnum typeEnum, BigDecimal updateAmount) {
        update().set(SaleTaskTypeEnum.DEPARTMENT == typeEnum, "dep_sale_target_amount", updateAmount)
                .set(SaleTaskTypeEnum.PERSONAL == typeEnum, "per_sale_target_amount", updateAmount)
                .eq("id", id)
                .update();
    }

    /**
     * 获取月度的部门和个人销售任务对比
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-04
     * @updateDate 2019-11-04
     * @updatedBy shixiongfei
     */
    public AbstractPageService.PageResults<List<SearchSTCResponseVO>> monthSalesTaskComparison(SearchSTCRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        IPage<ProductionManageSaleTaskComparison> iPage = baseMapper.monthSalesTaskComparison(requestDTO, sysId, organizationId);
        Page pagination = new Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        List<ProductionManageSaleTaskComparison> records = CollectionUtils.elementIsNull(iPage.getRecords());
        List<SearchSTCResponseVO> list = records.stream().map(record -> {
            SearchSTCResponseVO responseVO = new SearchSTCResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            // 获取年 + 月度 ps:2019年2月
            responseVO.setSaleDate(LocalDateTimeUtil.getYearAndMonth(record.getSaleDate()));
            return responseVO;
        }).collect(Collectors.toList());

        return new AbstractPageService.PageResults<>(list, pagination);
    }

    /**
     * 获取季度的部门和个人销售任务对比
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-04
     * @updateDate 2019-11-04
     * @updatedBy shixiongfei
     */
    public AbstractPageService.PageResults<List<SearchSTCResponseVO>> quarterSalesTaskComparison(SearchSTCRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        IPage<ProductionManageSaleTaskComparison> iPage = baseMapper.quarterSalesTaskComparison(requestDTO, sysId, organizationId);
        Page pagination = new Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        List<ProductionManageSaleTaskComparison> records = CollectionUtils.elementIsNull(iPage.getRecords());
        List<SearchSTCResponseVO> list = records.stream().map(record -> {
            SearchSTCResponseVO responseVO = new SearchSTCResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            // 获取年 + 季度 ps:2019年第1季度
            responseVO.setSaleDate(LocalDateTimeUtil.getYearAndQuarterStr(record.getSaleDate()));
            return responseVO;
        }).collect(Collectors.toList());

        return new AbstractPageService.PageResults<>(list, pagination);
    }

    /**
     * 获取年度的部门和个人销售任务对比
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-04
     * @updateDate 2019-11-04
     * @updatedBy shixiongfei
     */
    public AbstractPageService.PageResults<List<SearchSTCResponseVO>> yearSalesTaskComparison(SearchSTCRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        IPage<ProductionManageSaleTaskComparison> iPage = baseMapper.yearSalesTaskComparison(requestDTO, sysId, organizationId);
        Page pagination = new Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        List<ProductionManageSaleTaskComparison> records = CollectionUtils.elementIsNull(iPage.getRecords());
        List<SearchSTCResponseVO> list = records.stream().map(record -> {
            SearchSTCResponseVO responseVO = new SearchSTCResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            // 获取年 ps:2019年
            responseVO.setSaleDate(LocalDateTimeUtil.getYearStr(record.getSaleDate()));
            return responseVO;
        }).collect(Collectors.toList());

        return new AbstractPageService.PageResults<>(list, pagination);
    }

    /**
     * 月度销售任务对比数据导出
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-05
     * @updateDate 2019-11-05
     * @updatedBy shixiongfei
     */
    public void monthSTCExport(SearchSTCRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<SearchSTCResponseVO> list = requestDTO.parseStr2List(SearchSTCResponseVO.class);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(list)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = monthSalesTaskComparison(requestDTO).getList();
        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "月度销售任务对比列表", response);
    }

    /**
     * 季度销售任务对比数据导出
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-05
     * @updateDate 2019-11-05
     * @updatedBy shixiongfei
     */
    public void quarterSTCExport(SearchSTCRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<SearchSTCResponseVO> list = requestDTO.parseStr2List(SearchSTCResponseVO.class);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(list)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = quarterSalesTaskComparison(requestDTO).getList();
        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "月度销售任务对比列表", response);
    }

    /**
     * 年度销售任务对比数据导出
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-05
     * @updateDate 2019-11-05
     * @updatedBy shixiongfei
     */
    public void yearSTCExport(SearchSTCRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<SearchSTCResponseVO> list = requestDTO.parseStr2List(SearchSTCResponseVO.class);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(list)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = yearSalesTaskComparison(requestDTO).getList();
        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "月度销售任务对比列表", response);
    }

    /**
     * 通过部门 + 产品 + 销售时间来获取销售任务对比数据
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-05
     * @updateDate 2019-11-05
     * @updatedBy shixiongfei
     */
    public ProductionManageSaleTaskComparison getByDepAndProAndDate(String departmentId, String productId, String saleDate) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        List<ProductionManageSaleTaskComparison> list = query().eq(StringUtils.isNotBlank(sysId), "sys_id", sysId)
                .eq(StringUtils.isNotBlank(organizationId), "organization_id", organizationId)
                .eq("department_id", departmentId)
                .eq("product_id", productId)
                .eq("DATE_FORMAT(sale_date, '%Y-%m')", saleDate)
                .list();
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(list)) {
            return null;
        }

        return list.get(0);
    }
}