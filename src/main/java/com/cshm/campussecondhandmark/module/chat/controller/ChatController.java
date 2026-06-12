package com.cshm.campussecondhandmark.module.chat.controller;

import bot.Bot;
import bot.BotServiceGrpc;
import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.result.Result;
import com.cshm.campussecondhandmark.module.chat.pojo.dto.ChatRoomCreateDTO;
import com.cshm.campussecondhandmark.module.chat.pojo.vo.ChatCredentialsVO;
import com.cshm.campussecondhandmark.module.chat.pojo.vo.ChatRoomVO;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@Slf4j
@Api(tags = "聊天接口")
public class ChatController {

    @Autowired
    private BotServiceGrpc.BotServiceBlockingStub botStub;

    @Value("${cshm.chat.homeserver.url}")
    private String homeserverUrl;

    @GetMapping("/credentials")
    @ApiOperation(value = "获取 Matrix 凭证", notes = "获取当前用户的 Matrix 登录凭证，首次调用会自动注册")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<ChatCredentialsVO> getCredentials() {
        Long currentUserId = BaseContext.getCurrentId();
        String platformUserId = String.valueOf(currentUserId);
        log.info("获取 Matrix 凭证，用户ID={}", currentUserId);

        try {
            Bot.RegisterUserResponse response = botStub.registerUser(
                    Bot.RegisterUserRequest.newBuilder()
                            .setPlatformUserId(platformUserId)
                            .build()
            );

            ChatCredentialsVO vo = new ChatCredentialsVO();
            vo.setAccessToken(response.getAccessToken());
            vo.setUserId(response.getMatrixUserId());
            vo.setDeviceId(response.getDeviceId());
            vo.setHomeserverUrl(homeserverUrl);

            log.info("Matrix 凭证获取成功，userId={}", response.getMatrixUserId());
            return Result.success(vo);
        } catch (StatusRuntimeException e) {
            log.error("gRPC 调用失败: {}", e.getStatus(), e);
            throw new BaseException("聊天服务暂不可用");
        }
    }

    @PostMapping("/rooms")
    @ApiOperation(value = "创建聊天房间", notes = "为商品交易创建聊天房间，买卖双方自动邀请")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<ChatRoomVO> createRoom(@RequestBody ChatRoomCreateDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        String buyerPid = String.valueOf(currentUserId);
        String sellerPid = String.valueOf(dto.getSellerId());
        log.info("创建聊天房间，买家ID={}，卖家ID={}，商品={}", currentUserId, dto.getSellerId(), dto.getProductTitle());

        try {
            Bot.CreateRoomRequest.Builder requestBuilder = Bot.CreateRoomRequest.newBuilder()
                    .setBuyerPid(buyerPid)
                    .setSellerPid(sellerPid)
                    .setTitle(dto.getProductTitle());

            if (dto.getTopic() != null) {
                requestBuilder.setTopic(dto.getTopic());
            }

            Bot.CreateRoomResponse response = botStub.createRoom(requestBuilder.build());

            ChatRoomVO vo = new ChatRoomVO();
            vo.setMatrixRoomId(response.getMatrixRoomId());

            log.info("聊天房间创建成功，roomId={}", response.getMatrixRoomId());
            return Result.success(vo);
        } catch (StatusRuntimeException e) {
            log.error("gRPC 调用失败: {}", e.getStatus(), e);
            throw new BaseException("创建聊天房间失败");
        }
    }
}
