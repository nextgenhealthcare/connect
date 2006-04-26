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


package com.webreach.mirth.applets.logviewer;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

public class LogViewer extends Applet implements MessageReceivedEvent,
		TreeSelectionListener {
	Hashtable logs = new Hashtable();
	DefaultListModel filters = new DefaultListModel();
	String currentCategory = "";
	boolean isStandalone = false;
	JTabbedPane jTabbedPane1 = new JTabbedPane();
	JPanel jPanel1 = new JPanel();
	JScrollPane jScrollPane1 = new JScrollPane();
	JScrollPane treeScrollPane = new JScrollPane();
	JTextArea jTextArea1 = new JTextArea();
	JTextPane jTextPane1 = new JTextPane();
	JPanel jPanel2 = new JPanel();
	JSplitPane jSplitPane1 = new JSplitPane();
	JList listFilter = new JList(filters);
	GridLayout gridLayout1 = new GridLayout();
	GridLayout gridLayout2 = new GridLayout();

	// JTree categoryTree = new JTree();
	// Get a parameter value
	public String getParameter(String key, String def) {
		return isStandalone ? System.getProperty(key, def)
				: (getParameter(key) != null ? getParameter(key) : def);
	}

	// Construct the applet
	public LogViewer() {
	}

	// Initialize the applet
	public void init() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Component initialization
	private void jbInit() throws Exception {
		this.setLayout(gridLayout1);
		jPanel1.setLayout(gridLayout2);
		jTextArea1.setBorder(BorderFactory.createLoweredBevelBorder());

		jTextArea1.setText("");
		jSplitPane1.setForeground(Color.magenta);
		jSplitPane1.setLastDividerLocation(150);
		jSplitPane1.setLeftComponent(listFilter);
		jSplitPane1.setOneTouchExpandable(true);
		jTabbedPane1.add(jPanel1, "Real-time Logs");
		jPanel1.add(jSplitPane1, null);
		jSplitPane1.add(jScrollPane1, JSplitPane.RIGHT);
		jScrollPane1.getViewport().add(jTextArea1);
		jTabbedPane1.add(jPanel2, "Log Files");
		this.add(jTabbedPane1, null);
		jTabbedPane1.setSelectedComponent(jPanel1);
		jSplitPane1.setDividerLocation(150);

	}

	// Start the applet
	public void start() {
		startClient();
	}

	// Stop the applet
	public void stop() {
	}

	// Destroy the applet
	public void destroy() {
	}

	// Get Applet information
	public String getAppletInfo() {
		return "Applet Information";
	}

	// Get parameter info
	public String[][] getParameterInfo() {
		return null;
	}

	// Main method
	public static void main(String[] args) {
		LogViewer applet = new LogViewer();
		applet.isStandalone = true;

		Frame frame;
		frame = new Frame();
		frame.setTitle("Applet Frame");

		frame.add(applet, BorderLayout.CENTER);

		applet.init();
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
		frame.setVisible(true);

	}

	public void startClient() {
		// get categories
		try {
			Socket cats = new Socket("localhost", 50500);

			BufferedReader br = new BufferedReader(new InputStreamReader(cats.getInputStream()));
			// cats.getOutputStream().write(("test").getBytes());
			fillTree(br.readLine());

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

		tcpClient tcpclient = new tcpClient("localhost", 5077, this);
		tcpclient.start();
	}

	private void fillTree(String categoryDump) {
		System.out.println(categoryDump);
		String[] categories = categoryDump.toString().split("\\|");
		Hashtable heirarchy = new Hashtable();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Categories");
		for (int i = 0; i < categories.length; i++) {
			if (categories[i].length() > 0) {
				String[] ns = categories[i].split("\\.");
				DefaultMutableTreeNode temproot = root;
				for (int j = 0; j < ns.length; j++) {
					DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(ns[j]);
					if (heirarchy.get(ns[j]) != null) {

						temproot = (DefaultMutableTreeNode) heirarchy.get(ns[j]);
					} else {
						heirarchy.put(ns[j], newNode);
						temproot.add(newNode);
						temproot = newNode;
					}
				}
			}

		}
		JTree categoryTree = new JTree(root);
		categoryTree.addTreeSelectionListener(this);
		jSplitPane1.add(treeScrollPane, JSplitPane.LEFT);
		treeScrollPane.getViewport().add(categoryTree);
		jSplitPane1.setDividerLocation(150);
	}

	public void MessageReceived(String sourceName, String message, Socket socket) {
		String text = "";
		if (logs.get(sourceName) != null) {
			text = (String) logs.get(sourceName);

		} else {
		}
		text += message + "\r\n";
		logs.put(sourceName, text);
		if (sourceName.equals(this.currentCategory)) {
			jTextArea1.setText((String) logs.get(sourceName));
			try {
				// JScrollBar sb = jScrollPane1.getVerticalScrollBar();
				// sb.setValue(sb.getMaximum());
				jTextArea1.setCaretPosition(((String) logs.get(sourceName)).length());
			} catch (Exception e) {

			}
		}
	}

	public void valueChanged(TreeSelectionEvent e) {
		System.out.println(e.getPath());
		String source = formatTreePath(e.getPath().toString());
		this.currentCategory = source;
		jTextArea1.setText((String) logs.get(source));
		try {
			jTextArea1.setCaretPosition(((String) logs.get(source)).length());
		} catch (Exception ex) {

		}
	}

	private String formatTreePath(String path) {
		String retVal = path;
		retVal = retVal.replaceAll("\\[", "").replaceAll(", ", "\\.").replaceAll("\\]", "").replaceAll("Categories.", "");
		return retVal;
	}
}
