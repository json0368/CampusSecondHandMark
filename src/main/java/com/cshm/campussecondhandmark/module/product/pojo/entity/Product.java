package com.cshm.campussecondhandmark.module.product.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cshm.campussecondhandmark.module.product.enums.ProductAuditStatusEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductConditionEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductSaleStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
@ApiModel(description = "商品实体")
public class Product {

    @ApiModelProperty(value = "商品 ID")
    private Long id;

    @ApiModelProperty(value = "卖家用户 ID")
    private Long sellerId;

    @ApiModelProperty(value = "分类 ID")
    private Long categoryId;

    @ApiModelProperty(value = "商品标题")
    private String title;

    @ApiModelProperty(value = "商品描述")
    private String description;

    @ApiModelProperty(value = "商品价格")
    private BigDecimal price;

    @ApiModelProperty(value = "商品成色")
    private ProductConditionEnum conditionLevel;

    @ApiModelProperty(value = "封面图片 URL")
    private String coverImageUrl;

    @ApiModelProperty(value = "审核状态")
    private ProductAuditStatusEnum auditStatus;

    @ApiModelProperty(value = "销售状态")
    private ProductSaleStatusEnum saleStatus;

    @ApiModelProperty(value = "驳回原因")
    private String rejectReason;

    @ApiModelProperty(value = "发布时间")
    private LocalDateTime publishTime;

    @ApiModelProperty(value = "审核时间")
    private LocalDateTime auditTime;

    @ApiModelProperty(value = "下架时间")
    private LocalDateTime offShelfTime;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
