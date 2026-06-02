package com.cshm.campussecondhandmark.module.report.pojo.dto;

import com.cshm.campussecondhandmark.module.report.enums.ReportTargetTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "提交举报参数")
public class ReportCreateDTO {

    @ApiModelProperty(value = "举报对象类型", required = true, example = "PRODUCT")
    private ReportTargetTypeEnum targetType;

    @ApiModelProperty(value = "举报对象 ID", required = true, example = "10")
    private Long targetId;

    @ApiModelProperty(value = "举报原因", required = true, example = "涉嫌虚假宣传")
    private String reason;

    @ApiModelProperty(value = "举报补充说明", example = "商品描述和图片明显不一致")
    private String description;
}
