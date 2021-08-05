package com.zxs.server.mapper.fuding.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.TeaFarmerDO;
import net.app315.hydra.intelligent.planting.vo.fuding.base.TeaFarmerRankListVO;
import net.app315.hydra.intelligent.planting.vo.fuding.base.TeaFarmerRankSearchModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 茶农 Mapper 接口
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
public interface TeaFarmerMapper extends BaseMapper<TeaFarmerDO> {

    /**
     * 修改茶农积分
     * @param farmerId 茶农id
     * @param add 要添加的分数，正数加，负数减
     * @return int
     */
    int addTeaFarmerFraction(@Param("farmerId") String farmerId, @Param("add") int add);


    /**
     * 获取茶农排名列表
     * @param model 查询条件实体
     * @return 列表
     */
    List<TeaFarmerRankListVO> getTeaFarmerRankList(TeaFarmerRankSearchModel model);

    /**
     * 获取茶农排名总数
     *
     * @param model 查询条件实体
     * @return long 数量
     */
    Long getTeaFarmerRankListCount(TeaFarmerRankSearchModel model);

    /**
     * 计算带动面积和年收购总量
     * @return
     */
    @Select("SELECT SUM(tea_garden) teaGardenArea,SUM(tea_green_acquisition_quantity) totalTeaGreenAcquisitionQuantity," +
            "cooperative_id FROM t_tea_farmer group by cooperative_id")
    List<CooperativeDO> calculatorAreaAndQuantity();

}
