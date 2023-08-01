package com.blazemaple.compress;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/30 14:03
 */
public interface Compressor {

        /**
         * 压缩
         * @param bytes 压缩前的字节数组
         * @return 压缩后的字节数组
         */
        byte[] compress(byte[] bytes);

        /**
         * 解压
         * @param bytes 压缩后的字节数组
         * @return 压缩前的字节数组
         */
        byte[] decompress(byte[] bytes);

}
