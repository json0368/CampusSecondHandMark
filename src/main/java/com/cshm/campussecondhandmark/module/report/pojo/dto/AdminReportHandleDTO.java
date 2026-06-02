package com.cshm.campussecondhandmark.module.report.pojo.dto;

import com.cshm.campussecondhandmark.module.report.enums.ReportHandleResultEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "后台处理举报参数")
public class AdminReportHandleDTO {

    @ApiModelProperty(value = "处理结果", required = true, example = "VALID")
    private ReportHandleResultEnum handleResult;

    @ApiModelProperty(value = "处理备注", example = "已核实")
    private String handleRemark;
}
