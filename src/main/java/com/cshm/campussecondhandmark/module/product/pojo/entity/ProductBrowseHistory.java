package com.cshm.campussecondhandmark.module.product.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_browse_history")
@ApiModel(description = "商品浏览历史实体")
public class ProductBrowseHistory {

    @ApiModelProperty(value = "浏览历史 ID")
    private Long id;

    @ApiModelProperty(value = "用户 ID")
    private Long userId;

    @ApiModelProperty(value = "商品 ID")
    private Long productId;

    @ApiModelProperty(value = "最近浏览时间")
    private LocalDateTime browseTime;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
