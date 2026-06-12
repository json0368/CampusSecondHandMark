package com.cshm.campussecondhandmark.module.chat.config;

import bot.BotServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@Configuration
@Slf4j
public class ChatGrpcConfig {

    @Value("${cshm.chat.bot.host}")
    private String botHost;

    @Value("${cshm.chat.bot.port}")
    private int botPort;

    private ManagedChannel channel;

    @Bean
    public ManagedChannel chatChannel() {
        channel = ManagedChannelBuilder.forAddress(botHost, botPort)
                .usePlaintext()
                .build();
        log.info("gRPC channel created: {}:{}", botHost, botPort);
        return channel;
    }

    @Bean
    public BotServiceGrpc.BotServiceBlockingStub botStub(ManagedChannel channel) {
        return BotServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            log.info("gRPC channel shut down");
        }
    }
}
