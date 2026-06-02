package com.cshm.campussecondhandmark.module.report.controller;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.common.result.Result;
import com.cshm.campussecondhandmark.module.report.pojo.dto.AdminReportHandleDTO;
import com.cshm.campussecondhandmark.module.report.pojo.dto.AdminReportQueryDTO;
import com.cshm.campussecondhandmark.module.report.pojo.vo.AdminReportDetailVO;
import com.cshm.campussecondhandmark.module.report.pojo.vo.AdminReportListVO;
import com.cshm.campussecondhandmark.module.report.service.ReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin-api/reports")
@Slf4j
@Api(tags = "后台举报管理")
public class AdminReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    @ApiOperation("分页查询举报列表")
    @ApiImplicitParam(name = "token", value = "管理员登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<PageResult<AdminReportListVO>> pageAdminReports(AdminReportQueryDTO dto) {
        log.info("后台分页查询举报列表");
        return Result.success(reportService.pageAdminReports(dto));
    }

    @GetMapping("/{reportId}")
    @ApiOperation("查询举报详情")
    @ApiImplicitParam(name = "token", value = "管理员登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<AdminReportDetailVO> getAdminReportDetail(
            @ApiParam(value = "举报 ID", required = true, example = "100") @PathVariable Long reportId) {
        log.info("后台查询举报详情，举报ID={}", reportId);
        return Result.success(reportService.getAdminReportDetail(reportId));
    }

    @PostMapping("/{reportId}/handle")
    @ApiOperation("处理举报")
    @ApiImplicitParam(name = "token", value = "管理员登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Void> handleReport(
            @ApiParam(value = "举报 ID", required = true, example = "100") @PathVariable Long reportId,
            @RequestBody AdminReportHandleDTO dto) {
        Long adminId = BaseContext.getCurrentId();
        log.info("后台处理举报，管理员ID={}，举报ID={}，处理结果={}", adminId, reportId, dto == null ? null : dto.getHandleResult());
        reportService.handleReport(reportId, adminId, dto);
        return Result.success();
    }
}
