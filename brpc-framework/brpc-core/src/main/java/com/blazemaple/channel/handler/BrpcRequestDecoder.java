package com.blazemaple.channel.handler;

import com.blazemaple.constant.RequestType;
import com.blazemaple.serialize.Serializer;
import com.blazemaple.serialize.SerializerFactory;
import com.blazemaple.transport.message.BrpcRequest;
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
public class BrpcRequestDecoder extends LengthFieldBasedFrameDecoder {

    public BrpcRequestDecoder() {
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
        byte requestType = byteBuf.readByte();
        byte serializeType = byteBuf.readByte();
        byte compressType = byteBuf.readByte();
        long requestId = byteBuf.readLong();
        BrpcRequest brpcRequest = BrpcRequest.builder()
            .requestId(requestId)
            .requestType(requestType)
            .serializeType(serializeType)
            .compressType(compressType)
            .build();

        //todo 心跳请求没有payload，直接返回
        if (requestType == RequestType.HEART_BEAT.getId()) {
            return brpcRequest;
        }

        int payloadLength = fullLength - headLength;
        byte[] payloadBytes = new byte[payloadLength];
        byteBuf.readBytes(payloadBytes);
        //todo 解压缩

        //反序列化
        Serializer serializer = SerializerFactory.getSerializer(serializeType).getImpl();
        RequestPayload requestPayload = serializer.deserialize(payloadBytes, RequestPayload.class);
        brpcRequest.setRequestPayload(requestPayload);

        if (log.isDebugEnabled()) {
            log.debug("Decode request: 【{}】 finished", brpcRequest.getRequestId());
        }

        return brpcRequest;
    }
}
