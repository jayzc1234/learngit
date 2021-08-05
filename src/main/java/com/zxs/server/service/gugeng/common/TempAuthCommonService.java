package com.zxs.server.service.gugeng.common;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.annotation.gugeng.CreateUserTag;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.trace.EmployeeMsgDTO;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.TempAuthMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.asm.ClassReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedIOException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.AnnotationMetadataReadingVisitor;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zc
 */
@Service
@Slf4j
public class TempAuthCommonService extends CommonUtil {

	@Autowired
	private TempAuthMapper authMapper;

	private static Map<String,String> departmentIdMap=new HashMap<>();

	private static AnnotationMetadataReadingVisitor visitor = new AnnotationMetadataReadingVisitor(TempAuthCommonService.class.getClassLoader());


    public void batchUpdateAuthDepartmentId() throws IOException {
		PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath*:com/jgw/supercodeplatform/productmanagement/pojo/**/*.class");
		if (null==resources || resources.length==0){
			CommonUtil.throwSuperCodeExtException(500,"无法获取实体类");
		}
		for (Resource resource : resources) {
			InputStream is = new BufferedInputStream(resource.getInputStream());
			ClassReader classReader;
			try {
				classReader = new ClassReader(is);
			}
			catch (IllegalArgumentException ex) {
				throw new NestedIOException("解析错误：ASM ClassReader failed to parse class file - " +
						"probably due to a new Java class file version that isn't supported yet: " + resource, ex);
			}
			finally {
				is.close();
			}
			classReader.accept(visitor, ClassReader.SKIP_DEBUG);
			String classWithPackageName = visitor.getClassName();
			try {
				Class<?> aClass = Class.forName(classWithPackageName);
				Field[] declaredFields = aClass.getDeclaredFields();
				for (Field declaredField : declaredFields) {
					CreateUserTag annotation = declaredField.getAnnotation(CreateUserTag.class);
					if (null!=annotation){
						String createUserIdField = declaredField.getName();
						TableField tableField = declaredField.getAnnotation(TableField.class);
						if (null!=tableField){
							String value = tableField.value();
							if (StringUtils.isNotBlank(value)){
								createUserIdField=value;
							}
						}
						String className = resource.getFilename().replace(".class","");
                        String tableName=transferToTableName(className);
                        String sql="SELECT DISTINCT "+createUserIdField+" from "+tableName;
						List<String> createUserIds = authMapper.select(sql);
                        if (null==createUserIds || createUserIds.isEmpty()){
                        	continue;
						}
                        StringBuilder updateBuilder=new StringBuilder();
						for (String createUserId : createUserIds) {
							String departmentId = departmentIdMap.get(createUserId);
							if (null==departmentId){
								EmployeeMsgDTO employeeMsg = getEmployeeMsg(getSuperToken(), createUserId);
								if (null!=employeeMsg && StringUtils.isNotBlank(employeeMsg.getDepartmentId())){
									departmentIdMap.put(createUserId,employeeMsg.getDepartmentId());
									departmentId=employeeMsg.getDepartmentId();
								}
							}
							if (StringUtils.isNotBlank(departmentId)){
								String updateSql = "update  " + tableName + " set auth_department_id='" + departmentId + "' where " + createUserIdField + "='" + createUserId + "';";
								updateBuilder.append(updateSql);
								authMapper.update(updateSql);
							}
						}
						if (updateBuilder.length()>0){
						}
					}
				}
			}catch (Exception e){
				CommonUtil.throwSuperCodeExtException(500,e.getLocalizedMessage());
			}
			classReader=null;
		}
	}

	private static String transferToTableName(String className) {
    	if("ProductionManageWeight".equals(className)) {
            return "production_manage_weighting";
        }

		char[] chars = className.toCharArray();
		StringBuilder builder=new StringBuilder();
		boolean isFirst=true;
		for (char aChar : chars) {
			if(aChar >= 97 && aChar <= 122) {
				builder.append(aChar);
			}else {
				if (!isFirst){
					builder.append("_");
				}
				isFirst=false;
				builder.append(Character.toLowerCase(aChar));
			}
		}
        return builder.toString();
	}

//	public static void main(String[] args) throws IOException {
//
//		String  tableName=transferToTableName("ProductionManageHarvestDamage");
//		System.out.println(tableName);
//		PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
//		Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath*:com/jgw/supercodeplatform/productmanagement/pojo/**/*.class");
//		if (null==resources || resources.length==0){
//			CommonUtil.throwSuperCodeExtException(500,"无法获取实体类");
//		}
//		for (Resource resource : resources) {
//			InputStream is = new BufferedInputStream(resource.getInputStream());
//			ClassReader classReader;
//			try {
//				classReader = new ClassReader(is);
//			}
//			catch (IllegalArgumentException ex) {
//				throw new NestedIOException("解析错误：ASM ClassReader failed to parse class file - " +
//						"probably due to a new Java class file version that isn't supported yet: " + resource, ex);
//			}
//			finally {
//				is.close();
//			}
//
//			classReader.accept(visitor, ClassReader.SKIP_DEBUG);
//			String className = visitor.getClassName();
//			try {
//
//				Class<?> aClass = Class.forName(className);
//			}catch (Exception e){
//				e.printStackTrace();
//			}
//			try {
//
//				Class<?> aClass1 = TempAuthCommonService.class.getClassLoader().loadClass(className);
//			}catch (Exception e){
//				e.printStackTrace();
//			}
//
//		}
//
//		System.out.println(resources);
//	}
}
