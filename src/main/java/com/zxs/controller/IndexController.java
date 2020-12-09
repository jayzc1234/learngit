package com.zxs.controller;

import java.util.ArrayList;
import java.util.List;

import com.zxs.dto.FieldBusinessParam;
import com.zxs.dto.MyThread;
import com.zxs.pojo.BeanAnnotation;
import com.zxs.service.PersonService;
import com.zxs.service.SonService1;
import com.zxs.service.SonService2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@RestController
@Api("swagger测试相关api")
public class IndexController{

//@Override
//public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
//	return new ModelAndView("jay");
//}

	@Autowired
	@Qualifier("service1")
	private SonService1 s1;

	@Autowired
	private MyThread myThread;
	
	@Autowired
	@Qualifier("service2")
	private SonService2 s2;

	@Autowired
	private PersonService personService;

	@Autowired
	private BeanAnnotation beanAnnotation;

	@RequestMapping(value="/person/service",method=RequestMethod.GET)
	@ApiOperation(value="person测试")
	public String personService(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null){
			for (Cookie cookie : cookies) {
				String name = cookie.getName();
				System.out.println(name);
			}
		}
		return "jay";
	}

	@RequestMapping(value="/index",method=RequestMethod.POST)
  @ApiOperation(value="返回字符串")
  public String index(List<FieldBusinessParam> list) {
	  s1.change("哈哈 ");
	  int l1=s1.list.size();

	  Integer age = myThread.getAge();
	  s2.change();
	  int l2=s2.list.size();

	  
	  System.out.println(list);
	  return "jay";
  }
  
  @RequestMapping(value="/index2",method=RequestMethod.GET)
  @ApiOperation(value="测试参数数组元素为字符串情况")
  public String index3(@RequestParam(name="names")ArrayList<String> names) {
	  System.out.println(names);
	  return "jay";
  }
  
  @RequestMapping(value="/index3",method=RequestMethod.GET)
  @ApiOperation(value="测试参数数组元素为对象情况")
  public String index4(ArrayList<FieldBusinessParam> names) {
	  System.out.println(names);
	  int l1=s1.list.size();
	  
	  s2.change();
	  int l2=s2.list.size();
	  
	  System.out.println(l2);
	  return "jay";
  }
}
