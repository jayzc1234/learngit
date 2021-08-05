package com.zxs.server.service.gugeng.common;

import com.alibaba.fastjson.JSONObject;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.OriginUtils;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClient;
import net.app315.hydra.user.data.auth.sdk.utils.AreaUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zc
 */
@Service
@Slf4j
public class CommonService extends CommonUtil {
	@Value("${gaoDe.map.key}")
	private String gaoDeMaKey;

	@Value("${gaoDe.map.getCode.url}")
	private String gaoDeMapUrl;


	/**
	 * 根据员工姓名获取员工信息
	 * @param saleUserName
	 * @return
	 * @throws SuperCodeException
	 */
	public String getEmployName(String saleUserName) throws SuperCodeException {
		Map<String, String>header=new HashMap<String, String>();
		header.put("super-token", getSuperToken());
		RestResult restResult=codeRequests.getAndGetResultBySpring(getHydraUserDomain()+"/employee/name?name="+saleUserName, null, header, RestResult.class, getUserIsLoadBalanced());
        Map dataMap = (Map) restResult.getResults();
        if (null == dataMap || dataMap.isEmpty()){
           return null;
        }
		List<Map<String, Object>> list=(List<Map<String, Object>>) dataMap.get("list");
		if (null!=list && !list.isEmpty()) {
			return list.get(0).get("employeeId").toString();
		}
		return null;
	}

	/**
	 * 根据员工获取userId
	 * @param employId
	 * @return
	 * @throws SuperCodeException
	 */
	public String getUserIdByEmployId(String employId)  {
		if (StringUtils.isBlank(employId)){
			CommonUtil.throwSuperCodeExtException(500,"员工id不能为空");
		}
		Map<String, String>header=new HashMap<String, String>();
		header.put("super-token", getSuperToken());
		RestResult restResult=codeRequests.getAndGetResultBySpring(getHydraUserDomain()+"/employee/getByEmpId?employeeId="+employId, null, header, RestResult.class, getUserIsLoadBalanced());
		Integer state=restResult.getState();
		if (null!=state && state.intValue()==200){
			Map<String,Object> map= (Map<String, Object>) restResult.getResults();
			Object userId=map.get("userId");
			if (null!=userId){
				return  userId.toString();
			}
		}
		return null;
	}

	/**
	 *需要解析的地址
	 * @param address
	 * @return
	 * @throws SuperCodeException
	 */
	public AreaUtil.AreaBean getAreaCodeFromGaoDe(String address) {
		if (StringUtils.isBlank(address)){
			CommonUtil.throwSuperCodeExtException(500,"获取地区编码地址不能为空");
		}
		JSONObject jsonObject = codeRequests.getAndGetResultBySpring(gaoDeMapUrl + "?address="+address+"&output=json&key="+gaoDeMaKey, null, null, JSONObject.class, false);
		log.info("获取到高德地图地区编码信息："+jsonObject);
		String provinceAdcode=null;
		String cityAdcode=null;
		AreaUtil.AreaBean areaBean=new AreaUtil.AreaBean();
		if (jsonObject.getIntValue("status")==1){
			JSONObject geocode = jsonObject.getJSONArray("geocodes").getJSONObject(0);
			String province = geocode.getString("province");
			String city = geocode.getString("city");
			String district = geocode.getString("district");
			String adcode = geocode.getString("adcode");
			areaBean.setProvinceName(province);
			areaBean.setCityName(city);
			areaBean.setCountyName(district);
			if (StringUtils.isNotBlank(district)){
				areaBean.setCounty(adcode);
				JSONObject cityJsonObject = codeRequests.getAndGetResultBySpring(gaoDeMapUrl + "?address="+province+city+"&output=json&key="+gaoDeMaKey, null, null, JSONObject.class, false);
				if (cityJsonObject.getIntValue("status")==1){
					JSONObject citygeocode = cityJsonObject.getJSONArray("geocodes").getJSONObject(0);
					cityAdcode= citygeocode.getString("adcode");
				}

				JSONObject provinceJsonObject = codeRequests.getAndGetResultBySpring(gaoDeMapUrl + "?address="+province+"&output=json&key="+gaoDeMaKey, null, null, JSONObject.class, false);
				if (provinceJsonObject.getIntValue("status")==1){
					JSONObject provincegeocode = provinceJsonObject.getJSONArray("geocodes").getJSONObject(0);
					provinceAdcode= provincegeocode.getString("adcode");
				}
				areaBean.setProvince(provinceAdcode);
				areaBean.setCity(cityAdcode);
			}else if (StringUtils.isNotBlank(city)){
				areaBean.setCity(adcode);
				JSONObject provinceJsonObject = codeRequests.getAndGetResultBySpring(gaoDeMapUrl + "?address="+province+"&output=json&key="+gaoDeMaKey, null, null, JSONObject.class, false);
				if (provinceJsonObject.getIntValue("status")==1){
					JSONObject provincegeocode = provinceJsonObject.getJSONArray("geocodes").getJSONObject(0);
					provinceAdcode= provincegeocode.getString("adcode");
				}
				areaBean.setProvince(provinceAdcode);
			}else if (StringUtils.isNotBlank(province)){
				areaBean.setProvince(adcode);
			}
		}else {
			CommonUtil.throwSuperCodeExtException(500,"从高德地图获取行政编码失败，错误码："+jsonObject.getString("info"));
		}
		return areaBean;
	}
	public AreaUtil.AreaBean getAreaCodeFromBase(String address) throws SuperCodeException {
		AreaUtil.AreaBean areaBean=new AreaUtil.AreaBean();
		ProductionManageClient client = new ProductionManageClient();
		// 处理省市区
		// 解析省市区字符串
		Map<String, String> resolution = OriginUtils.addressResolution(address);
		String provinceName = resolution.get("province");
		String cityName = resolution.get("city");
		String countyName = resolution.get("county");
		String townShipName = resolution.get("town");
		// 调用基础数据平台接口获取相关地区行政编码
		String countyCode=null;
		try{
			countyCode = getCountyCode(provinceName, cityName, countyName);
		}catch (Exception e){
		}
		if (StringUtils.isBlank(countyCode)){
			return  null;
		}
		String cityCode = countyCode.substring(0, 4) + "00";
		String provinceCode = countyCode.substring(0, 2) + "0000";
		areaBean.setProvinceName(provinceName);
		areaBean.setProvince(provinceCode);
		areaBean.setCityName(cityName);
		areaBean.setCity(cityCode);
		areaBean.setCountyName(countyName);
		areaBean.setCounty(countyCode);
		return areaBean;
	}



	public String syncBaseAdministrativeCode(AreaUtil.AreaBean areaBean) throws SuperCodeException {
		if (null==areaBean){
			CommonUtil.throwSupercodeException(500,"同步地区编码到基础数据，数据不能为空");
		}
		Map<String, String>header=new HashMap<String, String>();
		header.put("super-token", getSuperToken());
		JSONObject result = codeRequests.postAndGetResultBySpring(getUserDomain() + "/address/syncData", JSONObject.toJSONString(areaBean), header, JSONObject.class, getUserIsLoadBalanced());
		log.info("同步地区编码到基础数据返回信息："+result);
		int state = result.getIntValue("state");
		if (state!=200){
			CommonUtil.throwSuperCodeExtException(500,"同步地区编码到基础数据失败："+result.getString("msg"));
		}
		return null;
	}

}
