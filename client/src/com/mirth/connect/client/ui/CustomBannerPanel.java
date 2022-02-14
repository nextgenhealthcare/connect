/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class CustomBannerPanel extends JPanel implements ActionListener {

    protected JTextArea textArea;
    protected JButton btnAccept, btnCancel;

    public CustomBannerPanel() {

        JPanel outerPane = new JPanel();
        Box box = Box.createVerticalBox();
        outerPane.setLayout(new BoxLayout(outerPane, BoxLayout.PAGE_AXIS));
        textArea = new JTextArea(20, 50);
        textArea.setEditable(false);
        textArea.setText("aaaaa");
        JScrollPane scrollPane = new JScrollPane(textArea);

        btnAccept = new JButton("Accept");
        btnAccept.setVerticalTextPosition(AbstractButton.BOTTOM);
        btnAccept.setHorizontalTextPosition(AbstractButton.LEFT); //aka LEFT, for left-to-right locales
        btnAccept.setActionCommand("accept");

        btnCancel = new JButton("Cancel");
        btnCancel.setVerticalTextPosition(AbstractButton.BOTTOM);
        btnCancel.setHorizontalTextPosition(AbstractButton.RIGHT);
        btnAccept.setActionCommand("cancel");

        btnAccept.addActionListener(this);
        btnCancel.addActionListener(this);

        box.add(scrollPane);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(btnAccept);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(btnCancel);

        box.add(buttonPane);

        add(box);

    }

    public void actionPerformed(ActionEvent e) {
        if ("accept".equals(e.getActionCommand())) {
        } else {
        }
    }

    /**
     * Create the GUI and show it. For thread safety, this method should be invoked from the event
     * dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Custom Banner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add contents to the window.
        frame.add(new CustomBannerPanel());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
