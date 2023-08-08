package com.blazemaple.core;

import com.blazemaple.BrpcBootstrap;
import com.blazemaple.NettyBootstrapInitializer;
import com.blazemaple.compress.CompressorFactory;
import com.blazemaple.constant.RequestType;
import com.blazemaple.discovery.Registry;
import com.blazemaple.serialize.SerializerFactory;
import com.blazemaple.transport.message.BrpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/31 13:48
 */
@Slf4j
public class HeartbeatDetector {

    public static void detectHeartbeat(String serviceName){
        Registry registry = BrpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
        String group = BrpcBootstrap.getInstance().getConfiguration().getGroup();
        List<InetSocketAddress> socketAddresses = registry.lookup(serviceName,group);
        for (InetSocketAddress address : socketAddresses) {
            try {
                if (!BrpcBootstrap.CHANNEL_CACHE.containsKey(address)){
                    Channel channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    BrpcBootstrap.CHANNEL_CACHE.put(address,channel);
                }
            }catch (InterruptedException e){
                throw new RuntimeException(e);
            }
        }

        Thread thread = new Thread(() ->
            new Timer().scheduleAtFixedRate(new MyTimerTask(), 0, 2000)
            , "brpc-HeartbeatDetector-thread");
        thread.setDaemon(true);
        thread.start();
    }


    private static class MyTimerTask extends TimerTask {

        @Override
        public void run() {

            // 将响应时长的map清空
            BrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.clear();

            // 遍历所有的channel
            Map<InetSocketAddress, Channel> cache = BrpcBootstrap.CHANNEL_CACHE;
            for (Map.Entry<InetSocketAddress, Channel> entry : cache.entrySet()) {
                // 定义一个重试的次数
                int tryTimes = 3;
                while (tryTimes > 0) {
                    // 通过心跳检测处理每一个channel
                    Channel channel = entry.getValue();

                    long start = System.currentTimeMillis();
                    // 构建一个心跳请求
                    BrpcRequest brpcRequest = BrpcRequest.builder()
                        .requestId(BrpcBootstrap.getInstance().getConfiguration().getIdGenerator().getId())
                        .compressType(CompressorFactory.getCompressor(BrpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                        .requestType(RequestType.HEART_BEAT.getId())
                        .serializeType(SerializerFactory.getSerializer(BrpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                        .timeStamp(start)
                        .build();

                    // 4、写出报文
                    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                    // 将 completableFuture 暴露出去
                    BrpcBootstrap.PENDING_REQUEST.put(brpcRequest.getRequestId(), completableFuture);

                    channel.writeAndFlush(brpcRequest).addListener((ChannelFutureListener) promise -> {
                        if (!promise.isSuccess()) {
                            completableFuture.completeExceptionally(promise.cause());
                        }
                    });

                    long endTime;
                    try {
                        // 阻塞方法，get()方法如果得不到结果，就会一直阻塞
                        // 我们想不一直阻塞可以添加参数
                        completableFuture.get(1, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        e.printStackTrace();
                        // 一旦发生问题，需要优先重试
                        tryTimes --;
                        log.error("An exception occurred while connecting to the host with address【{}】. Retrying for the【{}】th time",
                            channel.remoteAddress(), 3 - tryTimes);

                        // 将重试的机会用尽，将失效的地址移出服务列表
                        if(tryTimes == 0){
                            BrpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                        }

                        // 尝试等到一段时间后重试
                        try {
                            Thread.sleep(10*(new Random().nextInt(5)));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }

                        continue;
                    }
                    Long time = endTime - start;

                    // 使用treemap进行缓存
                    BrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(time, channel);
                    log.debug("The response time to the server:【{}】 is 【{}】ms", entry.getKey(), time);
                    break;
                }
            }

            if (log.isDebugEnabled()) {
                log.info("-----------------------响应时间的treemap----------------------");
            }
            for (Map.Entry<Long, Channel> entry : BrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.entrySet()) {
                if (log.isDebugEnabled()) {
                    log.debug("[{}]--->channelId:[{}]", entry.getKey(), entry.getValue().id());
                }
            }
        }
    }

}
