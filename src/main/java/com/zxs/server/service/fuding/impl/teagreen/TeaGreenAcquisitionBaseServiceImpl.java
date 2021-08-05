package com.zxs.server.service.fuding.impl.teagreen;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.advancedsearch.common.AdvancedSearchUtil;
import com.jgw.supercodeplatform.advancedsearch.common.QuerySpecification;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.bo.fuding.TeaFarmerBO;
import net.app315.hydra.intelligent.planting.bo.fuding.TeaGreenAcquisitionBO;
import net.app315.hydra.intelligent.planting.enums.fuding.*;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.TeaFarmerDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionBaseDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionDO;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.teagreen.TeaGreenAcquisitionBaseMapper;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.ITeaFarmerService;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.ITeaFarmerYearOutputService;
import net.app315.hydra.intelligent.planting.server.service.fuding.task.TeaTaskService;
import net.app315.hydra.intelligent.planting.server.service.fuding.teagreen.ITeaGreenAcquisitionBaseService;
import net.app315.hydra.intelligent.planting.server.service.fuding.teagreen.ITeaGreenAcquisitionService;
import net.app315.hydra.intelligent.planting.utils.fuding.AuthorizationCodeUtil;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.utils.redis.RedisUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenAcquisitionPdaListVO;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenAcquisitionProductAddModel;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenAcquisitionSearchModel;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenAcquisitionVO;
import net.app315.hydra.user.sdk.provide.context.UserContextHelper;
import net.app315.hydra.user.sdk.provide.model.AccountCache;
import net.app315.nail.common.utils.DateUtil;
import net.app315.nail.common.utils.UUIDUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 茶青收购记录 服务实现类
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@Service
public class TeaGreenAcquisitionBaseServiceImpl extends ServiceImpl<TeaGreenAcquisitionBaseMapper, TeaGreenAcquisitionBaseDO> implements ITeaGreenAcquisitionBaseService {
    @Autowired
    private UserContextHelper userContextHelper;
    @Autowired
    private ITeaFarmerService teaFarmerService;
    @Autowired
    private ITeaGreenAcquisitionService teaGreenAcquisitionService;
    @Autowired
    private AuthorizationCodeUtil authorizationCodeUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ITeaFarmerYearOutputService teaFarmerYearOutputService;
    @Autowired
    private TeaTaskService teaTaskService;
    /**
     * 添加茶青收购记录
     * @param teaGreenAcquisitionBO 茶青收购对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeaGreenAcquisitionVO addAcquisition(TeaGreenAcquisitionBO teaGreenAcquisitionBO) {
        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal amount = BigDecimal.ZERO;
        TeaGreenAcquisitionBaseDO teaGreenAcquisitionBaseDO = CopyUtil.copy(teaGreenAcquisitionBO, new TeaGreenAcquisitionBaseDO());
        AccountCache accountCache = userContextHelper.getUserLoginCache();
        teaGreenAcquisitionBaseDO.setOperator(accountCache.getUserId());
        teaGreenAcquisitionBaseDO.setOperatorName(accountCache.getUserName());
        teaGreenAcquisitionBaseDO.setOrganizationId(accountCache.getOrganizationCache().getOrganizationId());
        teaGreenAcquisitionBaseDO.setOrganizationName(accountCache.getOrganizationCache().getOrganizationFullName());
        teaGreenAcquisitionBaseDO.setAcquisitionId(UUIDUtil.getUUID());

        // 设置作废状态为 未作废
        teaGreenAcquisitionBaseDO.setDisableStatus(DisableStatusEnum.UN_DISABLE.ordinal());
        saveWriteOffInfo(teaGreenAcquisitionBaseDO);

   /*     EmployeeCache department=userContextHelper.getOrganization().getEmployeeCache();
        if (department != null) {
//            teaGreenAcquisitionBaseDO.setDepartmentId(department.getDepartmentId());
//            teaGreenAcquisitionBaseDO.setDepartmentName(department.getDepartmentName());
            // 如果传入的合作社信息为空，设置为茶农的合作社
            if(StringUtils.isEmpty(teaGreenAcquisitionBaseDO.getCooperativeId())){
                TeaFarmerBO teaFarmer = teaFarmerService.getTeaFarmer(teaGreenAcquisitionBaseDO.getFarmerId());
                teaGreenAcquisitionBaseDO.setCooperativeId(teaFarmer.getCooperativeId());
                teaGreenAcquisitionBaseDO.setCooperativeName(teaFarmer.getCooperativeName());
            }
        }*/
        String contactNumber;
        //TODO::没有茶农信息新增
        String farmerId = teaGreenAcquisitionBO.getFarmerId();
        if (StringUtils.isBlank(farmerId)) {
            TeaFarmerBO teaFarmerBO = new TeaFarmerBO();
            teaFarmerBO.setContactNumber(teaGreenAcquisitionBO.getContactNumber());
            teaFarmerBO.setFarmerName(teaGreenAcquisitionBO.getFarmerName());
            farmerId = teaFarmerService.addTeaFarmer(teaFarmerBO);
            contactNumber=teaGreenAcquisitionBO.getContactNumber();
        } else {
             contactNumber = teaGreenAcquisitionBO.getContactNumber();
            TeaFarmerBO teaFarmer = teaFarmerService.getTeaFarmer(farmerId);
            teaFarmer.setContactNumber(contactNumber);
            teaFarmerService.updateTeaFarmer(teaFarmer);
        }
        teaGreenAcquisitionBaseDO.setFarmerId(farmerId);
        teaGreenAcquisitionBaseDO.setTransactionNo(getTransactionNo(accountCache.getOrganizationCache().getOrganizationId()));
        this.save(teaGreenAcquisitionBaseDO);
        // 保存详情
        List<TeaGreenAcquisitionProductAddModel> acquisitionProducts = teaGreenAcquisitionBO.getAcquisitionProducts();
        List<TeaGreenAcquisitionDO> teaGreenAcquisitionDOS = new ArrayList<>();
        if(!CollectionUtils.isEmpty(acquisitionProducts)){
            for (TeaGreenAcquisitionProductAddModel teaGreenAcquisitionProductAddModel : acquisitionProducts) {
                TeaGreenAcquisitionDO teaGreenAcquisitionDO = CopyUtil.copy(teaGreenAcquisitionBaseDO, new TeaGreenAcquisitionDO());
                teaGreenAcquisitionDO = CopyUtil.copy(teaGreenAcquisitionProductAddModel, teaGreenAcquisitionDO);
                teaGreenAcquisitionDO.setContactNumber(teaGreenAcquisitionBO.getContactNumber());
                teaGreenAcquisitionDOS.add(teaGreenAcquisitionDO);
                quantity = quantity.add(teaGreenAcquisitionDO.getQuantity());
                teaGreenAcquisitionProductAddModel.setAmount(teaGreenAcquisitionDO.getQuantity().multiply(teaGreenAcquisitionDO.getPrice()).setScale(2,BigDecimal.ROUND_HALF_UP));
                amount = amount.add(teaGreenAcquisitionProductAddModel.getAmount());
            }
        }
        amount = amount.setScale(2,BigDecimal.ROUND_HALF_UP);
        teaGreenAcquisitionService.saveBatch(teaGreenAcquisitionDOS);
        TeaGreenAcquisitionVO teaGreenAcquisitionVO = CopyUtil.copy(teaGreenAcquisitionBaseDO, new TeaGreenAcquisitionVO());
        teaGreenAcquisitionVO.setAcquisitionProducts(acquisitionProducts);
        teaGreenAcquisitionVO.setTotalAmount(amount);
        teaGreenAcquisitionVO.setContactNumber(contactNumber);
        teaGreenAcquisitionVO.setFarmerId(farmerId);
        //支付状态下修改年产量变化值
        //增加茶农年收购记录
        /*TeaFarmerYearOutputDO teaFarmerYearOutputDO = new TeaFarmerYearOutputBO();
        CopyUtil.copy(teaGreenAcquisitionBaseDO,teaFarmerYearOutputDO);
        teaFarmerYearOutputDO.setQuantity(quantity);
        teaFarmerYearOutputDO.setAmount(amount);
        teaFarmerYearOutputService.updateCurrentYearOutPut(teaFarmerYearOutputDO, YesOrNoEnum.YES);*/

        // 查询是否达到年产量提示值

        teaTaskService.teaGreenTask(teaGreenAcquisitionVO.getOrganizationId(), net.app315.nail.common.page.utils.DateUtil.format(teaGreenAcquisitionVO.getAcquisitionTime(), net.app315.nail.common.page.utils.DateUtil.monthDatePattern));

        return teaGreenAcquisitionVO;
    }

    private void saveWriteOffInfo(TeaGreenAcquisitionBaseDO teaGreenAcquisitionBaseDO) {
        AccountCache accountCache = userContextHelper.getUserLoginCache();

        // 根据支付状态设置 是否核销，已支付-》已核销，未支付-》未核销
        if(teaGreenAcquisitionBaseDO.getPayStatus().equals(PayStatusEnum.PAY.getCode())){
            teaGreenAcquisitionBaseDO.setWriteOffStatus(WriteOffStatusEnum.WRITE_Off.ordinal());
            teaGreenAcquisitionBaseDO.setWriteOffId(accountCache.getUserId());
            teaGreenAcquisitionBaseDO.setWriteOffName(accountCache.getUserName());
            teaGreenAcquisitionBaseDO.setWriteOffTime(new Date());
        }else {
            teaGreenAcquisitionBaseDO.setWriteOffStatus(WriteOffStatusEnum.UN_WRITE_Off.ordinal());
        }
    }

    @Override
    public AbstractPageService.PageResults<List<TeaGreenAcquisitionPdaListVO>> getPdaTeaGreenAcquisition(TeaGreenAcquisitionSearchModel teaGreenAcquisitionSearchModel) {
        List<QuerySpecification> querySpecifications = authorizationCodeUtil.getQueryWrapper(AuthorizationCode.TEA_ACQUISITION_RECORD);
        if(!CollectionUtils.isEmpty(querySpecifications)){
            teaGreenAcquisitionSearchModel.getAndConditions().addAll(querySpecifications);
        }
        AbstractPageService.PageResults<List<TeaGreenAcquisitionPdaListVO>> listPageResults = AdvancedSearchUtil.selectPage(teaGreenAcquisitionSearchModel, this.getBaseMapper(), TeaGreenAcquisitionPdaListVO.class);
        List<TeaGreenAcquisitionPdaListVO> acquisitionPdaListVOS = listPageResults.getList();
        if(!CollectionUtils.isEmpty(acquisitionPdaListVOS)){
            acquisitionPdaListVOS.forEach(teaGreenAcquisitionPdaListVO -> {
                // 计算总金额
                BigDecimal totalAmount = BigDecimal.ZERO;
                // 获取茶农信息
                TeaFarmerDO teaFarmer = this.teaFarmerService.getOne(new QueryWrapper<TeaFarmerDO>().lambda()
                        .eq(TeaFarmerDO::getFarmerId,teaGreenAcquisitionPdaListVO.getFarmerId()));
                if(teaFarmer != null){
                    // 防止operatorName被茶农中的数据覆盖
                    String operatorName = teaGreenAcquisitionPdaListVO.getOperatorName();
                    CopyUtil.copy(teaFarmer,teaGreenAcquisitionPdaListVO);
                    teaGreenAcquisitionPdaListVO.setOperatorName(operatorName);
                }
                // 获取详情
                List<TeaGreenAcquisitionProductAddModel> acquisitionProducts = new ArrayList<>();
                List<TeaGreenAcquisitionDO> targetList = teaGreenAcquisitionService.list(new QueryWrapper<TeaGreenAcquisitionDO>().lambda()
                        .eq(TeaGreenAcquisitionDO::getAcquisitionId, teaGreenAcquisitionPdaListVO.getAcquisitionId()));
                if(!CollectionUtils.isEmpty(targetList)){
                    for (TeaGreenAcquisitionDO teaGreenAcquisitionDO : targetList) {
                        TeaGreenAcquisitionProductAddModel productAddModel = CopyUtil.copy(teaGreenAcquisitionDO, new TeaGreenAcquisitionProductAddModel());
                        BigDecimal amount = productAddModel.getPrice().multiply(productAddModel.getQuantity()).setScale(2, BigDecimal.ROUND_HALF_UP);
                        productAddModel.setAmount(amount);
                        acquisitionProducts.add(productAddModel);
                        totalAmount = totalAmount.add(amount);
                    }
                }
                teaGreenAcquisitionPdaListVO.setTotalAmount(totalAmount);
                teaGreenAcquisitionPdaListVO.setAcquisitionProducts(acquisitionProducts);
            });
            listPageResults.setList(acquisitionPdaListVOS);
        }
        return listPageResults;
    }

    @Override
    public TeaGreenAcquisitionVO updateAcquisition(TeaGreenAcquisitionBO teaGreenAcquisitionBO) {
        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal amount = BigDecimal.ZERO;
        TeaGreenAcquisitionBaseDO teaGreenAcquisitionBaseDO = CopyUtil.copy(teaGreenAcquisitionBO, new TeaGreenAcquisitionBaseDO());
        // 设置作废状态为 未作废
//        teaGreenAcquisitionBaseDO.setDisableStatus(DisableStatusEnum.UN_DISABLE.ordinal());
        // 根据支付状态设置 是否核销，已支付-》已核销，未支付-》未核销
        saveWriteOffInfo(teaGreenAcquisitionBaseDO);

        this.updateById(teaGreenAcquisitionBaseDO);
        // 保存详情
        List<TeaGreenAcquisitionProductAddModel> acquisitionProducts = teaGreenAcquisitionBO.getAcquisitionProducts();
        List<TeaGreenAcquisitionDO> teaGreenAcquisitionDOS = new ArrayList<>();
        if(!CollectionUtils.isEmpty(acquisitionProducts)){
            for (TeaGreenAcquisitionProductAddModel teaGreenAcquisitionProductAddModel : acquisitionProducts) {
                TeaGreenAcquisitionDO teaGreenAcquisitionDO = CopyUtil.copy(teaGreenAcquisitionBaseDO, new TeaGreenAcquisitionDO());
                teaGreenAcquisitionDO = CopyUtil.copy(teaGreenAcquisitionProductAddModel, teaGreenAcquisitionDO);
                teaGreenAcquisitionDOS.add(teaGreenAcquisitionDO);
                quantity = quantity.add(teaGreenAcquisitionDO.getQuantity().subtract(teaGreenAcquisitionDO.getBareWeight()));
                teaGreenAcquisitionProductAddModel.setAmount((teaGreenAcquisitionDO.getQuantity().subtract(teaGreenAcquisitionDO.getBareWeight())).multiply(teaGreenAcquisitionDO.getPrice()).setScale(2,BigDecimal.ROUND_HALF_UP));
                amount = amount.add(teaGreenAcquisitionProductAddModel.getAmount());
            }
        }
        amount = amount.setScale(2,BigDecimal.ROUND_HALF_UP);
        teaGreenAcquisitionService.updateBatchById(teaGreenAcquisitionDOS);
        TeaGreenAcquisitionVO teaGreenAcquisitionVO = CopyUtil.copy(teaGreenAcquisitionBaseDO, new TeaGreenAcquisitionVO());
        teaGreenAcquisitionVO.setAcquisitionProducts(acquisitionProducts);
        teaGreenAcquisitionVO.setTotalAmount(amount);
        return teaGreenAcquisitionVO;
    }

    /**
     * 获取收购记录编号
     *
     * @return java.lang.String 记录编号t
     */
    private String getTransactionNo(String organizationId) {
        Date date = DateUtil.plusDays(1);
        String current = DateUtil.getCurrent(DateUtil.datePattern);
        long generate = redisUtil.generate(RedisKeyEnum.TEA_GREEN_NO.getKey() + ":" + organizationId+ ":" +current, date);
        //确定格式，把1转换为001
        DecimalFormat mFormat = new DecimalFormat("000");
        String no = mFormat.format(generate);
        return "CQ" + current + no;
    }

}
