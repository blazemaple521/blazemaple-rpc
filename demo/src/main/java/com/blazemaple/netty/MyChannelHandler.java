package com.blazemaple.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/18 1:22
 */
public class MyChannelHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in=(ByteBuf)msg;
        System.out.println("server received:---->"+in.toString(StandardCharsets.UTF_8));
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer("hello,client!".getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //出现异常时打印并关闭通道
        cause.printStackTrace();
        ctx.close();
    }
}
