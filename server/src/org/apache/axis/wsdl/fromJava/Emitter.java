/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis.wsdl.fromJava;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPFault;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.InternalException;
import org.apache.axis.Version;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.description.FaultDesc;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.encoding.TypeMappingRegistry;
import org.apache.axis.encoding.TypeMappingRegistryImpl;
import org.apache.axis.utils.ClassUtils;
import org.apache.axis.utils.JavaUtils;
import org.apache.axis.utils.Messages;
import org.apache.axis.utils.XMLUtils;
import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.commons.logging.Log;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;
import com.ibm.wsdl.extensions.soap.SOAPBodyImpl;
import com.ibm.wsdl.extensions.soap.SOAPHeaderImpl;
import com.ibm.wsdl.extensions.soap.SOAPOperationImpl;

/**
 * This class emits WSDL from Java classes.  It is used by the ?WSDL
 * Axis browser function and Java2WSDL commandline utility.
 * See Java2WSDL and Java2WSDLFactory for more information.
 * 
 * @author Glen Daniels (gdaniels@apache.org)
 * @author Rich Scheuerle (scheu@us.ibm.com)
 */
public class Emitter {

    /** Field log */
    protected static Log log = LogFactory.getLog(Emitter.class.getName());
    // Generated WSDL Modes

    /** Field MODE_ALL */
    public static final int MODE_ALL = 0;

    /** Field MODE_INTERFACE */
    public static final int MODE_INTERFACE = 1;

    /** Field MODE_IMPLEMENTATION */
    public static final int MODE_IMPLEMENTATION = 2;

    /** Field cls */
    private Class cls;

    /** Field extraClasses */
    private Class[] extraClasses;               // Extra classes to emit WSDL for

    /** Field implCls */
    private Class implCls;                      // Optional implementation class

    /** Field allowedMethods */
    private Vector allowedMethods = null;       // Names of methods to consider

    /** Field disallowedMethods */
    private Vector disallowedMethods = null;    // Names of methods to exclude

    /** Field stopClasses */
    private ArrayList stopClasses =
            new ArrayList();                        // class names which halt inheritace searches

    /** Field useInheritedMethods */
    private boolean useInheritedMethods = false;

    /** Field intfNS */
    private String intfNS;

    /** Field implNS */
    private String implNS;

    /** Field inputSchema */
    private String inputSchema;

    /** Field inputWSDL */
    private String inputWSDL;

    /** Field locationUrl */
    private String locationUrl;

    /** Field importUrl */
    private String importUrl;

    /** Field servicePortName */
    private String servicePortName;

    /** Field serviceElementName */
    private String serviceElementName;

    /** Field targetService */
    private String targetService = null;

    /** Field description */
    private String description;

    /** Field style */
    private Style style = Style.RPC;

    /** Field use */
    private Use use = null;                     // Default depends on style setting

    /** Field tm */
    private TypeMapping tm = null;              // Registered type mapping

    /** Field tmr */
    private TypeMappingRegistry tmr = new TypeMappingRegistryImpl();

    /** Field namespaces */
    private Namespaces namespaces;

    /** Field exceptionMsg */
    private Map exceptionMsg = null;

    /** Global element names already in use */
    private Map usedElementNames;

    /** Field encodingList */
    private ArrayList encodingList;

    /** Field types */
    protected Types types;

    /** Field clsName */
    private String clsName;

    /** Field portTypeName */
    private String portTypeName;

    /** Field bindingName */
    private String bindingName;

    /** Field serviceDesc */
    private ServiceDesc serviceDesc;

    /** Field serviceDesc2 */
    private JavaServiceDesc serviceDesc2;

    /** Field soapAction */
    private String soapAction = "DEFAULT";
    
    /** Should we emit all mapped types in every WSDL? */
    private boolean emitAllTypes = false;

    /** Version string to put at top of WSDL */
    private String versionMessage = null;
    
    /** The mapping of generated type qname to its corresponding java type. For use with java<-->wsdl roundtripping */
    private HashMap qName2ClassMap;

    // Style Modes

    /** DEPRECATED - Indicates style=rpc use=encoded */
    public static final int MODE_RPC = 0;

    /** DEPRECATED - Indicates style=document use=literal */
    public static final int MODE_DOCUMENT = 1;

    /** DEPRECATED - Indicates style=wrapped use=literal */
    public static final int MODE_DOC_WRAPPED = 2;

    /**
     * Construct Emitter.
     * Set the contextual information using set* methods
     * Invoke emit to emit the code
     */
    public Emitter() {

        createDocumentFragment();

        namespaces = new Namespaces();
        exceptionMsg = new HashMap();
        usedElementNames = new HashMap();
        qName2ClassMap = new HashMap();
    }

    /**
     * Generates WSDL documents for a given <code>Class</code>
     * 
     * @param filename1 interface WSDL
     * @param filename2 implementation WSDL
     * @throws IOException                  
     * @throws WSDLException                
     * @throws SAXException                 
     * @throws ParserConfigurationException 
     */
    public void emit(String filename1, String filename2)
            throws IOException, WSDLException, SAXException,
            ParserConfigurationException {

        // Get interface and implementation defs
        Definition intf = getIntfWSDL();
        Definition impl = getImplWSDL();

        // Supply reasonable file names if not supplied
        if (filename1 == null) {
            filename1 = getServicePortName() + "_interface.wsdl";
        }

        if (filename2 == null) {
            filename2 = getServicePortName() + "_implementation.wsdl";
        }

        for (int i = 0; (extraClasses != null) && (i < extraClasses.length);
             i++) {
            types.writeTypeForPart(extraClasses[i], null);
        }

        // types.updateNamespaces();
        // Write out the interface def
        Document doc =
                WSDLFactory.newInstance().newWSDLWriter().getDocument(intf);

        types.insertTypesFragment(doc);
        prettyDocumentToFile(doc, filename1);

        // Write out the implementation def
        doc = WSDLFactory.newInstance().newWSDLWriter().getDocument(impl);

        prettyDocumentToFile(doc, filename2);
    }

    /**
     * Generates a complete WSDL document for a given <code>Class</code>
     * 
     * @param filename WSDL
     * @throws IOException                  
     * @throws WSDLException                
     * @throws SAXException                 
     * @throws ParserConfigurationException 
     */
    public void emit(String filename)
            throws IOException, WSDLException, SAXException,
            ParserConfigurationException {
        emit(filename, MODE_ALL);
    }

    /**
     * Generates a WSDL document for a given <code>Class</code>.
     * The WSDL generated is controlled by the mode parameter
     * mode 0: All
     * mode 1: Interface
     * mode 2: Implementation
     * 
     * @param mode generation mode - all, interface, implementation
     * @return Document
     * @throws IOException                  
     * @throws WSDLException                
     * @throws SAXException                 
     * @throws ParserConfigurationException 
     */
    public Document emit(int mode)
            throws IOException, WSDLException, SAXException,
            ParserConfigurationException {

        Document doc;
        Definition def;

        switch (mode) {

            default:
            case MODE_ALL:
                def = getWSDL();

                for (int i = 0;
                     (extraClasses != null) && (i < extraClasses.length);
                     i++) {
                    types.writeTypeForPart(extraClasses[i], null);
                }

                // types.updateNamespaces();
                doc = WSDLFactory.newInstance().newWSDLWriter().getDocument(
                        def);

                types.insertTypesFragment(doc);
                break;

            case MODE_INTERFACE:
                def = getIntfWSDL();

                for (int i = 0;
                     (extraClasses != null) && (i < extraClasses.length);
                     i++) {
                    types.writeTypeForPart(extraClasses[i], null);
                }

                // types.updateNamespaces();
                doc = WSDLFactory.newInstance().newWSDLWriter().getDocument(
                        def);

                types.insertTypesFragment(doc);
                break;

            case MODE_IMPLEMENTATION:
                def = getImplWSDL();
                doc = WSDLFactory.newInstance().newWSDLWriter().getDocument(
                        def);
                break;
        }

        // Add Axis version info as comment to beginnning of generated WSDL
        if (versionMessage == null) {
            versionMessage = Messages.getMessage(
                    "wsdlCreated00",
                    XMLUtils.xmlEncodeString(Version.getVersion()));
        }
        // If version is empty string, don't emit one
        if (versionMessage != null && versionMessage.length() > 0) {
            Comment wsdlVersion = doc.createComment(versionMessage);
            doc.getDocumentElement().insertBefore(
                    wsdlVersion, doc.getDocumentElement().getFirstChild());
        }

        // Return the document
        return doc;
    }

    /**
     * Generates a String containing the WSDL for a given <code>Class</code>.
     * The WSDL generated is controlled by the mode parameter
     * mode 0: All
     * mode 1: Interface
     * mode 2: Implementation
     * 
     * @param mode generation mode - all, interface, implementation
     * @return String
     * @throws IOException                  
     * @throws WSDLException                
     * @throws SAXException                 
     * @throws ParserConfigurationException 
     */
    public String emitToString(int mode)
            throws IOException, WSDLException, SAXException,
            ParserConfigurationException {

        Document doc = emit(mode);
        StringWriter sw = new StringWriter();

        XMLUtils.PrettyDocumentToWriter(doc, sw);

        return sw.toString();
    }

    /**
     * Generates a WSDL document for a given <code>Class</code>.
     * The WSDL generated is controlled by the mode parameter
     * mode 0: All
     * mode 1: Interface
     * mode 2: Implementation
     * 
     * @param filename WSDL
     * @param mode     generation mode - all, interface, implementation
     * @throws IOException                  
     * @throws WSDLException                
     * @throws SAXException                 
     * @throws ParserConfigurationException 
     */
    public void emit(String filename, int mode)
            throws IOException, WSDLException, SAXException,
            ParserConfigurationException {

        Document doc = emit(mode);

        // Supply a reasonable file name if not supplied
        if (filename == null) {
            filename = getServicePortName();

            switch (mode) {

                case MODE_ALL:
                    filename += ".wsdl";
                    break;

                case MODE_INTERFACE:
                    filename += "_interface.wsdl";
                    break;

                case MODE_IMPLEMENTATION:
                    filename += "_implementation.wsdl";
                    break;
            }
        }

        prettyDocumentToFile(doc, filename);
    }

    /**
     * Get a Full WSDL <code>Definition</code> for the current
     * configuration parameters
     * 
     * @return WSDL <code>Definition</code>
     * @throws IOException                  
     * @throws WSDLException                
     * @throws SAXException                 
     * @throws ParserConfigurationException 
     */
    public Definition getWSDL()
            throws IOException, WSDLException, SAXException,
            ParserConfigurationException {

        // Invoke the init() method to ensure configuration is setup
        init(MODE_ALL);

        // Create a Definition for the output wsdl
        Definition def = createDefinition();

        // Write interface header
        writeDefinitions(def, intfNS);

        // Create Types
        types = createTypes(def);

        // Write the WSDL constructs and return full Definition
        Binding binding = writeBinding(def, true);

		log.debug("Writing types\r\n"+types+" defs "+def);
		
        writePortType(def, binding);
        writeService(def, binding);

        return def;
    }

    /**
     * Get a interface WSDL <code>Definition</code> for the
     * current configuration parameters
     * 
     * @return WSDL <code>Definition</code>
     * @throws IOException                  
     * @throws WSDLException                
     * @throws SAXException                 
     * @throws ParserConfigurationException 
     */
    public Definition getIntfWSDL()
            throws IOException, WSDLException, SAXException,
            ParserConfigurationException {

        // Invoke the init() method to ensure configuration is setup
        init(MODE_INTERFACE);

        // Create a definition for the output wsdl
        Definition def = createDefinition();

        // Write interface header
        writeDefinitions(def, intfNS);

        // Create Types
        types = createTypes(def);

        // Write the interface WSDL constructs and return the Definition
        Binding binding = writeBinding(def, true);

        writePortType(def, binding);

        return def;
    }

    /**
     * Get implementation WSDL <code>Definition</code> for the
     * current configuration parameters
     * 
     * @return WSDL <code>Definition</code>
     * @throws IOException                  
     * @throws WSDLException                
     * @throws SAXException                 
     * @throws ParserConfigurationException 
     */
    public Definition getImplWSDL()
            throws IOException, WSDLException, SAXException,
            ParserConfigurationException {

        // Invoke the init() method to ensure configuration is setup
        init(MODE_IMPLEMENTATION);

        // Create a Definition for the output wsdl
        Definition def = createDefinition();

        // Write implementation header and import
        writeDefinitions(def, implNS);
        writeImport(def, intfNS, importUrl);

        // Write the implementation WSDL constructs and return Definition
        Binding binding = writeBinding(def, false);    // Don't add binding to def

        writeService(def, binding);

        return def;
    }

    /**
     * Invoked prior to building a definition to ensure parms
     * and data are set up.
     * 
     * @param mode 
     */
    protected void init(int mode) {

        // Default use depending on setting of style
        if (use == null) {
            if (style == Style.RPC) {
                use = Use.ENCODED;
            } else {
                use = Use.LITERAL;
            }
        }

        if (tm == null) {
            String encodingStyle = "";
            if (use == Use.ENCODED) {
                encodingStyle = Constants.URI_SOAP11_ENC;
                /** TODO : Set this correctly if we do SOAP 1.2 support here */
            }
            tm = (TypeMapping)tmr.getTypeMapping(encodingStyle);
        }

        // Set up a ServiceDesc to use to introspect the Service
        if (serviceDesc == null) {
            JavaServiceDesc javaServiceDesc = new JavaServiceDesc();
            serviceDesc = javaServiceDesc;

            javaServiceDesc.setImplClass(cls);

            // Set the typeMapping to the one provided.
            serviceDesc.setTypeMapping(tm);

            javaServiceDesc.setStopClasses(stopClasses);
            serviceDesc.setAllowedMethods(allowedMethods);
            javaServiceDesc.setDisallowedMethods(disallowedMethods);
            serviceDesc.setStyle(style);
            serviceDesc.setUse(use);

            // If the class passed in is a portType,
            // there may be an implClass that is used to
            // obtain the method parameter names.  In this case,
            // a serviceDesc2 is built to get the method parameter names.
            if ((implCls != null) && (implCls != cls)
                    && (serviceDesc2 == null)) {
                serviceDesc2 = new JavaServiceDesc();

                serviceDesc2.setImplClass(implCls);

                // Set the typeMapping to the one provided.
                serviceDesc2.setTypeMapping(tm);

                serviceDesc2.setStopClasses(stopClasses);
                serviceDesc2.setAllowedMethods(allowedMethods);
                serviceDesc2.setDisallowedMethods(disallowedMethods);
                serviceDesc2.setStyle(style);
            }
        }

        if (encodingList == null) {

            // if cls contains a Class object with the service implementation use the Name of the
            // class else use the service name
            if (cls != null) {
                clsName = cls.getName();
                clsName = clsName.substring(clsName.lastIndexOf('.') + 1);
            } else {
                clsName = getServiceDesc().getName();
            }
			log.debug(" default class :"+cls+" Name: "+clsName);

            // Default the portType name
            if (getPortTypeName() == null) {
                setPortTypeName(clsName.replace("$","_"));
				log.debug("Default portTypeName ("+getPortTypeName()+")");
            }

            // Default the serviceElementName
            if (getServiceElementName() == null) {
                setServiceElementName(getPortTypeName() + "Service");
            }

            // If service port name is null, construct it from location or className
            if (getServicePortName() == null) {
                String name = getLocationUrl();

                if (name != null) {
                    if (name.lastIndexOf('/') > 0) {
                        name = name.substring(name.lastIndexOf('/') + 1);
                    } else if (name.lastIndexOf('\\') > 0) {
                        name = name.substring(name.lastIndexOf('\\') + 1);
                    } else {
                        name = null;
                    }

                    // if we got the name from the location, strip .jws from it
                    if ((name != null) && name.endsWith(".jws")) {
                        name = name.substring(0, (name.length()
                                - ".jws".length()));
                    }
                }

                if ((name == null) || name.equals("")) {
                    name = clsName;
                }

                setServicePortName(name);
            }

            // Default the bindingName
			log.trace(" Generating binding name. ServicePortName:"+getServicePortName());
            if (getBindingName() == null) {
                setBindingName(getServicePortName() + "SoapBinding");
            }

            encodingList = new ArrayList();

            encodingList.add(Constants.URI_DEFAULT_SOAP_ENC);

            if (intfNS == null) {
                Package pkg = cls.getPackage();

                intfNS = namespaces.getCreate((pkg == null)
                        ? null
                        : pkg.getName());
            }

            // Default the implementation namespace to the interface
            // namespace if not split wsdl mode.
            if (implNS == null) {
                if (mode == MODE_ALL) {
                    implNS = intfNS;
                } else {
                    implNS = intfNS + "-impl";
                }
            }

            // set the namespaces in the serviceDesc(s)
            serviceDesc.setDefaultNamespace(intfNS);

            if (serviceDesc2 != null) {
                serviceDesc2.setDefaultNamespace(implNS);
            }

            if (cls != null) {
                String clsName = cls.getName();
                int idx = clsName.lastIndexOf(".");
                if (idx > 0) {
                    String pkgName = clsName.substring(0, idx);
                    namespaces.put(pkgName, intfNS, "intf");
                }
            }

            namespaces.putPrefix(implNS, "impl");
        }
    }

    /**
     * Build a Definition from the input wsdl file or create
     * a new Definition
     * 
     * @return WSDL Definition
     * @throws WSDLException                
     * @throws SAXException                 
     * @throws IOException                  
     * @throws ParserConfigurationException 
     */
    protected Definition createDefinition()
            throws WSDLException, SAXException, IOException,
            ParserConfigurationException {

        Definition def;

        if (inputWSDL == null) {
            def = WSDLFactory.newInstance().newDefinition();
        } else {
            javax.wsdl.xml.WSDLReader reader =
                    WSDLFactory.newInstance().newWSDLReader();
            Document doc = XMLUtils.newDocument(inputWSDL);

            def = reader.readWSDL(null, doc);

            // The input wsdl types section is deleted.  The
            // types will be added back in at the end of processing.
            def.setTypes(null);
        }

        return def;
    }

    /** Field standardTypes */
    protected static TypeMapping standardTypes =
            (TypeMapping) new org.apache.axis.encoding.TypeMappingRegistryImpl().getTypeMapping(
                    null);

    /**
     * Build a Types object and load the input wsdl types
     * 
     * @param def Corresponding wsdl Definition
     * @return Types object
     * @throws IOException                  
     * @throws WSDLException                
     * @throws SAXException                 
     * @throws ParserConfigurationException 
     */
    protected Types createTypes(Definition def)
            throws IOException, WSDLException, SAXException,
            ParserConfigurationException {

        types = new Types(def, tm, (TypeMapping)tmr.getDefaultTypeMapping(),
                          namespaces, intfNS, stopClasses, serviceDesc, this);

        if (inputWSDL != null) {
            types.loadInputTypes(inputWSDL);
        }

        if (inputSchema != null) {
            StringTokenizer tokenizer = new StringTokenizer(inputSchema, ", ");

            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();

                types.loadInputSchema(token);
            }
        }
        
        // If we're supposed to emit all mapped types, do it now.
        if (emitAllTypes && tm != null) { 
            Class[] mappedTypes = tm.getAllClasses(); 
            
            for (int i = 0; i < mappedTypes.length; i++) { 
                Class mappedType = mappedTypes[i]; 
                QName name = tm.getTypeQName(mappedType); 
                if (name.getLocalPart().indexOf(SymbolTable.ANON_TOKEN) != -1) { 
                    // If this is an anonymous type, it doesn't need to be
                    // written out here (and trying to do so will generate an
                    // error). Skip it. 
                    continue; 
                } 
                
                /** 
                 * If it's a non-standard type, make sure it shows up in 
                 * our WSDL 
                 */ 
                if (standardTypes.getSerializer(mappedType) == null) { 
                    types.writeTypeForPart(mappedType, name); 
                } 
            }
            
            // Don't bother checking for subtypes, since we already wrote
            // all the possibilities.
            types.mappedTypes = null;
        }
        
        return types;
    }
    
    /**
     * Create a documentation element
     *  
     * @param documentation
     * @return
     */
    protected Element createDocumentationElement(String documentation) {
        Element element = docHolder.createElementNS(Constants.NS_URI_WSDL11, "documentation");
        element.setPrefix(Constants.NS_PREFIX_WSDL);
        Text textNode =
                docHolder.createTextNode(documentation);

        element.appendChild(textNode);
        return element;
    }
    
    /**
     * Create the definition header information.
     * 
     * @param def <code>Definition</code>
     * @param tns target namespace
     */
    protected void writeDefinitions(Definition def, String tns) {

        def.setTargetNamespace(tns);
        def.addNamespace("intf", intfNS);
        def.addNamespace("impl", implNS);
        def.addNamespace(Constants.NS_PREFIX_WSDL_SOAP,
                Constants.URI_WSDL11_SOAP);
        namespaces.putPrefix(Constants.URI_WSDL11_SOAP,
                Constants.NS_PREFIX_WSDL_SOAP);
        def.addNamespace(Constants.NS_PREFIX_WSDL, Constants.NS_URI_WSDL11);
        namespaces.putPrefix(Constants.NS_URI_WSDL11, Constants.NS_PREFIX_WSDL);

        if (use == Use.ENCODED) {
            def.addNamespace(Constants.NS_PREFIX_SOAP_ENC,
                    Constants.URI_DEFAULT_SOAP_ENC);
            namespaces.putPrefix(Constants.URI_DEFAULT_SOAP_ENC,
                    Constants.NS_PREFIX_SOAP_ENC);
        }

        def.addNamespace(Constants.NS_PREFIX_SCHEMA_XSD,
                Constants.URI_DEFAULT_SCHEMA_XSD);
        namespaces.putPrefix(Constants.URI_DEFAULT_SCHEMA_XSD,
                Constants.NS_PREFIX_SCHEMA_XSD);
        def.addNamespace(Constants.NS_PREFIX_XMLSOAP, Constants.NS_URI_XMLSOAP);
        namespaces.putPrefix(Constants.NS_URI_XMLSOAP,
                Constants.NS_PREFIX_XMLSOAP);
    }

    /**
     * Create and add an import
     * 
     * @param def <code>Definition</code>
     * @param tns target namespace
     * @param loc target location
     */
    protected void writeImport(Definition def, String tns, String loc) {

        Import imp = def.createImport();

        imp.setNamespaceURI(tns);

        if ((loc != null) && !loc.equals("")) {
            imp.setLocationURI(loc);
        }

        def.addImport(imp);
    }

    /**
     * Create the binding.
     * 
     * @param def <code>Definition</code>
     * @param add true if binding should be added to the def
     * @return 
     */
    protected Binding writeBinding(Definition def, boolean add) {

        QName bindingQName = new QName(intfNS, getBindingName());

        // If a binding already exists, don't replace it.
        Binding binding = def.getBinding(bindingQName);

        if (binding != null) {
            return binding;
        }

        // Create a binding
        binding = def.createBinding();

        binding.setUndefined(false);
        binding.setQName(bindingQName);

        SOAPBinding soapBinding = new SOAPBindingImpl();
        String styleStr = (style == Style.RPC)
                ? "rpc"
                : "document";

        soapBinding.setStyle(styleStr);
        soapBinding.setTransportURI(Constants.URI_SOAP11_HTTP);
        binding.addExtensibilityElement(soapBinding);

        if (add) {
            def.addBinding(binding);
        }

        return binding;
    }

    /** Field docHolder */
    Document docHolder;

    /**
     * Method createDocumentFragment
     */
    private void createDocumentFragment() {

        try {
            this.docHolder = XMLUtils.newDocument();
        } catch (ParserConfigurationException e) {

            // This should not occur
            throw new InternalException(e);
        }
    }

    /**
     * Create the service.
     * 
     * @param def     
     * @param binding 
     */
    protected void writeService(Definition def, Binding binding) {

        QName serviceElementQName = new QName(implNS, getServiceElementName());

        // Locate an existing service, or get a new service
        Service service = def.getService(serviceElementQName);

        if (service == null) {
            service = def.createService();

            service.setQName(serviceElementQName);
            def.addService(service);
        }

        if (description != null) {
            service.setDocumentationElement(
                    createDocumentationElement(description));
        } else if (serviceDesc.getDocumentation() != null) {
            service.setDocumentationElement(
                    createDocumentationElement(
                            serviceDesc.getDocumentation()));
        }

        // Add the port
        Port port = def.createPort();

        port.setBinding(binding);

        // Probably should use the end of the location Url
        port.setName(getServicePortName());

        SOAPAddress addr = new SOAPAddressImpl();

        addr.setLocationURI(locationUrl);
        port.addExtensibilityElement(addr);
        service.addPort(port);
    }

    /**
     * Create a PortType
     * 
     * @param def     
     * @param binding 
     * @throws WSDLException 
     * @throws AxisFault     
     */
    protected void writePortType(Definition def, Binding binding)
            throws WSDLException, AxisFault {

        QName portTypeQName = new QName(intfNS, getPortTypeName());
		
		log.debug("portTypeQName ["+portTypeQName+"] portTypeName:"+getPortTypeName() );

        // Get or create a portType
        PortType portType = def.getPortType(portTypeQName);
        boolean newPortType = false;

        if (portType == null) {
            portType = def.createPortType();

            portType.setUndefined(false);
            portType.setQName(portTypeQName);

            newPortType = true;
        } else if (binding.getBindingOperations().size() > 0) {

            // If both portType and binding already exist,
            // no additional processing is needed.
            return;
        }

        // Add the port and binding operations.
        ArrayList operations = serviceDesc.getOperations();

        for (Iterator i = operations.iterator(); i.hasNext();) {
            OperationDesc thisOper = (OperationDesc) i.next();
            BindingOperation bindingOper = writeOperation(def, binding,
                    thisOper);
            Operation oper = bindingOper.getOperation();
            OperationDesc messageOper = thisOper;

            // add the documentation to oper
            if (messageOper.getDocumentation() != null) {
                oper.setDocumentationElement(
                        createDocumentationElement(
                                messageOper.getDocumentation()));
            }

            if (serviceDesc2 != null) {

                // If a serviceDesc containing an impl class is provided,
                // try and locate the corresponding operation
                // (same name, same parm types and modes).  If a
                // corresponding operation is found, it is sent
                // to the writeMessages method so that its parameter
                // names will be used in the wsdl file.
                OperationDesc[] operArray =
                        serviceDesc2.getOperationsByName(thisOper.getName());
                boolean found = false;

                if (operArray != null) {
                    for (int j = 0; (j < operArray.length) && !found; j++) {
                        OperationDesc tryOper = operArray[j];

                        if (tryOper.getParameters().size()
                                == thisOper.getParameters().size()) {
                            boolean parmsMatch = true;

                            for (int k =
                                    0; (k < thisOper.getParameters().size())
                                    && parmsMatch; k++) {
                                if ((tryOper.getParameter(
                                        k).getMode() != thisOper.getParameter(
                                                k).getMode())
                                        || (!tryOper.getParameter(
                                                k).getJavaType().equals(
                                                        thisOper.getParameter(
                                                                k).getJavaType()))) {
                                    parmsMatch = false;
                                }
                            }

                            if (parmsMatch) {
                                messageOper = tryOper;
                                found = true;
                            }
                        }
                    }
                }
            }

            writeMessages(def, oper, messageOper, bindingOper);

            if (newPortType) {
                portType.addOperation(oper);
            }
        }

        if (newPortType) {
            def.addPortType(portType);
        }

        binding.setPortType(portType);
    }

    /**
     * Create a Message
     * 
     * @param def         Definition, the WSDL definition
     * @param oper        Operation, the wsdl operation
     * @param desc        OperationDesc, the Operation Description
     * @param bindingOper BindingOperation, corresponding Binding Operation
     * @throws WSDLException 
     * @throws AxisFault     
     */
    protected void writeMessages(Definition def,
                                 Operation oper,
                                 OperationDesc desc,
                                 BindingOperation bindingOper)
            throws WSDLException, AxisFault {

        Input input = def.createInput();
        Message msg = writeRequestMessage(def, desc, bindingOper);

        input.setMessage(msg);

        // Give the input element a name that matches the
        // message.  This is necessary for overloading.
        // The msg QName is unique.
        String name = msg.getQName().getLocalPart();

        input.setName(name);
        bindingOper.getBindingInput().setName(name);
        oper.setInput(input);
        def.addMessage(msg);
        
        if (OperationType.REQUEST_RESPONSE.equals(desc.getMep())) {
            msg = writeResponseMessage(def, desc, bindingOper);
            
            Output output = def.createOutput();
            
            output.setMessage(msg);
            
            // Give the output element a name that matches the
            // message.  This is necessary for overloading.
            // The message QName is unique.
            name = msg.getQName().getLocalPart();
            
            output.setName(name);
            bindingOper.getBindingOutput().setName(name);
            oper.setOutput(output);
            def.addMessage(msg);
        }
        
        ArrayList exceptions = desc.getFaults();

        for (int i = 0; (exceptions != null) && (i < exceptions.size()); i++) {
            FaultDesc faultDesc = (FaultDesc) exceptions.get(i);

            msg = writeFaultMessage(def, faultDesc);

            // Add the fault to the portType
            Fault fault = def.createFault();

            fault.setMessage(msg);
            fault.setName(faultDesc.getName());
            oper.addFault(fault);

            // Add the fault to the binding
            BindingFault bFault = def.createBindingFault();

            bFault.setName(faultDesc.getName());

            SOAPFault soapFault = writeSOAPFault(faultDesc);

            bFault.addExtensibilityElement(soapFault);
            bindingOper.addBindingFault(bFault);

            // Add the fault message
            if (def.getMessage(msg.getQName()) == null) {
                def.addMessage(msg);
            }
        }

        // Set the parameter ordering using the parameter names
        ArrayList parameters = desc.getParameters();
        Vector names = new Vector();

        for (int i = 0; i < parameters.size(); i++) {
            ParameterDesc param = (ParameterDesc) parameters.get(i);

            names.add(param.getName());
        }

        if (names.size() > 0) {
            if (style == Style.WRAPPED) {
                names.clear();
            } else {
                oper.setParameterOrdering(names);
            }
        }
    }

    /**
     * Create a Operation
     * 
     * @param def     
     * @param binding 
     * @param desc    
     * @return 
     */
    protected BindingOperation writeOperation(Definition def, Binding binding,
                                              OperationDesc desc) {

        Operation oper = def.createOperation();

        QName elementQName = desc.getElementQName();
        if(elementQName != null && elementQName.getLocalPart() != null) {
            oper.setName(elementQName.getLocalPart());
        } else {
            oper.setName(desc.getName());
        }
        oper.setUndefined(false);

        return writeBindingOperation(def, binding, oper, desc);
    }

    /**
     * Create a Binding Operation
     * 
     * @param def     
     * @param binding 
     * @param oper    
     * @param desc    
     * @return 
     */
    protected BindingOperation writeBindingOperation(Definition def,
                                                     Binding binding,
                                                     Operation oper,
                                                     OperationDesc desc) {

        BindingOperation bindingOper = def.createBindingOperation();
        BindingInput bindingInput = def.createBindingInput();
        BindingOutput bindingOutput = null;
        
        // TODO : Make this deal with all MEPs
        if (OperationType.REQUEST_RESPONSE.equals(desc.getMep())) 
            bindingOutput = def.createBindingOutput();

        bindingOper.setName(oper.getName());
        bindingOper.setOperation(oper);

        SOAPOperation soapOper = new SOAPOperationImpl();

        // If the soapAction option is OPERATION, force
        // soapAction to the name of the operation. If NONE,
        // force soapAction to "".
        // Otherwise use the information in the operationDesc.
        String soapAction;
        if (getSoapAction().equalsIgnoreCase("OPERATION")) {
            soapAction = oper.getName();
        } else if (getSoapAction().equalsIgnoreCase("NONE")) {
            soapAction = "";
        } else {
            soapAction = desc.getSoapAction();

            if (soapAction == null) {
                soapAction = "";
            }
        }

        soapOper.setSoapActionURI(soapAction);

        // Until we have per-operation configuration, this will always be
        // the same as the binding default.
        // soapOper.setStyle("rpc");
        bindingOper.addExtensibilityElement(soapOper);

        // Add soap:body element to the binding <input> element
        ExtensibilityElement inputBody = writeSOAPBody(desc.getElementQName());
        bindingInput.addExtensibilityElement(inputBody);

        // add soap:headers, if any, to binding <input> element
        // only when we write the Message and parts.

        // Add soap:body element to the binding <output> element
        if (bindingOutput != null) {
            ExtensibilityElement outputBody = writeSOAPBody(desc.getReturnQName());
            bindingOutput.addExtensibilityElement(outputBody);
            bindingOper.setBindingOutput(bindingOutput);

            // add soap:headers, if any, to binding <output> element
            // only when we write the Message and parts.
        }
        
        // Add input to operation
        bindingOper.setBindingInput(bindingInput);

        // Faults clause
        // Comment out the following part 
        // because it actually does the same thing as in writeMessages.
        /*
        ArrayList faultList = desc.getFaults();

        if (faultList != null) {
            for (Iterator it = faultList.iterator(); it.hasNext();) {
                FaultDesc faultDesc = (FaultDesc) it.next();

                // Get a soap:fault
                ExtensibilityElement soapFault = writeSOAPFault(faultDesc);

                // Get a wsdl:fault to put the soap:fault in
                BindingFault bindingFault = new BindingFaultImpl();

                bindingFault.setName(faultDesc.getName());
                bindingFault.addExtensibilityElement(soapFault);
                bindingOper.addBindingFault(bindingFault);
            }
        }
        */

        binding.addBindingOperation(bindingOper);

        return bindingOper;
    }

    /**
     * Create a SOAPHeader element
     */
    protected SOAPHeader writeSOAPHeader(ParameterDesc p, QName messageQName, String partName)
    {
        SOAPHeaderImpl soapHeader = new SOAPHeaderImpl();

        // for now, if its document, it is literal use.
        if (use == Use.ENCODED) {
            soapHeader.setUse("encoded");
            soapHeader.setEncodingStyles(encodingList);
        } else {
            soapHeader.setUse("literal");
        }

        // Set namespace
        if (targetService == null) {
            soapHeader.setNamespaceURI(intfNS);
        } else {
            soapHeader.setNamespaceURI(targetService);
        }
        QName headerQName = p.getQName();
        if ((headerQName != null) && !headerQName.getNamespaceURI().equals("")) {
            soapHeader.setNamespaceURI(headerQName.getNamespaceURI());
        }

        // Set the Message and Part information
         soapHeader.setMessage(messageQName);
         soapHeader.setPart(partName);

        return soapHeader;
    }

    /**
     * Method writeSOAPBody
     * 
     * @param operQName 
     * @return 
     */
    protected ExtensibilityElement writeSOAPBody(QName operQName) {

        SOAPBody soapBody = new SOAPBodyImpl();

        // for now, if its document, it is literal use.
        if (use == Use.ENCODED) {
            soapBody.setUse("encoded");
            soapBody.setEncodingStyles(encodingList);
        } else {
            soapBody.setUse("literal");
        }

        if (style == Style.RPC) {
            if (targetService == null) {
                soapBody.setNamespaceURI(intfNS);
            } else {
                soapBody.setNamespaceURI(targetService);
            }
    
            if ((operQName != null) && !operQName.getNamespaceURI().equals("")) {
                soapBody.setNamespaceURI(operQName.getNamespaceURI());
            }
        }

        // The parts attribute will get set if we have headers.
        // This gets done when the Message & parts are generated
        // soapBody.setParts(...);

        return soapBody;
    }    // writeSOAPBody

    /**
     * Method writeSOAPFault
     * 
     * @param faultDesc 
     * @return 
     */
    protected SOAPFault writeSOAPFault(FaultDesc faultDesc) {

        SOAPFault soapFault = new com.ibm.wsdl.extensions.soap.SOAPFaultImpl();

        soapFault.setName(faultDesc.getName());

        if (use != Use.ENCODED) {
            soapFault.setUse("literal");

            // no namespace for literal, gets it from the element
        } else {
            soapFault.setUse("encoded");
            soapFault.setEncodingStyles(encodingList);

            // Set the namespace from the fault QName if it exists
            // otherwise use the target (or interface) namespace
            QName faultQName = faultDesc.getQName();

            if ((faultQName != null)
                    && !faultQName.getNamespaceURI().equals("")) {
                soapFault.setNamespaceURI(faultQName.getNamespaceURI());
            } else {
                if (targetService == null) {
                    soapFault.setNamespaceURI(intfNS);
                } else {
                    soapFault.setNamespaceURI(targetService);
                }
            }
        }

        return soapFault;
    }    // writeSOAPFault

    /**
     * Create a Request Message
     * 
     * @param def  
     * @param oper 
     * @return 
     * @throws WSDLException 
     * @throws AxisFault     
     */
    protected Message writeRequestMessage(Definition def, OperationDesc oper, BindingOperation bindop)
            throws WSDLException, AxisFault
    {

        String partName;
        ArrayList bodyParts = new ArrayList();
        ArrayList parameters = oper.getAllInParams();

        Message msg = def.createMessage();
        QName qName = createMessageName(def,
                getRequestQName(oper).getLocalPart() + "Request");

        msg.setQName(qName);
        msg.setUndefined(false);

        // output all the parts for headers
        boolean headers = writeHeaderParts(def, parameters, bindop, msg, true);

        if (oper.getStyle() == Style.MESSAGE) {

            // If this is a MESSAGE-style operation, just write out
            // <xsd:any> for now.
            // TODO: Support custom schema in WSDD for these operations
            QName qname = oper.getElementQName();
            types.writeElementDecl(qname, Object.class,
                                   Constants.XSD_ANYTYPE, false, null);

            Part part = def.createPart();

            part.setName("part");
            part.setElementName(qname);
            msg.addPart(part);
            bodyParts.add(part.getName());

        } else if (oper.getStyle() == Style.WRAPPED) {

            // If we're WRAPPED, write the wrapper element first, and then
            // fill in any params.  If there aren't any params, we'll see
            // an empty <complexType/> for the wrapper element.
            partName = writeWrapperPart(def, msg, oper, true);
            bodyParts.add(partName);

        } else {
        	//Now we're either DOCUMENT or RPC. If we're doing doc/lit, and in the
        	//case of mulitple input params, we would warn user saying request
        	//message's type information is being written out as multiple parts
        	//than one single complexType and to interop with other soap stacks
        	//that do doc/lit by default, user might want to publish his service
        	//as a WRAPPED-LITERAL service instead.
        	//see http://issues.apache.org/jira/browse/AXIS-2017
        	if(oper.getStyle() == Style.DOCUMENT && parameters.size()>1 ) {
         		log.debug(Messages.getMessage("warnDocLitInteropMultipleInputParts"));
         	}

            // write a part for each non-header parameter
            for (int i = 0; i < parameters.size(); i++) {
                ParameterDesc parameter = (ParameterDesc) parameters.get(i);
                if (!parameter.isInHeader() && !parameter.isOutHeader()) {
                    partName = writePartToMessage(def, msg, true, parameter);
                    bodyParts.add(partName);
                }
            }
        }

        // If we have headers, we must fill in the parts attribute of soap:body
        // if not, we just leave it out (which means all parts)
        if (headers) {
            // Find soap:body in binding
            List extensibilityElements = bindop.getBindingInput().getExtensibilityElements();
            for (int i = 0; i < extensibilityElements.size(); i++)
            {
                Object ele = extensibilityElements.get(i);
                if (ele instanceof SOAPBodyImpl)
                {
                    SOAPBodyImpl soapBody = (SOAPBodyImpl) ele;
                    soapBody.setParts(bodyParts);
                }
            }
        }

        return msg;
    }

    /**
     * Create parts of a Message for header parameters and write then in
     * to the provided Message element. Also create a soap:header element
     * in the binding
     *
     * @param parameters the list of parameters for the current operation
     * @param bindop the current bindingOperation
     * @param msg the message to add the parts to
     * @param request true if we are do an input message, false if it is output
     * @return true if we wrote any header parts
     */
    private boolean writeHeaderParts(Definition def,
                                     ArrayList parameters,
                                     BindingOperation bindop,
                                     Message msg,
                                     boolean request) throws WSDLException, AxisFault
    {
        boolean wroteHeaderParts = false;
        String partName;

        // Loop over all the parameters for this operation
        for (int i = 0; i < parameters.size(); i++) {
            ParameterDesc parameter = (ParameterDesc) parameters.get(i);

            // write the input or output header parts in to the Message
            if (request && parameter.isInHeader()) {
                // put part in message
                partName = writePartToMessage(def, msg, request, parameter);
                // Create a soap:header element
                SOAPHeader hdr = writeSOAPHeader(parameter, msg.getQName(), partName);
                // put it in the binding <input> element
                bindop.getBindingInput().addExtensibilityElement(hdr);
                wroteHeaderParts = true;
            }
            else if (!request && parameter.isOutHeader()) {
                // put part in message
                partName = writePartToMessage(def, msg, request, parameter);
                // Create a soap:header element
                SOAPHeader hdr = writeSOAPHeader(parameter, msg.getQName(), partName);
                // put it in the binding <output> element
                bindop.getBindingOutput().addExtensibilityElement(hdr);
                wroteHeaderParts = true;
            }
            else {
                continue;   // body part
            }
        }
        return wroteHeaderParts;
    }

    /**
     * Method getRequestQName
     * 
     * @param oper 
     * @return 
     */
    protected QName getRequestQName(OperationDesc oper) {

        qualifyOperation(oper);

        QName qname = oper.getElementQName();

        if (qname == null) {
            qname = new QName(oper.getName());
        }

        return qname;
    }

    /**
     * Method qualifyOperation
     * 
     * @param oper 
     */
    private void qualifyOperation(OperationDesc oper) {

        if ((style == Style.WRAPPED) && (use == Use.LITERAL)) {
            QName qname = oper.getElementQName();

            if (qname == null) {
                qname = new QName(intfNS, oper.getName());
            } else if (qname.getNamespaceURI().equals("")) {
                qname = new QName(intfNS, qname.getLocalPart());
            }

            oper.setElementQName(qname);
        }
    }

    /**
     * Method getResponseQName
     * 
     * @param oper 
     * @return 
     */
    protected QName getResponseQName(OperationDesc oper) {

        qualifyOperation(oper);

        QName qname = oper.getElementQName();

        if (qname == null) {
            return new QName(oper.getName() + "Response");
        }

        return new QName(qname.getNamespaceURI(),
                qname.getLocalPart() + "Response");
    }

    /**
     * Write out the schema definition for a WRAPPED operation request or
     * response.
     * 
     * @param def     
     * @param msg     
     * @param oper    
     * @param request
     * @return the name of the part the was written
     * @throws AxisFault 
     */
    public String writeWrapperPart(
            Definition def, Message msg, OperationDesc oper, boolean request)
            throws AxisFault {

        QName qname = request
                ? getRequestQName(oper)
                : getResponseQName(oper);

        boolean hasParams;
        if (request) {
            hasParams = (oper.getNumInParams() > 0);
        } else {
            if (oper.getReturnClass() != void.class) {
                hasParams = true;
            } else {
                hasParams = (oper.getNumOutParams() > 0);
            }
        }

        // First write the wrapper element itself.
        Element sequence = types.writeWrapperElement(qname, request, hasParams);

        // If we got anything back above, there must be parameters in the
        // operation, and it's a <sequence> node in which to write them...
        if (sequence != null) {
            ArrayList parameters = request
                    ? oper.getAllInParams()
                    : oper.getAllOutParams();

            if (!request) {
                String retName;
                
                if (oper.getReturnQName() == null) {
                    retName = oper.getName() + "Return";
                } else {
                    retName = oper.getReturnQName().getLocalPart();
                }

                types.writeWrappedParameter(sequence, retName,
                        oper.getReturnType(),
                        oper.getReturnClass());
            }

            for (int i = 0; i < parameters.size(); i++) {
                ParameterDesc parameter = (ParameterDesc) parameters.get(i);

                // avoid headers
                if (!parameter.isInHeader() && !parameter.isOutHeader())
                {
                    types.writeWrappedParameter(sequence,
                                                parameter.getName(),
                                                parameter.getTypeQName(),
                                                parameter.getJavaType());
                }
            }
        }

        // Finally write the part itself
        Part part = def.createPart();

        part.setName("parameters");    // We always use "parameters"
        part.setElementName(qname);
        msg.addPart(part);

        return part.getName();
    }

    /**
     * Create a Response Message
     * 
     * @param def  
     * @param desc 
     * @return 
     * @throws WSDLException 
     * @throws AxisFault     
     */
    protected Message writeResponseMessage(Definition def, OperationDesc desc, BindingOperation bindop)
            throws WSDLException, AxisFault
    {
        String partName;
        ArrayList bodyParts = new ArrayList();
        ArrayList parameters = desc.getAllOutParams();

        Message msg = def.createMessage();
        QName qName =
                createMessageName(def, getResponseQName(desc).getLocalPart());

        msg.setQName(qName);
        msg.setUndefined(false);

        // output all the parts for headers
        boolean headers = writeHeaderParts(def, parameters, bindop, msg, false);

        if (desc.getStyle() == Style.WRAPPED) {
            partName = writeWrapperPart(def, msg, desc, false);
            bodyParts.add(partName);
        } else {

            // Write the return value part
            ParameterDesc retParam = new ParameterDesc();

            if (desc.getReturnQName() == null) {
                String ns = "";

                if (desc.getStyle() != Style.RPC) {
                    ns = getServiceDesc().getDefaultNamespace();

                    if ((ns == null) || "".equals(ns)) {
                        ns = "http://ws.apache.org/axis/defaultNS";
                    }
                }

                retParam.setQName(new QName(ns, desc.getName() + "Return"));
            } else {
                retParam.setQName(desc.getReturnQName());
            }

            retParam.setTypeQName(desc.getReturnType());
            retParam.setMode(ParameterDesc.OUT);
            retParam.setIsReturn(true);
            retParam.setJavaType(desc.getReturnClass());
            String returnPartName = writePartToMessage(def, msg, false, retParam);
            bodyParts.add(returnPartName);

            // write a part for each non-header parameter
            for (int i = 0; i < parameters.size(); i++) {
                ParameterDesc parameter = (ParameterDesc) parameters.get(i);
                if (!parameter.isInHeader() && !parameter.isOutHeader()) {
                    partName = writePartToMessage(def, msg, false, parameter);
                    bodyParts.add(partName);
                }
            }

        }
        // If we have headers, we must fill in the parts attribute of soap:body
        // if not, we just leave it out (which means all parts)
        if (headers) {
            // Find soap:body in binding
            List extensibilityElements = bindop.getBindingOutput().getExtensibilityElements();
            for (int i = 0; i < extensibilityElements.size(); i++)
            {
                Object ele = extensibilityElements.get(i);
                if (ele instanceof SOAPBodyImpl)
                {
                    SOAPBodyImpl soapBody = (SOAPBodyImpl) ele;
                    soapBody.setParts(bodyParts);
                }
            }
        }

        return msg;
    }

    /**
     * Create a Fault Message
     * 
     * @param def       
     * @param exception (an ExceptionRep object)
     * @return 
     * @throws WSDLException 
     * @throws AxisFault     
     */
    protected Message writeFaultMessage(Definition def, FaultDesc exception)
            throws WSDLException, AxisFault {

        String pkgAndClsName = exception.getClassName();
        String clsName =
                pkgAndClsName.substring(pkgAndClsName.lastIndexOf('.') + 1,
                        pkgAndClsName.length());

        // Do this to cover the complex type case with no meta data
        exception.setName(clsName);

        // The following code uses the class name for both the name= attribute
        // and the message= attribute.
        Message msg = (Message) exceptionMsg.get(pkgAndClsName);

        if (msg == null) {
            msg = def.createMessage();

            QName qName = createMessageName(def, clsName);

            msg.setQName(qName);
            msg.setUndefined(false);

            ArrayList parameters = exception.getParameters();

            if (parameters != null) {
                for (int i = 0; i < parameters.size(); i++) {
                    ParameterDesc parameter = (ParameterDesc) parameters.get(i);

                    writePartToMessage(def, msg, true, parameter);
                }
            }

            exceptionMsg.put(pkgAndClsName, msg);
        }

        return msg;
    }

    /**
     * Create a Part
     * 
     * @param def     
     * @param msg     
     * @param request message is for a request
     * @param param   ParamRep object
     * @return The parameter name added or null
     * @throws WSDLException 
     * @throws AxisFault     
     */
    public String writePartToMessage(
            Definition def, Message msg, boolean request, ParameterDesc param)
            throws WSDLException, AxisFault {

        // Return if this is a void type
        if ((param == null) || (param.getJavaType() == java.lang.Void.TYPE)) {
            return null;
        }

        // If Request message, only continue if IN or INOUT
        // If Response message, only continue if OUT or INOUT
        if (request && (param.getMode() == ParameterDesc.OUT)) {
            return null;
        }

        if (!request && (param.getMode() == ParameterDesc.IN)) {
            return null;
        }

        // Create the Part
        Part part = def.createPart();

        if (param.getDocumentation() != null) {
            part.setDocumentationElement(
                    createDocumentationElement(
                            param.getDocumentation()));
        }

        // Get the java type to represent in the wsdl
        // (if the mode is OUT or INOUT and this
        // parameter does not represent the return type,
        // the type held in the Holder is the one that should
        // be written.)
        Class javaType = param.getJavaType();

        if ((param.getMode() != ParameterDesc.IN)
                && (param.getIsReturn() == false)) {
            javaType = JavaUtils.getHolderValueType(javaType);
        }

        if ((use == Use.ENCODED) || (style == Style.RPC)) {

            // Add the type representing the param
            // Write <part name=param_name type=param_type>
            QName typeQName = param.getTypeQName();

            if (javaType != null) {
                typeQName = types.writeTypeAndSubTypeForPart(javaType, typeQName);
            }

            // types.writeElementForPart(javaType, param.getTypeQName());
            if (typeQName != null) {
                part.setName(param.getName());
                part.setTypeName(typeQName);
                msg.addPart(part);
            }
        } else if (use == Use.LITERAL) {

            // This is doc/lit.  So we should write out an element
            // declaration whose name and type may be found in the
            // ParameterDesc.
            QName qname = param.getQName();

            if (param.getTypeQName() == null) {
                log.warn(Messages.getMessage("registerTypeMappingFor01",
                        param.getJavaType().getName()));
                QName qName = types.writeTypeForPart(param.getJavaType(),null);
                if (qName != null) {
                    param.setTypeQName(qName);                    
                } else {
                    param.setTypeQName(Constants.XSD_ANYTYPE);                    
                }                    
            }

            if (param.getTypeQName().getNamespaceURI().equals("")) {
                param.setTypeQName(
                        new QName(intfNS, param.getTypeQName().getLocalPart()));
            }

            if (param.getQName().getNamespaceURI().equals("")) {
                qname = new QName(intfNS, param.getQName().getLocalPart());

                param.setQName(qname);
            }

            // Make sure qname's value is unique.
            ArrayList   names = (ArrayList)
                    usedElementNames.get(qname.getNamespaceURI());
            if (names == null) {
                names = new ArrayList(1);
                usedElementNames.put(qname.getNamespaceURI(), names);
            }
            else if (names.contains(qname.getLocalPart())) {
                qname = new QName(qname.getNamespaceURI(),
                    JavaUtils.getUniqueValue(names, qname.getLocalPart()));
            }
            names.add(qname.getLocalPart());

            types.writeElementDecl(qname,
                                   param.getJavaType(),
                                   param.getTypeQName(),
                                   false,
                                   param.getItemQName());

            part.setName(param.getName());
            part.setElementName(qname);
            msg.addPart(part);
        }

        // return the name of the parameter added
        return param.getName();
    }

    /*
     * Return a message QName which has not already been defined in the WSDL
     */

    /**
     * Method createMessageName
     * 
     * @param def        
     * @param methodName 
     * @return 
     */
    protected QName createMessageName(Definition def, String methodName) {

        QName qName = new QName(intfNS, methodName);

        // Check the make sure there isn't a message with this name already
        int messageNumber = 1;

        while (def.getMessage(qName) != null) {
            StringBuffer namebuf = new StringBuffer(methodName);

            namebuf.append(messageNumber);

            qName = new QName(intfNS, namebuf.toString());

            messageNumber++;
        }

        return qName;
    }

    /**
     * Write a prettified document to a file.
     * 
     * @param doc      the Document to write
     * @param filename the name of the file to be written
     * @throws IOException various file i/o exceptions
     */
    protected void prettyDocumentToFile(Document doc, String filename)
            throws IOException {

        FileOutputStream fos = new FileOutputStream(new File(filename));

        XMLUtils.PrettyDocumentToStream(doc, fos);
        fos.close();
    }

    // -------------------- Parameter Query Methods ----------------------------//

    /**
     * Returns the <code>Class</code> to export
     * 
     * @return the <code>Class</code> to export
     */
    public Class getCls() {
        return cls;
    }

    /**
     * Sets the <code>Class</code> to export
     * 
     * @param cls the <code>Class</code> to export
     */
    public void setCls(Class cls) {
        this.cls = cls;
    }

    /**
     * Sets the <code>Class</code> to export.
     * 
     * @param cls      the <code>Class</code> to export
     * @param location 
     */
    public void setClsSmart(Class cls, String location) {

        if ((cls == null) || (location == null)) {
            return;
        }

        // Strip off \ and / from location
        if (location.lastIndexOf('/') > 0) {
            location = location.substring(location.lastIndexOf('/') + 1);
        } else if (location.lastIndexOf('\\') > 0) {
            location = location.substring(location.lastIndexOf('\\') + 1);
        }

        // Get the constructors of the class
        java.lang.reflect.Constructor[] constructors =
                cls.getDeclaredConstructors();
        Class intf = null;

        for (int i = 0; (i < constructors.length) && (intf == null); i++) {
            Class[] parms = constructors[i].getParameterTypes();

            // If the constructor has a single parameter
            // that is an interface which
            // matches the location, then use this as the interface class.
            if ((parms.length == 1) && parms[0].isInterface()
                    && (parms[0].getName() != null)
                    && Types.getLocalNameFromFullName(
                            parms[0].getName()).equals(location)) {
                intf = parms[0];
            }
        }

        if (intf != null) {
            setCls(intf);

            if (implCls == null) {
                setImplCls(cls);
            }
        } else {
            setCls(cls);
        }
    }

    /**
     * Sets the <code>Class</code> to export
     * 
     * @param className the name of the <code>Class</code> to export
     * @throws ClassNotFoundException 
     */
    public void setCls(String className) throws ClassNotFoundException {
        cls = ClassUtils.forName(className);
    }

    /**
     * Returns the implementation <code>Class</code> if set
     * 
     * @return the implementation Class or null
     */
    public Class getImplCls() {
        return implCls;
    }

    /**
     * Sets the implementation <code>Class</code>
     * 
     * @param implCls the <code>Class</code> to export
     */
    public void setImplCls(Class implCls) {
        this.implCls = implCls;
    }

    /**
     * Sets the implementation <code>Class</code>
     * 
     * @param className the name of the implementation <code>Class</code>
     */
    public void setImplCls(String className) {

        try {
            implCls = ClassUtils.forName(className);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns the interface namespace
     * 
     * @return interface target namespace
     */
    public String getIntfNamespace() {
        return intfNS;
    }

    /**
     * Set the interface namespace
     * 
     * @param ns interface target namespace
     */
    public void setIntfNamespace(String ns) {
        this.intfNS = ns;
    }

    /**
     * Returns the implementation namespace
     * 
     * @return implementation target namespace
     */
    public String getImplNamespace() {
        return implNS;
    }

    /**
     * Set the implementation namespace
     * 
     * @param ns implementation target namespace
     */
    public void setImplNamespace(String ns) {
        this.implNS = ns;
    }

    /**
     * Returns a vector of methods to export
     * 
     * @return a space separated list of methods to export
     */
    public Vector getAllowedMethods() {
        return allowedMethods;
    }

    /**
     * Add a list of methods to export
     * 
     * @param text 
     */
    public void setAllowedMethods(String text) {

        if (text != null) {
            StringTokenizer tokenizer = new StringTokenizer(text, " ,+");

            if (allowedMethods == null) {
                allowedMethods = new Vector();
            }

            while (tokenizer.hasMoreTokens()) {
                allowedMethods.add(tokenizer.nextToken());
            }
        }
    }

    /**
     * Add a Vector of methods to export
     * 
     * @param allowedMethods a vector of methods to export
     */
    public void setAllowedMethods(Vector allowedMethods) {

        if (this.allowedMethods == null) {
            this.allowedMethods = new Vector();
        }

        this.allowedMethods.addAll(allowedMethods);
    }

    /**
     * Indicates if the emitter will search classes for inherited methods
     * 
     * @return 
     */
    public boolean getUseInheritedMethods() {
        return useInheritedMethods;
    }

    /**
     * Turn on or off inherited method WSDL generation.
     * 
     * @param useInheritedMethods 
     */
    public void setUseInheritedMethods(boolean useInheritedMethods) {
        this.useInheritedMethods = useInheritedMethods;
    }

    /**
     * Add a list of methods NOT to export
     * 
     * @param disallowedMethods vector of method name strings
     */
    public void setDisallowedMethods(Vector disallowedMethods) {

        if (this.disallowedMethods == null) {
            this.disallowedMethods = new Vector();
        }

        this.disallowedMethods.addAll(disallowedMethods);
    }

    /**
     * Add a list of methods NOT to export
     * 
     * @param text space separated list of method names
     */
    public void setDisallowedMethods(String text) {

        if (text != null) {
            StringTokenizer tokenizer = new StringTokenizer(text, " ,+");

            if (disallowedMethods == null) {
                disallowedMethods = new Vector();
            }

            disallowedMethods = new Vector();

            while (tokenizer.hasMoreTokens()) {
                disallowedMethods.add(tokenizer.nextToken());
            }
        }
    }

    /**
     * Return list of methods that should not be exported
     * 
     * @return 
     */
    public Vector getDisallowedMethods() {
        return disallowedMethods;
    }

    /**
     * Adds a list of classes (fully qualified) that will stop the traversal
     * of the inheritance tree if encounter in method or complex type generation
     * 
     * @param stopClasses vector of class name strings
     */
    public void setStopClasses(ArrayList stopClasses) {

        if (this.stopClasses == null) {
            this.stopClasses = new ArrayList();
        }

        this.stopClasses.addAll(stopClasses);
    }

    /**
     * Add a list of classes (fully qualified) that will stop the traversal
     * of the inheritance tree if encounter in method or complex type generation
     * 
     * @param text space separated list of class names
     */
    public void setStopClasses(String text) {

        if (text != null) {
            StringTokenizer tokenizer = new StringTokenizer(text, " ,+");

            if (stopClasses == null) {
                stopClasses = new ArrayList();
            }

            while (tokenizer.hasMoreTokens()) {
                stopClasses.add(tokenizer.nextToken());
            }
        }
    }

    /**
     * Return the list of classes which stop inhertance searches
     * 
     * @return 
     */
    public ArrayList getStopClasses() {
        return stopClasses;
    }

    /**
     * get the packagename to namespace map
     * 
     * @return <code>Map</code>
     */
    public Map getNamespaceMap() {
        return namespaces;
    }

    /**
     * Set the packagename to namespace map with the given map
     * 
     * @param map packagename/namespace <code>Map</code>
     */
    public void setNamespaceMap(Map map) {

        if (map != null) {
            namespaces.putAll(map);
        }
    }

    /**
     * Get the name of the input WSDL
     * 
     * @return name of the input wsdl or null
     */
    public String getInputWSDL() {
        return inputWSDL;
    }

    /**
     * Set the name of the input WSDL
     * 
     * @param inputWSDL the name of the input WSDL
     */
    public void setInputWSDL(String inputWSDL) {
        this.inputWSDL = inputWSDL;
    }

    /**
     * @return the name of the input schema, or null
     */
    public String getInputSchema() {
        return inputSchema;
    }

    /**
     * Set the name of the input schema
     * 
     * @param inputSchema the name of the input schema
     */
    public void setInputSchema(String inputSchema) {
        this.inputSchema = inputSchema;
    }

    /**
     * Returns the String representation of the service endpoint URL
     * 
     * @return String representation of the service endpoint URL
     */
    public String getLocationUrl() {
        return locationUrl;
    }

    /**
     * Set the String representation of the service endpoint URL
     * 
     * @param locationUrl the String representation of the service endpoint URL
     */
    public void setLocationUrl(String locationUrl) {
        this.locationUrl = locationUrl;
    }

    /**
     * Returns the String representation of the interface import location URL
     * 
     * @return String representation of the interface import location URL
     */
    public String getImportUrl() {
        return importUrl;
    }

    /**
     * Set the String representation of the interface location URL
     * for importing
     * 
     * @param importUrl the String representation of the interface
     *                  location URL for importing
     */
    public void setImportUrl(String importUrl) {
        this.importUrl = importUrl;
    }

    /**
     * Returns the String representation of the service port name
     * 
     * @return String representation of the service port name
     */
    public String getServicePortName() {
        return servicePortName;
    }

    /**
     * Set the String representation of the service port name
     * 
     * @param servicePortName the String representation of the service port name
     */
    public void setServicePortName(String servicePortName) {
        this.servicePortName = servicePortName;
    }

    /**
     * Returns the String representation of the service element name
     * 
     * @return String representation of the service element name
     */
    public String getServiceElementName() {
        return serviceElementName;
    }

    /**
     * Set the String representation of the service element name
     * 
     * @param serviceElementName the String representation of the service element name
     */
    public void setServiceElementName(String serviceElementName) {
        this.serviceElementName = serviceElementName;
    }

    /**
     * Returns the String representation of the portType name
     * 
     * @return String representation of the portType name
     */
    public String getPortTypeName() {
        return portTypeName;
    }

    /**
     * Set the String representation of the portType name
     * 
     * @param portTypeName the String representation of the portType name
     */
    public void setPortTypeName(String portTypeName) {
        this.portTypeName = portTypeName;
    }

    /**
     * Returns the String representation of the binding name
     * 
     * @return String representation of the binding name
     */
    public String getBindingName() {
        return bindingName;
    }

    /**
     * Set the String representation of the binding name
     * 
     * @param bindingName the String representation of the binding name
     */
    public void setBindingName(String bindingName) {
        this.bindingName = bindingName;
    }

    /**
     * Returns the target service name
     * 
     * @return the target service name
     */
    public String getTargetService() {
        return targetService;
    }

    /**
     * Set the target service name
     * 
     * @param targetService the target service name
     */
    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    /**
     * Returns the service description
     * 
     * @return service description String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the service description
     * 
     * @param description service description String
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the soapAction option value
     * 
     * @return the String DEFAULT, NONE or OPERATION
     */
    public String getSoapAction() {
        return soapAction;
    }

    /**
     * Sets the soapAction option value
     * 
     * @param value must be DEFAULT, NONE, or OPERATION
     */
    public void setSoapAction(String value) {
        soapAction = value;
    }

    /**
     * Returns the <code>TypeMapping</code> used by the service
     * 
     * @return the <code>TypeMapping</code> used by the service
     */
    public TypeMapping getTypeMapping() {
        return tm;
    }

    /**
     * Sets the <code>TypeMapping</code> used by the service
     * 
     * @param tm the <code>TypeMapping</code> used by the service
     */
    public void setTypeMapping(TypeMapping tm) {
        this.tm = tm;
    }

    /**
     * Returns the <code>defaultTypeMapping</code> used by the service
     * @return the <code>defaultTypeMapping</code> used by the service
     * @deprecated Use getTypeMappingRegistry instead
     */
    public TypeMapping getDefaultTypeMapping() {
        return (TypeMapping) tmr.getDefaultTypeMapping();
    }

    /**
     * Sets the <code>defaultTypeMapping</code> used by the service
     * @param tm the <code>defaultTypeMapping</code> used by the service
     * @deprecated Use setTypeMappingRegistry instead
     */
    public void setDefaultTypeMapping(TypeMapping tm) {
        tmr.registerDefault(tm);
    }

    /**
     * Set the TypeMappingRegistry for this Emitter.
     */
    public void setTypeMappingRegistry(TypeMappingRegistry tmr) {
        this.tmr = tmr;
    }
    /**
     * getStyle
     * 
     * @return Style setting (Style.RPC, Style.DOCUMENT, Style.WRAPPED, etc.)
     */
    public Style getStyle() {
        return style;
    }

    /**
     * setStyle
     * 
     * @param value String representing a style ("document", "rpc", "wrapped")
     *              Note that the case of the string is not important. "document" and "DOCUMENT"
     *              are both treated as document style.
     *              If the value is not a know style, the default setting is used.
     *              See org.apache.axis.constants.Style for a description of the interaction between
     *              Style/Use
     *              <br>NOTE: If style is specified as "wrapped", use is set to literal.
     */
    public void setStyle(String value) {
        setStyle(Style.getStyle(value));
    }

    /**
     * setStyle
     * 
     * @param value Style setting
     */
    public void setStyle(Style value) {

        style = value;

        if (style.equals(Style.WRAPPED)) {
            setUse(Use.LITERAL);
        }
    }

    /**
     * getUse
     * 
     * @return Use setting (Use.ENCODED, Use.LITERAL)
     */
    public Use getUse() {
        return use;
    }

    /**
     * setUse
     * 
     * @param value String representing a use ("literal", "encoded")
     *              Note that the case of the string is not important. "literal" and "LITERAL"
     *              are both treated as literal use.
     *              If the value is not a know use, the default setting is used.
     *              See org.apache.axis.constants.Style for a description of the interaction between
     *              Style/Use
     */
    public void setUse(String value) {
        use = Use.getUse(value);
    }

    /**
     * setUse
     * 
     * @param value Use setting
     */
    public void setUse(Use value) {
        use = value;
    }

    /**
     * setMode (sets style and use)
     * 
     * @param mode 
     * @deprecated (use setStyle and setUse)
     */
    public void setMode(int mode) {

        if (mode == MODE_RPC) {
            setStyle(Style.RPC);
            setUse(Use.ENCODED);
        } else if (mode == MODE_DOCUMENT) {
            setStyle(Style.DOCUMENT);
            setUse(Use.LITERAL);
        } else if (mode == MODE_DOC_WRAPPED) {
            setStyle(Style.WRAPPED);
            setUse(Use.LITERAL);
        }
    }

    /**
     * getMode (gets the mode based on the style setting)
     * 
     * @return returns the mode (-1 if invalid)
     * @deprecated (use getStyle and getUse)
     */
    public int getMode() {

        if (style == Style.RPC) {
            return MODE_RPC;
        } else if (style == Style.DOCUMENT) {
            return MODE_DOCUMENT;
        } else if (style == Style.WRAPPED) {
            return MODE_DOC_WRAPPED;
        }

        return -1;
    }

    /**
     * Method getServiceDesc
     * 
     * @return 
     */
    public ServiceDesc getServiceDesc() {
        return serviceDesc;
    }

    /**
     * Method setServiceDesc
     * 
     * @param serviceDesc 
     */
    public void setServiceDesc(ServiceDesc serviceDesc) {
        this.serviceDesc = serviceDesc;
    }

    /**
     * Return the list of extra classes that the emitter will produce WSDL for.
     * 
     * @return 
     */
    public Class[] getExtraClasses() {
        return extraClasses;
    }

    /**
     * Provide a list of classes which the emitter will produce WSDL
     * type definitions for.
     * 
     * @param extraClasses 
     */
    public void setExtraClasses(Class[] extraClasses) {
        this.extraClasses = extraClasses;
    }

    /**
     * Provide a comma or space seperated list of classes which
     * the emitter will produce WSDL type definitions for.
     * The classes will be added to the current list.
     * 
     * @param text 
     * @throws ClassNotFoundException 
     */
    public void setExtraClasses(String text) throws ClassNotFoundException {

        ArrayList clsList = new ArrayList();

        if (text != null) {
            StringTokenizer tokenizer = new StringTokenizer(text, " ,");

            while (tokenizer.hasMoreTokens()) {
                String clsName = tokenizer.nextToken();

                // Let the caller handler ClassNotFoundException
                Class cls = ClassUtils.forName(clsName);

                clsList.add(cls);
            }
        }

        // Allocate the new array
        Class[] ec;
        int startOffset = 0;

        if (extraClasses != null) {
            ec = new Class[clsList.size() + extraClasses.length];

            // copy existing elements
            for (int i = 0; i < extraClasses.length; i++) {
                Class c = extraClasses[i];

                ec[i] = c;
            }
            startOffset = extraClasses.length;
        } else {
            ec = new Class[clsList.size()];
        }

        // copy the new classes
        for (int i = 0; i < clsList.size(); i++) {
            Class c = (Class) clsList.get(i);

            ec[startOffset + i] = c;
        }

        // set the member variable
        this.extraClasses = ec;
    }

    public void setEmitAllTypes(boolean emitAllTypes) {
        this.emitAllTypes = emitAllTypes;
    }

    /**
     * Return the version message
     * @return message or null if emitter will use the default
     */
    public String getVersionMessage()
    {
        return versionMessage;
    }

    /**
     * Set the version message that appears at the top of the WSDL
     * If not set, we use the default version message.
     * If set to an empty string, no version message will be emitted
     * @param versionMessage the message to emit
     */
    public void setVersionMessage(String versionMessage)
    {
        this.versionMessage = versionMessage;
    }

	/**
     * Return the type qname to java type mapping
     * @return mapping of type qname to its corresponding java type
     */
    public HashMap getQName2ClassMap() {
        return qName2ClassMap;   
    }
}
