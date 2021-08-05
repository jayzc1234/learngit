package com.zxs.server.service.gugeng.repairmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffListDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffSpecificationDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffUpdateDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.stuffmanage.StuffStatusEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuff;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuffSpecification;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuffStock;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageStuffMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageStuffStockMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.utils.redis.RedisUtil;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffDetailVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffSpecificationListVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.MAINTAIN_MATERIALS;


/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-09-29
 */
@Service
public class ProductionManageStuffService extends ServiceImpl<ProductionManageStuffMapper, ProductionManageStuff> implements BaseService<ProductionManageStuffListVO> {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private ProductionManageStuffSpecificationService stuffSpecificationService;

    @Autowired
    private ProductionManageStuffStockMapper stuffStockMapper;

    private static String STUFF_NO_REDIS_KEY="stuff_no:";

    @Transactional(rollbackFor = Exception.class)
    public void add(ProductionManageStuffDTO stuffDTO) throws SuperCodeException, ParseException {
        List<ProductionManageStuffSpecificationDTO> specificationDTOS=stuffDTO.getSpecificationDTOList();
        if (CollectionUtils.isEmpty(specificationDTOS)){
          CommonUtil.throwSuperCodeExtException(500,"规格不能为空");
        }
        Date currentDate=CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss");
        Employee employee=commonUtil.getEmployee();
        String sameBatchStuffId=commonUtil.getUUID();
        String organizationId=commonUtil.getOrganizationId();
        String sysId=commonUtil.getSysId();
        String key=organizationId+sysId+":"+STUFF_NO_REDIS_KEY;

        List<String>list=new ArrayList<>();
        specificationDTOS.forEach(a->{
            String specification = a.getSpecification();
            boolean contains = list.contains(specification);
            if (contains){
                CommonUtil.throwSuperCodeExtException(500,"规格:"+specification+"已存在");
            }else {
                list.add(specification);
            }
        });
        List<ProductionManageStuff> stuffList=new ArrayList<>();
        String stuffName = stuffDTO.getStuffName();
        List<ProductionManageStuffSpecificationListVO> specificationList=stuffSpecificationService.listByStuffNameAndSortId(stuffName,stuffDTO.getStuffSortId());
        String measureUnitParam = stuffDTO.getMeasureUnit();

        if (CollectionUtils.isNotEmpty(specificationList)){
            specificationList.forEach(sp->{
                specificationDTOS.forEach(sd->{
                    if (sd.getSpecification().equals(sp.getSpecification())){
                        CommonUtil.throwSuperCodeExtException(500,"规格:"+sp.getSpecification()+"已存在");
                    }
                });
            });
            sameBatchStuffId=specificationList.get(0).getSameBatchStuffId();
            if (StringUtils.isNotBlank(measureUnitParam)){
                UpdateWrapper<ProductionManageStuff> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq(ProductionManageStuff.COL_SAME_BATCH_STUFF_ID, sameBatchStuffId);
                updateWrapper.set(ProductionManageStuff.COL_MEASURE_UNIT, measureUnitParam);
                baseMapper.update(null, updateWrapper);
            }else {
                measureUnitParam= specificationList.get(0).getMeasureUnit();
            }
        }
        List<ProductionManageStuffSpecification> stuffSpecifications=new ArrayList<>();
        for (ProductionManageStuffSpecificationDTO stuffSpecificationDTO:specificationDTOS) {
            String stuffNo=stuffSpecificationDTO.getStuffNo();
            String publicStuffId=commonUtil.getUUID();
            ProductionManageStuff entity = new ProductionManageStuff();
            BeanUtils.copyProperties(stuffDTO,entity);
            entity.setCreateDate(currentDate);
            entity.setDisableFlag(StuffStatusEnum.ENABLE.getStatus());
            entity.setCreateEmployeeId(employee.getEmployeeId());
            entity.setCreateEmployeeName(employee.getName());
            entity.setPublicStuffId(publicStuffId);
            entity.setSameBatchStuffId(sameBatchStuffId);
            entity.setDisableFlag(StuffStatusEnum.ENABLE.getStatus());
            entity.setMeasureUnit(measureUnitParam);
            entity.setAuthDepartmentId(employee.getDepartmentId());
            stuffList.add(entity);
            //自动生成材料编号
            stuffNo = generateRepairNo(stuffNo,key,publicStuffId, entity);
            ProductionManageStuffSpecification stuffSpecification=new ProductionManageStuffSpecification();
            stuffSpecification.setPublicStuffId(publicStuffId);
            stuffSpecification.setSameBatchStuffId(sameBatchStuffId);
            stuffSpecification.setUseNum(0L);
            stuffSpecification.setStuffNo(stuffNo);
            stuffSpecification.setSpecification(stuffSpecificationDTO.getSpecification());
            stuffSpecification.setPrice(stuffSpecificationDTO.getPrice());
            stuffSpecifications.add(stuffSpecification);
        }
        this.saveBatch(stuffList);
        stuffSpecificationService.saveBatch(stuffSpecifications);
    }


    @Transactional(rollbackFor = Exception.class)
    public void update(ProductionManageStuffUpdateDTO stuffDTO) throws SuperCodeException {
        Long id = stuffDTO.getId();
        Long stuffSpecificationId = stuffDTO.getStuffSpecificationId();
        if (null == id || null == stuffSpecificationId) {
            CommonUtil.throwSuperCodeExtException(500, "材料主键和规格注解不能为空");
        }
        ProductionManageStuff productionManageStuff = baseMapper.selectById(id);
        if (null == productionManageStuff) {
            CommonUtil.throwSuperCodeExtException(500, "材料不存在");
        }

        QueryWrapper<ProductionManageStuff> stuffQueryWrapper = commonUtil.queryTemplate(ProductionManageStuff.class);
        String stuffName = stuffDTO.getStuffName();
        stuffQueryWrapper.eq(ProductionManageStuff.COL_STUFF_NAME, stuffName);
        stuffQueryWrapper.eq(ProductionManageStuff.COL_STUFF_SORT_ID, productionManageStuff.getStuffSortId());
        stuffQueryWrapper.ne(ProductionManageStuff.COL_SAME_BATCH_STUFF_ID, productionManageStuff.getSameBatchStuffId());
        Integer count = baseMapper.selectCount(stuffQueryWrapper);
        if (null != count && count > 0) {
            CommonUtil.throwSuperCodeExtException(500, "材料名称已存在");
        }

        List<ProductionManageStuffSpecificationListVO> specificationList = stuffSpecificationService.listByStuffNameAndSortId(stuffName, productionManageStuff.getStuffSortId());
        if (CollectionUtils.isNotEmpty(specificationList)) {
            specificationList.forEach(sp -> {
                if (!sp.getId().equals(stuffDTO.getStuffSpecificationId())) {
                    if (sp.getSpecification().equals(stuffDTO.getSpecification())) {
                        CommonUtil.throwSuperCodeExtException(500, "材料规格已存在");
                    }
                }
            });
        }
        String measureUnit = stuffDTO.getMeasureUnit();
        if (!productionManageStuff.getStuffName().equals(stuffName) || StringUtils.isNotBlank(measureUnit)) {
            UpdateWrapper<ProductionManageStuff> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq(ProductionManageStuff.COL_SAME_BATCH_STUFF_ID, productionManageStuff.getSameBatchStuffId());
            updateWrapper.set(ProductionManageStuff.COL_STUFF_NAME, stuffName);
            updateWrapper.set(ProductionManageStuff.COL_MEASURE_UNIT, measureUnit);
            baseMapper.update(null, updateWrapper);
        }
        ProductionManageStuff entity = new ProductionManageStuff();
        BeanUtils.copyProperties(stuffDTO, entity);
        String organizationId = commonUtil.getOrganizationId();
        String sysId = commonUtil.getSysId();
        String key = organizationId + sysId + ":" + STUFF_NO_REDIS_KEY;

        //自动生成材料编号
        String stuffNo = generateRepairNo(stuffDTO.getStuffNo(), key, entity.getPublicStuffId(), entity);
        ProductionManageStuffSpecification stuffSpecification = new ProductionManageStuffSpecification();
        stuffSpecification.setSpecification(stuffDTO.getSpecification());
        stuffSpecification.setStuffNo(stuffNo);
        stuffSpecification.setId(stuffDTO.getStuffSpecificationId());
        stuffSpecification.setPrice(stuffDTO.getPrice());
        stuffSpecificationService.updateById(stuffSpecification);
        baseMapper.updateById(entity);

    }


    private String generateRepairNo(String stuffNo,String key,String publicStuffId, ProductionManageStuff entity) throws SuperCodeException {
        QueryWrapper<ProductionManageStuffSpecification> specificationQueryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(stuffNo)) {
            specificationQueryWrapper.eq(ProductionManageStuffSpecification.COL_STUFF_NO, stuffNo);
            specificationQueryWrapper.ne(ProductionManageStuffSpecification.COL_PUBLIC_STUFF_ID, publicStuffId);
            int count = stuffSpecificationService.count(specificationQueryWrapper);
            if (count > 0) {
                CommonUtil.throwSuperCodeExtException(500, "材料编号：" + stuffNo + " 已存在");
            } else {
                return stuffNo;
            }
        } else {
            long stuffNoNum = redisUtil.generate(key);
            String newstuffNo = "CL" + CommonUtil.transferNumToNo(stuffNoNum, 6);
            specificationQueryWrapper.eq(ProductionManageStuffSpecification.COL_STUFF_NO, newstuffNo);
            int count = stuffSpecificationService.count(specificationQueryWrapper);
            //材料编号存在则重新生成
            if (count > 0) {
                String organizationId = commonUtil.getOrganizationId();
                String sysId = commonUtil.getSysId();
                Long maxSerialNumber = stuffSpecificationService.getMaxSerialNumber(organizationId, sysId);
                //防止redis挂掉stuffNoNum值从0开始重置，大量查询数据库
                if (null != maxSerialNumber && maxSerialNumber > stuffNoNum) {
                    redisUtil.set(key, maxSerialNumber.toString());
                }
                while (count > 0) {
                    stuffNoNum = redisUtil.generate(key);
                    newstuffNo = "CL" + CommonUtil.transferNumToNo(stuffNoNum, 6);
                    count = stuffSpecificationService.count(specificationQueryWrapper);
                }
            }
            entity.setSerialNumber(stuffNoNum);
            return newstuffNo;
        }
        return null;
    }

    @Override
    public IPage<ProductionManageStuffListVO> pageList(DaoSearch daoSearch) {
        ProductionManageStuffListDTO stuffListDTO = (ProductionManageStuffListDTO) daoSearch;
        Page<ProductionManageStuff> page = new Page<>(stuffListDTO.getDefaultCurrent(), stuffListDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStuff> queryWrapper = commonUtil.queryTemplate(ProductionManageStuff.class);
        String search = stuffListDTO.getSearch();
        if (StringUtils.isNotBlank(stuffListDTO.getSearch())) {
            queryWrapper.and(outorder -> outorder.like("s." + ProductionManageStuff.COL_STUFF_NAME, search)
                    .or().like("ss." + ProductionManageStuffSpecification.COL_SPECIFICATION + " ", search)
                    .or().like("ss." + ProductionManageStuffSpecification.COL_STUFF_NO, search)
                    .or().like("s." + ProductionManageStuff.COL_STUFF_SORT_NAME, search)
            );
        } else {
            queryWrapper.eq(StringUtils.isNotBlank(stuffListDTO.getStuffName()), "s." + ProductionManageStuff.COL_STUFF_NAME, stuffListDTO.getStuffName());
            queryWrapper.eq(StringUtils.isNotBlank(stuffListDTO.getStuffNo()), "ss." + ProductionManageStuffSpecification.COL_STUFF_NO, stuffListDTO.getStuffNo());
            queryWrapper.eq(StringUtils.isNotBlank(stuffListDTO.getStuffSortName()), "s." + ProductionManageStuff.COL_STUFF_SORT_NAME, stuffListDTO.getStuffSortName());
            queryWrapper.eq(null != stuffListDTO.getStuffSortId(), "s." + ProductionManageStuff.COL_STUFF_SORT_ID, stuffListDTO.getStuffSortId());
            queryWrapper.eq(null != stuffListDTO.getDisableFlag(), "s." + ProductionManageStuff.COL_DISABLE_FLAG, stuffListDTO.getDisableFlag());
        }
        queryWrapper.orderByDesc("s." + ProductionManageStuff.COL_ID);

        // 添加数据权限
        commonUtil.roleDataAuthFilter(MAINTAIN_MATERIALS, queryWrapper, "s." + ProductionManageStuff.COL_CREATE_EMPLOYEE_ID, null);

        IPage<ProductionManageStuffListVO> ipage=baseMapper.pageList(page,queryWrapper);
        List<ProductionManageStuffListVO> records = ipage.getRecords();
        if (CollectionUtils.isNotEmpty(records)){
            for (ProductionManageStuffListVO record : records) {
                record.setDisableFlagName(StuffStatusEnum.getDesc(record.getDisableFlag()));
            }
        }
        return ipage;
    }


    @Override
    public List<ProductionManageStuffListVO> listExcelByIds(List<? extends Serializable> ids) throws SuperCodeException {
        QueryWrapper<ProductionManageStuff> queryWrapper = commonUtil.queryTemplate(ProductionManageStuff.class);
        queryWrapper.in("s." + ProductionManageStuff.COL_ID, ids);
        List<ProductionManageStuffListVO> stuffListVOS=baseMapper.selectVoByIds(queryWrapper);
        if (CollectionUtils.isNotEmpty(stuffListVOS)){
            for (ProductionManageStuffListVO record : stuffListVOS) {
                record.setDisableFlagName(StuffStatusEnum.getDesc(record.getDisableFlag()));
            }
        }
        return stuffListVOS;
    }

    public void disable(Long id) {
        ProductionManageStuff productionManageStuff=baseMapper.selectById(id);
        if (null==productionManageStuff){
            CommonUtil.throwSuperCodeExtException(500,"材料不存在");
        }
        productionManageStuff.setDisableFlag(StuffStatusEnum.DISABLE.getStatus());
        baseMapper.updateById(productionManageStuff);
    }

    public void enable(Long id) {
        ProductionManageStuff productionManageStuff=baseMapper.selectById(id);
        if (null==productionManageStuff){
            CommonUtil.throwSuperCodeExtException(500,"材料不存在");
        }
        productionManageStuff.setDisableFlag(StuffStatusEnum.ENABLE.getStatus());
        baseMapper.updateById(productionManageStuff);
    }

    public PageResults<List<ProductionManageStuffListVO>> dropDown(ProductionManageStuffListDTO stuffListDTO) throws SuperCodeException {
        Page<ProductionManageStuff> page = new Page<>(stuffListDTO.getDefaultCurrent(), stuffListDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStuff> queryWrapper = commonUtil.queryTemplate(ProductionManageStuff.class);
        queryWrapper.eq(ProductionManageStuff.COL_DISABLE_FLAG, StuffStatusEnum.ENABLE.getStatus());
        queryWrapper.eq(null != stuffListDTO.getStuffSortId(), ProductionManageStuff.COL_STUFF_SORT_ID, stuffListDTO.getStuffSortId());
        queryWrapper.like(StringUtils.isNotBlank(stuffListDTO.getSearch()), ProductionManageStuff.COL_STUFF_NAME, stuffListDTO.getSearch());
        queryWrapper.groupBy(ProductionManageStuff.COL_STUFF_SORT_ID, ProductionManageStuff.COL_STUFF_NAME);
        IPage<ProductionManageStuffListVO> ipage = baseMapper.dropDown(page, queryWrapper);
        List<ProductionManageStuffListVO> records = ipage.getRecords();
        BigDecimal zeroBigDecimal = new BigDecimal(0);
        if (CollectionUtils.isNotEmpty(records)) {
            for (ProductionManageStuffListVO record : records) {
                QueryWrapper<ProductionManageStuffStock> stockQueryWrapper = commonUtil.queryTemplate(ProductionManageStuffStock.class);
                stockQueryWrapper.eq(ProductionManageStuffStock.COL_PUBLIC_STUFF_ID, record.getPublicStuffId());
                stockQueryWrapper.eq(ProductionManageStuffStock.COL_STUFF_SPECIFICATION_ID, record.getStuffSpecificationId());
                BigDecimal bigDecimal = stuffStockMapper.selectSpecificationStock(stockQueryWrapper);
                record.setStuffSpecificationStockNum(null == bigDecimal ? zeroBigDecimal : bigDecimal);
                record.setDisableFlagName(StuffStatusEnum.getDesc(record.getDisableFlag()));
            }
        }
        return CommonUtil.iPageToPageResults(ipage, null);
    }

    public ProductionManageStuffDetailVO detail(Long id) {
        return  baseMapper.detail(id);
    }
}
