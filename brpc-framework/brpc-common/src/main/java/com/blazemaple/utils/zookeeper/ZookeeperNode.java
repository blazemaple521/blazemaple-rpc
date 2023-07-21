package com.blazemaple.utils.zookeeper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * @description zookeeper节点
 *
 * @author BlazeMaple
 * @date 2023/7/21 1:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZookeeperNode {
    private String nodePath;
    private byte[] data;
}