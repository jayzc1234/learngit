package com.zxs.server.service.gugeng.repairmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.DateTimePatternConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairmanage.*;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.enums.gugeng.repairmanage.RepairStatusEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageConsumeStuff;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageRepairManage;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageRepairManageMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairmanage.ProductionManageConsumeStuffVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairmanage.ProductionManageRepairManageDetailVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairmanage.ProductionManageRepairManageListVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.REPAIR;


/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-09-29
 */
@Service
public class ProductionManageRepairManageService extends ServiceImpl<ProductionManageRepairManageMapper, ProductionManageRepairManage> implements BaseService<ProductionManageRepairManageListVO> {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private ProductionManageConsumeStuffService consumeStuffService;

    /**
     * 维修申请
     * @param repairApplyDTO
     * @throws SuperCodeException
     * @throws ParseException
     */
    public void callRepair(ProductionManageRepairApplyDTO repairApplyDTO) throws SuperCodeException, ParseException {
        ProductionManageRepairManage entity = new ProductionManageRepairManage();
        BeanUtils.copyProperties(repairApplyDTO,entity);
        Employee employee=commonUtil.getEmployee();
        entity.setCreateEmployeeId(employee.getEmployeeId());
        entity.setRepairStatus(RepairStatusEnum.WAIT_REPAIR.getStatus());
        entity.setCreateUserId(employee.getUserId());
        entity.setApplyDoneDate(CommonUtil.formatStringToDate(repairApplyDTO.getApplyDoneDate(), DateTimePatternConstant.YYYY_MM_DD));
        entity.setCallRepairDate(CommonUtil.formatStringToDate(repairApplyDTO.getCallRepairDate(), DateTimePatternConstant.YYYY_MM_DD));
        String repairNo=getRepariNo(repairApplyDTO.getRepairNo(), repairApplyDTO.getId());
        entity.setRepairNo(repairNo);
        entity.setApplyDate(CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
        entity.setAuthDepartmentId(employee.getDepartmentId());
        baseMapper.insert(entity);
    }

    public void update(ProductionManageRepairApplyDTO repairApplyDTO) throws SuperCodeException {
        Long id=repairApplyDTO.getId();
        ProductionManageRepairManage repairManage=baseMapper.selectById(id);
        if (null==repairManage){
            CommonUtil.throwSuperCodeExtException(500,"该维修申请不存在");
        }
        Integer repairStatus=repairManage.getRepairStatus();
        if (repairStatus!=RepairStatusEnum.WAIT_REPAIR.getStatus() && repairStatus!=RepairStatusEnum.WAIT_ASSIGN.getStatus()){
            CommonUtil.throwSuperCodeExtException(500,"只有待维修状态可以编辑");
        }
        boolean flag=StringUtils.isBlank(repairApplyDTO.getRepairNo()) || !repairManage.getRepairNo().equals(repairApplyDTO.getRepairNo());
        ProductionManageRepairManage entity = new ProductionManageRepairManage();
        BeanUtils.copyProperties(repairApplyDTO,entity);
        if (flag){
            String repairNo=getRepariNo(repairApplyDTO.getRepairNo(),repairApplyDTO.getId());
            entity.setRepairNo(repairNo);
        }
        entity.setCallRepairDate(CommonUtil.formatStringToDate(repairApplyDTO.getCallRepairDate(), DateTimePatternConstant.YYYY_MM_DD));
        entity.setApplyDoneDate(CommonUtil.formatStringToDate(repairApplyDTO.getApplyDoneDate(), DateTimePatternConstant.YYYY_MM_DD));
        baseMapper.updateById(entity);
    }

    @Override
    public IPage<ProductionManageRepairManageListVO> pageList(DaoSearch daoSearch) throws SuperCodeException {
        ProductionManageRepairManageListDTO repairManageListDTO= (ProductionManageRepairManageListDTO) daoSearch;
        Page<ProductionManageRepairManageListVO> page = new Page<ProductionManageRepairManageListVO>(repairManageListDTO.getCurrent(), repairManageListDTO.getPageSize());
        QueryWrapper<ProductionManageRepairManageListVO> queryWrapper = commonUtil.advanceSearchQueryWrapperSet(repairManageListDTO, "ProductionManageInspectionManageService.list", ProductionManageRepairManage.class);
        queryWrapper.orderByDesc(ProductionManageRepairManage.COL_APPLY_DATE);

        // 添加数据权限
        commonUtil.roleDataAuthFilter(REPAIR, queryWrapper, ProductionManageRepairManage.COL_CREATE_EMPLOYEE_ID, null);

        IPage<ProductionManageRepairManageListVO> productionManageRepairManageListVOIPage = baseMapper.pageList(page, queryWrapper);
        List<ProductionManageRepairManageListVO> records = productionManageRepairManageListVOIPage.getRecords();
        if (CollectionUtils.isNotEmpty(records)){
            for (ProductionManageRepairManageListVO record : records) {
                record.setRepairStatusName(RepairStatusEnum.getDesc(record.getRepairStatus()));
            }
        }
        return productionManageRepairManageListVOIPage;
    }

    @Override
    public List<ProductionManageRepairManageListVO> listExcelByIds(List<? extends Serializable> ids) throws SuperCodeException {
        QueryWrapper<ProductionManageRepairManage> queryWrapper =commonUtil.queryTemplate(ProductionManageRepairManage.class);
        queryWrapper.in(ProductionManageRepairManage.COL_ID, ids);
        List<ProductionManageRepairManageListVO> records =baseMapper.selectVoByIds(queryWrapper);
        if (CollectionUtils.isNotEmpty(records)){
            for (ProductionManageRepairManageListVO record : records) {
                record.setRepairStatusName(RepairStatusEnum.getDesc(record.getRepairStatus()));
            }
        }
        return records;
    }

    /**
     * 维护指派
     * @param assignDTO
     */
    public void assignRepair(ProductionManageRepairAssignDTO assignDTO) {
        Long id=assignDTO.getId();
        ProductionManageRepairManage repairManage=baseMapper.selectById(id);
        if (null==repairManage){
            CommonUtil.throwSuperCodeExtException(500,"该维修申请不存在");
        }
        repairManage.setRepairStatus(RepairStatusEnum.WAIT_REPAIR.getStatus());
        repairManage.setAssignUserId(assignDTO.getAssignUserId());
        repairManage.setAssignUserName(assignDTO.getAssignUserName());
        repairManage.setAssignDate(CommonUtil.formatStringToDate(assignDTO.getAssignDate(),DateTimePatternConstant.YYYY_MM_DD_HH_MM_SS));
        baseMapper.updateById(repairManage);
    }

    /**
     * 执行维修
     * @param runRepairDTO
     * @throws ParseException
     */
    public void runRepair(ProductionManageRunRepairDTO runRepairDTO) throws ParseException {
        Long id=runRepairDTO.getId();
        ProductionManageRepairManage repairManage=baseMapper.selectById(id);
        if (null==repairManage){
            CommonUtil.throwSuperCodeExtException(500,"该维修申请不存在");
        }
        repairManage.setEstimateDoneRepairDate(CommonUtil.formatStringToDate(runRepairDTO.getEstimateDoneRepairDate(),"yyyy-MM-dd"));
        repairManage.setOrderTakerDate(CommonUtil.formatStringToDate(runRepairDTO.getOrderTakerDate(),"yyyy-MM-dd"));
        repairManage.setOrderTakerId(runRepairDTO.getOrderTakerId());
        repairManage.setOrderTakerName(runRepairDTO.getOrderTakerName());
        repairManage.setRepairStatus(RepairStatusEnum.DOING_REPAIR.getStatus());
        repairManage.setRepairDepartmentId(runRepairDTO.getRepairDepartmentId());
        repairManage.setRepairDepartmentName(runRepairDTO.getRepairDepartmentName());
        repairManage.setProgressDesc(runRepairDTO.getProgressDesc());
        repairManage.setProgressSate(runRepairDTO.getProgressSate());
        repairManage.setSafeMark(runRepairDTO.getSafeMark());
        baseMapper.updateById(repairManage);
    }

    /**
     * 完成维修
     * @param repairDoneDTO
     * @throws ParseException
     */
    @Transactional(rollbackFor = Exception.class)
    public void doneRepair(ProductionManageRepairDoneDTO repairDoneDTO) throws ParseException {
        Long id=repairDoneDTO.getId();
        ProductionManageRepairManage repairManage=baseMapper.selectById(id);
        if (null==repairManage){
            CommonUtil.throwSuperCodeExtException(500,"该维修申请不存在");
        }
        repairManage.setRepairEmployeeId(repairDoneDTO.getRepairEmployeeId());
        repairManage.setRepairEmployeeName(repairDoneDTO.getRepairEmployeeName());
        repairManage.setRepairDoneDate(CommonUtil.formatStringToDate(repairDoneDTO.getRepairDoneDate(),"yyyy-MM-dd"));
        repairManage.setRepairStatus(RepairStatusEnum.UN_CONFIRM.getStatus());
        List<ProductionManageConsumeStuffDTO> consumeStuffDTOS= repairDoneDTO.getConsumeStuffList();
        if (CollectionUtils.isNotEmpty(consumeStuffDTOS)){
            List<ProductionManageConsumeStuff> consumeStuffList=new ArrayList<>(consumeStuffDTOS.size());
            for (ProductionManageConsumeStuffDTO consumeStuffDTO:consumeStuffDTOS) {
                ProductionManageConsumeStuff consumeStuff=new ProductionManageConsumeStuff();
                consumeStuff.setPublicStuffId(consumeStuffDTO.getPublicStuffId());
                consumeStuff.setSameBatchStuffId(consumeStuffDTO.getSameBatchStuffId());
                consumeStuff.setSpecification(consumeStuffDTO.getSpecification());
                consumeStuff.setStuffAmount(consumeStuffDTO.getStuffAmount());
                consumeStuff.setStuffName(consumeStuffDTO.getStuffName());
                consumeStuff.setSpecificationId(consumeStuffDTO.getStuffSpecificationId());
                consumeStuff.setRepairApplyId(repairManage.getId());
                consumeStuff.setSameBatchStuffId(consumeStuffDTO.getSameBatchStuffId());
                consumeStuffList.add(consumeStuff);
            }
            consumeStuffService.saveBatch(consumeStuffList);
        }
        baseMapper.updateById(repairManage);
    }

    /**
     * 完成确定
     * @param confirmDoneManageDTO
     */
    public void doneConfirm(ProductionManageRepairConfirmDoneManageDTO confirmDoneManageDTO) throws ParseException {
        Long id=confirmDoneManageDTO.getId();
        ProductionManageRepairManage repairManage=baseMapper.selectById(id);
        if (null==repairManage){
            CommonUtil.throwSuperCodeExtException(500,"该维修申请不存在");
        }
        repairManage.setCallRepairDepartmentId(confirmDoneManageDTO.getCallRepairDepartmentId());
        repairManage.setCallRepairDepartmentName(confirmDoneManageDTO.getCallRepairDepartmentName());
        repairManage.setCallRepairEmployeeId(confirmDoneManageDTO.getCallRepairEmployeeId());
        repairManage.setCallRepairEmployeeName(confirmDoneManageDTO.getCallRepairEmployeeName());
        repairManage.setConfirmDoneDate(CommonUtil.formatStringToDate(confirmDoneManageDTO.getConfirmDoneDate(),"yyyy-MM-dd"));
        repairManage.setConfirmDoneMark(confirmDoneManageDTO.getConfirmDoneMark());
        repairManage.setRepairStatus(RepairStatusEnum.DONE.getStatus());
        repairManage.setGgEvaluationOpinion(confirmDoneManageDTO.getGgEvaluationOpinion());
        baseMapper.updateById(repairManage);
    }

    /**
     * 维修评价
     * @param commentDTO
     */
    public void commentRepair(ProductionManageRepairCommentDTO commentDTO) {
        Long id=commentDTO.getId();
        ProductionManageRepairManage repairManage=baseMapper.selectById(id);
        if (null==repairManage){
            CommonUtil.throwSuperCodeExtException(500,"该维修申请不存在");
        }
        repairManage.setCommentDepartmentId(commentDTO.getCommentDepartmentId());
        repairManage.setCommentDepartmentName(commentDTO.getCommentDepartmentName());
        repairManage.setCommentDesc(commentDTO.getCommentDesc());
        baseMapper.updateById(repairManage);
    }

    public ProductionManageRepairManageDetailVO detail(Long id) throws SuperCodeException {
        ProductionManageRepairManage repairManage=baseMapper.selectById(id);
        if (null==repairManage){
            CommonUtil.throwSuperCodeExtException(500,"该维修申请不存在");
        }
        ProductionManageRepairManageDetailVO repairManageDetailVO=new ProductionManageRepairManageDetailVO();
        BeanUtils.copyProperties(repairManage,repairManageDetailVO);

        QueryWrapper<ProductionManageConsumeStuff> consumeStuffQueryWrapper=new QueryWrapper<>();
        consumeStuffQueryWrapper.eq(ProductionManageConsumeStuff.COL_REPAIR_APPLY_ID, id);
        List<ProductionManageConsumeStuff> consumeStuffList=consumeStuffService.list(consumeStuffQueryWrapper);
        if (CollectionUtils.isNotEmpty(consumeStuffList)){
            List<ProductionManageConsumeStuffVO> consumeStuffVOS=new ArrayList<>();
            for (ProductionManageConsumeStuff consumeStuff:consumeStuffList) {
                ProductionManageConsumeStuffVO consumeStuffVO=new ProductionManageConsumeStuffVO();
                BeanUtils.copyProperties(consumeStuff,consumeStuffVO);
                consumeStuffVOS.add(consumeStuffVO);
                repairManageDetailVO.setConsumeStuffList(consumeStuffVOS);
            }
        }
      return repairManageDetailVO;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteOne(Long id) {
        baseMapper.deleteById(id);
        consumeStuffService.deleteByRepairApplyId(id);
    }

    private String getRepariNo(String repairNo, Long id) throws SuperCodeException {
        QueryWrapper<ProductionManageRepairManage> queryWrapper = commonUtil.queryTemplate(ProductionManageRepairManage.class);
        String newRepairNo = null;
        if (StringUtils.isNotBlank(repairNo)) {
            newRepairNo = repairNo;
            queryWrapper.ne(null != id, ProductionManageRepairManage.COL_ID, id);
            queryWrapper.eq(ProductionManageRepairManage.COL_REPAIR_NO, newRepairNo);
            Integer count = baseMapper.selectCount(queryWrapper);
            if (null != count && count > 0) {
                CommonUtil.throwSuperCodeExtException(500, "该编号已存在");
            }
        } else {
            String organizationId = commonUtil.getOrganizationId();
            String sysId = commonUtil.getSysId();
            newRepairNo = numberGenerator.getSerialNumber(6, RedisKey.REPAIR_NO_KEY, organizationId + sysId, CommonUtil.getSecondsNextEarlyMorning());
            queryWrapper.eq(ProductionManageRepairManage.COL_REPAIR_NO, newRepairNo);
            Integer count = baseMapper.selectCount(queryWrapper);
            while (null != count && count > 0) {
                newRepairNo = numberGenerator.getSerialNumber(6, RedisKey.REPAIR_NO_KEY, organizationId + sysId, CommonUtil.getSecondsNextEarlyMorning());
                queryWrapper.eq(ProductionManageRepairManage.COL_REPAIR_NO, newRepairNo);
                count = baseMapper.selectCount(queryWrapper);
            }
        }
     return newRepairNo;
    }

}
