package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSaleProductData;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageSaleProductDataListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-15
 */
public interface ProductionManageSaleProductDataMapper extends CommonSql<ProductionManageSaleProductData> {
    @Select(START_SCRIPT
            + "select ProductName,ProductSortName,TopProductSortName,OrderDate,truncate(SUM(OrderMoney),2) as orderMoney,truncate(SUM(ReceivedOrderMoney),2) as receivedOrderMoney " +
            "from production_manage_sale_product_data " +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    IPage<ProductionManageSaleProductDataListVO> pageList(Page<ProductionManageSaleProductDataListVO> page, @Param(Constants.WRAPPER) QueryWrapper<ProductionManageSaleProductData> queryWrapper);
}
