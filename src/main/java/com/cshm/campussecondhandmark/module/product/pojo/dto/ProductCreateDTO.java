package com.cshm.campussecondhandmark.module.product.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel(description = "商品发布参数")
public class ProductCreateDTO {

    @ApiModelProperty(value = "商品分类 ID", required = true, example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "商品标题", required = true, example = "九成新高等数学教材")
    private String title;

    @ApiModelProperty(value = "商品描述", example = "同济版高数上册，轻微笔记，不影响正常使用")
    private String description;

    @ApiModelProperty(value = "商品价格", required = true, example = "25.00")
    private BigDecimal price;

    @ApiModelProperty(value = "成色等级，1=近乎全新，2=成色较好，3=有明显使用痕迹", required = true, example = "1")
    private Integer conditionLevel;

    @ApiModelProperty(value = "商品图片 URL 列表，第一张会作为封面图", required = true)
    private List<String> imageUrls;
}
