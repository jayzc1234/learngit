package com.zxs.server.service.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.bo.gugeng.PackingOutboundNumBO;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.AddCodeLessPackageMessageRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.AddPackingOutboundRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchOutboundRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderOutBoundStatusEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderStatusEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.DeliveryTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClient;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutbound;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundPackageMessageMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductManageOrderService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchOutboundResponseVO;
import net.app315.hydra.user.data.auth.sdk.model.InterceptorUserRoleDataAuth;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.PACKING_OUTBOUND;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.DataPermissionConstants.PACKING_OUTBOUND_AUTH_DEPARTMENT_ID;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.DataPermissionConstants.PACKING_OUTBOUND_USER_ID;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-24
 */
@Service
public class ProductionManageOutboundService extends ServiceImpl<ProductionManageOutboundMapper, ProductionManageOutbound> implements BaseService<SearchOutboundResponseVO> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductManageOrderService orderService;

    @Autowired
    private ProductionManageOutboundPackageMessageMapper packageMessageMapper;

    @Autowired
    private ProductionManageOutboundPackageMessageService packageMessageService;

    /**
     * 出库时，如果不存在出库信息，则新增出库信息
     *
     * @param
     * @return 返回出库信息id
     * @author shixiongfei
     * @date 2019-09-09
     * @updateDate 2019-09-09
     * @updatedBy shixiongfei
     */
    public Long add(Long orderId) throws SuperCodeException {
        ProductionManageOutbound outbound = new ProductionManageOutbound();
        Employee employee = commonUtil.getEmployee();
        Date now = new Date();
        outbound.setOrderId(orderId);
        outbound.setCreateUserId(employee.getEmployeeId());
        outbound.setCreateUserName(employee.getName());
        outbound.setCreateDate(now);
        outbound.setOutboundDate(now);
        outbound.setSysId(commonUtil.getSysId());
        outbound.setOrganizationId(commonUtil.getOrganizationId());
        outbound.setPackingWeight(new BigDecimal(0));
        outbound.setPackingNum(0);
        outbound.setPackingBoxNum(0);
        CustomAssert.isGreaterThan0(baseMapper.insert(outbound), "新增出库信息失败");
        return outbound.getId();
    }

    /**
     * 通过订单id获取出库信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-07
     * @updateDate 2019-12-07
     * @updatedBy shixiongfei
     */
    public ProductionManageOutbound getByIdAndOrderId(Long orderId, Long outboundId) {
        List<ProductionManageOutbound> list =
                query().eq(ProductionManageOutbound.COL_ORDER_ID, orderId)
                        .eq(ProductionManageOutbound.COL_SYS_ID, commonUtil.getSysId())
                        .eq(ProductionManageOutbound.COL_ORGANIZATION_ID, commonUtil.getOrganizationId())
                        .eq(Objects.nonNull(outboundId), ProductionManageOutbound.COL_ID, outboundId)
                        .list();
        if (CollectionUtils.isEmpty(list)) {
            return null;
        } else {
            CustomAssert.greaterThanOne2Error(list.size(), "错误信息: 存在多个相同订单的出库信息");
            return list.get(0);
        }
    }

    /**
     * 新增出库信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-07
     * @updateDate 2019-12-07
     * @updatedBy shixiongfei
     */
    public void add(ProductionManageOutbound outbound) {
        CustomAssert.zero2Error(baseMapper.insert(outbound), "新增出库信息失败");
    }

    /**
     * 更新出库信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-07
     * @updateDate 2019-12-07
     * @updatedBy shixiongfei
     */
    public void update(ProductionManageOutbound outbound, AddCodeLessPackageMessageRequestDTO requestDTO) {
        // 更新出库信息
        BigDecimal weight = Optional.ofNullable(outbound.getPackingWeight()).orElse(BigDecimal.ZERO);
        Integer boxNum = Optional.ofNullable(outbound.getPackingBoxNum()).orElse(0);
        Integer quantity = Optional.ofNullable(outbound.getPackingNum()).orElse(0);
        Integer servings = Optional.ofNullable(outbound.getPackingServings()).orElse(0);
        boolean isSuccess = update().set(ProductionManageOutbound.COL_PACKING_WEIGHT, weight.add(requestDTO.getPackingWeight()))
                .set(ProductionManageOutbound.COL_PACKING_BOX_NUM, boxNum + requestDTO.getPackingBoxNum())
                .set(ProductionManageOutbound.COL_PACKING_NUM, quantity + requestDTO.getPackingNum())
                .set(ProductionManageOutbound.COL_GG_PACKING_SERVINGS, servings + requestDTO.getPackingServings())
                .eq(ProductionManageOutbound.COL_ID, outbound.getId())
                // 这里采用乐观锁的机制，防止同时多个对该出库信息进行新增或编辑造成数据不一致
                .eq(ProductionManageOutbound.COL_PACKING_WEIGHT, outbound.getPackingWeight())
                .eq(ProductionManageOutbound.COL_PACKING_BOX_NUM, outbound.getPackingBoxNum())
                .eq(ProductionManageOutbound.COL_PACKING_NUM, outbound.getPackingNum())
                .update();
        CustomAssert.false2Error(isSuccess, "更新出库信息失败，请稍后重试");
    }

    /**
     * 添加/更新出库信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-07
     * @updateDate 2019-12-07
     * @updatedBy shixiongfei
     */
    public ProductionManageOutbound addOrUpdate(ProductionManageOutbound outbound,
                            AddCodeLessPackageMessageRequestDTO packageMessage,
                            AddPackingOutboundRequestDTO requestDTO) {

        // 新增或更新出库信息,校验当前订单是否存在出库信息，存在则更新，不存在则新增
        if (Objects.isNull(outbound)) {
            // 添加出库信息,
            outbound = new ProductionManageOutbound();
            outbound.setOrderId(requestDTO.getOrderId());
            outbound.setPackingNum(packageMessage.getPackingNum());
            outbound.setPackingWeight(packageMessage.getPackingWeight());
            outbound.setPackingBoxNum(packageMessage.getPackingBoxNum());
            outbound.setPackingServings(packageMessage.getPackingServings());
            // 设置第二次发货原因
            outbound.setNeedSecDelivery(requestDTO.getNeedSecDelivery());
            outbound.setSecDeliveryReason(requestDTO.getSecDeliveryReason());
            add(outbound);
        } else {
            update(outbound, packageMessage);
        }
        return outbound;
    }

    /**
     * 获取出库信息列表
     *
     * @param daosearch
     */
    @Override
    public PageResults<List<SearchOutboundResponseVO>> list(DaoSearch daosearch) throws SuperCodeException {
        SearchOutboundRequestDTO requestDTO = (SearchOutboundRequestDTO) daosearch;
        Page<SearchOutboundResponseVO> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());

        QueryWrapper<SearchOutboundResponseVO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("o." + ProductionManageOrder.COL_SYS_ID, commonUtil.getSysId())
                .eq("o." + ProductionManageOrder.COL_ORGANIZATION_ID, commonUtil.getOrganizationId());
        //如果search为空则进行高级检索，不为空则进行普通检索
        if (StringUtils.isBlank(requestDTO.getSearch())) {
            String[] deliveryInterval = LocalDateTimeUtil.substringDate(requestDTO.getDeliveryDate());
            String[] outboundInterval = LocalDateTimeUtil.substringDate(requestDTO.getOutboundDate());

            queryWrapper.eq(StringUtils.isNotBlank(requestDTO.getOrderNo()), "o." + ProductionManageOrder.COL_ORDER_NO, requestDTO.getOrderNo())
                    .eq(StringUtils.isNotBlank(requestDTO.getClientName()), "c." + ProductionManageClient.COL_CLIENT_NAME, requestDTO.getClientName())
                    .ge(StringUtils.isNotBlank(deliveryInterval[0]), "o." + ProductionManageOrder.COL_DELIVERY_DATE, deliveryInterval[0])
                    .lt(StringUtils.isNotBlank(deliveryInterval[1]), "o." + ProductionManageOrder.COL_DELIVERY_DATE, LocalDateTimeUtil.addOneDay(deliveryInterval[1]))
                    .eq(StringUtils.isNotBlank(requestDTO.getProductNames()), "o." + ProductionManageOrder.COL_PRODUCT_NAMES, requestDTO.getProductNames())
                    .eq(Objects.nonNull(requestDTO.getOrderStatus()), "o." + ProductionManageOrder.COL_ORDER_STATUS, requestDTO.getOrderStatus())
                    .eq(Objects.nonNull(requestDTO.getOutboundStatus()), "o." + ProductionManageOrder.COL_OUTBOUND_STATUS, requestDTO.getOutboundStatus())
                    .ge(StringUtils.isNotBlank(outboundInterval[0]), "ob." + ProductionManageOutbound.COL_OUTBOUND_DATE, outboundInterval[0])
                    .le(StringUtils.isNotBlank(outboundInterval[1]), "ob." + ProductionManageOutbound.COL_OUTBOUND_DATE, outboundInterval[1])
                    .eq(StringUtils.isNotBlank(requestDTO.getCreateUserName()), "ob." + ProductionManageOutbound.COL_CREATE_USER_NAME, requestDTO.getCreateUserName())
                    .eq(Objects.nonNull(requestDTO.getDeliveryType()), "o." + ProductionManageOrder.COL_DELIVERY_TYPE, requestDTO.getDeliveryType());
        } else {
            queryWrapper.like(StringUtils.isNotBlank(requestDTO.getSearch()), "c." + ProductionManageClient.COL_CLIENT_NAME, requestDTO.getSearch());
        }

        // v1.9 新增数据权限
        InterceptorUserRoleDataAuth roleDataAuth = commonUtil.getRoleFunAuthWithAuthCode(PACKING_OUTBOUND);
        commonUtil.setAuthFilter(queryWrapper, roleDataAuth, PACKING_OUTBOUND_USER_ID, PACKING_OUTBOUND_AUTH_DEPARTMENT_ID, SearchOutboundResponseVO.class);

        // 这里就是将按订单信息进行分组，一个订单只会对应一条出库信息
        queryWrapper.groupBy("o." + ProductionManageOrder.COL_ID)
                .orderByDesc("o." + ProductionManageOrder.COL_ORDER_DATE, "o." + ProductionManageOrder.COL_ID);
        IPage<SearchOutboundResponseVO> iPage = baseMapper.list(page, queryWrapper);

        List<SearchOutboundResponseVO> records = net.app315.hydra.intelligent.planting.common.gugeng.util.CollectionUtils.elementIsNull(iPage.getRecords());

        // v2.0新加内容，校验当前订单的包装信息是否可以撤回
        if (CollectionUtils.isNotEmpty(records)) {
            List<Long> orderIds = records.stream().map(SearchOutboundResponseVO::getOrderId).collect(Collectors.toList());
            List<PackingOutboundNumBO> numBOS = packageMessageService.listMinObNumByOrderIds(orderIds);
            records.forEach(record -> {
                // 设置订单状态
                record.setOrderStatus(record.getOrderStatus());
                // 初始化量值, 后续可能涉及被改回
                record.setOrderWeight(Optional.ofNullable(record.getOrderWeight()).orElse(BigDecimal.ZERO));
                record.setOrderQuantity(Optional.ofNullable(record.getOrderQuantity()).orElse(0));
                record.setPackingNum(Optional.ofNullable(record.getPackingNum()).orElse(0));
                record.setPackingWeight(Optional.ofNullable(record.getPackingWeight()).orElse(BigDecimal.ZERO));

                int outboundNum = numBOS.stream().filter(t -> record.getOrderId().equals(t.getOrderId()))
                        .map(PackingOutboundNumBO::getOutboundNum)
                        // 如果没有获取到，则不对其分配撤回权限
                        .findFirst().orElse(1);
                record.setOutboundNum(outboundNum);
            });

        }
        return new PageResults<>(iPage.getRecords(),
                new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal()));
    }

    @Override
    public List<SearchOutboundResponseVO> listExcelByIds(List<? extends Serializable> ids) {
        List<SearchOutboundResponseVO> list = baseMapper.listExcelByIds(ids);
        if (CollectionUtils.isNotEmpty(list)) {
            for (SearchOutboundResponseVO outbound : list) {
                outbound.setOrderStatus(OrderStatusEnum.getDesc(outbound.getOrderStatus()));
                outbound.setOutboundStatus(OrderOutBoundStatusEnum.getDesc(outbound.getOutboundStatus()));
                outbound.setDeliveryType(DeliveryTypeEnum.getDesc(outbound.getDeliveryType()));
            }
        }
        return list;
    }

    @Override
    public void dataTransfer(List<SearchOutboundResponseVO> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (SearchOutboundResponseVO outbound : list) {
                outbound.setOutboundStatus(OrderOutBoundStatusEnum.getDesc(outbound.getOutboundStatus()));
                outbound.setOrderStatus(OrderStatusEnum.getDesc(outbound.getOrderStatus()));
                outbound.setDeliveryType(DeliveryTypeEnum.getDesc(outbound.getDeliveryType()));
            }
        }
    }

    /**
     * 修改更新时间
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDeliveryDate(Long outboundId, Date updateDate) {
        UpdateWrapper<ProductionManageOutbound> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set(ProductionManageOutbound.COL_OUTBOUND_DATE, updateDate);
        updateWrapper.eq(ProductionManageOutbound.COL_ID, outboundId);
        update(updateWrapper);
    }

    public void allDone(Long orderId) throws SuperCodeException {
        ProductionManageOrder order = orderService.selectById(orderId);
        if (null == order) {
            CommonUtil.throwSupercodeException(500, "订单不存在");
        }
        order.setOutboundStatus(OrderOutBoundStatusEnum.ALL_DELIVEY.getStatus());
        orderService.updateById(order);
    }

    /**
     * 校验是否需要显示二次发货
     *
     * @author shixiongfei
     * @date 2019-12-10
     * @updateDate 2019-12-10
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public Boolean validNeedSecondDelivery(Long orderId) {
        Integer count = packageMessageMapper.validNeedSecondDelivery(orderId);
        return count > 0 ? Boolean.FALSE : Boolean.TRUE;
    }
}