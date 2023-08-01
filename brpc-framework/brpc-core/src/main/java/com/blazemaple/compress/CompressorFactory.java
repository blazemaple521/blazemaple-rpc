package com.blazemaple.compress;

import com.blazemaple.compress.impl.GzipCompressor;
import com.blazemaple.config.ObjectWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author BlazeMaple
 * @description 压缩器工厂
 * @date 2023/7/30 14:04
 */
@Slf4j
public class CompressorFactory {

    private final static ConcurrentHashMap<String, ObjectWrapper<Compressor>> COMPRESSOR_CACHE = new ConcurrentHashMap<>(
        8);
    private final static ConcurrentHashMap<Byte, ObjectWrapper<Compressor>> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(
        8);

    static {
        ObjectWrapper<Compressor> gzip = new ObjectWrapper<>((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip", gzip);
        COMPRESSOR_CACHE_CODE.put((byte) 1, gzip);
    }

    public static ObjectWrapper<Compressor> getCompressor(String compressType) {
        ObjectWrapper<Compressor> compressorWrapper = COMPRESSOR_CACHE.get(compressType);
        if(compressorWrapper == null){
            log.error("cant find your compressType 【{}】,use gzip default.",compressType);
            return COMPRESSOR_CACHE.get("gzip");
        }

        return COMPRESSOR_CACHE.get(compressType);
    }

    public static  ObjectWrapper<Compressor> getCompressor(Byte compressCode) {
        ObjectWrapper<Compressor> compressorWrapper = COMPRESSOR_CACHE_CODE.get(compressCode);
        if(compressorWrapper == null){
            log.error("cant find your compressCode 【{}】,use gzip default.",compressCode);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return COMPRESSOR_CACHE_CODE.get(compressCode);
    }

    /**
     * 新增一个新的序列化器
     * @param compressorObjectWrapper 序列化器的包装
     */
    public static void addCompressor(ObjectWrapper<Compressor> compressorObjectWrapper){
        COMPRESSOR_CACHE.put(compressorObjectWrapper.getName(),compressorObjectWrapper);
        COMPRESSOR_CACHE_CODE.put(compressorObjectWrapper.getCode(),compressorObjectWrapper);
    }

}
