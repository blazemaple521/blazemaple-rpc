package com.blazemaple.loadbalancer;

import java.net.InetSocketAddress;

/**
 * @description
 *
 * @author BlazeMaple
 * @date 2023/7/30 20:18
 */

public interface Selector {
    
    /**
     * 根据服务列表执行一种算法获取一个服务节点
     * @return 具体的服务节点
     */
    InetSocketAddress getNext();
    
}