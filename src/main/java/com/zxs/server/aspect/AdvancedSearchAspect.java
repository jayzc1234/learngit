package com.zxs.server.aspect;

import com.alibaba.fastjson.JSONObject;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class AdvancedSearchAspect {

    private final static Logger logger = LoggerFactory.getLogger(AdvancedSearchAspect.class);

    @Pointcut("@annotation(net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch)")
    public void page() {
    }

//	@Before("page()")
//	public void deBefore1(JoinPoint joinPoint) throws SuperCodeException {
//		Object[] args = joinPoint.getArgs();
//		if (ArrayUtils.isEmpty(args)) {
//			return;
//		}
//
//		DaoSearch daoSearch = (DaoSearch) Stream.of(args).filter(arg -> arg instanceof DaoSearch).findFirst().get();
//		String advancedSearch = daoSearch.getAdvancedSearch();
//		// 如果请求体中没有高级搜索数据，则从请求头中获取
//		if (StringUtils.isBlank(advancedSearch)) {
//			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
//			advancedSearch = request.getHeader("advancedSearch");
//		}
//
//		String decodeSearch = null;
//		try {
//			decodeSearch = URLDecoder.decode(advancedSearch, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			throw new SuperCodeException("编码advancedSearch失败，advancedSearch = " + advancedSearch, 500);
//		}
//		if (StringUtils.isBlank(decodeSearch)) {
//			return;
//		}
//
//		daoSearch = JSONObject.parseObject(decodeSearch, daoSearch.getClass());
//	}

    @Before("page()")
    public void doBefore(JoinPoint joinPoint) throws SuperCodeException {
        Object[] params = joinPoint.getArgs();
        if (null != params && params.length > 0) {
            for (Object object : params) {
                if (object instanceof DaoSearch) {
                    DaoSearch deaDaoSearch = (DaoSearch) object;
                    Integer flag = deaDaoSearch.getFlag();
                    if (null!=flag){
                        if (flag==0){
                            deaDaoSearch.setSearch(null);
                        }
                    }
                    String advanceSearch = deaDaoSearch.getAdvancedSearch();
                    // 如果请求体中没有高级搜索数据，则从请求头中获取
                    if (StringUtils.isBlank(advanceSearch)) {
                        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                        advanceSearch = request.getHeader("advancedSearch");
                    }
                    try {
                        if (StringUtils.isNotBlank(advanceSearch)) {
                            String decodeSearch = URLDecoder.decode(advanceSearch, "UTF-8");
                            deaDaoSearch.setAdvancedSearch(decodeSearch);
                        }
                    } catch (UnsupportedEncodingException e1) {
                        throw new SuperCodeException("编码advanceSearch失败，advanceSearch=" + advanceSearch, 500);
                    }
                    Map<String, Object> data = JSONObject.parseObject(JSONObject.toJSONString(deaDaoSearch), Map.class);
                    try {
                        Map<String, Object> data2 = (Map<String, Object>) deaDaoSearch.advancedSearchToObj(Map.class);
                        if (null != data2) {
                            data.putAll(data2);
                        }
                        String json = JSONObject.toJSONString(data);
                        log.info("拦截器获取daosearch:" + json);
                        DaoSearch newdeaDaoSearch = JSONObject.parseObject(JSONObject.toJSONString(data), deaDaoSearch.getClass());
                        BeanUtils.copyProperties(newdeaDaoSearch, deaDaoSearch);

                    } catch (SuperCodeException e) {
                        e.printStackTrace();
                        throw new SuperCodeException("导出excel及高级搜切面失败" + e.getLocalizedMessage(), 500);
                    }
                }
            }
        }
    }
}
