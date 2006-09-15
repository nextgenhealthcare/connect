/*
 * A simple text editor that demonstrates the integration of the 
 * com.Ostermiller.Syntax Syntax Highlighting package with a text editor.
 * Copyright (C) 2001 Stephen Ostermiller 
 * http://ostermiller.org/contact.pl?regarding=Syntax+Highlighting
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See COPYING.TXT for details.
 */
 
package com.Ostermiller.Syntax;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A <a href="http://ostermiller.org/syntax/editor.html">demonstration text editor</a>
 * that uses syntax highlighting.
 */
public class ProgrammerEditorDemo extends JFrame {
    /** The document holding the text being edited. */
    private HighlightedDocument document = new HighlightedDocument();

	/** The text pane displaying the document. */
	private JTextPane textPane = new JTextPane(document);
	
    /**
     * Create a new Demo
     */
    public ProgrammerEditorDemo() {
        // initial set up that sets the title
        super("Programmer's Editor Demonstration");
        setLocation(50, 50);

        // Create a scroll pane wrapped around the text pane
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(620, 460));

        // Add the components to the frame.
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(scrollPane, BorderLayout.CENTER);
        setContentPane(contentPane);

        // Set up the menu bar.
        JMenu styleMenu = createStyleMenu();
        JMenuBar mb = new JMenuBar();
        mb.add(styleMenu);
        setJMenuBar(mb);

        // Make the window so that it can close the application
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
            public void windowActivated(WindowEvent e) {
                // focus magic
                textPane.requestFocus();
            }
        });

        // Put the initial text into the text pane and
        // set it's initial coloring style.
        initDocument();

        // put it all together and show it.
        pack();
        setVisible(true);
    }

	private class StyleMenuItem extends JRadioButtonMenuItem
			implements ActionListener {
		Object style;
		
		StyleMenuItem(String name, Object style) {
			super(name);
			this.style = style;
			addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent e) {
			document.setHighlightStyle(style);
		}
	}

    /**
	 * Create the style menu.
     *
     * @return the style menu.
     */
    private JMenu createStyleMenu() {
        JRadioButtonMenuItem[] items = {
            	new StyleMenuItem("Java",            HighlightedDocument.JAVA_STYLE),
            	new StyleMenuItem("C/C++",           HighlightedDocument.C_STYLE),
            	new StyleMenuItem("HTML (Simple)",   HighlightedDocument.HTML_STYLE),
            	new StyleMenuItem("HTML (Complex)",  HighlightedDocument.HTML_KEY_STYLE),
            	new StyleMenuItem("LaTeX",           HighlightedDocument.LATEX_STYLE),
            	new StyleMenuItem("SQL",             HighlightedDocument.SQL_STYLE),
            	new StyleMenuItem("Java Properties", HighlightedDocument.PROPERTIES_STYLE),
            	new StyleMenuItem("Plain",           HighlightedDocument.PLAIN_STYLE),
            	new StyleMenuItem("Grayed Out",      HighlightedDocument.GRAYED_OUT_STYLE),
        };

        JMenu menu = new JMenu("Style");
        ButtonGroup group = new ButtonGroup();
        for(int i = 0; i < items.length; i++) {
        	group.add(items[i]);
        	menu.add(items[i]);
        }
        return menu;
    }

    /**
     * Initialize the document with some default text and set
     * they initial type of syntax highlighting.
     */
    private void initDocument() {
        String initString = (
            "/**\n" +
            " * Simple common test program.\n" +
            " */\n" +
            "public class HelloWorld {\n" +
            "    public static void main(String[] args) {\n" +
            "        // Display the greeting.\n" +
            "        System.out.println(\"Hello World!\");\n" +
            "    }\n" +
            "}\n"
        );
        
        textPane.setText(initString);
    }

    /**
     * Run the demo.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        // create the demo
        ProgrammerEditorDemo frame = new ProgrammerEditorDemo();
    }
}
