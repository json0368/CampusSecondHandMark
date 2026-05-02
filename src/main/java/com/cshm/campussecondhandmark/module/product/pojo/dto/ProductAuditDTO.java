package com.cshm.campussecondhandmark.module.product.pojo.dto;

import com.cshm.campussecondhandmark.module.product.enums.ProductAuditStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "后台商品审核参数")
public class ProductAuditDTO {

    @ApiModelProperty(value = "审核结果，只能传 APPROVED 或 REJECTED", required = true, example = "APPROVED")
    private ProductAuditStatusEnum auditStatus;

    @ApiModelProperty(value = "驳回原因，审核驳回时必填", example = "商品图片不清晰，无法确认商品状态")
    private String rejectReason;
}
