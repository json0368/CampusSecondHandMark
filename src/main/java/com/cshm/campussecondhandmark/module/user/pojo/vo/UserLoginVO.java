package com.cshm.campussecondhandmark.module.user.pojo.vo;

import com.cshm.campussecondhandmark.module.user.enums.UserRoleEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Api(tags = "用户登录 VO")
public class UserLoginVO {

    @ApiModelProperty(value = "用户 ID")
    private Long id;

    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "JWT 令牌")
    private String token;

    @ApiModelProperty(value = "用户角色")
    private UserRoleEnum role;
}
