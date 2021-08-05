package com.zxs.filecontent;

import java.util.List;

public interface ContentConsumer<T> {

    /**
     * 内容消费
     * @param t
     */
    void consume(T t);

    /**
     * 消费异常
     * @param throwable
     */
    void onError(Throwable throwable);

    /**
     * 完成消费通知
     */
    void done();
}
