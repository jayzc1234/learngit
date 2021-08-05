package com.zxs.server.mapper.gugeng.producemanage;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.ProductionManageSdProduceSchemeNodeListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManageSdProduceSchemeNode;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zc
 * @since 2019-06-14
 */
public interface ProductionManageSdProduceSchemeNodeMapper extends CommonSql<ProductionManageSdProduceSchemeNode> {

	@Select(START_SCRIPT
            + "select " + ProductionManageSdProduceSchemeNode.COL_DAY_NUMBER + " dayNumber " +
            "from t_production_manage_sd_produce_scheme_node"
            + START_WHERE
            + "<if test='pNodeListDTO.sdProduceSchemeId !=null and pNodeListDTO.sdProduceSchemeId != &apos;&apos;'>  " + ProductionManageSdProduceSchemeNode.COL_SD_PRODUCE_SCHEME_ID + " =#{pNodeListDTO.sdProduceSchemeId}</if>"
            + END_WHERE
            + " group by " + ProductionManageSdProduceSchemeNode.COL_DAY_NUMBER + " "
            + " order by " + ProductionManageSdProduceSchemeNode.COL_DAY_NUMBER + " asc"
            + END_SCRIPT)
    IPage<Integer> list(Page<Integer> page, @Param("pNodeListDTO") ProductionManageSdProduceSchemeNodeListDTO pNodeListDTO);

	@Select(START_SCRIPT
            + "select * from t_production_manage_sd_produce_scheme_node"
            + START_WHERE
            + "<if test='dayNums !=null and dayNums.size()>0'> "+ProductionManageSdProduceSchemeNode.COL_DAY_NUMBER+" in"
            + " <foreach collection='dayNums' item='item' index='index' open='(' separator=',' close=')' >"
            + "#{item}"
            + " </foreach>"
            + "</if>"
            + "<if test='sdProduceSchemeId !=null'> and " + ProductionManageSdProduceSchemeNode.COL_SD_PRODUCE_SCHEME_ID + " =#{sdProduceSchemeId}</if>"
            + END_WHERE
            + " order by " + ProductionManageSdProduceSchemeNode.COL_DAY_NUMBER + " asc"
            + END_SCRIPT)
	List<ProductionManageSdProduceSchemeNode> selectByDayNumsAndSchemeId(@Param("dayNums") List<Integer> dayNums, @Param("sdProduceSchemeId") Long sdProduceSchemeId);

	@Select(START_SCRIPT
            + "select " + ProductionManageSdProduceSchemeNode.COL_DAY_NUMBER + ",count(1) as num " +
            "from t_production_manage_sd_produce_scheme_node"
            + START_WHERE
            + "<if test='sdProduceSchemeId !=null'> and " + ProductionManageSdProduceSchemeNode.COL_SD_PRODUCE_SCHEME_ID + " =#{sdProduceSchemeId}</if>"
            + END_WHERE
            + " group by " + ProductionManageSdProduceSchemeNode.COL_DAY_NUMBER
            + END_SCRIPT)
	List<Map<String, Object>> selectDayNumberCount(@Param("sdProduceSchemeId") Long schemeId);

}
