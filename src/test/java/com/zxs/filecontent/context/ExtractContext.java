package com.zxs.filecontent.context;

import com.zxs.filecontent.ContentConsumer;
import lombok.Data;

import java.util.List;
import java.util.function.Predicate;

/**
 * 文件文本抽取上下文
 * @author zc
 */
@Data
public class ExtractContext {

    /**
     * 文件所在目录地址
     */
    private String path;
    /**
     * 文件内容过滤器
     */
    private Predicate<String> predicate = f-> {return true;};

    /**
     * 分段长度
     */
    private Integer separateLen;

    /**
     * 文件内容消费者
     */
    private ContentConsumer contentConsumer;

}
