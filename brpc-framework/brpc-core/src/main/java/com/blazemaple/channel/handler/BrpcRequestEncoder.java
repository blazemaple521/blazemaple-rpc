package com.blazemaple.channel.handler;

import com.blazemaple.BrpcBootstrap;
import com.blazemaple.constant.RequestType;
import com.blazemaple.serialize.Serializer;
import com.blazemaple.serialize.SerializerFactory;
import com.blazemaple.serialize.impl.JdkSerializer;
import com.blazemaple.transport.message.BrpcRequest;
import com.blazemaple.transport.message.MessageFormatConstant;
import com.blazemaple.transport.message.RequestPayload;
import com.blazemaple.utils.serialize.SerializeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 自定义协议编码器
 * <p>
 * <pre>
 *   0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22
 *   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 *   |    magic          |ver |head  len|      full len     | qt | ser|comp|              RequestId                |
 *   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+---++----+
 *   |                                                                                                             |
 *   |                                            body                                                             |
 *   |                                                                                                             |
 *   +-------------------------------------------------------------------------------------------------------------+
 * </pre>
 * <p>
 * 4B magic(魔数)   --->brpc.getBytes() 1B version(版本)   ----> 1 2B header length 首部的长度 4B full length 报文总长度 1B serialize
 * 1B compress 1B requestType 8B requestId
 * <p>
 * body
 * <p>
 *
 * @author BlazeMaple
 * @description
 * @date 2023/7/26 16:34
 */
@Slf4j
public class BrpcRequestEncoder extends MessageToByteEncoder<BrpcRequest> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, BrpcRequest brpcRequest, ByteBuf byteBuf)
        throws Exception {
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
        byteBuf.writeByte(brpcRequest.getRequestType());
        byteBuf.writeByte(brpcRequest.getSerializeType());
        byteBuf.writeByte(brpcRequest.getCompressType());
        byteBuf.writeLong(brpcRequest.getRequestId());

        Serializer serializer = SerializerFactory.getSerializer(brpcRequest.getSerializeType()).getImpl();
        byte[] bodyBytes = serializer.serialize(brpcRequest.getRequestPayload());
        if (bodyBytes != null) {
            byteBuf.writeBytes(bodyBytes);
        }
        int bodyLength = bodyBytes == null ? 0 : bodyBytes.length;
        int writerIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length + MessageFormatConstant.HEADER_FIELD_LENGTH
            + MessageFormatConstant.VERSION_LENGTH);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
        byteBuf.writerIndex(writerIndex);

        if (log.isDebugEnabled()) {
            log.debug("Encode request: 【{}】 finished", brpcRequest.getRequestId());
        }

    }

}
