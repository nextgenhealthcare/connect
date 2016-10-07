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

import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateContextSet;
import com.mirth.connect.model.codetemplates.CodeTemplateProperties.CodeTemplateType;
import com.mirth.connect.plugins.CodeTemplatePlugin;

public class HttpListenerCodeTemplatePlugin extends CodeTemplatePlugin {

    public HttpListenerCodeTemplatePlugin(String name) {
        super(name);
    }

    @Override
    public Map<String, List<CodeTemplate>> getReferenceItems() {
        Map<String, List<CodeTemplate>> referenceItems = new HashMap<String, List<CodeTemplate>>();

        List<CodeTemplate> httpListenerFunctionsList = new ArrayList<CodeTemplate>();
        httpListenerFunctionsList.add(new CodeTemplate("Get HTTP Request Method", CodeTemplateType.DRAG_AND_DROP_CODE, CodeTemplateContextSet.getConnectorContextSet(), "sourceMap.get('method')", "Retrieves the method (e.g. GET, POST) from an incoming HTTP request."));
        httpListenerFunctionsList.add(new CodeTemplate("Get HTTP Request Context Path", CodeTemplateType.DRAG_AND_DROP_CODE, CodeTemplateContextSet.getConnectorContextSet(), "sourceMap.get('contextPath')", "Retrieves the context path from an incoming HTTP request."));
        httpListenerFunctionsList.add(new CodeTemplate("Get HTTP Request Header", CodeTemplateType.DRAG_AND_DROP_CODE, CodeTemplateContextSet.getConnectorContextSet(), "sourceMap.get('headers').get('Header-Name')", "Retrieves a header value from an incoming HTTP request."));
        httpListenerFunctionsList.add(new CodeTemplate("Get HTTP Request Parameter", CodeTemplateType.DRAG_AND_DROP_CODE, CodeTemplateContextSet.getConnectorContextSet(), "sourceMap.get('parameters').get('parameterName')", "Retrieves a query/form parameter from an incoming HTTP request. If multiple values exist for the parameter, an array will be returned."));
        httpListenerFunctionsList.add(new CodeTemplate("Convert HTTP Payload to XML", CodeTemplateType.DRAG_AND_DROP_CODE, CodeTemplateContextSet.getGlobalContextSet(), "HTTPUtil.httpBodyToXml(httpBody, contentType)", "Serializes an HTTP request body into XML. Multipart requests will also automatically be parsed into separate XML nodes. The body may be passed in as a string or input stream."));
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