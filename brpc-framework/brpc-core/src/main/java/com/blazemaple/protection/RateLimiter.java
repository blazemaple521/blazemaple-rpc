package com.blazemaple.protection;

/**
 * @description
 *
 * @author BlazeMaple
 * @date 2023/8/6 21:02
 */

public interface RateLimiter {
    
    /**
     * 是否允许新的请求进入
     * @return true 可以进入  false  拦截
     */
    boolean allowRequest();
}