package com.zxs.server.mapper.gugeng.hydra.operationsmanagement;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.gugeng.hydra.operationsmanagement.GuGengVisitorManage;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.hydra.operationsmanagement.GuGengVisitorManageListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>1散客，2团体客户，3会员客户，4接待
 *${ew.customSqlSegment}
 * @author shixiongfei
 * @since 2019-12-03
 */
public interface GuGengVisitorManageMapper extends CommonSql<GuGengVisitorManage> {
    /**
     * 分页
     * @param page
     * @param queryWrapper:ss
     * @return
     */
    @Select("select id,visit_no,client_name,visit_date,visitor_num,client_type," +
            "case when client_type=1 then '散客' when client_type=2 then '团体客户' when client_type=3 then '会员客户' when client_type=4 then '接待' end client_type_name," +
            " repast_receivable_money,repast_real_money,stay_receivable_money,stay_real_money,leisure_receivable_money,leisure_real_money,other_receivable_money,other_real_money "
            +" from t_gu_geng_visitor_manage ${ew.customSqlSegment}")
    IPage<GuGengVisitorManageListVO> pageList(Page<GuGengVisitorManageListVO> page, @Param(Constants.WRAPPER) QueryWrapper<GuGengVisitorManage> queryWrapper);
}
