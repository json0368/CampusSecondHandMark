package com.cshm.campussecondhandmark.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.product.enums.CategoryStatusEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductAuditStatusEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductConditionEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductSaleStatusEnum;
import com.cshm.campussecondhandmark.module.product.mapper.ProductCategoryMapper;
import com.cshm.campussecondhandmark.module.product.mapper.ProductImageMapper;
import com.cshm.campussecondhandmark.module.product.mapper.ProductMapper;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductAuditDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductAuditQueryDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductCreateDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductQueryDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductSearchDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductUpdateDTO;
import com.cshm.campussecondhandmark.module.product.pojo.entity.Product;
import com.cshm.campussecondhandmark.module.product.pojo.entity.ProductCategory;
import com.cshm.campussecondhandmark.module.product.pojo.entity.ProductImage;
import com.cshm.campussecondhandmark.module.product.pojo.vo.MyProductVO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductAuditVO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductDetailVO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductImageVO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductSummaryVO;
import com.cshm.campussecondhandmark.module.product.service.ProductService;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import com.cshm.campussecondhandmark.module.user.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    @Autowired
    private ProductImageMapper productImageMapper;

    @Autowired
    private ProductCategoryMapper productCategoryMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Long createProduct(Long currentUserId, ProductCreateDTO dto) {
        if (dto == null) {
            throw new BaseException("商品信息不能为空");
        }

        ProductCategory category = getEnabledCategoryOrThrow(dto.getCategoryId());
        String title = validateTitle(dto.getTitle());
        BigDecimal price = validatePrice(dto.getPrice());
        ProductConditionEnum condition = validateCondition(dto.getConditionLevel());
        List<String> imageUrls = normalizeRequiredImageUrls(dto.getImageUrls());

        LocalDateTime now = LocalDateTime.now();
        Product product = new Product();
        product.setSellerId(currentUserId);
        product.setCategoryId(category.getId());
        product.setTitle(title);
        product.setDescription(trimToNull(dto.getDescription()));
        product.setPrice(price);
        product.setConditionLevel(condition);
        product.setCoverImageUrl(imageUrls.get(0));
        product.setAuditStatus(ProductAuditStatusEnum.PENDING);
        product.setSaleStatus(ProductSaleStatusEnum.DRAFT);
        product.setCreateTime(now);
        product.setUpdateTime(now);

        if (!save(product)) {
            throw new BaseException("商品发布失败");
        }
        saveImages(product.getId(), imageUrls, now);
        return product.getId();
    }

    @Override
    @Transactional
    public void updateProduct(Long currentUserId, Long productId, ProductUpdateDTO dto) {
        if (dto == null) {
            throw new BaseException("商品信息不能为空");
        }

        Product product = getProductOrThrow(productId);
        validateSeller(currentUserId, product);
        validateCanEdit(product);

        if (dto.getCategoryId() != null) {
            product.setCategoryId(getEnabledCategoryOrThrow(dto.getCategoryId()).getId());
        }
        if (dto.getTitle() != null) {
            product.setTitle(validateTitle(dto.getTitle()));
        }
        if (dto.getDescription() != null) {
            product.setDescription(trimToNull(dto.getDescription()));
        }
        if (dto.getPrice() != null) {
            product.setPrice(validatePrice(dto.getPrice()));
        }
        if (dto.getConditionLevel() != null) {
            product.setConditionLevel(validateCondition(dto.getConditionLevel()));
        }

        List<String> imageUrls = null;
        if (dto.getImageUrls() != null) {
            imageUrls = normalizeRequiredImageUrls(dto.getImageUrls());
            product.setCoverImageUrl(imageUrls.get(0));
        }

        LocalDateTime now = LocalDateTime.now();
        product.setAuditStatus(ProductAuditStatusEnum.PENDING);
        product.setSaleStatus(ProductSaleStatusEnum.DRAFT);
        product.setRejectReason(null);
        product.setPublishTime(null);
        product.setAuditTime(null);
        product.setOffShelfTime(null);
        product.setUpdateTime(now);

        if (!updateById(product)) {
            throw new BaseException("商品更新失败");
        }
        if (imageUrls != null) {
            replaceImages(productId, imageUrls, now);
        }
    }

    @Override
    public PageResult<ProductSummaryVO> pageProducts(ProductSearchDTO dto) {
        Page<Product> page = new Page<>(normalizePageNum(dto.getPageNum()), normalizePageSize(dto.getPageSize()));
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getAuditStatus, ProductAuditStatusEnum.APPROVED)
                .eq(Product::getSaleStatus, ProductSaleStatusEnum.ON_SHELF);

        String keyword = trimToNull(dto.getKeyword());
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Product::getTitle, keyword);
        }
        if (dto.getCategoryId() != null) {
            queryWrapper.eq(Product::getCategoryId, dto.getCategoryId());
        }
        if (dto.getMinPrice() != null) {
            queryWrapper.ge(Product::getPrice, dto.getMinPrice());
        }
        if (dto.getMaxPrice() != null) {
            queryWrapper.le(Product::getPrice, dto.getMaxPrice());
        }
        if (dto.getConditionLevel() != null) {
            queryWrapper.eq(Product::getConditionLevel, validateCondition(dto.getConditionLevel()));
        }
        queryWrapper.orderByDesc(Product::getPublishTime).orderByDesc(Product::getId);

        Page<Product> resultPage = page(page, queryWrapper);
        List<ProductSummaryVO> records = resultPage.getRecords().stream()
                .map(this::buildProductSummaryVO)
                .collect(Collectors.toList());
        return new PageResult<>(resultPage.getTotal(), records);
    }

    @Override
    public ProductDetailVO getProductDetail(Long productId, Long currentUserId) {
        Product product = getProductOrThrow(productId);
        boolean seller = currentUserId != null && currentUserId.equals(product.getSellerId());
        boolean visible = isPublicVisible(product);
        if (!visible && !seller) {
            throw new BaseException("商品不存在或未上架");
        }
        return buildProductDetailVO(product, currentUserId, visible);
    }

    @Override
    public PageResult<MyProductVO> pageMyProducts(Long currentUserId, ProductQueryDTO dto) {
        Page<Product> page = new Page<>(normalizePageNum(dto.getPageNum()), normalizePageSize(dto.getPageSize()));
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getSellerId, currentUserId);
        if (dto.getAuditStatus() != null) {
            queryWrapper.eq(Product::getAuditStatus, dto.getAuditStatus());
        }
        if (dto.getSaleStatus() != null) {
            queryWrapper.eq(Product::getSaleStatus, dto.getSaleStatus());
        }
        queryWrapper.orderByDesc(Product::getUpdateTime).orderByDesc(Product::getId);

        Page<Product> resultPage = page(page, queryWrapper);
        List<MyProductVO> records = resultPage.getRecords().stream()
                .map(this::buildMyProductVO)
                .collect(Collectors.toList());
        return new PageResult<>(resultPage.getTotal(), records);
    }

    @Override
    public PageResult<ProductAuditVO> pageAuditProducts(ProductAuditQueryDTO dto) {
        Page<Product> page = new Page<>(normalizePageNum(dto.getPageNum()), normalizePageSize(dto.getPageSize()));
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getAuditStatus,
                dto.getAuditStatus() == null ? ProductAuditStatusEnum.PENDING : dto.getAuditStatus());

        String keyword = trimToNull(dto.getKeyword());
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Product::getTitle, keyword);
        }
        if (dto.getCategoryId() != null) {
            queryWrapper.eq(Product::getCategoryId, dto.getCategoryId());
        }
        queryWrapper.orderByDesc(Product::getCreateTime).orderByDesc(Product::getId);

        Page<Product> resultPage = page(page, queryWrapper);
        List<ProductAuditVO> records = resultPage.getRecords().stream()
                .map(this::buildProductAuditVO)
                .collect(Collectors.toList());
        return new PageResult<>(resultPage.getTotal(), records);
    }

    @Override
    @Transactional
    public void auditProduct(Long productId, ProductAuditDTO dto) {
        if (dto == null || dto.getAuditStatus() == null) {
            throw new BaseException("审核结果不能为空");
        }

        Product product = getProductOrThrow(productId);
        LocalDateTime now = LocalDateTime.now();
        if (dto.getAuditStatus() == ProductAuditStatusEnum.APPROVED) {
            product.setAuditStatus(ProductAuditStatusEnum.APPROVED);
            product.setSaleStatus(ProductSaleStatusEnum.ON_SHELF);
            product.setRejectReason(null);
            product.setAuditTime(now);
            product.setPublishTime(now);
            product.setOffShelfTime(null);
        } else if (dto.getAuditStatus() == ProductAuditStatusEnum.REJECTED) {
            String rejectReason = trimToNull(dto.getRejectReason());
            if (!StringUtils.hasText(rejectReason)) {
                throw new BaseException("驳回原因不能为空");
            }
            product.setAuditStatus(ProductAuditStatusEnum.REJECTED);
            product.setSaleStatus(ProductSaleStatusEnum.DRAFT);
            product.setRejectReason(rejectReason);
            product.setAuditTime(now);
            product.setPublishTime(null);
        } else {
            throw new BaseException("审核结果不正确");
        }
        product.setUpdateTime(now);
        if (!updateById(product)) {
            throw new BaseException("商品审核失败");
        }
    }

    @Override
    @Transactional
    public void offShelfProduct(Long currentUserId, Long productId) {
        Product product = getProductOrThrow(productId);
        validateSeller(currentUserId, product);
        if (product.getSaleStatus() == ProductSaleStatusEnum.REMOVED_BY_ADMIN) {
            throw new BaseException("管理员下架的商品不能操作");
        }
        if (product.getSaleStatus() == ProductSaleStatusEnum.IN_TRANSACTION) {
            throw new BaseException("交易中的商品不能下架");
        }
        if (product.getSaleStatus() == ProductSaleStatusEnum.SOLD) {
            throw new BaseException("已售出的商品不能下架");
        }

        LocalDateTime now = LocalDateTime.now();
        product.setSaleStatus(ProductSaleStatusEnum.OFF_SHELF);
        product.setOffShelfTime(now);
        product.setUpdateTime(now);
        if (!updateById(product)) {
            throw new BaseException("商品下架失败");
        }
    }

    @Override
    @Transactional
    public void removeProductByAdmin(Long productId, String reason) {
        String removeReason = trimToNull(reason);
        if (!StringUtils.hasText(removeReason)) {
            throw new BaseException("下架原因不能为空");
        }

        Product product = getProductOrThrow(productId);
        LocalDateTime now = LocalDateTime.now();
        product.setSaleStatus(ProductSaleStatusEnum.REMOVED_BY_ADMIN);
        product.setOffShelfTime(now);
        product.setUpdateTime(now);
        if (!updateById(product)) {
            throw new BaseException("商品强制下架失败");
        }
    }

    private Product getProductOrThrow(Long productId) {
        if (productId == null) {
            throw new BaseException("商品不存在");
        }
        Product product = getById(productId);
        if (product == null) {
            throw new BaseException("商品不存在");
        }
        return product;
    }

    private ProductCategory getEnabledCategoryOrThrow(Long categoryId) {
        if (categoryId == null) {
            throw new BaseException("商品分类不能为空");
        }
        ProductCategory category = productCategoryMapper.selectById(categoryId);
        if (category == null || category.getStatus() != CategoryStatusEnum.ENABLED) {
            throw new BaseException("商品分类不存在或已停用");
        }
        return category;
    }

    private void validateSeller(Long currentUserId, Product product) {
        if (!currentUserId.equals(product.getSellerId())) {
            throw new BaseException("只能操作自己的商品");
        }
    }

    private void validateCanEdit(Product product) {
        if (product.getSaleStatus() == ProductSaleStatusEnum.REMOVED_BY_ADMIN) {
            throw new BaseException("管理员下架的商品不能修改");
        }
        if (product.getSaleStatus() == ProductSaleStatusEnum.IN_TRANSACTION) {
            throw new BaseException("交易中的商品不能修改");
        }
        if (product.getSaleStatus() == ProductSaleStatusEnum.SOLD) {
            throw new BaseException("已售出的商品不能修改");
        }
    }

    private String validateTitle(String title) {
        String value = trimToNull(title);
        if (!StringUtils.hasText(value)) {
            throw new BaseException("商品标题不能为空");
        }
        if (value.length() > 128) {
            throw new BaseException("商品标题不能超过128个字符");
        }
        return value;
    }

    private BigDecimal validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException("商品价格必须大于0");
        }
        return price;
    }

    private ProductConditionEnum validateCondition(Integer conditionLevel) {
        ProductConditionEnum condition = ProductConditionEnum.fromCode(conditionLevel);
        if (condition == null) {
            throw new BaseException("商品成色不能为空");
        }
        return condition;
    }

    private List<String> normalizeRequiredImageUrls(List<String> imageUrls) {
        List<String> result = new ArrayList<>();
        if (imageUrls != null) {
            for (String imageUrl : imageUrls) {
                String value = trimToNull(imageUrl);
                if (StringUtils.hasText(value)) {
                    result.add(value);
                }
            }
        }
        if (result.isEmpty()) {
            throw new BaseException("商品图片不能为空");
        }
        return result;
    }

    private void saveImages(Long productId, List<String> imageUrls, LocalDateTime now) {
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setImageUrl(imageUrls.get(i));
            image.setSortOrder(i + 1);
            image.setCreateTime(now);
            image.setUpdateTime(now);
            productImageMapper.insert(image);
        }
    }

    private void replaceImages(Long productId, List<String> imageUrls, LocalDateTime now) {
        LambdaQueryWrapper<ProductImage> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ProductImage::getProductId, productId);
        productImageMapper.delete(deleteWrapper);
        saveImages(productId, imageUrls, now);
    }

    private List<ProductImageVO> listProductImages(Long productId) {
        LambdaQueryWrapper<ProductImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductImage::getProductId, productId)
                .orderByAsc(ProductImage::getSortOrder)
                .orderByAsc(ProductImage::getId);
        return productImageMapper.selectList(queryWrapper).stream()
                .map(this::buildProductImageVO)
                .collect(Collectors.toList());
    }

    private ProductSummaryVO buildProductSummaryVO(Product product) {
        ProductSummaryVO vo = new ProductSummaryVO();
        BeanUtils.copyProperties(product, vo);
        fillCategory(vo, product.getCategoryId());
        fillSeller(vo, product.getSellerId());
        return vo;
    }

    private ProductDetailVO buildProductDetailVO(Product product, Long currentUserId, boolean visible) {
        ProductDetailVO vo = new ProductDetailVO();
        BeanUtils.copyProperties(product, vo);

        ProductCategory category = productCategoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        User seller = userMapper.selectById(product.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
            vo.setSellerAvatarUrl(seller.getAvatarUrl());
            vo.setSellerCampusVerifyStatus(seller.getCampusVerifyStatus());
        }

        boolean self = currentUserId != null && currentUserId.equals(product.getSellerId());
        vo.setCanOrder(visible && currentUserId != null && !self);
        vo.setCanChat(visible && currentUserId != null && !self);
        return vo;
    }

    private MyProductVO buildMyProductVO(Product product) {
        MyProductVO vo = new MyProductVO();
        BeanUtils.copyProperties(product, vo);
        return vo;
    }

    private ProductAuditVO buildProductAuditVO(Product product) {
        ProductAuditVO vo = new ProductAuditVO();
        BeanUtils.copyProperties(product, vo);

        ProductCategory category = productCategoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        User seller = userMapper.selectById(product.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
        }
        return vo;
    }

    private ProductImageVO buildProductImageVO(ProductImage image) {
        ProductImageVO vo = new ProductImageVO();
        vo.setImageUrl(image.getImageUrl());
        vo.setSortOrder(image.getSortOrder());
        return vo;
    }

    private void fillCategory(ProductSummaryVO vo, Long categoryId) {
        ProductCategory category = productCategoryMapper.selectById(categoryId);
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
    }

    private void fillSeller(ProductSummaryVO vo, Long sellerId) {
        User seller = userMapper.selectById(sellerId);
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
            vo.setSellerCampusVerifyStatus(seller.getCampusVerifyStatus());
        }
    }

    private boolean isPublicVisible(Product product) {
        return product.getAuditStatus() == ProductAuditStatusEnum.APPROVED
                && product.getSaleStatus() == ProductSaleStatusEnum.ON_SHELF;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
