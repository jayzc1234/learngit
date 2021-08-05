package com.zxs.server.mapper.gugeng.salemanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.ProductManageOrderListDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.PersonOrderConditionDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SaleUserLineChartDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSaleOrderStatusDayStatistics;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSaleProductDayStatistics;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsSaleTargetData;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductManageOrderListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductManageOrderMapper extends CommonSql<ProductionManageOrder> {
	@Select(START_SCRIPT
			+ " select o.Id,o.order_no,o.order_type,o.delivery_date,o.product_name as productNames,o.order_weight,o.sale_user_id,o.sale_user_name,o.client_middle_address"
			+ ",o.order_quantity,o.order_money,o.order_status,o.order_date,o.operator_name,c.category_name,o.client_name,o.client_contact_man ContactMan,o.client_contact_phone ContactPhone,CONCAT_WS('',c.province_name,c.city_name,c.county_name,c.town_ship_name,c.detail_address) DetailAddress, "
			+ " verify_status,verify_not_passed_reason,client_contact_phone,client_area_code,client_detail_address,c.create_time,o.client_contact_man,o.client_category_name,o.client_category_id,o.client_address,"
			+ " o.outbound_status,o.order_product_num,o.gg_diet_taboos,o.gg_customers_fond,o.gg_contact_man_id,o.gg_total_partion_num "
			+ " from t_production_manage_order o left join t_production_manage_client c on o.client_id=c.Id "
			+ START_WHERE
			+ "<choose>"
			//当search为空时要么为高级搜索要么没有搜索暂时为不搜索
			+ "<when test='orderListDTO.search == null or orderListDTO.search == &apos;&apos;'>"
			+ "<if test='orderListDTO.categoryName !=null and orderListDTO.categoryName != &apos;&apos;'> c.category_name LIKE CONCAT('%', #{orderListDTO.categoryName}, '%') </if>"
			+ "<if test='orderListDTO.contactMan !=null and orderListDTO.contactMan != &apos;&apos; '> and o.client_contact_man LIKE CONCAT('%', #{orderListDTO.contactMan}, '%')  </if>"
			+ "<if test='orderListDTO.clientName !=null and orderListDTO.clientName != &apos;&apos;'> and o.client_name LIKE CONCAT('%', #{orderListDTO.clientName}, '%')  </if>"
			+ "<if test='orderListDTO.contactPhone !=null and orderListDTO.contactPhone != &apos;&apos;'> and o.client_contact_phone LIKE CONCAT('%', #{orderListDTO.contactPhone}, '%')  </if>"
			+ "<if test='orderListDTO.provinceName !=null and orderListDTO.provinceName != &apos;&apos;'> and o.client_address LIKE CONCAT('%', #{orderListDTO.provinceName}, '%') </if>"
			+ "<if test='orderListDTO.cityName !=null and orderListDTO.cityName != &apos;&apos;'> and o.client_address LIKE CONCAT('%', #{orderListDTO.cityName}, '%')  </if>"
			+ "<if test='orderListDTO.countyName !=null and orderListDTO.countyName != &apos;&apos;'> and o.client_address LIKE CONCAT('%', #{orderListDTO.countyName}, '%')  </if>"
			+ "<if test='orderListDTO.townShipName !=null and orderListDTO.townShipName != &apos;&apos;'> and o.client_address LIKE CONCAT('%', #{orderListDTO.townShipName}, '%') </if>"

			+ "<if test='orderListDTO.orderNo !=null and orderListDTO.orderNo != &apos;&apos;'> and o.order_no = #{orderListDTO.orderNo} </if>"
			+ "<if test='orderListDTO.startDeliveryDate !=null and orderListDTO.startDeliveryDate != &apos;&apos;'> and o.delivery_date &gt;= #{orderListDTO.startDeliveryDate} </if>"
			+ "<if test='orderListDTO.endDeliveryDate !=null and orderListDTO.endDeliveryDate != &apos;&apos;'> and o.delivery_date &lt;= #{orderListDTO.endDeliveryDate} </if>"
			+ "<if test='orderListDTO.startOrderDate !=null and orderListDTO.startOrderDate != &apos;&apos;'> and o.order_date &gt;= #{orderListDTO.startOrderDate} </if>"
			+ "<if test='orderListDTO.endOrderDate !=null and orderListDTO.endOrderDate != &apos;&apos;'> and o.order_date &lt;= #{orderListDTO.endOrderDate} </if>"

			+ "<if test='orderListDTO.productName !=null and orderListDTO.productName != &apos;&apos;'> and o.product_name LIKE CONCAT('%',#{orderListDTO.productName},'%') </if>"
			+ "<if test='orderListDTO.createUserId !=null and orderListDTO.createUserId != &apos;&apos;'> and o.create_user_id = #{orderListDTO.createUserId} </if>"
			+ "<if test='orderListDTO.orderStatus !=null and orderListDTO.orderStatus != &apos;&apos;'> and o.order_status = #{orderListDTO.orderStatus} </if>"
			+ "<if test='orderListDTO.saleUserName !=null and orderListDTO.saleUserName != &apos;&apos;'> and o.sale_user_name = #{orderListDTO.saleUserName} </if>"
			+ "<if test='orderListDTO.verifyStatus !=null '> and o.verify_status = #{orderListDTO.verifyStatus} </if>"
			+ "<if test='orderListDTO.outboundStatus !=null '> and o.outbound_status = #{orderListDTO.outboundStatus} </if>"
			+ "</when>"
			//如果search不为空则普通搜索
			+ "<otherwise>"
			+ "<if test='orderListDTO.search !=null and orderListDTO.search != &apos;&apos;'>"
			+ "("
			+ " c.category_name  LIKE CONCAT('%',#{orderListDTO.search},'%')"
			+ " or o.client_contact_man  LIKE CONCAT('%',#{orderListDTO.search},'%')"
			+ " or o.client_name  LIKE CONCAT('%',#{orderListDTO.search},'%')"
			+ " or o.client_contact_man  LIKE CONCAT('%',#{orderListDTO.search},'%')"
			+ " or o.client_address  LIKE CONCAT('%',#{orderListDTO.search},'%')"
			+ " or o.order_no  LIKE CONCAT('%',#{orderListDTO.search},'%')"
			+ " or o.product_name  LIKE CONCAT('%',#{orderListDTO.search},'%')"
			+ " or o.operator_name  LIKE CONCAT('%',#{orderListDTO.search},'%')"
			+ " or o.sale_user_name  LIKE CONCAT('%',#{orderListDTO.search},'%')"
			+ ")"
			+ "</if>"
			+ "</otherwise>"
			+ "</choose>"
			+ "<if test='orderListDTO.organizationId !=null and orderListDTO.organizationId != &apos;&apos;'> and o.organization_id =#{orderListDTO.organizationId}</if>"
			+ "<if test='orderListDTO.sysId !=null and orderListDTO.sysId != &apos;&apos;'> and o.sys_id =#{orderListDTO.sysId}</if>"
			+" ${authSql}"
			+ END_WHERE
			+ " order by order_date desc"
			+ END_SCRIPT)
	IPage<ProductManageOrderListVO> pageList(Page<ProductManageOrderListVO> page, @Param("orderListDTO") ProductManageOrderListDTO orderListDTO, @Param("authSql") String authSql);

	@Select({
			"<script>",
			" select o.Id,o.order_no,o.order_type,o.delivery_date,o.product_name,o.order_weight,o.sale_user_id,o.sale_user_name,o.client_middle_address,o.gg_total_partion_num",
			",o.order_quantity,o.order_money,o.order_status,o.order_date,o.operator_name,c.category_name,c.client_name,c.ContactMan,c.ContactPhone,CONCAT_WS('',c.province_name,c.city_name,c.county_name,c.town_ship_name,c.detail_address) DetailAddress, ",
			" verify_status,verify_not_passed_reason,client_contact_phone,client_area_code,client_detail_address,c.create_time,o.client_contact_man,o.client_category_name,o.client_category_id,o.client_address," +
					"o.outbound_status,o.order_product_num,o.gg_diet_taboos,o.gg_customers_fond",
			"FROM t_production_manage_order o LEFT JOIN t_production_manage_client c ON o.client_id=c.Id ",
			"WHERE o.sys_id = #{sysId} AND o.organization_id = #{organizationId} AND o.id IN",
			"<foreach item='id' collection='ids' open='(' separator=',' close=')'>",
			"#{id}",
			"</foreach>",
			"</script>"
	})
	List<ProductManageOrderListVO> listByIds(@Param("ids") List<String> ids, @Param("sysId") String sysId, @Param("organizationId") String organizationId);

	@Select(START_SCRIPT
			+ "SELECT truncate(SUM(order_money),2) as orderMoney,truncate(SUM(received_order_money),2) as receivedorder_money,SUM(order_quantity) as orderQuantity,SUM(order_weight) as orderWeight,COUNT(*) as orderNum ,DATE_FORMAT(order_date,'%Y-%m-%d') as orderDate from t_production_manage_order   "
			+ START_WHERE
			+ "<if test='startQueryDate !=null and startQueryDate != &apos;&apos;'> and DATE_FORMAT(order_date,'%Y-%m-%d') &gt;= #{startQueryDate}</if>"
			+ "<if test='endQueryDate !=null and endQueryDate != &apos;&apos;'> and DATE_FORMAT(order_date,'%Y-%m-%d') &lt;= #{endQueryDate}</if>"
			+ "<if test='organizationId !=null and organizationId != &apos;&apos;'> and organization_id =#{organizationId}</if>"
			+ "<if test='sysId !=null and sysId != &apos;&apos;'> and sys_id =#{sysId}</if>"
			+ "<if test='saleUserId !=null and saleUserId != &apos;&apos;'> and sale_user_id =#{saleUserId}</if>"
			+ END_WHERE
			+ "GROUP BY DATE_FORMAT(order_date,'%Y-%m-%d') ORDER BY order_date desc "
			+ END_SCRIPT)
	List<Map<String, Object>> amountAndNum(SaleUserLineChartDTO dateIntervalDTO);

	@Select(START_SCRIPT
			+ "select truncate(SUM(order_money),2) as orderMoney,COUNT(*) as orderNum,sale_user_id,sale_user_name," +
			"(SELECT truncate(SUM(opr.ReceivedProMoney),2) from t_production_manage_order ino left join t_production_manage_order_product_received opr on ino.Id=opr.OrderId " +
			START_WHERE
			+ " ino.sale_user_id=o.sale_user_id "
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(ino.done_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(ino.done_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ ") as receivedorder_money"
			+ " from t_production_manage_order o "
			+ START_WHERE
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o.order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o.order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ " GROUP BY o.sale_user_id  "
			+ "<choose>"
			+ "<when  test='dateIntervalListDTO.orderField !=null and dateIntervalListDTO.orderField != &apos;&apos; and dateIntervalListDTO.orderType !=null and dateIntervalListDTO.orderType != &apos;&apos;'>"
			+ " order by ${dateIntervalListDTO.orderField} ${dateIntervalListDTO.orderType}"
			+ " </when >"
			+ " <otherwise> ORDER BY  receivedorder_money desc,orderMoney desc</otherwise>"
			+ "</choose>"
			+ END_SCRIPT)
	IPage<SaleOrderPersonRankingVO> listSaleRanking(Page<SaleOrderPersonRankingVO> page, @Param("dateIntervalListDTO") DateIntervalListDTO dateIntervalListDTO);

	@Select(START_SCRIPT
			+ "select truncate(SUM(order_money),2) as orderMoney,COUNT(*) as orderNum,sale_user_id,sale_user_name," +
			"(SELECT truncate(SUM(opr.ReceivedProMoney),2) from t_production_manage_order ino left join t_production_manage_order_product_received opr on ino.Id=opr.OrderId " +
			START_WHERE
			+ " ino.sale_user_id=o.sale_user_id "
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(ino.done_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(ino.done_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ ") as receivedorder_money"
			+ " from t_production_manage_order o "
			+ START_WHERE
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o.order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o.order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ " GROUP BY o.sale_user_id ORDER BY  receivedorder_money desc,orderMoney desc "
			+ " limit #{dateIntervalListDTO.pageSize}"
			+ END_SCRIPT)
	List<SaleOrderPersonRankingVO> listSaleRankingWithLimt(@Param("dateIntervalListDTO") DateIntervalListDTO dateIntervalListDTO);


	@Select(START_SCRIPT
			+ "SELECT truncate(SUM(order_money),2) as orderMoney,"
			+ "(SELECT truncate(SUM(opr.ReceivedProMoney),2)  from t_production_manage_order o  left join t_production_manage_order_product_received  opr on o.Id=opr.OrderId left join t_production_manage_client c on o.client_id=c.Id "
			+ START_WHERE
			+ "c.Id=oo.client_id "
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o.done_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o.done_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ ") as receivedorder_money"
			+ ",COUNT(*) as orderNum ,c.client_name clientName,c.category_name categoryName,CONCAT_WS('',ProvinceName,CityName,CountyName,TownShipName,DetailAddress) detailAddress "
			+ "from t_production_manage_order oo left join t_production_manage_client c on oo.client_id=c.Id  "
			+ START_WHERE
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> DATE_FORMAT(order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and oo.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and oo.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ "GROUP BY oo.client_id ORDER BY  receivedorder_money desc ,orderMoney desc "
			+ END_SCRIPT)
	IPage<SaleClientRankingVO> listClientOrderRank(Page<SaleClientRankingVO> page, @Param("dateIntervalListDTO") DateIntervalListDTO dateIntervalListDTO);

	@Select(START_SCRIPT
			+ "SELECT truncate(SUM(order_money),2) as orderMoney,"
			+ "(SELECT truncate(SUM(opr.ReceivedProMoney),2)  from t_production_manage_order o  left join t_production_manage_order_product_received  opr on o.Id=opr.OrderId left join t_production_manage_client c on o.client_id=c.Id "
			+ START_WHERE
			+ "c.Id=oo.client_id "
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o.done_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o.done_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ ") as receivedorder_money"
			+ ",COUNT(*) as orderNum ,c.client_name clientName,c.category_name categoryName,CONCAT_WS('',ProvinceName,CityName,CountyName,TownShipName,DetailAddress) detailAddress "
			+ "from t_production_manage_order oo left join t_production_manage_client c on oo.client_id=c.Id  "
			+ START_WHERE
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> DATE_FORMAT(order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and oo.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and oo.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ "GROUP BY oo.client_id ORDER BY  receivedorder_money desc,orderMoney desc "
			+ " limit #{dateIntervalListDTO.pageSize}"
			+ END_SCRIPT)
	List<SaleClientRankingVO> listClientOrderRankWithLimt(@Param("dateIntervalListDTO") DateIntervalListDTO dateIntervalListDTO);


	@Select(START_SCRIPT
			+ " select count(*) from ( "
			+ "SELECT COUNT(*) from t_production_manage_order  "
			+ START_WHERE
			+ "<if test='startQueryDate !=null and startQueryDate != &apos;&apos;'> DATE_FORMAT(order_date,'%Y-%m-%d') &gt;= #{startQueryDate}</if>"
			+ "<if test='endQueryDate !=null and endQueryDate != &apos;&apos;'> and DATE_FORMAT(order_date,'%Y-%m-%d') &lt;= #{endQueryDate}</if>"
			+ "<if test='organizationId !=null and organizationId != &apos;&apos;'> and organization_id =#{organizationId}</if>"
			+ "<if test='sysId !=null and sysId != &apos;&apos;'> and sys_id =#{sysId}</if>"
			+ END_WHERE
			+ "GROUP BY client_id "
			+ " ) order2 "
			+ END_SCRIPT)
	Integer orderClientSum(DateIntervalListDTO dateIntervalListDTO);

	@Select(START_SCRIPT
			+ "SELECT truncate(SUM(TotalPrice),2) as orderMoney,COUNT(*) as orderNum,p.ProductName, "
			+ "(SELECT truncate(SUM(opr.ReceivedProMoney),2)  from t_production_manage_order inner_o  left join t_production_manage_order_product_received  opr on inner_o.Id=opr.OrderId "
			+ START_WHERE
			+ "opr.ProductId=p.ProductId "
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(inner_o.done_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(inner_o.done_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and inner_o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and inner_o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ ") as receivedProMoney"
			+ " from t_production_manage_order o left join t_production_manage_order_product p on p.OrderId=o.Id  "
			+ START_WHERE
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'>  DATE_FORMAT(order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ "GROUP BY p.ProductId ORDER BY  receivedProMoney  desc,orderMoney desc "
			+ END_SCRIPT)
	IPage<SaleOrderProductRankingVO> listOrderProductRank(Page<SaleOrderProductRankingVO> page, @Param("dateIntervalListDTO") DateIntervalListDTO dateIntervalListDTO);

	@Select(START_SCRIPT
			+ "SELECT truncate(SUM(TotalPrice),2) as orderMoney,COUNT(*) as orderNum,p.ProductName, "
			+ "(SELECT truncate(SUM(opr.ReceivedProMoney),2)  from t_production_manage_order inner_o  left join t_production_manage_order_product_received  opr on inner_o.Id=opr.OrderId "
			+ START_WHERE
			+ "opr.ProductId=p.ProductId "
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(inner_o.done_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(inner_o.done_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and inner_o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and inner_o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ ") as receivedProMoney"
			+ " from t_production_manage_order o left join t_production_manage_order_product p on p.OrderId=o.Id  "
			+ START_WHERE
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'>  DATE_FORMAT(order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ "GROUP BY p.ProductId ORDER BY  receivedProMoney desc ,orderMoney desc "
			+ " limit #{dateIntervalListDTO.pageSize}"
			+ END_SCRIPT)
	List<SaleOrderProductRankingVO> listlistExcelProductRankWithLimt(@Param("dateIntervalListDTO") DateIntervalListDTO dateIntervalListDTO);

	@Select(START_SCRIPT
			+ "SELECT COUNT(*) from t_production_manage_order   "
			+ START_WHERE
			+ "<if test='startQueryDate !=null and startQueryDate != &apos;&apos;'> and DATE_FORMAT(order_date,'%Y-%m-%d') &gt;= #{startQueryDate}</if>"
			+ "<if test='endQueryDate !=null and endQueryDate != &apos;&apos;'> and DATE_FORMAT(order_date,'%Y-%m-%d') &lt;= #{endQueryDate}</if>"
			+ "<if test='organizationId !=null and organizationId != &apos;&apos;'> and organization_id =#{organizationId}</if>"
			+ "<if test='sysId !=null and sysId != &apos;&apos;'> and sys_id =#{sysId}</if>"
			+ "<if test='saleUserId !=null and saleUserId != &apos;&apos;'> and sale_user_id =#{saleUserId}</if>"
			+ END_WHERE
			+ END_SCRIPT)
	Integer countOrderNum(SaleUserLineChartDTO dateIntervalDTO);

	@Select(START_SCRIPT
			+ " select * from ("
			+ "SELECT o.Id,o.order_status orderStatus,truncate(SUM(op.TotalPrice),2) as orderMoney,truncate(SUM(op.order_weight),2) as orderWeight,truncate(SUM(op.order_quantity),2) as orderBoxQuantity,truncate(SUM(ProductNum),2) as orderQuantity,COUNT(DISTINCT o.Id) as orderNum from t_production_manage_order o left join t_production_manage_order_product op on o.Id=op.OrderId"
			+ START_WHERE
			+ " (o.order_status =2) "
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ " UNION  "
			+ " SELECT o.Id,o.order_status orderStatus,truncate(SUM(op.TotalPrice),2) as orderMoney,truncate(SUM(op.order_weight),2) as orderWeight,truncate(SUM(op.order_quantity),2) as orderBoxQuantity,truncate(SUM(ProductNum),2) as orderQuantity,COUNT(DISTINCT o.Id) as orderNum "
			+ " from t_production_manage_order o  LEFT JOIN t_production_manage_order_product op on o.Id=op.OrderId "
			+ START_WHERE
			+ " (o.order_status =3)"
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ ") a where a.order_status is not NULL"
			+ END_SCRIPT)
	IPage<OrderStatusRankingVO> orderStatusRank(Page<OrderStatusRankingVO> page, DateIntervalListDTO dateIntervalListDTO);


	@Select(START_SCRIPT
			+ " select * from ("
			+ "SELECT o.Id,o.order_status orderStatus,truncate(SUM(op.TotalPrice),2) as orderMoney,truncate(SUM(op.order_weight),2) as orderWeight,truncate(SUM(op.order_quantity),2) as orderBoxQuantity,truncate(SUM(ProductNum),2) as orderQuantity,COUNT(DISTINCT o.Id) as orderNum from t_production_manage_order o left join t_production_manage_order_product op on o.Id=op.OrderId"
			+ START_WHERE
			+ " (o.order_status =2) "
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ " UNION  "
			+ " SELECT o.Id,o.order_status orderStatus,truncate(SUM(op.TotalPrice),2) as orderMoney,truncate(SUM(op.order_weight),2) as orderWeight,truncate(SUM(op.order_quantity),2) as orderBoxQuantity,truncate(SUM(ProductNum),2) as orderQuantity,COUNT(DISTINCT o.Id) as orderNum "
			+ " from t_production_manage_order o left join t_production_manage_outbound ob on o.Id=ob.OrderId  LEFT JOIN t_production_manage_order_product op on o.Id=op.OrderId "
			+ START_WHERE
			+ " (o.order_status =3)"
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(OutboundDate,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(OutboundDate,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ ") a where a.order_status is not NULL"
			+ " limit #{dateIntervalListDTO.pageSize}"
			+ END_SCRIPT)
	List<OrderStatusRankingVO> listOrderStatusRankPageWithLimt(@Param("dateIntervalListDTO") DateIntervalListDTO dateIntervalListDTO);

	@Select(START_SCRIPT
			+ " SELECT truncate(SUM(CASE order_status WHEN 4 THEN order_money ELSE 0 END),2) doneorder_money,o.sale_user_id,o.sale_user_name," +
			"     truncate(SUM(CASE order_status WHEN 4 THEN  received_order_money ELSE 0 END),2) donereceived_order_money," +
			"     COUNT(order_status=4 or null) as doneOrderNum,"
			+ "("
			+ " SELECT IFNULL(COUNT(order_status=2 or null),0) "
			+ "from t_production_manage_order o2 "
			+ START_WHERE
			+ " o2.sale_user_id=o.sale_user_id"
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o2.order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o2.order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o2.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o2.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ ") as unDeliveryNum,"
			+ "("
			+ " SELECT IFNULL(COUNT(order_status=3 or null),0) as unReceiptNum "
			+ "from t_production_manage_order o2 "
			+ START_WHERE
			+ " o2.sale_user_id=o.sale_user_id"
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o2.order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o2.order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o2.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o2.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ ") as unReceiptNum"
			+ " from t_production_manage_order o"
			+ START_WHERE
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o.done_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o.done_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ "<if test='dateIntervalListDTO.saleUserIds !=null and dateIntervalListDTO.saleUserIds != &apos;&apos;'> and o.sale_user_id in (${dateIntervalListDTO.saleUserIds})</if>"
			+ END_WHERE
			+ "GROUP BY o.sale_user_id"
			+ END_SCRIPT)
	IPage<PersonOrderConditionVO> listPersonOrderCondition(Page<PersonOrderConditionVO> page, @Param("dateIntervalListDTO") PersonOrderConditionDTO dateIntervalListDTO);


	@Select(START_SCRIPT
			+ " SELECT truncate(SUM(CASE order_status WHEN 4 THEN order_money ELSE 0 END),2) doneorder_money,o.sale_user_id,o.sale_user_name," +
			"     truncate(SUM(CASE order_status WHEN 4 THEN  received_order_money ELSE 0 END),2) donereceived_order_money," +
			"     COUNT(order_status=4 or null) as doneOrderNum,"
			+ "("
			+ " SELECT IFNULL(COUNT(order_status=2 or null),0) "
			+ "from t_production_manage_order o2 "
			+ START_WHERE
			+ " o2.sale_user_id=o.sale_user_id"
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o2.order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o2.order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o2.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o2.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ ") as unDeliveryNum,"
			+ "("
			+ " SELECT IFNULL(COUNT(order_status=3 or null),0) as unReceiptNum "
			+ "from t_production_manage_order o2 "
			+ START_WHERE
			+ " o2.sale_user_id=o.sale_user_id"
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o2.order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o2.order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o2.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o2.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ ") as unReceiptNum"
			+ " from t_production_manage_order o"
			+ START_WHERE
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o.done_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o.done_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ "<if test='dateIntervalListDTO.saleUserIds !=null and dateIntervalListDTO.saleUserIds != &apos;&apos;'> and o.sale_user_id in (${dateIntervalListDTO.saleUserIds})</if>"
			+ END_WHERE
			+ "GROUP BY o.sale_user_id"
			+ END_SCRIPT)
	List<PersonOrderConditionVO> listPersonOrderConditionLimit(@Param("dateIntervalListDTO") PersonOrderConditionDTO dateIntervalListDTO);


	@Select(START_SCRIPT
			+ " SELECT COUNT(order_status=2 or null) as unDeliveryNum,COUNT(order_status=3 or null) as unReceiptNum "
			+ "from t_production_manage_order o2 "
			+ START_WHERE
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o2.order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o2.order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o2.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o2.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ END_SCRIPT)
	PersonOrderConditionVO wholeOrderUnDoneCondition(@Param("dateIntervalListDTO") DateIntervalListDTO dateIntervalDTO);

	@Select(START_SCRIPT
			+ "SELECT truncate(SUM(CASE order_status WHEN 4 THEN order_money ELSE 0 END),2) doneorder_money,truncate(SUM(CASE order_status WHEN 4 THEN  received_order_money ELSE 0 END),2) donereceived_order_money,COUNT(order_status=4 or null) as doneOrderNum "
			+ "from t_production_manage_order o1"
			+ START_WHERE
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o1.done_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o1.done_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o1.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o1.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ END_SCRIPT)
	PersonOrderConditionVO wholeOrderDoneCondition(@Param("dateIntervalListDTO") DateIntervalListDTO dateIntervalDTO);

	@Select(START_SCRIPT
			+ "SELECT  COUNT(DISTINCT client_id) clientNum ,COUNT(*) orderNum,truncate(SUM(order_money),2) orderMoney,delivery_type  deliveryType "
			+ "from t_production_manage_order o1"
			+ START_WHERE
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o1.order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o1.order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o1.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o1.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ "GROUP BY o1.delivery_type"
			+ END_SCRIPT)
	IPage<OrderDeliveryTypeRankingVO> orderdeliveryTypeRank(Page<OrderDeliveryTypeRankingVO> page, DateIntervalListDTO dateIntervalListDTO);

	@Select(START_SCRIPT
			+ "SELECT  truncate(SUM(o1.received_order_money),2) "
			+ "from t_production_manage_order o1 "
			+ START_WHERE
			+ "<if test='deliveryType !=null '> and o1.delivery_type =#{deliveryType}</if>"
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o1.done_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o1.done_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o1.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o1.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ END_SCRIPT)
	BigDecimal sumReceivedOrderMoneyByDeliveryType(@Param("dateIntervalListDTO") DateIntervalListDTO dateIntervalListDTO, @Param("deliveryType") String deliveryType);

	@Select(START_SCRIPT
			+ " SELECT truncate(SUM(CASE order_status WHEN 4 THEN order_money ELSE 0 END),2) doneorder_money,o.sale_user_id,o.sale_user_name," +
			"     truncate(SUM(CASE order_status WHEN 4 THEN  received_order_money ELSE 0 END),2) donereceived_order_money," +
			"     COUNT(order_status=4 or null) as doneOrderNum,"
			+ "("
			+ " SELECT IFNULL(COUNT(order_status=2 or null),0) "
			+ "from t_production_manage_order o2 "
			+ START_WHERE
			+ " o2.sale_user_id=o.sale_user_id"
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o2.order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o2.order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o2.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o2.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ ") as unDeliveryNum,"
			+ "("
			+ " SELECT IFNULL(COUNT(order_status=3 or null),0) as unReceiptNum "
			+ "from t_production_manage_order o2 "
			+ START_WHERE
			+ " o2.sale_user_id=o.sale_user_id"
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o2.order_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o2.order_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o2.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o2.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ ") as unReceiptNum"
			+ " from t_production_manage_order o"
			+ START_WHERE
			+ "<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and DATE_FORMAT(o.done_date,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(o.done_date,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+ "<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and o.organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+ "<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and o.sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+ "GROUP BY o.sale_user_id"
			+ " order by donereceived_order_money desc,doneorder_money desc limit 1"
			+ END_SCRIPT)
	PersonOrderConditionVO personOrderConditionBestSaleUser(@Param("dateIntervalListDTO") DateIntervalListDTO dateIntervalListDTO);

	/**
	 * 未完成订单的待回款列表
	 *
	 * @param page
	 * @param queryWrapper
	 * @return
	 */
	@Select(START_SCRIPT
			+ "SELECT  order_no,order_money,sale_user_name,client_name,received_order_money,client_address,order_money-received_order_money as unPayBackMoney"
			+ " from t_production_manage_order  "
			+"${ew.customSqlSegment}"
			+ END_SCRIPT)
	IPage<ProductManageOrderUnPayBackListVO> orderPaybackPageList(Page<ProductionManageSaleProductDataListVO> page, @Param(Constants.WRAPPER) QueryWrapper<ProductionManageOrder> queryWrapper);

	@Select("select DATE_FORMAT(order_date,'%Y-%m-%d') orderDate, ProductId, p.ProductName, SUM(order_money) orderMoney, SUM(received_order_money) as receivedorder_money from t_production_manage_order o \n" +
			"join t_production_manage_order_product p on o.Id = p.OrderId\n" +
			" ${ew.customSqlSegment}" +
			"group by  DATE_FORMAT(order_date,'%Y-%m-%d'), ProductId ")
	List<ProductionManageSaleProductDayStatistics> selectSaleProduct(@Param(Constants.WRAPPER) Wrapper queryWrapper);

	@Select("select DATE_FORMAT(order_date,'%Y-%m-%d') orderDate, order_status, count(*) orderNum from t_production_manage_order o \n" +
			" ${ew.customSqlSegment}" +
			"group by  DATE_FORMAT(order_date,'%Y-%m-%d'), order_status \n" +
			"union all \n" +
			"select DATE_FORMAT(order_date,'%Y-%m-%d') orderDate, order_status, count(*) orderNum from t_production_manage_order o " +
			" ${ew.customSqlSegment}" +
			" and o.verify_status=0\n" +
			"group by  DATE_FORMAT(order_date,'%Y-%m-%d'), order_status \n")
	List<ProductionManageSaleOrderStatusDayStatistics> selectSaleOrderList(@Param(Constants.WRAPPER) Wrapper queryWrapper);


	@Select("SELECT op.ProductId as productId, op.ProductName as productName, o.sale_user_id AS salesPersonnelId, o.sale_user_name AS salesPersonnelName, " +
			"SUM(IFNULL(op.TotalPrice, 0)) AS actualSaleAmount, DATE_FORMAT(o.order_date,'%Y-%m-%d') AS saleTargetDate FROM t_production_manage_order_product op " +
			"INNER JOIN t_production_manage_order o ON o.Id = op.OrderId ${ew.customSqlSegment}")
	List<ProductionManageStatisticsSaleTargetData> listByStartAndEndDate(@Param(Constants.WRAPPER) Wrapper queryWrapper);

	/**
	 * 统计客户类目订单数据
	 *
	 * @param page
	 * @param orderQueryWrapper
	 * @return
	 */
	@Select("SELECT client_category_name , " +
			" truncate(SUM(IFNULL(order_money, 0)),2) AS orderMoney FROM t_production_manage_order " +
			" ${ew.customSqlSegment}")
	IPage<GuGengOrderClientCategoryListVO> statisticsOrderClientCategoryData(Page<GuGengOrderClientCategoryListVO> page, @Param(Constants.WRAPPER) QueryWrapper<ProductionManageOrder> orderQueryWrapper);

	/**
	 * 根据时间段内统计销售额
	 * @param orderQueryWrapper
	 * @return
	 */
	@Select("SELECT truncate(SUM(IFNULL(order_money, 0)),2)  FROM t_production_manage_order " +
			" ${ew.customSqlSegment}")
	BigDecimal sumOrderMoneyDuringDataInterval(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageOrder> orderQueryWrapper);

	@Select("SELECT client_category_name , " +
			" truncate(SUM(IFNULL(order_money, 0)),2) AS orderMoney FROM t_production_manage_order " +
			" ${ew.customSqlSegment}")
	List<GuGengOrderClientCategoryListVO> listOrderClientCategoryData(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageOrder> orderQueryWrapper);
}
