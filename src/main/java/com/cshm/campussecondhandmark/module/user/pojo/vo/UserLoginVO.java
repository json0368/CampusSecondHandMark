package com.cshm.campussecondhandmark.module.user.pojo.vo;

import com.cshm.campussecondhandmark.module.user.enums.UserRoleEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户登录结果")
public class UserLoginVO {

    @ApiModelProperty(value = "用户 ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "用户昵称", example = "张三")
    private String nickname;

    @ApiModelProperty(value = "JWT 登录令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @ApiModelProperty(value = "用户角色", example = "USER")
    private UserRoleEnum role;
}
