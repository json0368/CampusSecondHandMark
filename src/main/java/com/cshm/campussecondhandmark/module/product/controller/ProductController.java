package com.cshm.campussecondhandmark.module.product.controller;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.common.result.Result;
import com.cshm.campussecondhandmark.common.service.ImageService;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductCreateDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductQueryDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductSearchDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductUpdateDTO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.MyProductVO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductConversationOpenVO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductDetailVO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductSummaryVO;
import com.cshm.campussecondhandmark.module.product.service.ProductConversationService;
import com.cshm.campussecondhandmark.module.product.service.ProductInteractionService;
import com.cshm.campussecondhandmark.module.product.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
@Slf4j
@Api(tags = "商品接口")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductInteractionService productInteractionService;

    @Autowired
    private ProductConversationService productConversationService;

    @Autowired
    private ImageService imageService;

    @PostMapping("")
    @ApiOperation(value = "发布商品", notes = "发布后进入待审核状态")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Long> createProduct(@ApiParam(value = "商品发布请求", required = true) @RequestBody ProductCreateDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("发布商品，用户ID={}，商品标题={}", currentUserId, dto == null ? null : dto.getTitle());
        return Result.success(productService.createProduct(currentUserId, dto));
    }

    @PutMapping("/{productId}")
    @ApiOperation(value = "修改商品", notes = "仅卖家可修改，修改后重新进入待审核状态")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Void> updateProduct(
            @ApiParam(value = "商品 ID", required = true, example = "10") @PathVariable Long productId,
            @ApiParam(value = "商品修改请求", required = true) @RequestBody ProductUpdateDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("修改商品，用户ID={}，商品ID={}", currentUserId, productId);
        productService.updateProduct(currentUserId, productId, dto);
        return Result.success();
    }

    @GetMapping("")
    @ApiOperation(value = "分页查询商品", notes = "公开接口。可选携带 token，用于返回与当前用户相关的操作标记")
    @ApiImplicitParam(name = "token", value = "用户登录令牌，可选", required = false, paramType = "header", dataTypeClass = String.class)
    public Result<PageResult<ProductSummaryVO>> pageProducts(ProductSearchDTO dto) {
        log.info("分页查询商品列表");
        return Result.success(productService.pageProducts(dto));
    }

    @GetMapping("/{productId}")
    @ApiOperation(value = "获取商品详情", notes = "公开接口。卖家本人也可查看非公开商品详情")
    @ApiImplicitParam(name = "token", value = "用户登录令牌，可选", required = false, paramType = "header", dataTypeClass = String.class)
    public Result<ProductDetailVO> getProductDetail(
            @ApiParam(value = "商品 ID", required = true, example = "10") @PathVariable Long productId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("获取商品详情，用户ID={}，商品ID={}", currentUserId, productId);
        return Result.success(productService.getProductDetail(productId, currentUserId));
    }

    @GetMapping("/me")
    @ApiOperation("分页查询当前用户商品")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<PageResult<MyProductVO>> pageMyProducts(ProductQueryDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("分页查询当前用户商品，用户ID={}", currentUserId);
        return Result.success(productService.pageMyProducts(currentUserId, dto));
    }

    @PostMapping("/{productId}/off-shelf")
    @ApiOperation(value = "主动下架商品", notes = "仅卖家本人可主动下架商品")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Void> offShelfProduct(
            @ApiParam(value = "商品 ID", required = true, example = "10") @PathVariable Long productId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("主动下架商品，用户ID={}，商品ID={}", currentUserId, productId);
        productService.offShelfProduct(currentUserId, productId);
        return Result.success();
    }

    @PostMapping("/{productId}/favorite")
    @ApiOperation("收藏商品")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Void> favoriteProduct(
            @ApiParam(value = "商品 ID", required = true, example = "10") @PathVariable Long productId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("收藏商品，用户ID={}，商品ID={}", currentUserId, productId);
        productInteractionService.favoriteProduct(currentUserId, productId);
        return Result.success();
    }

    @DeleteMapping("/{productId}/favorite")
    @ApiOperation("取消收藏商品")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Void> unfavoriteProduct(
            @ApiParam(value = "商品 ID", required = true, example = "10") @PathVariable Long productId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("取消收藏商品，用户ID={}，商品ID={}", currentUserId, productId);
        productInteractionService.unfavoriteProduct(currentUserId, productId);
        return Result.success();
    }

    @GetMapping("/favorites")
    @ApiOperation("分页查询我的收藏商品")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<PageResult<ProductSummaryVO>> pageMyFavorites(ProductQueryDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("分页查询我的收藏商品，用户ID={}", currentUserId);
        return Result.success(productInteractionService.pageMyFavorites(currentUserId, dto));
    }

    @GetMapping("/browse-history")
    @ApiOperation("分页查询我的浏览历史")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<PageResult<ProductSummaryVO>> pageMyBrowseHistory(ProductQueryDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("分页查询我的浏览历史，用户ID={}", currentUserId);
        return Result.success(productInteractionService.pageMyBrowseHistory(currentUserId, dto));
    }

    @DeleteMapping("/browse-history")
    @ApiOperation("清空我的浏览历史")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Void> clearMyBrowseHistory() {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("清空我的浏览历史，用户ID={}", currentUserId);
        productInteractionService.clearMyBrowseHistory(currentUserId);
        return Result.success();
    }

    @PostMapping("/{productId}/conversation")
    @ApiOperation("开通商品会话，用于联系卖家")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<ProductConversationOpenVO> openConversation(
            @ApiParam(value = "商品 ID", required = true, example = "10") @PathVariable Long productId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("开通商品会话，用户ID={}，商品ID={}", currentUserId, productId);
        return Result.success(productConversationService.openConversation(currentUserId, productId));
    }

    @PostMapping("/images/upload")
    @ApiOperation("上传商品图片，返回图片URL")
    public Result uploadFile(@RequestParam MultipartFile file) {
        return Result.success(imageService.uploadFile(file));
    }
}
