package com.zxs.server.controller.gugeng.producemanage;


import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.ProductionManageSdProduceSchemeNodeListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManageSdProduceSchemeNode;
import net.app315.hydra.intelligent.planting.server.service.gugeng.producemanage.ProductionManageSdProduceSchemeNodeService;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.ProductionManageSdProduceSchemeNodeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zc
 * @since 2019-06-14
 */
@RestController
@RequestMapping(VALID_PATH+"/sd/produce/scheme/node")
@Api(tags = "标准化方案节点分页列表接口")
public class ProductionManageSdProduceSchemeNodeController {
   @Autowired
   private ProductionManageSdProduceSchemeNodeService service;
   
   
	/**
	 * 标准化方案节点列表
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/page",method = {RequestMethod.POST,RequestMethod.GET})
	@ApiOperation(value = "列表", notes = "")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public RestResult<PageResults<List<ProductionManageSdProduceSchemeNodeVO>>> list(ProductionManageSdProduceSchemeNodeListDTO daoSearch)
			throws Exception {
		RestResult<PageResults<List<ProductionManageSdProduceSchemeNodeVO>>> restResult = new RestResult<>();
		PageResults<List<ProductionManageSdProduceSchemeNodeVO>> pageResults = service.page(daoSearch);
		restResult.setState(200);
		restResult.setResults(pageResults);
		return restResult;
	}
	
	
	/**
	 * 标准化方案节点列表
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "/detail")
	@ApiOperation(value = "工作日节点详细信息", notes = "")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
		@ApiImplicitParam(name = "sdProduceSchemeId", paramType = "query", defaultValue = "1", value = "方案主键", required = true),
		@ApiImplicitParam(name = "dayNumber", paramType = "query", defaultValue = "2", value = "第几天", required = true)
		})
	public RestResult<List<ProductionManageSdProduceSchemeNode>> detail(@RequestParam Long sdProduceSchemeId, @RequestParam Integer dayNumber)
			throws Exception {
		List<ProductionManageSdProduceSchemeNode> nodes = service.selectBySchemeIdAndDayNumber(sdProduceSchemeId,dayNumber);
		return RestResult.successDefault(nodes);
	}
}

