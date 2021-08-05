package com.zxs.server.excel.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author shixiongfei
 * @date 2020-02-20
 * @since
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseData {

    /**
     * sheet名称
     */
    String sheetName;

    /**
     * 当前行
     */
    Integer currentNum;
}