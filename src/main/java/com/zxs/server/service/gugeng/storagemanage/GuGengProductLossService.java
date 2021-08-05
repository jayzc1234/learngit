package com.zxs.server.service.gugeng.storagemanage;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.AddPlReqDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchPlReqDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.UpdatePlReqDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.BatchTypesEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.LossConfirmationStatusEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.ProductLossReasonEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.GuGengProductLoss;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.GuGengProductLossMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchPlDetailRespVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchPlRespVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.export.ExportSheetNameConstants.PRODUCT_LOSS_LIST;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.ProductLossErrorMsgConstants.PRODUCT_LOSS_CONFIRM_HAS_BEEN_FINISHED;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.ProductLossErrorMsgConstants.PRODUCT_LOSS_MESSAGE_IS_NOT_EXISTS;
import static net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.LossConfirmationStatusEnum.HAS_BEEN_CONFIRMED;


/**
 * @author shixiongfei
 * @date 2020-02-25
 * @since
 */
@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GuGengProductLossService extends ServiceImpl<GuGengProductLossMapper, GuGengProductLoss> {

    CommonUtil commonUtil;

    /**
     * 获取产品报损列表
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2020-02-25
     * @updateDate 2020-02-25
     * @updatedBy shixiongfei
     */
    public PageResults<List<SearchPlRespVO>> list(SearchPlReqDTO request) {
        val iPage = baseMapper.list(request, commonUtil.getSysId(), commonUtil.getOrganizationId());
        val pagination = new Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        val records = Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());
        val list = records.stream().map(record -> {
            SearchPlRespVO vo = new SearchPlRespVO();
            BeanUtils.copyProperties(record, vo);
            vo.setBatchType(String.valueOf(record.getBatchType()));
            vo.setReportLossStatus(String.valueOf(record.getReportLossStatus()));
            vo.setReportLossReason(String.valueOf(record.getReportLossReason()));
            return vo;
        }).collect(Collectors.toList());

        return new PageResults<>(list, pagination);
    }

    /**
     * 添加产品报损
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2020-02-25
     * @updateDate 2020-02-25
     * @updatedBy shixiongfei
     */
    public void add(AddPlReqDTO request) {
        val productLoss = new GuGengProductLoss();
        BeanUtils.copyProperties(request, productLoss);
        productLoss.setReportLossReason(request.getReportLossReason().intValue());
        productLoss.setReportLossStatus(LossConfirmationStatusEnum.TO_BE_CONFIRMED.getKey());
        productLoss.setDeleteOrNot(DeleteOrNotEnum.NOT_DELETED.getKey());
        productLoss.setCreateUserId(commonUtil.getEmployee().getEmployeeId());
        productLoss.setCreateUserName(commonUtil.getEmployee().getName());
        productLoss.setBatchType(request.getBatchType().intValue());
        save(productLoss);
    }

    /**
     * 获取产品报损详情
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2020-02-25
     * @updateDate 2020-02-25
     * @updatedBy shixiongfei
     */
    public SearchPlDetailRespVO getDetail(Long id) {
        val sysId = commonUtil.getSysId();
        val organizationId = commonUtil.getOrganizationId();
        val entity = baseMapper.getById(id, sysId, organizationId);
        CustomAssert.null2Error(entity, PRODUCT_LOSS_MESSAGE_IS_NOT_EXISTS);
        SearchPlDetailRespVO vo = new SearchPlDetailRespVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setBatchType(String.valueOf(entity.getBatchType()));
        vo.setReportLossStatus(String.valueOf(entity.getReportLossStatus()));
        vo.setReportLossReason(String.valueOf(entity.getReportLossReason()));
        return vo;
    }

    /**
     * 产品报损确认
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2020-02-25
     * @updateDate 2020-02-25
     * @updatedBy shixiongfei
     */
    public void lossConfirm(Long id, UpdatePlReqDTO request) {
        val sysId = commonUtil.getSysId();
        val organizationId = commonUtil.getOrganizationId();
        val entity = baseMapper.getById(id, sysId, organizationId);
        CustomAssert.null2Error(entity, PRODUCT_LOSS_MESSAGE_IS_NOT_EXISTS);
        if (HAS_BEEN_CONFIRMED.getKey() == entity.getReportLossStatus()) {
            CustomAssert.throwExtException(PRODUCT_LOSS_CONFIRM_HAS_BEEN_FINISHED);
        }

        GuGengProductLoss productLoss = new GuGengProductLoss();
        BeanUtils.copyProperties(request, productLoss);
        productLoss.setId(id).setBatchType(request.getBatchType().intValue())
                .setReportLossStatus(HAS_BEEN_CONFIRMED.getKey());
        updateById(productLoss);
    }

    /**
     * 产品报损导出
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2020-02-25
     * @updateDate 2020-02-25
     * @updatedBy shixiongfei
     */
    public void export(SearchPlReqDTO request, HttpServletResponse response) {
        List<SearchPlRespVO> list = request.parseStr2List(SearchPlRespVO.class);
        if (CollectionUtils.isEmpty(list)) {
            request.setCurrent(1);
            request.setPageSize(commonUtil.getExportNumber());
            list = list(request).getList();
        }

        list.forEach(vo -> {
            vo.setReportLossStatus(LossConfirmationStatusEnum.getValue(Byte.valueOf(vo.getReportLossStatus())));
            vo.setReportLossReason(ProductLossReasonEnum.getValue(Byte.valueOf(vo.getReportLossReason())));
            if (BatchTypesEnum.OUT_PLANT.getKey() == Integer.valueOf(vo.getBatchType())) {
                vo.setBatchName(vo.getBatchId());
            }
        });

        try {
            ExcelUtils.listToExcel(list, request.exportMetadataToMap(), PRODUCT_LOSS_LIST, response);
        } catch (Exception e) {
            throw new SuperCodeExtException(e.getMessage(), e);
        }
    }
}