package com.blazemaple.watcher;

import com.blazemaple.BrpcBootstrap;
import com.blazemaple.NettyBootstrapInitializer;
import com.blazemaple.discovery.Registry;
import com.blazemaple.loadbalancer.LoadBalancer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/8/2 15:25
 */
@Slf4j
public class UpAndDownWatcher implements Watcher {

    @Override
    public void process(WatchedEvent event) {
        // 当前的阶段是否发生了变化
        if (event.getType() == Event.EventType.NodeChildrenChanged){
            if (log.isDebugEnabled()){
                log.debug("Detected nodes offline/online under service【{}】, will pull the service list again",event.getPath());
            }
            String serviceName = getServiceName(event.getPath());
            Registry registry = BrpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
            String group = BrpcBootstrap.getInstance().getConfiguration().getGroup();
            List<InetSocketAddress> addresses = registry.lookup(serviceName,group);
            // 处理新增的节点
            for (InetSocketAddress address : addresses) {
                if(!BrpcBootstrap.CHANNEL_CACHE.containsKey(address)){
                    // 根据地址建立连接，并且缓存
                    Channel channel;
                    try {
                        channel = NettyBootstrapInitializer.getBootstrap()
                            .connect(address).sync().channel();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    BrpcBootstrap.CHANNEL_CACHE.put(address,channel);
                }
            }

            // 处理下线的节点
            for (Map.Entry<InetSocketAddress,Channel> entry: BrpcBootstrap.CHANNEL_CACHE.entrySet()){
                if(!addresses.contains(entry.getKey())){
                    BrpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }

            // 重新负载均衡器
            LoadBalancer loadBalancer = BrpcBootstrap.getInstance().getConfiguration().getLoadBalancer();
            if (log.isDebugEnabled()){
                log.debug("start reLoadBalance");
            }
            loadBalancer.reLoadBalance(serviceName,addresses);
        }
    }
    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}
