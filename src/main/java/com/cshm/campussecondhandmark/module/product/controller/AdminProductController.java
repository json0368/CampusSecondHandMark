package com.cshm.campussecondhandmark.module.product.controller;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.common.result.Result;
import com.cshm.campussecondhandmark.module.product.pojo.dto.AdminProductRemoveDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductAuditDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductAuditQueryDTO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductAuditVO;
import com.cshm.campussecondhandmark.module.product.service.ProductService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Api(tags = "后台商品管理")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/admin-api/products/audit")
    @ApiOperation(value = "分页查询商品审核列表", notes = "默认查询待审核商品，也可按审核状态筛选")
    @ApiImplicitParam(name = "token", value = "管理员登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<PageResult<ProductAuditVO>> pageAuditProducts(ProductAuditQueryDTO dto) {
        log.info("后台分页查询商品审核列表");
        return Result.success(productService.pageAuditProducts(dto));
    }

    @PostMapping("/admin-api/products/{productId}/audit")
    @ApiOperation(value = "审核商品", notes = "支持通过或驳回商品")
    @ApiImplicitParam(name = "token", value = "管理员登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Void> auditProduct(
            @ApiParam(value = "商品 ID", required = true, example = "10") @PathVariable Long productId,
            @ApiParam(value = "商品审核请求", required = true) @RequestBody ProductAuditDTO dto) {
        Long adminId = BaseContext.getCurrentId();
        log.info("后台审核商品，管理员ID={}，商品ID={}，审核状态={}", adminId, productId, dto == null ? null : dto.getAuditStatus());
        productService.auditProduct(productId, dto);
        return Result.success();
    }

    @PostMapping("/admin-api/products/{productId}/remove")
    @ApiOperation(value = "强制下架商品", notes = "用于平台治理或违规商品处理")
    @ApiImplicitParam(name = "token", value = "管理员登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Void> removeProduct(
            @ApiParam(value = "商品 ID", required = true, example = "10") @PathVariable Long productId,
            @ApiParam(value = "强制下架请求", required = true) @RequestBody AdminProductRemoveDTO dto) {
        Long adminId = BaseContext.getCurrentId();
        String reason = dto == null ? null : dto.getReason();
        log.info("后台强制下架商品，管理员ID={}，商品ID={}，原因={}", adminId, productId, reason);
        productService.removeProductByAdmin(productId, reason);
        return Result.success();
    }
}
