package com.zxs.server.excel.exception;

import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import net.app315.hydra.intelligent.planting.server.excel.data.BaseData;

/**
 * @author shixiongfei
 * @date 2020-02-20
 * @since
 */
public interface ExcelExUtil {

    /**
     * 抛出excel导入错误的异常
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2020-02-20
     * @updateDate 2020-02-20
     * @updatedBy shixiongfei
     */
    static <T extends BaseData> void throwEx(T data, String errorMsg) {
        throw new SuperCodeExtException(String.format("错误工作表名称 => %s, 错误行数 => %d, 错误原因 => %s",
                data.getSheetName(), data.getCurrentNum(), errorMsg));
    }

}