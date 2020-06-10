package com.zxs.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
//@Service(value="service")
public class BaseService {
	public List<String> list=new ArrayList<String>();

	@Override
	public String toString() {
		String s=UUID.randomUUID().toString();
		System.out.println(s);
		return s;
	}

	public BaseService() {
		toString();
	}
	
	
}
