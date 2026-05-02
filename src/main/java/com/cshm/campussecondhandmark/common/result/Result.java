package com.cshm.campussecondhandmark.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "通用响应结果")
public class Result<T> implements Serializable {

    @ApiModelProperty(value = "业务状态码，1 表示成功，0 表示失败", example = "1")
    private Integer code;

    @ApiModelProperty(value = "响应消息", example = "操作成功")
    private String msg;

    @ApiModelProperty(value = "响应数据")
    private T data;

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 1;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<>();
        result.data = object;
        result.code = 1;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.code = 0;
        return result;
    }
}
