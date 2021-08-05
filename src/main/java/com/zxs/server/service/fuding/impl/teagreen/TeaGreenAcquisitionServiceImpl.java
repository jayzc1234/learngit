package com.zxs.server.service.fuding.impl.teagreen;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.advancedsearch.common.AdvancedSearchUtil;
import com.jgw.supercodeplatform.advancedsearch.common.QuerySpecification;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.bo.fuding.TeaGreenAcquisitionBO;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.enums.fuding.AuthorizationCode;
import net.app315.hydra.intelligent.planting.enums.fuding.DisableStatusEnum;
import net.app315.hydra.intelligent.planting.enums.fuding.PayStatusEnum;
import net.app315.hydra.intelligent.planting.enums.fuding.WriteOffStatusEnum;
import net.app315.hydra.intelligent.planting.exception.gugeng.TeaException;
import net.app315.hydra.intelligent.planting.exception.gugeng.base.ExcelException;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.TeaFarmerDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionBaseDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionDO;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.base.TeaFarmerMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.teagreen.TeaGreenAcquisitionBaseMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.teagreen.TeaGreenAcquisitionMapper;
import net.app315.hydra.intelligent.planting.server.service.fuding.task.TeaTaskService;
import net.app315.hydra.intelligent.planting.server.service.fuding.teagreen.ITeaGreenAcquisitionBaseService;
import net.app315.hydra.intelligent.planting.server.service.fuding.teagreen.ITeaGreenAcquisitionService;
import net.app315.hydra.intelligent.planting.server.util.ExcelUtils2;
import net.app315.hydra.intelligent.planting.utils.fuding.AuthorizationCodeUtil;
import net.app315.hydra.intelligent.planting.utils.fuding.TimeUtil;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.base.TeaFarmerVO;
import net.app315.hydra.intelligent.planting.vo.fuding.common.AcquisitionStatusVO;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.*;
import net.app315.hydra.user.sdk.provide.context.UserContextHelper;
import net.app315.hydra.user.sdk.provide.model.AccountCache;
import net.app315.nail.common.page.utils.DateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 茶青收购记录 服务实现类
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@Service
public class TeaGreenAcquisitionServiceImpl extends ServiceImpl<TeaGreenAcquisitionMapper, TeaGreenAcquisitionDO> implements ITeaGreenAcquisitionService {

    @Autowired
    private AuthorizationCodeUtil authorizationCodeUtil;
    @Autowired
    private UserContextHelper userContextHelper;
    @Autowired
    private ITeaGreenAcquisitionBaseService teaGreenAcquisitionBaseService;
    @Autowired
    private TeaGreenAcquisitionBaseMapper teaGreenAcquisitionBaseMapper;
    @Autowired
    private TeaFarmerMapper teaFarmerMapper;
    @Autowired
    private TeaTaskService teaTaskService;
    @Autowired
    private CommonUtil commonUtil;
    @Autowired
    private ExcelUtils2 excelUtils2;


    /**
     * 获取茶青收购记录详情
     *
     * @param acquisitionId 收购记录id
     * @return java.lang.String
     */
    @Override
    public TeaGreenAcquisitionBO getTeaGreenAcquisitionById(String acquisitionId) {
        TeaGreenAcquisitionDO hairyTeaAcquisitionDO = getOne(new QueryWrapper<TeaGreenAcquisitionDO>().lambda()
                .eq(TeaGreenAcquisitionDO::getAcquisitionId, acquisitionId));
        TeaGreenAcquisitionBO teaGreenAcquisitionBO = CopyUtil.copy(hairyTeaAcquisitionDO,new TeaGreenAcquisitionBO());
        return teaGreenAcquisitionBO;
    }
    /**
     * 茶青收购记录列表查询
     * @param teaGreenAcquisitionSearchModel 记录搜索对象
     * @return TeaGreenAcquisitionVO 收购记录列表
     */
    @Override
    public AbstractPageService.PageResults<List<TeaGreenAcquisitionListVO>> getTeaGreenAcquisition(TeaGreenAcquisitionSearchModel teaGreenAcquisitionSearchModel) {
//        teaGreenAcquisitionSearchModel.setGeneralParam(new String[]{"transaction_no", "farmer_name", "product_name", "base_name", "cooperative_name"});
        teaGreenAcquisitionSearchModel.setGeneralParam(new String[]{"transaction_no", "farmer_name", "contactNumber"});

        List<QuerySpecification> querySpecifications = authorizationCodeUtil.getQueryWrapper(AuthorizationCode.TEA_ACQUISITION_RECORD);
        if (!CollectionUtils.isEmpty(querySpecifications)) {
            teaGreenAcquisitionSearchModel.getAndConditions().addAll(querySpecifications);
        }
//        收购开始时间和结束时间
        if (StringUtils.isNotEmpty(teaGreenAcquisitionSearchModel.getAcquisitionStartTime())) {
            teaGreenAcquisitionSearchModel.setAcquisitionStartTime(teaGreenAcquisitionSearchModel.getAcquisitionStartTime() + " 00:00:00");
        }
        if (StringUtils.isNotEmpty(teaGreenAcquisitionSearchModel.getAcquisitionEndTime())) {
            teaGreenAcquisitionSearchModel.setAcquisitionEndTime(teaGreenAcquisitionSearchModel.getAcquisitionEndTime() + " 23:59:59");
        }
        AbstractPageService.PageResults<List<TeaGreenAcquisitionListVO>> pageResults = AdvancedSearchUtil.selectPage(teaGreenAcquisitionSearchModel, this.getBaseMapper(), TeaGreenAcquisitionListVO.class);
        if (!CollectionUtils.isEmpty(pageResults.getList())) {
            /*pageResults.getList().forEach(teaGreenAcquisitionListVO -> {
                BigDecimal bareWeight = teaGreenAcquisitionListVO.getBareWeight()==null?new BigDecimal("0.00"):teaGreenAcquisitionListVO.getBareWeight();
                BigDecimal quantity = teaGreenAcquisitionListVO.getQuantity();
                teaGreenAcquisitionListVO.setTeaGreenAcquisitionQuantity(quantity.subtract(bareWeight).setScale(2,BigDecimal.ROUND_HALF_UP));
                teaGreenAcquisitionListVO.setAmount(teaGreenAcquisitionListVO.getTeaGreenAcquisitionQuantity().multiply(teaGreenAcquisitionListVO.getPrice()).setScale(2,BigDecimal.ROUND_HALF_UP));
            });*/
            for (int i = 0; i < pageResults.getList().size(); i++) {
                TeaGreenAcquisitionListVO teaGreenAcquisitionListVO = pageResults.getList().get(i);
                teaGreenAcquisitionListVO.setButtonFlag(0);
                if (i == 0) {
                    teaGreenAcquisitionListVO.setButtonFlag(1);
                } else if (!teaGreenAcquisitionListVO.getAcquisitionId().equals(pageResults.getList().get(i - 1).getAcquisitionId())) {
                    teaGreenAcquisitionListVO.setButtonFlag(1);
                }
                BigDecimal bareWeight = teaGreenAcquisitionListVO.getBareWeight() == null ? new BigDecimal("0.00") : teaGreenAcquisitionListVO.getBareWeight();
                BigDecimal quantity = teaGreenAcquisitionListVO.getQuantity();
                teaGreenAcquisitionListVO.setTeaGreenAcquisitionQuantity(quantity.subtract(bareWeight).setScale(2, BigDecimal.ROUND_HALF_UP));
                teaGreenAcquisitionListVO.setAmount(teaGreenAcquisitionListVO.getTeaGreenAcquisitionQuantity().multiply(teaGreenAcquisitionListVO.getPrice()).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
        }
        return pageResults;
    }



    /**
     * 统计一个组织当天的茶青收购的数据
     * @return 统计数据
     */
    @Override
    public TeaGreenAcquisitionRealTimeStatisticsVO getTeaGreenAcquisitionRealTimeStatistics() {
        String organizationId = userContextHelper.getOrganizationId();
        // 查询统计数据
        TeaGreenAcquisitionRealTimeStatisticsVO todayTeaGreenAcquisitionStatistics = this.getBaseMapper().getTodayTeaGreenAcquisitionStatistics(organizationId);
        // 获取每小时的统计数据
        List<TeaGreenAcquisitionHourStatisticsVO> todayTeaGreenAcquisitionStatisticsByHour = this.getBaseMapper().getTodayTeaGreenAcquisitionStatisticsByHour(organizationId);
        // 获取没有数据的时间，补入值
        List<String> oneDayAllHours = TimeUtil.getOneDayAllHours();
        List<String> timeList = todayTeaGreenAcquisitionStatisticsByHour.stream().map(TeaGreenAcquisitionHourStatisticsVO::getCurrentHour).collect(Collectors.toList());
        List<String> lackTime = ListUtils.subtract(oneDayAllHours, timeList);
        lackTime.forEach(time -> todayTeaGreenAcquisitionStatisticsByHour.add(new TeaGreenAcquisitionHourStatisticsVO(time)));
        // 按照时间排序
        todayTeaGreenAcquisitionStatisticsByHour.sort(Comparator.comparing(TeaGreenAcquisitionHourStatisticsVO::getCurrentHour));
        todayTeaGreenAcquisitionStatistics.setHourStatistics(todayTeaGreenAcquisitionStatisticsByHour);
        return todayTeaGreenAcquisitionStatistics;
    }

    /**
     * 导出茶青收购记录
     *
     * @param daoSearch
     * @param response
     */
    @Override
    public void export(TeaGreenAcquisitionExportModel daoSearch, HttpServletResponse response) throws ExcelException {
        List<String> idList = daoSearch.getIdList();
        List list;
        // idList为空导出全部，不为空导出指定数据
        if (CollectionUtils.isEmpty(idList)) {
            daoSearch.setCurrent(1);
            daoSearch.setPageSize(9999);
            TeaGreenAcquisitionSearchModel advancedDaoSearch = new TeaGreenAcquisitionSearchModel();
            BeanUtils.copyProperties(daoSearch, advancedDaoSearch);
            if (StringUtils.isBlank(advancedDaoSearch.getAcquisitionEndTime())) {
                advancedDaoSearch.setAcquisitionEndTime(null);
            }
            if (StringUtils.isBlank(advancedDaoSearch.getAcquisitionStartTime())) {
                advancedDaoSearch.setAcquisitionStartTime(null);
            }
            if (StringUtils.isBlank(advancedDaoSearch.getWriteOffEndTime())) {
                advancedDaoSearch.setWriteOffEndTime(null);
            }
            if (StringUtils.isBlank(advancedDaoSearch.getWriteOffStartTime())) {
                advancedDaoSearch.setWriteOffStartTime(null);
            }
            AbstractPageService.PageResults<List<TeaGreenAcquisitionListVO>> pageResults = this.getTeaGreenAcquisitionBareWeight(advancedDaoSearch);
            list = pageResults.getList();
        } else {
            List<Long> ids = commonUtil.doIdList(idList);
            TeaGreenAcquisitionSearchModel advancedDaoSearch = new TeaGreenAcquisitionSearchModel();
            daoSearch.setCurrent(1);
            daoSearch.setPageSize(9999);
            advancedDaoSearch.setIdList(ids);
            AbstractPageService.PageResults<List<TeaGreenAcquisitionListVO>> pageResults = this.getTeaGreenAcquisitionBareWeight(advancedDaoSearch);
            list = pageResults.getList();
        }
        String exportMetadata = daoSearch.getExportMetadata();
        Map<String, String> exportMetadataMap;
        if (StringUtils.isBlank(exportMetadata)) {
            exportMetadataMap = excelUtils2.initExportMetadataMap(TeaGreenAcquisitionExportVO.class);
        } else {
            exportMetadataMap = daoSearch.exportMetadataToMap();
        }
        List<TeaGreenAcquisitionExportVO> exportVOList = new ArrayList<>();
        list.forEach(e -> {
            TeaGreenAcquisitionListVO teaGreenAcquisitionListVO = (TeaGreenAcquisitionListVO) e;
            TeaGreenAcquisitionExportVO teaGreenAcquisitionExportVO = new TeaGreenAcquisitionExportVO();
            BeanUtils.copyProperties(teaGreenAcquisitionListVO, teaGreenAcquisitionExportVO);
            teaGreenAcquisitionExportVO.setDisableStatus(DisableStatusEnum.DISABLE.ordinal() == teaGreenAcquisitionListVO.getDisableStatus() ? DisableStatusEnum.DISABLE.getDesc() : DisableStatusEnum.UN_DISABLE.getDesc());
            teaGreenAcquisitionExportVO.setPayStatus(PayStatusEnum.PAY.getCode().equals(teaGreenAcquisitionListVO.getPayStatus()) ? PayStatusEnum.PAY.getDesc() : PayStatusEnum.UN_PAY.getDesc());
            exportVOList.add(teaGreenAcquisitionExportVO);
        });
        ExcelUtils.listToExcel(exportVOList, exportMetadataMap, "茶青收购", response);
    }


    /**
     * 茶青收购记录核销
     *
     * @param acquisitionStatusVO
     */
    @Override
    public void writeOff(AcquisitionStatusVO acquisitionStatusVO) {

        TeaGreenAcquisitionBaseDO teaGreenAcquisitionBaseDO = teaGreenAcquisitionBaseMapper.selectOne(new QueryWrapper<TeaGreenAcquisitionBaseDO>()
                .lambda().eq(TeaGreenAcquisitionBaseDO::getAcquisitionId, acquisitionStatusVO.getAcquisitionId()));

        if(teaGreenAcquisitionBaseDO == null){
            throw new TeaException("茶青收购记录不存在！！");
        }
        if(teaGreenAcquisitionBaseDO.getWriteOffStatus() != null && WriteOffStatusEnum.WRITE_Off.ordinal() == teaGreenAcquisitionBaseDO.getWriteOffStatus()){
            return;
        }
        if(PayStatusEnum.PAY.getCode().equals(teaGreenAcquisitionBaseDO.getPayStatus())){
            return;
        }

        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal amount = BigDecimal.ZERO;
        List<TeaGreenAcquisitionDO> list = this.baseMapper.selectList(new QueryWrapper<TeaGreenAcquisitionDO>().lambda()
                .eq(TeaGreenAcquisitionDO::getAcquisitionId, acquisitionStatusVO.getAcquisitionId()));
        AccountCache accountCache = userContextHelper.getUserLoginCache();
        for (TeaGreenAcquisitionDO teaGreenAcquisitionDO : list) {
            teaGreenAcquisitionDO.setWriteOffStatus(WriteOffStatusEnum.WRITE_Off.ordinal());
            //设置为已支付
            teaGreenAcquisitionDO.setPayStatus(PayStatusEnum.PAY.getCode());
            teaGreenAcquisitionDO.setWriteOffId(accountCache.getUserId());
            teaGreenAcquisitionDO.setWriteOffName(accountCache.getUserName());
            teaGreenAcquisitionDO.setWriteOffTime(new Date());
            teaGreenAcquisitionDO.setUpdateTime(new Date());
            quantity = quantity.add(teaGreenAcquisitionDO.getQuantity());
            amount = amount.add(teaGreenAcquisitionDO.getQuantity().multiply(teaGreenAcquisitionDO.getPrice()));
        }
        this.updateBatchById(list);
        teaGreenAcquisitionBaseDO.setWriteOffStatus(WriteOffStatusEnum.WRITE_Off.ordinal());
        teaGreenAcquisitionBaseDO.setWriteOffId(accountCache.getUserId());
        teaGreenAcquisitionBaseDO.setWriteOffName(accountCache.getUserName());
        teaGreenAcquisitionBaseDO.setWriteOffTime(new Date());
        //设置为已支付
        teaGreenAcquisitionBaseDO.setPayStatus(PayStatusEnum.PAY.getCode());
        teaGreenAcquisitionBaseDO.setUpdateTime(new Date());
        teaGreenAcquisitionBaseMapper.updateById(teaGreenAcquisitionBaseDO);
    }

    /**
     * 茶青收购记录作废
     * @param acquisitionStatusVO
     */
    @Override
    public void disable(AcquisitionStatusVO acquisitionStatusVO) {
        TeaGreenAcquisitionBaseDO teaGreenAcquisitionBaseDO =teaGreenAcquisitionBaseMapper.selectOne(new QueryWrapper<TeaGreenAcquisitionBaseDO>()
                .lambda().eq(TeaGreenAcquisitionBaseDO::getAcquisitionId,acquisitionStatusVO.getAcquisitionId()));

        if(teaGreenAcquisitionBaseDO == null){
            throw new TeaException("茶青收购记录不存在！！");
        }
        if(teaGreenAcquisitionBaseDO.getDisableStatus() !=null && teaGreenAcquisitionBaseDO.getDisableStatus() == DisableStatusEnum.DISABLE.ordinal()){
            return;
        }

        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal amount = BigDecimal.ZERO;
        List<TeaGreenAcquisitionDO> list = this.baseMapper.selectList(new QueryWrapper<TeaGreenAcquisitionDO>().lambda()
                .eq(TeaGreenAcquisitionDO::getAcquisitionId, acquisitionStatusVO.getAcquisitionId()));
        for (TeaGreenAcquisitionDO teaGreenAcquisitionDO : list) {
            teaGreenAcquisitionDO.setDisableStatus(DisableStatusEnum.DISABLE.ordinal());
            teaGreenAcquisitionDO.setUpdateTime(new Date());
            quantity = quantity.add(teaGreenAcquisitionDO.getQuantity());
            amount = amount.add(teaGreenAcquisitionDO.getQuantity().multiply(teaGreenAcquisitionDO.getPrice()));
        }
        this.updateBatchById(list);
        teaGreenAcquisitionBaseDO.setDisableStatus(DisableStatusEnum.DISABLE.ordinal());
        teaGreenAcquisitionBaseDO.setUpdateTime(new Date());
        teaGreenAcquisitionBaseMapper.updateById(teaGreenAcquisitionBaseDO);
        //增加茶农年收购记录
      /*  TeaFarmerYearOutputDO teaFarmerYearOutputDO = new TeaFarmerYearOutputBO();
        CopyUtil.copy(teaGreenAcquisitionBaseDO,teaFarmerYearOutputDO);
        teaFarmerYearOutputDO.setQuantity(quantity);
        teaFarmerYearOutputDO.setAmount(amount);
        teaFarmerYearOutputService.updateCurrentYearOutPut(teaFarmerYearOutputDO, YesOrNoEnum.NO);
     */
        teaTaskService.teaGreenTask(teaGreenAcquisitionBaseDO.getOrganizationId(), DateUtil.format(teaGreenAcquisitionBaseDO.getCreateTime(),DateUtil.monthDatePattern));
    }

    /**
     * 批量提交茶青收购记录
     * @param model
     */
    @Override
    public TeaGreenAcquisitionVO batchRecord(TeaGreenAcquisitionAddModel model) {
//        TODO::新增筐数字段
        if (model == null) {
            return null;
        }
        TeaGreenAcquisitionBO teaGreenAcquisitionBO = CopyUtil.copy(model, new TeaGreenAcquisitionBO());
        return teaGreenAcquisitionBaseService.addAcquisition(teaGreenAcquisitionBO);
    }


    /**
     * 查询单个详情的信息
     * @param acquisitionId
     * @return
     */
    @Override
    public TeaGreenAcquisitionVO getAcquisition(String acquisitionId) {
        if (StringUtils.isEmpty(acquisitionId)) {
            return null;
        }
        TeaGreenAcquisitionBaseDO teaGreenAcquisitionBaseDO = teaGreenAcquisitionBaseMapper.selectOne(new QueryWrapper<TeaGreenAcquisitionBaseDO>()
                .lambda().eq(TeaGreenAcquisitionBaseDO::getAcquisitionId, acquisitionId));
        if (teaGreenAcquisitionBaseDO == null) {
            return null;
        }
        TeaGreenAcquisitionVO teaGreenAcquisitionVO = new TeaGreenAcquisitionVO();
        CopyUtil.copy(teaGreenAcquisitionBaseDO, teaGreenAcquisitionVO);
        List<String> imgList = new ArrayList<>();
        imgList.add(teaGreenAcquisitionBaseDO.getAcquisitionImgs());
        teaGreenAcquisitionVO.setAcquisitionImgs(imgList);
        teaGreenAcquisitionVO.setTotalAmount(new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP));
        List<TeaGreenAcquisitionDO> list = this.baseMapper.selectList(new QueryWrapper<TeaGreenAcquisitionDO>().lambda().eq(TeaGreenAcquisitionDO::getAcquisitionId, acquisitionId));
        teaGreenAcquisitionVO.setAcquisitionProducts(CopyUtil.copyList(list, TeaGreenAcquisitionProductAddModel::new, ((src, target) -> {
            target.setAmount(target.getPrice().multiply(target.getTeaGreenAcquisitionQuantity()).setScale(2, BigDecimal.ROUND_HALF_UP));
            teaGreenAcquisitionVO.setTotalAmount(target.getAmount().add(teaGreenAcquisitionVO.getTotalAmount()));
        })));
        TeaFarmerDO teaFarmerDO = teaFarmerMapper.selectOne(new QueryWrapper<TeaFarmerDO>().lambda().eq(TeaFarmerDO::getFarmerId, teaGreenAcquisitionBaseDO.getFarmerId()));
        teaGreenAcquisitionVO.setTeaFarmer(CopyUtil.copy(teaFarmerDO, new TeaFarmerVO()));
        BeanUtils.copyProperties(teaFarmerDO, teaGreenAcquisitionVO, "id");
        return teaGreenAcquisitionVO;
    }

    @Override
    public AbstractPageService.PageResults<List<TeaGreenAcquisitionListVO>> getTeaGreenAcquisitionBareWeight(TeaGreenAcquisitionSearchModel model) {
        AbstractPageService.PageResults<List<TeaGreenAcquisitionListVO>> teaGreenAcquisition = getTeaGreenAcquisition(model);
        QueryWrapper<TeaGreenAcquisitionDO> queryWrapper = AdvancedSearchUtil.getQueryWrapper(AdvancedSearchUtil.getAdvancedSearch(model));
        queryWrapper.groupBy("transaction_no");
        queryWrapper.last(" limit 0," + model.getPageSize());
        List<TeaGreenAcquisitionDO> list = list(queryWrapper);
        teaGreenAcquisition.getList().forEach(teaGreenAcquisitionListVO -> {
            Long id = teaGreenAcquisitionListVO.getId();
            TeaGreenAcquisitionDO teaGreenAcquisitionDO = list.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
            if (teaGreenAcquisitionDO != null) {
                teaGreenAcquisitionListVO.setNeedShow(0);
            }
            BigDecimal bareWeight = teaGreenAcquisitionListVO.getBareWeight() == null ? new BigDecimal("0.00") : teaGreenAcquisitionListVO.getBareWeight();
            BigDecimal quantity = teaGreenAcquisitionListVO.getQuantity();
            teaGreenAcquisitionListVO.setTeaGreenAcquisitionQuantity(quantity.subtract(bareWeight).setScale(2, BigDecimal.ROUND_HALF_UP));
            teaGreenAcquisitionListVO.setAmount(teaGreenAcquisitionListVO.getTeaGreenAcquisitionQuantity().multiply(teaGreenAcquisitionListVO.getPrice()).setScale(2, BigDecimal.ROUND_HALF_UP));
        });
        return teaGreenAcquisition;
    }

    @Override
    public TeaGreenAcquisitionVO saveBareWeight(TeaGreenAcquisitionAddModel model) {
        if (model == null) {
            return null;
        }
        TeaGreenAcquisitionBO teaGreenAcquisitionBO = CopyUtil.copy(model, new TeaGreenAcquisitionBO());
        teaGreenAcquisitionBO.getAcquisitionProducts().forEach(teaGreenAcquisitionProductAddModel -> {
            if (teaGreenAcquisitionProductAddModel.getBareWeight() == null) {
                teaGreenAcquisitionProductAddModel.setBareWeight(new BigDecimal("0.00"));
            }
        });
        return teaGreenAcquisitionBaseService.updateAcquisition(teaGreenAcquisitionBO);
    }

    @Override
    public void exportBareWeight(TeaGreenAcquisitionExportDTO daoSearch, HttpServletResponse response) throws ExcelException {
        List<String> idList = daoSearch.getIdList();
        List list;
        // idList为空导出全部，不为空导出指定数据
        if (CollectionUtils.isEmpty(idList)) {
            daoSearch.setCurrent(1);
            daoSearch.setPageSize(9999);
            TeaGreenAcquisitionSearchModel advancedDaoSearch = new TeaGreenAcquisitionSearchModel();
            BeanUtils.copyProperties(daoSearch, advancedDaoSearch);
            if (StringUtils.isBlank(advancedDaoSearch.getAcquisitionEndTime())) {
                advancedDaoSearch.setAcquisitionEndTime(null);
            }
            if (StringUtils.isBlank(advancedDaoSearch.getAcquisitionStartTime())) {
                advancedDaoSearch.setAcquisitionStartTime(null);
            }
            AbstractPageService.PageResults<List<TeaGreenAcquisitionListVO>> pageResults = this.getTeaGreenAcquisitionBareWeight(advancedDaoSearch);
            list = pageResults.getList();
        } else {
            List<Long> ids = commonUtil.doIdList(idList);
            TeaGreenAcquisitionSearchModel advancedDaoSearch = new TeaGreenAcquisitionSearchModel();
            daoSearch.setCurrent(1);
            daoSearch.setPageSize(9999);
            advancedDaoSearch.setIdList(ids);
            AbstractPageService.PageResults<List<TeaGreenAcquisitionListVO>> pageResults = this.getTeaGreenAcquisitionBareWeight(advancedDaoSearch);
            list = pageResults.getList();
        }
        String exportMetadata = daoSearch.getExportMetadata();
        Map<String, String> exportMetadataMap;
        if (StringUtils.isBlank(exportMetadata)) {
            exportMetadataMap = excelUtils2.initExportMetadataMap(TeaGreenAcquisitionBareWeightExportVO.class);
        } else {
            exportMetadataMap = daoSearch.exportMetadataToMap();
        }
        List<TeaGreenAcquisitionBareWeightExportVO> exportVOList = new ArrayList<>();
        list.forEach(e -> {
            TeaGreenAcquisitionListVO teaGreenAcquisitionListVO = (TeaGreenAcquisitionListVO) e;
            TeaGreenAcquisitionBareWeightExportVO teaGreenAcquisitionExportVO = new TeaGreenAcquisitionBareWeightExportVO();
            BeanUtils.copyProperties(teaGreenAcquisitionListVO, teaGreenAcquisitionExportVO);
            teaGreenAcquisitionExportVO.setDisableStatus(DisableStatusEnum.DISABLE.ordinal() == teaGreenAcquisitionListVO.getDisableStatus() ? DisableStatusEnum.DISABLE.getDesc() : DisableStatusEnum.UN_DISABLE.getDesc());
            exportVOList.add(teaGreenAcquisitionExportVO);
        });
        ExcelUtils.listToExcel(exportVOList, exportMetadataMap, "茶青皮重", response);
    }

    @Override
    public void exportQuantity(TeaGreenAcquisitionExportDTO daoSearch, HttpServletResponse response) throws ExcelException {
        List<String> idList = daoSearch.getIdList();
        List list;
        // idList为空导出全部，不为空导出指定数据
        if (CollectionUtils.isEmpty(idList)) {
            daoSearch.setCurrent(1);
            daoSearch.setPageSize(9999);
            TeaGreenAcquisitionSearchModel advancedDaoSearch = new TeaGreenAcquisitionSearchModel();
            BeanUtils.copyProperties(daoSearch, advancedDaoSearch);
            if (StringUtils.isBlank(advancedDaoSearch.getAcquisitionEndTime())) {
                advancedDaoSearch.setAcquisitionEndTime(null);
            }
            if (StringUtils.isBlank(advancedDaoSearch.getAcquisitionStartTime())) {
                advancedDaoSearch.setAcquisitionStartTime(null);
            }
            AbstractPageService.PageResults<List<TeaGreenAcquisitionListVO>> pageResults = this.getTeaGreenAcquisition(advancedDaoSearch);
            list = pageResults.getList();
        } else {
            List<Long> ids = commonUtil.doIdList(idList);
            TeaGreenAcquisitionSearchModel advancedDaoSearch = new TeaGreenAcquisitionSearchModel();
            daoSearch.setCurrent(1);
            daoSearch.setPageSize(9999);
            advancedDaoSearch.setIdList(ids);
            AbstractPageService.PageResults<List<TeaGreenAcquisitionListVO>> pageResults = this.getTeaGreenAcquisition(advancedDaoSearch);
            list = pageResults.getList();
        }
        String exportMetadata = daoSearch.getExportMetadata();
        Map<String, String> exportMetadataMap;
        if (StringUtils.isBlank(exportMetadata)) {
            exportMetadataMap = excelUtils2.initExportMetadataMap(TeaGreenAcquisitionQuantityExportVO.class);
        } else {
            exportMetadataMap = daoSearch.exportMetadataToMap();
        }
        List<TeaGreenAcquisitionQuantityExportVO> exportVOList = new ArrayList<>();
        list.forEach(e -> {
            TeaGreenAcquisitionListVO teaGreenAcquisitionListVO = (TeaGreenAcquisitionListVO) e;
            TeaGreenAcquisitionQuantityExportVO teaGreenAcquisitionExportVO = new TeaGreenAcquisitionQuantityExportVO();
            BeanUtils.copyProperties(teaGreenAcquisitionListVO, teaGreenAcquisitionExportVO);
            teaGreenAcquisitionExportVO.setDisableStatus(DisableStatusEnum.DISABLE.ordinal() == teaGreenAcquisitionListVO.getDisableStatus() ? DisableStatusEnum.DISABLE.getDesc() : DisableStatusEnum.UN_DISABLE.getDesc());
            exportVOList.add(teaGreenAcquisitionExportVO);
        });
        ExcelUtils.listToExcel(exportVOList, exportMetadataMap, "茶青总重", response);
    }

    @Override
    public AbstractPageService.PageResults<List<TeaGreenAcquisitionVO>> getTeaGreenAcquisitionMobile(TeaGreenAcquisitionSearchModel teaGreenAcquisitionSearchModel) {
        teaGreenAcquisitionSearchModel.setGeneralParam(new String[]{"farmer_name"});

        List<QuerySpecification> querySpecifications = authorizationCodeUtil.getQueryWrapper(AuthorizationCode.TEA_ACQUISITION_RECORD);
        if (!CollectionUtils.isEmpty(querySpecifications)) {
            teaGreenAcquisitionSearchModel.getAndConditions().addAll(querySpecifications);
        }
        AbstractPageService.PageResults<List<TeaGreenAcquisitionVO>> pageResults = AdvancedSearchUtil.selectPage(teaGreenAcquisitionSearchModel, teaGreenAcquisitionBaseService.getBaseMapper(), TeaGreenAcquisitionVO.class);
        List<TeaGreenAcquisitionDO> teaGreenAcquisitionDOList = this.baseMapper.selectList(new QueryWrapper<>());
        pageResults.getList().forEach(teaGreenAcquisitionVO -> {
            teaGreenAcquisitionVO.setTotalAmount(new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP));
            List<TeaGreenAcquisitionDO> list = teaGreenAcquisitionDOList.stream().filter(teaGreenAcquisitionDO -> teaGreenAcquisitionVO.getAcquisitionId().equals(teaGreenAcquisitionDO.getAcquisitionId())).collect(Collectors.toList());
            teaGreenAcquisitionVO.setAcquisitionProducts(CopyUtil.copyList(list, TeaGreenAcquisitionProductAddModel::new, ((src, target) -> {
                target.setAmount(target.getPrice().multiply(target.getTeaGreenAcquisitionQuantity()).setScale(2, BigDecimal.ROUND_HALF_UP));
                teaGreenAcquisitionVO.setTotalAmount(target.getAmount().add(teaGreenAcquisitionVO.getTotalAmount()));
            })));
            TeaFarmerDO teaFarmerDO = teaFarmerMapper.selectOne(new QueryWrapper<TeaFarmerDO>().lambda().eq(TeaFarmerDO::getFarmerId, teaGreenAcquisitionVO.getFarmerId()));
//            teaGreenAcquisitionVO.setTeaFarmer(CopyUtil.copy(teaFarmerDO, new TeaFarmerVO()));
            teaGreenAcquisitionVO.setContactNumber(teaFarmerDO.getContactNumber());
            TeaGreenAcquisitionBaseDO teaGreenAcquisitionBaseDO = teaGreenAcquisitionBaseMapper.selectOne(new QueryWrapper<TeaGreenAcquisitionBaseDO>()
                    .lambda().eq(TeaGreenAcquisitionBaseDO::getAcquisitionId, teaGreenAcquisitionVO.getAcquisitionId()));
            List<String> imgs = new ArrayList<>();
            imgs.add(teaGreenAcquisitionBaseDO.getAcquisitionImgs());
            teaGreenAcquisitionVO.setAcquisitionImgs(imgs);
        });
        return pageResults;
    }

    @Override
    public void updateTeaFarmerByCooperativeId(CooperativeDO cooperativeDO) {
        UpdateWrapper updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("cooperative_id", cooperativeDO.getCooperativeId());
        updateWrapper.set("cooperative_name", cooperativeDO.getCooperativeName());
        update(updateWrapper);
        teaGreenAcquisitionBaseService.update(updateWrapper);
    }

}
