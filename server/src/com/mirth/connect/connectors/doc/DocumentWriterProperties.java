/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.doc;

import java.util.Properties;

import com.mirth.connect.model.ComponentProperties;

public class DocumentWriterProperties implements ComponentProperties {
    public static final String name = "Document Writer";

    public static final String DATATYPE = "DataType";
    public static final String FILE_DIRECTORY = "host";
    public static final String FILE_NAME = "outputPattern";
    public static final String DOCUMENT_TYPE = "documentType";
    public static final String DOCUMENT_PASSWORD_PROTECTED = "encrypt";
    public static final String DOCUMENT_PASSWORD = "password";
    public static final String FILE_CONTENTS = "template";

    public Properties getDefaults() {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(FILE_DIRECTORY, "");
        properties.put(FILE_NAME, "");
        properties.put(DOCUMENT_PASSWORD_PROTECTED, "0");
        properties.put(DOCUMENT_PASSWORD, "");
        properties.put(FILE_CONTENTS, "");
        properties.put(DOCUMENT_TYPE, "pdf");
        return properties;
    }

    public static String getInformation(Properties properties) {
        String info = "";
        if (!properties.getProperty(DOCUMENT_PASSWORD_PROTECTED).equals("0")) {
            info = "Encrypted ";
        }
        info += properties.get(DOCUMENT_TYPE) + " Document Type Result Written To: " + properties.get(FILE_DIRECTORY) + "/" + properties.getProperty(FILE_NAME);
        return info;
    }
}
