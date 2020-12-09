package com.zxs.service;

import org.springframework.stereotype.Service;

@Service(value = "service1")
public class SonService1 extends BaseService {

	public String change(String name) {
		list.add("service1");
		super.list.add("s_service1");
		StringBuilder build = new StringBuilder();
		for (String s : list) {
			System.out.println(s);
			build.append(s);
		}
		System.out.println(name);
		return build.toString();
	}

	public SonService1() {
		super();
	}

}
