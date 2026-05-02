package com.cshm.campussecondhandmark.module.product.pojo.vo;

import com.cshm.campussecondhandmark.module.product.enums.ProductAuditStatusEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductSaleStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "我的商品列表项信息")
public class MyProductVO {

    @ApiModelProperty(value = "商品 ID", example = "10")
    private Long id;

    @ApiModelProperty(value = "商品标题", example = "九成新高等数学教材")
    private String title;

    @ApiModelProperty(value = "商品价格", example = "25.00")
    private BigDecimal price;

    @ApiModelProperty(value = "封面图片 URL", example = "http://example.com/product-cover.jpg")
    private String coverImageUrl;

    @ApiModelProperty(value = "审核状态", example = "PENDING")
    private ProductAuditStatusEnum auditStatus;

    @ApiModelProperty(value = "销售状态", example = "DRAFT")
    private ProductSaleStatusEnum saleStatus;

    @ApiModelProperty(value = "审核驳回原因", example = "商品描述不完整")
    private String rejectReason;

    @ApiModelProperty(value = "发布时间")
    private LocalDateTime publishTime;

    @ApiModelProperty(value = "最后更新时间")
    private LocalDateTime updateTime;
}
