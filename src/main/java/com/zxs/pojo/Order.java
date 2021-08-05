package com.zxs.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_order")
public class Order {

    private  Integer id;

    private Integer userId;

    private Integer orderId;

    private String orderNo;

    private String userName;


    private int getData(){
        return 2;
    }
}
