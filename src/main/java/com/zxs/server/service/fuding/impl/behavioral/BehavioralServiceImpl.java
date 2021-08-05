package com.zxs.server.service.fuding.impl.behavioral;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.advancedsearch.common.QuerySpecification;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.bo.fuding.BehavioralBO;
import net.app315.hydra.intelligent.planting.constants.ExcelConstants;
import net.app315.hydra.intelligent.planting.enums.fuding.AuthorizationCode;
import net.app315.hydra.intelligent.planting.enums.fuding.YesOrNoEnum;
import net.app315.hydra.intelligent.planting.exception.gugeng.TeaException;
import net.app315.hydra.intelligent.planting.pojo.fuding.behavioral.BehavioralDO;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.base.BehavioralMapper;
import net.app315.hydra.intelligent.planting.server.service.fuding.behavioral.IBehavioralService;
import net.app315.hydra.intelligent.planting.utils.fuding.AuthorizationCodeUtil;
import net.app315.hydra.intelligent.planting.utils.fuding.CustomExcelStyleUtils;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.BehavioralExportVO;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.BehavioralSearchModel;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.BehavioralVO;
import net.app315.hydra.user.sdk.provide.context.UserContextHelper;
import net.app315.hydra.user.sdk.provide.model.AccountCache;
import net.app315.hydra.user.sdk.provide.model.EmployeeCache;
import net.app315.nail.common.utils.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 行为管理 服务实现类
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@Service
public class BehavioralServiceImpl extends ServiceImpl<BehavioralMapper, BehavioralDO> implements IBehavioralService {

    @Autowired
    private UserContextHelper userContextHelper;
    @Autowired
    private AuthorizationCodeUtil authorizationCodeUtil;

    @Override
    public String addBehavioral(BehavioralBO behavioralBO) {
        BehavioralDO behavioralDO = CopyUtil.copy(behavioralBO, new BehavioralDO());
        AccountCache accountCache = userContextHelper.getUserLoginCache();
        behavioralDO.setOperator(accountCache.getUserId());
        behavioralDO.setOperatorName(accountCache.getUserName());
        behavioralDO.setOrganizationId(accountCache.getOrganizationCache().getOrganizationId());
        behavioralDO.setOrganizationName(accountCache.getOrganizationCache().getOrganizationFullName());
        behavioralDO.setBehavioralId(UUIDUtil.getUUID());
        //未删除
        behavioralDO.setDeleted(YesOrNoEnum.NO.ordinal());
        EmployeeCache department = userContextHelper.getOrganization().getEmployeeCache();
        if (department != null) {
            behavioralDO.setDepartmentId(department.getDepartmentId());
            behavioralDO.setDepartmentName(department.getDepartmentName());
        }
        // 名称重复判断
        synchronized (this){
            int count = count(new QueryWrapper<BehavioralDO>().lambda()
                    .eq(BehavioralDO::getBehavioralContent, behavioralDO.getBehavioralContent())
                    .eq(BehavioralDO::getDeleted,1));
            if(count > 0){
                throw new TeaException("行为: "+behavioralDO.getBehavioralContent()+" 已存在，请勿重复添加");
            }
            this.save(behavioralDO);
        }
        return behavioralDO.getBehavioralId();
    }

    @Override
    public void updateBehavioral(BehavioralBO behavioralBO) {
        BehavioralDO behavioralDO = CopyUtil.copy(behavioralBO, new BehavioralDO());
        behavioralDO.setUpdateTime(new Date());
        // 名称重复判断
        synchronized (this){
            int count = count(new QueryWrapper<BehavioralDO>().lambda()
                    .eq(BehavioralDO::getBehavioralContent, behavioralDO.getBehavioralContent())
                    .ne(BehavioralDO::getBehavioralId,behavioralDO.getBehavioralId())
                    .eq(BehavioralDO::getDeleted,1));
            if(count > 0){
                throw new TeaException("行为: "+behavioralDO.getBehavioralContent()+" 已存在，请勿重复添加");
            }
            behavioralDO.setUpdateTime(new Date());
            this.updateById(behavioralDO);
        }
    }

    @Override
    public BehavioralBO getBehavioral(String behavioralId) {
        BehavioralDO behavioralDO = getOne(new QueryWrapper<BehavioralDO>().lambda()
                .eq(BehavioralDO::getBehavioralId, behavioralId));
        return CopyUtil.copy(behavioralDO, new BehavioralBO());
    }


    /**
     * 删除行为信息
     *
     * @param behavioralId
     * @return
     */
    @Override
    public void deleteBehavioral(String behavioralId) {
        BehavioralDO behavioralDO = getOne(new QueryWrapper<BehavioralDO>().lambda()
                .eq(BehavioralDO::getBehavioralId, behavioralId));
        behavioralDO.setUpdateTime(new Date());
        behavioralDO.setDeleted(YesOrNoEnum.YES.ordinal());
        this.updateById(behavioralDO);
    }

    @Override
    public AbstractPageService.PageResults<List<BehavioralVO>> getBehavioralList(BehavioralSearchModel model) {
        model.setGeneralParam(new String[]{"behavioral_content", "fraction"});
        List<QuerySpecification> querySpecifications = authorizationCodeUtil.getQueryWrapper(AuthorizationCode.BEHAVIOR_MANGE);
        if(!CollectionUtils.isEmpty(querySpecifications)){
            model.getAndConditions().addAll(querySpecifications);
        }
        return this.selectPage(model, getBaseMapper(), BehavioralVO.class);
    }

    @Override
    public void export(BehavioralSearchModel model, HttpServletResponse response) {
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
                AbstractPageService.PageResults<List<BehavioralVO>> pageResults = getBehavioralList(model);
                if(!CollectionUtils.isEmpty(pageResults.getList())){
                    WriteSheet sheet = EasyExcel.writerSheet((i+1),"行为记录"+(i+1)).head(BehavioralExportVO.class)
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
}
