package com.blazemaple.serialize.impl;

import com.alibaba.fastjson2.JSON;
import com.blazemaple.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/29 20:48
 */
@Slf4j
public class JsonSerializer implements Serializer {

    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        byte[] result = JSON.toJSONBytes(object);
        return result;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null) {
            return null;
        }
        T t = JSON.parseObject(bytes, clazz);
        return t;
    }
}
