package com.zxs.ipaddress;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.Objects;

/**
 * 地理位置对应IP地址存储结构设计
 * @author zc
 */
public class ExecuteIpSearch {

    private IpSearch ipSearch = new IpSearch();
    /**
     * 初始化
     * @param inputStream
     */
    public void init(InputStream inputStream) throws IOException {

        BufferedReader reader = null;
        try {
            reader =new BufferedReader(new InputStreamReader(inputStream));
            String content;
            while ((content=reader.readLine()) != null){
                String[] split = content.split(",");
                buildData(split[0],split[1],split[2],split[3],split[4]);
            }
        }finally {
            if (!Objects.isNull(reader)){
                reader.close();
            }
        }
    }

    /**
     * 搜索
     * @param ip
     * @return
     */
    public String search(String ip) {
        String district = ipSearch.getIpMap().get(Long.parseLong(ip.replace(".", "")));
        String city = ipSearch.getDistinctMap().get(district);
        String province = ipSearch.getCityMap().get(city);
        String country = ipSearch.getProvinceMap().get(province);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("country",country);
        jsonObject.put("province",province);
        jsonObject.put("city",city);
        jsonObject.put("district",district);
        return jsonObject.toJSONString();
    }

    /**
     * 构建内存ipSearch数据
     * @param ip
     * @param nation
     * @param province
     * @param city
     * @param district
     */
    private void buildData(String ip, String nation, String province, String city, String district) {
        ipSearch.getProvinceMap().put(province,nation);
        ipSearch.getCityMap().put(city,province);
        ipSearch.getDistinctMap().put(district,city);
        ipSearch.getIpMap().put(Long.parseLong(ip.replace(".","")),district);
    }

}
