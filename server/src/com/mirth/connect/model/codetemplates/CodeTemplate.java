/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.codetemplates;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.text.WordUtils;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.mirth.connect.model.Cacheable;
import com.mirth.connect.model.codetemplates.CodeTemplateProperties.CodeTemplateType;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.util.CodeTemplateUtil;
import com.mirth.connect.util.CodeTemplateUtil.CodeTemplateDocumentation;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("codeTemplate")
public class CodeTemplate implements Serializable, Migratable, Purgable, Cacheable<CodeTemplate> {

    public static final String DEFAULT_CODE = "/**\n\tModify the description here. Modify the function name and parameters as needed. One function per\n\ttemplate is recommended; create a new code template for each new function.\n\n\t@param {String} arg1 - arg1 description\n\t@return {String} return description\n*/\nfunction new_function1(arg1) {\n\t// TODO: Enter code here\n}";

    private String id;
    private String name;
    private Integer revision;
    private Calendar lastModified;
    private CodeTemplateContextSet contextSet;
    private String code;
    private CodeTemplateProperties properties;

    private transient String description;
    private transient CodeTemplateFunctionDefinition functionDefinition;

    public CodeTemplate(String id) {
        this.id = id;
    }

    public CodeTemplate(String name, CodeTemplateType type, CodeTemplateContextSet contextSet, String code, String description) {
        this(name, new BasicCodeTemplateProperties(type), contextSet, addComment(code, description));
    }

    public CodeTemplate(String name, CodeTemplateProperties properties, CodeTemplateContextSet contextSet, String code, String description) {
        this(name, properties, contextSet, addComment(code, description));
    }

    public CodeTemplate(String name, CodeTemplateType type, CodeTemplateContextSet contextSet, String code) {
        this(name, new BasicCodeTemplateProperties(type), contextSet, code);
    }

    public CodeTemplate(String name, CodeTemplateProperties properties, CodeTemplateContextSet contextSet, String code) {
        this(UUID.randomUUID().toString());
        this.name = name;
        this.properties = properties;
        this.contextSet = contextSet;
        setCode(code);
    }

    public CodeTemplate(CodeTemplate codeTemplate) {
        id = codeTemplate.getId();
        name = codeTemplate.getName();
        revision = codeTemplate.getRevision();
        lastModified = codeTemplate.getLastModified();
        if (codeTemplate.getContextSet() != null) {
            contextSet = new CodeTemplateContextSet(codeTemplate.getContextSet());
        }
        setCode(codeTemplate.getCode());
        if (codeTemplate.getProperties() != null) {
            properties = codeTemplate.getProperties().clone();
        }
    }

    public static CodeTemplate getDefaultCodeTemplate(String name) {
        return new CodeTemplate(name, CodeTemplateType.FUNCTION, CodeTemplateContextSet.getConnectorContextSet(), DEFAULT_CODE);
    }

    private static String addComment(String code, String description) {
        if (StringUtils.isNotBlank(description)) {
            return new StringBuilder("/**\n\t").append(WordUtils.wrap(description, 80, "\n\t", false)).append("\n*/\n").append(code).toString();
        }
        return code;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public Calendar getLastModified() {
        return lastModified;
    }

    public void setLastModified(Calendar lastModified) {
        this.lastModified = lastModified;
    }

    public CodeTemplateType getType() {
        return properties.getType();
    }

    public boolean isAddToScripts() {
        return properties.getType() == CodeTemplateType.FUNCTION || properties.getType() == CodeTemplateType.COMPILED_CODE;
    }

    public CodeTemplateContextSet getContextSet() {
        return contextSet;
    }

    public void setContextSet(CodeTemplateContextSet contextSet) {
        this.contextSet = contextSet;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        updateDocumentation();
    }

    public CodeTemplateProperties getProperties() {
        return properties;
    }

    public void setProperties(CodeTemplateProperties properties) {
        this.properties = properties;
    }

    @Override
    public CodeTemplate cloneIfNeeded() {
        return new CodeTemplate(this);
    }

    public String getDescription() {
        if (description == null) {
            updateDocumentation();
        }
        return description;
    }

    public CodeTemplateFunctionDefinition getFunctionDefinition() {
        if (functionDefinition == null) {
            updateDocumentation();
        }
        return functionDefinition;
    }

    private void updateDocumentation() {
        String description = null;
        CodeTemplateFunctionDefinition functionDefinition = null;

        if (StringUtils.isNotBlank(code)) {
            CodeTemplateDocumentation documentation = CodeTemplateUtil.getDocumentation(code);
            description = documentation.getDescription();
            functionDefinition = documentation.getFunctionDefinition();
        }

        this.description = description;
        this.functionDefinition = functionDefinition;
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public void migrate3_2_0(DonkeyElement element) {}

    @Override
    public void migrate3_3_0(DonkeyElement element) {
        element.addChildElement("revision", "1");

        try {
            element.addChildElementFromXml(ObjectXMLSerializer.getInstance().serialize(Calendar.getInstance())).setNodeName("lastModified");
        } catch (DonkeyElementException e) {
            throw new SerializerException("Failed to migrate code template last modified date.", e);
        }

        String type = element.getChildElement("type").getTextContent();
        if (type.equals("CODE") || type.equals("VARIABLE")) {
            element.getChildElement("type").setTextContent("DRAG_AND_DROP_CODE");
        }

        DonkeyElement codeElement = element.getChildElement("code");
        String code = StringUtils.trim(codeElement.getTextContent());
        String toolTip = StringUtils.trim(element.removeChild("tooltip").getTextContent());

        if (StringUtils.isNotBlank(toolTip)) {
            if (code.startsWith("/**")) {
                // Code already has a documentation block, so put the tooltip inside it
                int index = StringUtils.indexOfAnyBut(code.substring(1), '*') + 1;
                StringBuilder builder = new StringBuilder(code.substring(0, index)).append("\n\t").append(WordUtils.wrap(toolTip, 100, "\n\t", false)).append('\n');
                String remaining = code.substring(index);
                if (StringUtils.indexOfAnyBut(remaining.trim(), '*', '/') == 0) {
                    builder.append("\n\t");
                }
                code = builder.append(remaining).toString();
            } else {
                // Add a new documentation block
                code = new StringBuilder("/**\n\t").append(WordUtils.wrap(toolTip, 100, "\n\t", false)).append("\n*/\n").append(code).toString();
            }

            codeElement.setTextContent(code);
        }

        DonkeyElement contextSet = element.addChildElement("contextSet").addChildElement("delegate");

        switch (Integer.parseInt(element.removeChild("scope").getTextContent())) {
            case 0:
            case 1:
                contextSet.addChildElement("contextType", "GLOBAL_DEPLOY");
                contextSet.addChildElement("contextType", "GLOBAL_UNDEPLOY");
                contextSet.addChildElement("contextType", "GLOBAL_PREPROCESSOR");
            case 2:
                contextSet.addChildElement("contextType", "GLOBAL_POSTPROCESSOR");
                contextSet.addChildElement("contextType", "CHANNEL_DEPLOY");
                contextSet.addChildElement("contextType", "CHANNEL_UNDEPLOY");
                contextSet.addChildElement("contextType", "CHANNEL_PREPROCESSOR");
                contextSet.addChildElement("contextType", "CHANNEL_POSTPROCESSOR");
                contextSet.addChildElement("contextType", "CHANNEL_ATTACHMENT");
                contextSet.addChildElement("contextType", "CHANNEL_BATCH");
            case 3:
                contextSet.addChildElement("contextType", "SOURCE_RECEIVER");
                contextSet.addChildElement("contextType", "SOURCE_FILTER_TRANSFORMER");
                contextSet.addChildElement("contextType", "DESTINATION_FILTER_TRANSFORMER");
                contextSet.addChildElement("contextType", "DESTINATION_DISPATCHER");
                contextSet.addChildElement("contextType", "DESTINATION_RESPONSE_TRANSFORMER");
        }
    }

    @Override
    public void migrate3_4_0(DonkeyElement element) {}

    @Override
    public void migrate3_5_0(DonkeyElement element) {
        DonkeyElement propertiesElement = element.addChildElement("properties");
        propertiesElement.setAttribute("class", "com.mirth.connect.model.codetemplates.BasicCodeTemplateProperties");
        propertiesElement.addChildElement("type", element.removeChild("type").getTextContent());
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("id", id);
        purgedProperties.put("nameChars", PurgeUtil.countChars(name));
        purgedProperties.put("lastModified", lastModified);
        purgedProperties.put("contextSet", contextSet);
        purgedProperties.put("codeLines", PurgeUtil.countLines(code));
        CodeTemplateFunctionDefinition functionDefinition = getFunctionDefinition();
        purgedProperties.put("parameterCount", functionDefinition != null ? functionDefinition.getParameters().size() : 0);
        purgedProperties.put("properties", properties.getPurgedProperties());
        return purgedProperties;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName()).append('[');
        builder.append("id=").append(id).append(", ");
        builder.append("name=").append(name).append(", ");
        builder.append("revision=").append(revision).append(", ");
        builder.append("lastModified=").append(lastModified).append(", ");
        builder.append("contextSet=").append(contextSet).append(", ");
        builder.append("code=").append(code).append(", ");
        builder.append("properties=").append(properties).append(']');
        return builder.toString();
    }
}