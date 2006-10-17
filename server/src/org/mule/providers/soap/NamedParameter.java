/* 
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/NamedParameter.java,v 1.4 2005/10/17 14:52:55 rossmason Exp $
 * $Revision: 1.4 $
 * $Date: 2005/10/17 14:52:55 $
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

import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

/**
 * Representation of a Named parameter in a soap or XML-RPC call
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.4 $
 */
public class NamedParameter {

    public final int MODE_IN = 0;
    public final int MODE_OUT = 1;
    public final int MODE_INOUT = 2;

    public static final String URI_1999_SCHEMA_XSD = "http://www.w3.org/1999/XMLSchema";
    public static final String URI_2000_SCHEMA_XSD = "http://www.w3.org/2000/10/XMLSchema";
    public static final String URI_2001_SCHEMA_XSD = "http://www.w3.org/2001/XMLSchema";

    public static final String URI_DEFAULT_SCHEMA_XSD = URI_2001_SCHEMA_XSD;

    public static final QName XSD_STRING = new QName(URI_DEFAULT_SCHEMA_XSD, "string");
    public static final QName XSD_BOOLEAN = new QName(URI_DEFAULT_SCHEMA_XSD, "boolean");
    public static final QName XSD_DOUBLE = new QName(URI_DEFAULT_SCHEMA_XSD, "double");
    public static final QName XSD_FLOAT = new QName(URI_DEFAULT_SCHEMA_XSD, "float");
    public static final QName XSD_INT = new QName(URI_DEFAULT_SCHEMA_XSD, "int");
    public static final QName XSD_INTEGER = new QName(URI_DEFAULT_SCHEMA_XSD, "integer");
    public static final QName XSD_LONG = new QName(URI_DEFAULT_SCHEMA_XSD, "long");
    public static final QName XSD_SHORT = new QName(URI_DEFAULT_SCHEMA_XSD, "short");
    public static final QName XSD_BYTE = new QName(URI_DEFAULT_SCHEMA_XSD, "byte");
    public static final QName XSD_DECIMAL = new QName(URI_DEFAULT_SCHEMA_XSD, "decimal");
    public static final QName XSD_BASE64 = new QName(URI_DEFAULT_SCHEMA_XSD, "base64Binary");
    public static final QName XSD_HEXBIN = new QName(URI_DEFAULT_SCHEMA_XSD, "hexBinary");
    public static final QName XSD_ANYSIMPLETYPE = new QName(URI_DEFAULT_SCHEMA_XSD, "anySimpleType");
    public static final QName XSD_ANYTYPE = new QName(URI_DEFAULT_SCHEMA_XSD, "anyType");
    public static final QName XSD_ANY = new QName(URI_DEFAULT_SCHEMA_XSD, "any");
    public static final QName XSD_QNAME = new QName(URI_DEFAULT_SCHEMA_XSD, "QName");
    public static final QName XSD_DATETIME = new QName(URI_DEFAULT_SCHEMA_XSD, "dateTime");
    public static final QName XSD_DATE = new QName(URI_DEFAULT_SCHEMA_XSD, "date");
    public static final QName XSD_TIME = new QName(URI_DEFAULT_SCHEMA_XSD, "time");
    public static final QName XSD_TIMEINSTANT1999 = new QName(URI_1999_SCHEMA_XSD, "timeInstant");
    public static final QName XSD_TIMEINSTANT2000 = new QName(URI_2000_SCHEMA_XSD, "timeInstant");

    public static final QName XSD_NORMALIZEDSTRING = new QName(URI_2001_SCHEMA_XSD, "normalizedString");
    public static final QName XSD_TOKEN = new QName(URI_2001_SCHEMA_XSD, "token");

    public static final QName XSD_UNSIGNEDLONG = new QName(URI_DEFAULT_SCHEMA_XSD, "unsignedLong");
    public static final QName XSD_UNSIGNEDINT = new QName(URI_DEFAULT_SCHEMA_XSD, "unsignedInt");
    public static final QName XSD_UNSIGNEDSHORT = new QName(URI_DEFAULT_SCHEMA_XSD, "unsignedShort");
    public static final QName XSD_UNSIGNEDBYTE = new QName(URI_DEFAULT_SCHEMA_XSD, "unsignedByte");
    public static final QName XSD_POSITIVEINTEGER = new QName(URI_DEFAULT_SCHEMA_XSD, "positiveInteger");
    public static final QName XSD_NEGATIVEINTEGER = new QName(URI_DEFAULT_SCHEMA_XSD, "negativeInteger");
    public static final QName XSD_NONNEGATIVEINTEGER = new QName(URI_DEFAULT_SCHEMA_XSD, "nonNegativeInteger");
    public static final QName XSD_NONPOSITIVEINTEGER = new QName(URI_DEFAULT_SCHEMA_XSD, "nonPositiveInteger");

    public static final QName XSD_YEARMONTH = new QName(URI_DEFAULT_SCHEMA_XSD, "gYearMonth");
    public static final QName XSD_MONTHDAY = new QName(URI_DEFAULT_SCHEMA_XSD, "gMonthDay");
    public static final QName XSD_YEAR = new QName(URI_DEFAULT_SCHEMA_XSD, "gYear");
    public static final QName XSD_MONTH = new QName(URI_DEFAULT_SCHEMA_XSD, "gMonth");
    public static final QName XSD_DAY = new QName(URI_DEFAULT_SCHEMA_XSD, "gDay");
    public static final QName XSD_DURATION = new QName(URI_DEFAULT_SCHEMA_XSD, "duration");

    public static final QName XSD_NAME = new QName(URI_DEFAULT_SCHEMA_XSD, "Name");
    public static final QName XSD_NCNAME = new QName(URI_DEFAULT_SCHEMA_XSD, "NCName");
    public static final QName XSD_NMTOKEN = new QName(URI_DEFAULT_SCHEMA_XSD, "NMTOKEN");
    public static final QName XSD_NMTOKENS = new QName(URI_DEFAULT_SCHEMA_XSD, "NMTOKENS");
    public static final QName XSD_NOTATION = new QName(URI_DEFAULT_SCHEMA_XSD, "NOTATION");
    public static final QName XSD_ENTITY = new QName(URI_DEFAULT_SCHEMA_XSD, "ENTITY");
    public static final QName XSD_ENTITIES = new QName(URI_DEFAULT_SCHEMA_XSD, "ENTITIES");
    public static final QName XSD_IDREF = new QName(URI_DEFAULT_SCHEMA_XSD, "IDREF");
    public static final QName XSD_IDREFS = new QName(URI_DEFAULT_SCHEMA_XSD, "IDREFS");
    public static final QName XSD_ANYURI = new QName(URI_DEFAULT_SCHEMA_XSD, "anyURI");
    public static final QName XSD_LANGUAGE = new QName(URI_DEFAULT_SCHEMA_XSD, "language");
    public static final QName XSD_ID = new QName(URI_DEFAULT_SCHEMA_XSD, "ID");
    public static final QName XSD_SCHEMA = new QName(URI_DEFAULT_SCHEMA_XSD, "schema");

    private QName name;
    private QName type;
    private ParameterMode mode;

    public NamedParameter(QName name, QName type, String mode) {
        this.name = name;
        this.type = type;
        setMode(mode);
    }


    public NamedParameter(QName name, QName type, ParameterMode mode) {
        this.name = name;
        this.type = type;
        this.mode = mode;
    }

    public NamedParameter(String name, String type, ParameterMode mode) {
        this.name = new QName(name);
        this.type = createQName(name);
        this.mode = mode;
    }

    protected void setMode(String mode)
    {
        mode = mode.toLowerCase().trim();
        if(mode.equals("in")) {
            this.mode = ParameterMode.IN;
        } else if(mode.equals("out")) {
            this.mode = ParameterMode.OUT;
        } else if(mode.equals("inout")) {
            this.mode = ParameterMode.INOUT;
        } else {
            throw new IllegalArgumentException(new Message(Messages.VALUE_X_IS_INVALID_FOR_X, mode, "mode").toString());
        }
    }
    public QName getName() {
        return name;
    }

    public QName getType() {
        return type;
    }

    public ParameterMode getMode() {
        return mode;
    }

    public static QName createQName(String value) {
        return new QName(URI_DEFAULT_SCHEMA_XSD, value);
    }
}
