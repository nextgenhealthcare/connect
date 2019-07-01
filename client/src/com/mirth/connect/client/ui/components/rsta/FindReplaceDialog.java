/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.util.DisplayUtil;

public class FindReplaceDialog extends MirthDialog {

    private MirthRSyntaxTextArea textArea;

    public FindReplaceDialog(MirthRSyntaxTextArea textArea) {
        super(PlatformUI.MIRTH_FRAME, "Find / Replace", true);
        this.textArea = textArea;
        initComponents();
        initLayout();
        setProperties();
        DisplayUtil.setResizable(this, false);
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(PlatformUI.MIRTH_FRAME);
        setVisible(true);
    }

    private void find() {
        search(true, false);
    }

    private void replace() {
        search(false, false);
    }

    private void replaceAll() {
        search(false, true);
    }

    private void search(boolean find, boolean replaceAll) {
        warningLabel.setText("");
        if (StringUtils.isEmpty((String) findComboBox.getSelectedItem())) {
            return;
        }

        SearchContext context = getContext();
        SearchResult result = getResult(context, find, replaceAll);
        boolean wrapped = false;

        if (result.getCount() == 0 && wrapSearchCheckBox.isSelected()) {
            int position = textArea.getCaretPosition();
            textArea.setCaretPosition(directionForwardRadio.isSelected() ? 0 : textArea.getDocument().getLength());
            result = getResult(context, find, replaceAll);
            if (result.getCount() == 0) {
                textArea.setCaretPosition(position);
            } else {
                wrapped = true;
                Toolkit.getDefaultToolkit().beep();
            }
        }

        if ((find || !replaceAll) && result.getMarkedCount() == 0 || replaceAll && result.getCount() == 0) {
            warningLabel.setText("No results found.");
        } else if (find) {
            warningLabel.setText(result.getMarkedCount() + " results found" + (wrapped ? "; wrapped." : "."));
        } else {
            warningLabel.setText(result.getCount() + " out of " + (replaceAll ? result.getCount() : result.getMarkedCount()) + " results replaced.");
        }

        updateProperties();
    }

    private SearchContext getContext() {
        SearchContext context = new SearchContext();
        context.setSearchFor((String) findComboBox.getSelectedItem());
        context.setReplaceWith((String) replaceComboBox.getSelectedItem());
        context.setSearchForward(directionForwardRadio.isSelected());
        context.setMatchCase(matchCaseCheckBox.isSelected());
        context.setRegularExpression(regularExpressionCheckBox.isSelected());
        context.setWholeWord(wholeWordCheckBox.isSelected());
        return context;
    }

    private SearchResult getResult(SearchContext context, boolean find, boolean replaceAll) {
        if (find) {
            return SearchEngine.find(textArea, context);
        } else if (!replaceAll) {
            return SearchEngine.replace(textArea, context);
        } else {
            return SearchEngine.replaceAll(textArea, context);
        }
    }

    private void setProperties() {
        FindReplaceProperties findReplaceProperties = MirthRSyntaxTextArea.getRSTAPreferences().getFindReplaceProperties();

        List<String> findHistory = findReplaceProperties.getFindHistory();
        findComboBox.setModel(new DefaultComboBoxModel(findHistory.toArray(new String[findHistory.size()])));
        if (StringUtils.isNotEmpty(textArea.getSelectedText())) {
            findComboBox.setSelectedItem(textArea.getSelectedText());
        }

        List<String> replaceHistory = findReplaceProperties.getReplaceHistory();
        replaceComboBox.setModel(new DefaultComboBoxModel(replaceHistory.toArray(new String[replaceHistory.size()])));

        if (findReplaceProperties.isForward()) {
            directionForwardRadio.setSelected(true);
        } else {
            directionBackwardRadio.setSelected(true);
        }

        wrapSearchCheckBox.setSelected(findReplaceProperties.isWrapSearch());
        matchCaseCheckBox.setSelected(findReplaceProperties.isMatchCase());
        regularExpressionCheckBox.setSelected(findReplaceProperties.isRegularExpression());
        wholeWordCheckBox.setSelected(findReplaceProperties.isWholeWord());

        if (!textArea.isEditable()) {
            replaceLabel.setEnabled(false);
            replaceComboBox.setEnabled(false);
            replaceButton.setEnabled(false);
            replaceAllButton.setEnabled(false);
        }
    }

    private void updateProperties() {
        FindReplaceProperties findReplaceProperties = MirthRSyntaxTextArea.getRSTAPreferences().getFindReplaceProperties();

        String findText = (String) findComboBox.getSelectedItem();
        List<String> findHistory = findReplaceProperties.getFindHistory();
        findHistory.remove(findText);
        findHistory.add(0, findText);
        while (findHistory.size() > 10) {
            findHistory.remove(10);
        }
        findComboBox.setModel(new DefaultComboBoxModel(findHistory.toArray(new String[findHistory.size()])));
        findComboBox.setSelectedIndex(0);

        String replaceText = (String) replaceComboBox.getSelectedItem();
        if (StringUtils.isNotEmpty(replaceText)) {
            List<String> replaceHistory = findReplaceProperties.getReplaceHistory();
            replaceHistory.remove(replaceText);
            replaceHistory.add(0, replaceText);
            while (replaceHistory.size() > 10) {
                replaceHistory.remove(10);
            }
            replaceComboBox.setModel(new DefaultComboBoxModel(replaceHistory.toArray(new String[replaceHistory.size()])));
            replaceComboBox.setSelectedIndex(0);
        }

        findReplaceProperties.setForward(directionForwardRadio.isSelected());
        findReplaceProperties.setWrapSearch(wrapSearchCheckBox.isSelected());
        findReplaceProperties.setMatchCase(matchCaseCheckBox.isSelected());
        findReplaceProperties.setRegularExpression(regularExpressionCheckBox.isSelected());
        findReplaceProperties.setWholeWord(wholeWordCheckBox.isSelected());

        MirthRSyntaxTextArea.updateFindReplacePreferences();
    }

    private void initComponents() {
        setLayout(new MigLayout("insets 11, novisualpadding, hidemode 3, fill, gap 6"));
        setBackground(UIConstants.COMBO_BOX_BACKGROUND);
        getContentPane().setBackground(getBackground());

        findPanel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap 6"));
        findPanel.setBackground(getBackground());

        ActionListener findActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                find();
            }
        };

        findLabel = new JLabel("Find text:");
        findComboBox = new JComboBox<String>();
        findComboBox.setEditable(true);
        findComboBox.setBackground(UIConstants.BACKGROUND_COLOR);

        findComboBox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(final KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER && evt.getModifiers() == 0) {
                    find();
                }
            }
        });

        ActionListener replaceActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                replace();
            }
        };

        replaceLabel = new JLabel("Replace with:");
        replaceComboBox = new JComboBox<String>();
        replaceComboBox.setEditable(true);
        replaceComboBox.setBackground(UIConstants.BACKGROUND_COLOR);

        directionPanel = new JPanel(new MigLayout("insets 8, novisualpadding, hidemode 3, fill, gap 6"));
        directionPanel.setBackground(getBackground());
        directionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)), "Direction", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("宋体", 1, 11)));

        ButtonGroup directionButtonGroup = new ButtonGroup();

        directionForwardRadio = new JRadioButton("Forward");
        directionForwardRadio.setBackground(directionPanel.getBackground());
        directionButtonGroup.add(directionForwardRadio);

        directionBackwardRadio = new JRadioButton("Backward");
        directionBackwardRadio.setBackground(directionPanel.getBackground());
        directionButtonGroup.add(directionBackwardRadio);

        optionsPanel = new JPanel(new MigLayout("insets 8, novisualpadding, hidemode 3, fill, gap 6"));
        optionsPanel.setBackground(getBackground());
        optionsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)), "Options", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("宋体", 1, 11)));

        wrapSearchCheckBox = new JCheckBox("Wrap Search");
        matchCaseCheckBox = new JCheckBox("Match Case");
        regularExpressionCheckBox = new JCheckBox("Regular Expression");
        wholeWordCheckBox = new JCheckBox("Whole Word");

        findButton = new JButton("Find");
        findButton.addActionListener(findActionListener);

        replaceButton = new JButton("Replace");
        replaceButton.addActionListener(replaceActionListener);

        replaceAllButton = new JButton("Replace All");
        replaceAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                replaceAll();
            }
        });

        warningLabel = new JLabel();

        closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
    }

    private void initLayout() {
        findPanel.add(findLabel, "right");
        findPanel.add(findComboBox, "grow, pushx");
        findPanel.add(replaceLabel, "newline, right");
        findPanel.add(replaceComboBox, "grow, pushx");
        add(findPanel, "grow");

        directionPanel.add(directionForwardRadio, "w 96!");
        directionPanel.add(directionBackwardRadio);
        add(directionPanel, "newline, sx, grow");

        optionsPanel.add(wrapSearchCheckBox);
        optionsPanel.add(matchCaseCheckBox);
        optionsPanel.add(regularExpressionCheckBox, "newline");
        optionsPanel.add(wholeWordCheckBox);
        add(optionsPanel, "newline, sx, grow, push");

        add(findButton, "newline, split 3, w 81!");
        add(replaceButton, "w 81!");
        add(replaceAllButton, "w 81!");

        add(new JSeparator(), "newline, sx, grow");

        add(warningLabel, "newline, split, growx, pushx");
        add(closeButton, "right, w 81!");
    }

    private JPanel findPanel;
    private JLabel findLabel;
    private JComboBox<String> findComboBox;
    private JLabel replaceLabel;
    private JComboBox<String> replaceComboBox;
    private JPanel directionPanel;
    private JRadioButton directionForwardRadio;
    private JRadioButton directionBackwardRadio;
    private JPanel optionsPanel;
    private JCheckBox matchCaseCheckBox;
    private JCheckBox regularExpressionCheckBox;
    private JCheckBox wholeWordCheckBox;
    private JCheckBox wrapSearchCheckBox;
    private JButton findButton;
    private JButton replaceButton;
    private JButton replaceAllButton;
    private JLabel warningLabel;
    private JButton closeButton;
}