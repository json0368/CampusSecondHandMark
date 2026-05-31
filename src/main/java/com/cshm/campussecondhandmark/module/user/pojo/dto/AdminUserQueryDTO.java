package com.cshm.campussecondhandmark.module.user.pojo.dto;

import com.cshm.campussecondhandmark.module.user.enums.UserStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "后台用户列表查询参数")
public class AdminUserQueryDTO {

    @ApiModelProperty(value = "关键字，按用户名、昵称、邮箱、学号模糊搜索", example = "zhangsan")
    private String keyword;

    @ApiModelProperty(value = "账号状态筛选", example = "BANNED")
    private UserStatusEnum status;

    @ApiModelProperty(value = "页码，从 1 开始", example = "1")
    private Integer pageNum;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize;
}
