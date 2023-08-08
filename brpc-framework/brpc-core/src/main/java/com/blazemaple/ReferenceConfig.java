package com.blazemaple;

import com.blazemaple.discovery.Registry;
import com.blazemaple.proxy.handler.RpcConsumerInvocationHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;


/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/20 2:16
 */
@Slf4j
public class ReferenceConfig<T> {

    private Class<T> interfaceRef;
    private Registry registry;
    private String group;

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

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<T>[] classes = new Class[]{interfaceRef};
        InvocationHandler handler=new RpcConsumerInvocationHandler(registry,interfaceRef,group);
        Object invokeMethod = Proxy.newProxyInstance(classLoader, classes, handler);

        return (T) invokeMethod;

    }
}
