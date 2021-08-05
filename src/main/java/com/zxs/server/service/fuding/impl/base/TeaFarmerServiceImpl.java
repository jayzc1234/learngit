package com.zxs.server.service.fuding.impl.base;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.advancedsearch.common.AdvancedSearchUtil;
import com.jgw.supercodeplatform.advancedsearch.common.OrderByOperatorObject;
import com.jgw.supercodeplatform.advancedsearch.common.QuerySpecification;
import com.jgw.supercodeplatform.advancedsearch.enums.OrderByOperatorEnum;
import com.jgw.supercodeplatform.common.AbstractPageService;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.bo.fuding.TeaFarmerBO;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.enums.fuding.AuthorizationCode;
import net.app315.hydra.intelligent.planting.enums.fuding.YesOrNoEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.exception.gugeng.TeaException;
import net.app315.hydra.intelligent.planting.exception.gugeng.base.ExcelException;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.TeaFarmerDO;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.base.TeaFarmerMapper;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.ICooperativeService;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.ITeaFarmerService;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.ITeaFarmerYearOutputService;
import net.app315.hydra.intelligent.planting.server.service.fuding.common.ISynchronizeDataService;
import net.app315.hydra.intelligent.planting.server.util.ExcelUtils2;
import net.app315.hydra.intelligent.planting.utils.fuding.AreaUtils;
import net.app315.hydra.intelligent.planting.utils.fuding.AreaVO;
import net.app315.hydra.intelligent.planting.utils.fuding.AuthorizationCodeUtil;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.base.*;
import net.app315.hydra.user.sdk.provide.context.UserContextHelper;
import net.app315.hydra.user.sdk.provide.model.AccountCache;
import net.app315.hydra.user.sdk.provide.model.EmployeeCache;
import net.app315.nail.common.utils.UUIDUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * 茶农 服务实现类
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@Service
@Slf4j
public class TeaFarmerServiceImpl extends ServiceImpl<TeaFarmerMapper, TeaFarmerDO> implements ITeaFarmerService {

    @Autowired
    private UserContextHelper userContextHelper;
    @Autowired
    private AreaUtils areaUtil;
    @Autowired
    private AuthorizationCodeUtil authorizationCodeUtil;
    @Autowired
    private ISynchronizeDataService iSynchronizeDataService;
    @Autowired
    private ITeaFarmerYearOutputService teaFarmerYearOutputService;
    private static Long yearWarnLimit = 9999999999L;
    @Autowired
    private ExcelUtils2 excelUtils2;
    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ICooperativeService cooperativeService;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    /**
     * 修改茶农状态
     */
    @Override
    public void updateTeaFarmerStatus(String farmerId) {
        TeaFarmerDO teaFarmerDO = getOne(new QueryWrapper<TeaFarmerDO>().lambda().eq(TeaFarmerDO::getFarmerId, farmerId)
                .eq(TeaFarmerDO::getOrganizationId, userContextHelper.getOrganizationId()));
        if (teaFarmerDO == null) {
            return;
        }
        if (YesOrNoEnum.NO.ordinal() == teaFarmerDO.getStatus()) {
            teaFarmerDO.setStatus(YesOrNoEnum.YES.ordinal());
        } else {
            teaFarmerDO.setStatus(YesOrNoEnum.NO.ordinal());
        }
        teaFarmerDO.setUpdateTime(new Date());
        updateById(teaFarmerDO);
    }
    private void checkTeaFarmer(TeaFarmerBO teaFarmerBO) {
        TeaFarmerDO one = getOne(new QueryWrapper<TeaFarmerDO>().lambda()
                .ne(teaFarmerBO.getId()!=null,TeaFarmerDO::getId,
                        teaFarmerBO.getId())
                .eq(TeaFarmerDO::getOrganizationId, commonUtil.getOrganizationId())
                .eq(TeaFarmerDO::getFarmerName, teaFarmerBO.getFarmerName())
                .eq(TeaFarmerDO::getContactNumber, teaFarmerBO.getContactNumber()).last(" limit 0,1"));
        CustomAssert.notNull(one,"当前农户["+teaFarmerBO.getFarmerName()+"]已存在，手机号是"+teaFarmerBO.getContactNumber());
    }
    /**
     * 添加茶农
     * @param teaFarmerBO
     * @return
     */
    @Override
    public String addTeaFarmer(TeaFarmerBO teaFarmerBO) {
        checkTeaFarmer(teaFarmerBO);
        //处理省市区
        if(StringUtils.isNotEmpty(teaFarmerBO.getAreaCode())){
            AreaVO areaVO = areaUtil.getAllAreaByCode(teaFarmerBO.getAreaCode());
            if(areaVO !=null){
                teaFarmerBO.setCity(areaVO.getCity());
                teaFarmerBO.setCityName(areaVO.getCityName());
                teaFarmerBO.setCounty(areaVO.getCounty());
                teaFarmerBO.setCountyName(areaVO.getCountyName());
                teaFarmerBO.setProvince(areaVO.getProvince());
                teaFarmerBO.setProvinceName(areaVO.getProvinceName());
                teaFarmerBO.setTownShipCode(areaVO.getStreet());
                teaFarmerBO.setTownShipName(areaVO.getStreetName());
            }
        }
        TeaFarmerDO teaFarmerDO = CopyUtil.copy(teaFarmerBO,new TeaFarmerDO());
        AccountCache accountCache = userContextHelper.getUserLoginCache();
        teaFarmerDO.setOperator(accountCache.getUserId());
        teaFarmerDO.setOperatorName(accountCache.getUserName());
        teaFarmerDO.setOrganizationId(accountCache.getOrganizationCache().getOrganizationId());
        teaFarmerDO.setOrganizationName(accountCache.getOrganizationCache().getOrganizationFullName());
        teaFarmerDO.setFarmerId(UUIDUtil.getUUID());
        //设置分数
        teaFarmerDO.setFraction(100);
        EmployeeCache department=userContextHelper.getOrganization().getEmployeeCache();
        if(department !=null){
            teaFarmerDO.setDepartmentId(department.getDepartmentId());
            teaFarmerDO.setDepartmentName(department.getDepartmentName());
        }
    /*    if(teaFarmerDO.getAmountWarn() !=null && teaFarmerDO.getAmountWarn().compareTo(BigDecimal.valueOf(yearWarnLimit)) > 0 ){
            throw new TeaException("年产量限制超出限制：" + yearWarnLimit);
        }*/
        save(teaFarmerDO);

        teaFarmerYearOutputService.addCurrentYearOutPut(teaFarmerDO);
        taskExecutor.execute(() -> {
            try {
                cooperativeService.calculatorAreaAndQuantity();
            } catch (Exception e) {
                log.error("calculatorAreaAndQuantity: 计算茶园面积和茶青数量失败" + e);
                e.printStackTrace();
            }
        });
        return teaFarmerDO.getFarmerId();
    }

    @Override
    public void batchAddTeaFarmer(TeaFarmerBatchListModel model) {
        if(model == null){
            return ;
        }
        List<TeaFarmerAddModel> batchRecords = model.getBatchRecords();
        if(CollectionUtils.isEmpty(batchRecords)){
            return;
        }
        List<String> errorMsgs = new ArrayList<>();
        batchRecords.forEach(teaFarmerAddModel -> {
            try {
                TeaFarmerBO teaFarmerBO = CopyUtil.copy(teaFarmerAddModel,new TeaFarmerBO());
                addTeaFarmer(teaFarmerBO);
            }catch (Exception e){
                errorMsgs.add(e.getMessage());
            }
        });
        if(!CollectionUtils.isEmpty(errorMsgs)){
            throw new TeaException(JSONObject.toJSONString(errorMsgs));
        }
    }

    /**
     * 修改茶农信息
     * @param teaFarmerBO
     */
    @Override
    public void updateTeaFarmer(TeaFarmerBO teaFarmerBO) {
        checkTeaFarmer(teaFarmerBO);
        //处理省市区
        if(StringUtils.isNotEmpty(teaFarmerBO.getAreaCode())){
            AreaVO areaVO = areaUtil.getAllAreaByCode(teaFarmerBO.getAreaCode());
            if(areaVO !=null){
                teaFarmerBO.setCity(areaVO.getCity());
                teaFarmerBO.setCityName(areaVO.getCityName());
                teaFarmerBO.setCounty(areaVO.getCounty());
                teaFarmerBO.setCountyName(areaVO.getCountyName());
                teaFarmerBO.setProvince(areaVO.getProvince());
                teaFarmerBO.setProvinceName(areaVO.getProvinceName());
                teaFarmerBO.setTownShipCode(areaVO.getStreet());
                teaFarmerBO.setTownShipName(areaVO.getStreetName());
            }
        }
        TeaFarmerDO oldTeaFarmerDO = getOne(new QueryWrapper<TeaFarmerDO>().lambda().eq(TeaFarmerDO::getFarmerId,teaFarmerBO.getFarmerId()));
        // 旧的年产量
//        BigDecimal oldAmountWarn = oldTeaFarmerDO.getAmountWarn();
        TeaFarmerDO teaFarmerDO = CopyUtil.copy(teaFarmerBO,new TeaFarmerDO());
        teaFarmerDO.setUpdateTime(new Date());
        if(null == teaFarmerDO.getCity()){
            teaFarmerDO.setCity("");
            teaFarmerDO.setCityName("");
        }
        if(null == teaFarmerDO.getCounty()){
            teaFarmerDO.setCounty("");
            teaFarmerDO.setCountyName("");
        }
        if(null == teaFarmerDO.getTownShipCode()){
            teaFarmerDO.setTownShipCode("");
            teaFarmerDO.setTownShipName("");
        }
        if(null == teaFarmerDO.getProvince()){
            teaFarmerDO.setProvince("");
            teaFarmerDO.setProvinceName("");
        }

        updateById(teaFarmerDO);
        if(!oldTeaFarmerDO.getFarmerName().equals(teaFarmerBO.getFarmerName())){
            iSynchronizeDataService.synchronizeTeaFarmerInfo(teaFarmerDO);
        }

        taskExecutor.execute(() -> {
            try {
                cooperativeService.calculatorAreaAndQuantity();
            } catch (Exception e) {
                log.error("calculatorAreaAndQuantity: 计算茶园面积和茶青数量失败" + e);
                e.printStackTrace();
            }
        });
        /*if(oldAmountWarn == null || !oldAmountWarn.equals(teaFarmerDO.getAmountWarn())){
            if(teaFarmerDO.getAmountWarn().compareTo(BigDecimal.valueOf(yearWarnLimit)) > 0 ){
                throw new TeaException("年产量限制超出限制：" + yearWarnLimit);
            }
            teaFarmerYearOutputService.updateOrAddTeaFarmerYearOutputWarnValue(teaFarmerDO.getCooperativeId(),teaFarmerDO.getFarmerId(),teaFarmerDO.getAmountWarn());
        }*/

    }

    /**
     * 获取茶农信息
     * @param farmerId
     * @return
     */
    @Override
    public TeaFarmerBO getTeaFarmer(String farmerId) {
        TeaFarmerDO teaFarmerDO = getOne(new QueryWrapper<TeaFarmerDO>().lambda().eq(TeaFarmerDO::getFarmerId,farmerId)
        .eq(TeaFarmerDO::getOrganizationId,commonUtil.getOrganizationId())
        .eq(TeaFarmerDO::getStatus,0));
        if(teaFarmerDO == null){
            throw new TeaException("查询茶农信息不存在");
        }
        return CopyUtil.copy(teaFarmerDO,new TeaFarmerBO(),((src, target) -> {
            if(StringUtils.isNotEmpty(src.getTownShipCode())){
                target.setAreaCode(src.getTownShipCode());
            }else if(StringUtils.isNotEmpty(src.getCounty())){
                target.setAreaCode(src.getCounty());
            }else if(StringUtils.isNotEmpty(src.getCity())){
                target.setAreaCode(src.getCity());
            }else if(StringUtils.isNotEmpty(src.getProvince())){
                target.setAreaCode(src.getProvince());
            }
        }));
    }

    /**
     * 获取茶农列表
     * @param model
     * @return
     */
    @Override
    public AbstractPageService.PageResults<List<TeaFarmerListVO>> getTeaFarmerList(TeaFarmerSearchModel model) {
        model.setGeneralParam(new String[]{"farmer_name", "cooperative_name","base_name"});
        model.getOrderBy().add(OrderByOperatorObject.builder().field("create_time").operator(OrderByOperatorEnum.DESC).build());
        List<QuerySpecification> andConditions = new ArrayList<>();
        if(StringUtils.isNotBlank(model.getFarmerName())){
            andConditions.add(new QuerySpecification("farmer_name",model.getFarmerName(),"like"));
            model.setFarmerName(null);
        }
        if(StringUtils.isNotBlank(model.getIdCard())){
            andConditions.add(new QuerySpecification("id_card",model.getIdCard(),"like"));
            model.setIdCard(null);
        }
        if(StringUtils.isNotBlank(model.getCooperativeName())){
            andConditions.add(new QuerySpecification("cooperative_name",model.getCooperativeName(),"like"));
            model.setCooperativeName(null);
        }
        model.getAndConditions().addAll(andConditions);
        List<QuerySpecification> querySpecifications = authorizationCodeUtil.getQueryWrapper(AuthorizationCode.TEA_FARMERS_MANAGE);
        if(!CollectionUtils.isEmpty(querySpecifications)){
            model.getAndConditions().addAll(querySpecifications);
        }
        return AdvancedSearchUtil.selectPage(model, this.getBaseMapper(), TeaFarmerListVO.class);
    }


    @Override
    public AbstractPageService.PageResults<List<TeaFarmerListVO>> getTeaFarmerListByCooperativeId(TeaFarmerSearchModel model) {
        model.setOrganizationId(userContextHelper.getOrganizationId());
        model.setGeneralParam(new String[]{"farmer_name"});
        return AdvancedSearchUtil.selectPage(model, getBaseMapper(), TeaFarmerListVO.class);
    }

    @Override
    public List<CooperativeDO> calculatorAreaAndQuantity() {
        return baseMapper.calculatorAreaAndQuantity();
    }

    @Override
    public void updateTeaFarmerByCooperativeId(CooperativeDO cooperativeDO) {
        UpdateWrapper<TeaFarmerDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("cooperative_id",cooperativeDO.getCooperativeId());
        updateWrapper.set("cooperative_name",cooperativeDO.getCooperativeName());
        baseMapper.update(null,updateWrapper);
    }

    @Override
    public void addTeaFarmerFraction(String farmerId, Integer fraction) {
        this.getBaseMapper().addTeaFarmerFraction(farmerId,fraction);
    }

    /**
     * 导出茶农
     *
     * @param
     * @param response
     */
    @Override
    public void export(TeaFarmerExportDTO daoSearch, HttpServletResponse response) throws ExcelException {
        List<String> idList = daoSearch.getIdList();
        List list ;
        // idList为空导出全部，不为空导出指定数据
        if (CollectionUtils.isEmpty(idList)) {
            daoSearch.setCurrent(1);
            daoSearch.setPageSize(9999);
            TeaFarmerSearchModel advancedDaoSearch = new TeaFarmerSearchModel();
            BeanUtils.copyProperties(daoSearch,advancedDaoSearch);
            AbstractPageService.PageResults<List<TeaFarmerListVO>> pageResults = this.getTeaFarmerList(advancedDaoSearch);
            list = pageResults.getList();
        } else {
            List<Long> ids = commonUtil.doIdList(idList);
            TeaFarmerSearchModel advancedDaoSearch = new TeaFarmerSearchModel();
            daoSearch.setCurrent(1);
            daoSearch.setPageSize(9999);
            advancedDaoSearch.setIdList(ids);
            AbstractPageService.PageResults<List<TeaFarmerListVO>> pageResults = this.getTeaFarmerList(advancedDaoSearch);
            list = pageResults.getList();
        }
        String exportMetadata = daoSearch.getExportMetadata();
        Map<String, String> exportMetadataMap ;
        if (StringUtils.isBlank(exportMetadata)){
            exportMetadataMap= excelUtils2.initExportMetadataMap(TeaFarmerExportVO.class);
        }else {
            exportMetadataMap=daoSearch.exportMetadataToMap();
        }
        List<TeaFarmerExportVO> exportVOList=new ArrayList<>();
        list.forEach(e -> {
            TeaFarmerListVO teaFarmerListVO = (TeaFarmerListVO) e;
            TeaFarmerExportVO teaFarmerExportVO = new TeaFarmerExportVO();
            BeanUtils.copyProperties(teaFarmerListVO, teaFarmerExportVO);
            teaFarmerExportVO.setAddress(teaFarmerListVO.getProvinceName()+teaFarmerListVO.getCityName()+teaFarmerListVO.getCountyName()+teaFarmerListVO.getTownShipName()+teaFarmerExportVO.getAddress());
            teaFarmerExportVO.setStatus(DeleteOrNotEnum.NOT_DELETED.getKey() ==
                    teaFarmerListVO.getStatus() ?
                    DeleteOrNotEnum.NOT_DELETED.getValue() : DeleteOrNotEnum.DELETED.getValue());
            exportVOList.add(teaFarmerExportVO);
        });
        ExcelUtils.listToExcel(exportVOList, exportMetadataMap, "茶农管理", response);
    }

    @Override
    public void importExcel(InputStream file)  {
        List<TeaFarmerImportVO> batchRecodes = new ArrayList<>();
        String orgId = userContextHelper.getOrganizationId();
        EasyExcel.read(file, TeaFarmerImportVO.class, new AnalysisEventListener<TeaFarmerImportVO>() {
            @Override
            public void invoke(TeaFarmerImportVO teaFarmerImportVO, AnalysisContext analysisContext) {
                if(StringUtils.isEmpty(teaFarmerImportVO.getFarmerId())){
                    return;
                }
                batchRecodes.add(teaFarmerImportVO);
                if(batchRecodes.size() >= 1000){
                    ArrayList<TeaFarmerImportVO> teaFarmerImportVOS = new ArrayList<>(batchRecodes);
                    updateFarmer(teaFarmerImportVOS,orgId);
                    batchRecodes.clear();
                }
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).doReadAll();
        if(!CollectionUtils.isEmpty(batchRecodes)){
            log.info("最后不足1000的数据-开始异步修改茶农数据");
            updateFarmer(batchRecodes,orgId);
        }
        taskExecutor.execute(() -> {
            try {
                cooperativeService.calculatorAreaAndQuantity();
            } catch (Exception e) {
                log.error("calculatorAreaAndQuantity: 计算茶园面积和茶青数量失败" + e);
                e.printStackTrace();
            }
        });
    }

    /**
     * 异步导入数据
     * @param batchRecodes 导入的数据
     * @param orgId 组织id
     */
    private void updateFarmer(List<TeaFarmerImportVO> batchRecodes, String orgId){
        if(CollectionUtils.isEmpty(batchRecodes)){
            return;
        }
        List<TeaFarmerDO> realRecodes = new ArrayList<>();
        CompletableFuture.runAsync(()->{
            for (TeaFarmerImportVO batchRecode : batchRecodes) {
                TeaFarmerDO teaFarmerDO = getOne(new QueryWrapper<TeaFarmerDO>().lambda()
                        .eq(TeaFarmerDO::getFarmerId,batchRecode.getFarmerId())
                        .eq(TeaFarmerDO::getOrganizationId,orgId));
                if (teaFarmerDO == null) {
                    break;
                }
                if(batchRecode.getAmountWarn() == null){
                    break;
                }
                if(batchRecode.getAmountWarn().compareTo(BigDecimal.valueOf(yearWarnLimit)) > 0 ){
                    break;
                }
                //判断是否修改年产量
                BigDecimal oldAmountWarn = teaFarmerDO.getAmountWarn();
                if(batchRecode.getAmountWarn().equals(oldAmountWarn)){
                    break;
                }
                teaFarmerDO.setAmountWarn(batchRecode.getAmountWarn());
                teaFarmerDO.setUpdateTime(new Date());
                realRecodes.add(teaFarmerDO);
                try {
                    teaFarmerYearOutputService.updateOrAddTeaFarmerYearOutputWarnValue(teaFarmerDO.getCooperativeId(),teaFarmerDO.getFarmerId(),batchRecode.getAmountWarn());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if(!CollectionUtils.isEmpty(realRecodes)){
                this.updateBatchById(realRecodes);
            }
        }).exceptionally((e) -> {
            log.error("开始通过导入数据修改茶农的信息失败", e);
            return null;
        });
    }
}
