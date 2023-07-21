package com.blazemaple.utils;

import com.blazemaple.exceptions.NetworkException;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author BlazeMaple
 * @description 网络工具类
 * @date 2023/7/21 1:15
 */
@Slf4j
public class NetUtils {

    public static String getIp() {
        try {
            // 获取所有的网卡信息
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iFace = interfaces.nextElement();
                // 过滤非回环接口和虚拟接口
                if (iFace.isLoopback() || iFace.isVirtual() || !iFace.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iFace.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // 过滤IPv6地址和回环地址
                    if (addr instanceof Inet6Address || addr.isLoopbackAddress()) {
                        continue;
                    }
                    String ipAddress = addr.getHostAddress();
                    if(log.isDebugEnabled()){
                        log.debug("局域网IP地址：{}",ipAddress);
                    }
                    return ipAddress;
                }
            }
            throw new NetworkException();
        } catch (SocketException e) {
            log.error("获取局域网ip时放生异常。", e);
            throw new NetworkException(e);
        }
    }

    public static void main(String[] args) {
        String ip = NetUtils.getIp();
        System.out.println("ip = " + ip);
    }

}
