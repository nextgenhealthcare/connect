/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.plugins.CodeTemplatePlugin;

public class FileReaderCodeTemplatePlugin extends CodeTemplatePlugin {

    public FileReaderCodeTemplatePlugin(String name) {
        super(name);
    }

    @Override
    public Map<String, List<CodeTemplate>> getReferenceItems() {
        Map<String, List<CodeTemplate>> referenceItems = new HashMap<String, List<CodeTemplate>>();

        List<CodeTemplate> fileReaderFunctionsList = new ArrayList<CodeTemplate>();
        fileReaderFunctionsList.add(new CodeTemplate("Get Original File Name", "Retrieves the name of the file read by the File Reader.", "sourceMap.get('originalFilename')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        fileReaderFunctionsList.add(new CodeTemplate("Get Original File Directory", "Retrieves the parent directory of the file read by the File Reader.", "sourceMap.get('fileDirectory')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        fileReaderFunctionsList.add(new CodeTemplate("Get Original File Size in Bytes", "Retrieves the size (in bytes) of the file read by the File Reader.", "sourceMap.get('fileSize')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        fileReaderFunctionsList.add(new CodeTemplate("Get Original File Last Modified Timestamp", "Retrieves the last modified timestamp (in milliseconds since January 1st, 1970) of the file read by the File Reader.", "sourceMap.get('fileLastModified')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        referenceItems.put("File Reader Functions", fileReaderFunctionsList);

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