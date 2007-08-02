package com.webreach.mirth.plugins;

import java.util.Map;

import javax.swing.table.TableCellRenderer;

import com.webreach.mirth.client.ui.DashboardPanel;
import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.MirthEditorPane;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.model.ChannelStatus;

public abstract class DashboardColumnPlugin {
	protected String name;
	protected DashboardPanel parent;
	public DashboardColumnPlugin(String name, DashboardPanel parent) {
		this.name = name;
		this.parent = parent;
	}
	public String getName(){
		return name;
	}
	public abstract String getColumnHeader();
	
	public abstract Object getTableData(ChannelStatus status);
	
	public abstract TableCellRenderer getCellRenderer();
	
	public abstract int getMaxWidth();
	
	public abstract int getMinWidth();
}
