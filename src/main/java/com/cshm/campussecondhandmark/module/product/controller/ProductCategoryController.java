package com.cshm.campussecondhandmark.module.product.controller;

import com.cshm.campussecondhandmark.common.result.Result;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductCategoryVO;
import com.cshm.campussecondhandmark.module.product.service.ProductCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api/categories")
@Slf4j
@Api(tags = "商品分类")
public class ProductCategoryController {

    @Autowired
    private ProductCategoryService productCategoryService;

    @GetMapping("")
    @ApiOperation("获取启用中的商品分类")
    public Result<List<ProductCategoryVO>> listCategories() {
        log.info("获取启用中的商品分类");
        return Result.success(productCategoryService.listEnabledCategories());
    }
}
