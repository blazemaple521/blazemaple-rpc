package com.blazemaple;

import com.blazemaple.annotation.BrpcService;
import com.blazemaple.channel.handler.BrpcRequestDecoder;
import com.blazemaple.channel.handler.BrpcResponseEncoder;
import com.blazemaple.channel.handler.MethodCallHandler;
import com.blazemaple.config.Configuration;
import com.blazemaple.core.BrpcShutdownHook;
import com.blazemaple.core.HeartbeatDetector;
import com.blazemaple.discovery.RegistryConfig;
import com.blazemaple.loadbalancer.LoadBalancer;
import com.blazemaple.transport.message.BrpcRequest;
import com.blazemaple.utils.IdGenerator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/20 1:32
 */
@Slf4j
public class BrpcBootstrap {

    //BrpcBootstrap是一个单例，每个应用程序只有一个实例

    private static final BrpcBootstrap brpcBootstrap = new BrpcBootstrap();

    //全局的基础配置
    private final Configuration configuration;

    // 保存request对象，可以到当前线程中随时获取
    public static final ThreadLocal<BrpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    //维护已发布且暴露的服务列表
    public static final Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);
    //缓存channel
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    public final static TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();
    // 定义全局的对外挂起的 completableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);


    private BrpcBootstrap() {
        configuration = new Configuration();
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
        configuration.setApplicationName(applicationName);
        return this;
    }

    /**
     * 设置注册中心
     *
     * @param registryConfig 注册中心配置
     * @return this
     */

    public BrpcBootstrap registry(RegistryConfig registryConfig) {
        configuration.setRegistryConfig(registryConfig);

        return this;
    }

    /**
     * 配置负载均衡策略
     *
     * @param loadBalancer 注册中心
     * @return this当前实例
     */
    public BrpcBootstrap loadBalancer(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
        return this;
    }


    /**
     * 发布服务
     *
     * @param service 服务配置
     * @return this
     */

    public BrpcBootstrap publish(ServiceConfig<?> service) {
        configuration.getRegistryConfig().getRegistry().register(service);
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
        for (ServiceConfig<?> service : services) {
            this.publish(service);
        }
        return this;
    }

    /**
     * 启动引导程序
     */
    public void start() {
        Runtime.getRuntime().addShutdownHook(new BrpcShutdownHook());

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
            ChannelFuture channelFuture = serverBootstrap.bind(configuration.getPort()).sync();
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
        HeartbeatDetector.detectHeartbeat(reference.getInterface().getName());
        reference.setRegistry(configuration.getRegistryConfig().getRegistry());
        reference.setGroup(configuration.getGroup());
        return this;
    }

    /**
     * 设置序列化协议
     *
     * @param serializeType 序列化方式
     * @return this
     */

    public BrpcBootstrap serializer(String serializeType) {
        configuration.setSerializeType(serializeType);
        if (log.isDebugEnabled()) {
            log.debug("set serializeType:【{}】", serializeType);
        }
        return this;
    }

    /**
     * 设置压缩协议
     *
     * @param compressType 压缩方式
     * @return this
     */
    public BrpcBootstrap compressor(String compressType) {
        configuration.setCompressType(compressType);
        if (log.isDebugEnabled()) {
            log.debug("set compressType:【{}】", compressType);
        }
        return this;
    }


    /**
     * 扫描包
     *
     * @param packageName 包名
     * @return this
     */
    public BrpcBootstrap scan(String packageName) {
        List<String> classNames = getAllClassNames(packageName);
        List<Class<?>> classes = classNames.stream().map(className -> {
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).filter(clazz -> clazz.getAnnotation(BrpcService.class) != null)
            .collect(Collectors.toList());

        for (Class<?> clazz : classes) {
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance = null;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            BrpcService brpcService = clazz.getAnnotation(BrpcService.class);
            String group = brpcService.group();

            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                serviceConfig.setGroup(group);
                if (log.isDebugEnabled()) {
                    log.debug("Service【{}】has been published through package scanning.", anInterface);
                }
                // 3、发布
                publish(serviceConfig);
            }
        }
        return this;
    }

    /**
     * 获取包下的所有的类名
     * @param packageName 包名
     * @return 类名集合
     */

    private List<String> getAllClassNames(String packageName) {
        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if (url == null) {
            throw new RuntimeException("包扫描时，发现路径不存在.");
        }
        String absolutePath = url.getPath();
        //
        List<String> classNames = new ArrayList<>();
        classNames = recursionFile(absolutePath, classNames, basePath);

        return classNames;
    }

    /**
     * 递归获取文件夹下的所有的文件
     * @param absolutePath  绝对路径
     * @param classNames   类名集合
     * @param basePath     基础路径
     * @return 类名集合
     */

    private List<String> recursionFile(String absolutePath, List<String> classNames, String basePath) {
        // 获取文件
        File file = new File(absolutePath);
        // 判断文件是否是文件夹
        if (file.isDirectory()) {
            // 找到文件夹的所有的文件
            File[] children = file.listFiles(
                pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if (children == null || children.length == 0) {
                return classNames;
            }
            for (File child : children) {
                if (child.isDirectory()) {
                    // 递归调用
                    recursionFile(child.getAbsolutePath(), classNames, basePath);
                } else {
                    // 文件 --> 类的权限定名称
                    String className = getClassNameByAbsolutePath(child.getAbsolutePath(), basePath);
                    classNames.add(className);
                }
            }

        } else {
            // 文件 --> 类的权限定名称
            String className = getClassNameByAbsolutePath(absolutePath, basePath);
            classNames.add(className);
        }
        return classNames;
    }

    /**
     * 通过绝对路径获取类的权限定名称
     * @param absolutePath 绝对路径
     * @param basePath 基础路径
     * @return 类的权限定名称
     */

    private String getClassNameByAbsolutePath(String absolutePath, String basePath) {
        String fileName = absolutePath
            .substring(absolutePath.indexOf(basePath.replaceAll("/", "\\\\")))
            .replaceAll("\\\\", ".");

        fileName = fileName.substring(0, fileName.indexOf(".class"));
        return fileName;
    }

    /**
     * 获取配置
     * @return 配置
     */

    public Configuration getConfiguration() {
        return configuration;
    }

    public BrpcBootstrap group(String group) {
        configuration.setGroup(group);
        return this;
    }

    /**
     * 设置端口
     * @param port 端口
     * @return this
     */
    public BrpcBootstrap port(int port) {
        configuration.setPort(port);
        return this;
    }

    /**
     * 设置ID生成器
     * @param idGenerator id生成器
     * @return this
     */

    public BrpcBootstrap idGenerator(IdGenerator idGenerator) {
        configuration.setIdGenerator(idGenerator);
        return this;
    }

}
