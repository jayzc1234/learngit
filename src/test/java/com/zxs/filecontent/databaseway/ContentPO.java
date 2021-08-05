package com.zxs.filecontent.databaseway;

import lombok.Data;

@Data
public class ContentPO {

    /**
     * 日志日期--正常使用Date类型
     */
    private String logDate;

    /**
     * 日志完整内容
     */
    private String logText;

}
