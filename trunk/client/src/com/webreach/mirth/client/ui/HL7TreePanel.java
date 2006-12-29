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


package com.webreach.mirth.client.ui;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Composite;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Primitive;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class HL7TreePanel extends JPanel {
	private PipeParser parser;
	private EncodingCharacters encodingChars;

	public HL7TreePanel() {
		parser = new PipeParser();
		parser.setValidationContext(new NoValidation());
		encodingChars = new EncodingCharacters('|', null);
		this.setLayout(new GridLayout(1, 1));
		this.setBackground( Color.white );
		setBorder(BorderFactory.createEmptyBorder());
	}

	/**
	 * Updates the panel with a new Message.
	 */
	public void setMessage(String source) {
		Message message = null;
		String messageName = new String();

		if (source != null) {
			try {
				if (!source.startsWith("<")){
					message = parser.parse(source);
					Terser terser = new Terser(message);
					messageName = terser.get("/MSH-9-1") + "-" + terser.get("/MSH-9-2") + " (" + message.getVersion() + ")";
				}
			} catch (EncodingNotSupportedException e) {
				PlatformUI.MIRTH_FRAME.alertWarning( "Encoding not supported.\n" +
						"Please check the syntax of your message\n" +
						"and try again.");
			} catch (HL7Exception e) {
				PlatformUI.MIRTH_FRAME.alertError( "HL7 Error!\n" +
						"Please check the syntax of your message\n" +
						"and try again.");
			} catch (Exception e) {
				PlatformUI.MIRTH_FRAME.alertException(e.getStackTrace(), e.getMessage());
				e.printStackTrace();
			}
			
			if (message != null) {
				DefaultMutableTreeNode top = new DefaultMutableTreeNode(messageName);
				addChildren(message, top);
	
				JTree tree = new JTree(top);
				tree.setDragEnabled( true ); //XXXX
				removeAll();
				add(tree);
				revalidate();
			}
		}
	}
	
        /**
         * Clears the current tree so that it is empty.
         */
	public void clearMessage() {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Select a message to view HL7 message tree.");
		JTree tree = new JTree(top);
		removeAll();
		add(tree);
		revalidate();
	}

	/**
	 * Adds the children of the given group under the given tree node.
	 */
	private void addChildren(Group messageParent, MutableTreeNode treeParent) {
		String[] childNames = messageParent.getNames();
		int currentChild = 0;

		for (int i = 0; i < childNames.length; i++) {
			try {
				Structure[] childReps = messageParent.getAll(childNames[i]);

				for (int j = 0; j < childReps.length; j++) {
					DefaultMutableTreeNode newNode = null;

					if (childReps[j] instanceof Group) {
						String groupName = childReps[j].getClass().getName();
						groupName = groupName.substring(groupName.lastIndexOf('.') + 1, groupName.length());
						newNode = new DefaultMutableTreeNode(groupName + " (rep " + j + ")");
						addChildren((Group) childReps[j], newNode);
					} else if (childReps[j] instanceof Segment) {
						newNode = new DefaultMutableTreeNode(parser.encode((Segment) childReps[j], encodingChars));
						addChildren((Segment) childReps[j], newNode);
					}

					treeParent.insert(newNode, currentChild++);
				}
			} catch (HL7Exception e) {
				PlatformUI.MIRTH_FRAME.alertException(e.getStackTrace(), e.getMessage());
			}
		}
	}

	/**
	 * Add fields of a segment to the tree ...
	 */
	private void addChildren(Segment messageParent, MutableTreeNode treeParent) {
		int n = messageParent.numFields();
		int currentChild = 0;
		for (int i = 1; i <= n; i++) {
			try {
				Type[] reps = messageParent.getField(i);
				for (int j = 0; j < reps.length; j++) {
					String field = parser.encode(reps[j], encodingChars);
					DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("Field " + i + " rep " + j + " (" + getLabel(reps[j]) + "): " + field);
					addChildren(reps[j], newNode);
					treeParent.insert(newNode, currentChild++);
				}
			} catch (HL7Exception e) {
				PlatformUI.MIRTH_FRAME.alertException(e.getStackTrace(), e.getMessage());
			}
		}
	}

	/**
	 * Adds children to the tree. If the Type is a Varies, the Varies data are
	 * added under a new node called "Varies". If there are extra components,
	 * these are added under a new node called "ExtraComponents"
	 */
	private void addChildren(Type messageParent, MutableTreeNode treeParent) {
		if (Varies.class.isAssignableFrom(messageParent.getClass())) {
			// DefaultMutableTreeNode variesNode = new
			// DefaultMutableTreeNode("Varies");
			// treeParent.insert(variesNode, treeParent.getChildCount());
			Type data = ((Varies) messageParent).getData();
			DefaultMutableTreeNode dataNode = new DefaultMutableTreeNode(getLabel(data));
			treeParent.insert(dataNode, 0);
			addChildren(data, dataNode);
		} else {
			if (Composite.class.isAssignableFrom(messageParent.getClass())) {
				addChildren((Composite) messageParent, treeParent);
			} else if (Primitive.class.isAssignableFrom(messageParent.getClass())) {
				addChildren((Primitive) messageParent, treeParent);
			}

			if (messageParent.getExtraComponents().numComponents() > 0) {
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("ExtraComponents");
				treeParent.insert(newNode, treeParent.getChildCount());
				for (int i = 0; i < messageParent.getExtraComponents().numComponents(); i++) {
					DefaultMutableTreeNode variesNode = new DefaultMutableTreeNode("Varies");
					newNode.insert(variesNode, i);
					addChildren(messageParent.getExtraComponents().getComponent(i), variesNode);
				}
			}
		}
	}

	/**
	 * Adds components of a composite to the tree ...
	 */
	private void addChildren(Composite messageParent, MutableTreeNode treeParent) {
		Type[] components = messageParent.getComponents();

		for (int i = 0; i < components.length; i++) {
			DefaultMutableTreeNode newNode;
			newNode = new DefaultMutableTreeNode(getLabel(components[i]));
			addChildren(components[i], newNode);
			treeParent.insert(newNode, i);
		}
	}

	/**
	 * Adds single text value to tree as a leaf
	 */
	private void addChildren(Primitive messageParent, MutableTreeNode treeParent) {
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(messageParent.getValue());
		treeParent.insert(newNode, 0);
	}

	/**
	 * Returns the unqualified class name as a label for tree nodes.
	 */
	private static String getLabel(Object o) {
		String name = o.getClass().getName();
		return name.substring(name.lastIndexOf('.') + 1, name.length());
	}
}
