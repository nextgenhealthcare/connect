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
    public String doValidate()
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
		if (parts.length == 1) {
			// segment
			// PID
			mappingDescription = Component.getSegmentDescription(parts[0]);
		} else if (parts.length == 2) {
			// segment + field
			// PID, PID.5
			String segmentDescription = Component.getSegmentDescription(parts[0]);
			String fieldDescription = Component.getSegmentorCompositeFieldDescription(parts[1], false);
			mappingDescription = segmentDescription + " " + fieldDescription;
		} else if (parts.length == 3) {
			// segment + field + composite
			// PID,PID.5,PID.5.1 or PID,PID.5,XPN.1
			// must check last element
			mappingDescription = Component.getSegmentorCompositeFieldDescription(parts[1], false);
			if (parts[2].split("\\.").length > 1) {
				// PID.5.1 style
				mappingDescription += " " + Component.getCompositeFieldDescriptionWithSegment(parts[2], false);
			} else {
				// XPN.1 style
				mappingDescription += " " + Component.getSegmentorCompositeFieldDescription(parts[2], false);
			}
		}
		mappingDescription = mappingDescription.trim();

		return mappingDescription.trim();
	}


	public String removeInvalidCharacters(String source) {
		source = source.toLowerCase();
		source = source.replaceAll("\\/", "_or_");
		source = source.replaceAll(" - ", "_");
		source = source.replaceAll("&", "and");
		source = source.replaceAll("\\'|\\’|\\(|\\)", "");
		source = source.replaceAll(" |\\.", "_");
		return source;
	}

}
