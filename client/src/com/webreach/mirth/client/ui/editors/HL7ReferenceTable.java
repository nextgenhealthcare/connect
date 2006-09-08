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

import java.io.IOException;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.CenterCellRenderer;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.util.HL7ReferenceLoader;


public class HL7ReferenceTable extends ReferenceTable {

	public HL7ReferenceTable () {
		super();
		
		String[][] referenceData = null;
        
        try {
        	referenceData = ( new HL7ReferenceLoader()).getReferenceTable();
        } catch (IOException e) {
        	parent.alertError("Could not load HL7 Reference Table!\n\n"
        			+ e.getMessage() );
        	System.err.println( "Could not load HL& Reference Table!" );
        	e.printStackTrace(); 
        }
        
        this.setModel( new DefaultTableModel( referenceData, 
				new String[] {"ID","Description","Chapter"} ) {
					public boolean isCellEditable ( int row, int col ) {
						return false;
					}
				});
       
       this.getColumnExt( "ID" ).setMaxWidth( UIConstants.MAX_WIDTH );
       this.getColumnExt( "Chapter" ).setMaxWidth( UIConstants.MAX_WIDTH );
       
       this.getColumnExt( "ID" ).setPreferredWidth( 35 );
       this.getColumnExt( "Chapter" ).setPreferredWidth( 55 );
       
       DefaultTableCellRenderer highlightColumn = new CenterCellRenderer();
       highlightColumn.setBackground( UIConstants.HIGHLIGHTER_COLOR );
       this.getColumnExt( "ID" ).setCellRenderer( highlightColumn );
       
       this.getColumnExt( "ID" ).setHeaderRenderer( PlatformUI.CENTER_COLUMN_HEADER_RENDERER );
       this.getColumnExt( "Chapter" ).setHeaderRenderer( PlatformUI.CENTER_COLUMN_HEADER_RENDERER );
	}
	
	private Frame parent = PlatformUI.MIRTH_FRAME;
}
