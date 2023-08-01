package com.blazemaple.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author BlazeMaple
 * @description 服务调用方请求内容封装
 * @date 2023/7/26 15:52
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BrpcRequest {

    private long requestId;
    private byte requestType;
    private byte compressType;
    private byte serializeType;

    private long timeStamp;

    private RequestPayload requestPayload;

}
