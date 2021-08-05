package com.zxs.server.mapper.gugeng.repairmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffStockFlowDetailDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffStockListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.*;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffStockBatchDetailListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffStockDetailVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffStockFlowDetailListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffStockListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-08
 */
public interface ProductionManageStuffStockMapper extends CommonSql<ProductionManageStuffStock> {
    @Select(START_SCRIPT
            + "select " + ProductionManageStuffStock.COL_TOTAL_INVENTORY + " from t_production_manage_stuff_stock ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    BigDecimal selectSpecificationStock(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageStuffStock> queryWrapper);

    @Select(START_SCRIPT
            + "select " + ProductionManageStuffInWarehouse.COL_INBOUND_CODE + " as boundCode," +
            ProductionManageStuffInWarehouse.COL_STUFF_BATCH + "," + ProductionManageStuffInWarehouse.COL_STUFF_NAME + "," +
            ProductionManageStuffInWarehouse.COL_INBOUND_TYPE_NAME + " as boundTypeName," +
            ProductionManageStuffInWarehouse.COL_INBOUND_NUM + " boundNum," + ProductionManageStuffInWarehouse.COL_CREATE_EMPLOYEE_NAME + "," +
            ProductionManageStuffInWarehouse.COL_CREATE_DATE + " boundDate from t_production_manage_stuff_in_warehouse  ${ew.customSqlSegment}"
            + " union all"
            + " select " + ProductionManageStuffOutWarehouse.COL_OUTBOUND_CODE + " as boundCode," +
            ProductionManageStuffOutWarehouse.COL_STUFF_BATCH + "," + ProductionManageStuffOutWarehouse.COL_STUFF_NAME + "," +
            ProductionManageStuffOutWarehouse.COL_OUTBOUND_TYPE_NAME + " as boundTypeName," +
            ProductionManageStuffOutWarehouse.COL_OUTBOUND_NUM + " boundNum," +
            ProductionManageStuffOutWarehouse.COL_CREATE_EMPLOYEE_NAME + "," +
            ProductionManageStuffOutWarehouse.COL_CREATE_DATE + " boundDate " +
            "from t_production_manage_stuff_out_warehouse ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    IPage<ProductionManageStuffStockFlowDetailListVO> flowDetailList(Page<ProductionManageStuffStockFlowDetailListVO> page, @Param(Constants.WRAPPER) QueryWrapper<ProductionManageStuffStock> queryWrapper);

    @Select(START_SCRIPT
            + "select iw." + ProductionManageStuffInWarehouse.COL_SUPPER_NAME + ",iw." + ProductionManageStuffInWarehouse.COL_STUFF_BATCH +
            ",iw." + ProductionManageStuffInWarehouse.COL_WARE_HOUSE_NAME + ",iw." + ProductionManageStuffInWarehouse.COL_INBOUND_NUM +
            ",ow." + ProductionManageStuffOutWarehouse.COL_OUTBOUND_NUM + ",iw." + ProductionManageStuffInWarehouse.COL_INBOUND_REMAINING_NUM +
            " as totalInventory from t_production_manage_stuff_in_warehouse iw left join t_production_manage_stuff_out_warehouse ow"
            + " on iw." + ProductionManageStuffInWarehouse.COL_PUBLIC_STUFF_ID + "=ow." + ProductionManageStuffOutWarehouse.COL_PUBLIC_STUFF_ID +
            " and iw." + ProductionManageStuffInWarehouse.COL_STUFF_SPECIFICATION_ID + "=ow." + ProductionManageStuffOutWarehouse.COL_STUFF_SPECIFICATION_ID +
            " and iw." + ProductionManageStuffInWarehouse.COL_STUFF_BATCH + "=ow." + ProductionManageStuffOutWarehouse.COL_STUFF_BATCH
            + START_WHERE
            + " <if test='stuffStockFlowDetailDTO.organizationId !=null and stuffStockFlowDetailDTO.organizationId != &apos;&apos; '> " +
            "and iw." + ProductionManageStuffInWarehouse.COL_ORGANIZATION_ID + " = #{stuffStockFlowDetailDTO.organizationId}" +
            " </if> "
            + " <if test='stuffStockFlowDetailDTO.sysId !=null and stuffStockFlowDetailDTO.sysId != &apos;&apos; '> " +
            "and iw." + ProductionManageStuffInWarehouse.COL_SYS_ID + " = #{stuffStockFlowDetailDTO.sysId}" +
            " </if> "
            + " <if test='stuffStockFlowDetailDTO.publicStuffId !=null and stuffStockFlowDetailDTO.publicStuffId != &apos;&apos; '> " +
            "and iw." + ProductionManageStuffInWarehouse.COL_PUBLIC_STUFF_ID + " = #{stuffStockFlowDetailDTO.publicStuffId} " +
            "</if> "
            + " <if test='stuffStockFlowDetailDTO.stuffSpecificationId !=null'> " +
            "and iw." + ProductionManageStuffInWarehouse.COL_STUFF_SPECIFICATION_ID + " = #{stuffStockFlowDetailDTO.stuffSpecificationId} " +
            "</if> "
            + "<choose>"
            + "<when test='stuffStockFlowDetailDTO.search!= null or stuffStockFlowDetailDTO.search!= &apos;&apos;'>" +
            "AND ( iw." + ProductionManageStuffInWarehouse.COL_STUFF_BATCH + " like '%${stuffStockFlowDetailDTO.search}%' " +
            "or ow." + ProductionManageStuffOutWarehouse.COL_STUFF_BATCH + " like '%${stuffStockFlowDetailDTO.search}%' " +
            " or iw." + ProductionManageStuffInWarehouse.COL_SUPPER_NAME + " like '%${stuffStockFlowDetailDTO.search}%' " +
            "or iw." + ProductionManageStuffInWarehouse.COL_WARE_HOUSE_NAME + " like '%${stuffStockFlowDetailDTO.search}%'"
            + ")"
            + "</when>"
            + "</choose>"
            + END_WHERE
            + END_SCRIPT
    )
    IPage<ProductionManageStuffStockBatchDetailListVO> batchDetailList(Page<ProductionManageStuffStockBatchDetailListVO> page, @Param("stuffStockFlowDetailDTO") ProductionManageStuffStockFlowDetailDTO stuffStockFlowDetailDTO);

    @Select(START_SCRIPT
            + "select s." + ProductionManageStuff.COL_STUFF_NAME + ",s." + ProductionManageStuff.COL_PUBLIC_STUFF_ID + "," +
            "st." + ProductionManageStuffStock.COL_STUFF_SPECIFICATION_ID + ",st." + ProductionManageStuffStock.COL_STUFF_NO + "," +
            "s." + ProductionManageStuff.COL_STUFF_SORT_NAME + ",st." + ProductionManageStuffStock.COL_SPECIFICATION + "," +
            "s." + ProductionManageStuff.COL_MEASURE_UNIT + ",st." + ProductionManageStuffStock.COL_TOTAL_INVENTORY
            + " FROM t_production_manage_stuff s " +
            " LEFT JOIN  t_production_manage_stuff_specification ss " +
            "ON s." + ProductionManageStuff.COL_PUBLIC_STUFF_ID + " = ss." + ProductionManageStuffSpecification.COL_PUBLIC_STUFF_ID +
            " LEFT JOIN t_production_manage_stuff_stock st " +
            "ON st." + ProductionManageStuffStock.COL_PUBLIC_STUFF_ID + " = ss." + ProductionManageStuffSpecification.COL_PUBLIC_STUFF_ID + "  " +
            "AND st." + ProductionManageStuffStock.COL_STUFF_SPECIFICATION_ID + " = ss." + ProductionManageStuffSpecification.COL_ID
            + START_WHERE
            + " <if test='publicStuffId !=null and publicStuffId != &apos;&apos; '> and st." + ProductionManageStuffStock.COL_PUBLIC_STUFF_ID + " = #{publicStuffId} </if> "
            + " <if test='stuffSpecificationId !=null'> and st." + ProductionManageStuffStock.COL_STUFF_SPECIFICATION_ID + " = #{stuffSpecificationId} </if> "
            + END_WHERE
            + END_SCRIPT
    )
    ProductionManageStuffStockDetailVO detail(@Param("publicStuffId") String publicStuffId, @Param("stuffSpecificationId") Long stuffSpecificationId);

    @Select(START_SCRIPT
            + "select ss." + ProductionManageStuffStock.COL_ID + " id,s." + ProductionManageStuff.COL_STUFF_NAME + ",ss." + ProductionManageStuffStock.COL_STUFF_NO + ",s." + ProductionManageStuff.COL_STUFF_SORT_NAME + ",ss." + ProductionManageStuffStock.COL_SPECIFICATION + ",s." + ProductionManageStuff.COL_MEASURE_UNIT + ",ss." + ProductionManageStuffStock.COL_TOTAL_INVENTORY + ",ss." + ProductionManageStuffStock.COL_PUBLIC_STUFF_ID + ",ss." + ProductionManageStuffStock.COL_STUFF_SPECIFICATION_ID + "," +
            "if(ss." + ProductionManageStuffStock.COL_TOTAL_INVENTORY + ">=ss." + ProductionManageStuffStock.COL_WARNING_TOTAL_INVENTORY + ",'√库存充足','!库存不足') as warningTotalInventory," + ProductionManageStuffStock.COL_WARNING_TOTAL_INVENTORY + " num"
            + " from t_production_manage_stuff_stock  ss inner join t_production_manage_stuff s on s." + ProductionManageStuff.COL_PUBLIC_STUFF_ID + "=ss." + ProductionManageStuffStock.COL_PUBLIC_STUFF_ID
            + START_WHERE
            + "<if test='stuffStockListDTO.organizationId != null and stuffStockListDTO.organizationId != &apos;&apos;'> AND ss." + ProductionManageStuffStock.COL_ORGANIZATION_ID + "=#{stuffStockListDTO.organizationId} </if>"
            + "<if test='stuffStockListDTO.sysId != null and stuffStockListDTO.sysId != &apos;&apos;'> AND ss." + ProductionManageStuffStock.COL_SYS_ID + "=#{stuffStockListDTO.sysId} </if>"
            + "<choose>"
            + "<when test='stuffStockListDTO.search== null or stuffStockListDTO.search== &apos;&apos;'>" +
            "<if test='stuffStockListDTO.stuffName != null and stuffStockListDTO.stuffName != &apos;&apos;'> AND s." + ProductionManageStuff.COL_STUFF_NAME + "=#{stuffStockListDTO.stuffName} </if>" +
            "<if test='stuffStockListDTO.stuffNo != null and stuffStockListDTO.stuffNo != &apos;&apos;'> AND ss." + ProductionManageStuffStock.COL_STUFF_NO + "=#{stuffStockListDTO.stuffNo} </if>" +
            "<if test='stuffStockListDTO.stuffSortName != null and stuffStockListDTO.stuffSortName != &apos;&apos;'> AND s." + ProductionManageStuff.COL_STUFF_SORT_NAME + "=#{stuffStockListDTO.stuffSortName} </if>" +
            "<if test='stuffStockListDTO.warningInventory != null and stuffStockListDTO.warningInventory ==1'> AND " + ProductionManageStuffStock.COL_TOTAL_INVENTORY + " &gt;= " + ProductionManageStuffStock.COL_WARNING_TOTAL_INVENTORY + "</if>" +
            "<if test='stuffStockListDTO.warningInventory != null and stuffStockListDTO.warningInventory ==-1'> AND " + ProductionManageStuffStock.COL_TOTAL_INVENTORY + " &lt; " + ProductionManageStuffStock.COL_WARNING_TOTAL_INVENTORY + "</if>" +
            "</when>" +
            "<otherwise>" +
            "AND (s." + ProductionManageStuff.COL_STUFF_NAME + " like '%${stuffStockListDTO.search}%'  or s." + ProductionManageStuff.COL_STUFF_SORT_NAME + " like '%${stuffStockListDTO.search}%'  or ss." + ProductionManageStuffStock.COL_SPECIFICATION + " like '%${stuffStockListDTO.search}%' )"
            + "</otherwise>" +
            "</choose>"
            + END_WHERE
            + " order by ss." + ProductionManageStuffStock.COL_ID + " desc "
            + END_SCRIPT
    )
    IPage<ProductionManageStuffStockListVO> pageList(Page<ProductionManageStuffStockListVO> page, @Param("stuffStockListDTO") ProductionManageStuffStockListDTO stuffStockListDTO);

    @Select(START_SCRIPT
            + "select ss." + ProductionManageStuffStock.COL_ID + ",s." + ProductionManageStuff.COL_STUFF_NAME + ",s." + ProductionManageStuff.COL_STUFF_SORT_NAME + ",ss." + ProductionManageStuffStock.COL_SPECIFICATION + ",s." + ProductionManageStuff.COL_MEASURE_UNIT + ",ss." + ProductionManageStuffStock.COL_STUFF_NO + ",ss." + ProductionManageStuffStock.COL_TOTAL_INVENTORY + ",ss." + ProductionManageStuffStock.COL_PUBLIC_STUFF_ID + ",ss." + ProductionManageStuffStock.COL_STUFF_SPECIFICATION_ID + "," +
            "if(ss." + ProductionManageStuffStock.COL_TOTAL_INVENTORY + ">ss." + ProductionManageStuffStock.COL_WARNING_TOTAL_INVENTORY + ",'√库存充足','!库存不足') as warningTotalInventory "
            + " from t_production_manage_stuff s left join t_production_manage_stuff_stock ss on s." + ProductionManageStuff.COL_PUBLIC_STUFF_ID + "=ss." + ProductionManageStuffStock.COL_PUBLIC_STUFF_ID
            + END_SCRIPT
    )
    List<ProductionManageStuffStockListVO> selectVoByIds(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageStuffStockListVO> queryWrapper);
}
