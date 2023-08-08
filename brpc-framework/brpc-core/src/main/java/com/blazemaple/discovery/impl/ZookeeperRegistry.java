package com.blazemaple.discovery.impl;

import com.blazemaple.BrpcBootstrap;
import com.blazemaple.ServiceConfig;
import com.blazemaple.constant.BrpcConstant;
import com.blazemaple.discovery.AbstractRegistry;
import com.blazemaple.exceptions.DiscoveryException;
import com.blazemaple.utils.NetUtils;
import com.blazemaple.utils.zookeeper.ZookeeperNode;
import com.blazemaple.utils.zookeeper.ZookeeperUtils;
import com.blazemaple.watcher.UpAndDownWatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/21 2:10
 */
@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {

    private ZooKeeper zooKeeper;

    public ZookeeperRegistry() {
        this.zooKeeper = ZookeeperUtils.createZookeeper();
    }

    public ZookeeperRegistry(String connectStr, int timeout) {
        this.zooKeeper = ZookeeperUtils.createZookeeper(connectStr, timeout);
    }

    @Override
    public void register(ServiceConfig<?> service) {
        String parentNode = BrpcConstant.BASE_PROVIDERS_PATH + "/" + service.getInterface().getName();
        if (!ZookeeperUtils.exists(zooKeeper, parentNode, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.PERSISTENT);
        }
        // 建立分组节点
        parentNode = parentNode + "/" + service.getGroup();
        if(!ZookeeperUtils.exists(zooKeeper,parentNode,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode,null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.PERSISTENT);
        }
        //服务地址结点，临时结点
        String childNode = parentNode + "/" + NetUtils.getIp() + ":" + BrpcBootstrap.getInstance().getConfiguration().getPort();
        if (!ZookeeperUtils.exists(zooKeeper, childNode, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(childNode, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.EPHEMERAL);
        }
        if (log.isDebugEnabled()) {
            log.debug("publish service:{}", service.getInterface().getName());
        }
    }

    @Override
    public List<InetSocketAddress>  lookup(String serviceName, String group) {
        String serviceNode = BrpcConstant.BASE_PROVIDERS_PATH + "/" + serviceName+ "/"+ group;
        List<String> children = ZookeeperUtils.getChildren(zooKeeper, serviceNode, new UpAndDownWatcher());
        List<InetSocketAddress> inetSocketAddress = children.stream().map(ipString -> {
            String[] ipAndPort = ipString.split(":");
            String ip = ipAndPort[0];
            int port = Integer.parseInt(ipAndPort[1]);
            return new InetSocketAddress(ip, port);
        }).toList();
        if (inetSocketAddress.size()==0){
            throw new DiscoveryException("no service provider");
        }
        return inetSocketAddress;
    }
}
