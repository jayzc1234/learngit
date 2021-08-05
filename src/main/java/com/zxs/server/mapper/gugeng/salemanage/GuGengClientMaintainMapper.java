package com.zxs.server.mapper.gugeng.salemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.GuGengClientMaintain;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.GuGengClientMaintainDetailVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-11-28
 */
public interface GuGengClientMaintainMapper extends CommonSql<GuGengClientMaintain> {
    @Select(" select cm.*,gcm.* from t_gu_geng_client_maintain cm left  join  t_gu_geng_contact_man gcm on cm.gg_contact_man_id=gcm.Id where cm.Id=#{id}")
    GuGengClientMaintainDetailVO getDetailById(@Param("id") Long id);

    @Select(" select cm.Id,cm.client_id,gg_contact_man_id,area_code,client_name, CONCAT_WS('',province_name,city_name,county_name,town_ship_name,detail_address) detailAddress,CONCAT_WS('',province_name,city_name,county_name,town_ship_name) location," +
            " cm.maintain_date,cm.maintain_project,cm.maintain_content,cm.deal_condition,cm.remark,cm.maintain_employee_name,gcm.contact_man,gcm.contact_phone,cm.client_name," +
            " gcm.Id as ggContactManId,cm.maintain_date"
            +" from t_gu_geng_client_maintain cm left  join  t_gu_geng_contact_man gcm on cm.gg_contact_man_id=gcm.Id ${ew.customSqlSegment}")
    IPage<GuGengClientMaintainDetailVO> pageList(Page<GuGengClientMaintain> page, @Param(Constants.WRAPPER) QueryWrapper<GuGengClientMaintain> queryWrapper);
}
