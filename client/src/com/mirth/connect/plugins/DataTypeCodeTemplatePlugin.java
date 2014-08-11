/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mirth.connect.client.ui.panels.reference.ReferenceListFactory.ListType;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.DataTypeProperties;

public abstract class DataTypeCodeTemplatePlugin extends CodeTemplatePlugin {

    public DataTypeCodeTemplatePlugin(String name) {
        super(name);
    }

    @Override
    public String getPluginPointName() {
        return pluginName + " Code Template Plugin";
    }

    protected abstract DataTypeDelegate getDataTypeDelegate();

    protected abstract String getDisplayName();

    protected boolean isDefaultOnly() {
        return false;
    }

    @Override
    public Map<String, List<CodeTemplate>> getReferenceItems() {
        Map<String, List<CodeTemplate>> referenceItems = new HashMap<String, List<CodeTemplate>>();
        List<CodeTemplate> conversionTemplates = new ArrayList<CodeTemplate>();
        String pluginName = getDataTypeDelegate().getName();

        conversionTemplates.add(new CodeTemplate("Convert " + getDisplayName() + " to XML (default parameters)", "Converts an encoded " + getDisplayName() + " string to XML with the default serializer parameters.", "SerializerFactory.getSerializer('" + pluginName + "').toXML(message);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        if (!isDefaultOnly()) {
            conversionTemplates.add(new CodeTemplate("Convert " + getDisplayName() + " to XML (custom parameters)", "Converts an encoded " + getDisplayName() + " string to XML with custom serializer parameters. " + getMapKeysToolTipText(), "var serializationProperties = SerializerFactory.getDefaultSerializationProperties('" + pluginName + "');\nSerializerFactory.getSerializer('" + pluginName + "', serializationProperties, null).toXML(message);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        }

        conversionTemplates.add(new CodeTemplate("Convert XML to " + getDisplayName() + " (default parameters)", "Converts an XML string to " + getDisplayName() + " with the default serializer parameters.", "SerializerFactory.getSerializer('" + pluginName + "').fromXML(message);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        if (!isDefaultOnly() && getDataTypeDelegate().getDefaultProperties().getDeserializationProperties() != null) {
            conversionTemplates.add(new CodeTemplate("Convert XML to " + getDisplayName() + " (custom parameters)", "Converts an XML string to " + getDisplayName() + " with custom serializer parameters. " + getMapKeysToolTipText(), "var deserializationProperties = SerializerFactory.getDefaultDeserializationProperties('" + pluginName + "');\nSerializerFactory.getSerializer('" + pluginName + "', null, deserializationProperties).fromXML(message);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        }

        referenceItems.put(ListType.CONVERSION.getValue(), conversionTemplates);
        return referenceItems;
    }

    private String getMapKeysToolTipText() {
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

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}
}