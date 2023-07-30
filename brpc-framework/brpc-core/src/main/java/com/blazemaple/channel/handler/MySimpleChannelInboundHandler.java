package com.blazemaple.channel.handler;

import com.blazemaple.BrpcBootstrap;
import com.blazemaple.transport.message.BrpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * @author BlazeMaple
 * @description 测试用的ChannelHandler
 * @date 2023/7/23 22:09
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<BrpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BrpcResponse brpcResponse) throws Exception {
        Object returnValue = brpcResponse.getBody();
        CompletableFuture<Object> completableFuture = BrpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(returnValue);
    }
}
