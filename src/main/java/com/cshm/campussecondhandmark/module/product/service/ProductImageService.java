package com.cshm.campussecondhandmark.module.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cshm.campussecondhandmark.module.product.pojo.entity.ProductImage;
import org.springframework.web.multipart.MultipartFile;

public interface ProductImageService extends IService<ProductImage> {

    String uploadFile(MultipartFile file);
}
