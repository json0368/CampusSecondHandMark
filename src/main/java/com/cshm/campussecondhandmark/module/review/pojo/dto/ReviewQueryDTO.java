package com.cshm.campussecondhandmark.module.review.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "评价分页查询参数")
public class ReviewQueryDTO {

    @ApiModelProperty(value = "页码，从 1 开始", example = "1")
    private Integer pageNum;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize;
}
