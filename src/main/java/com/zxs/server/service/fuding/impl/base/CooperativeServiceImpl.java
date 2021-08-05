package com.zxs.server.service.fuding.impl.base;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.advancedsearch.common.AdvancedSearchUtil;
import com.jgw.supercodeplatform.advancedsearch.common.OrderByOperatorObject;
import com.jgw.supercodeplatform.advancedsearch.common.QuerySpecification;
import com.jgw.supercodeplatform.advancedsearch.enums.OrderByOperatorEnum;
import com.jgw.supercodeplatform.common.AbstractPageService;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.bo.fuding.CooperativeBO;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.enums.fuding.AuthorizationCode;
import net.app315.hydra.intelligent.planting.enums.fuding.IndexNoEnum;
import net.app315.hydra.intelligent.planting.enums.fuding.YesOrNoEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.exception.gugeng.TeaException;
import net.app315.hydra.intelligent.planting.exception.gugeng.base.ExcelException;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeDO;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.base.CooperativeMapper;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.ICooperativeService;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.ICooperativeYearOutputService;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.ITeaFarmerService;
import net.app315.hydra.intelligent.planting.server.service.fuding.common.ISynchronizeDataService;
import net.app315.hydra.intelligent.planting.server.service.fuding.teagreen.ITeaGreenAcquisitionService;
import net.app315.hydra.intelligent.planting.server.util.ExcelUtils2;
import net.app315.hydra.intelligent.planting.utils.fuding.AreaUtils;
import net.app315.hydra.intelligent.planting.utils.fuding.AreaVO;
import net.app315.hydra.intelligent.planting.utils.fuding.AuthorizationCodeUtil;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.base.*;
import net.app315.hydra.user.sdk.provide.context.UserContextHelper;
import net.app315.hydra.user.sdk.provide.model.AccountCache;
import net.app315.hydra.user.sdk.provide.model.EmployeeCache;
import net.app315.nail.common.result.RichResult;
import net.app315.nail.common.utils.IndexNoUtil;
import net.app315.nail.common.utils.UUIDUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 合作社 服务实现类
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@Service
@Slf4j
public class CooperativeServiceImpl extends ServiceImpl<CooperativeMapper, CooperativeDO> implements ICooperativeService {
    @Autowired
    private UserContextHelper userContextHelper;
    @Autowired
    private AreaUtils areaUtil;
    @Autowired
    private AuthorizationCodeUtil authorizationCodeUtil;
    @Autowired
    private ExcelUtils2 excelUtils2;
    @Autowired
    private ISynchronizeDataService iSynchronizeDataService;
    @Autowired
    private ICooperativeYearOutputService cooperativeYearOutputService;
    @Autowired
    private ITeaFarmerService teaFarmerService;

    @Autowired
    private ITeaGreenAcquisitionService teaGreenAcquisitionService;

    @Autowired
    private CommonUtil commonUtil;

    private static final Integer SUCCESS_CODE = 200;

    private static Long yearWarnLimit = 9999999999L;

    /**
     * 添加合作社
     *
     * @param cooperativeBO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addCooperative(CooperativeBO cooperativeBO) {
        //处理省市区
        if(StringUtils.isNotEmpty(cooperativeBO.getAreaCode())){
            AreaVO areaVO = areaUtil.getAllAreaByCode(cooperativeBO.getAreaCode());
            if(areaVO !=null){
                cooperativeBO.setCity(areaVO.getCity());
                cooperativeBO.setCityName(areaVO.getCityName());
                cooperativeBO.setCounty(areaVO.getCounty());
                cooperativeBO.setCountyName(areaVO.getCountyName());
                cooperativeBO.setProvince(areaVO.getProvince());
                cooperativeBO.setProvinceName(areaVO.getProvinceName());
                cooperativeBO.setTownShipCode(areaVO.getStreet());
                cooperativeBO.setTownShipName(areaVO.getStreetName());
            }
        }
        CooperativeDO cooperativeDO = CopyUtil.copy(cooperativeBO, new CooperativeDO());
        AccountCache accountCache = userContextHelper.getUserLoginCache();
        cooperativeDO.setOperator(accountCache.getUserId());
        cooperativeDO.setOperatorName(accountCache.getUserName());
        cooperativeDO.setOrganizationId(accountCache.getOrganizationCache().getOrganizationId());
        cooperativeDO.setOrganizationName(accountCache.getOrganizationCache().getOrganizationFullName());
        EmployeeCache department=userContextHelper.getOrganization().getEmployeeCache();
        if(department!=null){
            cooperativeDO.setDepartmentId(department.getDepartmentId());
            cooperativeDO.setDepartmentName(department.getDepartmentName());
        }
        // 验证合作社编号
        if (StringUtils.isBlank(cooperativeDO.getCooperativeNo())){
            String cooperativeNo = getNo();
            cooperativeDO.setCooperativeNo(cooperativeNo);
        }
        int checkNo = count(new QueryWrapper<CooperativeDO>().lambda().eq(CooperativeDO::getCooperativeNo, cooperativeDO.getCooperativeNo())
                .eq(CooperativeDO::getOrganizationId, cooperativeDO.getOrganizationId()));
        if (checkNo > 0) {
            throw new TeaException("合作社编号:" + cooperativeDO.getCooperativeNo() + "已存在");
        }
        // 验证合作社名称
        int checkName = count(new QueryWrapper<CooperativeDO>().lambda().eq(CooperativeDO::getCooperativeName, cooperativeDO.getCooperativeName())
                .eq(CooperativeDO::getOrganizationId, cooperativeDO.getOrganizationId()));
        if (checkName > 0) {
            throw new TeaException("合作社名称:" + cooperativeDO.getCooperativeName() + "已存在");
        }
        cooperativeDO.setInrcNo(0L);
        if(cooperativeDO.getCooperativeNo().contains(IndexNoEnum.H.toString())){
            try {
                Long no = Long.parseLong(cooperativeDO.getCooperativeNo().substring(1));
                cooperativeDO.setInrcNo(no);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        cooperativeDO.setCooperativeId(UUIDUtil.getUUID());
        cooperativeDO.setStatus(YesOrNoEnum.YES.ordinal());

       /* AddDepartmentModel departmentModel = new AddDepartmentModel();
        AddDepartmentModel departmentModel = new AddDepartmentModel();
        departmentModel.setDepartmentName(cooperativeDO.getCooperativeName());
        // 部门类型：0-公司部门
        departmentModel.setDepartmentType(0);
        departmentModel.setOrgId(cooperativeDO.getOrganizationId());
        departmentModel.setOrgName(cooperativeDO.getOrganizationName());
        // 1-新增到分支机构下
        departmentModel.setAddType(1);
        departmentModel.setRemark("创建合作社自动生成的部门");
        RichResult<AddDepartmentVO> richResult = departmentService.addDepartment(departmentModel);
        if(richResult != null && SUCCESS_CODE.equals(richResult.getState())){
            AddDepartmentVO departmentVO = richResult.getResults();
            cooperativeDO.setDepartmentId(departmentVO.getDepartmentId());
            cooperativeDO.setDepartmentName(departmentVO.getDepartmentName());
        }else if(richResult != null){
            throw new TeaException(richResult.getMsg());
        }else{
            throw new TeaException("创建部门失败");
        }
        if(cooperativeDO.getAmountWarn() !=null && cooperativeDO.getAmountWarn().compareTo(BigDecimal.valueOf(yearWarnLimit)) > 0 ){
            throw new TeaException("年产量限制超出限制：" + yearWarnLimit);
        }*/
        save(cooperativeDO);
        cooperativeYearOutputService.addCurrentYearOutPut(cooperativeDO);
        return cooperativeDO.getCooperativeId();
    }

    @Override
    public RichResult batchAddCooperative(CooperativeBatchListModel model) {
        RichResult richResult = new RichResult();
        if(model == null){
            return richResult;
        }
        List<CooperativeAddModel> batchRecords = model.getBatchRecords();
        if(CollectionUtils.isEmpty(batchRecords)){
            return richResult;
        }
        List<CooperativeAddModel> errorBatchRecords = new ArrayList<>();
        List<String> errorMsgs = new ArrayList<>();
        batchRecords.forEach(cooperativeAddModel -> {
            try {
                CooperativeBO cooperativeBO = CopyUtil.copy(cooperativeAddModel,new CooperativeBO());
                addCooperative(cooperativeBO);
            }catch (Exception e){
                errorMsgs.add(e.getMessage());
                errorBatchRecords.add(cooperativeAddModel);
            }
        });
        if(!CollectionUtils.isEmpty(errorMsgs)){
            BatchAddCooperativeErrorVO batchAddCooperativeErrorVO = new BatchAddCooperativeErrorVO();
            batchAddCooperativeErrorVO.setErrorMsgs(errorMsgs);
            batchAddCooperativeErrorVO.setErrorBatchRecords(errorBatchRecords);
            richResult.setState(500);
            richResult.setMsg(null);
            richResult.setResults(batchAddCooperativeErrorVO);
        }
        return richResult;
    }

    /**
     * 修改合作社
     *
     * @param cooperativeBO
     */
    @Override
    public void updateCooperative(CooperativeBO cooperativeBO) {
        //处理省市区
        //处理省市区
        if(StringUtils.isNotEmpty(cooperativeBO.getAreaCode())){
            AreaVO areaVO = areaUtil.getAllAreaByCode(cooperativeBO.getAreaCode());
            if(areaVO !=null){
                cooperativeBO.setCity(areaVO.getCity());
                cooperativeBO.setCityName(areaVO.getCityName());
                cooperativeBO.setCounty(areaVO.getCounty());
                cooperativeBO.setCountyName(areaVO.getCountyName());
                cooperativeBO.setProvince(areaVO.getProvince());
                cooperativeBO.setProvinceName(areaVO.getProvinceName());
                cooperativeBO.setTownShipCode(areaVO.getStreet());
                cooperativeBO.setTownShipName(areaVO.getStreetName());
            }
        }
        CooperativeDO cooperativeDO = CopyUtil.copy(cooperativeBO, new CooperativeDO());
        // 验证合作社编号
        int checkNoCount = count(new QueryWrapper<CooperativeDO>().lambda()
                .eq(CooperativeDO::getCooperativeNo, cooperativeDO.getCooperativeNo())
                .eq(CooperativeDO::getOrganizationId, userContextHelper.getOrganizationId())
                .ne(CooperativeDO::getCooperativeId,cooperativeDO.getCooperativeId()));
        if (checkNoCount > 0) {
            throw new TeaException("合作社编号:" + cooperativeDO.getCooperativeNo() + "已存在");
        }
        // 验证合作社名称
        int checkNameCount = count(new QueryWrapper<CooperativeDO>().lambda()
                .eq(CooperativeDO::getCooperativeName, cooperativeDO.getCooperativeName())
                .eq(CooperativeDO::getOrganizationId, userContextHelper.getOrganizationId())
                .ne(CooperativeDO::getCooperativeId,cooperativeDO.getCooperativeId()));
        if (checkNameCount > 0) {
            throw new TeaException("合作社名称:" + cooperativeDO.getCooperativeName() + "已存在");
        }
        if(cooperativeDO.getCooperativeNo().contains(IndexNoEnum.H.toString())){
            try {
                Long no = Long.parseLong(cooperativeDO.getCooperativeNo().substring(1));
                cooperativeDO.setInrcNo(no);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        cooperativeDO.setUpdateTime(new Date());
        if(null == cooperativeDO.getCity()){
            cooperativeDO.setCity("");
            cooperativeDO.setCityName("");
        }
        if(null == cooperativeDO.getCounty()){
            cooperativeDO.setCounty("");
            cooperativeDO.setCountyName("");
        }
        if(null == cooperativeDO.getTownShipCode()){
            cooperativeDO.setTownShipCode("");
            cooperativeDO.setTownShipName("");
        }
        if(null == cooperativeDO.getProvince()){
            cooperativeDO.setProvince("");
            cooperativeDO.setProvinceName("");
        }
        /*  UpdateDepartmentModel departmentModel = new UpdateDepartmentModel();
        CooperativeDO oldCooperativeDO = getOne(new QueryWrapper<CooperativeDO>().lambda()
                .eq(CooperativeDO::getCooperativeId, cooperativeDO.getCooperativeId()));
        // 旧的年产量预警值
      BigDecimal oldAmountWarn = oldCooperativeDO.getAmountWarn();
        if(!cooperativeDO.getCooperativeName().equals(oldCooperativeDO.getCooperativeName())){
            departmentModel.setDepartmentId(oldCooperativeDO.getDepartmentId());
            departmentModel.setDepartmentName(cooperativeDO.getCooperativeName());
            RichResult<Void> richResult = departmentService.updateDepartment(departmentModel);
            if(richResult == null || !SUCCESS_CODE.equals(richResult.getState())) {
                throw new TeaException("修改部门信息失败");
            }
            iSynchronizeDataService.synchronizeCooperativeInfo(cooperativeDO);
        }*/
        cooperativeDO.setUpdateTime(new Date());
        updateById(cooperativeDO);

        teaFarmerService.updateTeaFarmerByCooperativeId(cooperativeDO);
        teaGreenAcquisitionService.updateTeaFarmerByCooperativeId(cooperativeDO);
       /* if(oldAmountWarn == null || !oldAmountWarn.equals(cooperativeDO.getAmountWarn())){
            // 修改合作社年产量记录表
            if(cooperativeDO.getAmountWarn().compareTo(BigDecimal.valueOf(yearWarnLimit)) > 0 ){
                throw new TeaException("年产量限制超出限制：" + yearWarnLimit);
            }
            cooperativeYearOutputService.updateOrAddCooperativeYearOutputWarnValue(cooperativeDO.getCooperativeId(),cooperativeDO.getAmountWarn());
        }*/
    }

    @Override
    public void updateCooperativeAmountWarn(String cooperativeId, BigDecimal warnValue) {
        if(StringUtils.isBlank(cooperativeId) || warnValue == null){
            return;
        }
        CooperativeDO cooperativeDO = getOne(new QueryWrapper<CooperativeDO>().lambda()
                .eq(CooperativeDO::getCooperativeId, cooperativeId));
        if (cooperativeDO == null) {
            return;
        }
        cooperativeDO.setAmountWarn(warnValue);
        updateById(cooperativeDO);
        // 修改合作社年产量记录表
        cooperativeYearOutputService.updateCooperativeYearOutputWarnValue(cooperativeId,warnValue);
    }

    /**
     * 获取合作社信息
     *
     * @param cooperativeId
     * @return
     */
    @Override
    public CooperativeBO getCooperative(String cooperativeId) {
        QueryWrapper<CooperativeDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("t.cooperative_id", cooperativeId);
        CooperativeBO cooperativeDO = baseMapper.getCooperativeInfo(queryWrapper);
        if (cooperativeDO == null) {
            return null;
        }

        return CopyUtil.copy(cooperativeDO, new CooperativeBO(), ((src, target) -> {
            if (StringUtils.isNotEmpty(src.getTownShipCode())) {
                target.setAreaCode(src.getTownShipCode());
            } else if (StringUtils.isNotEmpty(src.getCounty())) {
                target.setAreaCode(src.getCounty());
            } else if (StringUtils.isNotEmpty(src.getCity())) {
                target.setAreaCode(src.getCity());
            } else if (StringUtils.isNotEmpty(src.getProvince())) {
                target.setAreaCode(src.getProvince());
            }
        }));
    }

    /**
     * 获取合作分页列表
     *
     * @param model
     * @return
     */
    @Override
    public AbstractPageService.PageResults<List<CooperativeListVO>> getCooperativeList(CooperativeSearchModel model) {
        model.setGeneralParam(new String[]{"cooperative_name", "cooperative_no"});
        model.getOrderBy().add(OrderByOperatorObject.builder().field("create_time").operator(OrderByOperatorEnum.DESC).build());
        List<QuerySpecification> andConditions = new ArrayList<>();
        if(StringUtils.isNotBlank(model.getCooperativeNo())){
            andConditions.add(new QuerySpecification("cooperative_no",model.getCooperativeNo(),"like"));
            model.setCooperativeNo(null);
        }
        if(StringUtils.isNotBlank(model.getCooperativeName())){
            andConditions.add(new QuerySpecification("cooperative_name",model.getCooperativeName(),"like"));
            model.setCooperativeName(null);
        }
        model.getAndConditions().addAll(andConditions);
        List<QuerySpecification> querySpecifications = authorizationCodeUtil.getQueryWrapper(AuthorizationCode.COOPERATIVE_MANAGE);
        if(!CollectionUtils.isEmpty(querySpecifications)){
            model.getAndConditions().addAll(querySpecifications);
        }
        return AdvancedSearchUtil.selectPage(model,this.getBaseMapper(), CooperativeListVO.class);
    }


    /**
     * 根据当前登录用户获取合作分页列表
     * @param departmentId
     * @return
     */
    @Override
    public CooperativeListVO getCooperativeListByDepartmentId(String departmentId) {
        List<CooperativeDO> cooperativeDOList = this.baseMapper.selectList(new QueryWrapper<CooperativeDO>().lambda()
                .eq(CooperativeDO::getDepartmentId, departmentId));
        if(CollectionUtils.isEmpty(cooperativeDOList)){
            return null;
        }
        return CopyUtil.copy(cooperativeDOList.get(0),new CooperativeListVO());
    }

    /**
     * 修改启用禁用状态
     *
     * @param cooperativeId
     */
    @Override
    public void updateCooperativeStatus(String cooperativeId) {
        CooperativeDO cooperativeDO = getOne(new QueryWrapper<CooperativeDO>().lambda()
                .eq(CooperativeDO::getCooperativeId, cooperativeId)
                .eq(CooperativeDO::getOrganizationId, userContextHelper.getOrganizationId()));
        if (cooperativeDO == null) {
            return;
        }
        if (YesOrNoEnum.NO.ordinal() == cooperativeDO.getStatus()) {
            cooperativeDO.setStatus(YesOrNoEnum.YES.ordinal());
        } else {
            cooperativeDO.setStatus(YesOrNoEnum.NO.ordinal());
        }
        cooperativeDO.setUpdateTime(new Date());
        updateById(cooperativeDO);
    }

    /**
     * 获取合作社编号
     * @return
     */
    @Override
    public String getNo() {
        Long incrNo = this.getBaseMapper().maxIncrNo(userContextHelper.getOrganizationId());
        if (incrNo == null) {
            incrNo = 1L;
        } else {
            incrNo++;
        }
        return IndexNoUtil.getAutoNo(IndexNoEnum.H.toString(), incrNo, 3);
    }

    /**
     * 导出合作社
     * @param daoSearch
     * @param response
     */
    @Override
    public void export(CooperativeExportDTO daoSearch, HttpServletResponse response) throws ExcelException {
        List<String> idList = daoSearch.getIdList();
        List list ;
        // idList为空导出全部，不为空导出指定数据
        if (CollectionUtils.isEmpty(idList)) {
            daoSearch.setCurrent(1);
            daoSearch.setPageSize(9999);
            CooperativeSearchModel advancedDaoSearch = new CooperativeSearchModel();
            BeanUtils.copyProperties(daoSearch,advancedDaoSearch);
            AbstractPageService.PageResults<List<CooperativeListVO>> pageResults = this.getCooperativeList(advancedDaoSearch);
            list = pageResults.getList();
        } else {
            List<Long> ids = commonUtil.doIdList(idList);
            CooperativeSearchModel advancedDaoSearch = new CooperativeSearchModel();
            daoSearch.setCurrent(1);
            daoSearch.setPageSize(9999);
            advancedDaoSearch.setIdList(ids);
            AbstractPageService.PageResults<List<CooperativeListVO>> pageResults = this.getCooperativeList(advancedDaoSearch);
            list = pageResults.getList();
        }
        String exportMetadata = daoSearch.getExportMetadata();
        Map<String, String> exportMetadataMap ;
        if (StringUtils.isBlank(exportMetadata)){
            exportMetadataMap= excelUtils2.initExportMetadataMap(CooperativeExportVO.class);
        }else {
            exportMetadataMap=daoSearch.exportMetadataToMap();
        }
        List<CooperativeExportVO> exportVOList=new ArrayList<>();
        list.forEach(e -> {
            CooperativeListVO teaFarmerListVO = (CooperativeListVO) e;
            CooperativeExportVO teaFarmerExportVO = new CooperativeExportVO();
            BeanUtils.copyProperties(teaFarmerListVO, teaFarmerExportVO);
            teaFarmerExportVO.setAddress(teaFarmerListVO.getProvinceName() + teaFarmerListVO.getCityName() + teaFarmerListVO.getCountyName() + teaFarmerListVO.getTownShipName() + teaFarmerExportVO.getAddress());
            teaFarmerExportVO.setStatus(DeleteOrNotEnum.NOT_DELETED.getKey() ==
                    teaFarmerListVO.getStatus() ?
                    DeleteOrNotEnum.NOT_DELETED.getValue() : DeleteOrNotEnum.DELETED.getValue());
            exportVOList.add(teaFarmerExportVO);
        });
        ExcelUtils.listToExcel(exportVOList, exportMetadataMap, "合作社管理", response);
    }

    @Override
    public void importExcel(InputStream  file) {
       List<CooperativeDO> batchRecodes = new ArrayList<>();
       EasyExcel.read(file, CooperativeImportVO.class, new AnalysisEventListener() {
          @Override
          public void invoke(Object o, AnalysisContext analysisContext) {
              CooperativeImportVO cooperativeImportVO = new CooperativeImportVO();
              CopyUtil.copy(o,cooperativeImportVO);
              if(StringUtils.isEmpty(cooperativeImportVO.getCooperativeId())){
                  return;
              }
              CooperativeDO cooperativeDO = getOne(new QueryWrapper<CooperativeDO>().lambda().eq(CooperativeDO::getCooperativeId,cooperativeImportVO.getCooperativeId())
                      .eq(CooperativeDO::getOrganizationId,userContextHelper.getOrganizationId()));
              if (cooperativeDO == null) {
                  return;
              }
              if(cooperativeImportVO.getAmountWarn() == null){
                  return;
              }
              if(cooperativeImportVO.getAmountWarn().compareTo(BigDecimal.valueOf(yearWarnLimit)) > 0 ){
                  return;
              }

              if(!cooperativeImportVO.getAmountWarn().equals(cooperativeDO.getAmountWarn())){
                  cooperativeDO.setAmountWarn(cooperativeImportVO.getAmountWarn());
                  cooperativeDO.setUpdateTime(new Date());
                  batchRecodes.add(cooperativeDO);
              }
              if(batchRecodes.size() >= 1000){
                  updateCooperative(batchRecodes);
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
            log.info("最后不足1000的数据-开始异步修改合作社数据");
            updateCooperative(batchRecodes);
       }
    }

    @Override
    public void calculatorAreaAndQuantity() {
        List<CooperativeDO> list= teaFarmerService.calculatorAreaAndQuantity();
        list.forEach(cooperativeDO -> {
            UpdateWrapper<CooperativeDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().eq(CooperativeDO::getCooperativeId,cooperativeDO.getCooperativeId())
            .set(CooperativeDO::getTeaGardenArea,cooperativeDO.getTeaGardenArea())
            .set(CooperativeDO::getTotalTeaGreenAcquisitionQuantity,cooperativeDO.getTotalTeaGreenAcquisitionQuantity());
            baseMapper.update(null,updateWrapper);
        });
    }

    public void updateCooperative(List<CooperativeDO> batchRecodes){
        if(CollectionUtils.isEmpty(batchRecodes)){
            return;
        }
        updateBatchById(batchRecodes);
        for (CooperativeDO batchRecode : batchRecodes) {
            // 修改合作社年产量记录表
            try {
                cooperativeYearOutputService.updateOrAddCooperativeYearOutputWarnValue(batchRecode.getCooperativeId(),batchRecode.getAmountWarn());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
