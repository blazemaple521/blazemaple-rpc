package com.blazemaple.serialize.impl;

import com.blazemaple.exceptions.SerializeException;
import com.blazemaple.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/29 16:25
 */
@Slf4j
public class JdkSerializer implements Serializer {

    @Override
    public byte[] serialize(Object object) {
        if (object== null) {
            return null;
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            outputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("JdkSerializer serialize object 【{}】 error",object,e);
            throw new SerializeException(e);
        }

    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if(bytes == null || clazz == null){
            return null;
        }
        try (
            ByteArrayInputStream byteArrayOutputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayOutputStream);
        ) {
            Object object = objectInputStream.readObject();
            return (T)object;
        } catch (IOException | ClassNotFoundException e) {
            log.error("JdkSerializer deserialize clazz 【{}】 error",clazz,e);
            throw new SerializeException(e);
        }
    }
}
