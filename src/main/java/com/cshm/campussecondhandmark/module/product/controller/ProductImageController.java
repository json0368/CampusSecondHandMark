package com.cshm.campussecondhandmark.module.product.controller;

import com.cshm.campussecondhandmark.common.result.Result;
import com.cshm.campussecondhandmark.module.product.service.ProductImageService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/product-images")
@Slf4j
@Api("商品图片上传")
public class ProductImageController {

    @Autowired
    private ProductImageService productImageService;

    @PostMapping("/upload")
    public Result uploadFile(MultipartFile file) {
        return Result.success(productImageService.uploadFile(file));
    }
}
