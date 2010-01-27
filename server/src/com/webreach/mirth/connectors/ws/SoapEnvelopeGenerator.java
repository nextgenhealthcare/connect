package com.webreach.mirth.connectors.ws;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.wsdl.Definition;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.parser.XSOMParser;

public class SoapEnvelopeGenerator {
    private static final String SOAPENV_NS = "soapenv";
    private XSSchema schema = null;
    private boolean useComments = true;
    private XSOMParser parser = new XSOMParser();

    public SoapEnvelopeGenerator(Definition definition) throws Exception {
        ExtensibilityElement extensibilityElement = findExtensibilityElementByName(definition.getTypes().getExtensibilityElements(), "schema");

        if ((extensibilityElement != null) && (extensibilityElement instanceof Schema)) {
            Schema schemaExtensibilityElement = (Schema) extensibilityElement;
            SchemaImport schemaImport = ((Vector<SchemaImport>) schemaExtensibilityElement.getImports().values().iterator().next()).get(0);
            Element schemaElement = schemaImport.getReferencedSchema().getElement();
            parser.parse(new InputSource(new StringReader(domToString(schemaElement))));
            schema = parser.getResult().getSchema(1);
        }
    }

    public SoapEnvelopeGenerator(String source) throws Exception {
        parser.parse(new InputSource(new StringReader(source)));
        schema = parser.getResult().getSchema(1);
    }

    public boolean isUseComments() {
        return useComments;
    }

    public void setUseComments(boolean useComments) {
        this.useComments = useComments;
    }

    private ExtensibilityElement findExtensibilityElementByName(List<ExtensibilityElement> extensbilityElements, String name) {
        for (Iterator<ExtensibilityElement> iterator = extensbilityElements.iterator(); iterator.hasNext();) {
            ExtensibilityElement extensibilityElement = iterator.next();
            if (extensibilityElement.getElementType().getLocalPart().equals(name)) {
                return extensibilityElement;
            }
        }

        return null;
    }

    public String generateEnvelopeForOperation(String operation) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element envelopeElement = document.createElement(SOAPENV_NS + ":Envelope");
        envelopeElement.setAttribute("xmlns:" + SOAPENV_NS, "http://schemas.xmlsoap.org/soap/envelope/");
        envelopeElement.setAttribute("xmlns:ns", schema.getTargetNamespace());
        Element headerElement = document.createElement(SOAPENV_NS + ":Header");
        envelopeElement.appendChild(headerElement);
        Element bodyElement = document.createElement(SOAPENV_NS + ":Body");
        Element operationElement = document.createElement("ns:" + operation);

        XSContentType contentType = schema.getComplexType(operation).getContentType();
        generateRequestElement(document, operationElement, contentType);

        bodyElement.appendChild(operationElement);
        envelopeElement.appendChild(bodyElement);
        document.appendChild(envelopeElement);
        return domToString(document);
    }

    private void generateRequestElement(Document document, Element parentElement, XSContentType contentType) {
        XSTerm term = contentType.asParticle().getTerm();

        if (term.isModelGroup()) {
            XSModelGroup modelGroup = term.asModelGroup();

            for (XSParticle particle : modelGroup.getChildren()) {
                if (isUseComments()) {
                    Comment comment;

                    if (particle.getMinOccurs() == 0) {
                        comment = document.createComment("Optional");
                    } else {
                        comment = document.createComment("Required");
                    }

                    parentElement.appendChild(comment);
                }

                XSTerm particleTerm = particle.getTerm();

                if (particleTerm.isElementDecl()) {
                    // xs:element inside complex type
                    XSElementDecl elementDecl = particleTerm.asElementDecl();
                    Element requestElement;

                    if (elementDecl.getType().isComplexType()) {
                        XSComplexType complexType = schema.getComplexType(elementDecl.getType().getName());

                        if (elementDecl.getName().startsWith("arg")) {
                            requestElement = document.createElement(elementDecl.getName());
                            parentElement.appendChild(requestElement);
                        } else {
                            requestElement = document.createElement(complexType.getName());
                            parentElement.appendChild(requestElement);
                        }

                        generateRequestElement(document, requestElement, complexType.getContentType());
                    } else {
                        requestElement = document.createElement(elementDecl.getName());
                        requestElement.setTextContent("? (" + elementDecl.getType().getName() + ")");
                        parentElement.appendChild(requestElement);
                    }
                }
            }
        }
    }

    private String domToString(Node node) throws Exception {
        Writer writer = new StringWriter();
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute("indent-number", new Integer(2));
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }
}
