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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.Ostermiller.Syntax.HighlightedDocument;
import com.webreach.mirth.client.ui.components.MirthTextField;
import com.webreach.mirth.client.ui.components.MirthTextPane;


public class MapperPanel extends CardPanel {
	
	/** Creates new form MapperPanel */
	public MapperPanel() { initComponents(); }
	public MapperPanel(MirthEditorPane p) {
		super();
		parent = p;		
		initComponents();
	}
	
	/** initialize components and set layout;
	 *  originally created with NetBeans, modified by franciscos
	 */
	protected void initComponents() {
		mappingPanel = new JPanel();
		labelPanel = new JPanel();
		mappingLabel = new JLabel( "   " + label );
		mappingTextField = new MirthTextField();
		mappingScrollPane = new JScrollPane();
		
		mappingDoc = new HighlightedDocument();
		mappingDoc.setHighlightStyle( HighlightedDocument.JAVASCRIPT_STYLE );
		mappingTextPane = new MirthTextPane();
                mappingTextPane.setDocument(mappingDoc);
		
		mappingPanel.setBorder( BorderFactory.createEmptyBorder() );
		mappingTextField.setBorder( BorderFactory.createEtchedBorder() );
		mappingTextPane.setBorder( BorderFactory.createEmptyBorder() );
		mappingScrollPane.setBorder( BorderFactory.createTitledBorder( 
				BorderFactory.createLoweredBevelBorder(), "Mapping: ", TitledBorder.LEFT,
				TitledBorder.ABOVE_TOP, new Font( null, Font.PLAIN, 11 ), 
				Color.black ));
		
		mappingTextPane.setFont( EditorConstants.DEFAULT_FONT );
		
		mappingTextPanel = new JPanel();
		mappingTextPanel.setLayout( new BorderLayout() );
		mappingTextPanel.add( mappingTextPane, BorderLayout.CENTER );
		
		labelPanel.setLayout( new BorderLayout() );
		labelPanel.add( mappingLabel, BorderLayout.NORTH );
		labelPanel.add( new JLabel( " " ), BorderLayout.WEST );
		labelPanel.add( mappingTextField, BorderLayout.CENTER );
		labelPanel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 150) );
		
		mappingScrollPane.setViewportView( mappingTextPanel );
		
		mappingPanel.setLayout( new BorderLayout() );
		mappingPanel.add( labelPanel, BorderLayout.NORTH );
		mappingPanel.add( mappingScrollPane, BorderLayout.CENTER );
		
		// BGN listeners
		mappingTextField.getDocument().addDocumentListener(
				new DocumentListener() {
					public void changedUpdate(DocumentEvent arg0) {
						parent.modified = true;
					}
					
					public void insertUpdate(DocumentEvent arg0) {
						parent.modified = true;						
					}
					
					public void removeUpdate(DocumentEvent arg0) {
						parent.modified = true;						
					}
					
				});
		
		mappingTextPane.getDocument().addDocumentListener(
				new DocumentListener() {
					public void changedUpdate(DocumentEvent arg0) {
						parent.modified = true;
					}
					
					public void insertUpdate(DocumentEvent arg0) {
						parent.modified = true;						
					}
					
					public void removeUpdate(DocumentEvent arg0) {
						parent.modified = true;						
					}
					
				});
		// END listeners
		
		this.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10) );
		this.setLayout( new BorderLayout() );
		this.add( mappingPanel, BorderLayout.CENTER );
	} 
	
	public void update(){
		parent.update();
		mappingLabel.setText( "   Variable: " );
		parent.setDroppedTextPrefix("msg");
		
	}
	
	public Map<Object, Object> getData() {
		Map<Object, Object> m = new HashMap<Object, Object>();
		m.put( "Variable", mappingTextField.getText().trim() );
		m.put( "Mapping", mappingTextPane.getText().trim() );
		
		return m;
	}
	
	
	public void setData( Map<Object, Object> data ) {
		if ( data != null ) {
			mappingTextField.setText( (String)data.get( "Variable" ) );
			mappingTextPane.setText( (String)data.get( "Mapping" ) );
		} else {
			mappingTextField.setText( "" );
			mappingTextPane.setText( "" );
		}
	}
	
	
	protected String label;
	protected JPanel mappingTextPanel;		// for no linewrap in textpane
	protected MirthTextPane mappingTextPane;
	protected static HighlightedDocument mappingDoc;
	protected JLabel mappingLabel;
	protected JPanel labelPanel;
	protected JPanel mappingPanel;
	protected MirthTextField mappingTextField;
	protected JScrollPane mappingScrollPane;
	protected MirthEditorPane parent;
}
