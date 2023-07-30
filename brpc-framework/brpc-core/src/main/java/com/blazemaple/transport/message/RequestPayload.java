package com.blazemaple.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author BlazeMaple
 * @description 请求的接口方法的参数封装
 * @date 2023/7/26 16:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestPayload implements Serializable {

    private String interfaceName;
    private String methodName;
    private Class<?>[] parametersType;
    private Object[] parametersValue;
    private Class<?> returnType;

}
