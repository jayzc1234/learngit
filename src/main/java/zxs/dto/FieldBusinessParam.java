package zxs.dto;

import org.hibernate.validator.constraints.NotEmpty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "字段业务数据")
public class FieldBusinessParam {
	@NotEmpty
    @ApiModelProperty(name = "fieldCode", value = "字段code", example = "FunctionName",required=true)
    private String fieldCode;
    
	@NotEmpty
    @ApiModelProperty(name = "fieldValue", value = "字段值", example = "搜索",required=true)
    private String fieldValue;
    
    @ApiModelProperty(name = "objectType", value = "对象类型，不是对象类型不需要传", example = "1001")
    private Integer objectType;

    @ApiModelProperty(name = "objectUniqueValue", value = "对象类型中唯一值，不是对象类型不需要传", example = "唯一主键id")
    private String objectUniqueValue;
    
	public String getFieldCode() {
		return fieldCode;
	}

	public void setFieldCode(String fieldCode) {
		this.fieldCode = fieldCode;
	}

	public String getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

	public Integer getObjectType() {
		return objectType;
	}

	public void setObjectType(Integer objectType) {
		this.objectType = objectType;
	}

	public String getObjectUniqueValue() {
		return objectUniqueValue;
	}

	public void setObjectUniqueValue(String objectUniqueValue) {
		this.objectUniqueValue = objectUniqueValue;
	}
    
    
}
