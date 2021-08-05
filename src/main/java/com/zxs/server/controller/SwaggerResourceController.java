package com.zxs.server.controller;


import net.app315.hydra.intelligent.planting.AppConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.json.Json;
import springfox.documentation.spring.web.json.JsonSerializer;
import springfox.documentation.swagger.web.ApiResourceController;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;
import springfox.documentation.swagger2.web.Swagger2Controller;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Description :描述
 * @Author : linfei.li
 * @Date :2020/4/23
 * @Version :1.0
 * @Modified By :
 */
@Controller
@RequestMapping(AppConstants.SERVICE_NAME)
public class SwaggerResourceController implements InitializingBean {
    @Autowired
    private ApiResourceController apiResourceController;

    @Autowired
    private Environment environment;

    @Autowired
    private DocumentationCache documentationCache;

    @Autowired
    private ServiceModelToSwagger2Mapper mapper;

    @Autowired
    private JsonSerializer jsonSerializer;

    private Swagger2Controller swagger2Controller;

    @Override
    public void afterPropertiesSet() {
        swagger2Controller = new Swagger2Controller(environment, documentationCache, mapper, jsonSerializer);
    }

    /**
     * 首页
     *
     * @return
     */
    @RequestMapping
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("redirect:" +"/doc.html");
        return modelAndView;
    }

    @RequestMapping("/swagger-resources/configuration/security")
    @ResponseBody
    public ResponseEntity<SecurityConfiguration> securityConfiguration() {
        return apiResourceController.securityConfiguration();
    }

    @RequestMapping("/swagger-resources/configuration/ui")
    @ResponseBody
    public ResponseEntity<UiConfiguration> uiConfiguration() {
        return apiResourceController.uiConfiguration();
    }

    @RequestMapping("/swagger-resources")
    @ResponseBody
    public ResponseEntity<List<SwaggerResource>> swaggerResources() {
        return apiResourceController.swaggerResources();
    }

    @RequestMapping(value = "/v2/api-docs", method = RequestMethod.GET, produces = {"application/json", "application/hal+json"})
    @ResponseBody
    public ResponseEntity<Json> getDocumentation(
            @RequestParam(value = "group", required = false) String swaggerGroup,
            HttpServletRequest servletRequest) {
        return swagger2Controller.getDocumentation(swaggerGroup, servletRequest);
    }
}