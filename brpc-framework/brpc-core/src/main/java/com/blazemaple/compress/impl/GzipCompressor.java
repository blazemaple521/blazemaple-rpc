package com.blazemaple.compress.impl;

import com.blazemaple.compress.Compressor;
import com.blazemaple.exceptions.CompressException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/30 14:13
 */
@Slf4j
public class GzipCompressor implements Compressor {

    @Override
    public byte[] compress(byte[] bytes) {
        if (bytes == null || bytes.length==0){
            return null;
        }
        try (
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos);
        ) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            byte[] result = baos.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("gzip compress success,origin size:【{}】,compressed size:【{}】", bytes.length, result.length);
            }
            return result;
        } catch (IOException e){
            log.error("gzip compress error",e);
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if (bytes == null || bytes.length==0){
            return null;
        }
        try (
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            GZIPInputStream gzipInputStream = new GZIPInputStream(bais);
        ) {
            byte[] result = gzipInputStream.readAllBytes();
            if (log.isDebugEnabled()) {
                log.debug("gzip decompress success,origin size:【{}】,compressed size:【{}】", bytes.length, result.length);
            }
            return result;
        } catch (IOException e){
            log.error("gzip decompress error",e);
            throw new CompressException(e);
        }
    }
}
