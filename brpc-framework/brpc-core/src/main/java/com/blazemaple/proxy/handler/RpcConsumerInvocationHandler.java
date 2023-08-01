package com.blazemaple.proxy.handler;

import com.blazemaple.BrpcBootstrap;
import com.blazemaple.NettyBootstrapInitializer;
import com.blazemaple.compress.CompressorFactory;
import com.blazemaple.constant.RequestType;
import com.blazemaple.discovery.Registry;
import com.blazemaple.exceptions.DiscoveryException;
import com.blazemaple.exceptions.NetworkException;
import com.blazemaple.serialize.SerializerFactory;
import com.blazemaple.transport.message.BrpcRequest;
import com.blazemaple.transport.message.RequestPayload;
import com.blazemaple.utils.IdGenerator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/23 4:47
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {

    private final Registry registry;
    private final Class<?> interfaceRef;

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.debug("invoke method");
        //封装报文
        RequestPayload requestPayload = RequestPayload.builder()
            .interfaceName(interfaceRef.getName())
            .methodName(method.getName())
            .parametersType(method.getParameterTypes())
            .parametersValue(args)
            .returnType(method.getReturnType())
            .build();

        BrpcRequest brpcRequest = BrpcRequest.builder()
            .requestId(BrpcBootstrap.ID_GENERATOR.getId())
            .compressType(CompressorFactory.getCompressor(BrpcBootstrap.COMPRESS_TYPE).getCode())
            .serializeType(SerializerFactory.getSerializer(BrpcBootstrap.SERIALIZE_TYPE).getCode())
            .requestType(RequestType.REQUEST.getId())
            .timeStamp(System.currentTimeMillis())
            .requestPayload(requestPayload)
            .build();

        BrpcBootstrap.REQUEST_THREAD_LOCAL.set(brpcRequest);

        InetSocketAddress address = BrpcBootstrap.LOAD_BALANCER.selectServiceAddress(interfaceRef.getName());
        if (log.isDebugEnabled()) {
            log.debug("service consumer find service【{}】and it's ip is【{}】", interfaceRef.getName(),
                address);
        }
        Channel channel = getAvailableChannel(address);
        if (log.isDebugEnabled()) {
            log.debug("get channel success with address【{}】", address);
        }
        //同步策略
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        BrpcBootstrap.PENDING_REQUEST.put(brpcRequest.getRequestId(), completableFuture);
        channel.writeAndFlush(brpcRequest)
            .addListener((ChannelFutureListener) promise -> {
                if (!promise.isSuccess()) {
                    log.error("send message to server【{}】failed", address);
                    completableFuture.completeExceptionally(promise.cause());
                }
            });

        BrpcBootstrap.REQUEST_THREAD_LOCAL.remove();

        return completableFuture.get(10, TimeUnit.SECONDS);
    }

    private Channel getAvailableChannel(InetSocketAddress address) {
        Channel channel = BrpcBootstrap.CHANNEL_CACHE.get(address);
        if (channel == null) {
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap().connect(address)
                .addListener((ChannelFutureListener) promise -> {
                    if (promise.isDone()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Connect to server【{}】success", address);
                        }
                        channelFuture.complete(promise.channel());
                    } else if (!promise.isSuccess()) {
                        channelFuture.completeExceptionally(promise.cause());
                    }
                });
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("Get channel failed", e);
                throw new DiscoveryException(e);
            }
            BrpcBootstrap.CHANNEL_CACHE.put(address, channel);
        }
        if (channel == null) {
            throw new NetworkException("Get channel failed!");
        }

        return channel;
    }
}
