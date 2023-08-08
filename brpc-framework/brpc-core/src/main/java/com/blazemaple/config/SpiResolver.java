package com.blazemaple.config;

import com.blazemaple.compress.Compressor;
import com.blazemaple.compress.CompressorFactory;
import com.blazemaple.serialize.Serializer;
import com.blazemaple.serialize.SerializerFactory;
import com.blazemaple.spi.SpiHandler;

import java.util.List;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/8/4 16:06
 */
public class SpiResolver {

    /**
     * 通过spi的方式加载配置项
     * @param configuration 配置上下文
     */
    public void loadFromSpi(Configuration configuration) {


        List<ObjectWrapper<Compressor>> objectWrappers = SpiHandler.getList(Compressor.class);
        if(objectWrappers != null){
            objectWrappers.forEach(CompressorFactory::addCompressor);
        }

        List<ObjectWrapper<Serializer>> serializerObjectWrappers = SpiHandler.getList(Serializer.class);
        if (serializerObjectWrappers != null){
            serializerObjectWrappers.forEach(SerializerFactory::addSerializer);
        }
    }

}
