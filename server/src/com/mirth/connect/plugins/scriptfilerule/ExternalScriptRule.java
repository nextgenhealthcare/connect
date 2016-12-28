/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.scriptfilerule;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.mirth.connect.model.Rule;
import com.mirth.connect.util.ScriptBuilderException;

public class ExternalScriptRule extends Rule {

    public static final String PLUGIN_POINT = "External Script";

    private String scriptPath;

    public ExternalScriptRule() {
        scriptPath = "";
    }

    public ExternalScriptRule(ExternalScriptRule props) {
        super(props);
        scriptPath = props.getScriptPath();
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    @Override
    public String getScript(boolean loadFiles) throws ScriptBuilderException {
        StringBuilder script = new StringBuilder();
        if (loadFiles) {
            try {
                script.append("\n" + FileUtils.readFileToString(new File(scriptPath)) + "\n");
            } catch (IOException e) {
                throw new ScriptBuilderException("Could not add script file.", e);
            }
        } else {
            script.append("// External script will be loaded on deploy\n");
            script.append("// Path: ").append(scriptPath).append('\n');
        }
        return script.toString();
    }

    @Override
    public String getType() {
        return PLUGIN_POINT;
    }

    @Override
    public Rule clone() {
        return new ExternalScriptRule(this);
    }
}