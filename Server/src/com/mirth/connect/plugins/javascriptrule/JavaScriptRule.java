/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.javascriptrule;

import java.util.Collection;
import java.util.Map;

import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.mirth.connect.model.Rule;
import com.mirth.connect.util.JavaScriptSharedUtil;

public class JavaScriptRule extends Rule {

    public static final String PLUGIN_POINT = "JavaScript";

    private String script;

    public JavaScriptRule() {
        script = "";
    }

    public JavaScriptRule(JavaScriptRule props) {
        super(props);
        script = props.getScript();
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public String getScript(boolean loadFiles) {
        return script;
    }

    @Override
    public String getType() {
        return PLUGIN_POINT;
    }

    @Override
    public Rule clone() {
        return new JavaScriptRule(this);
    }

    @Override
    public Collection<String> getResponseVariables() {
        return JavaScriptSharedUtil.getResponseVariables(getScript(false));
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("scriptLines", PurgeUtil.countLines(script));
        return purgedProperties;
    }
}