package com.cshm.campussecondhandmark.module.product.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cshm.campussecondhandmark.module.product.enums.CategoryStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_category")
@ApiModel(description = "商品分类实体")
public class ProductCategory {

    @ApiModelProperty(value = "分类 ID")
    private Long id;

    @ApiModelProperty(value = "分类名称")
    private String name;

    @ApiModelProperty(value = "父分类 ID")
    private Long parentId;

    @ApiModelProperty(value = "排序值")
    private Integer sortOrder;

    @ApiModelProperty(value = "分类状态")
    private CategoryStatusEnum status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
