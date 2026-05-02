package com.cshm.campussecondhandmark.module.product.pojo.vo;

import com.cshm.campussecondhandmark.module.product.enums.ProductSaleStatusEnum;
import com.cshm.campussecondhandmark.module.user.enums.CampusVerifyStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "商品列表项信息")
public class ProductSummaryVO {

    @ApiModelProperty(value = "商品 ID", example = "10")
    private Long id;

    @ApiModelProperty(value = "商品标题", example = "九成新高等数学教材")
    private String title;

    @ApiModelProperty(value = "商品价格", example = "25.00")
    private BigDecimal price;

    @ApiModelProperty(value = "封面图片 URL", example = "http://example.com/product-cover.jpg")
    private String coverImageUrl;

    @ApiModelProperty(value = "分类 ID", example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "分类名称", example = "教材图书")
    private String categoryName;

    @ApiModelProperty(value = "成色等级代码", example = "1")
    private Integer conditionLevel;

    @ApiModelProperty(value = "卖家用户 ID", example = "2")
    private Long sellerId;

    @ApiModelProperty(value = "卖家昵称", example = "张三")
    private String sellerNickname;

    @ApiModelProperty(value = "卖家校园认证状态", example = "APPROVED")
    private CampusVerifyStatusEnum sellerCampusVerifyStatus;

    @ApiModelProperty(value = "销售状态", example = "ON_SHELF")
    private ProductSaleStatusEnum saleStatus;

    @ApiModelProperty(value = "发布时间")
    private LocalDateTime publishTime;
}
