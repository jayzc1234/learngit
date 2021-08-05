package com.zxs.filecontent.databaseway;

import com.zxs.filecontent.ContentConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * 核心思想就是把数据先解析出日期然后把日期和完整数据存到数据库或者es中，然后根据时间字段排序分页查询出数据
 * 再反写到error.log文件中
 * @author zc
 */
public class DataBaseContentConsumer implements ContentConsumer<List<String>> {
    /**
     * 伪代码 用来操作ContentPO对应的表t_log_content
     */
     private String jdbc;

    @Override
    public void consume(List<String> contentList) {
        List<ContentPO> contentPOS = new ArrayList<>(contentList.size());
        for (String content : contentList) {
            String[] split = content.split("\\.");
            ContentPO contentPO = new ContentPO();
            contentPO.setLogDate(split[0]);
            contentPO.setLogText(content);
            contentPOS.add(contentPO);
        }
        /**
         * 最后一步使用伪代码代替
         */
        //jdbc.saveBatch(contentPOS);
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void done() {
        /**
         * 1.使用分页查询从数据库中分页获取error级别日志文本
         */
       // List<String> contentPOS=select log_text from t_log_content order by log_date desc limit 0,500

        /**
         * 2.写入error.log文件
         */
        //write contentPOS to error.log
    }
}
