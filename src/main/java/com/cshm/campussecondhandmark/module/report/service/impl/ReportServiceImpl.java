package com.cshm.campussecondhandmark.module.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.product.mapper.ProductMapper;
import com.cshm.campussecondhandmark.module.product.pojo.entity.Product;
import com.cshm.campussecondhandmark.module.report.enums.ReportStatusEnum;
import com.cshm.campussecondhandmark.module.report.enums.ReportTargetTypeEnum;
import com.cshm.campussecondhandmark.module.report.mapper.ContentReportMapper;
import com.cshm.campussecondhandmark.module.report.pojo.dto.AdminReportHandleDTO;
import com.cshm.campussecondhandmark.module.report.pojo.dto.AdminReportQueryDTO;
import com.cshm.campussecondhandmark.module.report.pojo.dto.ReportCreateDTO;
import com.cshm.campussecondhandmark.module.report.pojo.entity.ContentReport;
import com.cshm.campussecondhandmark.module.report.pojo.vo.AdminReportDetailVO;
import com.cshm.campussecondhandmark.module.report.pojo.vo.AdminReportListVO;
import com.cshm.campussecondhandmark.module.report.service.ReportService;
import com.cshm.campussecondhandmark.module.review.mapper.TradeReviewMapper;
import com.cshm.campussecondhandmark.module.review.pojo.entity.TradeReview;
import com.cshm.campussecondhandmark.module.user.enums.UserRoleEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserStatusEnum;
import com.cshm.campussecondhandmark.module.user.mapper.UserMapper;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    @Autowired
    private ContentReportMapper contentReportMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private TradeReviewMapper tradeReviewMapper;

    @Override
    @Transactional
    public Long createReport(Long currentUserId, ReportCreateDTO dto) {
        User reporter = getNormalUserOrThrow(currentUserId);
        validateCreateDto(dto);
        assertTargetExistsAndNotSelf(currentUserId, dto.getTargetType(), dto.getTargetId());

        ContentReport existed = getReportByReporterAndTarget(currentUserId, dto.getTargetType(), dto.getTargetId());
        if (existed != null) {
            if (existed.getStatus() == ReportStatusEnum.PENDING) {
                throw new BaseException("该对象已举报，等待平台处理");
            }
            throw new BaseException("该对象已处理过举报，请勿重复提交");
        }

        LocalDateTime now = LocalDateTime.now();
        ContentReport report = new ContentReport();
        report.setReporterId(reporter.getId());
        report.setTargetType(dto.getTargetType());
        report.setTargetId(dto.getTargetId());
        report.setReason(trimRequired(dto.getReason(), 100, "举报原因不能为空"));
        report.setDescription(trimOptional(dto.getDescription(), 500));
        report.setStatus(ReportStatusEnum.PENDING);
        report.setCreateTime(now);
        report.setUpdateTime(now);
        if (contentReportMapper.insert(report) <= 0) {
            throw new BaseException("提交举报失败");
        }
        return report.getId();
    }

    @Override
    public PageResult<AdminReportListVO> pageAdminReports(AdminReportQueryDTO dto) {
        AdminReportQueryDTO queryDTO = dto == null ? new AdminReportQueryDTO() : dto;
        Page<ContentReport> page = new Page<>(normalizePageNum(queryDTO), normalizePageSize(queryDTO));
        LambdaQueryWrapper<ContentReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(queryDTO.getStatus() == null, ContentReport::getStatus, ReportStatusEnum.PENDING)
                .eq(queryDTO.getStatus() != null, ContentReport::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getTargetType() != null, ContentReport::getTargetType, queryDTO.getTargetType())
                .eq(queryDTO.getHandleResult() != null, ContentReport::getHandleResult, queryDTO.getHandleResult());

        applyKeywordFilter(queryWrapper, queryDTO);

        queryWrapper.orderByDesc(ContentReport::getCreateTime)
                .orderByDesc(ContentReport::getId);

        Page<ContentReport> resultPage = contentReportMapper.selectPage(page, queryWrapper);
        return new PageResult<>(resultPage.getTotal(), buildAdminReportList(resultPage.getRecords()));
    }

    @Override
    public AdminReportDetailVO getAdminReportDetail(Long reportId) {
        ContentReport report = getReportOrThrow(reportId);
        AdminReportDetailVO detailVO = new AdminReportDetailVO();
        BeanUtils.copyProperties(report, detailVO);

        Map<Long, User> reporterMap = mapUsersById(List.of(report.getReporterId()));
        User reporter = reporterMap.get(report.getReporterId());
        if (reporter != null) {
            detailVO.setReporterUsername(reporter.getUsername());
            detailVO.setReporterNickname(reporter.getNickname());
        }

        fillTargetInfo(detailVO, report);
        return detailVO;
    }

    @Override
    @Transactional
    public void handleReport(Long reportId, Long adminId, AdminReportHandleDTO dto) {
        if (adminId == null) {
            throw new BaseException("管理员未登录");
        }
        if (dto == null || dto.getHandleResult() == null) {
            throw new BaseException("处理结果不能为空");
        }

        ContentReport report = getReportOrThrow(reportId);
        if (report.getStatus() == ReportStatusEnum.HANDLED) {
            throw new BaseException("举报已处理");
        }

        LocalDateTime now = LocalDateTime.now();
        report.setStatus(ReportStatusEnum.HANDLED);
        report.setHandleResult(dto.getHandleResult());
        report.setHandleRemark(trimOptional(dto.getHandleRemark(), 500));
        report.setHandleAdminId(adminId);
        report.setHandleTime(now);
        report.setUpdateTime(now);
        if (contentReportMapper.updateById(report) <= 0) {
            throw new BaseException("处理举报失败");
        }
    }

    private User getNormalUserOrThrow(Long userId) {
        if (userId == null) {
            throw new BaseException("用户未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        if (user.getRole() != UserRoleEnum.USER) {
            throw new BaseException("举报人必须是普通用户");
        }
        if (user.getStatus() == UserStatusEnum.BANNED) {
            throw new BaseException("账号已被封禁");
        }
        return user;
    }

    private void validateCreateDto(ReportCreateDTO dto) {
        if (dto == null) {
            throw new BaseException("举报参数不能为空");
        }
        if (dto.getTargetType() == null) {
            throw new BaseException("举报对象类型不能为空");
        }
        if (dto.getTargetId() == null) {
            throw new BaseException("举报对象不能为空");
        }
        trimRequired(dto.getReason(), 100, "举报原因不能为空");
        trimOptional(dto.getDescription(), 500);
    }

    private void assertTargetExistsAndNotSelf(Long currentUserId, ReportTargetTypeEnum targetType, Long targetId) {
        if (targetType == ReportTargetTypeEnum.PRODUCT) {
            Product product = productMapper.selectById(targetId);
            if (product == null) {
                throw new BaseException("举报对象不存在");
            }
            if (currentUserId.equals(product.getSellerId())) {
                throw new BaseException("不能举报自己的商品");
            }
            return;
        }
        if (targetType == ReportTargetTypeEnum.USER) {
            User user = userMapper.selectById(targetId);
            if (user == null) {
                throw new BaseException("举报对象不存在");
            }
            if (currentUserId.equals(user.getId())) {
                throw new BaseException("不能举报自己");
            }
            return;
        }
        if (targetType == ReportTargetTypeEnum.REVIEW) {
            TradeReview review = tradeReviewMapper.selectById(targetId);
            if (review == null) {
                throw new BaseException("举报对象不存在");
            }
            if (currentUserId.equals(review.getReviewerId())) {
                throw new BaseException("不能举报自己发表的评价");
            }
            return;
        }
        throw new BaseException("举报对象类型非法");
    }

    private void applyKeywordFilter(LambdaQueryWrapper<ContentReport> queryWrapper, AdminReportQueryDTO dto) {
        String keyword = trimToNull(dto.getKeyword());
        if (!StringUtils.hasText(keyword)) {
            return;
        }

        Set<Long> reporterIds = findReporterIdsByKeyword(keyword);
        queryWrapper.and(wrapper -> {
            wrapper.like(ContentReport::getReason, keyword)
                    .or()
                    .like(ContentReport::getDescription, keyword);
            if (!reporterIds.isEmpty()) {
                wrapper.or().in(ContentReport::getReporterId, reporterIds);
            }
        });
    }

    private Set<Long> findReporterIdsByKeyword(String keyword) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getRole, UserRoleEnum.USER)
                .and(wrapper -> wrapper.like(User::getUsername, keyword)
                        .or()
                        .like(User::getNickname, keyword));
        return userMapper.selectList(queryWrapper).stream()
                .map(User::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private ContentReport getReportByReporterAndTarget(Long reporterId, ReportTargetTypeEnum targetType, Long targetId) {
        LambdaQueryWrapper<ContentReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentReport::getReporterId, reporterId)
                .eq(ContentReport::getTargetType, targetType)
                .eq(ContentReport::getTargetId, targetId);
        return contentReportMapper.selectOne(queryWrapper);
    }

    private ContentReport getReportOrThrow(Long reportId) {
        ContentReport report = contentReportMapper.selectById(reportId);
        if (report == null) {
            throw new BaseException("举报不存在");
        }
        return report;
    }

    private List<AdminReportListVO> buildAdminReportList(List<ContentReport> reports) {
        if (reports == null || reports.isEmpty()) {
            return List.of();
        }

        List<Long> reporterIds = extractIds(reports, ContentReport::getReporterId);
        List<Long> targetUserIds = reports.stream()
                .filter(report -> report.getTargetType() == ReportTargetTypeEnum.USER)
                .map(ContentReport::getTargetId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, User> usersById = mapUsersById(mergeIds(reporterIds, targetUserIds));
        BatchTargetData batchTargetData = loadBatchTargetData(reports, usersById);

        return reports.stream().map(report -> {
            AdminReportListVO vo = new AdminReportListVO();
            BeanUtils.copyProperties(report, vo);
            User reporter = usersById.get(report.getReporterId());
            if (reporter != null) {
                vo.setReporterUsername(reporter.getUsername());
                vo.setReporterNickname(reporter.getNickname());
            }
            fillTargetSummary(vo, report, batchTargetData);
            return vo;
        }).toList();
    }

    private void fillTargetInfo(AdminReportDetailVO detailVO, ContentReport report) {
        if (report.getTargetType() == ReportTargetTypeEnum.PRODUCT) {
            Product product = productMapper.selectById(report.getTargetId());
            if (product != null) {
                detailVO.setTargetSummary(product.getTitle());
                detailVO.setTargetOwnerId(product.getSellerId());
                User owner = userMapper.selectById(product.getSellerId());
                if (owner != null) {
                    detailVO.setTargetOwnerNickname(owner.getNickname());
                }
            }
            return;
        }
        if (report.getTargetType() == ReportTargetTypeEnum.USER) {
            User user = userMapper.selectById(report.getTargetId());
            if (user != null) {
                detailVO.setTargetSummary(user.getNickname());
                detailVO.setTargetOwnerId(user.getId());
                detailVO.setTargetOwnerNickname(user.getNickname());
            }
            return;
        }
        if (report.getTargetType() == ReportTargetTypeEnum.REVIEW) {
            TradeReview review = tradeReviewMapper.selectById(report.getTargetId());
            if (review != null) {
                detailVO.setTargetSummary(trimSummary(review.getContent()));
                detailVO.setTargetOwnerId(review.getReviewerId());
                User owner = userMapper.selectById(review.getReviewerId());
                if (owner != null) {
                    detailVO.setTargetOwnerNickname(owner.getNickname());
                }
            }
        }
    }

    private void fillTargetSummary(AdminReportListVO vo, ContentReport report, BatchTargetData batchTargetData) {
        if (report.getTargetType() == ReportTargetTypeEnum.PRODUCT) {
            Product product = batchTargetData.productsById.get(report.getTargetId());
            if (product != null) {
                vo.setTargetSummary(product.getTitle());
            }
            return;
        }
        if (report.getTargetType() == ReportTargetTypeEnum.USER) {
            User user = batchTargetData.usersById.get(report.getTargetId());
            if (user != null) {
                vo.setTargetSummary(user.getNickname());
            }
            return;
        }
        if (report.getTargetType() == ReportTargetTypeEnum.REVIEW) {
            TradeReview review = batchTargetData.reviewsById.get(report.getTargetId());
            if (review != null) {
                vo.setTargetSummary(trimSummary(review.getContent()));
            }
        }
    }

    private BatchTargetData loadBatchTargetData(List<ContentReport> reports, Map<Long, User> usersById) {
        Map<ReportTargetTypeEnum, List<Long>> targetIdsByType = reports.stream()
                .filter(Objects::nonNull)
                .filter(report -> report.getTargetType() != null && report.getTargetId() != null)
                .collect(Collectors.groupingBy(ContentReport::getTargetType,
                        () -> new EnumMap<>(ReportTargetTypeEnum.class),
                        Collectors.mapping(ContentReport::getTargetId,
                                Collectors.collectingAndThen(Collectors.toList(), this::distinctIds))));

        Map<Long, Product> productsById = mapProductsById(targetIdsByType.get(ReportTargetTypeEnum.PRODUCT));
        Map<Long, TradeReview> reviewsById = mapReviewsById(targetIdsByType.get(ReportTargetTypeEnum.REVIEW));
        return new BatchTargetData(productsById, usersById, reviewsById);
    }

    private Map<Long, User> mapUsersById(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(User::getId, Function.identity(), (l, r) -> l));
    }

    private Map<Long, Product> mapProductsById(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return productMapper.selectBatchIds(productIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Product::getId, Function.identity(), (l, r) -> l));
    }

    private Map<Long, TradeReview> mapReviewsById(Collection<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Map.of();
        }
        return tradeReviewMapper.selectBatchIds(reviewIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(TradeReview::getId, Function.identity(), (l, r) -> l));
    }

    private <T> List<Long> extractIds(Collection<T> source, Function<T, Long> extractor) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        return source.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<Long> distinctIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<Long> mergeIds(Collection<Long> first, Collection<Long> second) {
        return java.util.stream.Stream.concat(
                        first == null ? java.util.stream.Stream.empty() : first.stream(),
                        second == null ? java.util.stream.Stream.empty() : second.stream())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private String trimRequired(String value, int maxLength, String emptyMessage) {
        String trimmed = trimOptional(value, maxLength);
        if (!StringUtils.hasText(trimmed)) {
            throw new BaseException(emptyMessage);
        }
        return trimmed;
    }

    private String trimOptional(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            return trimmed.substring(0, maxLength);
        }
        return trimmed;
    }

    private String trimSummary(String value) {
        return trimOptional(value, 30);
    }

    private String trimToNull(String value) {
        return trimOptional(value, 255);
    }

    private int normalizePageNum(AdminReportQueryDTO dto) {
        if (dto == null || dto.getPageNum() == null || dto.getPageNum() < 1) {
            return DEFAULT_PAGE_NUM;
        }
        return dto.getPageNum();
    }

    private int normalizePageSize(AdminReportQueryDTO dto) {
        if (dto == null || dto.getPageSize() == null || dto.getPageSize() < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(dto.getPageSize(), MAX_PAGE_SIZE);
    }

    private static class BatchTargetData {

        private final Map<Long, Product> productsById;
        private final Map<Long, User> usersById;
        private final Map<Long, TradeReview> reviewsById;

        private BatchTargetData(Map<Long, Product> productsById,
                                Map<Long, User> usersById,
                                Map<Long, TradeReview> reviewsById) {
            this.productsById = productsById;
            this.usersById = usersById;
            this.reviewsById = reviewsById;
        }
    }
}
