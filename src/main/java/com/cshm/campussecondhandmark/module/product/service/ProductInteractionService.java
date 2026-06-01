package com.cshm.campussecondhandmark.module.product.service;

import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductQueryDTO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductSummaryVO;

public interface ProductInteractionService {

    void favoriteProduct(Long currentUserId, Long productId);

    void unfavoriteProduct(Long currentUserId, Long productId);

    PageResult<ProductSummaryVO> pageMyFavorites(Long currentUserId, ProductQueryDTO dto);

    void recordBrowseHistory(Long currentUserId, Long productId);

    PageResult<ProductSummaryVO> pageMyBrowseHistory(Long currentUserId, ProductQueryDTO dto);

    void clearMyBrowseHistory(Long currentUserId);

    boolean isFavorited(Long currentUserId, Long productId);
}
