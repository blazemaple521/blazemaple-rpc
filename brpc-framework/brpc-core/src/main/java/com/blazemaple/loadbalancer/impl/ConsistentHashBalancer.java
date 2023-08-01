package com.blazemaple.loadbalancer.impl;

import com.blazemaple.BrpcBootstrap;
import com.blazemaple.loadbalancer.AbstractLoadBalancer;
import com.blazemaple.loadbalancer.Selector;
import com.blazemaple.transport.message.BrpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/30 21:48
 */
@Slf4j
public class ConsistentHashBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new ConsistentHashSelector(serviceList,128);
    }

    private class ConsistentHashSelector implements Selector {

        // hash环用来存储服务器节点
        private SortedMap<Integer,InetSocketAddress> circle= new TreeMap<>();
        // 虚拟节点的个数
        private int virtualNodes;

        public ConsistentHashSelector(List<InetSocketAddress> serviceList, int virtualNodes) {
            this.virtualNodes = virtualNodes;
            for (InetSocketAddress address : serviceList) {
                addNodeToCircle(address);
            }
        }

        @Override
        public InetSocketAddress getNext() {
            BrpcRequest brpcRequest = BrpcBootstrap.REQUEST_THREAD_LOCAL.get();
            String requestId = Long.toString(brpcRequest.getRequestId());
            int hash = hash(requestId);

            if (!circle.containsKey(hash)){
                // 寻找理我最近的一个节点
                SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }
            return circle.get(hash);
        }

        /**
         * 将每个节点挂载到hash环上
         * @param inetSocketAddress 节点的地址
         */
        private void addNodeToCircle(InetSocketAddress inetSocketAddress) {
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                // 关在到hash环上
                circle.put(hash,inetSocketAddress);
                if(log.isDebugEnabled()){
                    log.debug("The node with hash【{}】 has already been mounted on the hash ring",hash);
                }
            }
        }

        private void removeNodeFromCircle(InetSocketAddress inetSocketAddress) {
            // 为每一个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                // 关在到hash环上
                circle.remove(hash);
            }
        }

        /**
         * 具体的hash算法
         * @param s 要hash的字符串
         * @return hash值
         */
        private int hash(String s) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            byte[] digest = md.digest(s.getBytes());

            int res = 0;
            for (int i = 0; i < 4; i++) {
                res = res << 8;
                if( digest[i] < 0 ){
                    res = res | (digest[i] & 255);
                } else {
                    res = res | digest[i];
                }
            }
            return res;
        }

        private String toBinary(int i){
            String s = Integer.toBinaryString(i);
            int index = 32 -s.length();
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < index; j++) {
                sb.append(0);
            }
            sb.append(s);
            return sb.toString();
        }
    }
}
