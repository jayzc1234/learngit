package com.zxs.server.service.gugeng.common;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import net.app315.hydra.intelligent.planting.common.gugeng.model.excel.HistoryOrderImportExcelHead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 模板的读取类
 *
 * @author Jiaju Zhuang
 */
public class HistoryOrderImportAnalysisEventListener extends AnalysisEventListener<HistoryOrderImportExcelHead> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryOrderImportAnalysisEventListener.class);
    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */

    private ApplicationContext applicationContext;

    private boolean isHistoryImport=true;
    private Date orderDate;
    public HistoryOrderImportAnalysisEventListener(ApplicationContext applicationContext, Date orderDate) {
        this.applicationContext = applicationContext;
        this.orderDate=orderDate;
    }

    private static final int BATCH_COUNT = 5;
    List<HistoryOrderImportExcelHead> list = new ArrayList<HistoryOrderImportExcelHead>();

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        System.out.println(headMap);
    }

    @Override
    public void invoke(HistoryOrderImportExcelHead data, AnalysisContext context) {
        HistoryOrderImportService orderImportService=applicationContext.getBean(HistoryOrderImportService.class);
        orderImportService.invoke(data,orderDate,context);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        LOGGER.info("所有数据解析完成！");
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
        LOGGER.info("{}条数据，开始存储数据库！", list.size());
        LOGGER.info("存储数据库成功！");
    }
}
