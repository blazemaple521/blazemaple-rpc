package com.blazemaple.channel.handler;

import com.blazemaple.BrpcBootstrap;
import com.blazemaple.ServiceConfig;
import com.blazemaple.constant.RespCode;
import com.blazemaple.transport.message.BrpcRequest;
import com.blazemaple.transport.message.BrpcResponse;
import com.blazemaple.transport.message.RequestPayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/27 15:07
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<BrpcRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BrpcRequest brpcRequest) throws Exception {
        RequestPayload requestPayload = brpcRequest.getRequestPayload();
        Object object = callTargetMethod(requestPayload);
        if (log.isDebugEnabled()) {
            log.debug("Call request: 【{}】 finished", brpcRequest.getRequestId());
        }
        BrpcResponse brpcResponse = BrpcResponse.builder()
            .requestId(brpcRequest.getRequestId())
            .serializeType(brpcRequest.getSerializeType())
            .compressType(brpcRequest.getCompressType())
            .code(RespCode.SUCCESS.getCode())
            .body(object)
            .build();
        channelHandlerContext.channel().writeAndFlush(brpcResponse);

    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParametersType();
        Object[] parametersValue = requestPayload.getParametersValue();

        ServiceConfig<?> serviceConfig = BrpcBootstrap.SERVICE_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();
        Object returnValue = null;
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
