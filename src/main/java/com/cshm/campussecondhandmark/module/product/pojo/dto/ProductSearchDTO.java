package com.cshm.campussecondhandmark.module.product.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "商品分页查询参数")
public class ProductSearchDTO {

    @ApiModelProperty(value = "关键词，按商品标题模糊搜索", example = "教材")
    private String keyword;

    @ApiModelProperty(value = "商品分类 ID", example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "最低价格", example = "10.00")
    private BigDecimal minPrice;

    @ApiModelProperty(value = "最高价格", example = "50.00")
    private BigDecimal maxPrice;

    @ApiModelProperty(value = "成色等级，1=近乎全新，2=成色较好，3=有明显使用痕迹", example = "1")
    private Integer conditionLevel;

    @ApiModelProperty(value = "页码，从 1 开始", example = "1")
    private Integer pageNum;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize;
}
