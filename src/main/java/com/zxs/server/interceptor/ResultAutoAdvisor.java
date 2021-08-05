package com.zxs.server.interceptor;

import com.alibaba.fastjson.support.spring.MappingFastJsonValue;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.nail.common.result.NoPackageResult;
import net.app315.nail.common.result.RichResult;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletResponse;

/**
 *  响应结果自动封装，来源于--福鼎白茶
 *  其它工程将不使用该方式返回RichResult
 */
@ControllerAdvice(basePackages={"net.app315.hydra.intelligent.planting.server.controller.fuding","net.app315.hydra.intelligent.planting.server.mobile.controller.fuding"})
public class ResultAutoAdvisor implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return methodParameter.getMethodAnnotation(NoPackageResult.class) == null;
    }

    /**
     *
     * @param body  返回结果
     * @param methodParameter 方法参数
     * @param mediaType  返回类型
     * @param aClass
     * @param serverHttpRequest
     * @param serverHttpResponse
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest,
                                  ServerHttpResponse serverHttpResponse) {
        if(body instanceof RichResult || body instanceof RestResult){
            return body;
        }
        if (body instanceof MappingFastJsonValue) {

            Object value = ((MappingFastJsonValue) body).getValue();
            if(value instanceof RichResult){
                return body;
            }
        }
        if (serverHttpResponse != null && serverHttpResponse instanceof HttpServletResponse) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) serverHttpResponse;
            int status = httpServletResponse.getStatus();
            if (status > 200) {
                return body;
            }
        }
        // swagger 文档过滤
        if("/error".equals(serverHttpRequest.getURI().getPath())){
            return body;
        }else if("/swagger-resources".equals(serverHttpRequest.getURI().getPath())){
            return body;
        }else if("/v2/api-docs".equals(serverHttpRequest.getURI().getPath())){
            return body;
        }
        return new RichResult(body);
    }
}
