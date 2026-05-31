package com.cshm.campussecondhandmark.module.user.pojo.dto;

import com.cshm.campussecondhandmark.module.user.enums.CampusVerifyStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "后台校园认证审核参数")
public class CampusVerifyAuditDTO {

    @ApiModelProperty(value = "审核结果，只能传 APPROVED 或 REJECTED", required = true, example = "APPROVED")
    private CampusVerifyStatusEnum campusVerifyStatus;

    @ApiModelProperty(value = "审核备注，驳回时必填", example = "学号与专业信息不一致")
    private String remark;
}
