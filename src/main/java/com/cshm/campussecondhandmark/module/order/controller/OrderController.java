package com.cshm.campussecondhandmark.module.order.controller;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.common.result.Result;
import com.cshm.campussecondhandmark.module.order.pojo.dto.OrderQueryDTO;
import com.cshm.campussecondhandmark.module.order.pojo.vo.OrderDetailVO;
import com.cshm.campussecondhandmark.module.order.pojo.vo.OrderSummaryVO;
import com.cshm.campussecondhandmark.module.order.service.TradeOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Slf4j
@Api(tags = "订单接口")
public class OrderController {

    @Autowired
    private TradeOrderService tradeOrderService;

    @PostMapping("")
    @ApiOperation(value = "创建订单", notes = "买家基于商品发起订单，初始状态为待确认")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Long> createOrder(
            @ApiParam(value = "商品 ID", required = true, example = "10") @RequestParam Long productId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("创建订单，用户ID={}，商品ID={}", currentUserId, productId);
        return Result.success(tradeOrderService.createOrder(currentUserId, productId));
    }

    @GetMapping("/mine")
    @ApiOperation("分页查询我的订单")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<PageResult<OrderSummaryVO>> pageMyOrders(OrderQueryDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("分页查询我的订单，用户ID={}", currentUserId);
        return Result.success(tradeOrderService.pageMyOrders(currentUserId, dto));
    }

    @GetMapping("/{orderId}")
    @ApiOperation("获取订单详情")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<OrderDetailVO> getOrderDetail(
            @ApiParam(value = "订单 ID", required = true, example = "1") @PathVariable Long orderId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("获取订单详情，用户ID={}，订单ID={}", currentUserId, orderId);
        return Result.success(tradeOrderService.getOrderDetail(currentUserId, orderId));
    }

    @PostMapping("/{orderId}/confirm")
    @ApiOperation(value = "确认订单", notes = "仅卖家可确认订单，订单进入交易中")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Void> confirmOrder(@ApiParam(value = "订单 ID", required = true, example = "1") @PathVariable Long orderId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("确认订单，用户ID={}，订单ID={}", currentUserId, orderId);
        tradeOrderService.confirmOrder(currentUserId, orderId);
        return Result.success();
    }

    @PostMapping("/{orderId}/cancel")
    @ApiOperation(value = "取消订单", notes = "买卖双方均可取消订单")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Void> cancelOrder(
            @ApiParam(value = "订单 ID", required = true, example = "1") @PathVariable Long orderId,
            @ApiParam(value = "取消备注，可作为取消原因", example = "双方临时调整交易时间") @RequestParam(required = false) String remark) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("取消订单，用户ID={}，订单ID={}", currentUserId, orderId);
        tradeOrderService.cancelOrder(currentUserId, orderId, remark);
        return Result.success();
    }

    @PostMapping("/{orderId}/complete")
    @ApiOperation(value = "完成订单", notes = "仅买家可确认交易完成")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Void> completeOrder(@ApiParam(value = "订单 ID", required = true, example = "1") @PathVariable Long orderId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("完成订单，用户ID={}，订单ID={}", currentUserId, orderId);
        tradeOrderService.completeOrder(currentUserId, orderId);
        return Result.success();
    }
}
