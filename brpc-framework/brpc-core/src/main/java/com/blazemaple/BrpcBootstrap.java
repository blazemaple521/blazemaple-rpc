package com.blazemaple;

import com.blazemaple.channel.handler.BrpcRequestDecoder;
import com.blazemaple.channel.handler.BrpcResponseEncoder;
import com.blazemaple.channel.handler.MethodCallHandler;
import com.blazemaple.discovery.Registry;
import com.blazemaple.discovery.RegistryConfig;
import com.blazemaple.utils.IdGenerator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/20 1:32
 */
@Slf4j
public class BrpcBootstrap {

    //BrpcBootstrap是一个单例，每个应用程序只有一个实例

    private static BrpcBootstrap brpcBootstrap = new BrpcBootstrap();

    //相关基础配置
    private String applicationName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;

    public static final IdGenerator ID_GENERATOR = new IdGenerator(1,2);

    //维护已发布且暴露的服务列表
    public static final Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);
    //缓存channel
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    // 定义全局的对外挂起的 completableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);

    //todo 待处理
    private Registry registry;

    private int port = 8088;

    public static String SERIALIZE_TYPE = "jdk";


    private BrpcBootstrap() {
        //todo 构造启动引导程序，需要初始化一些参数
    }

    public static BrpcBootstrap getInstance() {
        return brpcBootstrap;
    }

    /**
     * 设置应用程序名称
     *
     * @param applicationName 应用名称
     * @return this
     */

    public BrpcBootstrap application(String applicationName) {
        //todo 设置应用程序名称
        this.applicationName = applicationName;
        return this;
    }

    /**
     * 设置注册中心
     *
     * @param registryConfig 注册中心配置
     * @return this
     */

    public BrpcBootstrap registry(RegistryConfig registryConfig) {
        this.registry = registryConfig.getRegistry();
        return this;
    }

    /**
     * 设置协议
     *
     * @param protocolConfig 协议配置
     * @return this
     */
    public BrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        if (log.isDebugEnabled()) {
            log.debug("set protocolConfig:{}", protocolConfig.toString());
        }
        //todo 设置序列化协议
        this.protocolConfig = protocolConfig;
        return this;
    }

    /**
     * 发布服务
     *
     * @param service 服务配置
     * @return this
     */

    public BrpcBootstrap publish(ServiceConfig<?> service) {
        //todo 发布服务
        registry.register(service);
        SERVICE_LIST.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * 批量发布服务
     *
     * @param services 服务配置列表
     * @return this
     */
    public BrpcBootstrap publish(List<ServiceConfig<?>> services) {
        //todo 发布服务
        for (ServiceConfig<?> service : services) {
            this.publish(service);
        }
        return this;
    }

    /**
     * 启动引导程序
     */
    public void start() {
        //todo 启动引导程序
        NioEventLoopGroup boss = new NioEventLoopGroup(2);
        NioEventLoopGroup worker = new NioEventLoopGroup(10);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new LoggingHandler())
                            .addLast(new BrpcRequestDecoder())
                            .addLast(new MethodCallHandler())
                            .addLast(new BrpcResponseEncoder());
                    }
                });
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            System.out.println("server started and listen " + channelFuture.channel().localAddress());
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public BrpcBootstrap reference(ReferenceConfig<?> reference) {
        //todo 引用服务
        reference.setRegistry(registry);
        return this;
    }

    /**
     * 设置序列化协议
     * @param serializeType 序列化方式
     * @return this
     */

    public BrpcBootstrap serializer(String serializeType) {
        SERIALIZE_TYPE = serializeType;
        if (log.isDebugEnabled()) {
            log.debug("set serializeType:【{}】", serializeType);
        }
        return this;
    }
}
