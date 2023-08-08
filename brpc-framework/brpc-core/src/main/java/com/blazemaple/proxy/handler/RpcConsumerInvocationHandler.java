package com.blazemaple.proxy.handler;

import com.blazemaple.BrpcBootstrap;
import com.blazemaple.NettyBootstrapInitializer;
import com.blazemaple.annotation.TryTimes;
import com.blazemaple.compress.CompressorFactory;
import com.blazemaple.constant.RequestType;
import com.blazemaple.discovery.Registry;
import com.blazemaple.exceptions.DiscoveryException;
import com.blazemaple.exceptions.NetworkException;
import com.blazemaple.protection.CircuitBreaker;
import com.blazemaple.serialize.SerializerFactory;
import com.blazemaple.transport.message.BrpcRequest;
import com.blazemaple.transport.message.RequestPayload;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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

    private final String group;

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef,String group) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
        this.group=group;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 从接口中获取判断是否需要重试
        TryTimes tryTimesAnnotation = method.getAnnotation(TryTimes.class);

        // 默认值0,代表不重试
        int tryTimes = 0;
        int intervalTime = 0;
        if (tryTimesAnnotation != null) {
            tryTimes = tryTimesAnnotation.tryTimes();
            intervalTime = tryTimesAnnotation.intervalTime();
        }
        //封装报文
        while (true) {
            RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args)
                .returnType(method.getReturnType())
                .build();

            BrpcRequest brpcRequest = BrpcRequest.builder()
                .requestId(BrpcBootstrap.getInstance().getConfiguration().getIdGenerator().getId())
                .compressType(CompressorFactory.getCompressor(
                    BrpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                .serializeType(SerializerFactory.getSerializer(
                    BrpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                .requestType(RequestType.REQUEST.getId())
                .timeStamp(System.currentTimeMillis())
                .requestPayload(requestPayload)
                .build();

            BrpcBootstrap.REQUEST_THREAD_LOCAL.set(brpcRequest);

            InetSocketAddress address = BrpcBootstrap.getInstance().getConfiguration().getLoadBalancer()
                .selectServiceAddress(interfaceRef.getName(),group);
            if (log.isDebugEnabled()) {
                log.debug("service consumer find service【{}】and it's ip is【{}】", interfaceRef.getName(),
                    address);
            }

            Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = BrpcBootstrap.getInstance()
                .getConfiguration().getEveryIpCircuitBreaker();
            CircuitBreaker circuitBreaker = everyIpCircuitBreaker.get(address);
            if (circuitBreaker == null) {
                circuitBreaker = new CircuitBreaker(10, 0.5F);
                everyIpCircuitBreaker.put(address, circuitBreaker);
            }

            try {
                // 如果断路器是打开的
                if (brpcRequest.getRequestType() != RequestType.HEART_BEAT.getId() && circuitBreaker.isBreak()) {
                    // 定期打开
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            BrpcBootstrap.getInstance()
                                .getConfiguration().getEveryIpCircuitBreaker()
                                .get(address).reset();
                        }
                    }, 5000);

                    throw new RuntimeException("The current circuitBreaker is turned on and cannot send requests");
                }

                Channel channel = getAvailableChannel(address);
                if (log.isDebugEnabled()) {
                    log.debug("get channel success with address【{}】", address);
                }
                //异步策略
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
                Object result = completableFuture.get(10, TimeUnit.SECONDS);
                circuitBreaker.recordRequest();
                return result;
            } catch (Exception e) {
                tryTimes--;
                circuitBreaker.recordErrorRequest();
                try {
                    Thread.sleep(intervalTime);
                } catch (InterruptedException ex) {
                    log.error("An exception occurred while retrying was in progress.", ex);
                }
                if (tryTimes < 0) {
                    log.error("When calling the method【{}】 remotely, retry【{}】times, but still cannot be called",
                        method.getName(), tryTimes, e);
                    break;
                }
                log.error("An exception occurred while retrying【{}】.", 3 - tryTimes, e);
            }
        }
        throw new RuntimeException("Execute the remote method: " + method.getName() + "is failed");
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
