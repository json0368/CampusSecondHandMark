package com.cshm.campussecondhandmark.module.report.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cshm.campussecondhandmark.module.report.enums.ReportHandleResultEnum;
import com.cshm.campussecondhandmark.module.report.enums.ReportStatusEnum;
import com.cshm.campussecondhandmark.module.report.enums.ReportTargetTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("content_report")
@ApiModel(description = "举报实体")
public class ContentReport {

    @ApiModelProperty(value = "举报 ID")
    private Long id;

    @ApiModelProperty(value = "举报人用户 ID")
    private Long reporterId;

    @ApiModelProperty(value = "举报对象类型")
    private ReportTargetTypeEnum targetType;

    @ApiModelProperty(value = "举报对象 ID")
    private Long targetId;

    @ApiModelProperty(value = "举报原因")
    private String reason;

    @ApiModelProperty(value = "举报补充说明")
    private String description;

    @ApiModelProperty(value = "举报状态")
    private ReportStatusEnum status;

    @ApiModelProperty(value = "处理结果")
    private ReportHandleResultEnum handleResult;

    @ApiModelProperty(value = "处理备注")
    private String handleRemark;

    @ApiModelProperty(value = "处理管理员 ID")
    private Long handleAdminId;

    @ApiModelProperty(value = "处理时间")
    private LocalDateTime handleTime;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
