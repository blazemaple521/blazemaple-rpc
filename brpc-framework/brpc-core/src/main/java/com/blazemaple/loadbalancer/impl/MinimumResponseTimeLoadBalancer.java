package com.blazemaple.loadbalancer.impl;

import com.blazemaple.BrpcBootstrap;
import com.blazemaple.loadbalancer.AbstractLoadBalancer;
import com.blazemaple.loadbalancer.Selector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/30 23:11
 */
@Slf4j
public class MinimumResponseTimeLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new MinimumResponseTimeSelector(serviceList);
    }

    private class MinimumResponseTimeSelector implements Selector {

        public MinimumResponseTimeSelector(List<InetSocketAddress> serviceList) {
        }

        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = BrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.firstEntry();
            if (entry != null) {
                if (log.isDebugEnabled()){
                    log.debug("Selected a service node with a response time of【{}】ms.",entry.getKey());
                }
                return (InetSocketAddress) entry.getValue().remoteAddress();
            }

            Channel channel = (Channel)BrpcBootstrap.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress)channel.remoteAddress();
        }
    }
}
