package com.blazemaple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/17 1:39
 */
public class NettyTest {

    public void testCompositeByteBuf() {
        ByteBuf header = Unpooled.buffer();
        ByteBuf body = Unpooled.buffer();
        CompositeByteBuf httpBuf= Unpooled.compositeBuffer();
        httpBuf.addComponents(header, body);
    }

}
