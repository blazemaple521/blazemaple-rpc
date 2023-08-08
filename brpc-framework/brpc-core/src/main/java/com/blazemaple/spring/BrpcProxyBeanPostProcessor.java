package com.blazemaple.spring;

import com.blazemaple.annotation.BrpcServiceReference;
import com.blazemaple.proxy.BrpcProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/8/8 13:12
 */
@Component
public class BrpcProxyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            BrpcServiceReference brpcServiceReference = field.getAnnotation(BrpcServiceReference.class);
            if (brpcServiceReference != null) {
                Class<?> type = field.getType();
                Object proxy = BrpcProxyFactory.getProxy(type);
                field.setAccessible(true);
                try {
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }
}
