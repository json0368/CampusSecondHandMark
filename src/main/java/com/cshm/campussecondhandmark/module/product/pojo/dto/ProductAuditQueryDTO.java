package com.cshm.campussecondhandmark.module.product.pojo.dto;

import com.cshm.campussecondhandmark.module.product.enums.ProductAuditStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "后台商品审核列表查询参数")
public class ProductAuditQueryDTO {

    @ApiModelProperty(value = "关键词，按商品标题模糊搜索", example = "教材")
    private String keyword;

    @ApiModelProperty(value = "商品分类 ID", example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "审核状态，默认查询待审核商品", example = "PENDING")
    private ProductAuditStatusEnum auditStatus;

    @ApiModelProperty(value = "页码，从 1 开始", example = "1")
    private Integer pageNum;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize;
}
