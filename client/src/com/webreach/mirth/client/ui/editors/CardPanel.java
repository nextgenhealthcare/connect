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

import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;


/**
 * @author chrisl, franciscos
 *
 */
// we don't really need this class.  we could
// place these methods in the TransformerPane, which
// i might, when i get a chance to clean that class...
public class CardPanel extends JPanel {

	public CardPanel() {
		super();
		data = null;
		this.setLayout( new CardLayout() );
		this.setBorder( BorderFactory.createEmptyBorder() );
		this.setPreferredSize( new Dimension( 0, 0 ) );
	}
	
	// Adds a new card to the panel.  
	public void addCard( JPanel panel, String type ){
		this.add(panel, type);
	}
	
	// Shows a certain card
	public void showCard( String type ) {
		CardLayout cl = (CardLayout)this.getLayout();
		cl.show(this, type);
	}

	// return the data object
	public Map<Object, Object> getData() {
		return data;
	}
	
	// set the data object
	public void setData( Map<Object, Object> data ) {
		this.data = data;
	}
	
	
	private Map<Object, Object> data;
	
}
