package com.cshm.campussecondhandmark.module.product.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "后台强制下架商品参数")
public class AdminProductRemoveDTO {

    @ApiModelProperty(value = "强制下架原因", required = true, example = "商品涉嫌违规，已强制下架")
    private String reason;
}
