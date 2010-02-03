/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model.dicom;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.util.MessageVocabulary;

public class DICOMVocabulary extends MessageVocabulary {
    private DICOMReference reference = null;
    private String version;

    public DICOMVocabulary(String version, String type) {
        super(version, type);
        this.version = version;
        reference = DICOMReference.getInstance();
    }

    // For now we are going to use the large hashmap
    public String getDescription(String elementId) {
        return reference.getDescription(elementId, version);
    }

    public MessageObject.Protocol getProtocol() {
        return MessageObject.Protocol.DICOM;
    }
}
