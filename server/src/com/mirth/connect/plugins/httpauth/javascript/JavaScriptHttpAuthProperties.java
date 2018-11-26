/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth.javascript;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.model.channel.ConnectorPluginProperties;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.mirth.connect.plugins.httpauth.HttpAuthConnectorPluginProperties;

public class JavaScriptHttpAuthProperties extends HttpAuthConnectorPluginProperties {

    private String script;

    public JavaScriptHttpAuthProperties() {
        super(AuthType.JAVASCRIPT);
        script = "// Return an AuthenticationResult object to authenticate users.\n// Boolean return values may also be used.\n// You have access to the source map here.\n\nreturn AuthenticationResult.Success();";
    }

    public JavaScriptHttpAuthProperties(JavaScriptHttpAuthProperties props) {
        super(AuthType.JAVASCRIPT);
        script = props.getScript();
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public ConnectorPluginProperties clone() {
        return new JavaScriptHttpAuthProperties(this);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("authType", getAuthType());
        purgedProperties.put("scriptLines", PurgeUtil.countLines(script));
        return purgedProperties;
    }
}
