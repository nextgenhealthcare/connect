/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.syntax.jedit.tokenmarker.TokenMarker;
import org.w3c.dom.Element;

import com.mirth.connect.client.ui.panels.reference.ReferenceListFactory;
import com.mirth.connect.client.ui.panels.reference.ReferenceListFactory.ListType;
import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.model.attachments.AttachmentHandlerType;
import com.mirth.connect.model.converters.IXMLSerializer;
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
    final public IXMLSerializer getSerializer(SerializerProperties properties) {
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
    final public SerializationType getSerializationType() {
        return getDataTypeDelegate().getSerializationType();
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
     * Get the message vocabulary
     */
    public abstract Class<? extends MessageVocabulary> getVocabulary();

    /**
     * If this data type is binary data, use this method to convert the byte
     * data to a string for the template panel.
     * If this data type is not binary, this method is not used.
     */
    public abstract String getTemplateString(byte[] content) throws Exception;

    /**
     * Get the minimum tree level before the tree panel will not create an extra
     * node if the field does not have child nodes
     */
    public abstract int getMinTreeLevel();

    /**
     * Get the node text for a given element in the tree.
     */
    public String getNodeText(MessageVocabulary vocabulary, Element element) {
        String description = vocabulary.getDescription(element.getNodeName());

        String nodeText;
        if (description != null && description.length() > 0) {
            nodeText = element.getNodeName() + " (" + description + ")";
        } else {
            nodeText = element.getNodeName();
        }

        return nodeText;
    }

    protected final void addConversionTemplates(String namespace, boolean defaultOnly) {
        Map<String, ArrayList<CodeTemplate>> references = ReferenceListFactory.getInstance().getReferences();
        List<CodeTemplate> conversionTemplates = references.get(ListType.CONVERSION.getValue());
        String pluginName = getDataTypeDelegate().getName();
        String displayName = getDisplayName();
        namespace = namespace == null ? "" : "\n" + namespace;

        conversionTemplates.add(new CodeTemplate("Convert " + displayName + " to XML (default parameters)", "Converts an encoded " + displayName + " string to XML with the default serializer parameters.", "SerializerFactory.getSerializer('" + pluginName + "').toXML(message);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        if (!defaultOnly) {
            conversionTemplates.add(new CodeTemplate("Convert " + displayName + " to XML (custom parameters)", "Converts an encoded " + displayName + " string to XML with custom serializer parameters. " + getMapKeysToolTipText(), "var serializationProperties = SerializerFactory.getDefaultSerializationProperties('" + pluginName + "');\nSerializerFactory.getSerializer('" + pluginName + "', serializationProperties, null).toXML(message);" + namespace, CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        }

        conversionTemplates.add(new CodeTemplate("Convert XML to " + displayName + " (default parameters)", "Converts an XML string to " + displayName + " with the default serializer parameters.", "SerializerFactory.getSerializer('" + pluginName + "').fromXML(message);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        if (!defaultOnly) {
            conversionTemplates.add(new CodeTemplate("Convert XML to " + displayName + " (custom parameters)", "Converts an XML string to " + displayName + " with custom serializer parameters. " + getMapKeysToolTipText(), "var deserializationProperties = SerializerFactory.getDefaultDeserializationProperties('" + pluginName + "');\nSerializerFactory.getSerializer('" + pluginName + "', null, deserializationProperties).fromXML(message);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        }
    }

    protected final String getMapKeysToolTipText() {
        StringBuilder builder = new StringBuilder("The serialization and deserialization properties are stored as maps, with the following keys:<br/><br/>");
        DataTypeProperties dataTypeProperties = getDataTypeDelegate().getDefaultProperties();

        builder.append("Serialization:<br/>");
        for (String key : dataTypeProperties.getSerializationProperties().getPropertyDescriptors().keySet()) {
            builder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            builder.append(key);
            builder.append("<br/>");
        }

        if (dataTypeProperties.getDeserializationProperties() != null) {
            builder.append("<br/>Deserialization:<br/>");
            for (String key : dataTypeProperties.getDeserializationProperties().getPropertyDescriptors().keySet()) {
                builder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                builder.append(key);
                builder.append("<br/>");
            }
        }

        return builder.toString();
    }
}
