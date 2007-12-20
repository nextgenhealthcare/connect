package com.webreach.mirth.plugins;

import java.util.Map;

import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.MirthEditorPane;
import com.webreach.mirth.model.hl7v2.Component;

public abstract class MirthEditorPanePlugin
{
    protected String name;
    protected MirthEditorPane parent;
    protected boolean provideOwnStepName = false;
    public MirthEditorPanePlugin(String name, MirthEditorPane parent)
    {
        this.parent = parent;
        this.name = name;
    }
    public String getPluginName()
    {
        return name;
    }
    public abstract BasePanel getPanel();
    public abstract boolean isNameEditable();
    public String getNewName()
    {
        return new String();
    }
    public abstract String getDisplayName();
    public abstract Map<Object, Object> getData(int row);
    public abstract void setData(Map<Object, Object> data);
    public String getName()
    {
        return null;
    }
    public String doValidate(Map<Object, Object> data)
    {
        return null;
    }
    public boolean showValidateTask()
    {
        return false;
    }

    public abstract String getScript(Map<Object, Object> data);
    public abstract void clearData();
    public abstract void initData();
	public boolean isProvideOwnStepName() {
		return provideOwnStepName;
	}
	protected String getVocabDescription(String[] parts) {
		
		String mappingDescription = new String();
		int currentSegment = 0;
		
		// For every part, check if it's an index.
		// If not, get the correct vocab for the current segment.
		for (int i = 0; i < parts.length; i++) {
			
			if (parts[i].indexOf("'") == -1) {
				mappingDescription += " [" + parts[i] + "]";
			} else {
				String part = parts[i].replaceAll("'", "");
				String segmentDescription = "";
				
				if (currentSegment == 0) {
					// segment
					// PID
					segmentDescription = Component.getSegmentDescription(part);
				} else if (currentSegment == 1) {
					// segment + field
					// PID, PID.5
					segmentDescription = Component.getSegmentorCompositeFieldDescription(part, false);
				} else if (currentSegment == 2) {
					// segment + field + composite
					// PID,PID.5,PID.5.1 or PID,PID.5,XPN.1
					// must check last element
					if (part.split("\\.").length > 1) {
						// PID.5.1 style
						segmentDescription = Component.getCompositeFieldDescriptionWithSegment(part, false);
					} else {
						// XPN.1 style
						segmentDescription = Component.getSegmentorCompositeFieldDescription(part, false);
					}
				}
				
				// If no vocab was found, use the segment name.
				if (segmentDescription.length() == 0)
					segmentDescription = part;
				
				if (mappingDescription.length() != 0) {
					mappingDescription += " - ";
				}
				
				mappingDescription += segmentDescription;
				currentSegment++;
			}
		}
		
//		if (mappingDescription.endsWith(" - Value")) {
//			mappingDescription = mappingDescription.substring(0, mappingDescription.length() - 8);
//		}
		
		return mappingDescription.trim();
	}
}
