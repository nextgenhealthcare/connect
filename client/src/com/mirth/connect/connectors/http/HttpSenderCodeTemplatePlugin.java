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

public class HttpSenderCodeTemplatePlugin extends CodeTemplatePlugin {

    public HttpSenderCodeTemplatePlugin(String name) {
        super(name);
    }

    @Override
    public Map<String, List<CodeTemplate>> getReferenceItems() {
        Map<String, List<CodeTemplate>> referenceItems = new HashMap<String, List<CodeTemplate>>();

        List<CodeTemplate> httpSenderFunctionsList = new ArrayList<CodeTemplate>();
        httpSenderFunctionsList.add(new CodeTemplate("Get HTTP Response Status Line", CodeTemplateType.DRAG_AND_DROP_CODE, CodeTemplateContextSet.getConnectorContextSet(), "$('responseStatusLine')", "Retrieves the status line (e.g. \"HTTP/1.1 200 OK\") from an HTTP response, for use in the response transformer."));
        httpSenderFunctionsList.add(new CodeTemplate("Get HTTP Response Header", CodeTemplateType.DRAG_AND_DROP_CODE, CodeTemplateContextSet.getConnectorContextSet(), "$('responseHeaders').get('Header-Name')", "Retrieves a header value from an HTTP response, for use in the response transformer."));
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