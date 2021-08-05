package com.zxs.server.service.gugeng.repairmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.ProductManageConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffSpecificationListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuff;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuffSpecification;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuffStock;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageStuffSpecificationMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageStuffStockMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffSpecificationListVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-09-29
 */
@Service
public class ProductionManageStuffSpecificationService extends ServiceImpl<ProductionManageStuffSpecificationMapper, ProductionManageStuffSpecification> {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private ProductionManageStuffStockMapper stuffStockMapper;

    public Long getMaxSerialNumber(String organizationId, String sysId) {
        return baseMapper.getMaxSerialNumber(organizationId,sysId);
    }

    public PageResults<List<ProductionManageStuffSpecificationListVO>> dropDown(ProductionManageStuffSpecificationListDTO specificationListDTO) {
        Page<ProductionManageStuffSpecificationListVO> page = CommonUtil.genPage(specificationListDTO);
        QueryWrapper<ProductionManageStuffSpecification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("s." + ProductManageConstant.SYS_ORGANIZATIONID, commonUtil.getOrganizationId());
        queryWrapper.eq("s." + ProductManageConstant.SYS_SYSID, commonUtil.getSysId());
        queryWrapper.like(StringUtils.isNotBlank(specificationListDTO.getSearch()), ProductionManageStuffSpecification.COL_SPECIFICATION, specificationListDTO.getSearch());
        queryWrapper.eq(StringUtils.isNotBlank(specificationListDTO.getSameBatchStuffId()),"ss." + ProductionManageStuffSpecification.COL_SAME_BATCH_STUFF_ID, specificationListDTO.getSameBatchStuffId());
        IPage<ProductionManageStuffSpecificationListVO> iPage = baseMapper.pageList(page, queryWrapper);
        List<ProductionManageStuffSpecificationListVO> records = iPage.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            BigDecimal zeroBigDecimal = new BigDecimal(0);
            records.forEach(re -> {
                Long id = re.getId();
                String publicStuffId = re.getPublicStuffId();
                QueryWrapper<ProductionManageStuffStock> stockQueryWrapper = commonUtil.queryTemplate(ProductionManageStuffStock.class);
                stockQueryWrapper.eq(ProductionManageStuffStock.COL_PUBLIC_STUFF_ID, publicStuffId);
                stockQueryWrapper.eq(ProductionManageStuffStock.COL_STUFF_SPECIFICATION_ID, id);
                BigDecimal bigDecimal = stuffStockMapper.selectSpecificationStock(stockQueryWrapper);
                re.setStuffSpecificationStockNum(null == bigDecimal ? zeroBigDecimal : bigDecimal);
            });
        }
        return CommonUtil.iPageToPageResults(iPage, null);
    }

    public void updateUseNum(Long stuffSpecificationId) {
        baseMapper.updateUseNum(stuffSpecificationId);
    }

    public List<ProductionManageStuffSpecificationListVO> listByStuffNameAndSortId(String stuffName, String stuffSortId) {
        QueryWrapper<ProductionManageStuffSpecification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProductManageConstant.SYS_ORGANIZATIONID, commonUtil.getOrganizationId())
                .eq(ProductManageConstant.SYS_SYSID, commonUtil.getSysId());
        queryWrapper.eq("s." + ProductionManageStuff.COL_STUFF_NAME, stuffName);
        queryWrapper.eq("s." + ProductionManageStuff.COL_STUFF_SORT_ID, stuffSortId);
        return baseMapper.listByStuffNameAndSortId(queryWrapper);
    }

    public List<ProductionManageStuffSpecification> selectByIds(List<Long> ids) {
        return  baseMapper.selectBatchIds(ids);
    }
}
