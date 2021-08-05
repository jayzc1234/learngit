package com.zxs.server.mapper.gugeng.datascreen;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.app315.hydra.intelligent.planting.pojo.gugeng.datascreen.ProductionManageStatisticsRecord;
import org.apache.ibatis.annotations.Select;

//×
public interface ProductionManageStatisticsRecordMapper extends BaseMapper<ProductionManageStatisticsRecord> {

    @Select("SELECT " + ProductionManageStatisticsRecord.COL_PAGE_CONTENT + " FROM `t_production_manage_statistics_record` WHERE " + ProductionManageStatisticsRecord.COL_PAGE_ID + " = #{pageId} ")
    String selectPageContent(String pageId);

}