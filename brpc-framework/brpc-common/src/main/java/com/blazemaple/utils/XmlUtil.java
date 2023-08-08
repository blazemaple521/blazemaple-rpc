package com.blazemaple.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/8/5 23:08
 */
public class XmlUtil {

    public static boolean isNodeExist(Document doc, XPath xpath, String expression) {
        XPathExpression expr;
        Node targetNode;
        try {
            expr = xpath.compile(expression);
            targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        return targetNode != null;
    }

}
