package com.cshm.campussecondhandmark.module.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductAuditDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductAuditQueryDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductCreateDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductQueryDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductSearchDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductUpdateDTO;
import com.cshm.campussecondhandmark.module.product.pojo.entity.Product;
import com.cshm.campussecondhandmark.module.product.pojo.vo.MyProductVO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductAuditVO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductDetailVO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductSummaryVO;

public interface ProductService extends IService<Product> {

    Long createProduct(Long currentUserId, ProductCreateDTO dto);

    void updateProduct(Long currentUserId, Long productId, ProductUpdateDTO dto);

    PageResult<ProductSummaryVO> pageProducts(ProductSearchDTO dto);

    ProductDetailVO getProductDetail(Long productId, Long currentUserId);

    PageResult<MyProductVO> pageMyProducts(Long currentUserId, ProductQueryDTO dto);

    PageResult<ProductAuditVO> pageAuditProducts(ProductAuditQueryDTO dto);

    void auditProduct(Long productId, ProductAuditDTO dto);

    void offShelfProduct(Long currentUserId, Long productId);

    void removeProductByAdmin(Long productId, String reason);
}
