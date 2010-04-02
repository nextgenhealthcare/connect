/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.mule.adaptors;

import java.util.regex.Pattern;

import com.webreach.mirth.model.MessageObject;

public class XMLAdaptor extends Adaptor {

    final static Pattern whiteSpacePattern = Pattern.compile("\\s|/");

    protected void populateMessage(boolean emptyFilterAndTransformer) throws AdaptorException {
        messageObject.setRawDataProtocol(MessageObject.Protocol.XML);
        messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
        messageObject.setEncodedDataProtocol(MessageObject.Protocol.XML);
        messageObject.setType("XML");
        messageObject.setTransformedData(source);

        if (emptyFilterAndTransformer) {
            messageObject.setEncodedData(source);
        }

        try {
            messageObject.setSource(new String());
            messageObject.setType(getNodeNameFromXMLString(source));
            messageObject.setVersion("1.0");
        } catch (Exception e) {
            handleException(e);
        }

    }

    /** 
     * Gets the name of the root node in an XML string
     */
    private String getNodeNameFromXMLString(String xml) {
        xml = xml.trim();
        
        // remove processing-instructions or comments
        while (xml.substring(0, 2).equals("<?") || xml.substring(0, 4).equals("<!--")) {
            if (xml.substring(0, 2).equals("<?")) {
                // remove processing-instructions
                xml = xml.substring(xml.indexOf("?>") + 2).trim();
            } else {
                // remove comments
                xml = xml.substring(xml.indexOf("-->") + 3).trim();
            }
        }
        
        // get the node name in the form <[prefix]:nodeName[ (..)][/]>
        String rootNodeName = xml.substring(1, xml.indexOf(">")).trim();
        
        // remove attributes (if any)
        rootNodeName = whiteSpacePattern.split(rootNodeName, 2)[0];
        
        // remove prefix (if any)
        int nsPos = rootNodeName.indexOf(":");
        if (nsPos > 0)
            rootNodeName = rootNodeName.substring(nsPos + 1);

        return rootNodeName;
    }
}
