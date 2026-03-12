/**
 * Copyright Sp42 frank@ajaxjs.com Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.ajaxjs.util;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.*;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * XML Processing Utility Class - Provides methods for parsing, manipulating, and querying XML documents.
 * <a href="https://blog.csdn.net/axman/article/details/420910">...</a>
 *
 * <p>This class simplifies common XML operations including XPath queries, DOM parsing,
 * node text extraction, attribute retrieval, and conversion between XML formats.
 * It provides a clean API for working with XML in Java applications.
 *
 * @author sp42 frank@ajaxjs.com
 */
@Slf4j
public class XmlHelper {
    /**
     * Initializes and returns a DocumentBuilder for XML parsing operations.
     *
     * @return A DocumentBuilder instance for XML transformation and parsing
     */
    public static DocumentBuilder initBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.warn("Parser Configuration Exception", e);
            throw new RuntimeException("Parser Configuration Exception", e);
        }
    }

    /**
     * Retrieves a specific node using XPath query.
     *
     * @param xml   The XML file path
     * @param xpath The XPath expression to locate nodes
     * @param fn    The consumer function to process each matched Node
     */
    public static void xPath(String xml, String xpath, Consumer<Node> fn) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        try {
            XPathExpression expr = XPathFactory.newInstance().newXPath().compile(xpath);
            NodeList nodes = (NodeList) expr.evaluate(factory.newDocumentBuilder().parse(xml), XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++)
                fn.accept(nodes.item(i));
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            log.warn("Get a node from XML err. XPath: {}", xpath, e);
            throw new RuntimeException("Get a node from XML err. XPath: " + xpath, e);
        }
    }

    /**
     * Parses XML content and processes nodes with a consumer function.
     *
     * @param xml The XML content to parse
     * @param fn  The bi-consumer function to process each Node and its NodeList of children
     */
    public static void parseXML(String xml, BiConsumer<Node, NodeList> fn) {
        try (InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            Element el = Objects.requireNonNull(initBuilder()).parse(in).getDocumentElement();
            NodeList nodeList = el.getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                fn.accept(node, nodeList);
            }
        } catch (SAXException | IOException e) {
            log.warn("Parsed this XML err. {}", xml, e);
            throw new RuntimeException("Parsed this XML err." + xml, e);
        }
    }

    /**
     * Gets the root element from the given XML string.
     *
     * @param xml The XML string content
     * @return The root Element of the XML document
     */
    public static Element getRoot(String xml) {
        try (InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            return Objects.requireNonNull(initBuilder()).parse(in).getDocumentElement();
        } catch (SAXException | IOException e) {
            log.warn("Get the root of this XML err. {}", xml, e);
            throw new RuntimeException("Get the root of this XML err." + xml, e);
        }
    }

    /**
     * Converts all attributes of a node to a map.
     *
     * @param xml   The XML file path
     * @param xpath The XPath expression to locate the node
     * @return A map containing attribute names as keys and attribute values as values
     */
    public static Map<String, String> nodeAsMap(String xml, String xpath) {
        Map<String, String> map = new HashMap<>();

        XmlHelper.xPath(xml, xpath, node -> {
            NamedNodeMap _map = node.getAttributes();

            if (_map != null) {
                for (int i = 0, n = _map.getLength(); i < n; i++) {
                    Node _node = _map.item(i);
                    map.put(_node.getNodeName(), _node.getNodeValue());
                }
            }
        });

        if (map.isEmpty())
            return null;

        return map;
    }

    /**
     * Gets the text content within a node, including nested tags.
     *
     * @param node The node object to extract text from
     * @return The inner text content
     */
    public static String getNodeText(Node node) {
        DOMImplementationLS lsImpl = (DOMImplementationLS) node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
        LSSerializer lsSerializer = lsImpl.createLSSerializer();
        DOMConfiguration domConfig = lsSerializer.getDomConfig();
        domConfig.setParameter("xml-declaration", false);
        NodeList childNodes = node.getChildNodes();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < childNodes.getLength(); i++)
            sb.append(lsSerializer.writeToString(childNodes.item(i)));

        return sb.toString();
    }

    /**
     * Gets a specific attribute value from a node.
     *
     * @param node     The node object
     * @param attrName The name of the attribute to retrieve
     * @return The attribute value, or null if empty
     */
    public static String getNodeAttribute(Node node, String attrName) {
        NamedNodeMap attrs = node.getAttributes();

        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);

            if (attr.getNodeName().equals(attrName))
                return attr.getValue();
        }

        return null;
    }

    /**
     * Gets a specific attribute value from a node.
     *
     * @param el       The node object
     * @param attrName The name of the attribute to retrieve
     * @return The attribute value
     */
    public static String getAttribute(Node el, String attrName) {
        NamedNodeMap attributes = el.getAttributes();

        if (attributes != null && attributes.getLength() > 0) {
            Node namedItem = attributes.getNamedItem(attrName);

            if (namedItem != null)
                return namedItem.getNodeValue();
            else {
                log.warn("The attribute: {} is not found", attrName);
                return null;
            }

        } else
            return null;
    }
}