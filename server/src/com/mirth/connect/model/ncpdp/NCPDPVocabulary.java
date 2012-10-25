/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.ncpdp;

import com.mirth.connect.model.converters.DataTypeFactory;
import com.mirth.connect.model.util.MessageVocabulary;

public class NCPDPVocabulary extends MessageVocabulary {
    private NCPDPReference reference = NCPDPReference.getInstance();
    private String version = "51";

    public NCPDPVocabulary(String version, String type) {
        super(version, type);
        this.version = version;
    }

    // For now we are going to use the large hashmap
    public String getDescription(String elementId) {
        return reference.getDescription(elementId, version);
    }

    public String getDataType() {
        return DataTypeFactory.NCPDP;
    }
}
