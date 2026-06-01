package com.cshm.campussecondhandmark.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.product.enums.ProductAuditStatusEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductSaleStatusEnum;
import com.cshm.campussecondhandmark.module.product.mapper.ProductBrowseHistoryMapper;
import com.cshm.campussecondhandmark.module.product.mapper.ProductCategoryMapper;
import com.cshm.campussecondhandmark.module.product.mapper.ProductFavoriteMapper;
import com.cshm.campussecondhandmark.module.product.mapper.ProductMapper;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductQueryDTO;
import com.cshm.campussecondhandmark.module.product.pojo.entity.Product;
import com.cshm.campussecondhandmark.module.product.pojo.entity.ProductBrowseHistory;
import com.cshm.campussecondhandmark.module.product.pojo.entity.ProductCategory;
import com.cshm.campussecondhandmark.module.product.pojo.entity.ProductFavorite;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductSummaryVO;
import com.cshm.campussecondhandmark.module.product.service.ProductInteractionService;
import com.cshm.campussecondhandmark.module.user.mapper.UserMapper;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductInteractionServiceImpl implements ProductInteractionService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    @Autowired
    private ProductFavoriteMapper productFavoriteMapper;

    @Autowired
    private ProductBrowseHistoryMapper productBrowseHistoryMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductCategoryMapper productCategoryMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void favoriteProduct(Long currentUserId, Long productId) {
        Product product = getProductOrThrow(productId);
        assertPublicVisible(product);
        if (currentUserId.equals(product.getSellerId())) {
            throw new BaseException("不能收藏自己的商品");
        }
        if (isFavorited(currentUserId, productId)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        ProductFavorite favorite = new ProductFavorite();
        favorite.setUserId(currentUserId);
        favorite.setProductId(productId);
        favorite.setCreateTime(now);
        favorite.setUpdateTime(now);
        if (productFavoriteMapper.insert(favorite) <= 0) {
            throw new BaseException("收藏失败");
        }
    }

    @Override
    @Transactional
    public void unfavoriteProduct(Long currentUserId, Long productId) {
        LambdaQueryWrapper<ProductFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductFavorite::getUserId, currentUserId)
                .eq(ProductFavorite::getProductId, productId);
        productFavoriteMapper.delete(queryWrapper);
    }

    @Override
    public PageResult<ProductSummaryVO> pageMyFavorites(Long currentUserId, ProductQueryDTO dto) {
        Page<ProductFavorite> page = new Page<>(normalizePageNum(dto), normalizePageSize(dto));
        LambdaQueryWrapper<ProductFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductFavorite::getUserId, currentUserId)
                .orderByDesc(ProductFavorite::getCreateTime)
                .orderByDesc(ProductFavorite::getId);

        Page<ProductFavorite> resultPage = productFavoriteMapper.selectPage(page, queryWrapper);
        List<Long> productIds = resultPage.getRecords().stream()
                .map(ProductFavorite::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<ProductSummaryVO> records = buildProductSummaries(productIds);
        return new PageResult<>(resultPage.getTotal(), records);
    }

    @Override
    @Transactional
    public void recordBrowseHistory(Long currentUserId, Long productId) {
        if (currentUserId == null || productId == null) {
            return;
        }
        Product product = productMapper.selectById(productId);
        if (product == null || !isPublicVisible(product) || currentUserId.equals(product.getSellerId())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        ProductBrowseHistory history = getBrowseHistory(currentUserId, productId);
        if (history == null) {
            history = new ProductBrowseHistory();
            history.setUserId(currentUserId);
            history.setProductId(productId);
            history.setBrowseTime(now);
            history.setCreateTime(now);
            history.setUpdateTime(now);
            if (productBrowseHistoryMapper.insert(history) <= 0) {
                throw new BaseException("记录浏览历史失败");
            }
            return;
        }

        history.setBrowseTime(now);
        history.setUpdateTime(now);
        if (productBrowseHistoryMapper.updateById(history) <= 0) {
            throw new BaseException("记录浏览历史失败");
        }
    }

    @Override
    public PageResult<ProductSummaryVO> pageMyBrowseHistory(Long currentUserId, ProductQueryDTO dto) {
        Page<ProductBrowseHistory> page = new Page<>(normalizePageNum(dto), normalizePageSize(dto));
        LambdaQueryWrapper<ProductBrowseHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductBrowseHistory::getUserId, currentUserId)
                .orderByDesc(ProductBrowseHistory::getBrowseTime)
                .orderByDesc(ProductBrowseHistory::getId);

        Page<ProductBrowseHistory> resultPage = productBrowseHistoryMapper.selectPage(page, queryWrapper);
        List<Long> productIds = resultPage.getRecords().stream()
                .map(ProductBrowseHistory::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<ProductSummaryVO> records = buildProductSummaries(productIds);
        return new PageResult<>(resultPage.getTotal(), records);
    }

    @Override
    @Transactional
    public void clearMyBrowseHistory(Long currentUserId) {
        LambdaQueryWrapper<ProductBrowseHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductBrowseHistory::getUserId, currentUserId);
        productBrowseHistoryMapper.delete(queryWrapper);
    }

    @Override
    public boolean isFavorited(Long currentUserId, Long productId) {
        if (currentUserId == null || productId == null) {
            return false;
        }
        LambdaQueryWrapper<ProductFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductFavorite::getUserId, currentUserId)
                .eq(ProductFavorite::getProductId, productId);
        return productFavoriteMapper.selectCount(queryWrapper) > 0;
    }

    private Product getProductOrThrow(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BaseException("商品不存在");
        }
        return product;
    }

    private void assertPublicVisible(Product product) {
        if (!isPublicVisible(product)) {
            throw new BaseException("商品未公开在售");
        }
    }

    private boolean isPublicVisible(Product product) {
        return product.getAuditStatus() == ProductAuditStatusEnum.APPROVED
                && product.getSaleStatus() == ProductSaleStatusEnum.ON_SHELF;
    }

    private ProductBrowseHistory getBrowseHistory(Long currentUserId, Long productId) {
        LambdaQueryWrapper<ProductBrowseHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductBrowseHistory::getUserId, currentUserId)
                .eq(ProductBrowseHistory::getProductId, productId);
        return productBrowseHistoryMapper.selectOne(queryWrapper);
    }

    private List<ProductSummaryVO> buildProductSummaries(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Product> productsById = productMapper.selectBatchIds(productIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Product::getId, Function.identity(), (l, r) -> l));
        Map<Long, ProductCategory> categoriesById = mapCategoriesById(extractIds(productsById.values(), Product::getCategoryId));
        Map<Long, User> usersById = mapUsersById(extractIds(productsById.values(), Product::getSellerId));

        return productIds.stream()
                .map(productsById::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(product -> productIds.indexOf(product.getId())))
                .map(product -> buildProductSummaryVO(product, categoriesById, usersById))
                .toList();
    }

    private ProductSummaryVO buildProductSummaryVO(Product product,
                                                   Map<Long, ProductCategory> categoriesById,
                                                   Map<Long, User> usersById) {
        ProductSummaryVO vo = new ProductSummaryVO();
        BeanUtils.copyProperties(product, vo);
        vo.setConditionLevel(product.getConditionLevel() == null ? null : product.getConditionLevel().getCode());

        ProductCategory category = categoriesById.get(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        User seller = usersById.get(product.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
            vo.setSellerCampusVerifyStatus(seller.getCampusVerifyStatus());
        }
        return vo;
    }

    private Map<Long, ProductCategory> mapCategoriesById(Collection<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Map.of();
        }
        return productCategoryMapper.selectBatchIds(categoryIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ProductCategory::getId, Function.identity(), (l, r) -> l));
    }

    private Map<Long, User> mapUsersById(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(User::getId, Function.identity(), (l, r) -> l));
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

    private int normalizePageNum(ProductQueryDTO dto) {
        if (dto == null || dto.getPageNum() == null || dto.getPageNum() < 1) {
            return DEFAULT_PAGE_NUM;
        }
        return dto.getPageNum();
    }

    private int normalizePageSize(ProductQueryDTO dto) {
        if (dto == null || dto.getPageSize() == null || dto.getPageSize() < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(dto.getPageSize(), MAX_PAGE_SIZE);
    }
}
