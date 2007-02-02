/* 
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/SoapMethod.java,v 1.8 2005/10/17 17:40:51 rossmason Exp $
 * $Revision: 1.8 $
 * $Date: 2005/10/17 17:40:51 $
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */
package org.mule.providers.soap;

import org.mule.config.converters.QNameConverter;
import org.mule.util.ClassHelper;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A soap method representation where the parameters are named
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.8 $
 */
public class SoapMethod {

    private QName name;
    private List namedParameters = new ArrayList();
    private QName returnType;
    private Class returnClass = Object.class;
    private static QNameConverter converter = new QNameConverter();

    public SoapMethod(String methodName, String paramsString) throws ClassNotFoundException {
        this((QName)converter.convert(QName.class, methodName), paramsString);
    }
    /**
     * Creates a Soap Method using the param string set in the MUle configuration
     * file
     * @param methodName the name of the method
     * @param params the param string to parse
     */
    public SoapMethod(String methodName, List params) throws ClassNotFoundException
    {
         this((QName)converter.convert(QName.class, methodName), params);
    }

    public SoapMethod(QName methodName, String paramsString) throws ClassNotFoundException {
         name =  methodName;
        List params = new ArrayList();
        for (StringTokenizer stringTokenizer = new StringTokenizer(paramsString, ","); stringTokenizer.hasMoreTokens();) {
            params.add(stringTokenizer.nextToken().trim());
        }
        initParams(params);
    }

    public SoapMethod(QName methodName, List params) throws ClassNotFoundException
    {
        name = methodName;
        initParams(params);
    }

    private void initParams(List params) throws ClassNotFoundException {

        NamedParameter param;
        for (Iterator iterator = params.iterator(); iterator.hasNext();) {
            String s = (String) iterator.next();

            for (StringTokenizer tokenizer = new StringTokenizer(s, ";"); tokenizer.hasMoreTokens();) {
                String name = tokenizer.nextToken();
                String type = tokenizer.nextToken();
                if(name.equalsIgnoreCase("return")) {
                    returnType = NamedParameter.createQName(type);
                } else if(name.equalsIgnoreCase("returnClass")) {
                    returnClass = ClassHelper.loadClass(type, getClass());
                } else {
                    String mode = tokenizer.nextToken();
                    QName paramName = null;
                    if(name.startsWith("qname{")) {
                        paramName = (QName)converter.convert(QName.class, name);
                    } else {
                        paramName = new QName(getName().getNamespaceURI(), name, getName().getPrefix());
                    }
                    QName qtype = null;
                    if(type.startsWith("qname{")) {
                        qtype = (QName)converter.convert(QName.class, type);
                    } else {
                        qtype = NamedParameter.createQName(type);
                    }
                    param = new NamedParameter(paramName, qtype, mode);
                    addNamedParameter(param);
                }
            }
        }
    }
    public SoapMethod(QName name) {
        this.name = name;
        this.returnType = null;
    }

    public SoapMethod(QName name, QName returnType) {
        this.name = name;
        this.returnType = returnType;
    }

    public SoapMethod(QName name, QName returnType, Class returnClass) {
        this.name = name;
        this.returnType = returnType;
        this.returnClass = returnClass;
    }

     public SoapMethod(QName name, Class returnClass) {
        this.name = name;
        this.returnClass = returnClass;
    }

    public SoapMethod(QName name, List namedParameters, QName returnType) {
        this.name = name;
        this.namedParameters = namedParameters;
        this.returnType = returnType;
    }

    public void addNamedParameter(NamedParameter param) {
        namedParameters.add(param);
    }

    public NamedParameter addNamedParameter(QName name, QName type, String mode) {
        NamedParameter param = new NamedParameter(name, type, mode);
        namedParameters.add(param);
        return param;
    }

    public NamedParameter addNamedParameter(QName name, QName type, ParameterMode mode) {
        NamedParameter param = new NamedParameter(name, type, mode);
        namedParameters.add(param);
        return param;
    }

    public void removeNamedParameter(NamedParameter param) {
        namedParameters.remove(param);
    }

    public QName getName() {
        return name;
    }

    public List getNamedParameters() {
        return namedParameters;
    }

    public QName getReturnType() {
        return returnType;
    }

    public void setReturnType(QName returnType) {
        this.returnType = returnType;
    }

    public Class getReturnClass() {
        return returnClass;
    }

    public void setReturnClass(Class returnClass) {
        this.returnClass = returnClass;
    }
}
