package com.blazemaple;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/20 1:57
 */
public class ServiceConfig<T> {
    private Class<?> interfaceProvider;
    private Object ref;

    public Class<?> getInterface() {
        return interfaceProvider;
    }

    public void setInterface(Class<?> interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }
}
