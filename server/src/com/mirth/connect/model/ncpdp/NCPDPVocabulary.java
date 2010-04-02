/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model.ncpdp;

import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.util.MessageVocabulary;

public class NCPDPVocabulary extends MessageVocabulary {
    private NCPDPReference reference = null;

    public NCPDPVocabulary(String version, String type) {
        super(version, type);
        reference = NCPDPReference.getInstance();
    }

    // For now we are going to use the large hashmap
    // TODO: 1.4.1 - Use hl7 model XML from JAXB to generate vocab in real-time
    public String getDescription(String elementId) {
        return reference.getDescription(elementId);
    }

    public Protocol getProtocol() {
        return Protocol.NCPDP;
    }
}
