package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ProductionManageSaleProductDataListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSaleProductData;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageSaleProductDataMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageSaleProductDataListVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-10-15
 */
@Service
public class ProductionManageSaleProductDataService extends ServiceImpl<ProductionManageSaleProductDataMapper, ProductionManageSaleProductData> implements BaseService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;


    public IPage<ProductionManageSaleProductDataListVO> pageList(ProductionManageSaleProductDataListDTO saleProductDataListDTO) throws SuperCodeException {
        Page<ProductionManageSaleProductDataListVO> page = CommonUtil.genPage(saleProductDataListDTO);
        QueryWrapper<ProductionManageSaleProductData> queryWrapper = commonUtil.queryTemplate(ProductionManageSaleProductData.class);
        queryWrapper.ge(StringUtils.isNotBlank(saleProductDataListDTO.getStartQueryDate()),"OrderDate",saleProductDataListDTO.getStartQueryDate());
        queryWrapper.le(StringUtils.isNotBlank(saleProductDataListDTO.getEndQueryDate()),"OrderDate",saleProductDataListDTO.getEndQueryDate());
        queryWrapper.eq(StringUtils.isNotBlank(saleProductDataListDTO.getProductSortId()),"ProductSortId",saleProductDataListDTO.getProductSortId());
        queryWrapper.groupBy("ProductId");
        return baseMapper.pageList(page,queryWrapper);
    }

}
