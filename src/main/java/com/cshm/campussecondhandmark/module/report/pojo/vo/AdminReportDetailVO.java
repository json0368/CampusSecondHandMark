package com.cshm.campussecondhandmark.module.report.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "后台举报详情")
public class AdminReportDetailVO extends AdminReportListVO {

    @ApiModelProperty(value = "举报补充说明", example = "商品描述和图片明显不一致")
    private String description;

    @ApiModelProperty(value = "被举报对象归属用户 ID", example = "3")
    private Long targetOwnerId;

    @ApiModelProperty(value = "被举报对象归属用户昵称", example = "卖家")
    private String targetOwnerNickname;

    @ApiModelProperty(value = "处理备注", example = "已核实")
    private String handleRemark;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
