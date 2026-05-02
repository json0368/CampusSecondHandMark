package com.cshm.campussecondhandmark.module.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cshm.campussecondhandmark.module.product.pojo.entity.ProductCategory;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductCategoryVO;

import java.util.List;

public interface ProductCategoryService extends IService<ProductCategory> {

    List<ProductCategoryVO> listEnabledCategories();
}
