package com.cshm.campussecondhandmark.module.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.order.pojo.dto.OrderQueryDTO;
import com.cshm.campussecondhandmark.module.order.pojo.entity.TradeOrder;
import com.cshm.campussecondhandmark.module.order.pojo.vo.OrderDetailVO;
import com.cshm.campussecondhandmark.module.order.pojo.vo.OrderSummaryVO;

public interface TradeOrderService extends IService<TradeOrder> {

    Long createOrder(Long currentUserId, Long productId);

    PageResult<OrderSummaryVO> pageMyOrders(Long currentUserId, OrderQueryDTO dto);

    OrderDetailVO getOrderDetail(Long currentUserId, Long orderId);

    void confirmOrder(Long currentUserId, Long orderId);

    void cancelOrder(Long currentUserId, Long orderId, String remark);

    void completeOrder(Long currentUserId, Long orderId);
}
