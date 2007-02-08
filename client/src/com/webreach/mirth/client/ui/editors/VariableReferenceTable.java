/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.client.ui.editors;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.FunctionListItem;


public class VariableReferenceTable extends ReferenceTable {
	private Object[] tooltip;
	private ArrayList<FunctionListItem> _listItems;
	public VariableReferenceTable () {
		super();
		makeTable( null, null );
	}
	
	public VariableReferenceTable ( Object[] data ) {
		super();
		makeTable( data, null );
	}
	
	public VariableReferenceTable ( Object[] data, Object[] tooltip ) {
		super();
		makeTable( data, tooltip );
	}
	
	public VariableReferenceTable(ArrayList<FunctionListItem> listItems){
		this._listItems = listItems;
		makeTable(listItems);
	}
	private void makeTable(ArrayList<FunctionListItem> listItems){
		if (listItems == null) return;
		Object[] tooltips = new String[listItems.size()];
		Object[] names = new String[listItems.size()];
		Iterator<FunctionListItem> listItemIterator = listItems.iterator();
		int i = 0; 
		while (listItemIterator.hasNext()){
			FunctionListItem listItem = listItemIterator.next();
			names[i]= listItem.getName();
			tooltips[i] = listItem.getTooltip();    
			i++;
		}
		makeTable(names, tooltips);
	}
	private void makeTable(Object[] data, Object[] tooltip) {
		if (data == null) return;
		
		this.tooltip = tooltip;
		
		Object[][] d = new String[data.length][2];
		for ( int i = 0;  i < data.length;  i++ ) {
			d[i][0] = data[i];
			d[i][1] = null;
		}
		
		this.setModel( new DefaultTableModel( d,
				new Object[] {"Common Variables and Functions"} ) {
			public boolean isCellEditable ( int row, int col ) {
				return false;
			}
		});
		
		this.getColumnExt( "Common Variables and Functions" ).setPreferredWidth( 80 );
		this.getColumnExt( "Common Variables and Functions" ).setHeaderRenderer( PlatformUI.CENTER_COLUMN_HEADER_RENDERER );
		
	}
	
	public String getToolTipText( MouseEvent event ) {
		Point p = event.getPoint();
		int col = columnAtPoint(p);
		int row = rowAtPoint(p);
		if ( col >= 0  &&  row >= 0  &&  tooltip != null ) {
			Object o = getValueAt( row, col );
			if ( o != null ) 
				return "<html><body style=\"width:150px\"><p>" + 
						tooltip[row] + 
						"</p></body></html>";
		}
		return null;
	}
	
}
