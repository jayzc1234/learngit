package com.zxs.server.mapper.gugeng;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 描述：公共sql模块
 *
 * @author corbett
 *         Created by corbett on 2018/10/15.
 * @param <T>
 */
public interface CommonSql<T> extends BaseMapper<T> {
    String select = " SELECT ";
    String START_SCRIPT = "<script>";
    String END_SCRIPT = "</script>";
    String count = "count(*)";
    String page = "<if test='startNumber != null and startNumber != 0 and pageCount != null and pageCount != 0'> LIMIT #{startNumber},#{pageCount}</if>";
    String page_limit = "LIMIT #{startNum},#{pageCount}";
    
    String START_WHERE = "<where>";
    String END_WHERE = "</where>";
	String search =
					"<choose>" +
					//当search为空时要么为高级搜索要么没有搜索暂时为不搜索
					"<when test='search == null or search == &apos;&apos;'>" +
//                    " <if test = ' activityName != null and activityName != &apos;&apos; '> mmw.ActivityName = #{activityName}</if>" +
//                    " <if test = ' userName != null and userName != &apos;&apos; '> mm.UserName  = #{userName}</if>" +
//                    " <if test = ' wxName != null and wxName != &apos;&apos; '> WxName = #{wxName}</if>" +
//                    " <if test = ' openid != null and openid != &apos;&apos; '> mmw.Openid  = #{openid}</if>" +
//                    " <if test = ' mobile != null and mobile != &apos;&apos; '> mmw.Mobile = #{mobile}</if>" +
//                    " <if test = ' prizeTypeName != null and prizeTypeName != &apos;&apos; '> mpt.PrizeTypeName = #{prizeTypeName}</if>" +
//                    " <if test = ' winningAmount != null and  winningAmount != &apos;&apos; '> mmw.WinningAmount = #{winningAmount}</if>" +
//                    " <if test = ' winningCode != null and winningCode != &apos;&apos; '> mmw.WinningCode = #{winningCode}</if>" +
//                    " <if test = ' productName != null and productName != &apos;&apos; '> map.ProductName = #{productName}</if>" +
                    "</when>" +
					//如果search不为空则普通搜索
					"<otherwise>" +
						"<if test='search !=null and search != &apos;&apos;'>" +
					     "${commonSearchXml}" +
						"</if>" +
					"</otherwise>" +
					"</choose>";

	/**
	 * 通用批量新增方法
	 */
	@Insert("${sql}")
	void batchAdd(@Param("sql") String sql);
}
