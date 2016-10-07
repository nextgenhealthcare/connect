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

import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateContextSet;
import com.mirth.connect.model.codetemplates.CodeTemplateProperties.CodeTemplateType;
import com.mirth.connect.plugins.CodeTemplatePlugin;

public class FileReaderCodeTemplatePlugin extends CodeTemplatePlugin {

    public FileReaderCodeTemplatePlugin(String name) {
        super(name);
    }

    @Override
    public Map<String, List<CodeTemplate>> getReferenceItems() {
        Map<String, List<CodeTemplate>> referenceItems = new HashMap<String, List<CodeTemplate>>();

        List<CodeTemplate> fileReaderFunctionsList = new ArrayList<CodeTemplate>();
        fileReaderFunctionsList.add(new CodeTemplate("Get Original File Name", CodeTemplateType.DRAG_AND_DROP_CODE, CodeTemplateContextSet.getConnectorContextSet(), "sourceMap.get('originalFilename')", "Retrieves the name of the file read by the File Reader."));
        fileReaderFunctionsList.add(new CodeTemplate("Get Original File Directory", CodeTemplateType.DRAG_AND_DROP_CODE, CodeTemplateContextSet.getConnectorContextSet(), "sourceMap.get('fileDirectory')", "Retrieves the parent directory of the file read by the File Reader."));
        fileReaderFunctionsList.add(new CodeTemplate("Get Original File Size in Bytes", CodeTemplateType.DRAG_AND_DROP_CODE, CodeTemplateContextSet.getConnectorContextSet(), "sourceMap.get('fileSize')", "Retrieves the size (in bytes) of the file read by the File Reader."));
        fileReaderFunctionsList.add(new CodeTemplate("Get Original File Last Modified Timestamp", CodeTemplateType.DRAG_AND_DROP_CODE, CodeTemplateContextSet.getConnectorContextSet(), "sourceMap.get('fileLastModified')", "Retrieves the last modified timestamp (in milliseconds since January 1st, 1970) of the file read by the File Reader."));
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