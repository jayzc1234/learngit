package com.zxs.filecontent;

import com.zxs.filecontent.context.ExtractContext;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * 已知命名文件指定内容提取
 * 主要逻辑是多线程读取文件内容提升速度，使用分段获取防止内存溢出，使用监听消费解耦读取数据逻辑和数据处理逻辑
 * 并允许多种消费实现
 * @date 2021/06/29 15:39
 * @author zc
 */
@Slf4j
public class ContentExtract {

    /**
     * 获取文件，开启读取文件内容任务
     * @param extractContext
     */
    public void extractContent(ExtractContext extractContext) {
        /**
         * 假设路径正确且该路径下的文件都是需要解析的文件
         */
        File file = new File(extractContext.getPath());
        /**
         * 并发消费文件内容
         */
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,15,5, TimeUnit.SECONDS,new ArrayBlockingQueue<>(100));
        for (File listFile : file.listFiles()) {
            threadPoolExecutor.execute(()->{
                try {
                    executeExtract(extractContext, listFile);
                } catch (IOException e) {
                   log.error("execute extract content error",e);
                    extractContext.getContentConsumer().onError(e);
                }
            });
        }

        /**
         * 消费完成通知
         */
        threadPoolExecutor.shutdown();
        while (threadPoolExecutor.isTerminated()){
            extractContext.getContentConsumer().done();
        }
    }

    /**
     * 从文件中抽取内容
     * @param extractContext
     * @param listFile
     * @throws IOException
     */
    private void executeExtract(ExtractContext extractContext,File listFile) throws IOException {
        int separateLen = Objects.isNull(extractContext.getSeparateLen())?500:extractContext.getSeparateLen();
        Predicate<String> predicate = extractContext.getPredicate();

        List<String> contentList = new ArrayList<>(separateLen);
        BufferedReader reader = null;
        int index = 0;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(listFile)));
            String content;
            while ((content=reader.readLine()) != null){
                /**
                 * 1.过滤满足含有error日志的内容
                 */
                if (predicate.test(content)){
                    /**
                     * 2.满足分段获取长度separateLen时开始消费
                     */
                    if (++index % separateLen == 0){
                        extractContext.getContentConsumer().consume(contentList);
                        contentList = new ArrayList<>();
                        index = 0;
                    }else {
                        contentList.add(content);
                    }
                }
            }
            /**
             * 3.最后处理不满足separateLen整数倍时的内容
             */
            extractContext.getContentConsumer().consume(contentList);
        }finally {
            if (!Objects.isNull(reader)){
                reader.close();
            }
        }
    }


}
