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
import net.app315.hydra.intelligent.planting.dto.gugeng.repairmanage.ProductionManageInspectionManageDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairmanage.ProductionManageInspectionManageListDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.enums.gugeng.repairmanage.InspectionStatusEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageInspectionManage;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageInspectionManageMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.utils.redis.RedisUtil;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairmanage.ProductionManageInspectionManageListVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.INSPECTION;


/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-09-30
 */
@Service
public class ProductionManageInspectionManageService extends ServiceImpl<ProductionManageInspectionManageMapper, ProductionManageInspectionManage> implements BaseService<ProductionManageInspectionManageListVO> {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;


    public void add(ProductionManageInspectionManageDTO inspectionManageDTO) throws SuperCodeException, ParseException {

        Date inspectionStartDate = inspectionManageDTO.getInspectionStartDate();
        Date inspectionEndDate = inspectionManageDTO.getInspectionEndDate();
        if (null!=inspectionStartDate && null!=inspectionEndDate){
            if (inspectionStartDate.compareTo(inspectionEndDate)>0){
                CommonUtil.throwSuperCodeExtException(500,"开始时间不能大于结束时间");
            }
        }
        ProductionManageInspectionManage entity = new ProductionManageInspectionManage();
        BeanUtils.copyProperties(inspectionManageDTO,entity);
        entity.setCreateDate(CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
        Employee employee=commonUtil.getEmployee();
        entity.setCreateEmployeeId(employee.getEmployeeId());
        entity.setAuthDepartmentId(employee.getDepartmentId());
        String taskNo=inspectionManageDTO.getTaskNo();
        taskNo = genTaslNo(taskNo,null);
        entity.setTaskNo(taskNo);
        entity.setInspectionStatus(InspectionStatusEnum.UN_INSPECTION.getStatus());
        baseMapper.insert(entity);
    }

    private String genTaslNo(String taskNo,Long id) throws ParseException {
        QueryWrapper<ProductionManageInspectionManage> queryWrapper=commonUtil.queryTemplate(ProductionManageInspectionManage.class);
        if (StringUtils.isBlank(taskNo)){
            taskNo = getBoundCode();
            queryWrapper.eq(ProductionManageInspectionManage.COL_TASK_NO, taskNo);
            Integer count = baseMapper.selectCount(queryWrapper);
            int i = 0;
            while (count > 0) {
                i++;
                taskNo = getBoundCode();
                queryWrapper.eq(ProductionManageInspectionManage.COL_TASK_NO, taskNo);
                count = baseMapper.selectCount(queryWrapper);
                if (i > 20) {
                    CommonUtil.throwSuperCodeExtException(500, "生成巡检编号异常");
                }
            }
        }else {
            queryWrapper.ne(null != id, ProductionManageInspectionManage.COL_ID, id);
            queryWrapper.eq(ProductionManageInspectionManage.COL_TASK_NO, taskNo);
            Integer count = baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                CommonUtil.throwSuperCodeExtException(500, "巡检编号已存在");
            }
        }
        return taskNo;
    }

    public String getBoundCode() throws ParseException {
        StringBuilder stringBuilder = new StringBuilder(1024);
        stringBuilder.append(CommonUtil.getCurrentDateStr(DateTimePatternConstant.yyyyMMdd));
        String organizationId=commonUtil.getOrganizationId();
        String sysId=commonUtil.getSysId();
        long n = redisUtil.generate(RedisKey.STUFF_INSPECTION_CODE+organizationId+":"+sysId, CommonUtil.getSecondsNextEarlyMorning());
        int count = 6 - String.valueOf(n).length();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                stringBuilder.append("0");
            }
            stringBuilder.append(n);
        } else {
            stringBuilder.append(n);
        }
        return stringBuilder.toString();
    }

    public void update(ProductionManageInspectionManageDTO inspectionManageDTO) throws SuperCodeException, ParseException {
        Long id=inspectionManageDTO.getId();
        if (null==id){
            CommonUtil.throwSuperCodeExtException(500,"id不能为空");
        }
        ProductionManageInspectionManage existentity=baseMapper.selectById(id);
        if (null==existentity){
            CommonUtil.throwSuperCodeExtException(500,"不存在该巡检记录");
        }
        Date inspectionStartDate = inspectionManageDTO.getInspectionStartDate();
        Date inspectionEndDate = inspectionManageDTO.getInspectionEndDate();
        if (null!=inspectionStartDate && null!=inspectionEndDate){
            if (inspectionStartDate.compareTo(inspectionEndDate)>0){
                CommonUtil.throwSuperCodeExtException(500,"开始时间不能大于结束时间");
            }
        }
        ProductionManageInspectionManage entity = new ProductionManageInspectionManage();
        BeanUtils.copyProperties(inspectionManageDTO,entity);
        String taskNo=inspectionManageDTO.getTaskNo();
        taskNo = genTaslNo(taskNo,id);
        entity.setTaskNo(taskNo);
        baseMapper.updateById(entity);
    }

    @Override
    public IPage<ProductionManageInspectionManageListVO> pageList(DaoSearch daoSearch) throws SuperCodeException {
        ProductionManageInspectionManageListDTO inspectionManageListDTO= (ProductionManageInspectionManageListDTO) daoSearch;
        Page<ProductionManageInspectionManageListVO> page = new Page<>(inspectionManageListDTO.getDefaultCurrent(), inspectionManageListDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageInspectionManage> queryWrapper = commonUtil.advanceSearchQueryWrapperSet(inspectionManageListDTO,"ProductionManageInspectionManageService.list",ProductionManageInspectionManage.class);
        queryWrapper.orderByDesc(ProductionManageInspectionManage.COL_CREATE_DATE);

        // 添加数据权限
        commonUtil.roleDataAuthFilter(INSPECTION, queryWrapper, ProductionManageInspectionManage.COL_CREATE_EMPLOYEE_ID, null);

        IPage<ProductionManageInspectionManageListVO> iPage = baseMapper.pageList(page, queryWrapper);
        List<ProductionManageInspectionManageListVO> records = iPage.getRecords();
        if (CollectionUtils.isNotEmpty(records)){
            for (ProductionManageInspectionManageListVO record : records) {
                record.setInspectionStatusName(InspectionStatusEnum.getDesc(record.getInspectionStatus()));
            }
        }
        return iPage;
    }

    @Override
    public List<ProductionManageInspectionManageListVO> listExcelByIds(List<? extends Serializable> ids) throws SuperCodeException {
        QueryWrapper<ProductionManageInspectionManage> queryWrapper =commonUtil.queryTemplate(ProductionManageInspectionManage.class);
        queryWrapper.in(ProductionManageInspectionManage.COL_ID, ids);
        List<ProductionManageInspectionManageListVO> productionManageInspectionManages = baseMapper.selectVOByIds(queryWrapper);
        if (CollectionUtils.isNotEmpty(productionManageInspectionManages)){
            for (ProductionManageInspectionManageListVO record : productionManageInspectionManages) {
                record.setInspectionStatusName(InspectionStatusEnum.getDesc(record.getInspectionStatus()));
            }
        }
        return productionManageInspectionManages;
    }

    public ProductionManageInspectionManage getById(String id) throws SuperCodeException {
        QueryWrapper<ProductionManageInspectionManage> queryWrapper = commonUtil.queryTemplate(ProductionManageInspectionManage.class);
        queryWrapper.eq(ProductionManageInspectionManage.COL_ID, id);
        return baseMapper.selectOne(queryWrapper);
    }

    public void deleteOne(Long id) {
        baseMapper.deleteById(id);
    }

    public void inspection(Long id) {
        ProductionManageInspectionManage inspectionManage=getById(id);
        if(null==inspectionManage){
            CommonUtil.throwSuperCodeExtException(500,"不存在该巡检记录");
        }
        inspectionManage.setInspectionStatus(InspectionStatusEnum.DOING_INSPECTION.getStatus());
        baseMapper.updateById(inspectionManage);
    }

    public void doneInspection(ProductionManageInspectionManageDTO inspectionManageDTO) {
        Long id = inspectionManageDTO.getId();
        if (null==id){
            CommonUtil.throwSuperCodeExtException(500,"主键不能为空");
        }
        ProductionManageInspectionManage inspectionManage=getById(id);
        if(null==inspectionManage){
            CommonUtil.throwSuperCodeExtException(500,"不存在该巡检记录");
        }
        if (inspectionManage.getInspectionStatus()!=InspectionStatusEnum.DOING_INSPECTION.getStatus()){
            CommonUtil.throwSuperCodeExtException(500,"维护中状态才可以完成执行");
        }
        ProductionManageInspectionManage inspectionManage1=new ProductionManageInspectionManage();
        BeanUtils.copyProperties(inspectionManageDTO,inspectionManage1);
        inspectionManage1.setInspectionStatus(InspectionStatusEnum.DONE_INSPECTION.getStatus());
        baseMapper.updateById(inspectionManage1);
    }
}
