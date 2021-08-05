package com.zxs.server.service.gugeng.repairmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.DateTimePatternConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffStockFlowDetailDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffStockListDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuff;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuffInWarehouse;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuffStock;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageStuffInWarehouseMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageStuffStockMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.utils.redis.RedisUtil;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffStockBatchDetailListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffStockDetailVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffStockFlowDetailListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffStockListVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-10-08
 */
@Service
public class ProductionManageStuffStockService extends ServiceImpl<ProductionManageStuffStockMapper, ProductionManageStuffStock> implements BaseService<ProductionManageStuffStockListVO> {


    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private ProductionManageStuffInWarehouseMapper stuffInWarehouseMapper;

    @Override
    public IPage<ProductionManageStuffStockListVO> pageList(DaoSearch daoSearch) throws SuperCodeException {
        ProductionManageStuffStockListDTO stuffStockListDTO= (ProductionManageStuffStockListDTO) daoSearch;
        Page<ProductionManageStuffStockListVO> page = new Page<>(stuffStockListDTO.getDefaultCurrent(), stuffStockListDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStuffStockListVO> queryWrapper = commonUtil.queryTemplate(ProductionManageStuffStockListVO.class);
        stuffStockListDTO.setOrganizationId(commonUtil.getOrganizationId());
        stuffStockListDTO.setSysId(commonUtil.getSysId());
        Employee employee = commonUtil.getEmployee();

        IPage<ProductionManageStuffStockListVO> iPage=baseMapper.pageList(page,stuffStockListDTO);
        return iPage;
    }

    @Override
    public List<ProductionManageStuffStockListVO> listExcelByIds(List<? extends Serializable> ids) throws SuperCodeException {
        QueryWrapper<ProductionManageStuffStockListVO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("s." + ProductionManageStuff.COL_ID, ids);
        queryWrapper.eq("s." + ProductionManageStuff.COL_ORGANIZATION_ID, commonUtil.getOrganizationId());
        queryWrapper.eq("s." + ProductionManageStuff.COL_SYS_ID, commonUtil.getSysId());
        List<ProductionManageStuffStockListVO> stuffStockListVOS = baseMapper.selectVoByIds(queryWrapper);
        return stuffStockListVOS;
    }

    public BigDecimal selectSpecificationStock(String publicStuffId, Long stuffSpecificationId) {
        QueryWrapper<ProductionManageStuffStock> queryWrapper = commonUtil.queryTemplate(ProductionManageStuffStock.class);
        queryWrapper.eq(ProductionManageStuffStock.COL_PUBLIC_STUFF_ID, publicStuffId);
        queryWrapper.eq(ProductionManageStuffStock.COL_STUFF_SPECIFICATION_ID, stuffSpecificationId);
        return baseMapper.selectSpecificationStock(queryWrapper);
    }

    public ProductionManageStuffStock selectByPublicStuffIdAndSpecificationId(String publicStuffId, Long stuffSpecificationId) {
        QueryWrapper<ProductionManageStuffStock> queryWrapper = commonUtil.queryTemplate(ProductionManageStuffStock.class);
        queryWrapper.eq(ProductionManageStuffStock.COL_PUBLIC_STUFF_ID, publicStuffId);
        queryWrapper.eq(ProductionManageStuffStock.COL_STUFF_SPECIFICATION_ID, stuffSpecificationId);
        return baseMapper.selectOne(queryWrapper);
    }

    public PageResults<List<ProductionManageStuffStockFlowDetailListVO>> flowDetailList(ProductionManageStuffStockFlowDetailDTO specificationListDTO) {
        Page<ProductionManageStuffStockFlowDetailListVO> page = CommonUtil.genPage(specificationListDTO);
        QueryWrapper<ProductionManageStuffStock> queryWrapper = commonUtil.queryTemplate(ProductionManageStuffStock.class);
        queryWrapper.eq(ProductionManageStuffStock.COL_PUBLIC_STUFF_ID, specificationListDTO.getPublicStuffId());
        queryWrapper.eq(ProductionManageStuffStock.COL_STUFF_SPECIFICATION_ID, specificationListDTO.getStuffSpecificationId());
        IPage<ProductionManageStuffStockFlowDetailListVO> ipage = baseMapper.flowDetailList(page, queryWrapper);
        return CommonUtil.iPageToPageResults(ipage, null);
    }

    public PageResults<List<ProductionManageStuffStockBatchDetailListVO>> batchDetailList(ProductionManageStuffStockFlowDetailDTO specificationListDTO) {
        Page<ProductionManageStuffStockBatchDetailListVO> page=CommonUtil.genPage(specificationListDTO);
        specificationListDTO.setOrganizationId(commonUtil.getOrganizationId());
        specificationListDTO.setSysId(commonUtil.getSysId());
        IPage<ProductionManageStuffStockBatchDetailListVO> ipage=baseMapper.batchDetailList(page,specificationListDTO);
        return CommonUtil.iPageToPageResults(ipage,null);
    }

    public ProductionManageStuffStockDetailVO detail(String publicStuffId, Long stuffSpecificationId) {
        return  baseMapper.detail(publicStuffId,stuffSpecificationId);
    }


    public String getBoundCode() throws ParseException {
        StringBuilder stringBuilder = new StringBuilder(1024);
        stringBuilder.append("CRK");
        stringBuilder.append(CommonUtil.getCurrentDateStr(DateTimePatternConstant.yyyyMMdd));
        String organizationId=commonUtil.getOrganizationId();
        String sysId=commonUtil.getSysId();
        long n = redisUtil.generate(RedisKey.STUFF_BOUND_CODE+organizationId+":"+sysId, CommonUtil.getSecondsNextEarlyMorning());
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

    public void warningInventory(Long id, Double num) {
        ProductionManageStuffStock stuffStock = baseMapper.selectById(id);
        if (null==stuffStock){
            CommonUtil.throwSuperCodeExtException(500,"库存不存在");
        }
        stuffStock.setWarningTotalInventory(new BigDecimal(num));
        baseMapper.updateById(stuffStock);
    }

    public void outboundCheck(String publicStuffId, Long stuffSpecificationId, String stuffBatch, BigDecimal outboundNum) {
       if (StringUtils.isBlank(stuffBatch)){
           ProductionManageStuffStock stuffStock = selectByPublicStuffIdAndSpecificationId(publicStuffId, stuffSpecificationId);
           if (null==stuffStock){
               CommonUtil.throwSuperCodeExtException(500,"库存不存在");
           }
           if (stuffStock.getTotalInventory().compareTo(outboundNum)<0){
               CommonUtil.throwSuperCodeExtException(500,"库存不足");
           }
       }else {
           QueryWrapper<ProductionManageStuffInWarehouse> inWarehouseQueryWrapper = commonUtil.queryTemplate(ProductionManageStuffInWarehouse.class);
           inWarehouseQueryWrapper.eq(ProductionManageStuffInWarehouse.COL_PUBLIC_STUFF_ID, publicStuffId);
           inWarehouseQueryWrapper.eq(ProductionManageStuffInWarehouse.COL_STUFF_SPECIFICATION_ID, stuffSpecificationId);
           inWarehouseQueryWrapper.eq(ProductionManageStuffInWarehouse.COL_STUFF_BATCH, stuffBatch);

           ProductionManageStuffInWarehouse productionManageStuffInWarehouse = stuffInWarehouseMapper.selectOne(inWarehouseQueryWrapper);
           if (null == productionManageStuffInWarehouse) {
               CommonUtil.throwSuperCodeExtException(500, "不存在该批次入库记录");
           }
           if (productionManageStuffInWarehouse.getInboundRemainingNum().compareTo(outboundNum) < 0) {
               CommonUtil.throwSuperCodeExtException(500, "库存不足");
           }
       }
    }
}
