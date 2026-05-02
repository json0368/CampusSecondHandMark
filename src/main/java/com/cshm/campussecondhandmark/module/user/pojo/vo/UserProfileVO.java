package com.cshm.campussecondhandmark.module.user.pojo.vo;

import com.cshm.campussecondhandmark.module.user.enums.CampusVerifyStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户公开资料")
public class UserProfileVO {

    @ApiModelProperty(value = "用户 ID")
    private Long id;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "头像 URL")
    private String avatarUrl;

    @ApiModelProperty(value = "专业")
    private String major;

    @ApiModelProperty(value = "校园认证状态")
    private CampusVerifyStatusEnum campusVerifyStatus;
}
