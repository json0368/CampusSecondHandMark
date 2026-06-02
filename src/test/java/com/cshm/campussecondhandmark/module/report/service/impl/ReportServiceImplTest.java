package com.cshm.campussecondhandmark.module.report.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.product.mapper.ProductMapper;
import com.cshm.campussecondhandmark.module.product.pojo.entity.Product;
import com.cshm.campussecondhandmark.module.report.enums.ReportHandleResultEnum;
import com.cshm.campussecondhandmark.module.report.enums.ReportStatusEnum;
import com.cshm.campussecondhandmark.module.report.enums.ReportTargetTypeEnum;
import com.cshm.campussecondhandmark.module.report.mapper.ContentReportMapper;
import com.cshm.campussecondhandmark.module.report.pojo.dto.AdminReportHandleDTO;
import com.cshm.campussecondhandmark.module.report.pojo.dto.AdminReportQueryDTO;
import com.cshm.campussecondhandmark.module.report.pojo.dto.ReportCreateDTO;
import com.cshm.campussecondhandmark.module.report.pojo.entity.ContentReport;
import com.cshm.campussecondhandmark.module.report.pojo.vo.AdminReportDetailVO;
import com.cshm.campussecondhandmark.module.report.pojo.vo.AdminReportListVO;
import com.cshm.campussecondhandmark.module.review.mapper.TradeReviewMapper;
import com.cshm.campussecondhandmark.module.review.pojo.entity.TradeReview;
import com.cshm.campussecondhandmark.module.user.enums.UserRoleEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserStatusEnum;
import com.cshm.campussecondhandmark.module.user.mapper.UserMapper;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ContentReportMapper contentReportMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private TradeReviewMapper tradeReviewMapper;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl();
        ReflectionTestUtils.setField(reportService, "contentReportMapper", contentReportMapper);
        ReflectionTestUtils.setField(reportService, "userMapper", userMapper);
        ReflectionTestUtils.setField(reportService, "productMapper", productMapper);
        ReflectionTestUtils.setField(reportService, "tradeReviewMapper", tradeReviewMapper);
    }

    @Test
    void createProductReportShouldSavePendingReport() {
        ReportCreateDTO dto = new ReportCreateDTO();
        dto.setTargetType(ReportTargetTypeEnum.PRODUCT);
        dto.setTargetId(10L);
        dto.setReason("虚假宣传");
        dto.setDescription("商品描述和图片明显不一致");

        Product product = new Product();
        product.setId(10L);
        product.setSellerId(3L);

        when(userMapper.selectById(2L)).thenReturn(normalUser(2L, "buyer"));
        when(productMapper.selectById(10L)).thenReturn(product);
        when(contentReportMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            ContentReport report = invocation.getArgument(0);
            report.setId(100L);
            return 1;
        }).when(contentReportMapper).insert(any(ContentReport.class));

        Long reportId = reportService.createReport(2L, dto);

        assertEquals(100L, reportId);
        verify(contentReportMapper).insert(any(ContentReport.class));
    }

    @Test
    void createReportShouldRejectDuplicateHandledReport() {
        ReportCreateDTO dto = new ReportCreateDTO();
        dto.setTargetType(ReportTargetTypeEnum.USER);
        dto.setTargetId(8L);
        dto.setReason("恶意骚扰");

        ContentReport report = new ContentReport();
        report.setId(100L);
        report.setReporterId(2L);
        report.setTargetType(ReportTargetTypeEnum.USER);
        report.setTargetId(8L);
        report.setStatus(ReportStatusEnum.HANDLED);

        when(userMapper.selectById(2L)).thenReturn(normalUser(2L, "buyer"));
        when(userMapper.selectById(8L)).thenReturn(normalUser(8L, "target"));
        when(contentReportMapper.selectOne(any())).thenReturn(report);

        BaseException exception = assertThrows(BaseException.class, () -> reportService.createReport(2L, dto));

        assertEquals("该对象已处理过举报，请勿重复提交", exception.getMessage());
    }

    @Test
    void createReportShouldRejectBannedUser() {
        ReportCreateDTO dto = new ReportCreateDTO();
        dto.setTargetType(ReportTargetTypeEnum.USER);
        dto.setTargetId(8L);
        dto.setReason("恶意骚扰");

        User bannedUser = normalUser(2L, "buyer");
        bannedUser.setStatus(UserStatusEnum.BANNED);
        when(userMapper.selectById(2L)).thenReturn(bannedUser);

        BaseException exception = assertThrows(BaseException.class, () -> reportService.createReport(2L, dto));

        assertEquals("账号已被封禁", exception.getMessage());
    }

    @Test
    void handleReportShouldWriteHandleMetadata() {
        AdminReportHandleDTO dto = new AdminReportHandleDTO();
        dto.setHandleResult(ReportHandleResultEnum.VALID);
        dto.setHandleRemark("已核实，交由运营继续处理");

        ContentReport report = pendingProductReport();
        when(contentReportMapper.selectById(100L)).thenReturn(report);
        when(contentReportMapper.updateById(any(ContentReport.class))).thenReturn(1);

        reportService.handleReport(100L, 7L, dto);

        verify(contentReportMapper).updateById(any(ContentReport.class));
    }

    @Test
    void getAdminReportDetailShouldReturnTargetOwnerForReview() {
        ContentReport report = pendingReviewReport();
        TradeReview review = new TradeReview();
        review.setId(30L);
        review.setReviewerId(9L);
        review.setContent("辱骂性评价内容");

        when(contentReportMapper.selectById(100L)).thenReturn(report);
        when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(normalUser(2L, "reporter"), normalUser(9L, "reviewer")));
        when(tradeReviewMapper.selectById(30L)).thenReturn(review);

        AdminReportDetailVO detail = reportService.getAdminReportDetail(100L);

        assertEquals(9L, detail.getTargetOwnerId());
    }

    @Test
    void pageAdminReportsShouldBatchLoadReporterAndTargets() {
        ContentReport productReport = pendingProductReport();
        ContentReport userReport = pendingUserReport();
        ContentReport reviewReport = pendingReviewReport();

        Page<ContentReport> page = new Page<>(1, 10);
        page.setTotal(3);
        page.setRecords(List.of(productReport, userReport, reviewReport));

        Product product = new Product();
        product.setId(10L);
        product.setTitle("高等数学教材");

        TradeReview review = new TradeReview();
        review.setId(30L);
        review.setReviewerId(9L);
        review.setContent("辱骂性评价内容，明显带有人身攻击");

        when(contentReportMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(
                normalUser(2L, "reporter"),
                normalUser(8L, "target"),
                normalUser(9L, "reviewer")));
        when(productMapper.selectBatchIds(anyCollection())).thenReturn(List.of(product));
        when(tradeReviewMapper.selectBatchIds(anyCollection())).thenReturn(List.of(review));

        PageResult<AdminReportListVO> result = reportService.pageAdminReports(new AdminReportQueryDTO());

        assertEquals(3, result.getTotal());
        assertEquals(3, result.getRecords().size());
        assertEquals("高等数学教材", result.getRecords().get(0).getTargetSummary());
        assertEquals("target", result.getRecords().get(1).getTargetSummary());
        assertEquals("辱骂性评价内容，明显带有人身攻击", result.getRecords().get(2).getTargetSummary());
        verify(userMapper).selectBatchIds(anyCollection());
        verify(productMapper).selectBatchIds(anyCollection());
        verify(tradeReviewMapper).selectBatchIds(anyCollection());
        verify(productMapper, never()).selectById(10L);
        verify(userMapper, never()).selectById(8L);
        verify(tradeReviewMapper, never()).selectById(30L);
    }

    @Test
    void pageAdminReportsShouldSearchReasonDescriptionAndReporterKeyword() {
        AdminReportQueryDTO dto = new AdminReportQueryDTO();
        dto.setKeyword("虚假");

        when(userMapper.selectList(any())).thenReturn(List.of(normalUser(2L, "reporter")));
        when(contentReportMapper.selectPage(any(Page.class), any())).thenReturn(new Page<>(1, 10));

        reportService.pageAdminReports(dto);

        verify(userMapper).selectList(any());
        verify(contentReportMapper).selectPage(any(Page.class), any());
    }

    private ContentReport pendingProductReport() {
        ContentReport report = new ContentReport();
        report.setId(100L);
        report.setReporterId(2L);
        report.setTargetType(ReportTargetTypeEnum.PRODUCT);
        report.setTargetId(10L);
        report.setReason("虚假宣传");
        report.setStatus(ReportStatusEnum.PENDING);
        report.setCreateTime(LocalDateTime.now());
        return report;
    }

    private ContentReport pendingUserReport() {
        ContentReport report = new ContentReport();
        report.setId(101L);
        report.setReporterId(2L);
        report.setTargetType(ReportTargetTypeEnum.USER);
        report.setTargetId(8L);
        report.setReason("恶意骚扰");
        report.setDescription("频繁私信辱骂");
        report.setStatus(ReportStatusEnum.PENDING);
        report.setCreateTime(LocalDateTime.now());
        return report;
    }

    private ContentReport pendingReviewReport() {
        ContentReport report = new ContentReport();
        report.setId(102L);
        report.setReporterId(2L);
        report.setTargetType(ReportTargetTypeEnum.REVIEW);
        report.setTargetId(30L);
        report.setReason("内容不当");
        report.setStatus(ReportStatusEnum.PENDING);
        report.setCreateTime(LocalDateTime.now());
        return report;
    }

    private User normalUser(Long id, String nickname) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        user.setUsername(nickname);
        user.setRole(UserRoleEnum.USER);
        user.setStatus(UserStatusEnum.NORMAL);
        return user;
    }
}
