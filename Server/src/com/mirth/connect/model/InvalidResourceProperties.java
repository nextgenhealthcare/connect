/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.mirth.connect.userutil.XmlUtil;

public class InvalidResourceProperties extends ResourceProperties {

    private String propertiesXml;
    private Throwable cause;

    public InvalidResourceProperties(String preUnmarshalXml, DonkeyElement properties, Throwable cause) {
        super(null, null);

        if (preUnmarshalXml == null || properties == null) {
            throw new SerializerException("Could not create invalid resource properties. The properties element or XML is null.");
        }

        DonkeyElement pluginPointName = properties.getChildElement("pluginPointName");
        DonkeyElement type = properties.getChildElement("type");
        DonkeyElement id = properties.getChildElement("id");
        DonkeyElement name = properties.getChildElement("name");

        if (pluginPointName == null || type == null || name == null || id == null) {
            throw new SerializerException("Could not create invalid resource properties. The plugin point, type, name, or ID is missing.");
        }

        try {
            setPluginPointName(pluginPointName.getTextContent());
            setType(type.getTextContent());
            setId(id.getTextContent());
            super.setName(name.getTextContent());
            this.propertiesXml = preUnmarshalXml;
            this.cause = cause;
        } catch (Exception e) {
            throw new SerializerException(e);
        }
    }

    public String getPropertiesXml() {
        return propertiesXml;
    }

    public Throwable getCause() {
        return cause;
    }

    @Override
    public void setName(String name) {
        if (propertiesXml != null) {
            try {
                DonkeyElement root = new DonkeyElement(propertiesXml);
                DonkeyElement child = root.getChildElement("name");
                if (child != null) {
                    child.setTextContent(name);
                }
                propertiesXml = root.toXml();
            } catch (Exception e) {
            }
        }
        super.setName(name);
    }

    @Override
    public String toString() {
        return "<html>" + XmlUtil.encode(super.toString()) + " <b>(Invalid)</b></html>";
    }
}