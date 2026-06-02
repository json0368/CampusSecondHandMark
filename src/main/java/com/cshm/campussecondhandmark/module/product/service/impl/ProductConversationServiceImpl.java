package com.cshm.campussecondhandmark.module.product.service.impl;

import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.module.product.enums.ProductAuditStatusEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductSaleStatusEnum;
import com.cshm.campussecondhandmark.module.product.mapper.ProductMapper;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ChatConversationOpenRequest;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ChatUserProfileDTO;
import com.cshm.campussecondhandmark.module.product.pojo.entity.Product;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductConversationOpenVO;
import com.cshm.campussecondhandmark.module.product.service.ProductConversationService;
import com.cshm.campussecondhandmark.module.product.service.support.ChatServiceClient;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import com.cshm.campussecondhandmark.module.user.service.support.UserAccessValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductConversationServiceImpl implements ProductConversationService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserAccessValidator userAccessValidator;

    @Autowired
    private ChatServiceClient chatServiceClient;

    @Override
    public ProductConversationOpenVO openConversation(Long currentUserId, Long productId) {
        User buyer = userAccessValidator.getNormalUserOrThrow(currentUserId);
        Product product = getProductOrThrow(productId);
        if (!isPublicVisible(product)) {
            throw new BaseException("商品不存在或已下架");
        }
        if (buyer.getId().equals(product.getSellerId())) {
            throw new BaseException("不能联系自己的商品");
        }
        if (product.getSellerId() == null) {
            throw new BaseException("卖家状态异常，暂时无法联系");
        }
        User seller = userAccessValidator.getNormalUserOrThrow(
                product.getSellerId(),
                "卖家状态异常，暂时无法联系",
                "卖家状态异常，暂时无法联系");
        return chatServiceClient.openProductConversation(buildRequest(buyer, seller, product));
    }

    private Product getProductOrThrow(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BaseException("商品不存在");
        }
        return product;
    }

    private boolean isPublicVisible(Product product) {
        return product.getAuditStatus() == ProductAuditStatusEnum.APPROVED
                && product.getSaleStatus() == ProductSaleStatusEnum.ON_SHELF;
    }

    private ChatConversationOpenRequest buildRequest(User buyer, User seller, Product product) {
        ChatConversationOpenRequest request = new ChatConversationOpenRequest();
        request.setBusinessKey(buildBusinessKey(buyer.getId(), seller.getId(), product.getId()));
        request.setBuyerId(buyer.getId());
        request.setSellerId(seller.getId());
        request.setProductId(product.getId());
        request.setProductTitle(product.getTitle());
        request.setProductCoverUrl(product.getCoverImageUrl());
        request.setBuyerProfile(buildProfile(buyer));
        request.setSellerProfile(buildProfile(seller));
        return request;
    }

    private ChatUserProfileDTO buildProfile(User user) {
        ChatUserProfileDTO profile = new ChatUserProfileDTO();
        profile.setNickname(user.getNickname());
        profile.setAvatarUrl(user.getAvatarUrl());
        return profile;
    }

    private String buildBusinessKey(Long buyerId, Long sellerId, Long productId) {
        return "buyer:" + buyerId + ":seller:" + sellerId + ":product:" + productId;
    }
}
