package com.blazemaple.serialize;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/29 16:09
 */
public interface Serializer {

    /**
     * 序列化
     * @param object 待序列化的对象
     * @return 序列化后的字节数组
     */
    byte[] serialize(Object object);

    /**
     * 反序列化
     * @param bytes 待反序列化的字节数组
     * @param clazz 反序列化后的对象类型
     * @param <T> 反序列化后的对象类型
     * @return 反序列化后的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);

}
