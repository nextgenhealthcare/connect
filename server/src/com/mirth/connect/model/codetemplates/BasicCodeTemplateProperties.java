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

public class BasicCodeTemplateProperties extends CodeTemplateProperties {

    public BasicCodeTemplateProperties(CodeTemplateType type) {
        super(type);
    }

    @Override
    public String getPluginPointName() {
        return getType().toString();
    }

    @Override
    public CodeTemplateProperties clone() {
        return new BasicCodeTemplateProperties(getType());
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("type", getType());
        return purgedProperties;
    }
}