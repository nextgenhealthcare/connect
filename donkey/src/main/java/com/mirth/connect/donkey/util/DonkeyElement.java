package com.mirth.connect.donkey.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.InputSource;

/**
 * Represents a DOM document element. Implements additional convenience methods beyond what is defined by the org.w3c.dom.Element interface.
 */
public class DonkeyElement implements Element {
    private org.w3c.dom.Element element;

    public DonkeyElement(org.w3c.dom.Element element) {
        this.element = element;
    }
    
    public DonkeyElement(String xml) throws DonkeyElementException {
        this.element = elementFromXml(xml);
    }

    public DonkeyElement getChildElement(String name) {
        NodeList children = element.getChildNodes();
        int childCount = children.getLength();

        for (int i = 0; i < childCount; i++) {
            if (children.item(i).getNodeName().equals(name) && children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return new DonkeyElement((org.w3c.dom.Element) children.item(i));
            }
        }

        return null;
    }

    public List<DonkeyElement> getChildElements() {
        NodeList childNodes = element.getChildNodes();
        int childCount = childNodes.getLength();
        List<DonkeyElement> childElements = new ArrayList<DonkeyElement>();

        for (int i = 0; i < childCount; i++) {
            Node node = childNodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                childElements.add(new DonkeyElement((org.w3c.dom.Element) node));
            }
        }

        return childElements;
    }

    public DonkeyElement addChildElement(String name) {
        org.w3c.dom.Element child = element.getOwnerDocument().createElement(name);
        element.appendChild(child);
        return new DonkeyElement(child);
    }
    
    public DonkeyElement addChildElement(String name, String content) {
        org.w3c.dom.Element child = element.getOwnerDocument().createElement(name);
        element.appendChild(child);
        child.setTextContent(content);
        return new DonkeyElement(child);
    }
    
    public DonkeyElement addChildElementFromXml(String xml) throws Exception {
        return new DonkeyElement((Element) element.appendChild(element.getOwnerDocument().importNode(elementFromXml(xml), true)));
    }

    public DonkeyElement removeChild(String name) {
        NodeList children = element.getChildNodes();
        int childCount = children.getLength();

        for (int i = 0; i < childCount; i++) {
            if (children.item(i).getNodeName().equals(name) && children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return new DonkeyElement((org.w3c.dom.Element) element.removeChild(children.item(i)));
            }
        }

        return null;
    }

    public void removeChildren() {
        NodeList children = element.getChildNodes();
        int childCount = children.getLength();

        for (int i = childCount - 1; i >= 0; i--) {
            element.removeChild(children.item(i));
        }
    }

    public boolean removeChildren(String name) {
        NodeList children = element.getChildNodes();
        int childCount = children.getLength();
        boolean removed = false;

        for (int i = childCount - 1; i >= 0; i--) {
            if (children.item(i).getNodeName().equals(name)) {
                element.removeChild(children.item(i));
                removed = true;
            }
        }

        return removed;
    }

    public void setNodeName(String name) {
        element.getOwnerDocument().renameNode(element, element.getNamespaceURI(), name);
    }

    @Override
    public String getNodeName() {
        return element.getNodeName();
    }

    @Override
    public String getNodeValue() throws DOMException {
        return element.getNodeValue();
    }

    @Override
    public void setNodeValue(String nodeValue) throws DOMException {
        element.setNodeValue(nodeValue);
    }

    @Override
    public short getNodeType() {
        return element.getNodeType();
    }

    @Override
    public Node getParentNode() {
        return element.getParentNode();
    }

    @Override
    public NodeList getChildNodes() {
        return element.getChildNodes();
    }

    @Override
    public Node getFirstChild() {
        return element.getFirstChild();
    }

    @Override
    public Node getLastChild() {
        return element.getLastChild();
    }

    @Override
    public Node getPreviousSibling() {
        return element.getPreviousSibling();
    }

    @Override
    public Node getNextSibling() {
        return element.getNextSibling();
    }

    @Override
    public NamedNodeMap getAttributes() {
        return element.getAttributes();
    }

    @Override
    public Document getOwnerDocument() {
        return element.getOwnerDocument();
    }

    @Override
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return element.insertBefore(newChild, refChild);
    }

    @Override
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        return element.replaceChild(newChild, oldChild);
    }

    @Override
    public Node removeChild(Node oldChild) throws DOMException {
        return element.removeChild(oldChild);
    }

    @Override
    public Node appendChild(Node newChild) throws DOMException {
        return element.appendChild(newChild);
    }

    @Override
    public boolean hasChildNodes() {
        return element.hasChildNodes();
    }

    @Override
    public Node cloneNode(boolean deep) {
        return element.cloneNode(deep);
    }

    @Override
    public void normalize() {
        element.normalize();
    }

    @Override
    public boolean isSupported(String feature, String version) {
        return element.isSupported(feature, version);
    }

    @Override
    public String getNamespaceURI() {
        return element.getNamespaceURI();
    }

    @Override
    public String getPrefix() {
        return element.getPrefix();
    }

    @Override
    public void setPrefix(String prefix) throws DOMException {
        element.setPrefix(prefix);
    }

    @Override
    public String getLocalName() {
        return element.getLocalName();
    }

    @Override
    public boolean hasAttributes() {
        return element.hasAttributes();
    }

    @Override
    public String getBaseURI() {
        return element.getBaseURI();
    }

    @Override
    public short compareDocumentPosition(Node other) throws DOMException {
        return element.compareDocumentPosition(other);
    }

    @Override
    public String getTextContent() throws DOMException {
        return element.getTextContent();
    }

    @Override
    public void setTextContent(String textContent) throws DOMException {
        element.setTextContent(textContent);
    }

    @Override
    public boolean isSameNode(Node other) {
        return element.isSameNode(other);
    }

    @Override
    public String lookupPrefix(String namespaceURI) {
        return element.lookupPrefix(namespaceURI);
    }

    @Override
    public boolean isDefaultNamespace(String namespaceURI) {
        return element.isDefaultNamespace(namespaceURI);
    }

    @Override
    public String lookupNamespaceURI(String prefix) {
        return element.lookupNamespaceURI(prefix);
    }

    @Override
    public boolean isEqualNode(Node arg) {
        return element.isEqualNode(arg);
    }

    @Override
    public Object getFeature(String feature, String version) {
        return element.getFeature(feature, version);
    }

    @Override
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        return element.setUserData(key, data, handler);
    }

    @Override
    public Object getUserData(String key) {
        return element.getUserData(key);
    }

    @Override
    public String getTagName() {
        return element.getTagName();
    }

    @Override
    public String getAttribute(String name) {
        return element.getAttribute(name);
    }

    @Override
    public void setAttribute(String name, String value) throws DOMException {
        element.setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) throws DOMException {
        element.removeAttribute(name);
    }

    @Override
    public Attr getAttributeNode(String name) {
        return element.getAttributeNode(name);
    }

    @Override
    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        return element.setAttributeNode(newAttr);
    }

    @Override
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        return element.removeAttributeNode(oldAttr);
    }

    @Override
    public NodeList getElementsByTagName(String name) {
        return element.getElementsByTagName(name);
    }

    @Override
    public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
        return element.getAttributeNS(namespaceURI, localName);
    }

    @Override
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
        element.setAttributeNS(namespaceURI, qualifiedName, value);
    }

    @Override
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        element.removeAttributeNS(namespaceURI, localName);
    }

    @Override
    public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException {
        return element.getAttributeNodeNS(namespaceURI, localName);
    }

    @Override
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        return element.setAttributeNodeNS(newAttr);
    }

    @Override
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException {
        return element.getElementsByTagNameNS(namespaceURI, localName);
    }

    @Override
    public boolean hasAttribute(String name) {
        return element.hasAttribute(name);
    }

    @Override
    public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
        return element.hasAttributeNS(namespaceURI, localName);
    }

    @Override
    public TypeInfo getSchemaTypeInfo() {
        return element.getSchemaTypeInfo();
    }

    @Override
    public void setIdAttribute(String name, boolean isId) throws DOMException {
        element.setIdAttribute(name, isId);
    }

    @Override
    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
        element.setIdAttributeNS(namespaceURI, localName, isId);
    }

    @Override
    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        element.setIdAttributeNode(idAttr, isId);
    }
    
    public String toXml() throws Exception {
        return elementToXml(element);
    }
    
    public static String elementToXml(Element element) throws DonkeyElementException {
        Writer writer = new StringWriter();

        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.transform(new DOMSource(element), new StreamResult(writer));
        } catch (TransformerException e) {
            throw new DonkeyElementException(e);
        }

        return writer.toString();
    }

    public static Element elementFromXml(String xml) throws DonkeyElementException {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml))).getDocumentElement();
        } catch (Exception e) {
            throw new DonkeyElementException(e);
        }
    }
    
    public static class DonkeyElementException extends Exception {
        public DonkeyElementException(Throwable cause) {
            super(cause);
        }
    }
}
