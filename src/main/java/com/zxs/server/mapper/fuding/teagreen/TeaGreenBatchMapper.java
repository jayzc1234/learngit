package com.zxs.server.mapper.fuding.teagreen;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenBatch;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenAcquisitionListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author caihong
 * @since 2020-09-07
 */
public interface TeaGreenBatchMapper extends BaseMapper<TeaGreenBatch> {
    @Select("SELECT sum(IFNULL(t.quantity,0))-sum(IFNULL(t.bare_weight,0)) teaGreenAcquisitionQuantity,t.* " +
            " FROM t_tea_green_acquisition t  " +
            " WHERE t.disable_status =1 and organization_id=#{organizationId} " +
            " GROUP BY DATE_FORMAT(t.acquisition_time,'%Y-%m-%d'),t.product_id,t.product_level_name")
    List<TeaGreenAcquisitionListVO> getTeaBatchData(@Param("organizationId") String organizationId);

    // 添加自定义接口方法
}
