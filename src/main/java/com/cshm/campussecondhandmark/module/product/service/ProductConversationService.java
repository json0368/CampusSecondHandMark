package com.cshm.campussecondhandmark.module.product.service;

import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductConversationOpenVO;

public interface ProductConversationService {

    ProductConversationOpenVO openConversation(Long currentUserId, Long productId);
}
