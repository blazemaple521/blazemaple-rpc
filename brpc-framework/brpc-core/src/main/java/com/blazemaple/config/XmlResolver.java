package com.blazemaple.config;

import com.blazemaple.compress.Compressor;
import com.blazemaple.compress.CompressorFactory;
import com.blazemaple.discovery.RegistryConfig;
import com.blazemaple.loadbalancer.LoadBalancer;
import com.blazemaple.loadbalancer.impl.RoundRobinLoadBalancer;
import com.blazemaple.serialize.Serializer;
import com.blazemaple.serialize.SerializerFactory;
import com.blazemaple.utils.IdGenerator;
import com.blazemaple.utils.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/8/3 20:38
 */
@Slf4j
public class XmlResolver {

    /**
     * 从配置文件读取配置信息
     *
     * @param configuration 配置信息
     */
    public void loadFromXml(Configuration configuration) {
        try {
            //创建一个Document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //关闭dtd校验
            factory.setValidating(false);
            // 禁用外部实体解析
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("brpc.xml");
            Document doc = builder.parse(inputStream);

            //获取一个xpath解析类
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            //解析所有标签
            configuration.setPort(resolvePort(doc, xpath));
            configuration.setApplicationName(resolveAppName(doc, xpath));
            configuration.setGroup(resolveGroup(doc, xpath));

            configuration.setIdGenerator(resolveIdGenerator(doc, xpath));

            configuration.setRegistryConfig(resolveRegistryConfig(doc, xpath));

            // 处理使用的压缩方式和序列化方式
            configuration.setCompressType(resolveCompressType(doc, xpath));
            configuration.setSerializeType(resolveSerializeType(doc, xpath));

            // 配置新的压缩方式和序列化方式，并将其纳入工厂中
            ObjectWrapper<Compressor> compressorObjectWrapper = resolveCompressor(doc, xpath);
            if (compressorObjectWrapper!=null){
                CompressorFactory.addCompressor(compressorObjectWrapper);
            }

            ObjectWrapper<Serializer> serializerObjectWrapper = resolveSerializer(doc, xpath);
            if (serializerObjectWrapper!=null){
                SerializerFactory.addSerializer(serializerObjectWrapper);
            }

            configuration.setLoadBalancer(resolveLoadBalancer(doc, xpath));
            System.out.println("111");

        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.info("If no configuration file is found or an exception occurs when parsing the configuration file, " +
                "the default configuration is used.", e);
        }
    }

    /**
     * 解析端口号
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 端口号
     */
    private int resolvePort(Document doc, XPath xpath) {
        String expression = "/configuration/port";
        if (XmlUtil.isNodeExist(doc,xpath,expression)){
            String portString = parseString(doc, xpath, expression);
            return portString == null ? 8091 : Integer.parseInt(portString);
        }
        return 8091;

    }

    /**
     * 解析应用名称
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 应用名
     */
    private String resolveAppName(Document doc, XPath xpath) {
        String expression = "/configuration/applicationName";
        if (XmlUtil.isNodeExist(doc,xpath,expression)){
            return parseString(doc, xpath, expression);
        }
        return "default";
    }


    /**
     * 解析分组名称
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 应用名
     */
    private String resolveGroup(Document doc, XPath xpath) {
        String expression = "/configuration/group";
        if (XmlUtil.isNodeExist(doc,xpath,expression)){
            return parseString(doc, xpath, expression);
        }
        return "default";
    }



    /**
     * 解析负载均衡器
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 负载均衡器实例
     */
    private LoadBalancer resolveLoadBalancer(Document doc, XPath xpath) {
        String expression = "/configuration/loadBalancer";
        if (XmlUtil.isNodeExist(doc,xpath,expression)){
            return parseObject(doc, xpath, expression, null);
        }
        return new RoundRobinLoadBalancer();
    }

    /**
     * 解析id发号器
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return id发号器实例
     */
    private IdGenerator resolveIdGenerator(Document doc, XPath xpath) {
        String expression = "/configuration/idGenerator";
        if (XmlUtil.isNodeExist(doc,xpath,expression)){
            String aClass = parseString(doc, xpath, expression, "class");
            String dataCenterId = parseString(doc, xpath, expression, "dataCenterId");
            String machineId = parseString(doc, xpath, expression, "MachineId");

            try {
                Class<?> clazz = Class.forName(aClass);
                Object instance = clazz.getConstructor(new Class[]{long.class, long.class})
                    .newInstance(Long.parseLong(dataCenterId), Long.parseLong(machineId));
                return (IdGenerator) instance;
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return new IdGenerator(1, 2);

    }

    /**
     * 解析注册中心
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return RegistryConfig
     */
    private RegistryConfig resolveRegistryConfig(Document doc, XPath xpath) {
        String expression = "/configuration/registry";
        if (XmlUtil.isNodeExist(doc,xpath,expression)){
            String url = parseString(doc, xpath, expression, "url");
            return new RegistryConfig(url);
        }
        return new RegistryConfig("zookeeper://127.0.0.1:2181");
    }


    /**
     * 解析压缩的具体实现
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return ObjectWrapper<Compressor>
     */
    private ObjectWrapper<Compressor> resolveCompressor(Document doc, XPath xpath) {
        String expression = "/configuration/compressor";
        if (XmlUtil.isNodeExist(doc,xpath,expression)){
            Compressor compressor = parseObject(doc, xpath, expression, null);
            Byte code = Byte.valueOf(Objects.requireNonNull(parseString(doc, xpath, expression, "code")));
            String name = parseString(doc, xpath, expression, "name");
            return new ObjectWrapper<>(code,name,compressor);
        }
        return null;
    }

    /**
     * 解析压缩的算法名称
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 压缩算法名称
     */
    private String resolveCompressType(Document doc, XPath xpath) {
        String expression = "/configuration/compressType";
        if (XmlUtil.isNodeExist(doc,xpath,expression)){
            return parseString(doc, xpath, expression, "type");
        }
        return "gzip";
    }

    /**
     * 解析序列化的方式
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 序列化的方式
     */
    private String resolveSerializeType(Document doc, XPath xpath) {
        String expression = "/configuration/serializeType";
        if (XmlUtil.isNodeExist(doc,xpath,expression)){
            return parseString(doc, xpath, expression, "type");
        }
        return "jdk";
    }

    /**
     * 解析序列化器
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 序列化器
     */
    private ObjectWrapper<Serializer> resolveSerializer(Document doc, XPath xpath) {
        String expression = "/configuration/serializer";
        if (XmlUtil.isNodeExist(doc,xpath,expression)){
            Serializer serializer = parseObject(doc, xpath, expression, null);
            Byte code = Byte.valueOf(Objects.requireNonNull(parseString(doc, xpath, expression, "code")));
            String name = parseString(doc, xpath, expression, "name");
            return new ObjectWrapper<>(code,name,serializer);
        }
        return null;
    }


    /**
     * 获得一个节点文本值
     * @param doc        文档对象
     * @param xpath      xpath解析器
     * @param expression xpath表达式
     * @return 节点的值
     */
    private String parseString(Document doc, XPath xpath, String expression) {
        try {
            XPathExpression expr = xpath.compile(expression);
            // 我们的表达式可以帮我们获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getTextContent();
        } catch (XPathExpressionException e) {
            log.error("An exception occurred while parsing the expression.", e);
        }
        return null;
    }



    /**
     * 获得一个节点属性的值
     *
     * @param doc           文档对象
     * @param xpath         xpath解析器
     * @param expression    xpath表达式
     * @param attributeName 节点名称
     * @return 节点的值
     */
    private String parseString(Document doc, XPath xpath, String expression, String attributeName) {
        try {
            XPathExpression expr = xpath.compile(expression);
            // 我们的表达式可以帮我们获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getAttributes().getNamedItem(attributeName).getNodeValue();
        } catch (XPathExpressionException e) {
            log.error("An exception occurred while parsing the expression.", e);
        }
        return null;
    }



    /**
     * 解析一个节点，返回一个实例
     *
     * @param xpath xpath解析器
     * @param doc 文档对象
     * @param expression xpath表达式
     * @param paramType 参数类型列表
     * @param param 参数
     * @return 配置的实例
     * @param <T> 泛型
     */

    private <T> T parseObject(Document doc,XPath xpath,  String expression,Class<?>[] paramType, Object... param){
        try {
            XPathExpression expr = xpath.compile(expression);
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            String classname = targetNode.getAttributes().getNamedItem("class").getNodeValue();
            Class<?> clazz = Class.forName(classname);
            Object instant;
            if (paramType==null){
                instant=clazz.getConstructor().newInstance();
            }else {
                instant=clazz.getConstructor(paramType).newInstance(param);
            }
            return (T)instant;
        } catch (XPathExpressionException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            log.error("An exception occurred while parsing the expression.", e);
        }

        return null;
    }

}
