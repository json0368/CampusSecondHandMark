package com.cshm.campussecondhandmark.module.user.pojo.dto;

import com.cshm.campussecondhandmark.module.user.enums.CampusVerifyStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "后台校园认证审核列表查询参数")
public class CampusVerifyQueryDTO {

    @ApiModelProperty(value = "关键字，按用户名、昵称、学号模糊搜索", example = "20240001")
    private String keyword;

    @ApiModelProperty(value = "校园认证状态，默认查 PENDING", example = "PENDING")
    private CampusVerifyStatusEnum campusVerifyStatus;

    @ApiModelProperty(value = "页码，从 1 开始", example = "1")
    private Integer pageNum;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize;
}
