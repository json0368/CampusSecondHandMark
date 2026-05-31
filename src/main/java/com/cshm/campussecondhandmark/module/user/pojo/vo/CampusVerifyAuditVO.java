package com.cshm.campussecondhandmark.module.user.pojo.vo;

import com.cshm.campussecondhandmark.module.user.enums.CampusVerifyStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "后台校园认证审核列表项信息")
public class CampusVerifyAuditVO {

    @ApiModelProperty(value = "用户 ID", example = "3")
    private Long id;

    @ApiModelProperty(value = "用户名", example = "zhangsan")
    private String username;

    @ApiModelProperty(value = "昵称", example = "张三")
    private String nickname;

    @ApiModelProperty(value = "学号", example = "20240001")
    private String studentNo;

    @ApiModelProperty(value = "专业", example = "软件工程")
    private String major;

    @ApiModelProperty(value = "头像 URL", example = "http://example.com/avatar.jpg")
    private String avatarUrl;

    @ApiModelProperty(value = "校园认证状态", example = "PENDING")
    private CampusVerifyStatusEnum campusVerifyStatus;

    @ApiModelProperty(value = "审核备注", example = "学号与专业信息不一致")
    private String campusVerifyRemark;

    @ApiModelProperty(value = "审核时间")
    private LocalDateTime campusVerifyTime;

    @ApiModelProperty(value = "审核管理员 ID", example = "7")
    private Long campusVerifyAdminId;

    @ApiModelProperty(value = "注册时间")
    private LocalDateTime createTime;
}
