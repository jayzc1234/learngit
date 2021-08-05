package com.zxs.server.controller.gugeng.common;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.OuterRequestUrlConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.base.ListModel;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.ClientCategoryDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.common.CommonService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.common.MessageInformService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.common.TempAuthCommonService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.spring.web.PropertySourcedMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-16
 */
@Slf4j
@RestController
@RequestMapping(VALID_PATH+"/common")
@Api(value = "古耕公共接口模块", tags = "古耕公共接口")
public class ProductionManageCommonController extends CommonUtil {
	@Autowired
	private MessageInformService messageInformService;

	@Autowired
	private CommonService commonService;

	@Autowired
	private TempAuthCommonService authCommonService;

    // Redis服务器地址
    @Value("${express.info.appcode}")
    private String appcode;
    
    @Value("${express.info.expressinfourl}")
    private String expressinfourl;
    
    @Value("${express.info.expresscomUrl}")
    private String expresscomUrl;
    /**
     * 校验码是否合法
     * @return
     */
    @GetMapping("/expressInfo")
    @ApiOperation(value = "根据运单号查询物流信息", notes = "根据运单号查询物流信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(name = "expressNo", paramType = "query", value = "运单号", required = true)
    })
    public JSONArray expressInfo(@RequestParam("expressNo") String expressNo) throws SuperCodeException {
    	Map<String, Object> params=new HashMap<String, Object>();
    	params.put("nu", expressNo);
    	Map<String, String> header=new HashMap<String, String>();
    	header.put("Authorization", "APPCODE "+appcode);
    	
    	JSONObject comdataObj=codeRequests.getAndGetResultBySpring(OuterRequestUrlConstant.EXPRESS_COM_INFO_URL,params , header, JSONObject.class, false);
    	log.info("根据运单号请求快递公司数据："+comdataObj.toJSONString());
    	JSONArray comArray=comdataObj.getJSONObject("showapi_res_body").getJSONArray("data");
    	for (int i = 0; i < comArray.size(); i++) {
    		String simpleName=comArray.getJSONObject(i).getString("simpleName");
    		params.put("com", simpleName);
    		JSONObject dataObj=codeRequests.getAndGetResultBySpring(OuterRequestUrlConstant.EXPRESS_INFO_URL,params , header, JSONObject.class, false);
    		JSONArray jSONArray=dataObj.getJSONObject("showapi_res_body").getJSONArray("data");
    		if (null==jSONArray || jSONArray.isEmpty()) {
    			continue;
			}
    		return jSONArray;
    	}
    	return new JSONArray();
    }
    
    /**
     * 校验码是否合法
     * @return
     * @throws JSONException 
     */
    @GetMapping("/sendmess")
    @ApiOperation(value = "发送消息", notes = "发送消息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(name = "recipientId", paramType = "query", value = "接收者id", required = true),
            @ApiImplicitParam(name = "recipientName", paramType = "query", value = "接收者姓名", required = true),
            @ApiImplicitParam(name = "msgContent", paramType = "query", value = "发送内容", required = true)
    })
    public org.json.JSONObject sendmess(@RequestParam("recipientId") String recipientId, @RequestParam("recipientName") String recipientName, @RequestParam("msgContent") String msgContent) throws SuperCodeException, JSONException {
    	String data=messageInformService.sendOrgMessageToAllPartmentUser(recipientId, recipientName, msgContent,2,1,1);
    	return new org.json.JSONObject(data);
    }


	/**
	 *
	 */
	@PostMapping("/batchUpdateAuthDepartmentId")
	@ApiOperation(value = "批量更新表的部门字段", notes = "批量更新表的部门字段")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	})
	public RestResult batchUpdateAuthDepartmentId() throws SuperCodeException, IOException {
		authCommonService.batchUpdateAuthDepartmentId();
		return RestResult.success();
	}
	/**
	 * 订单导入
	 */
	@PostMapping("/orderImport")
	@ApiOperation(value = "客户信息导入", notes = "客户信息导入")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	})
	public RestResult orderImport(@RequestParam(name="file") MultipartFile multipartFile) throws SuperCodeException, IOException {
		if (null==multipartFile){
			CommonUtil.throwSupercodeException(500,"请先上传文件");
		}
//		commonService.orderImport(multipartFile.getInputStream());
		return RestResult.success();
	}

	/**
	 * 订单导入
	 */
	@GetMapping("/test")
	@ApiOperation(value = "接口测试--json对象", notes = "客户信息导入")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	})
	public RestResult test(ClientCategoryDTO clientCategoryDTO, @RequestHeader(name="channel")String channel) throws SuperCodeException, IOException {

		System.out.println(channel);
		return RestResult.success();
	}

	/**
	 * 订单导入
	 */
	@PostMapping("/test2")
	@ApiOperation(value = "接口测试--POST非json对象参数", notes = "客户信息导入")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	})
	public RestResult test2(ClientCategoryDTO clientCategoryDTO) throws SuperCodeException, IOException {

		System.out.println(clientCategoryDTO);
		return RestResult.success();
	}

	/**
	 * 订单导入
	 */
	@GetMapping("/test3")
	@ApiOperation(value = "接口测试--GET非json对象参数", notes = "客户信息导入")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	})
	public RestResult test3(ClientCategoryDTO clientCategoryDTO) throws SuperCodeException, IOException {
		System.out.println(clientCategoryDTO);
		return RestResult.success();
	}

	/**
	 * 订单导入
	 */
	@PropertySourcedMapping(value = "/test4",propertyKey = "")
	@ApiOperation(value = "接口测试--POST非json对象参数", notes = "客户信息导入")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	})
	public RestResult test4(ListModel models) throws SuperCodeException, IOException {

		System.out.println(models);
		return RestResult.success();
	}
}