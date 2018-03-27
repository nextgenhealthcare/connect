/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import org.syntax.jedit.tokenmarker.TokenMarker;
import org.w3c.dom.Element;

import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.model.attachments.AttachmentHandlerType;
import com.mirth.connect.model.converters.IMessageSerializer;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.model.util.MessageVocabulary;

public abstract class DataTypeClientPlugin extends ClientPlugin {

    public DataTypeClientPlugin(String name) {
        super(name);
    }

    /**
     * Get an instance of the data type's serializer with the given properties
     */
    final public IMessageSerializer getSerializer(SerializerProperties properties) {
        return getDataTypeDelegate().getSerializer(properties);
    }

    /**
     * Indicates if the data type is in binary format
     */
    final public boolean isBinary() {
        return getDataTypeDelegate().isBinary();
    }

    /**
     * Get the serialization type
     */
    final public SerializationType getDefaultSerializationType() {
        return getDataTypeDelegate().getDefaultSerializationType();
    }

    /**
     * Get the default properties of the data type. Must not return null.
     */
    final public DataTypeProperties getDefaultProperties() {
        return getDataTypeDelegate().getDefaultProperties();
    }

    /**
     * Get the data type delegate that is used for client/server shared methods
     */
    protected abstract DataTypeDelegate getDataTypeDelegate();

    /**
     * Get the display name that is shown in the client UI
     */
    public abstract String getDisplayName();

    /**
     * Get the default attachment handler type
     */
    public abstract AttachmentHandlerType getDefaultAttachmentHandlerType();

    /**
     * Get the token marker used by the client UI
     */
    public abstract TokenMarker getTokenMarker();

    /**
     * Get the token marker used by the client UI based on the dataTypeProperties
     * @param dataTypeProperties
     * @return TokenMarker
     */
    public TokenMarker getTokenMarker(DataTypeProperties dataTypeProperties) {
        return getTokenMarker();
    }
    
    /**
     * Get the message vocabulary
     */
    public abstract Class<? extends MessageVocabulary> getVocabulary();

    /**
     * If this data type is binary data, use this method to convert the byte data to a string for
     * the template panel. If this data type is not binary, this method is not used.
     */
    public abstract String getTemplateString(byte[] content) throws Exception;

    /**
     * Get the minimum tree level before the tree panel will not create an extra node if the field
     * does not have child nodes
     */
    public abstract int getMinTreeLevel();

    /**
     * Get the node text for a given element in the tree.
     */
    public String getNodeText(MessageVocabulary vocabulary, Element element) {
        return getNodeText(vocabulary, element.getNodeName());
    }

    /**
     * Get the node text for a given element in the tree.
     */
    public String getNodeText(MessageVocabulary vocabulary, String nodeName) {
        String description = vocabulary.getDescription(nodeName);

        String nodeText;
        if (description != null && description.length() > 0) {
            nodeText = nodeName + " (" + description + ")";
        } else {
            nodeText = nodeName;
        }

        return nodeText;
    }
}
