package com.blazemaple.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @description
 *
 * @author BlazeMaple
 * @date 2023/8/6 10:33
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TryTimes {
    
    int tryTimes() default 3;
    int intervalTime() default 2000;
    
}