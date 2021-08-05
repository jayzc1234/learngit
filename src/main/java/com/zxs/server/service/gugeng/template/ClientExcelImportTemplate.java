package com.zxs.server.service.gugeng.template;

import com.jgw.supercodeplatform.exception.SuperCodeException;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.OriginUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.template.BaseExcelImportTemplate;
import net.app315.hydra.intelligent.planting.common.gugeng.util.template.TemplateEntity;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClientCategory;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ClientCategoryMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.common.CommonService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户管理excel导入实现类
 *
 * @author shixiongfei
 */
@Component
@Slf4j
public class ClientExcelImportTemplate extends BaseExcelImportTemplate {

    @Autowired
    private ClientCategoryMapper categoryMapper;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private CommonService commonService;
    
    /**
     * 解析自定义excel
     *
     * @param params
     * @return
     */
    @Transactional
    @Override
    public List<Map<String, String>> customRow(List<Map<String, String>> params) throws SuperCodeException {
        // 获取客户类目（当类目不存在时，创建）
        List<String> cns = params.stream().map((map -> map.get("CategoryName"))).distinct().collect(Collectors.toList());
        List<TemplateEntity> entities = getCategoryId(cns);

        for (Map<String, String> map : params) {
        	String saleUserName=map.get("SaleUserName");
        	if (StringUtils.isNotBlank(saleUserName)) {
        		try {
        			String saleUserId=commonService.getEmployName(saleUserName);
        			if (StringUtils.isBlank(saleUserId)) {
        				map.remove("SaleUserName");
                        CommonUtil.throwSupercodeException(500, "根据销售人员名称："+saleUserName+"获取销售人员信息失败");
					}else {
						map.put("SaleUserId", saleUserId);
					}
				} catch (Exception e) {
					log.error(e.getLocalizedMessage());
					CommonUtil.throwSupercodeException(500, "根据销售人员名称："+saleUserName+"获取销售人员信息失败");
				}
			}
            // 获取匹配的id
            String id = entities.stream().filter(filter -> map.get("CategoryName").equals(filter.getKey())).map(entity -> entity.getValue()).limit(1).collect(Collectors.joining());
            map.put("CategoryId", id);

            // 添加创建时间和更新时间, 创建人，更新人，系统id,组织id
            map.put("SysId", commonUtil.getSysId());
            map.put("OrganizationId", commonUtil.getOrganizationId());
            map.put("CreateUserId", commonUtil.getEmployee().getEmployeeId());
            map.put("UpdateUserId", commonUtil.getEmployee().getEmployeeId());
            String now = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            map.put("CreateDate", now);
            map.put("UpdateDate", now);

            // 处理省市区
            String address = map.get("Address");
            // 解析省市区字符串
            Map<String, String> resolution = OriginUtils.addressResolution(address);
            String provinceName = resolution.get("province");
            String cityName = resolution.get("city");
            String countyName = resolution.get("county");
            map.put("ProvinceName", provinceName);
            map.put("CityName", cityName);
            map.put("CountyName", countyName);
            // 调用基础数据平台接口获取相关地区行政编码
            String countyCode = commonUtil.getCountyCode(provinceName, cityName, countyName);
            String cityCode = countyCode.substring(0, 4) + "00";
            String provinceCode = countyCode.substring(0, 2) + "0000";
            map.put("Province", provinceCode);
            map.put("City", cityCode);
            map.put("County", countyCode);
            map.put("AreaCode", countyCode);
            // 移除Address
            map.remove("Address");
        }

        return params;
    }

    /**
     * 执行sql
     *
     * @param sql
     */
    @Transactional
    @Override
    public void executeSql(String sql) {
        // execute sql
        categoryMapper.batchAdd(sql);
    }

    /**
     * 通过cns获取客户类目id
     *
     * @param cns
     * @return
     */
    @Transactional
    public List<TemplateEntity> getCategoryId(List<String> cns) throws SuperCodeException {
        if (CollectionUtils.isEmpty(cns)) {
            throw new SuperCodeException("客户类目不可为空", HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        // 查询是否需要新增客户类目
        List<TemplateEntity> entityList = categoryMapper.listByCategoryNames(commonUtil.getOrganizationId(),commonUtil.getSysId());
        // 移除cns中已存在的客户类目
        cns.removeAll(entityList.stream().map(entity -> entity.getKey()).collect(Collectors.toList()));

        int size = cns.size();
        for (int i = 0; i < size; i++) {
            ProductionManageClientCategory category = new ProductionManageClientCategory();
            // 默认设置等级为1
            category.setSortWeight(1);
            category.setCategoryName(cns.get(i));
            category.setCreateUserId(commonUtil.getEmployee().getEmployeeId());
            category.setSysId(commonUtil.getSysId());
            category.setOrganizationId(commonUtil.getOrganizationId());
            categoryMapper.insert(category);
            // 将新增的id设置到集合entityList中
            TemplateEntity entity = new TemplateEntity();
            entity.setKey(cns.get(i));
            entity.setValue(category.getId().toString());
            entityList.add(entity);
        }

        return entityList;
    }
}
