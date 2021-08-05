package com.zxs.filecontent.fileway;

import com.zxs.filecontent.ContentConsumer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于文件的操作。
 * 核心思想：将每个日志文件的error级别日志先按天写到单独文件中，如/2021-05-26/error.2021-05-26-1.log,
 *                                                            /2021-05-26/error.2021-05-26-2.log
 * 文件后面的-1，-2代表的是线程id，为了减少多线程写文件竞争所以每天的数据每个线程写不同文件
 *
 * 然后对每天的error.2021-*-*.log内容加载到内存进行排序， 最后根据文件名排序将所有文件内容合并到error.log文件中
 * ps：如果按日分的数据量还是很大可以再按小时分，先把每个小时的数据排序然后再合并成按天的数据最后再把按天的数据写到一个error.log文件
 *
 * @author zc
 */
@Slf4j
public class FileContentConsumer implements ContentConsumer<List<String>> {

    public Map<String, String> fileMap = new ConcurrentHashMap<>();

    public final String FILE_SUBFfIX = ".log";
    public final String FILE_PREFIX = "error.";
    @Override
    public void consume(List<String> contentList) {
        /**
         * 1.根据年月日对数据进行分组
         */
        Map<String, List<String>> dateMap = groupData(contentList);

        /**
         * 2.创建对应日期文件
         *   将数据写到对应日期文件中
         */
        writeToFile(dateMap);
    }

    /**
     * 将数据按照年-月-日划分写入到年-月-日文件夹下
     * 随着执行的线程不同将在年-月-日文件夹下生成多个error.年-月-日-线程id.log文件
     * @param dateMap 每个年-月-日对应的日志数据
     */
    private void writeToFile(Map<String, List<String>> dateMap) {
        for (String date : dateMap.keySet()) {
            File file = new File(date);
            try {
                if (!file.exists()){
                    file.mkdirs();
                }
            }catch (Exception e){
            }

            BufferedWriter writer = null;
            String filePath = date + File.separator + FILE_PREFIX + date+"-"+Thread.currentThread().getId() + FILE_SUBFfIX;
            try {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));
                for (String key : dateMap.keySet()) {
                    for (String content : dateMap.get(key)) {
                        writer.write(content);
                    }
                    writer.flush();
                }
            } catch (IOException e) {
               log.error("write to file:{} failed",filePath,e);
            }finally {
                if (!Objects.isNull(writer)){
                    try {
                        writer.close();
                    } catch (IOException e) {
                    }
                }
            }

            /**
             * 记录error日志目录
             */
            fileMap.put(date,null);
        }
    }

    private Map<String, List<String>> groupData(List<String> contentList) {
        Map<String, List<String>> dateMap = new HashMap<>();
        for (String content : contentList) {
            String date = content.substring(0,10);
            List<String> list = dateMap.get(date);
            if (Objects.isNull(list)){
                list = new ArrayList<>();
                dateMap.put(date,list);
            }
            list.add(content);
        }
        return dateMap;
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void done() {
        /**
         * 将每个日期目录下的/error.日期-线程id.log文件排序写到一个 error.日期.log文件中
         */
        combineSingeDateFile();

        /**
         * 按照文件名大小反序获取每个日期目录下的/error.日期.log文件内容写入error.log文件
         * 比如先获取2021-06-29文件夹下的error.2021-06-29.log文件写入error.log文件，
         * 再获取2021-06-28文件夹下的error.2021-06-28.log文件内容写入error.log文件
         * 具体实现就是写文件就不是写了
         */
        //writeDataToErrorLog();
    }

    /**
     * 将每个日期目录下的/error.日期-线程id.log文件排序写到一个 error.日期.log文件中
     */
    private void combineSingeDateFile() {
        for (String date : fileMap.keySet()) {
           Set<String> dateSet = new TreeSet<>();
            /**
             * 1.将小文件数据写到有序dateSet中
             */
           File d = new File(date);
            for (File file : d.listFiles()) {
                RandomAccessFile randomAccessFile = null;
                try {
                    String content = null;
                    randomAccessFile = new RandomAccessFile(file,"rw");
                    while ((content = randomAccessFile.readLine()) != null){
                        dateSet.add(content);
                    }
                }  catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if (!Objects.isNull(randomAccessFile)) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }

            /**
             *2.将有序数据写到一个error.日期.log文件中
             */
            List<String> contentList = new ArrayList<>(dateSet.size());
            contentList.addAll(dateSet);
            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile(date + File.separator + FILE_PREFIX + date + FILE_SUBFfIX,"rw");
                for (int index = contentList.size()-1;index>=0;index--){
                    randomAccessFile.writeChars(contentList.get(index));
                }
            }catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (!Objects.isNull(randomAccessFile)) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                    }
                }
            }

        }
    }

}
