package com.zxs.server.mapper.gugeng.salemanage;

import net.app315.hydra.intelligent.planting.common.gugeng.util.template.TemplateEntity;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClientCategory;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ClientCategoryMapper extends CommonSql<ProductionManageClientCategory> {
	@Select(START_SCRIPT
			+"select * from t_production_manage_client_category "
			+ START_WHERE
			+ " <if test='categoryName !=null and categoryName != &apos;&apos; '> category_name = #{categoryName} </if> "
			+ " <if test='organizationId !=null and organizationId != &apos;&apos; '> and organization_id = #{organizationId} </if> "
			+ " <if test='sysId !=null and sysId != &apos;&apos; '> and sys_id = #{sysId} </if> "
			+ " <if test='id !=null '> and Id != #{id} </if> "
			+ END_WHERE
			+ END_SCRIPT
	)
	ProductionManageClientCategory selectByNameInOtherSysId(@Param("categoryName") String categoryName, @Param("id") Long id, @Param("organizationId") String organizationId, @Param("sysId") String sysId);

	@Select(START_SCRIPT
			+"select * from t_production_manage_client_category "
			+ START_WHERE
			+"<choose>"
			+" <when test='direction >0'>"
			+"  sort_weight &lt; #{sortWeight}"
			+" </when > "
			+"<otherwise>  "
			+"  sort_weight &gt; #{sortWeight}"
			+"</otherwise>"
			+"</choose>  "
			+ " <if test='organizationId !=null and organizationId != &apos;&apos; '> and  organization_id = #{organizationId} </if> "
			+ " <if test='sysId !=null and sysId != &apos;&apos; '> and sys_id = #{sysId} </if> "
			+ END_WHERE
			+" order by sort_weight asc"
			+" limit #{moveStep}"
			+ END_SCRIPT
	)
	List<ProductionManageClientCategory> selectMoveStep(@Param("sortWeight") int sortWeight, @Param("moveStep") int moveStep, @Param("direction") int direction, @Param("organizationId") String organizationId, @Param("sysId") String sysId);

	@Select(START_SCRIPT
			+"select * from t_production_manage_client_category "
			+ START_WHERE
			+ " <if test='organizationId !=null and organizationId != &apos;&apos; '> organization_id = #{organizationId} </if> "
			+ " <if test='sysId !=null and sysId != &apos;&apos; '> and sys_id = #{sysId} </if> "
			+ END_WHERE
			+" order by sort_weight asc"
			+ END_SCRIPT
	)
	List<ProductionManageClientCategory> selectAndSortByOrgIdAndSysId(@Param("organizationId") String organizationId, @Param("sysId") String sysId);

	/**
	 * 通过分类名称来获取分类列表
	 * @return
	 */
	@Select(START_SCRIPT
			+"select CategoryName, Id from production_manage_client_category "
			+START_WHERE
			+ " <if test='organizationId !=null and organizationId != &apos;&apos; '> OrganizationId = #{organizationId} </if> "
			+ " <if test='sysId !=null and sysId != &apos;&apos; '> and SysId = #{sysId} </if> "
			+END_WHERE
			+END_SCRIPT
	)
	List<TemplateEntity> listByCategoryNames(@Param("organizationId") String organizationId, @Param("sysId") String sysId);

}
