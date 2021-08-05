package com.zxs.server.mapper.fuding.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.TeaFarmerYearOutputDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * <p>
 * 茶农社年收购记录表 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2020-02-17
 */
public interface TeaFarmerYearOutputMapper extends BaseMapper<TeaFarmerYearOutputDO> {

    @Update("update t_tea_farmer_year_output set amount = IFNULL(amount,0) + #{amount},quantity = IFNULL(quantity,0) + ${quantity},update_time = now() where id = #{id}")
    int addCurrentOutPut(@Param("id") Long id, @Param("amount") BigDecimal amount, @Param("quantity") BigDecimal quantity);

    @Update("update t_tea_farmer_year_output set amount = amount - #{amount},quantity = quantity - ${quantity},update_time = now() where id = #{id}")
    int lessCurrentOutPut(@Param("id") Long id, @Param("amount") BigDecimal amount, @Param("quantity") BigDecimal quantity);

}
