/*
 * Copyright 2002-2004 The Apache Software Foundation.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, 2002, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.wsif.schema;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.xml.WSDLLocator;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.util.WSIFUtils;
import org.apache.wsif.wsdl.WSIFWSDLLocatorImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.ibm.wsdl.util.xml.QNameUtils;

/**
 * A class used for parsing the schema(s) defined in a Definition object. It does not provide
 * full schema parsing. Its main purpose is to help in determining a list off all the types that
 * are defined in schemas either inline in the wsdl document or imported xsd files.
 *
 * @author Owen Burroughs <owenb@apache.org>
 */
public class Parser {

    private static final QName schema1999 =
        new QName(WSIFConstants.NS_URI_1999_SCHEMA_XSD, "schema");
    private static final QName schema2000 =
        new QName(WSIFConstants.NS_URI_2000_SCHEMA_XSD, "schema");
    private static final QName schema2001 =
        new QName(WSIFConstants.NS_URI_2001_SCHEMA_XSD, "schema");

    /**
     * Given a Definition object, populate a Map with all the types defined in the schemas in the definition and
     * their corresponding Java class names.
     * @param def The Definition object representing the wsdl
     * @param table The Map to proulate with xml type -> Java class name (QName -> String) mappings
     * @deprecated Use getAllSchemaTypes in combination with a {@link org.apache.wsif.mapping.WSIFMapper} rather
     * than this method
     */
    public static void getTypeMappings(Definition def, Map table) throws WSIFException {
        getTypeMappings(def, table, true, null);
    }

    /**
     * Given a Definition object, populate a Map with all the types defined in the schemas in the definition and
     * their corresponding Java class names.
     * @param def The Definition object representing the wsdl
     * @param table The Map to proulate with xml type -> Java class name (QName -> String) mappings
     * @param loader A ClassLoader to use in resolving xsd locations
     * @deprecated Use getAllSchemaTypes in combination with a {@link org.apache.wsif.mapping.WSIFMapper} rather
     * than this method
     */
    public static void getTypeMappings(Definition def, Map table, ClassLoader loader) throws WSIFException {
                WSDLLocator     locator = new WSIFWSDLLocatorImpl((String) null, (String) null, loader);
        getTypeMappings(def, table, true, locator);
    }

    /**
     * Given a Definition object, populate a Map with all the types defined in the schemas in the definition and
     * their corresponding Java class names.
     * @param def The Definition object representing the wsdl
     * @param table The Map to proulate with xml type -> Java class name (QName -> String) mappings
     * @param loader A ClassLoader to use in resolving xsd locations
     * @param includeStandardMappings Flag to indicate whether or not standard xsd, soapenc and Apache SOAP mappings
     * should be included in the table
     * @deprecated Use getAllSchemaTypes in combination with a {@link org.apache.wsif.mapping.WSIFMapper} rather
     * than this method
     */
    public static void getTypeMappings(
        Definition def,
        Map table,
        ClassLoader loader,
        boolean includeStandardMappings) throws WSIFException {
                WSDLLocator     locator = new WSIFWSDLLocatorImpl((String) null, (String) null, loader);
        getTypeMappings(def, table, includeStandardMappings, locator);
    }

    /**
     * Given a Definition object, populate a Map with all the types defined in the schemas in the definition and
     * their corresponding Java class names.
     * @param def The Definition object representing the wsdl
     * @param table The Map to proulate with xml type -> Java class name (QName -> String) mappings
     * @param loc WSDLLocator equal or equivalent to that used to locate the original wsdl document
     * @deprecated Use getAllSchemaTypes in combination with a {@link org.apache.wsif.mapping.WSIFMapper} rather
     * than this method
     */
    public static void getTypeMappings(Definition def, Map table, WSDLLocator loc) throws WSIFException {
        getTypeMappings(def, table, true, loc);
    }

    /**
     * Given a Definition object, populate a Map with all the types defined in the schemas in the definition and
     * their corresponding Java class names.
     * @param def The Definition object representing the wsdl
     * @param table The Map to proulate with xml type -> Java class name (QName -> String) mappings
     * @param includeStandardMappings Flag to indicate whether or not standard xsd, soapenc and Apache SOAP mappings
     * should be included in the table
     * @deprecated Use getAllSchemaTypes in combination with a {@link org.apache.wsif.mapping.WSIFMapper} rather
     * than this method
     */
    public static void getTypeMappings(
        Definition def,
        Map table,
        boolean includeStandardMappings) throws WSIFException {
        
        getTypeMappings(def, table, includeStandardMappings, null);
    }

    /**
     * Given a Definition object, populate a Map with all the types defined in the schemas in the definition and
     * their corresponding Java class names.
     * @param def The Definition object representing the wsdl
     * @param table The Map to proulate with xml type -> Java class name (QName -> String) mappings
     * @param includeStandardMappings Flag to indicate whether or not standard xsd, soapenc and Apache SOAP mappings
     * should be included in the table
     * @param loc WSDLLocator equal or equivalent to that used to locate the original wsdl document
     * @deprecated Use getAllSchemaTypes in combination with a {@link org.apache.wsif.mapping.WSIFMapper} rather
     * than this method
     */
    public static void getTypeMappings(
        Definition def,
        Map table,
        boolean includeStandardMappings,
        WSDLLocator loc) throws WSIFException {

                Trc.entry(null, def, table, new Boolean(includeStandardMappings), loc);
                if (loc == null) {
                        loc = new WSIFWSDLLocatorImpl((String) null, (String) null, null);
                }
                                
        ArrayList schemaList = new ArrayList();
        getTypesSchemas(def, schemaList, loc);
                
        Hashtable standards = null;

        if (includeStandardMappings) {
            // set up all standard mappings
            populateWithStandardMappings(
                table,
                WSIFConstants.NS_URI_1999_SCHEMA_XSD,
                true);
            populateWithStandardMappings(
                table,
                WSIFConstants.NS_URI_2000_SCHEMA_XSD,
                false);
            populateWithStandardMappings(
                table,
                WSIFConstants.NS_URI_2001_SCHEMA_XSD,
                false);
        } else {
            // set up all standard mappings in a seperate map for use when resolving arrays
            standards = new Hashtable();
            populateWithStandardMappings(
                standards,
                WSIFConstants.NS_URI_1999_SCHEMA_XSD,
                true);
            populateWithStandardMappings(
                standards,
                WSIFConstants.NS_URI_2000_SCHEMA_XSD,
                false);
            populateWithStandardMappings(
                standards,
                WSIFConstants.NS_URI_2001_SCHEMA_XSD,
                false);
        }

        // Create temporary list to hold types which are arrays. We can then resolve them
        // after resolving all other types
        List arrays = new ArrayList();

        // Create temporary list to hold types which are elements. We can then resolve them
        // after resolving all other types
        List elements = new ArrayList();

        // Iterate through all the schemas found in the wsdl and imports
        Iterator si = schemaList.iterator();
        while (si.hasNext()) {
            Schema ts = (Schema) si.next();
            if (ts != null) {
                // Get all the types defined in this schema
                List types = ts.getTypes();
                Iterator ti = types.iterator();
                while (ti.hasNext()) {
                    SchemaType st = (SchemaType) ti.next();
                    // Ignore null types
                    if (st == null)
                        continue;
                    QName typeName = st.getTypeName();
                    if (typeName == null)
                        continue;

                    if (st.isArray()) {
                        arrays.add(st);
                    } else {
                        // Deal with elements
                        if (st instanceof ElementType) {
                            QName baseType = ((ElementType) st).getElementType();

                            if (baseType != null) {
                                if (((ElementType) st).isNillable()) {
                                        String wrapperClass = getWrapperClassName(baseType);
                                        if (wrapperClass != null) {
                                                table.put(typeName, wrapperClass);
                                                continue;
                                        }
                                }
                                String baseClassName =
                                    (String) table.get(baseType);
                                if (baseClassName == null
                                    && !includeStandardMappings) {
                                    baseClassName =
                                        (String) standards.get(baseType);
                                }
                                if (baseClassName != null) {
                                    table.put(typeName, baseClassName);
                                } else {
                                    elements.add(st);
                                }
                            } else {
                                String className = resolveClassName(typeName);
                                // Distinguish the class for this element from a complexType with the same name
                                // by appending "Element" onto the class name.
                                className = className + "Element";
                                if (className != null) {
                                    table.put(typeName, className);
                                }
                            }
                        } else {
                                // Deal with complexTypes and simpleTypes
                            String className = resolveClassName(typeName);
                            if (className != null) {
                                table.put(typeName, className);
                            }
                        }
                    }
                }
            }
        }

        // Create a temporary list for arrays of arrays so we can resolve them last
        ArrayList multiArrays = new ArrayList();

        // Now we'll resolve any array types that were found
        Iterator ai = arrays.iterator();
        while (ai.hasNext()) {
            SchemaType st = (SchemaType) ai.next();
            // We've already checked that its an array so cut to the chase!
            QName theType = st.getTypeName();
            if (theType == null) continue;
            
            QName arrayType = st.getArrayType();
            if (arrayType != null && theType != null) {
                String baseClass = (String) table.get(arrayType);
                if (baseClass == null && standards != null) {
                    // Check for base class in the standard mappings
                    baseClass = (String) standards.get(arrayType);
                }
                if (baseClass == null) {
                    String lp = arrayType.getLocalPart();
                    if (lp != null && lp.startsWith("ArrayOf")) {
                        // This is an array of an array. Perhaps we've
                        // not mapped the base array yet so re-try this
                        // at the end
                        multiArrays.add(st);
                    }
                    continue;
                }
                // Deal with multidimentional array classes
                String extraDims = "";
                for (int x = 1; x < st.getArrayDimension(); x++) {
                    extraDims += "[";
                }
                if (baseClass != null) {
                    // Check for primitive types
                    if (baseClass.equals("int")) {
                        table.put(theType, extraDims + "[I");
                    } else if (baseClass.equals("float")) {
                        table.put(theType, extraDims + "[F");
                    } else if (baseClass.equals("long")) {
                        table.put(theType, extraDims + "[J");
                    } else if (baseClass.equals("double")) {
                        table.put(theType, extraDims + "[D");
                    } else if (baseClass.equals("boolean")) {
                        table.put(theType, extraDims + "[Z");
                    } else if (baseClass.equals("byte")) {
                        table.put(theType, extraDims + "[B");
                    } else if (baseClass.equals("short")) {
                        table.put(theType, extraDims + "[S");
                    } else if (baseClass.startsWith("[")) {
                        // The base for this array is another array!!
                        String arrayOfBase = "[" + baseClass;
                        table.put(theType, arrayOfBase);
                    } else {
                        String arrayOfBase = extraDims + "[L" + baseClass + ";";
                        table.put(theType, arrayOfBase);
                    }
                }
            }
        }

        // Now we'll resolve any arrays of arrays that are outstanding
        Iterator mi = multiArrays.iterator();
        while (mi.hasNext()) {
            SchemaType st = (SchemaType) mi.next();
            QName theType = st.getTypeName();
            if (theType == null) continue;
            
            QName arrayType = st.getArrayType();
            if (arrayType != null && theType != null) {
                String extraDims = "";
                for (int x = 1; x < st.getArrayDimension(); x++) {
                    extraDims += "[";
                }
                String baseClass = (String) table.get(arrayType);
                if (baseClass != null) {
                    // Base class should be an array
                    if (baseClass.startsWith("[")) {
                        String arrayOfBase = "[" + baseClass;
                        table.put(theType, arrayOfBase);
                    }
                }
            }
        }
        
        // Finally we'll resolve any elements that are outstanding
        Iterator ei = elements.iterator();
        while (ei.hasNext()) {
            SchemaType st = (SchemaType) ei.next();
            QName theType = st.getTypeName();
            if (theType == null)
                continue;

            QName baseType = null;
            if (st instanceof ElementType) {
                baseType = ((ElementType) st).getElementType();
            }
            if (baseType != null) {
                String baseClassName = (String) table.get(baseType);
                if (baseClassName != null) {
                    table.put(theType, baseClassName);
                }
            }
        }
                       
        Trc.exit();
    }

    /**
     * Populate a List with all the top level SchemaType objects (complexTypes, simpleTypes and elements) generated
     * by parsing the schemas associated with a Definition object
     * @param def The Definition object representing the wsdl
     * @param schemaTypes The List to proulate with the SchemaType objects
     * @param loc WSDLLocator equal or equivalent to that used to locate the original wsdl document. This is required in order
     * to resolve imported schemas.
     * @exception A WSIFException is thrown if a problem occurs when parsing the schemas
     */
    public static void getAllSchemaTypes(
        Definition def,
        List schemaTypes,
        WSDLLocator loc)
            throws WSIFException {
        try {
            ArrayList schemas = new ArrayList();
            if (loc == null) {
                loc =
                    new WSIFWSDLLocatorImpl((String) null, (String) null, null);
            }
            Parser.getTypesSchemas(def, schemas, loc);
            Iterator si = schemas.iterator();
            while (si.hasNext()) {
                Schema ts = (Schema) si.next();
                if (ts != null) {
                    // Get all the types defined in this schema
                    List types = ts.getTypes();
                    Iterator ti = types.iterator();
                    while (ti.hasNext()) {
                        SchemaType st = (SchemaType) ti.next();
                        // Ignore null types
                        if (st == null)
                            continue;
                        schemaTypes.add(st);
                    }
                }
            }
        } catch (WSIFException e) {
        }
    }

    /**
     * Populate a map with the standard xml type -> Java class name mappings
     */
    private static void populateWithStandardMappings(
        Map t,
        String schemaURI,
        boolean oneTimeAdds) {

        t.put(new QName(schemaURI, "string"), "java.lang.String");
        t.put(new QName(schemaURI, "integer"), "java.math.BigInteger");
        t.put(new QName(schemaURI, "boolean"), "boolean");
        t.put(new QName(schemaURI, "float"), "float");
        t.put(new QName(schemaURI, "double"), "double");
        t.put(new QName(schemaURI, "base64Binary"), "[B");
        t.put(new QName(schemaURI, "hexBinary"), "[B");
        t.put(new QName(schemaURI, "long"), "long");
        t.put(new QName(schemaURI, "int"), "int");
        t.put(new QName(schemaURI, "short"), "short");
        t.put(new QName(schemaURI, "decimal"), "java.math.BigDecimal");
        t.put(new QName(schemaURI, "byte"), "byte");
        t.put(new QName(schemaURI, "QName"), "javax.xml.namespace.QName");

        // Register dateTime or timeInstant depending on schema
        if (schemaURI.equals(WSIFConstants.NS_URI_2001_SCHEMA_XSD)) {
            t.put(new QName(schemaURI, "dateTime"), "java.util.Calendar");
        } else {
            t.put(new QName(schemaURI, "timeInstant"), "java.util.Calendar");
        }

        // Only add the SOAP-ENC simple types and soap collection class mappings once
        if (oneTimeAdds) {
            // SOAP encoding simple types
            t.put(
                new QName(WSIFConstants.NS_URI_SOAP_ENC, "string"),
                "java.lang.String");
            t.put(
                new QName(WSIFConstants.NS_URI_SOAP_ENC, "boolean"),
                "java.lang.Boolean");
            t.put(
                new QName(WSIFConstants.NS_URI_SOAP_ENC, "float"),
                "java.lang.Float");
            t.put(
                new QName(WSIFConstants.NS_URI_SOAP_ENC, "double"),
                "java.lang.Double");
            t.put(
                new QName(WSIFConstants.NS_URI_SOAP_ENC, "decimal"),
                "java.math.BigDecimal");
            t.put(
                new QName(WSIFConstants.NS_URI_SOAP_ENC, "int"),
                "java.lang.Integer");
            t.put(
                new QName(WSIFConstants.NS_URI_SOAP_ENC, "short"),
                "java.lang.Short");
            t.put(
                new QName(WSIFConstants.NS_URI_SOAP_ENC, "byte"),
                "java.lang.Byte");
            t.put(new QName(WSIFConstants.NS_URI_SOAP_ENC, "base64"), "[B");

            // soap Java collection mappings
            t.put(
                new QName(WSIFConstants.NS_URI_APACHE_SOAP, "Map"),
                "java.util.Map");
            t.put(
                new QName(WSIFConstants.NS_URI_APACHE_SOAP, "Vector"),
                "java.util.Vector");
            t.put(
                new QName(WSIFConstants.NS_URI_APACHE_SOAP, "Hashtable"),
                "java.util.Hashtable");
        }
    }

    /**
     * Get all the schemas defined in the Definition object
     */
    private static void getTypesSchemas(Definition def, List schemas, WSDLLocator loc) throws WSIFException {
        Types types = def.getTypes();
        if (types != null) {
            Iterator extEleIt = types.getExtensibilityElements().iterator();

            while (extEleIt.hasNext()) {
                Object nextEl = extEleIt.next();
                if(!(nextEl instanceof UnknownExtensibilityElement)) {
                  //  continue;
                }
//                UnknownExtensibilityElement typesElement =
//                    (UnknownExtensibilityElement) nextEl;

                //Element schemaEl = typesElement.getElement();
                Element schemaEl;
                
                if(nextEl instanceof javax.wsdl.extensions.schema.Schema) {
                    javax.wsdl.extensions.schema.Schema typesElement = (javax.wsdl.extensions.schema.Schema)nextEl;
                    schemaEl = typesElement.getElement();
                } else if (nextEl instanceof UnknownExtensibilityElement) {
                    UnknownExtensibilityElement typesElement = (UnknownExtensibilityElement) nextEl;
                    schemaEl = typesElement.getElement();
                } else {
                    continue;
                }

                if (QNameUtils.matches(schema2001, schemaEl)
                    || QNameUtils.matches(schema2000, schemaEl)
                    || QNameUtils.matches(schema1999, schemaEl)) {
                    Schema sc = new Schema(schemaEl);
                    schemas.add(sc);
                    String docBase = def.getDocumentBaseURI();
                    if (docBase != null && loc != null) {
                        String[] importsAndIncludes = sc.getImportsAndIncludes();
                        for (int i=0; i<importsAndIncludes.length; i++) {
                            String sl = importsAndIncludes[i];
                            getImportedSchemas(docBase, sl, loc, schemas);
                        }
                    }
                }
            }
        }

        Map imports = def.getImports();

        if (imports != null) {
            Iterator valueIterator = imports.values().iterator();

            while (valueIterator.hasNext()) {
                List importList = (List) valueIterator.next();

                if (importList != null) {
                    Iterator importIterator = importList.iterator();

                    while (importIterator.hasNext()) {
                        Import tempImport = (Import) importIterator.next();

                        if (tempImport != null) {
                            Definition importedDef = tempImport.getDefinition();

                            if (importedDef != null) {
                                getTypesSchemas(importedDef, schemas, loc);
                            } else {
                                    String baseLoc = def.getDocumentBaseURI();
                                    String importLoc = tempImport.getLocationURI();
                                    if (baseLoc != null && importLoc != null && loc != null) {
                                            getImportedSchemas(baseLoc, importLoc, loc, schemas);
                                    }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Get all nested schemas
     */
    private static void getImportedSchemas(String base, String rel, WSDLLocator loc, List schemaList) throws WSIFException  {
        try {
            InputSource inputSource = loc.getImportInputSource(base, rel);
            if (inputSource == null) {
                throw new WSIFException("Unable to read schema file "+rel+" relative to "+base);
            }
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);
            factory.setValidating(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputSource);
            if (inputSource.getCharacterStream() != null) {
                inputSource.getCharacterStream().close();
            } else if (inputSource.getByteStream() != null) {
                inputSource.getByteStream().close();
            }

            Element el = doc.getDocumentElement();
            if (el != null) {
                if (QNameUtils.matches(schema2001, el)
                    || QNameUtils.matches(schema2000, el)
                    || QNameUtils.matches(schema1999, el)) {
                        Schema sc = new Schema(el);
                    schemaList.add(sc);
                    String[] importsAndIncludes = sc.getImportsAndIncludes();
                    String lastURI = loc.getLatestImportURI();
                    for (int i=0; i<importsAndIncludes.length; i++) {
                        String sl = importsAndIncludes[i];
                        getImportedSchemas(lastURI, sl, loc, schemaList);
                    }
                }
            }
        } catch (Exception e) {
                Trc.exception(e);
                if (e instanceof WSIFException) {
                        throw (WSIFException) e;
                } else {
                        throw new WSIFException("Error when getting imported schemas", e);
                }
        }
    }

    /**
     * Generate a Java class name corresponding to an xml type
     */
    private static String resolveClassName(QName qn) {
        String namespace = qn.getNamespaceURI();
        String localPart = qn.getLocalPart();
        String packageName =
            WSIFUtils.getPackageNameFromNamespaceURI(namespace);
        String className = WSIFUtils.getJavaClassNameFromXMLName(localPart);
        if (packageName != null
            && !packageName.equals("")
            && className != null
            && !className.equals("")) {
            return packageName + "." + className;
        }
        return null;
    }
    
    /**
     * Elements which are nillable and are based on xsd simple types should map to
     * the object wrapper version of the corresponding primitive type. This method
     * will return the wrapper class name for a given QName.
     */
    private static String getWrapperClassName(QName qn) {
        if (qn == null) return null;
        String ns = qn.getNamespaceURI();
        if (WSIFConstants.NS_URI_1999_SCHEMA_XSD.equals(ns)
            || WSIFConstants.NS_URI_2000_SCHEMA_XSD.equals(ns)
            || WSIFConstants.NS_URI_2001_SCHEMA_XSD.equals(ns)) {
            String lp = qn.getLocalPart();
            if (lp == null) return null;
            if (lp.equals("int")) {
                return "java.lang.Integer";
            } else if (lp.equals("long")) {
                return "java.lang.Long";
            } else if (lp.equals("float")) {
                return "java.lang.Float";
            } else if (lp.equals("short")) {
                return "java.lang.Short";
            } else if (lp.equals("double")) {
                return "java.lang.Double";
            } else if (lp.equals("boolean")) {
                return "java.lang.Boolean";
            } else if (lp.equals("byte")) {
                return "java.lang.Byte";
            }
        }
        return null;
    }
}
