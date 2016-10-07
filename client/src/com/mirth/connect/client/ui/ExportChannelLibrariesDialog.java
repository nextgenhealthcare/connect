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
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringEscapeUtils;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;

public class ExportChannelLibrariesDialog extends MirthDialog {

    private int result = JOptionPane.CLOSED_OPTION;

    public ExportChannelLibrariesDialog(Channel channel) {
        super(PlatformUI.MIRTH_FRAME, true);
        initComponents(channel);
        initLayout();
        setMaximumSize(new Dimension(800, 600));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Select an Option");
        pack();
        setLocationRelativeTo(PlatformUI.MIRTH_FRAME);
        yesButton.requestFocus();
        setVisible(true);
    }

    public int getResult() {
        return result;
    }

    private void initComponents(Channel channel) {
        label1 = new JLabel("   The following code template libraries are linked to this channel:");
        label1.setIcon(UIManager.getIcon("OptionPane.questionIcon"));

        librariesTextPane = new JTextPane();
        librariesTextPane.setContentType("text/html");
        HTMLEditorKit editorKit = new HTMLEditorKit();
        StyleSheet styleSheet = editorKit.getStyleSheet();
        styleSheet.addRule(".export-channel-libraries-dialog {font-family:\"Tahoma\";font-size:11;text-align:top}");
        librariesTextPane.setEditorKit(editorKit);
        librariesTextPane.setEditable(false);
        librariesTextPane.setBackground(getBackground());
        librariesTextPane.setBorder(null);

        StringBuilder librariesText = new StringBuilder("<html><ul class=\"export-channel-libraries-dialog\">");
        for (CodeTemplateLibrary library : PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplateLibraries().values()) {
            if (library.getEnabledChannelIds().contains(channel.getId()) || (library.isIncludeNewChannels() && !library.getDisabledChannelIds().contains(channel.getId()))) {
                librariesText.append("<li>").append(StringEscapeUtils.escapeHtml4(library.getName())).append("</li>");
            }
        }
        librariesText.append("</ul></html>");
        librariesTextPane.setText(librariesText.toString());
        librariesTextPane.setCaretPosition(0);

        librariesScrollPane = new JScrollPane(librariesTextPane);

        label2 = new JLabel("Do you wish to include these libraries in the channel export?");

        alwaysChooseCheckBox = new JCheckBox("Always choose this option by default in the future (may be changed in the Administrator settings)");

        yesButton = new JButton("Yes");
        yesButton.setMnemonic('Y');
        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                result = JOptionPane.YES_OPTION;
                if (alwaysChooseCheckBox.isSelected()) {
                    Preferences.userNodeForPackage(Mirth.class).putBoolean("exportChannelCodeTemplateLibraries", true);
                }
                dispose();
            }
        });

        noButton = new JButton("No");
        noButton.setMnemonic('N');
        noButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                result = JOptionPane.NO_OPTION;
                if (alwaysChooseCheckBox.isSelected()) {
                    Preferences.userNodeForPackage(Mirth.class).putBoolean("exportChannelCodeTemplateLibraries", false);
                }
                dispose();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.setMnemonic('C');
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }
        });
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill, gap 10"));
        add(label1);
        add(librariesScrollPane, "newline, grow, push");
        add(label2, "newline");
        add(alwaysChooseCheckBox, "newline");
        add(yesButton, "newline, center, split 3, w 75!, h 24!");
        add(noButton, "gapbefore 6, w 75!, h 24!");
        add(cancelButton, "gapbefore 6, w 75!, h 24!");
    }

    private JLabel label1;
    private JTextPane librariesTextPane;
    private JScrollPane librariesScrollPane;
    private JLabel label2;
    private JCheckBox alwaysChooseCheckBox;
    private JButton yesButton;
    private JButton noButton;
    private JButton cancelButton;
}