package com.blazemaple.channel;

import com.blazemaple.channel.handler.BrpcRequestEncoder;
import com.blazemaple.channel.handler.BrpcResponseDecoder;
import com.blazemaple.channel.handler.MySimpleChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/23 22:14
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
            .addLast(new LoggingHandler(LogLevel.DEBUG))
            .addLast(new BrpcRequestEncoder())
            .addLast(new BrpcResponseDecoder())
            .addLast(new MySimpleChannelInboundHandler());
    }
}
