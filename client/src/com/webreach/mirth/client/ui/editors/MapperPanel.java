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
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;

import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthSyntaxTextArea;
import com.webreach.mirth.client.ui.components.MirthTextField;
import com.webreach.mirth.client.ui.components.MirthTextPane;
import com.webreach.mirth.model.Channel;

public class MapperPanel extends CardPanel {

	/** Creates new form MapperPanel */
	public MapperPanel() {
		initComponents();
	}

	public MapperPanel(MirthEditorPane p) {
		super();
		parent = p;
		initComponents();
	}

	/**
	 * initialize components and set layout; originally created with NetBeans,
	 * modified by franciscos
	 */
	protected void initComponents() {
		mappingPanel = new JPanel();
		labelPanel = new JPanel();
		mappingLabel = new JLabel("   " + label);
		mappingTextField = new MirthTextField();
		mappingScrollPane = new JScrollPane();

		mappingDoc = new SyntaxDocument();
		mappingDoc.setTokenMarker(new JavaScriptTokenMarker());
		mappingTextPane = new MirthSyntaxTextArea(true, true);
		mappingTextPane.setDocument(mappingDoc);

		labelPanel.setLayout(new BorderLayout());
		labelPanel.add(mappingLabel, BorderLayout.NORTH);
		labelPanel.add(new JLabel(" "), BorderLayout.WEST);
		labelPanel.add(mappingTextField, BorderLayout.CENTER);
		labelPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 150));

		mappingPanel.setBorder(BorderFactory.createEmptyBorder());
		mappingTextField.setBorder(BorderFactory.createEtchedBorder());
		mappingTextPane.setBorder(BorderFactory.createEmptyBorder());
		mappingScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLoweredBevelBorder(), "Mapping: ",
				TitledBorder.LEFT, TitledBorder.ABOVE_TOP, new Font(null,
						Font.PLAIN, 11), Color.black));

		mappingTextPane.setFont(EditorConstants.DEFAULT_FONT);

		mappingTextPanel = new JPanel();
		mappingTextPanel.setLayout(new BorderLayout());
		mappingTextPanel.add(mappingTextPane, BorderLayout.CENTER);
		setAddAsGlobal();
		mappingScrollPane.setViewportView(mappingTextPanel);

		mappingPanel.setLayout(new BorderLayout());
		mappingPanel.add(labelPanel, BorderLayout.NORTH);
		mappingPanel.add(mappingScrollPane, BorderLayout.CENTER);

		// BGN listeners
		mappingTextField.getDocument().addDocumentListener(
				new DocumentListener() {
					public void changedUpdate(DocumentEvent arg0) {

					}

					public void insertUpdate(DocumentEvent arg0) {
						updateTable();
						parent.modified = true;
					}

					public void removeUpdate(DocumentEvent arg0) {
						updateTable();
						parent.modified = true;
					}

				});

		mappingTextPane.getDocument().addDocumentListener(
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
		// END listeners

		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.setLayout(new BorderLayout());
		this.add(mappingPanel, BorderLayout.CENTER);
	}

	public void updateTable() {
		if (parent.getSelectedRow() != -1) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					parent.getTableModel().setValueAt(
							mappingTextField.getText(),
							parent.getSelectedRow(), parent.STEP_NAME_COL);
					parent.updateTaskPane();
				}
			});
		}
	}

	public void setAsJavaScript() {
		if (parent.getSelectedRow() != -1) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					parent.getTableModel().setValueAt("New Step",
							parent.getSelectedRow(), parent.STEP_NAME_COL);
					parent.updateTaskPane();
				}
			});
		}
	}

	public void update() {
		parent.update();
		mappingLabel.setText("   Variable: ");
		if (addToGlobal != null)
			addToGlobal.setSelected(false);
		parent.setDroppedTextSuffixPrefix("msg", ".toString()");

	}

	public Map<Object, Object> getData() {
		Map<Object, Object> m = new HashMap<Object, Object>();
		m.put("Variable", mappingTextField.getText().trim());
		m.put("Mapping", mappingTextPane.getText().trim());
		if (addToGlobal != null) {
			if (addToGlobal.isSelected())
				m.put("isGlobal", UIConstants.YES_OPTION);
			else
				m.put("isGlobal", UIConstants.NO_OPTION);
		}
		return m;
	}

	public void setData(Map<Object, Object> data) {
		boolean modified = parent.modified;

		if (data != null) {
			mappingTextField.setText((String) data.get("Variable"));
			mappingTextPane.setText((String) data.get("Mapping"));
			if (addToGlobal != null) {
				if (data.get("isGlobal") == null
						|| ((String) data.get("isGlobal"))
								.equals(UIConstants.NO_OPTION))
					addToGlobal.setSelected(false);
				else
					addToGlobal.setSelected(true);
			}
		} else {
			mappingTextField.setText("");
			mappingTextPane.setText("");
			setAsJavaScript();
		}

		parent.modified = modified;
	}

	public void setAddAsGlobal() {
		globalPanel = new JPanel();
		globalPanel.setLayout(new BorderLayout());
		globalPanel.add(new JLabel("  "), BorderLayout.WEST);
		addToGlobal = new JCheckBox();
		addToGlobal.setFocusable(false);
		addToGlobal.setText("Add as global variable");
		addToGlobal.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				parent.modified = true;
			}
		});
		globalPanel.add(addToGlobal, BorderLayout.EAST);
		labelPanel.add(globalPanel, BorderLayout.EAST);
		labelPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

	}

	public boolean updating = false;

	protected String label;

	protected JPanel mappingTextPanel; // for no linewrap in textpane

	protected MirthSyntaxTextArea mappingTextPane;

	protected static SyntaxDocument mappingDoc;

	protected JLabel mappingLabel;

	protected JPanel labelPanel;

	protected JPanel mappingPanel;

	protected JPanel globalPanel;

	protected MirthTextField mappingTextField;

	protected JScrollPane mappingScrollPane;

	protected MirthEditorPane parent;

	public JCheckBox addToGlobal;
}
