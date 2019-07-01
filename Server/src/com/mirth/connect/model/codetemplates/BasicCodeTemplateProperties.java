/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.codetemplates;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.mirth.connect.donkey.util.purge.PurgeUtil;

public class BasicCodeTemplateProperties extends CodeTemplateProperties {

    private String code;

    public BasicCodeTemplateProperties(CodeTemplateType type, String code, String description) {
        this(type, addComment(code, description));
    }

    public BasicCodeTemplateProperties(CodeTemplateType type, String code) {
        super(type);
        setCode(code);
    }

    @Override
    public String getPluginPointName() {
        return getType().toString();
    }

    @Override
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        updateDocumentation();
    }

    private static String addComment(String code, String description) {
        if (StringUtils.isNotBlank(description)) {
            return new StringBuilder("/**\n\t").append(WordUtils.wrap(description, 80, "\n\t", false)).append("\n*/\n").append(code).toString();
        }
        return code;
    }

    @Override
    public CodeTemplateProperties clone() {
        return new BasicCodeTemplateProperties(getType(), code);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("type", getType());
        purgedProperties.put("codeLines", PurgeUtil.countLines(code));
        return purgedProperties;
    }
}