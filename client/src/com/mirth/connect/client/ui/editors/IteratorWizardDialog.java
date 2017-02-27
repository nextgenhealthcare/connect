/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;

import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.model.FilterTransformer;
import com.mirth.connect.model.FilterTransformerElement;
import com.mirth.connect.model.IteratorElement;
import com.mirth.connect.model.IteratorProperties;
import com.mirth.connect.util.JavaScriptSharedUtil;
import com.mirth.connect.util.JavaScriptSharedUtil.ExprPart;

public class IteratorWizardDialog<T extends FilterTransformer<C>, C extends FilterTransformerElement> extends MirthDialog {

    private static final Color GREEN = new Color(0, 169, 0);

    private String targetSuffix;
    private List<ExprPart> targetComponents;
    private String indexVariable;
    private List<String> ancestorIndexVariables;
    private List<String> descendantIndexVariables;
    private FilterTransformerTreeTableNode<T, C> selectedNode;
    private DefaultTreeTableModel treeTableModel;
    private TreeTableNode root;
    private List<IteratorEntry> iteratorEntries;
    private String outbound;
    private List<ExprPart> outboundComponents;
    private boolean accepted;
    private int preferredMaxDepth = -1;
    private TreePath preferredPath = null;

    @SuppressWarnings("unchecked")
    public IteratorWizardDialog(String target, FilterTransformerTreeTableNode<T, C> selectedNode, TreeTableNode parent, DefaultTreeTableModel treeTableModel, String outbound) {
        super(PlatformUI.MIRTH_FRAME, "Iterator Wizard", true);
        this.selectedNode = selectedNode;
        this.treeTableModel = treeTableModel;
        this.root = (TreeTableNode) treeTableModel.getRoot();
        this.outbound = outbound;

        ancestorIndexVariables = IteratorUtil.getAncestorIndexVariables(parent);
        descendantIndexVariables = IteratorUtil.getDescendantIndexVariables(selectedNode);
        indexVariable = IteratorUtil.getValidIndexVariable(ancestorIndexVariables, descendantIndexVariables);

        iteratorEntries = new ArrayList<IteratorEntry>();
        fillIteratorEntries();

        if (StringUtils.endsWith(target, ".toString()")) {
            targetSuffix = ".toString()";
            target = StringUtils.removeEnd(target, targetSuffix);
        }
        targetComponents = JavaScriptSharedUtil.getExpressionParts(target, false);

        if (outbound != null) {
            outboundComponents = JavaScriptSharedUtil.getExpressionParts(outbound, false);
        } else {
            outboundComponents = new ArrayList<ExprPart>();
        }

        initComponents();
        initLayout();

        IteratorEntry selectedEntry = null;

        if (!iteratorEntries.isEmpty()) {
            // Find the preferred iterator
            FilterTransformerTreeTableNode<T, C> preferred = null;

            if (parent instanceof FilterTransformerTreeTableNode) {
                preferred = (FilterTransformerTreeTableNode<T, C>) parent;
            } else {
                findPreferred(IteratorUtil.removeIteratorVariables(target, parent));
                if (preferredPath != null) {
                    preferred = (FilterTransformerTreeTableNode<T, C>) preferredPath.getLastPathComponent();
                }
            }

            if (preferred != null) {
                for (IteratorEntry entry : iteratorEntries) {
                    if (Objects.equals(entry.getPath().getLastPathComponent(), preferred)) {
                        selectedEntry = entry;
                        break;
                    }
                }

                if (selectedEntry != null) {
                    iteratorComboBox.setSelectedItem(selectedEntry);
                }
            }
        }

        if (selectedEntry != null || targetComponents.isEmpty()) {
            chooseExistingRadio.setSelected(true);
            iteratorRadioActionPerformed(false);
        } else {
            createNewRadio.setSelected(true);
            iteratorRadioActionPerformed(true);
        }

        if (!targetComponents.isEmpty()) {
            // Find the first index after all ancestor Iterator index variables are located
            int minIndex = 0;
            if (!ancestorIndexVariables.isEmpty()) {
                minIndex = targetComponents.size();
                for (int i = targetComponents.size() - 1; i >= 0; i--) {
                    if (ancestorIndexVariables.contains(targetComponents.get(i).getPropertyName())) {
                        break;
                    }
                    minIndex = i;
                }
            }

            // Find the index to select in the target, starting at the minIndex and preferring something > 0
            int index = -1;
            for (int i = minIndex; i < targetComponentRadioButtons.size(); i++) {
                if (targetComponentRadioButtons.get(i).isVisible()) {
                    index = i;
                    if (index > 0) {
                        break;
                    }
                }
            }
            // If nothing was found, find an alternative index
            if (index < 0) {
                for (int i = 0; i < minIndex; i++) {
                    if (targetComponentRadioButtons.get(i).isVisible()) {
                        index = i;
                        if (index > 0) {
                            break;
                        }
                    }
                }
            }

            // Select the target radio button
            if (index >= 0) {
                targetComponentRadioButtons.get(index).setSelected(true);
                targetComponentRadioButtonActionPerformed(index);
            }

            // Make sure that at least one outbound radio button is selected
            if (!outboundComponents.isEmpty() && (index >= outboundComponents.size() || !outboundComponentRadioButtons.get(index).isVisible())) {
                for (int i = 0; i < outboundComponentRadioButtons.size(); i++) {
                    if (outboundComponentRadioButtons.get(i).isVisible()) {
                        outboundComponentRadioButtons.get(i).setSelected(true);
                        outboundComponentRadioButtonActionPerformed(i);
                        break;
                    }
                }
            }
        }

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(PlatformUI.MIRTH_FRAME);
        okButton.requestFocus();
        setVisible(true);
    }

    private void fillIteratorEntries() {
        for (Enumeration<? extends TreeTableNode> en = root.children(); en.hasMoreElements();) {
            fillIteratorEntries(en.nextElement(), 0);
        }
    }

    @SuppressWarnings("unchecked")
    private void fillIteratorEntries(TreeTableNode parent, int depth) {
        if (parent instanceof FilterTransformerTreeTableNode && ((FilterTransformerTreeTableNode<T, C>) parent).isIteratorNode() && (selectedNode == null || !Objects.equals(parent, selectedNode))) {
            iteratorEntries.add(new IteratorEntry(new TreePath(treeTableModel.getPathToRoot(parent)), depth));
            for (Enumeration<? extends TreeTableNode> en = parent.children(); en.hasMoreElements();) {
                fillIteratorEntries(en.nextElement(), depth + 1);
            }
        }
    }

    private void findPreferred(String expression) {
        for (Enumeration<? extends TreeTableNode> en = root.children(); en.hasMoreElements();) {
            findPreferred(en.nextElement(), expression, 0);
        }
    }

    @SuppressWarnings("unchecked")
    private void findPreferred(TreeTableNode parent, String expression, int depth) {
        if (parent instanceof FilterTransformerTreeTableNode && (selectedNode == null || !Objects.equals(parent, selectedNode))) {
            FilterTransformerTreeTableNode<T, C> node = (FilterTransformerTreeTableNode<T, C>) parent;
            if (node.isIteratorNode()) {
                IteratorElement<C> iterator = (IteratorElement<C>) node.getElement();
                for (String prefix : iterator.getProperties().getPrefixSubstitutions()) {
                    if (StringUtils.startsWith(expression, prefix)) {
                        if (depth > preferredMaxDepth) {
                            preferredPath = new TreePath(treeTableModel.getPathToRoot(node));
                            preferredMaxDepth = depth;
                        }
                        String replaced = prefix + "[" + iterator.getProperties().getIndexVariable() + "]" + StringUtils.removeStart(expression, prefix);
                        for (Enumeration<? extends TreeTableNode> en = parent.children(); en.hasMoreElements();) {
                            findPreferred(en.nextElement(), replaced, depth + 1);
                        }
                    }
                }
            }
        }
    }

    public boolean wasAccepted() {
        return accepted;
    }

    public boolean isCreateNew() {
        return iteratorEntries.isEmpty() || createNewRadio.isSelected();
    }

    @SuppressWarnings("unchecked")
    public FilterTransformerTreeTableNode<T, C> getSelectedParent() {
        IteratorEntry entry = (IteratorEntry) iteratorComboBox.getSelectedItem();
        return (FilterTransformerTreeTableNode<T, C>) entry.getPath().getLastPathComponent();
    }

    public void fillIteratorProperties(IteratorProperties<C> props) {
        if (!targetComponents.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < targetComponentRadioButtons.size(); i++) {
                builder.append(targetComponents.get(i).getValue());
                if (targetComponentRadioButtons.get(i).isSelected()) {
                    break;
                }
            }
            props.setTarget(builder.toString());
            props.getPrefixSubstitutions().add(props.getTarget());

            if (!outboundComponents.isEmpty()) {
                builder = new StringBuilder();
                for (int i = 0; i < outboundComponentRadioButtons.size(); i++) {
                    builder.append(outboundComponents.get(i).getValue());
                    if (outboundComponentRadioButtons.get(i).isSelected()) {
                        break;
                    }
                }
                props.getPrefixSubstitutions().add(builder.toString());
            } else if (StringUtils.equals(targetComponents.get(0).getValue(), "msg") && StringUtils.startsWith(props.getTarget(), "msg")) {
                props.getPrefixSubstitutions().add("tmp" + StringUtils.removeStart(props.getTarget(), "msg"));
            }
        }

        props.setIndexVariable(indexVariable);
    }

    private void initComponents() {
        setBackground(UIConstants.COMBO_BOX_BACKGROUND);
        getContentPane().setBackground(getBackground());

        ButtonGroup selectIteratorButtonGroup = new ButtonGroup();

        createNewRadio = new JRadioButton("Create New Iterator");
        createNewRadio.setBackground(getBackground());
        createNewRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                iteratorRadioActionPerformed(true);
            }
        });
        selectIteratorButtonGroup.add(createNewRadio);

        chooseExistingRadio = new JRadioButton("Choose Existing Iterator");
        chooseExistingRadio.setBackground(getBackground());
        chooseExistingRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                iteratorRadioActionPerformed(false);
            }
        });
        selectIteratorButtonGroup.add(chooseExistingRadio);

        iteratorComboBox = new JComboBox<IteratorEntry>(new Vector<IteratorWizardDialog<T, C>.IteratorEntry>(iteratorEntries));

        createNewWarningLabel = new JLabel("<html>Not enough information available to select iteration target. A new blank Iterator will be added.</html>");
        createNewWarningLabel.setVisible(false);

        createNewPanel = new JPanel();
        createNewPanel.setBackground(getBackground());

        targetLabel = new JLabel("Select the part of the object to iterate on:");

        targetComponentLabels = new ArrayList<JLabel>();
        targetComponentRadioButtons = new ArrayList<JRadioButton>();
        ButtonGroup targetButtonGroup = new ButtonGroup();

        for (int i = 0; i < targetComponents.size(); i++) {
            final int index = i;
            ExprPart component = targetComponents.get(index);

            JLabel label = new JLabel(component.getValue());
            targetComponentLabels.add(label);

            JRadioButton radioButton = new JRadioButton();
            radioButton.setBackground(getBackground());
            radioButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    targetComponentRadioButtonActionPerformed(index);
                }
            });

            if (i + 1 < targetComponents.size() && ancestorIndexVariables.contains(targetComponents.get(i + 1).getPropertyName())) {
                radioButton.setVisible(false);
                radioButton.setEnabled(false);
            } else {
                targetButtonGroup.add(radioButton);
            }

            targetComponentRadioButtons.add(radioButton);
        }

        targetPanel = new JPanel();
        targetPanel.setBackground(getBackground());

        targetScrollPane = new JScrollPane(targetPanel);
        targetScrollPane.setBorder(BorderFactory.createEmptyBorder());

        if (!outboundComponents.isEmpty()) {
            outboundLabel = new JLabel("Select the part of the outbound message to iterate through:");

            outboundComponentLabels = new ArrayList<JLabel>();
            outboundComponentRadioButtons = new ArrayList<JRadioButton>();
            ButtonGroup outboundButtonGroup = new ButtonGroup();

            for (int i = 0; i < outboundComponents.size(); i++) {
                final int index = i;
                ExprPart component = outboundComponents.get(index);

                JLabel label = new JLabel(component.getValue());
                outboundComponentLabels.add(label);

                JRadioButton radioButton = new JRadioButton();
                radioButton.setBackground(getBackground());
                radioButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        outboundComponentRadioButtonActionPerformed(index);
                    }
                });

                if (i + 1 < outboundComponents.size() && ancestorIndexVariables.contains(outboundComponents.get(i + 1).getPropertyName())) {
                    radioButton.setVisible(false);
                    radioButton.setEnabled(false);
                } else {
                    outboundButtonGroup.add(radioButton);
                }

                outboundComponentRadioButtons.add(radioButton);
            }

            outboundPanel = new JPanel();
            outboundPanel.setBackground(getBackground());

            outboundScrollPane = new JScrollPane(outboundPanel);
            outboundScrollPane.setBorder(BorderFactory.createEmptyBorder());
        }

        substitutionsLabel = new JLabel("The following drag-and-drop substitutions will be made:");

        substitutionsTargetLabel1 = new JLabel();
        substitutionsTargetLabel2 = new JLabel(" -> ");
        substitutionsTargetLabel3 = new JLabel();
        substitutionsTargetIndexLabel = new JLabel("<html><b>[" + StringEscapeUtils.escapeHtml4(indexVariable) + "]</b></html>");
        substitutionsTargetIndexLabel.setForeground(GREEN);
        substitutionsTargetLabel4 = new JLabel();

        if (!outboundComponents.isEmpty()) {
            substitutionsOutboundLabel1 = new JLabel();
            substitutionsOutboundLabel2 = new JLabel(" -> ");
            substitutionsOutboundLabel3 = new JLabel();
            substitutionsOutboundIndexLabel = new JLabel("<html><b>[" + StringEscapeUtils.escapeHtml4(indexVariable) + "]</b></html>");
            substitutionsOutboundIndexLabel.setForeground(GREEN);
            substitutionsOutboundLabel4 = new JLabel();
        }

        substitutionsPanel = new JPanel();
        substitutionsPanel.setBackground(getBackground());

        substitutionsScrollPane = new JScrollPane(substitutionsPanel);
        substitutionsScrollPane.setBorder(BorderFactory.createEmptyBorder());

        separator = new JSeparator(SwingConstants.HORIZONTAL);

        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                accepted = true;
                dispose();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3"));

        if (!iteratorEntries.isEmpty()) {
            add(createNewRadio, "split 2");
            add(chooseExistingRadio);
            add(iteratorComboBox, "newline, sx, growx, w 100:");
        }

        add(createNewWarningLabel, "newline, grow");

        createNewPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, gapx 0"));

        createNewPanel.add(targetLabel, "sx");

        targetPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 0, gap 0"));
        for (JLabel label : targetComponentLabels) {
            targetPanel.add(label, "align center");
        }
        for (int i = 0; i < targetComponentRadioButtons.size(); i++) {
            targetPanel.add(targetComponentRadioButtons.get(i), (i == 0 ? "newline, " : "") + "align center");
        }
        createNewPanel.add(targetScrollPane, "newline, gapleft 30, growx, h 32:50, sx");

        if (!outboundComponents.isEmpty()) {
            createNewPanel.add(outboundLabel, "newline, sx");

            outboundPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 0, gap 0"));
            for (JLabel label : outboundComponentLabels) {
                outboundPanel.add(label, "align center");
            }
            for (int i = 0; i < outboundComponentRadioButtons.size(); i++) {
                outboundPanel.add(outboundComponentRadioButtons.get(i), (i == 0 ? "newline, " : "") + "align center");
            }
            createNewPanel.add(outboundScrollPane, "newline, gapleft 30, growx, h 32:50, sx");
        }

        createNewPanel.add(substitutionsLabel, "newline, sx");

        substitutionsPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 0, gap 0"));

        substitutionsPanel.add(substitutionsTargetLabel1);
        substitutionsPanel.add(substitutionsTargetLabel2);
        substitutionsPanel.add(substitutionsTargetLabel3, "split 3, gapright 0");
        substitutionsPanel.add(substitutionsTargetIndexLabel, "gapleft 0, gapright 0");
        substitutionsPanel.add(substitutionsTargetLabel4, "gapleft 0");

        if (!outboundComponents.isEmpty()) {
            substitutionsPanel.add(substitutionsOutboundLabel1, "newline");
            substitutionsPanel.add(substitutionsOutboundLabel2);
            substitutionsPanel.add(substitutionsOutboundLabel3, "split 3, gapright 0");
            substitutionsPanel.add(substitutionsOutboundIndexLabel, "gapleft 0, gapright 0");
            substitutionsPanel.add(substitutionsOutboundLabel4, "gapleft 0");
        }

        createNewPanel.add(substitutionsScrollPane, "newline, sx, growx, h " + (!outboundComponents.isEmpty() ? "32:50" : "14:32") + ", gapleft 30");

        add(createNewPanel, "newline, grow");

        add(new JPanel(), "newline, grow, sx, push, h 0:0");

        add(separator, "newline, growx, sx, pushx");

        add(okButton, "newline, right, sx, split 2, w 45!");
        add(cancelButton, "w 45!");
    }

    private void targetComponentRadioButtonActionPerformed(int index) {
        StringBuilder builder1 = new StringBuilder();
        StringBuilder builder2 = new StringBuilder();
        StringBuilder builder3 = new StringBuilder();
        substitutionsTargetIndexLabel.setVisible(false);

        for (int i = 0; i < targetComponents.size(); i++) {
            ExprPart component = targetComponents.get(i);
            JLabel label = targetComponentLabels.get(i);

            if (i <= index) {
                label.setText("<html><b>" + StringEscapeUtils.escapeHtml4(component.getValue()) + "</b></html>");
                label.setForeground(GREEN);
            } else {
                label.setText(component.getValue());
                label.setForeground(Color.BLACK);
            }

            builder1.append(component.getValue());
            if (i <= index) {
                builder2.append(component.getValue());
                if (i == index) {
                    substitutionsTargetIndexLabel.setVisible(true);
                }
            } else {
                builder3.append(component.getValue());
            }
        }

        substitutionsTargetLabel1.setText(builder1.toString());
        substitutionsTargetLabel3.setText(builder2.toString());
        substitutionsTargetLabel4.setText(builder3.toString());

        if (!outboundComponents.isEmpty() && index < outboundComponents.size() && outboundComponentRadioButtons.get(index).isVisible()) {
            outboundComponentRadioButtons.get(index).setSelected(true);
            outboundComponentRadioButtonActionPerformed(index);
        }
    }

    private void outboundComponentRadioButtonActionPerformed(int index) {
        StringBuilder builder1 = new StringBuilder();
        StringBuilder builder2 = new StringBuilder();
        StringBuilder builder3 = new StringBuilder();
        substitutionsOutboundIndexLabel.setVisible(false);

        for (int i = 0; i < outboundComponents.size(); i++) {
            ExprPart component = outboundComponents.get(i);
            JLabel label = outboundComponentLabels.get(i);

            if (i <= index) {
                label.setText("<html><b>" + StringEscapeUtils.escapeHtml4(component.getValue()) + "</b></html>");
                label.setForeground(GREEN);
            } else {
                label.setText(component.getValue());
                label.setForeground(Color.BLACK);
            }

            builder1.append(component.getValue());
            if (i <= index) {
                builder2.append(component.getValue());
                if (i == index) {
                    substitutionsOutboundIndexLabel.setVisible(true);
                }
            } else {
                builder3.append(component.getValue());
            }
        }

        substitutionsOutboundLabel1.setText(builder1.toString());
        substitutionsOutboundLabel3.setText(builder2.toString());
        substitutionsOutboundLabel4.setText(builder3.toString());
    }

    private void iteratorRadioActionPerformed(boolean createNew) {
        if (iteratorComboBox != null) {
            iteratorComboBox.setVisible(!createNew);
        }

        createNewPanel.setVisible(createNew && !targetComponents.isEmpty());
        createNewWarningLabel.setVisible(createNew && targetComponents.isEmpty());

        if (createNew && !targetComponents.isEmpty()) {
            setPreferredSize(new Dimension(500, !outboundComponents.isEmpty() ? 311 : 221));
        } else {
            setPreferredSize(new Dimension(487, 124));
        }
        pack();
    }

    private class IteratorEntry {
        private TreePath path;
        private int depth;

        public IteratorEntry(TreePath path, int depth) {
            this.path = path;
            this.depth = depth;
        }

        public TreePath getPath() {
            return path;
        }

        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        @SuppressWarnings("unchecked")
        public String toString() {
            return StringUtils.repeat('\t', Math.max(depth - 1, 0) * 8) + (depth > 0 ? "└─" : "") + ((FilterTransformerTreeTableNode<T, C>) path.getLastPathComponent()).getElement().getName();
        }
    }

    private JRadioButton createNewRadio;
    private JRadioButton chooseExistingRadio;
    private JComboBox<IteratorEntry> iteratorComboBox;

    private JLabel createNewWarningLabel;
    private JPanel createNewPanel;

    private JLabel targetLabel;
    private List<JLabel> targetComponentLabels;
    private List<JRadioButton> targetComponentRadioButtons;
    private JPanel targetPanel;
    private JScrollPane targetScrollPane;

    private JLabel outboundLabel;
    private List<JLabel> outboundComponentLabels;
    private List<JRadioButton> outboundComponentRadioButtons;
    private JPanel outboundPanel;
    private JScrollPane outboundScrollPane;

    private JLabel substitutionsLabel;
    private JLabel substitutionsTargetLabel1;
    private JLabel substitutionsTargetLabel2;
    private JLabel substitutionsTargetLabel3;
    private JLabel substitutionsTargetIndexLabel;
    private JLabel substitutionsTargetLabel4;
    private JLabel substitutionsOutboundLabel1;
    private JLabel substitutionsOutboundLabel2;
    private JLabel substitutionsOutboundLabel3;
    private JLabel substitutionsOutboundIndexLabel;
    private JLabel substitutionsOutboundLabel4;
    private JPanel substitutionsPanel;
    private JScrollPane substitutionsScrollPane;

    private JSeparator separator;
    private JButton okButton;
    private JButton cancelButton;
}