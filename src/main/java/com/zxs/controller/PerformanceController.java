package com.zxs.controller;

import com.zxs.dto.FieldBusinessParam;
import com.zxs.dto.MyThread;
import com.zxs.pojo.BeanAnnotation;
import com.zxs.service.PersonService;
import com.zxs.service.SonService1;
import com.zxs.service.SonService2;
import com.zxs.test.FreemarkerUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Api("性能测试相关api")
public class PerformanceController {

	private volatile int status = 0;

  @RequestMapping(value="/index223",method=RequestMethod.GET)
  @ApiOperation(value="测试cpu")
  public void index3(Integer status) {
     while (status == 0){
         for (int i=0;i<Integer.MAX_VALUE;i++){
             int r = 500 * 20 / 10 +5;
         }
     }
  }

    @RequestMapping(value = "/test", method = {RequestMethod.GET})
    public void test(HttpServletResponse response) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("msg", "html--生成图片测试");
            map.put("img", "https://upload-images.jianshu.io/upload_images/912344-3054132dd6939004.png?imageMogr2/auto-orient/strip|imageView2/1/w/300/h/240");
            FreemarkerUtils.turnImage("ddd.html", map, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(11);
        list.add(2);
        list.add(2);

        int sum = list.stream().mapToInt(c -> c).sum();
        System.out.println(sum);


    }
}
