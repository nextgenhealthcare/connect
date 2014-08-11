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

public class HttpListenerCodeTemplatePlugin extends CodeTemplatePlugin {

    public HttpListenerCodeTemplatePlugin(String name) {
        super(name);
    }

    @Override
    public Map<String, List<CodeTemplate>> getReferenceItems() {
        Map<String, List<CodeTemplate>> referenceItems = new HashMap<String, List<CodeTemplate>>();

        List<CodeTemplate> httpListenerFunctionsList = new ArrayList<CodeTemplate>();
        httpListenerFunctionsList.add(new CodeTemplate("Get HTTP Request Method", "Retrieves the method (e.g. GET, POST) from an incoming HTTP request.", "sourceMap.get('method')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        httpListenerFunctionsList.add(new CodeTemplate("Get HTTP Request Context Path", "Retrieves the context path from an incoming HTTP request.", "sourceMap.get('contextPath')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        httpListenerFunctionsList.add(new CodeTemplate("Get HTTP Request Header", "Retrieves a header value from an incoming HTTP request.", "sourceMap.get('headers').get('Header-Name')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        httpListenerFunctionsList.add(new CodeTemplate("Get HTTP Request Parameter", "Retrieves a query/form parameter from an incoming HTTP request. If multiple values exist for the parameter, an array will be returned.", "sourceMap.get('parameters').get('parameterName')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        httpListenerFunctionsList.add(new CodeTemplate("Convert HTTP Payload to XML", "Serializes an HTTP request body into XML. Multipart requests will also automatically be parsed into separate XML nodes. The body may be passed in as a string or input stream.", "HTTPUtil.httpBodyToXml(httpBody, contentType)", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        referenceItems.put("HTTP Listener Functions", httpListenerFunctionsList);

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