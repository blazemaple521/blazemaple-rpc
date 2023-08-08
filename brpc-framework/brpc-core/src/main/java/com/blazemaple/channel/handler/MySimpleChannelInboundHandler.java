package com.blazemaple.channel.handler;

import com.blazemaple.BrpcBootstrap;
import com.blazemaple.constant.RespCode;
import com.blazemaple.exceptions.ResponseException;
import com.blazemaple.loadbalancer.LoadBalancer;
import com.blazemaple.protection.CircuitBreaker;
import com.blazemaple.transport.message.BrpcRequest;
import com.blazemaple.transport.message.BrpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
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
        CompletableFuture<Object> completableFuture = BrpcBootstrap.PENDING_REQUEST.get(brpcResponse.getRequestId());
        SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
        Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = BrpcBootstrap.getInstance()
            .getConfiguration().getEveryIpCircuitBreaker();
        CircuitBreaker circuitBreaker = everyIpCircuitBreaker.get(socketAddress);

        byte code = brpcResponse.getCode();
        if(code == RespCode.FAIL.getCode()){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("The request with the current id [{}] returns an incorrect result with the response code [{}].",
                brpcResponse.getRequestId(),brpcResponse.getCode());
            throw new ResponseException(code,RespCode.FAIL.getDesc());

        } else if (code == RespCode.RATE_LIMIT.getCode()){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("The request with the current id [{}] is throttled with the response code [{}].",
                brpcResponse.getRequestId(),brpcResponse.getCode());
            throw new ResponseException(code,RespCode.RATE_LIMIT.getDesc());

        } else if (code == RespCode.RESOURCE_NOT_FOUND.getCode() ){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("The request with the current id [{}], target resource not found, response code [{}].",
                brpcResponse.getRequestId(),brpcResponse.getCode());
            throw new ResponseException(code,RespCode.RESOURCE_NOT_FOUND.getDesc());

        } else if (code == RespCode.SUCCESS.getCode() ){
            // 服务提供方，给予的结果
            Object returnValue = brpcResponse.getBody();
            completableFuture.complete(returnValue);
            if (log.isDebugEnabled()) {
                log.debug("Find completableFuture with the number [{}] and process the response result.", brpcResponse.getRequestId());
            }
        } else if(code == RespCode.SUCCESS_HEART_BEAT.getCode()){
            completableFuture.complete(null);
            if (log.isDebugEnabled()) {
                log.debug("Find completableFuture with the number [{}], process the heartbeat detection, and process the response result.", brpcResponse.getRequestId());
            }
        } else if(code == RespCode.BECLOSING.getCode()){
            completableFuture.complete(null);
            if (log.isDebugEnabled()) {
                log.debug("Request with current id [{}], access denied, target server is down, response code [{}].",
                    brpcResponse.getRequestId(),brpcResponse.getCode());
            }

            // 修正负载均衡器
            // 从健康列表中移除
            BrpcBootstrap.CHANNEL_CACHE.remove(socketAddress);
            // reLoadBalance
            LoadBalancer loadBalancer = BrpcBootstrap.getInstance()
                .getConfiguration().getLoadBalancer();
            // 重新进行负载均衡
            BrpcRequest brpcRequest = BrpcBootstrap.REQUEST_THREAD_LOCAL.get();
            loadBalancer.reLoadBalance(brpcRequest.getRequestPayload().getInterfaceName()
                ,BrpcBootstrap.CHANNEL_CACHE.keySet().stream().toList());

            throw new ResponseException(code,RespCode.BECLOSING.getDesc());
        }
    }
}
