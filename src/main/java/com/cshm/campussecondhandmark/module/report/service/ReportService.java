package com.cshm.campussecondhandmark.module.report.service;

import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.report.pojo.dto.AdminReportHandleDTO;
import com.cshm.campussecondhandmark.module.report.pojo.dto.AdminReportQueryDTO;
import com.cshm.campussecondhandmark.module.report.pojo.dto.ReportCreateDTO;
import com.cshm.campussecondhandmark.module.report.pojo.vo.AdminReportDetailVO;
import com.cshm.campussecondhandmark.module.report.pojo.vo.AdminReportListVO;

public interface ReportService {

    Long createReport(Long currentUserId, ReportCreateDTO dto);

    PageResult<AdminReportListVO> pageAdminReports(AdminReportQueryDTO dto);

    AdminReportDetailVO getAdminReportDetail(Long reportId);

    void handleReport(Long reportId, Long adminId, AdminReportHandleDTO dto);
}
