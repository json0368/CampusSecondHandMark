package com.cshm.campussecondhandmark.module.product.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "商品分类信息")
public class ProductCategoryVO {

    @ApiModelProperty(value = "分类 ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "分类名称", example = "教材图书")
    private String name;

    @ApiModelProperty(value = "父分类 ID，一级分类为 0", example = "0")
    private Long parentId;

    @ApiModelProperty(value = "排序值", example = "1")
    private Integer sortOrder;
}
