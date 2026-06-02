package com.cshm.campussecondhandmark.module.report.controller;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.result.Result;
import com.cshm.campussecondhandmark.module.report.pojo.dto.ReportCreateDTO;
import com.cshm.campussecondhandmark.module.report.service.ReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@Slf4j
@Api(tags = "举报接口")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    @ApiOperation("提交举报")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Long> createReport(@RequestBody ReportCreateDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("提交举报，用户ID={}，对象类型={}，对象ID={}", currentUserId, dto == null ? null : dto.getTargetType(), dto == null ? null : dto.getTargetId());
        return Result.success(reportService.createReport(currentUserId, dto));
    }
}
