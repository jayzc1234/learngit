package com.zxs.service;

import org.springframework.stereotype.Service;

@Service(value="service2")
public class SonService2 extends BaseService{
 public void change() {
	 list.add("service2");
	 super.list.add("s_service2");
 }
}
