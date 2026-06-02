package com.cshm.campussecondhandmark.module.report.pojo.vo;

import com.cshm.campussecondhandmark.module.report.enums.ReportHandleResultEnum;
import com.cshm.campussecondhandmark.module.report.enums.ReportStatusEnum;
import com.cshm.campussecondhandmark.module.report.enums.ReportTargetTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "后台举报列表项")
public class AdminReportListVO {

    @ApiModelProperty(value = "举报 ID", example = "100")
    private Long id;

    @ApiModelProperty(value = "举报人用户 ID", example = "2")
    private Long reporterId;

    @ApiModelProperty(value = "举报人用户名", example = "buyer")
    private String reporterUsername;

    @ApiModelProperty(value = "举报人昵称", example = "买家")
    private String reporterNickname;

    @ApiModelProperty(value = "举报对象类型", example = "PRODUCT")
    private ReportTargetTypeEnum targetType;

    @ApiModelProperty(value = "举报对象 ID", example = "10")
    private Long targetId;

    @ApiModelProperty(value = "举报对象摘要", example = "高等数学教材")
    private String targetSummary;

    @ApiModelProperty(value = "举报原因", example = "涉嫌虚假宣传")
    private String reason;

    @ApiModelProperty(value = "举报状态", example = "PENDING")
    private ReportStatusEnum status;

    @ApiModelProperty(value = "处理结果", example = "VALID")
    private ReportHandleResultEnum handleResult;

    @ApiModelProperty(value = "处理管理员 ID", example = "7")
    private Long handleAdminId;

    @ApiModelProperty(value = "处理时间")
    private LocalDateTime handleTime;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
