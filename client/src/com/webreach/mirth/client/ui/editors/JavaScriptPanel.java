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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;

import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthSyntaxTextArea;
import com.webreach.mirth.client.ui.components.MirthTextPane;


/**
 * @author franciscos
 *
 */
public class JavaScriptPanel extends CardPanel {
	
	public JavaScriptPanel(){initComponents();}
	public JavaScriptPanel(MirthEditorPane p) {
		super();
		parent = p;
		initComponents();
	}
	
	private void initComponents() {
		scriptPanel = new JPanel();
		scriptScrollPane = new JScrollPane();
		scriptScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scriptScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		scriptDoc = new SyntaxDocument();
		scriptDoc.setTokenMarker(new JavaScriptTokenMarker());
		scriptTextPane = new MirthSyntaxTextArea(true, true);
                scriptTextPane.setDocument(scriptDoc);
	
		scriptTextPane.setBorder( BorderFactory.createEmptyBorder() );
		scriptPanel.setBorder( BorderFactory.createEmptyBorder() );
		
		scriptTextPane.setFont( EditorConstants.DEFAULT_FONT );
		
		scriptPanel.setLayout( new BorderLayout() );
		scriptPanel.add( scriptTextPane, BorderLayout.CENTER );

		scriptScrollPane.setViewportView( scriptPanel );
                
		scriptScrollPane.setBorder( BorderFactory.createTitledBorder( 
				BorderFactory.createLoweredBevelBorder(), "JavaScript", TitledBorder.LEFT,
				TitledBorder.ABOVE_TOP, new Font( null, Font.PLAIN, 11 ), 
				Color.black ));
		
		//BGN listeners
		scriptTextPane.getDocument().addDocumentListener(
				new DocumentListener() {
					
					public void changedUpdate(DocumentEvent arg0) {
                                            
					}
					
					public void insertUpdate(DocumentEvent arg0) {
						parent.modified = true;			
					}
					
					public void removeUpdate(DocumentEvent arg0) {
						parent.modified = true;
					}
					
				});
		//END listeners
		
		this.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10) );
		this.setLayout( new BorderLayout() );
		this.add( scriptScrollPane, BorderLayout.CENTER );
		
	}
	
	public void update(){
		parent.update();
	}
	
	public Map<Object, Object> getData() {
		Map<Object, Object> m = new HashMap<Object, Object>();
		m.put( "Script", scriptTextPane.getText().trim() );
		return m;
	}
	
	
	public void setData( Map<Object, Object> m ) {
                boolean modified = parent.modified;
		
                if ( m != null )
			scriptTextPane.setText( (String)m.get( "Script" ) );	
		else
			scriptTextPane.setText( "" );
                
                parent.modified = modified;
	}
        
        public String getJavaScript()
        {
            return scriptTextPane.getText();
        }
	
	private JPanel scriptPanel;
	private static SyntaxDocument scriptDoc;
	private MirthSyntaxTextArea scriptTextPane;
	private JScrollPane scriptScrollPane;
	private LineNumber lineNumbers;
	private MirthEditorPane parent;
	
	private String header = "{";
	private String footer = "}";
	
}
