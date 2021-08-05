package com.zxs.server.service.gugeng.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.JsonObject;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.model.ProductionManageClientMO;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.OriginUtils;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.GuGengContactMan;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClient;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClientCategory;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.*;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductManageClientService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageSaleClientNumStatisticsService;
import net.app315.hydra.user.data.auth.sdk.utils.AreaUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderImportBaseInfoCommonService extends CommonUtil {

    @Autowired
    private CommonService commonService;

    @Autowired
    private ProductManageClientMapper clientMapper;

    @Autowired
    private ClientCategoryMapper clientCategoryMapper;

    @Autowired
    private ProductManageOrderMapper orderMapper;

    @Autowired
    private ProductionManageOrderProductMapper orderProductMapper;

    @Autowired
    private ProductManageClientService clientService;

    @Autowired
    private GuGengContactManMapper guGengContactManMapper;

    @Autowired
    private ProductionManageSaleClientNumStatisticsService saleClientNumStatisticsService;

    private String gloableProductCategoryId = null;

    private String gloableProductCategoryName = null;

    private String gloableProductSortId = null;

    private String gloableProductSortName = null;

    private Map<String, ProductInfo> productInfoMap = new HashMap<>();

    private Map<String, Long> clientCatoryMap = new HashMap<>();

    private Map<String, Specification> specificationMap = new HashMap<>();

    private Map<String, ProductionManageClient> clientMap = new HashMap<>();

    private Map<String, Employee> saleUserMap = new HashMap<>();


    public void close() {
        saleUserMap.clear();
        clientMap.clear();
        clientCatoryMap.clear();
        productInfoMap.clear();
        specificationMap.clear();
        gloableProductSortName = null;
        gloableProductCategoryId = null;
        gloableProductCategoryName = null;
    }

    /**
     * 根据员工姓名获取员工信息
     *
     * @param saleUserName
     * @return
     * @throws SuperCodeExtException
     */
    public Employee getEmployName(int rowindex, String saleUserName) throws SuperCodeExtException {

        Employee employee = saleUserMap.get(saleUserName);
        if (null != employee) {
            return employee;
        }
        Map<String, String> header = new HashMap<String, String>();
        header.put("super-token", getSuperToken());
        RestResult restResult = codeRequests.getAndGetResultBySpring(getHydraUserDomain() + "/employee/name?name=" + saleUserName, null, header, RestResult.class, getUserIsLoadBalanced());
        Map dataMap = (Map) restResult.getResults();
        if (null == dataMap || dataMap.isEmpty()){
            CommonUtil.throwSuperCodeExtException(500, "系统中没有第" + rowindex + "行的销售人员：" + saleUserName + "请联系系统管理员添加该员工");
        }
        List<Map<String, String>> list = (List<Map<String, String>>) dataMap.get("list");
        if (null != list && !list.isEmpty()) {
            Map<String, String> map = list.get(0);
            employee = new Employee();
            employee.setEmployeeId((String) map.get("employeeId"));
            employee.setName((String) map.get("name"));
            employee.setUserId((String) map.get("userId"));
            employee.setDepartmentId(null== map.get("departmentId")?null:map.get("departmentId"));
            employee.setDepartmentName(null== map.get("departmentName")?null:map.get("departmentName"));
            saleUserMap.put(saleUserName, employee);
            return employee;
        }
        CommonUtil.throwSuperCodeExtException(500, "系统中没有第" + rowindex + "行的销售人员：" + saleUserName + "请联系系统管理员添加该员工");
        return null;
    }

    /**
     * 新增客户类目
     *
     * @param categoryName
     * @return
     * @throws SuperCodeExtException
     */
    public Long addClientCategory(String categoryName) throws SuperCodeExtException {
        if (null != clientCatoryMap.get(categoryName)) {
            return clientCatoryMap.get(categoryName);
        }
        QueryWrapper<ProductionManageClientCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProductionManageClientCategory.COL_ORGANIZATION_ID, getOrganizationId());
        queryWrapper.eq(ProductionManageClientCategory.COL_SYS_ID, getSysId());
        queryWrapper.eq(ProductionManageClientCategory.COL_CATEGORY_NAME, categoryName);
        List<ProductionManageClientCategory> clientCategoryList = clientCategoryMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(clientCategoryList)) {
            ProductionManageClientCategory productionManageClientCategory = new ProductionManageClientCategory();
            productionManageClientCategory.setCategoryName(categoryName);
            productionManageClientCategory.setOrganizationId(getOrganizationId());
            productionManageClientCategory.setSysId(getSysId());
            productionManageClientCategory.setCreateUserId(getUserId());
            clientCategoryMapper.insert(productionManageClientCategory);
            if (null == productionManageClientCategory.getId()) {
                CommonUtil.throwSuperCodeExtException(500, "创建类目失败");
            }
            clientCatoryMap.put(categoryName, productionManageClientCategory.getId());
            return productionManageClientCategory.getId();
        } else {
            clientCatoryMap.put(categoryName, clientCategoryList.get(0).getId());
            return clientCategoryList.get(0).getId();
        }
    }

    /**
     * 新增客户类目
     *
     * @param categoryName
     * @param rowIndex
     * @param ggCustomersFond
     * @param ggDietTaboos
     * @return
     * @throws SuperCodeExtException
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductionManageClientMO addClient(String clientName, String contactMan, String contactPhone, String categoryName, Long categoryId, String address, String detailAddress, String saleuserName, boolean isHistory, Employee employee, int rowIndex, String ggCustomersFond, String ggDietTaboos) throws SuperCodeExtException {
        String key=categoryName+clientName;
        ProductionManageClient productionManageClient = clientMap.get(key);
        ProductionManageClientMO productionManageClientMO = new ProductionManageClientMO();
        boolean isNew = false;
        if (null != productionManageClient) {
            BeanUtils.copyProperties(productionManageClient, productionManageClientMO);
            productionManageClientMO.setJustCreateRightNow(isNew);
            return productionManageClientMO;
        }
        String copyAddress = address;
        ProductionManageClient productionManageClient1 = clientService.existClient(null, categoryId, clientName, contactMan, contactPhone, null);
        if (null != productionManageClient1) {
            BeanUtils.copyProperties(productionManageClient1, productionManageClientMO);
            clientMap.put(clientName, productionManageClient1);
            productionManageClientMO.setJustCreateRightNow(isNew);
            return productionManageClientMO;
        } else {
            isNew = true;
            GuGengContactMan guGengContactMan = new GuGengContactMan();
            guGengContactMan.setContactMan(contactMan);
            guGengContactMan.setContactPhone(contactPhone);
            guGengContactMan.setDetailAddress(detailAddress);
            ProductionManageClient client = new ProductionManageClient();
            client.setClientName(clientName);
            client.setCategoryId(categoryId);
            client.setCategoryName(categoryName);
            client.setContactMan(contactMan);
            client.setContactPhone(contactPhone);
            client.setClientType(1);
            client.setDelStatus(0);
            client.setOrganizationId(getOrganizationId());
            client.setSysId(getSysId());
            client.setSaleUserId(employee.getEmployeeId());
            client.setSaleUserName(employee.getName());
            client.setDetailAddress(detailAddress);
            client.setGgDietTaboos(ggDietTaboos);
            client.setGgCustomersFond(ggCustomersFond);
            detailAddress = areaSet(address, detailAddress, isHistory, rowIndex, copyAddress, guGengContactMan, client);
            client.setOperator(employee.getEmployeeId());
            client.setDetailAddress(detailAddress);
            client.setCreateDate(CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
            clientMapper.insert(client);
            guGengContactMan.setClientId(client.getId());
            guGengContactManMapper.insert(guGengContactMan);
            client.setGgContactManId(guGengContactMan.getId());
            //新增潜在及订单客户转化率统计
            saleClientNumStatisticsService.updatePotentialClientNum(client.getSaleUserId(), client.getSaleUserName(), 1);
            BeanUtils.copyProperties(client, productionManageClientMO);
            productionManageClientMO.setJustCreateRightNow(isNew);
            clientMap.put(key, client);
        }
        return productionManageClientMO;
    }

    /**
     * 设置客户区域信息
     *
     * @param address
     * @param detailAddress
     * @param isHistory
     * @param rowIndex
     * @param copyAddress
     * @param guGengContactMan
     * @param client
     * @return
     */
    public String areaSet(String address, String detailAddress, boolean isHistory, int rowIndex, String copyAddress, GuGengContactMan guGengContactMan, ProductionManageClient client) {
        if (StringUtils.isNotBlank(address)) {
            // 处理省市区
            // 解析省市区字符串
            Map<String, String> resolution = OriginUtils.addressResolution(address);
            String provinceName = resolution.get("province");
            String cityName = resolution.get("city");
            String countyName = resolution.get("county");
            String townShipName = resolution.get("town");
            String cityCode = null;
            String provinceCode = null;
            // 调用基础数据平台接口获取相关地区行政编码
            String countyCode = null;
            try {
                countyCode = getCountyCode(provinceName, cityName, countyName);
            } catch (Exception e) {

            }
            if (StringUtils.isBlank(countyCode)) {
                AreaUtil.AreaBean areaBean = commonService.getAreaCodeFromGaoDe(address);
                if (null == areaBean) {
                    CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行所在地解析失败，请填写正确的省市区");
                }
                if (StringUtils.isNotBlank(countyName) && StringUtils.isBlank(areaBean.getCounty())) {
                    CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行所在地解析失败，区或者县无法获取行政编码请放入详细地址中");
                }
                try {
                    commonService.syncBaseAdministrativeCode(areaBean);
                } catch (Exception e) {
                    CommonUtil.throwSuperCodeExtException(500, "同步基础数据编码信息失败" + e.getLocalizedMessage());
                }
                String county = areaBean.getCounty();
                String city = areaBean.getCity();
                String province = areaBean.getProvince();
                countyCode = getAreaCode(county, city, province);
                cityCode = city;
                provinceCode = province;
            } else {
                cityCode = countyCode.substring(0, 4) + "00";
                provinceCode = countyCode.substring(0, 2) + "0000";
            }
            guGengContactMan.setProvinceName(provinceName);
            guGengContactMan.setProvince(provinceCode);
            guGengContactMan.setCityName(cityName);
            guGengContactMan.setCity(cityCode);
            guGengContactMan.setCountyName(countyName);
            guGengContactMan.setCounty(countyCode);
            guGengContactMan.setAreaCode(countyCode);

            client.setProvinceName(provinceName);
            client.setProvince(provinceCode);
            client.setCityName(cityName);
            client.setCity(cityCode);
            client.setCountyName(countyName);
            client.setCounty(countyCode);
            client.setAreaCode(countyCode);
            //处理历史订单
            if (StringUtils.isBlank(detailAddress) && isHistory) {
                if (copyAddress.contains("县")) {
                    detailAddress = copyAddress.split("县")[1];
                } else if (copyAddress.contains("市")) {
                    detailAddress = copyAddress.split("市")[1];
                } else if (copyAddress.contains("省")) {
                    detailAddress = copyAddress.split("省")[1];
                } else {
                    detailAddress = copyAddress;
                }
            }
        }
        return detailAddress;
    }

    public String getAreaCode(String county, String city, String province) {
        String countyCode = null;
        if (StringUtils.isNotBlank(county)) {
            countyCode = county;
        } else if (StringUtils.isNotBlank(city)) {
            countyCode = county;
        } else if (StringUtils.isNotBlank(province)) {
            countyCode = province;
        }
        return countyCode;
    }

    /**
     * 获取产品类目，默认获取产品类目第一等级
     *
     * @return
     * @throws SuperCodeExtException
     */
    public void setDefaultProductCategory() throws SuperCodeExtException {
        Map<String, String> header = new HashMap<String, String>();
        header.put("super-token", getSuperToken());
        RestResult restResult = codeRequests.getAndGetResultBySpring(getUserDomain() + "/product-category", null, header, RestResult.class, getUserIsLoadBalanced());
        Integer state = restResult.getState();
        if (null != state && state.intValue() == 200) {
            Map<String, Object> listMap = (Map<String, Object>) restResult.getResults();
            List<Map<String, String>> list = (List<Map<String, String>>) listMap.get("list");
            Map<String, String> map = list.get(0);
            gloableProductCategoryId = map.get("categoryId");
            gloableProductCategoryName = map.get("categoryName");
            if (null == gloableProductCategoryId || null == gloableProductCategoryName) {
                CommonUtil.throwSuperCodeExtException(500, "无法获取产品类目");
            }
        } else {
            CommonUtil.throwSuperCodeExtException(500, "无法获取产品类目，返回结果：" + restResult.getMsg());
        }
    }

    /**
     * 获取产品分类
     *
     * @return
     * @throws SuperCodeExtException
     */
    public void setProductSort(String checkSortName) throws SuperCodeExtException {
        Map<String, String> header = new HashMap<String, String>();
        header.put("super-token", getSuperToken());
        boolean find = findProductSort(checkSortName, header);
        if (!find) {
            Map<String, String> addsortmap = new HashMap<String, String>();
            addsortmap.put("sortName", checkSortName);
            String result = codeRequests.postAndGetResultBySpring(getUserDomain() + "product-sort", JSONObject.toJSONString(addsortmap), header, String.class, getUserIsLoadBalanced());
            log.info("添加产品分类返回结果：" + result);
            RestResult restResult = JSONObject.parseObject(result, RestResult.class);
            if (null == restResult.getState() || restResult.getState().intValue() != 200) {
                CommonUtil.throwSuperCodeExtException(500, "添加产品分类失败-返回结果" + restResult.getMsg() + ",请求参数：" + JSONObject.toJSONString(addsortmap));
            }
            findProductSort(checkSortName, header);
            if (null == gloableProductSortId || null == gloableProductSortName) {
                CommonUtil.throwSuperCodeExtException(500, "无法获取产品分类");
            }
        }
    }

    private boolean findProductSort(String checkSortName, Map<String, String> header) {
        RestResult restResult = codeRequests.getAndGetResultBySpring(getUserDomain() + "/product-sort/firsts", null, header, RestResult.class, getUserIsLoadBalanced());
        Integer state = restResult.getState();
        boolean find = false;
        if (null != state && state.intValue() == 200) {
            List<Map<String, String>> list = (List<Map<String, String>>) restResult.getResults();
            for (int i = 0; i < list.size(); i++) {
                Map<String, String> map = list.get(i);
                String sortId = map.get("sortId");
                String sortName = map.get("sortName");
                if (sortName.equals(checkSortName)) {
                    gloableProductSortId = sortId;
                    gloableProductSortName = sortName;
                    find = true;
                    break;
                }
            }
        }
        return find;
    }

    /**
     * 新增产品接口
     *
     * @param rowIndex
     * @param productName
     * @return {
     * "name": "农业产品",
     * "productName": "大西12",
     * "productSortName": "ffds",
     * "productSortId": "c98b34de92b642ffa6da2c68a361e7b7",
     * "producLargeCategory": "01",
     * "productWareHouse": {
     * "smallUnitCode": "018101",
     * "smallUnitName": "个"
     * }
     * }
     * @throws SuperCodeExtException
     */
    public String getOrAddProduct(int rowIndex, String productName) throws SuperCodeExtException {
        Map<String, Object> map = new HashMap<>();
        if (null == gloableProductCategoryId || null == gloableProductCategoryName) {
            setDefaultProductCategory();
        }
        if (null == gloableProductCategoryId || null == gloableProductCategoryName) {
            CommonUtil.throwSuperCodeExtException(500, "无法获取产品类目");
        }
        Map<String, Object> producLargeCategoryMap = new HashMap<>();
        map.put("productName", productName);
        map.put("name", gloableProductCategoryName);
        map.put("producLargeCategory", gloableProductCategoryId);
        map.put("productSortName", gloableProductSortName);
        map.put("productSortId", gloableProductSortId);

        producLargeCategoryMap.put("smallUnitCode", "018101");
        producLargeCategoryMap.put("smallUnitName", "个");
        map.put("productWareHouse", producLargeCategoryMap);

        Map<String, String> head = new HashMap<>();
        head.put("super-token", getSuperToken());
        String result = codeRequests.postAndGetResultBySpring(getUserDomain() + "/product", JSONObject.toJSONString(map), head, String.class, getUserIsLoadBalanced());
        log.info("添加产品返回结果：" + result);
        RestResult restResult = JSONObject.parseObject(result, RestResult.class);
        if (null == restResult.getState() || restResult.getState().intValue() != 200) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行，添加产品失败-返回结果" + restResult.getMsg() + ",请求参数：" + JSONObject.toJSONString(map));
        }
        return result;
    }

    /**
     * 获取产品信息
     *
     * @param rowIndex
     * @param productName
     * @return
     */
    public String getProductId(int rowIndex, String productName, boolean needCreate) throws SuperCodeExtException {
        Map<String, String> header = new HashMap<>();
        header.put("super-token", getSuperToken());

        Map<String, Object> params = new HashMap<>();
        params.put("search", productName);
        params.put("current", 1);
        params.put("pageSize", 1000);

        RestResult restResult = codeRequests.getAndGetResultBySpring(getUserDomain() + "/product/enable/list", params, header, RestResult.class, getUserIsLoadBalanced());
        Integer state = restResult.getState();
        if (null != state && state.intValue() == 200) {
            Map<String, Object> listMap = (Map<String, Object>) restResult.getResults();
            List<Map<String, String>> list = (List<Map<String, String>>) listMap.get("list");
            if (CollectionUtils.isNotEmpty(list)) {
                for (Map<String, String> map : list) {
                    String backproductName = map.get("productName");
                    if (backproductName.equals(productName)) {
                        return list.get(0).get("productId");
                    }
                }
            }
            if (needCreate) {
                //添加产品
                getOrAddProduct(rowIndex, productName);
                restResult = codeRequests.getAndGetResultBySpring(getUserDomain() + "/product/enable/list", params, header, RestResult.class, getUserIsLoadBalanced());
                if (null != state && state.intValue() == 200) {
                    listMap = (Map<String, Object>) restResult.getResults();
                    list = (List<Map<String, String>>) listMap.get("list");
                    if (CollectionUtils.isEmpty(list)) {
                        CommonUtil.throwSuperCodeExtException(500, "查询产品" + productName + "失败，参数：" + JSONObject.toJSONString(params));
                    } else {
                        for (Map<String, String> map : list) {
                            String backproductName = map.get("productName");
                            if (backproductName.equals(productName)) {
                                return list.get(0).get("productId");
                            }
                        }
                    }
                }
            }
        }
        CommonUtil.throwSuperCodeExtException(500, "系统中没有第" + rowIndex + "行的" + productName + "产品，请先添加该产品");
        return null;
    }

    /**
     * 新增产品信息
     *
     * @param productId
     * @return
     * @throws SuperCodeExtException
     */
    public ProductInfo getOrAddProductInfo(String productId, String productLevelName, String productSpecicationNameParam, String productSortSpecicationNameParam) throws SuperCodeExtException {
        if (StringUtils.isBlank(productId) || StringUtils.isBlank(productLevelName) || StringUtils.isBlank(productSpecicationNameParam) || StringUtils.isBlank(productSortSpecicationNameParam)) {

        }
        String key = productId + productLevelName + productSpecicationNameParam + productSortSpecicationNameParam;

        ProductInfo productInfo = productInfoMap.get(key);
        if (null == productInfo) {
            productInfo = listProductInfo(productId, productLevelName, productSpecicationNameParam, productSortSpecicationNameParam);
        }

        if (null == productInfo) {
            Map<String, Object> paramMap = new HashMap<>();
            //产品等级
            JSONObject levelJsonObject = new JSONObject();
            levelJsonObject.put("productId", productId);
            levelJsonObject.put("specificationId", "");
            levelJsonObject.put("specificationName", productLevelName);
            levelJsonObject.put("type", 0);
            levelJsonObject.put("sequence", "2");

            //产品规格
            JSONObject specificationJsonObject = new JSONObject();
            specificationJsonObject.put("productId", productId);
            specificationJsonObject.put("specificationId", "");
            specificationJsonObject.put("specificationName", productSpecicationNameParam);
            specificationJsonObject.put("type", 1);

            //分拣规格
            JSONObject sortJsonObject = new JSONObject();
            sortJsonObject.put("productId", productId);
            sortJsonObject.put("specificationId", "");
            sortJsonObject.put("specificationName", productSortSpecicationNameParam);
            sortJsonObject.put("type", 6);

            //封装集合数据
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(levelJsonObject);
            jsonArray.add(specificationJsonObject);
            jsonArray.add(sortJsonObject);

            //组合最终数据
            paramMap.put("params", jsonArray);

            Map<String, String> head = new HashMap<>();
            head.put("super-token", getSuperToken());
            String result = codeRequests.postAndGetResultBySpring(getUserDomain() + "/specification/product/addProductInfo", JSONObject.toJSONString(paramMap), head, String.class, getUserIsLoadBalanced());
            log.info("添加产品维护信息返回结果：" + result);
            RestResult restResult = JSONObject.parseObject(result, RestResult.class);
            if (null == restResult.getState() || restResult.getState().intValue() != 200) {
                CommonUtil.throwSuperCodeExtException(500, "添加产品信息维护失败，产品id：" + productId + "，productLevel=" + productLevelName + "，productSpecicationNameParam=" + productSpecicationNameParam + ",返回结果：" + restResult.getMsg());
            }

            productInfo = listProductInfo(productId, productLevelName, productSpecicationNameParam, productSortSpecicationNameParam);
            if (null == productInfo) {
                CommonUtil.throwSuperCodeExtException(500, "添加产品信息维护失败，产品id：" + productId + "，productLevel=" + productLevelName + "，productSpecicationNameParam=" + productSpecicationNameParam + ",返回结果：" + restResult.getMsg());
            }
            productInfoMap.put(key, productInfo);
        }
        return productInfo;
    }

    /**
     * 获取产品信息
     *
     * @param productId
     * @return
     */
    private ProductInfo listProductInfo(String productId, String productLevel, String productSpecicationNameParam, String productSortSpecicationNameParam) throws SuperCodeExtException {
        Map<String, String> header = new HashMap<>();
        header.put("super-token", getSuperToken());

        Map<String, Object> params = new HashMap<>();
        params.put("productId", productId);
        params.put("type", 0);
        params.put("current", 1);
        params.put("pageSize", 1000);

        String result = codeRequests.getAndGetResultBySpring(getUserDomain() + "/specification/product/list", params, header, String.class, getUserIsLoadBalanced());
        log.info("获取产品维护信息返回结果：" + result);
        RestResult restResult = JSONObject.parseObject(result, RestResult.class);
        JsonObject jsonObject = new JsonObject();
        Integer state = restResult.getState();
        boolean find = false;
        if (null != state && state.intValue() == 200) {
            JSONObject resultObject = (JSONObject) restResult.getResults();
            JSONArray jsonArray = resultObject.getJSONArray("list");
            if (null != jsonArray && !jsonArray.isEmpty()) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject map = jsonArray.getJSONObject(i);
                    String levelSpecificationName = map.getString("levelSpecificationName");
                    String productSpecificationName = map.getString("specificationName");
                    String sortingSpecificationName = map.getString("sortingSpecificationName");
                    if (productLevel.equals(levelSpecificationName) && productSpecificationName.equals(productSpecicationNameParam)) {
                        ProductInfo prductInfo = new ProductInfo();
                        prductInfo.setLevelSpecificationId(map.getString("levelSpecificationId"));
                        prductInfo.setLevelSpecificationName(levelSpecificationName);
                        prductInfo.setSortingSpecificationId(map.getString("sortingSpecificationId"));
                        prductInfo.setSortingSpecificationName(map.getString("sortingSpecificationName"));
                        prductInfo.setSpecificationId(map.getString("specificationId"));
                        prductInfo.setSpecificationName(map.getString("specificationName"));
                        prductInfo.setSortingSpecificationId(map.getString("sortingSpecificationId"));
                        prductInfo.setSortingSpecificationName(map.getString("sortingSpecificationName"));
                        return prductInfo;
                    }
                }
            }
        } else {
            CommonUtil.throwSuperCodeExtException(500, "获取产品信息维护失败，产品id：" + productId + "，productLevel=" + productLevel + "，productSpecicationNameParam=" + productSpecicationNameParam + ",返回结果：" + restResult.getMsg());
        }
        return null;
    }

    /**
     * 添加包装规格或包装方式
     *
     * @param rowIndex
     * @param specificationName
     * @param type              :2包装规格，3包装方式
     * @return
     * @throws SuperCodeExtException
     */
    public Specification getOrAddSpecification(int rowIndex, String specificationName, int type) throws SuperCodeExtException {
        Specification specification = specificationMap.get(specificationName + type);
        if (null == specification) {
            specification = listPackageSpecification(specificationName, type, rowIndex);
        }
        if (null == specification) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sequence", "1");
            jsonObject.put("specificationId", "");
            jsonObject.put("specificationName", specificationName);
            jsonObject.put("type", type);

            Map<String, String> head = new HashMap<>();
            head.put("super-token", getSuperToken());
            String result = codeRequests.postAndGetResultBySpring(getUserDomain() + "/specification", jsonObject.toJSONString(), head, String.class, getUserIsLoadBalanced());
            log.info("添加包装规格/方式返回结果：" + result);

            RestResult restResult = JSONObject.parseObject(result, RestResult.class);
            if (restResult.getState() != 200) {
                CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行，添加包装规格/方式" + specificationName + "失败，返回结果：" + restResult.getMsg());
            }
            Map<String, String> dataMap = (Map<String, String>) restResult.getResults();
            String specificationId = dataMap.get("specificationId");
            specification = new Specification();
            specification.setSpecificationId(specificationId);
            specification.setSpecificationName(specificationName);
        }
        return specification;
    }

    /**
     * 添加包装规格或包装方式
     *
     * @param search
     * @param rowIndex
     * @param type     :2包装规格，3包装方式
     * @return
     */
    private Specification listPackageSpecification(String search, int type, int rowIndex) {
        Map<String, String> header = new HashMap<>();
        header.put("super-token", getSuperToken());

        Map<String, Object> params = new HashMap<>();
        if (StringUtils.isNotBlank(search)) {
            try {
                String encode = URLEncoder.encode(search, "UTF-8");
                params.put("specificationName", encode);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                if (type == 2) {
                    CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行解析包装规格失败");
                } else {
                    CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行解析包装方式失败");
                }
            }
        }
        params.put("type", type);
        params.put("current", 1);
        params.put("pageSize", 1000);

        RestResult restResult = codeRequests.getAndGetResultBySpring(getUserDomain() + "/specification/selectByNameAndType", params, header, RestResult.class, getUserIsLoadBalanced());
        Integer state = restResult.getState();
        boolean find = false;
        if (null != state && state.intValue() == 200) {
            List<Map<String, String>> list = (List<Map<String, String>>) restResult.getResults();
            if (null == list || list.isEmpty()) {
                return null;
            }
            for (Map<String, String> map : list) {
                String specificationName = map.get("specificationName");
                if (search.equals(specificationName)) {
                    Specification specification = new Specification();
                    specification.setSpecificationId(map.get("specificationId"));
                    specification.setSpecificationName(map.get("specificationName"));
                    return specification;
                }
            }
        }
        return null;
    }

    private static void checkParams(int line, String... paramsChecks) throws SuperCodeExtException {
        if (null != paramsChecks && paramsChecks.length != 0) {
            for (int i = 0; i < paramsChecks.length; i++) {
                if (StringUtils.isBlank(paramsChecks[i])) {
                    CommonUtil.throwSuperCodeExtException(500, "第" + line + "行,第" + i + "列不能为空");
                }
            }
        }
    }

}

@Data
class ProductInfo {
    String levelSpecificationId;
    String levelSpecificationName;
    String sortingSpecificationId;
    String sortingSpecificationName;
    String specificationId;
    String specificationName;
}

@Data
class Specification {
    String specificationId;
    String specificationName;
}

enum SpecificationEnum {

    /**
     * 包装方式
     */
    PACK_WAY(3, "气柱袋+溯源码+封口标签"),

    /**
     * 包装规格
     */
    PACK_SPECIFICATION(2, "6个/箱");

    private int type;
    private String specificationName;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSpecificationName() {
        return specificationName;
    }

    public void setSpecificationName(String specificationName) {
        this.specificationName = specificationName;
    }

    SpecificationEnum(int type, String specificationName) {
        this.type = type;
        this.specificationName = specificationName;
    }

    public static void main(String[] args) throws SuperCodeException {
        Map<String, String> map = OriginUtils.addressResolution("湖南省长沙市芙蓉区红星大道111号");
        System.out.println(map);
    }

}