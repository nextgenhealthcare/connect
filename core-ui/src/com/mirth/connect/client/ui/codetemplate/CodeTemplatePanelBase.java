package com.mirth.connect.client.ui.codetemplate;

import java.util.Map;

import com.mirth.connect.client.ui.AbstractFramePanel;
import com.mirth.connect.client.ui.ExtendedSwingWorker;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrarySaveResult;

public abstract class CodeTemplatePanelBase extends AbstractFramePanel {
    
    public static final String NEW_CHANNELS = "[New Channels]";
    
    public abstract Map<String, CodeTemplateLibrary> getCachedCodeTemplateLibraries();
    
    public abstract Map<String, CodeTemplate> getCachedCodeTemplates();
    
    public abstract ExtendedSwingWorker<CodeTemplateLibrarySaveResult, Void> getSwingWorker(Map<String, CodeTemplateLibrary> libraries, Map<String, CodeTemplateLibrary> removedLibraries, Map<String, CodeTemplate> updatedCodeTemplates, Map<String, CodeTemplate> removedCodeTemplates, boolean override);
}
