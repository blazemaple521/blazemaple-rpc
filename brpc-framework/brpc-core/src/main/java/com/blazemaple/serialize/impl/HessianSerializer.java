package com.blazemaple.serialize.impl;

import com.blazemaple.exceptions.SerializeException;
import com.blazemaple.serialize.Serializer;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/29 20:48
 */
@Slf4j
public class HessianSerializer implements Serializer {

    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        try (
            // 将流的定义写在这里会自动关闭，不需要在写finally
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ) {
            Hessian2Output hessian2Output = new Hessian2Output(baos);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            byte[] result = baos.toByteArray();
            return result;
        } catch (IOException e) {
            log.error("HessianSerializer serialize object 【{}】 error",object,e);
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if(bytes == null || clazz == null){
            return null;
        }
        try (
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ) {

            Hessian2Input hessian2Input = new Hessian2Input(bais);
            T t = (T) hessian2Input.readObject();
            return t;
        } catch (IOException  e) {
            log.error("HessianSerializer deserialize clazz 【{}】 error",clazz,e);
            throw new SerializeException(e);
        }
    }
}
