package com.blazemaple.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * @description
 *
 * @author BlazeMaple
 * @date 2023/8/7 19:44
 */
public class ShutDownHolder {
    
    // 用来标记请求挡板
    public static AtomicBoolean BAFFLE = new AtomicBoolean(false);
    
    // 用于请求的计数器
    public static LongAdder REQUEST_COUNTER = new LongAdder();
}