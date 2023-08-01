package com.blazemaple.serialize.impl;

import com.blazemaple.exceptions.SerializeException;
import com.blazemaple.serialize.Serializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/29 21:56
 */
@Slf4j
public class kryoSerializer implements Serializer {

    private final ThreadLocal<Kryo> kryoThreadLocal=ThreadLocal.withInitial(()->{
       Kryo kryo=new Kryo();
       return kryo;
    });

    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        try (ByteArrayOutputStream baos=new ByteArrayOutputStream();
            Output output=new Output(baos)){
            Kryo kryo=kryoThreadLocal.get();
            kryo.writeObject(output,object);
            kryoThreadLocal.remove();
            byte[] result = output.toBytes();
            if (log.isDebugEnabled()) {
                log.debug("kryoSerializer serialize object 【{}】 success,object size:【{}】,serialized size:【{}】", object, object.toString().length(), result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("kryoSerializer serialize object 【{}】 error",object,e);
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes==null || clazz==null){
            return null;
        }
        try (ByteArrayInputStream baos=new ByteArrayInputStream(bytes);
            Input input=new Input(baos)){
            Kryo kryo=kryoThreadLocal.get();
            Object t = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            if (log.isDebugEnabled()) {
                log.debug("kryoSerializer deserialize clazz 【{}】 success,serialized size:【{}】,object size:【{}】", clazz, bytes.length, t.toString().length());
            }
            return clazz.cast(t);
        } catch (Exception e) {
            log.error("kryoSerializer deserialize clazz 【{}】 error",clazz,e);
            throw new SerializeException(e);
        }
    }
}
