package com.blazemaple.api.service;

import com.blazemaple.annotation.TryTimes;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/19 16:09
 */
public interface HelloBRPC {

    /**
     * 通用接口
     * @param msg 发送的消息
     * @return 返回的消息
     */
    @TryTimes(tryTimes = 3,intervalTime = 3000)
    String sayHello(String msg);

}
