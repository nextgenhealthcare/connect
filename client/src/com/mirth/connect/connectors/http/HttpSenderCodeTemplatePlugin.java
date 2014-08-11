/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.plugins.CodeTemplatePlugin;

public class HttpSenderCodeTemplatePlugin extends CodeTemplatePlugin {

    public HttpSenderCodeTemplatePlugin(String name) {
        super(name);
    }

    @Override
    public Map<String, List<CodeTemplate>> getReferenceItems() {
        Map<String, List<CodeTemplate>> referenceItems = new HashMap<String, List<CodeTemplate>>();

        List<CodeTemplate> httpSenderFunctionsList = new ArrayList<CodeTemplate>();
        httpSenderFunctionsList.add(new CodeTemplate("Get HTTP Response Status Line", "Retrieves the status line (e.g. \"HTTP/1.1 200 OK\") from an HTTP response, for use in the response transformer.", "$('responseStatusLine')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        httpSenderFunctionsList.add(new CodeTemplate("Get HTTP Response Header", "Retrieves a header value from an HTTP response, for use in the response transformer.", "$('responseHeaders').get('Header-Name')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        referenceItems.put("HTTP Sender Functions", httpSenderFunctionsList);

        return referenceItems;
    }

    @Override
    public String getPluginPointName() {
        return pluginName + " Code Template Plugin";
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}
}