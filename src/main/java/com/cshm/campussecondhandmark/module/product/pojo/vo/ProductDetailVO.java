package com.cshm.campussecondhandmark.module.product.pojo.vo;

import com.cshm.campussecondhandmark.module.product.enums.ProductSaleStatusEnum;
import com.cshm.campussecondhandmark.module.user.enums.CampusVerifyStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(description = "商品详情信息")
public class ProductDetailVO {

    @ApiModelProperty(value = "商品 ID", example = "10")
    private Long id;

    @ApiModelProperty(value = "卖家用户 ID", example = "2")
    private Long sellerId;

    @ApiModelProperty(value = "卖家昵称", example = "张三")
    private String sellerNickname;

    @ApiModelProperty(value = "卖家头像 URL", example = "http://example.com/avatar.jpg")
    private String sellerAvatarUrl;

    @ApiModelProperty(value = "卖家校园认证状态", example = "APPROVED")
    private CampusVerifyStatusEnum sellerCampusVerifyStatus;

    @ApiModelProperty(value = "分类 ID", example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "分类名称", example = "教材图书")
    private String categoryName;

    @ApiModelProperty(value = "商品标题", example = "九成新高等数学教材")
    private String title;

    @ApiModelProperty(value = "商品描述", example = "同济版高数上册，轻微笔记，不影响正常使用")
    private String description;

    @ApiModelProperty(value = "商品价格", example = "25.00")
    private BigDecimal price;

    @ApiModelProperty(value = "成色等级代码", example = "1")
    private Integer conditionLevel;

    @ApiModelProperty(value = "封面图片 URL", example = "http://example.com/product-cover.jpg")
    private String coverImageUrl;

    @ApiModelProperty(value = "商品图片列表")
    private List<ProductImageVO> images;

    @ApiModelProperty(value = "销售状态", example = "ON_SHELF")
    private ProductSaleStatusEnum saleStatus;

    @ApiModelProperty(value = "发布时间")
    private LocalDateTime publishTime;

    @ApiModelProperty(value = "当前用户是否可以下单", example = "true")
    private Boolean canOrder;

    @ApiModelProperty(value = "当前用户是否可以发起聊天", example = "true")
    private Boolean canChat;
}
