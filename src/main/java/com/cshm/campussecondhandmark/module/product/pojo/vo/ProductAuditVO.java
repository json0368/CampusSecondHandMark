package com.cshm.campussecondhandmark.module.product.pojo.vo;

import com.cshm.campussecondhandmark.module.product.enums.ProductAuditStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "后台商品审核列表项信息")
public class ProductAuditVO {

    @ApiModelProperty(value = "商品 ID", example = "10")
    private Long id;

    @ApiModelProperty(value = "商品标题", example = "九成新高等数学教材")
    private String title;

    @ApiModelProperty(value = "卖家用户 ID", example = "2")
    private Long sellerId;

    @ApiModelProperty(value = "卖家昵称", example = "张三")
    private String sellerNickname;

    @ApiModelProperty(value = "分类名称", example = "教材图书")
    private String categoryName;

    @ApiModelProperty(value = "商品价格", example = "25.00")
    private BigDecimal price;

    @ApiModelProperty(value = "成色等级代码", example = "1")
    private Integer conditionLevel;

    @ApiModelProperty(value = "封面图片 URL", example = "http://example.com/product-cover.jpg")
    private String coverImageUrl;

    @ApiModelProperty(value = "审核状态", example = "PENDING")
    private ProductAuditStatusEnum auditStatus;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
