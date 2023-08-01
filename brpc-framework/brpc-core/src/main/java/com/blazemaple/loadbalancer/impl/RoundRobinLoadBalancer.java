package com.blazemaple.loadbalancer.impl;

import com.blazemaple.exceptions.LoadBalancerException;
import com.blazemaple.loadbalancer.AbstractLoadBalancer;
import com.blazemaple.loadbalancer.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/30 20:13
 */
@Slf4j
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {


    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new RoundRobinSelector(serviceList);
    }

    private static class RoundRobinSelector implements Selector{

        private List<InetSocketAddress> serviceList;
        private AtomicInteger index;

        public RoundRobinSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {
            if(serviceList == null || serviceList.size() == 0){
                log.error("it was found that the serviceList is empty when selecting nodes for loadBalancing");
                throw new LoadBalancerException();
            }

            InetSocketAddress address = serviceList.get(index.get());

            if(index.get() == serviceList.size() - 1){
                index.set(0);
            } else {
                index.incrementAndGet();
            }

            return address;
        }
    }

}
