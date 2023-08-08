package com.blazemaple.channel.handler;

import com.blazemaple.BrpcBootstrap;
import com.blazemaple.ServiceConfig;
import com.blazemaple.constant.RequestType;
import com.blazemaple.constant.RespCode;
import com.blazemaple.core.ShutDownHolder;
import com.blazemaple.protection.RateLimiter;
import com.blazemaple.protection.TokenBuketRateLimiter;
import com.blazemaple.transport.message.BrpcRequest;
import com.blazemaple.transport.message.BrpcResponse;
import com.blazemaple.transport.message.RequestPayload;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Map;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/27 15:07
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<BrpcRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BrpcRequest brpcRequest) throws Exception {



        BrpcResponse brpcResponse=new BrpcResponse();
        brpcResponse.setRequestId(brpcRequest.getRequestId());
        brpcResponse.setSerializeType(brpcRequest.getSerializeType());
        brpcResponse.setCompressType(brpcRequest.getCompressType());

        Channel channel = channelHandlerContext.channel();

        // 查看关闭的挡板是否打开，如果挡板已经打开，返回一个错误的响应
        if( ShutDownHolder.BAFFLE.get() ){
            brpcResponse.setCode(RespCode.BECLOSING.getCode());
            brpcResponse.setTimeStamp(System.currentTimeMillis());
            channel.writeAndFlush(brpcResponse);
            return;
        }

        // 计数器加一
        ShutDownHolder.REQUEST_COUNTER.increment();

        SocketAddress socketAddress = channel.remoteAddress();
        Map<SocketAddress, RateLimiter> everyIpRateLimiter = BrpcBootstrap.getInstance().getConfiguration()
            .getEveryIpRateLimiter();
        RateLimiter rateLimiter = everyIpRateLimiter.get(socketAddress);

        if (rateLimiter==null){
            rateLimiter=new TokenBuketRateLimiter(10,10);
            everyIpRateLimiter.put(socketAddress,rateLimiter);
        }
        boolean allowRequest = rateLimiter.allowRequest();

        if (!allowRequest){
            brpcResponse.setCode(RespCode.RATE_LIMIT.getCode());
        }else if(brpcRequest.getRequestType()==RequestType.HEART_BEAT.getId()){
            brpcResponse.setCode(RespCode.SUCCESS_HEART_BEAT.getCode());
        }else {
            RequestPayload requestPayload = brpcRequest.getRequestPayload();
            try {
                Object result = callTargetMethod(requestPayload);
                if (log.isDebugEnabled()) {
                    log.debug("Call request: 【{}】 finished", brpcRequest.getRequestId());
                }
                brpcResponse.setCode(RespCode.SUCCESS.getCode());
                brpcResponse.setBody(result);
            } catch (Exception e) {
                log.error("The request numbered【{}】 had an exception during the call.", brpcRequest.getRequestId(),e);
                brpcResponse.setCode(RespCode.FAIL.getCode());
            }
        }

        brpcResponse.setTimeStamp(System.currentTimeMillis());
        channel.writeAndFlush(brpcResponse);

        ShutDownHolder.REQUEST_COUNTER.decrement();

    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParametersType();
        Object[] parametersValue = requestPayload.getParametersValue();

        ServiceConfig<?> serviceConfig = BrpcBootstrap.SERVICE_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();
        Object returnValue;
        try {
            Class<?> aClass = refImpl.getClass();
            Method method = aClass.getMethod(methodName, parametersType);
            returnValue = method.invoke(refImpl, parametersValue);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            log.error("invoke method:【{}】of class:【{}】failed", methodName, interfaceName, e);
            throw new RuntimeException(e);
        }
        return returnValue;
    }

}
