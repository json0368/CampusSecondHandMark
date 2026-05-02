package com.cshm.campussecondhandmark.module.product.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "商品图片信息")
public class ProductImageVO {

    @ApiModelProperty(value = "图片 URL", example = "http://example.com/product-1.jpg")
    private String imageUrl;

    @ApiModelProperty(value = "图片排序值", example = "1")
    private Integer sortOrder;
}
