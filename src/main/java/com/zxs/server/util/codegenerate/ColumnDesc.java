package com.zxs.server.util.codegenerate;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ColumnDesc {
    private String tableName;
    private String columnName;
    private String type;
    private Integer columnSize;
    private String desc;
    private String isNullable;
    private String defaultValue;
    private boolean isPrimaryKey;
    private String DECIMAL_DIGITS;
}
