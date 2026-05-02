package com.cshm.campussecondhandmark.module.product.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel(description = "商品修改参数")
public class ProductUpdateDTO {

    @ApiModelProperty(value = "商品分类 ID", example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "商品标题", example = "九成新高等数学教材")
    private String title;

    @ApiModelProperty(value = "商品描述", example = "补充了封面磨损说明")
    private String description;

    @ApiModelProperty(value = "商品价格", example = "20.00")
    private BigDecimal price;

    @ApiModelProperty(value = "成色等级，1=近乎全新，2=成色较好，3=有明显使用痕迹", example = "2")
    private Integer conditionLevel;

    @ApiModelProperty(value = "商品图片 URL 列表，传入后会整体替换原图片列表")
    private List<String> imageUrls;
}
