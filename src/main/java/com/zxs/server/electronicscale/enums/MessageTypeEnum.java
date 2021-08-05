package com.zxs.server.electronicscale.enums;

/**
 * 电子秤消息号类型
 * @author zc
 */
public enum MessageTypeEnum {
    /**
     * 消息号类型
     */
    REGISTER(1,"注册"),RECEIVE_WEIGHT(2,"接收重量");
    private Integer type;

    private String desc;

    MessageTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
