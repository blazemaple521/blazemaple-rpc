package com.blazemaple;

import com.blazemaple.discovery.Registry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;


/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/20 2:16
 */
@Slf4j
public class ReferenceConfig<T> {

    private Class<T> interfaceRef;

    private Registry registry;

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public Class<T> getInterface() {
        return interfaceRef;
    }

    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<T>[] classes = new Class[]{interfaceRef};
        Object invokeMethod = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("invoke method");
                //从注册中心获取服务地址 ip+port
                //todo 1.每次调用相关方法的时候都需要从注册中心获取服务列表吗？==> 本地缓存+watcher
                //     2.我们如何合理的选择一个可用的服务地址？ ==> 负载均衡
                List<InetSocketAddress> addressList = registry.lookup(interfaceRef.getName());
                if (log.isDebugEnabled()){
                    log.debug("service consumer find service【{}】and it's ip is【{}】",interfaceRef.getName(),addressList);
                }
                return null;
            }
        });

        return (T) invokeMethod;

    }
}
