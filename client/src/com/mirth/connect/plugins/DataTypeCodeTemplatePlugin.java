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

import com.mirth.connect.client.ui.reference.Category;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateContextSet;
import com.mirth.connect.model.codetemplates.CodeTemplateProperties.CodeTemplateType;
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

        conversionTemplates.add(new CodeTemplate("Convert " + getDisplayName() + " to XML (default parameters)", CodeTemplateType.DRAG_AND_DROP_CODE, CodeTemplateContextSet.getGlobalContextSet(), "SerializerFactory.getSerializer('" + pluginName + "').toXML(message);", "Converts an encoded " + getDisplayName() + " string to XML with the default serializer parameters."));
        if (!isDefaultOnly()) {
            conversionTemplates.add(new CodeTemplate("Convert " + getDisplayName() + " to XML (custom parameters)", CodeTemplateType.DRAG_AND_DROP_CODE, CodeTemplateContextSet.getGlobalContextSet(), "var serializationProperties = SerializerFactory.getDefaultSerializationProperties('" + pluginName + "');\nSerializerFactory.getSerializer('" + pluginName + "', serializationProperties, null).toXML(message);", "Converts an encoded " + getDisplayName() + " string to XML with custom serializer parameters. " + getMapKeysToolTipText()));
        }

        conversionTemplates.add(new CodeTemplate("Convert XML to " + getDisplayName() + " (default parameters)", CodeTemplateType.DRAG_AND_DROP_CODE, CodeTemplateContextSet.getGlobalContextSet(), "SerializerFactory.getSerializer('" + pluginName + "').fromXML(message);", "Converts an XML string to " + getDisplayName() + " with the default serializer parameters."));
        if (!isDefaultOnly() && getDataTypeDelegate().getDefaultProperties().getDeserializationProperties() != null) {
            conversionTemplates.add(new CodeTemplate("Convert XML to " + getDisplayName() + " (custom parameters)", CodeTemplateType.DRAG_AND_DROP_CODE, CodeTemplateContextSet.getGlobalContextSet(), "var deserializationProperties = SerializerFactory.getDefaultDeserializationProperties('" + pluginName + "');\nSerializerFactory.getSerializer('" + pluginName + "', null, deserializationProperties).fromXML(message);", "Converts an XML string to " + getDisplayName() + " with custom serializer parameters. " + getMapKeysToolTipText()));
        }

        referenceItems.put(Category.CONVERSION.toString(), conversionTemplates);
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