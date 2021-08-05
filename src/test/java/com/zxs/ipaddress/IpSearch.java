package com.zxs.ipaddress;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据结构
 */
@Data
public class IpSearch {

    private Map<Long,String> ipMap = new ConcurrentHashMap<>();

    private Map<String,String> provinceMap = new ConcurrentHashMap<>();

    private Map<String,String> cityMap = new ConcurrentHashMap<>();

    private Map<String,String> distinctMap = new ConcurrentHashMap<>();
}
