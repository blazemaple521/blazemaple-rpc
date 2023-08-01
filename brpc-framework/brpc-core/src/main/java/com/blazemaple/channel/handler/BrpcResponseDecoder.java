package com.blazemaple.channel.handler;

import com.blazemaple.compress.Compressor;
import com.blazemaple.compress.CompressorFactory;
import com.blazemaple.constant.RequestType;
import com.blazemaple.serialize.Serializer;
import com.blazemaple.serialize.SerializerFactory;
import com.blazemaple.transport.message.BrpcRequest;
import com.blazemaple.transport.message.BrpcResponse;
import com.blazemaple.transport.message.MessageFormatConstant;
import com.blazemaple.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/26 23:50
 */
@Slf4j
public class BrpcResponseDecoder extends LengthFieldBasedFrameDecoder {

    public BrpcResponseDecoder() {
        super(
            MessageFormatConstant.MAX_FRAME_LENGTH,
            MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH
                + MessageFormatConstant.HEADER_FIELD_LENGTH,
            MessageFormatConstant.FULL_FIELD_LENGTH,
            -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH
                + MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FIELD_LENGTH),
            0
        );
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf byteBuf) {
            return decodeFrame(byteBuf);
        }
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MessageFormatConstant.MAGIC[i]) {
                throw new RuntimeException("Request magic is not right");
            }
        }
        byte version = byteBuf.readByte();
        if (version > MessageFormatConstant.VERSION) {
            throw new RuntimeException("Request version is not right");
        }
        short headLength = byteBuf.readShort();
        int fullLength = byteBuf.readInt();
        byte responseCode = byteBuf.readByte();
        byte serializeType = byteBuf.readByte();
        byte compressType = byteBuf.readByte();
        long requestId = byteBuf.readLong();
        long timestamp = byteBuf.readLong();
        BrpcResponse brpcResponse = BrpcResponse.builder()
            .requestId(requestId)
            .serializeType(serializeType)
            .compressType(compressType)
            .code(responseCode)
            .timeStamp(timestamp)
            .build();

        int payloadLength = fullLength - headLength;
        byte[] payloadBytes = new byte[payloadLength];
        byteBuf.readBytes(payloadBytes);

       if(payloadBytes!=null && payloadBytes.length>0){
           Compressor compressor= CompressorFactory.getCompressor(compressType).getImpl();
           payloadBytes = compressor.decompress(payloadBytes);

           //反序列化
           Serializer serializer = SerializerFactory.getSerializer(serializeType).getImpl();
           Object body = serializer.deserialize(payloadBytes, Object.class);
           brpcResponse.setBody(body);
       }
        if (log.isDebugEnabled()) {
            log.debug("Decode response: 【{}】 finished", brpcResponse.getRequestId());
        }

        return brpcResponse;
    }
}
