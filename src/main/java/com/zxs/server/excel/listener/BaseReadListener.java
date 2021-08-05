package com.zxs.server.excel.listener;


import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.app315.hydra.intelligent.planting.server.excel.data.BaseData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shixiongfei
 * @date 2020-02-20
 * @since
 */
@Slf4j
public abstract class BaseReadListener<T extends BaseData> extends AnalysisEventListener<T> {

    List<T> list = new ArrayList<>(BATCH_COUNT);

    /**
     * 每隔1000条存储数据库，实际使用中可以3000条，然后清理list, 方便内存回收
     */
    static final int BATCH_COUNT = 1000;

    @Override
    public void invoke(T data, AnalysisContext context) {
        val readSheetHolder = context.readSheetHolder();
        data.setSheetName(readSheetHolder.getSheetName());
        val readRowHolder = context.readRowHolder();
        data.setCurrentNum(readRowHolder.getRowIndex());
        // 参数校验 => 产品说权限放开，不做校验
        // EasyExcelValidHelper.validateEntity(data);
    }

    /**
     * 数据读取完毕后，会执行此回调方法
     * 如果存在多个sheet则该方法会被调用多次
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("{}条数据，开始存储数据库！", list.size());
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        // 如果是类型转换异常，则自定义抛出
        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException convertException = (ExcelDataConvertException) exception;
            val index = convertException.getExcelContentProperty().getHead().getColumnIndex();
            Throwable throwable = convertException.getCause();
            val data = new BaseData();
            data.setSheetName(context.readSheetHolder().getSheetName());
            // 行索引需要加1,因为表头占1行
            data.setCurrentNum(context.readRowHolder().getRowIndex() + index);
            //ExcelExUtil.throwEx(data, throwable.getMessage());
        }
        throw exception;
    }
}