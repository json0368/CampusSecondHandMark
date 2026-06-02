package com.cshm.campussecondhandmark.module.report.pojo.dto;

import com.cshm.campussecondhandmark.module.report.enums.ReportHandleResultEnum;
import com.cshm.campussecondhandmark.module.report.enums.ReportStatusEnum;
import com.cshm.campussecondhandmark.module.report.enums.ReportTargetTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "后台举报列表查询参数")
public class AdminReportQueryDTO {

    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNum;

    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer pageSize;

    @ApiModelProperty(value = "举报状态", example = "PENDING")
    private ReportStatusEnum status;

    @ApiModelProperty(value = "举报对象类型", example = "PRODUCT")
    private ReportTargetTypeEnum targetType;

    @ApiModelProperty(value = "处理结果", example = "VALID")
    private ReportHandleResultEnum handleResult;

    @ApiModelProperty(value = "关键字", example = "虚假宣传")
    private String keyword;
}
