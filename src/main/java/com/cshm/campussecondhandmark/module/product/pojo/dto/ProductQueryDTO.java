package com.cshm.campussecondhandmark.module.product.pojo.dto;

import com.cshm.campussecondhandmark.module.product.enums.ProductAuditStatusEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductSaleStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "我的商品列表查询参数")
public class ProductQueryDTO {

    @ApiModelProperty(value = "审核状态筛选", example = "PENDING")
    private ProductAuditStatusEnum auditStatus;

    @ApiModelProperty(value = "销售状态筛选", example = "ON_SHELF")
    private ProductSaleStatusEnum saleStatus;

    @ApiModelProperty(value = "页码，从 1 开始", example = "1")
    private Integer pageNum;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize;
}
