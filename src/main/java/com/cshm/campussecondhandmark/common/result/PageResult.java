package com.cshm.campussecondhandmark.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "分页响应结果")
public class PageResult<T> implements Serializable {

    @ApiModelProperty(value = "总记录数", example = "25")
    private long total;

    @ApiModelProperty(value = "当前页数据列表")
    private List<T> records;
}
