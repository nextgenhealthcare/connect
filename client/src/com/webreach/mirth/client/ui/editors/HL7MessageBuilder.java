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

import com.webreach.mirth.model.Channel;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class HL7MessageBuilder extends MapperPanel {
	public HL7MessageBuilder(MirthEditorPane p) {
		super();
		parent = p;
		initComponents();
	}

	public void update() {
		parent.update();
		mappingLabel.setText("   HL7 Message Segment: ");
		parent.setDroppedTextSuffixPrefix("temp", ".text()[0]");
		
	}
	
	public void setAddAsGlobal() {
		labelPanel.setLayout(new BorderLayout());
		labelPanel.add(mappingLabel, BorderLayout.NORTH);
		labelPanel.add(new JLabel(" "), BorderLayout.WEST);
		labelPanel.add(mappingTextField, BorderLayout.CENTER);
		labelPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
	}
}
