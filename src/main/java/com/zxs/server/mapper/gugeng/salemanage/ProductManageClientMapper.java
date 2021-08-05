package com.zxs.server.mapper.gugeng.salemanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.ProductManageClientListDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClient;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageCustomerNumDayStatistics;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductManageClientListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.PotentialClientRankingVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageSaleClientNumStatisticsVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ProductManageClientMapper extends CommonSql<ProductionManageClient> {
    
	@Select(START_SCRIPT
			+"select c.Id,c.client_name,contact_man,contact_phone,CONCAT_WS('',province_name,city_name,county_name,town_ship_name,detail_address) detail_address,category_name,del_status,area_code, " +
			"fo_estimate_sales ,DATE_FORMAT(estimate_sales_date,'%Y-%m-%d') estimate_sales_date,c.sale_user_id,c.sale_user_name,client_type,truncate(SUM(order_money),2) as orderMoney,COUNT(*) as orderNum,"
			+"c.create_time as createDate,c.gg_customers_fond,c.gg_diet_taboos,c.gg_birth_day"
			+" from t_production_manage_client c left join t_production_manage_order o on o.client_id= c.Id"
			+ START_WHERE
			   +	"<choose>" 
					//当search为空时要么为高级搜索要么没有搜索暂时为不搜索
					+"<when test='clientListDTO.search == null or clientListDTO.search == &apos;&apos;'>" 
					  +"<if test='clientListDTO.categoryName !=null and clientListDTO.categoryName != &apos;&apos;'>  category_name LIKE CONCAT('%', #{clientListDTO.categoryName}, '%') </if>"
					  +"<if test='clientListDTO.contactMan !=null and clientListDTO.contactMan != &apos;&apos; '>  and contact_man LIKE CONCAT('%', #{clientListDTO.contactMan}, '%') </if>"
					  +"<if test='clientListDTO.clientName !=null and clientListDTO.clientName != &apos;&apos;'> and c.client_name LIKE CONCAT('%', #{clientListDTO.clientName}, '%') </if>"
					  +"<if test='clientListDTO.contactPhone !=null and clientListDTO.contactPhone != &apos;&apos;'> and contact_phone LIKE CONCAT('%', #{clientListDTO.contactPhone}, '%') </if>"
					  +"<if test='clientListDTO.provinceName !=null and clientListDTO.provinceName != &apos;&apos;'> and province_name LIKE CONCAT('%', #{clientListDTO.provinceName}, '%') </if>"
					  +"<if test='clientListDTO.cityName !=null and clientListDTO.cityName != &apos;&apos;'> and city_name LIKE CONCAT('%', #{clientListDTO.cityName}, '%') </if>"
					  +"<if test='clientListDTO.countyName !=null and clientListDTO.countyName != &apos;&apos;'> and county_name LIKE CONCAT('%', #{clientListDTO.countyName}, '%') </if>"
					  +"<if test='clientListDTO.townShipName !=null and clientListDTO.townShipName != &apos;&apos;'> and town_ship_name LIKE CONCAT('%', #{clientListDTO.townShipName}, '%') </if>"
					  +"<if test='clientListDTO.startEstimateSalesDate !=null and clientListDTO.startEstimateSalesDate != &apos;&apos;'> and estimate_sales_date &gt;= #{clientListDTO.startEstimateSalesDate}</if>"
					  +"<if test='clientListDTO.endEstimateSalesDate !=null and clientListDTO.endEstimateSalesDate != &apos;&apos;'> and estimate_sales_date &lt;= #{clientListDTO.endEstimateSalesDate} </if>"
					  +"<if test='clientListDTO.startOrderDate !=null and clientListDTO.startOrderDate != &apos;&apos;'> and order_date &gt;= #{clientListDTO.startOrderDate}</if>"
					  +"<if test='clientListDTO.endOrderDate !=null and clientListDTO.endOrderDate != &apos;&apos;'> and order_date &lt;= #{clientListDTO.endOrderDate} </if>"
					  +"<if test='clientListDTO.saleUserId !=null and clientListDTO.saleUserId != &apos;&apos;'> and c.sale_user_id =#{clientListDTO.saleUserId} </if>"
					  +"</when>" 
					//如果search不为空则普通搜索
					+"<otherwise>" 
					  +"<if test='clientListDTO.search !=null and clientListDTO.search != &apos;&apos;'>" 
					     +"("
					     + "category_name  LIKE CONCAT('%',#{clientListDTO.search},'%')"
					     + " or contact_man  LIKE CONCAT('%',#{clientListDTO.search},'%')"
					     + " or c.client_name  LIKE CONCAT('%',#{clientListDTO.search},'%')"
					     + " or contact_phone  LIKE CONCAT('%',#{clientListDTO.search},'%')"
					     + " or province_name  LIKE CONCAT('%',#{clientListDTO.search},'%')"
					     + " or city_name  LIKE CONCAT('%',#{clientListDTO.search},'%')"
					     + " or county_name  LIKE CONCAT('%',#{clientListDTO.search},'%')"
					     + " or town_ship_name  LIKE CONCAT('%',#{clientListDTO.search},'%')"
					     + ")"
						 +"</if>"
					+"</otherwise>" 
			  +"</choose>"
			  +"<if test='clientListDTO.clientType !=null'> and client_type =#{clientListDTO.clientType}</if>"
			  +"<if test='clientListDTO.categoryId !=null'> and category_id =#{clientListDTO.categoryId}</if>"
			  +"<if test='clientListDTO.organizationId !=null and clientListDTO.organizationId != &apos;&apos;'> and c.organization_id =#{clientListDTO.organizationId}</if>"
			  +"<if test='clientListDTO.sysId !=null and clientListDTO.sysId != &apos;&apos;'> and c.sys_id =#{clientListDTO.sysId}</if>"
			  +" and del_status=0"
			  +" ${sql}"
			+ END_WHERE
			+" group by c.Id"
			+" order by c.create_time desc"
			+ END_SCRIPT
			)
	IPage<ProductManageClientListVO> list(Page<ProductManageClientListVO> page, @Param("clientListDTO") ProductManageClientListDTO clientListDTO, @Param("sql") String roleFunAuthSqlByAuthCode);


	@Select(START_SCRIPT
			+"select c.Id,c.client_name,contact_man,contact_phone,CONCAT_WS('',province_name,city_name,county_name,town_ship_name,detail_address) detail_address,category_name,del_status,area_code, " +
			"fo_estimate_sales ,DATE_FORMAT(estimate_sales_date,'%Y-%m-%d') estimate_sales_date,c.sale_user_id,c.sale_user_name,client_type,truncate(SUM(order_money),2) as orderMoney,COUNT(*) as orderNum,"
			+"c.create_time,c.gg_customers_fond,c.gg_diet_taboos"
			+" from t_production_manage_client c left join t_production_manage_order o on o.client_id= c.Id"
			+ START_WHERE
			+"c.Id in "
			+"<foreach item='id' collection='ids' open='(' separator=',' close=')'>"
			+"#{id}"
			+"</foreach>"
			+"<if test='organizationId !=null and organizationId != &apos;&apos;'> and c.organization_id =#{organizationId}</if>"
			+"<if test='sysId !=null and sysId != &apos;&apos;'> and c.sys_id =#{sysId}</if>"
			+ END_WHERE
			+" group by c.Id"
			+" order by c.Id desc"
			+ END_SCRIPT
	)
	List<ProductManageClientListVO> listByIds(@Param("ids") List<String> ids, @Param("sysId") String sysId, @Param("organizationId") String organizationId);


	@Select(START_SCRIPT
			+"select c.id,client_name,a.contact_man,a.contact_phone, a.detail_address,category_name,del_status,a.area_code,a.Id as ggContactManId, " +
			"fo_estimate_sales ,DATE_FORMAT(estimate_sales_date,'%Y-%m-%d') estimate_sales_date,c.sale_user_id,c.sale_user_name,client_type,c.gg_customers_fond,c.gg_diet_taboos"
			+" from t_production_manage_client c LEFT JOIN (SELECT * from t_gu_geng_contact_man GROUP BY client_id ORDER BY client_id)a on c.Id=a.client_id "
			+START_WHERE
			   +	"<choose>"
					//当search为空时要么为高级搜索要么没有搜索暂时为不搜索
					+"<when test='clientListDTO.search == null or clientListDTO.search == &apos;&apos;'>"
					+"</when>"
					//如果search不为空则普通搜索
					+"<otherwise>"
					  +"<if test='clientListDTO.search !=null and clientListDTO.search != &apos;&apos;'>"
					     + "client_name  LIKE CONCAT('%',#{clientListDTO.search},'%')"
					  +"</if>"
					+"</otherwise>"
			  +"</choose>"
			  +"<if test='clientListDTO.clientType !=null'> and client_type =#{clientListDTO.clientType}</if>"
			  +"<if test='clientListDTO.categoryId !=null'> and category_id =#{clientListDTO.categoryId}</if>"
			  +"<if test='clientListDTO.organizationId !=null and clientListDTO.organizationId != &apos;&apos;'> and c.organization_id =#{clientListDTO.organizationId}</if>"
			  +"<if test='clientListDTO.sysId !=null and clientListDTO.sysId != &apos;&apos;'> and c.sys_id =#{clientListDTO.sysId}</if>"
			  +" and del_status=0"
			+ END_WHERE
			+" order by c.create_time desc"
			+ END_SCRIPT
			)
	IPage<ProductManageClientListVO> dropPage(Page<ProductManageClientListVO> page,
                                              @Param("clientListDTO") ProductManageClientListDTO clientListDTO);

	@Select(START_SCRIPT
			+"select sale_user_name,count(*) as clientNum"
			+" from t_production_manage_client  "
			+ START_WHERE
			+" client_type=0 "
			+"<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and  DATE_FORMAT(create_time,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+"<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(create_time,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+"<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+"<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+" group by sale_user_id"
			+" order by clientNum desc"
			+ END_SCRIPT)
    IPage<PotentialClientRankingVO> listPotentialclientRank(Page<PotentialClientRankingVO> page, @Param("dateIntervalListDTO") DateIntervalListDTO dateIntervalListDTO);


	@Select(START_SCRIPT
			+"select sale_user_name,count(*) as clientNum"
			+" from t_production_manage_client "
			+ START_WHERE
			+" client_type=0 "
			+"<if test='dateIntervalListDTO.startQueryDate !=null and dateIntervalListDTO.startQueryDate != &apos;&apos;'> and  DATE_FORMAT(create_time,'%Y-%m-%d') &gt;= #{dateIntervalListDTO.startQueryDate}</if>"
			+"<if test='dateIntervalListDTO.endQueryDate !=null and dateIntervalListDTO.endQueryDate != &apos;&apos;'> and DATE_FORMAT(create_time,'%Y-%m-%d') &lt;= #{dateIntervalListDTO.endQueryDate}</if>"
			+"<if test='dateIntervalListDTO.organizationId !=null and dateIntervalListDTO.organizationId != &apos;&apos;'> and organization_id =#{dateIntervalListDTO.organizationId}</if>"
			+"<if test='dateIntervalListDTO.sysId !=null and dateIntervalListDTO.sysId != &apos;&apos;'> and sys_id =#{dateIntervalListDTO.sysId}</if>"
			+ END_WHERE
			+" group by sale_user_id"
			+" order by clientNum desc"
			+" limit #{dateIntervalListDTO.pageSize}"
			+ END_SCRIPT)
	List<PotentialClientRankingVO> listPotentialclientRankWithLimt(@Param("dateIntervalListDTO") DateIntervalListDTO dateIntervalListDTO);

	@Select(START_SCRIPT
			+"select count(DISTINCT o.client_id) as clientNum,DATE_FORMAT(o.order_date,'%Y-%m-%d') orderDate"
			+" FROM `t_production_manage_client` c LEFT JOIN t_production_manage_order o on c.Id=o.client_id "
			+"${ew.customSqlSegment}"
			+ END_SCRIPT)
    List<Map<String, Object>> orderClientNum(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageClient> clientQueryWrapper);

	@Select(START_SCRIPT
			+"select count(c.Id) as clientNum,DATE_FORMAT(c.create_time,'%Y-%m-%d') createDate"
			+" FROM `t_production_manage_client` c "
			+"${ew.customSqlSegment}"
			+ END_SCRIPT)
	List<Map<String, Object>> potentialClientNum(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageClient> clientQueryWrapper);

	@Select("select DATE_FORMAT(create_time,'%Y-%m-%d') create_time, client_type, count(*) CustomerNum\n" +
			"from t_production_manage_client \n" +
			" ${ew.customSqlSegment}" +
			"group by  DATE_FORMAT(create_time,'%Y-%m-%d') , client_type")
	List<ProductionManageCustomerNumDayStatistics> selectClientList(@Param(Constants.WRAPPER) Wrapper queryWrapper);

	/**
	 * 数据统计-潜在客户与订单客户转化率
	 * @param page
	 * @param queryWrapper
	 * @return
	 */
	@Select(START_SCRIPT
			+"select saleUserName,sale_user_id,count(if(client_type=0,1,0) or null )PotentialClientNum,count(if(client_type=1,1,0) or null)OrderClientNum from t_production_manage_client" +
			" ${ew.customSqlSegment}"
			+ END_SCRIPT
	)
	IPage<ProductionManageSaleClientNumStatisticsVO> clientConversionPage(Page<ProductionManageSaleClientNumStatisticsVO> page, @Param(Constants.WRAPPER) QueryWrapper<ProductionManageClient> queryWrapper);

	@Select(START_SCRIPT
			+"select c.Id,client_name,cm.contact_man,cm.contact_phone,cm.detail_address,category_name,del_status,cm.area_code, "
			+" fo_estimate_sales ,DATE_FORMAT(estimate_sales_date,'%Y-%m-%d') estimate_sales_date,c.sale_user_id,c.sale_user_name,client_type,c.gg_customers_fond,c.gg_diet_taboos"
			+" from t_production_manage_client c left join t_gu_geng_contact_man cm on c.Id=cm.client_id" +
			" ${ew.customSqlSegment}"
			+ END_SCRIPT
	)
    ProductionManageClient selectExistClient(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageClient> queryWrapper);

	@Select(START_SCRIPT
			+"select c.Id,c.client_name,contact_man,contact_phone,CONCAT_WS('',province_name,city_name,county_name,town_ship_name) location,detail_address,category_name,del_status,area_code, " +
			"fo_estimate_sales ,DATE_FORMAT(estimate_sales_date,'%Y-%m-%d') estimate_sales_date,c.sale_user_id,c.sale_user_name,client_type,truncate(SUM(order_money),2) as orderMoney,COUNT(*) as orderNum,"
			+"c.create_time,c.gg_customers_fond,c.gg_diet_taboos,c.gg_birth_day"
			+" from t_production_manage_client c left join t_production_manage_order o on o.client_id= c.Id"
			+ START_WHERE
			+	"<choose>"
			//当search为空时要么为高级搜索要么没有搜索暂时为不搜索
			+"<when test='clientListDTO.search == null or clientListDTO.search == &apos;&apos;'>"
			+"<if test='clientListDTO.categoryName !=null and clientListDTO.categoryName != &apos;&apos;'>  category_name LIKE CONCAT('%', #{clientListDTO.categoryName}, '%') </if>"
			+"<if test='clientListDTO.contactMan !=null and clientListDTO.contactMan != &apos;&apos; '>  and contact_man LIKE CONCAT('%', #{clientListDTO.contactMan}, '%') </if>"
			+"<if test='clientListDTO.clientName !=null and clientListDTO.clientName != &apos;&apos;'> and c.client_name LIKE CONCAT('%', #{clientListDTO.clientName}, '%') </if>"
			+"<if test='clientListDTO.contactPhone !=null and clientListDTO.contactPhone != &apos;&apos;'> and contact_phone LIKE CONCAT('%', #{clientListDTO.contactPhone}, '%') </if>"
			+"<if test='clientListDTO.provinceName !=null and clientListDTO.provinceName != &apos;&apos;'> and province_name LIKE CONCAT('%', #{clientListDTO.provinceName}, '%') </if>"
			+"<if test='clientListDTO.cityName !=null and clientListDTO.cityName != &apos;&apos;'> and city_name LIKE CONCAT('%', #{clientListDTO.cityName}, '%') </if>"
			+"<if test='clientListDTO.countyName !=null and clientListDTO.countyName != &apos;&apos;'> and county_name LIKE CONCAT('%', #{clientListDTO.countyName}, '%') </if>"
			+"<if test='clientListDTO.townShipName !=null and clientListDTO.townShipName != &apos;&apos;'> and town_ship_name LIKE CONCAT('%', #{clientListDTO.townShipName}, '%') </if>"
			+"<if test='clientListDTO.startEstimateSalesDate !=null and clientListDTO.startEstimateSalesDate != &apos;&apos;'> and estimate_sales_date &gt;= #{clientListDTO.startEstimateSalesDate}</if>"
			+"<if test='clientListDTO.endEstimateSalesDate !=null and clientListDTO.endEstimateSalesDate != &apos;&apos;'> and estimate_sales_date &lt;= #{clientListDTO.endEstimateSalesDate} </if>"
			+"<if test='clientListDTO.startOrderDate !=null and clientListDTO.startOrderDate != &apos;&apos;'> and order_date &gt;= #{clientListDTO.startOrderDate}</if>"
			+"<if test='clientListDTO.endOrderDate !=null and clientListDTO.endOrderDate != &apos;&apos;'> and order_date &lt;= #{clientListDTO.endOrderDate} </if>"
			+"<if test='clientListDTO.saleUserId !=null and clientListDTO.saleUserId != &apos;&apos;'> and c.sale_user_id =#{clientListDTO.saleUserId} </if>"
			+"</when>"
			//如果search不为空则普通搜索
			+"<otherwise>"
			+"<if test='clientListDTO.search !=null and clientListDTO.search != &apos;&apos;'>"
			+"("
			+ "category_name  LIKE CONCAT('%',#{clientListDTO.search},'%')"
			+ " or contact_man  LIKE CONCAT('%',#{clientListDTO.search},'%')"
			+ " or c.client_name  LIKE CONCAT('%',#{clientListDTO.search},'%')"
			+ " or contact_phone  LIKE CONCAT('%',#{clientListDTO.search},'%')"
			+ " or province_name  LIKE CONCAT('%',#{clientListDTO.search},'%')"
			+ " or city_name  LIKE CONCAT('%',#{clientListDTO.search},'%')"
			+ " or county_name  LIKE CONCAT('%',#{clientListDTO.search},'%')"
			+ " or town_ship_name  LIKE CONCAT('%',#{clientListDTO.search},'%')"
			+ ")"
			+"</if>"
			+"</otherwise>"
			+"</choose>"
			+"<if test='clientListDTO.clientType !=null'> and client_type =#{clientListDTO.clientType}</if>"
			+"<if test='clientListDTO.categoryId !=null'> and category_id =#{clientListDTO.categoryId}</if>"
			+"<if test='clientListDTO.organizationId !=null and clientListDTO.organizationId != &apos;&apos;'> and c.organization_id =#{clientListDTO.organizationId}</if>"
			+"<if test='clientListDTO.sysId !=null and clientListDTO.sysId != &apos;&apos;'> and c.sys_id =#{clientListDTO.sysId}</if>"
			+" and del_status=0"
			+" ${sql}"
			+ END_WHERE
			+" group by c.Id"
			+" order by c.create_time desc"
			+ END_SCRIPT
	)
	IPage<ProductManageClientListVO> mobilePage(Page<ProductManageClientListVO> page, @Param("clientListDTO") ProductManageClientListDTO clientListDTO, @Param("sql") String roleFunAuthSqlByAuthCode);

	/**
	 * 统计一段时间的订单客户数
	 * @param clientQueryWrapper
	 * @return
	 */
	@Select(START_SCRIPT
			+"select COUNT(DISTINCT client_id)"
			+" FROM `t_production_manage_client` c LEFT JOIN t_production_manage_order o on c.Id=o.client_id "
			+"${ew.customSqlSegment}"
			+ END_SCRIPT)
	Integer countOrderClientNum(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageClient> clientQueryWrapper);
}