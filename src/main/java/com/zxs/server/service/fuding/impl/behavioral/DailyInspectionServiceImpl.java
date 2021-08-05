package com.zxs.server.service.fuding.impl.behavioral;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.advancedsearch.common.QuerySpecification;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.bo.fuding.DailyInspectionBO;
import net.app315.hydra.intelligent.planting.bo.fuding.TeaFarmerBO;
import net.app315.hydra.intelligent.planting.constants.ExcelConstants;
import net.app315.hydra.intelligent.planting.enums.fuding.AuthorizationCode;
import net.app315.hydra.intelligent.planting.enums.fuding.IndexNoEnum;
import net.app315.hydra.intelligent.planting.enums.fuding.RedisKeyEnum;
import net.app315.hydra.intelligent.planting.enums.fuding.YesOrNoEnum;
import net.app315.hydra.intelligent.planting.pojo.fuding.behavioral.DailyInspectionDO;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.behavioral.DailyInspectionMapper;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.ITeaFarmerService;
import net.app315.hydra.intelligent.planting.server.service.fuding.behavioral.IDailyInspectionService;
import net.app315.hydra.intelligent.planting.utils.fuding.AuthorizationCodeUtil;
import net.app315.hydra.intelligent.planting.utils.fuding.CustomExcelStyleUtils;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.utils.redis.RedisUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.DailyInspectionExportVO;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.DailyInspectionListVO;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.DailyInspectionSearchModel;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.DailyInspectionVO;
import net.app315.hydra.user.sdk.provide.context.UserContextHelper;
import net.app315.hydra.user.sdk.provide.model.AccountCache;
import net.app315.hydra.user.sdk.provide.model.EmployeeCache;
import net.app315.nail.common.utils.DateUtil;
import net.app315.nail.common.utils.UUIDUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 日常巡检 服务实现类
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@Service
public class DailyInspectionServiceImpl extends ServiceImpl<DailyInspectionMapper, DailyInspectionDO> implements IDailyInspectionService {

    @Autowired
    private UserContextHelper userContextHelper;
    @Autowired
    private ITeaFarmerService teaFarmerService;
    @Autowired
    private AuthorizationCodeUtil authorizationCodeUtil;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public String addDailyInspection(DailyInspectionBO dailyInspectionBO) {
        DailyInspectionDO dailyInspectionDO = CopyUtil.copy(dailyInspectionBO, new DailyInspectionDO());
        AccountCache accountCache = userContextHelper.getUserLoginCache();
        dailyInspectionDO.setOperator(accountCache.getUserId());
        dailyInspectionDO.setOperatorName(accountCache.getUserName());
        dailyInspectionDO.setOrganizationId(accountCache.getOrganizationCache().getOrganizationId());
        dailyInspectionDO.setOrganizationName(accountCache.getOrganizationCache().getOrganizationFullName());
        dailyInspectionDO.setInspectionId(UUIDUtil.getUUID());
        EmployeeCache department=userContextHelper.getOrganization().getEmployeeCache();
        if(department!=null){
            dailyInspectionDO.setDepartmentId(department.getDepartmentId());
            dailyInspectionDO.setDepartmentName(department.getDepartmentName());
        }
        if(dailyInspectionDO.getFarmerId() != null){
            // 根据茶农获取联合体信息
            TeaFarmerBO teaFarmer = teaFarmerService.getTeaFarmer(dailyInspectionDO.getFarmerId());
            if(teaFarmer != null){
                dailyInspectionDO.setCooperativeId(teaFarmer.getCooperativeId());
                dailyInspectionDO.setCooperativeName(teaFarmer.getCooperativeName());
            }
        }
        synchronized (this){
            dailyInspectionDO.setInspectionNo(getInspectionNo(accountCache.getOrganizationCache().getOrganizationId()));
            this.save(dailyInspectionDO);
        }
        // 扣减分数
        if(dailyInspectionDO.getViolation() == YesOrNoEnum.NO.ordinal() && dailyInspectionDO.getFraction() != null){
            teaFarmerService.addTeaFarmerFraction(dailyInspectionDO.getFarmerId(), dailyInspectionDO.getFraction());
        }
        return dailyInspectionDO.getInspectionId();
    }

    @Override
    public void updateDailyInspection(DailyInspectionBO dailyInspectionBO) {
        int fraction = 0;
        // 查询原巡检记录
        DailyInspectionDO dailyInspectionDO = this.baseMapper.selectOne(new QueryWrapper<DailyInspectionDO>().lambda().eq(DailyInspectionDO::getInspectionId, dailyInspectionBO.getInspectionId()));

        //判断新的是否扣减分数
        if(dailyInspectionBO.getViolation() == YesOrNoEnum.NO.ordinal() && dailyInspectionBO.getFraction() != null) {
            fraction = fraction + dailyInspectionBO.getFraction();
        }

        // 判断原巡检记录是否扣分
        if(dailyInspectionDO.getViolation() == YesOrNoEnum.NO.ordinal() && dailyInspectionDO.getFraction() != null) {
            fraction = fraction - dailyInspectionDO.getFraction();
        }

        // 修改巡检记录
        DailyInspectionDO inspectionDO = CopyUtil.copy(dailyInspectionBO, new DailyInspectionDO());
        inspectionDO.setUpdateTime(new Date());
        this.baseMapper.updateById(inspectionDO);
        // 修改分数
        if(fraction != 0){
            teaFarmerService.addTeaFarmerFraction(dailyInspectionDO.getFarmerId(),fraction);
        }
    }

    @Override
    public void deleteDailyInspection(String inspectionId) {
        DailyInspectionDO dailyInspectionDO = this.baseMapper.selectOne(new QueryWrapper<DailyInspectionDO>().lambda().eq(DailyInspectionDO::getInspectionId, inspectionId));
        // 加分数分数
        if(dailyInspectionDO.getViolation() == YesOrNoEnum.NO.ordinal() && dailyInspectionDO.getFraction() != null){
            teaFarmerService.addTeaFarmerFraction(dailyInspectionDO.getFarmerId(), - dailyInspectionDO.getFraction());
        }
        // 删除巡检记录
        this.baseMapper.deleteById(dailyInspectionDO.getId());
    }

    @Override
    public AbstractPageService.PageResults<List<DailyInspectionListVO>> getDailyInspectionList(DailyInspectionSearchModel dailyInspectionSearchModel) {
        dailyInspectionSearchModel.setGeneralParam(new String[]{"inspection_no", "farmer_name", "base_name", "cooperative_name", "violation", "behavioral_content"});
        List<QuerySpecification> querySpecifications = authorizationCodeUtil.getQueryWrapper(AuthorizationCode.INSPECTION_RECORD);
        if(!CollectionUtils.isEmpty(querySpecifications)){
            dailyInspectionSearchModel.getAndConditions().addAll(querySpecifications);
        }

        List<QuerySpecification> andConditions = new ArrayList<>();
        if(StringUtils.isNotBlank(dailyInspectionSearchModel.getFarmerName())){
            andConditions.add(new QuerySpecification("farmer_name",dailyInspectionSearchModel.getFarmerName(),"like"));
            dailyInspectionSearchModel.setFarmerName(null);
        }
        if(StringUtils.isNotBlank(dailyInspectionSearchModel.getCooperativeName())){
            andConditions.add(new QuerySpecification("cooperative_name",dailyInspectionSearchModel.getCooperativeName(),"like"));
            dailyInspectionSearchModel.setCooperativeName(null);
        }
        if(StringUtils.isNotBlank(dailyInspectionSearchModel.getBehavioralContent())){
            andConditions.add(new QuerySpecification("behavioral_content",dailyInspectionSearchModel.getBehavioralContent(),"like"));
            dailyInspectionSearchModel.setBehavioralContent(null);
        }
        dailyInspectionSearchModel.getAndConditions().addAll(andConditions);
        return this.selectPage(dailyInspectionSearchModel, this.getBaseMapper(), DailyInspectionListVO.class);
    }

    /**
     * 导出日常巡检数据
     * @param model
     * @param response
     */
    @Override
    public void export(DailyInspectionSearchModel model, HttpServletResponse response) {
        ExcelWriter excelWriter =null;
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            excelWriter = EasyExcel.write(outputStream)
                    .registerWriteHandler(CustomExcelStyleUtils.getHorizontalCellStyleStrategy())
                    .autoCloseStream(true).build();
            for (int i = 0; i < ExcelConstants.EXCEL_EXPORT_ROW / ExcelConstants.EXCEL_SHEET_ROW; i++){
                model.setCurrent(i + 1);
                model.setPageSize(ExcelConstants.EXCEL_SHEET_ROW);
                AbstractPageService.PageResults<List<DailyInspectionListVO>> pageResults = getDailyInspectionList(model);
                if(!CollectionUtils.isEmpty(pageResults.getList())){
                    WriteSheet sheet = EasyExcel.writerSheet((i+1),"日常巡检记录"+(i+1)).head(DailyInspectionExportVO.class)
                            .registerWriteHandler(CustomExcelStyleUtils.getColumnWidthStyleStrategy(null))
                            .needHead(true).build();
                    excelWriter.write(pageResults.getList(),sheet);
                }else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(excelWriter != null){
                excelWriter.finish();
            }
        }
    }

    @Override
    public DailyInspectionVO getInspectionInfo(String inspectionId) {
        if(StringUtils.isEmpty(inspectionId)){
            return null;
        }
        DailyInspectionDO dailyInspectionDO = this.baseMapper.selectOne(new QueryWrapper<DailyInspectionDO>().lambda()
            .eq(DailyInspectionDO::getInspectionId,inspectionId));
        if(dailyInspectionDO == null){
            return null;
        }
        return CopyUtil.copy(dailyInspectionDO,new DailyInspectionVO());
    }


    /**
     * 获取巡检记录编号
     *
     * @return java.lang.String 记录编号
     */
    private String getInspectionNo(String organizationId) {
        String current = DateUtil.getCurrent(DateUtil.datePattern);
        Date date = DateUtil.plusDays(1);
        long generate = redisUtil.generate(RedisKeyEnum.DAILY_INSPECTION_NO.getKey() + ":" + organizationId + ":" +current, date);
        //确定格式，把1转换为001
        DecimalFormat mFormat = new DecimalFormat("000");
        String no = mFormat.format(generate);
        return IndexNoEnum.XJ.toString() + current + no;
    }
}
