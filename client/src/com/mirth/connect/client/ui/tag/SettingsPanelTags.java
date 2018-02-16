/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.tag;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.CellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.AbstractSettingsPanel;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthButton;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTriStateCheckBox;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.util.ColorUtil;

import net.miginfocom.swing.MigLayout;

public class SettingsPanelTags extends AbstractSettingsPanel {

    public static final String TAB_NAME = "Tags";

    private static int MAX_TAG_COUNT = 10;

    private static final int TAGS_NAME_COLUMN = 0;
    private static final int TAGS_BACKGROUND_COLUMN = 1;
    private static final int TAGS_CHANNEL_COUNT_COLUMN = 2;
    private static final int TAGS_TAG_COLUMN = 3;

    private static final int CHANNELS_SELECTED_COLUMN = 0;
    private static final int CHANNELS_NAME_COLUMN = 1;
    private static final int CHANNELS_ID_COLUMN = 2;

    private Set<ChannelTag> cachedChannelTags = new HashSet<ChannelTag>();
    private AtomicBoolean channelsTableAdjusting = new AtomicBoolean(false);

    public SettingsPanelTags(String tabName) {
        super(tabName);

        initComponents();
        initLayout();
    }

    public Set<ChannelTag> getCachedChannelTags() {
        return cachedChannelTags;
    }

    @Override
    public void doRefresh() {
        if (PlatformUI.MIRTH_FRAME.alertRefresh()) {
            return;
        }

        final String workingId = getFrame().startWorking("Loading tags...");
        final int[] selectedRows = tagsTable.getSelectedRows();

        SwingWorker<Set<ChannelTag>, Void> worker = new SwingWorker<Set<ChannelTag>, Void>() {

            @Override
            public Set<ChannelTag> doInBackground() throws ClientException {
                if (MapUtils.isEmpty(getFrame().channelPanel.getCachedChannelStatuses())) {
                    getFrame().channelPanel.retrieveChannels(false);
                }
                return getFrame().mirthClient.getChannelTags();
            }

            @Override
            public void done() {
                try {
                    updateTagsTable(get(), selectedRows, true);
                } catch (Throwable t) {
                    if (t instanceof ExecutionException) {
                        t = t.getCause();
                    }
                    getFrame().alertThrowable(getFrame(), t, "Error loading tags: " + t.toString());
                } finally {
                    getFrame().stopWorking(workingId);
                }
            }
        };

        worker.execute();
    }

    public void refresh() {
        try {
            updateTagsTable(getFrame().mirthClient.getChannelTags(), tagsTable.getSelectedRows(), false);
        } catch (Throwable t) {
            getFrame().alertThrowable(getFrame(), t, "Error loading tags: " + t.toString(), false);
        }
    }

    public void updateTagsTable(Set<ChannelTag> tags) {
        updateTagsTable(tags, null, false);
    }

    public void updateTagsTable(Set<ChannelTag> tags, int[] selectedRows, boolean clearSaveEnabled) {
        List<ChannelTag> tagList = new ArrayList<ChannelTag>(tags);
        Collections.sort(tagList, new Comparator<ChannelTag>() {
            @Override
            public int compare(ChannelTag o1, ChannelTag o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        cachedChannelTags = new LinkedHashSet<ChannelTag>(tagList);

        Object[][] data = new Object[tagList.size()][4];
        int i = 0;
        for (ChannelTag tag : tagList) {
            data[i][TAGS_NAME_COLUMN] = tag.getName();
            data[i][TAGS_BACKGROUND_COLUMN] = tag.getBackgroundColor();
            data[i][TAGS_CHANNEL_COUNT_COLUMN] = tag.getChannelIds().size();
            data[i][TAGS_TAG_COLUMN] = new ChannelTag(tag);
            i++;
        }
        ((RefreshTableModel) tagsTable.getModel()).refreshDataVector(data);

        channelsTableAdjusting.set(true);
        try {
            List<ChannelStatus> channels = new ArrayList<ChannelStatus>(getFrame().channelPanel.getCachedChannelStatuses().values());
            Object[][] channelData = new Object[channels.size()][3];
            i = 0;
            for (ChannelStatus channel : channels) {
                channelData[i][CHANNELS_SELECTED_COLUMN] = MirthTriStateCheckBox.UNCHECKED;
                channelData[i][CHANNELS_NAME_COLUMN] = channel.getChannel().getName();
                channelData[i][CHANNELS_ID_COLUMN] = channel.getChannel().getId();
                i++;
            }
            ((RefreshTableModel) channelsTable.getModel()).refreshDataVector(channelData);
        } finally {
            channelsTableAdjusting.set(false);
        }

        if (selectedRows != null) {
            tagsTable.clearSelection();
            for (int selectedRow : selectedRows) {
                if (selectedRow < tagsTable.getRowCount()) {
                    tagsTable.addRowSelectionInterval(selectedRow, selectedRow);
                }
            }
        } else if (tagsTable.getRowCount() > 0) {
            tagsTable.setRowSelectionInterval(0, 0);
        } else {
            tagsTable.clearSelection();
        }
        tagSelectionChanged();

        if (clearSaveEnabled) {
            setSaveEnabled(false);
        }
    }

    @Override
    public boolean doSave() {
        final String workingId = getFrame().startWorking("Saving tags...");
        final Set<ChannelTag> tags = new HashSet<ChannelTag>();

        for (int row = 0; row < tagsTable.getModel().getRowCount(); row++) {
            ChannelTag tag = (ChannelTag) tagsTable.getModel().getValueAt(row, TAGS_TAG_COLUMN);
            tag.setName((String) tagsTable.getModel().getValueAt(row, TAGS_NAME_COLUMN));
            tag.setBackgroundColor((Color) tagsTable.getModel().getValueAt(row, TAGS_BACKGROUND_COLUMN));
            tags.add(tag);
        }

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            @Override
            public Void doInBackground() throws ClientException {
                getFrame().mirthClient.setChannelTags(tags);
                return null;
            }

            @Override
            public void done() {
                try {
                    get();
                    setSaveEnabled(false);
                } catch (Throwable t) {
                    if (t instanceof ExecutionException) {
                        t = t.getCause();
                    }
                    getFrame().alertThrowable(getFrame(), t, "Error saving tags: " + t.toString());
                } finally {
                    getFrame().stopWorking(workingId);
                }
            }
        };

        worker.execute();

        return true;
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        container = new JPanel();
        container.setBackground(getBackground());
        container.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Tags", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        leftPanel = new JPanel();
        leftPanel.setBackground(getBackground());

        tagsTable = new MirthTable();
        tagsTable.setModel(new RefreshTableModel(new Object[] { "Name", "Color", "Channel Count",
                "Tag" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == TAGS_NAME_COLUMN || column == TAGS_BACKGROUND_COLUMN;
            }
        });

        tagsTable.setDragEnabled(false);
        tagsTable.setRowSelectionAllowed(true);
        tagsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tagsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        tagsTable.setFocusable(true);
        tagsTable.setOpaque(true);
        tagsTable.setEditable(true);
        tagsTable.setSortable(true);
        tagsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        tagsTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
        tagsTable.getActionMap().put("enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CellEditor editor = tagsTable.getCellEditor();
                if (editor != null) {
                    editor.stopCellEditing();
                }
            }
        });

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            tagsTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        tagsTable.getColumnExt(TAGS_NAME_COLUMN).setCellEditor(new TagNameCellEditor());

        tagsTable.getColumnExt(TAGS_BACKGROUND_COLUMN).setWidth(90);
        tagsTable.getColumnExt(TAGS_BACKGROUND_COLUMN).setMinWidth(90);
        tagsTable.getColumnExt(TAGS_BACKGROUND_COLUMN).setMaxWidth(90);
        tagsTable.getColumnExt(TAGS_BACKGROUND_COLUMN).setCellEditor(new ColorChooserCellEditor());
        tagsTable.getColumnExt(TAGS_BACKGROUND_COLUMN).setCellRenderer(new CustomColorCellRenderer());

        tagsTable.getColumnExt(TAGS_CHANNEL_COUNT_COLUMN).setWidth(90);
        tagsTable.getColumnExt(TAGS_CHANNEL_COUNT_COLUMN).setMinWidth(90);
        tagsTable.getColumnExt(TAGS_CHANNEL_COUNT_COLUMN).setMaxWidth(90);
        tagsTable.getColumnExt(TAGS_CHANNEL_COUNT_COLUMN).setEditable(false);
        tagsTable.getColumnExt(TAGS_CHANNEL_COUNT_COLUMN).setComparator(ComparatorUtils.NATURAL_COMPARATOR);

        DefaultTableCellRenderer centerAlignedRenderer = new DefaultTableCellRenderer();
        centerAlignedRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tagsTable.getColumnExt(TAGS_CHANNEL_COUNT_COLUMN).setCellRenderer(centerAlignedRenderer);

        tagsTable.getColumnExt(TAGS_TAG_COLUMN).setVisible(false);

        tagsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                checkSelection(evt);
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                checkSelection(evt);
            }

            private void checkSelection(MouseEvent evt) {
                if (tagsTable.rowAtPoint(new Point(evt.getX(), evt.getY())) < 0) {
                    tagsTable.clearSelection();
                }
            }
        });

        tagsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                tagSelectionChanged();
            }
        });

        tagsScrollPane = new JScrollPane(tagsTable);

        tagsAddButton = new MirthButton("Add");
        tagsAddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                addTag();
            }
        });

        tagsRemoveButton = new MirthButton("Remove");
        tagsRemoveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                removeTag();
            }
        });

        channelsSeparator = new JPanel();
        channelsSeparator.setBackground(getBackground());
        channelsSeparator.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Channels", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        channelsLabel = new JLabel("Channel selections will be applied to the currently selected tags.");

        channelsFilterLabel = new JLabel("Filter:");

        channelFilterField = new JTextField();
        channelFilterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent evt) {
                filterChanged();
            }

            @Override
            public void insertUpdate(DocumentEvent evt) {
                filterChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                filterChanged();
            }

            private void filterChanged() {
                channelsTable.getRowSorter().allRowsChanged();
            }
        });

        channelsSelectAllLabel = new JLabel("<html><u>Select All</u></html>");
        channelsSelectAllLabel.setForeground(Color.BLUE);
        channelsSelectAllLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        channelsSelectAllLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                if (evt.getComponent().isEnabled()) {
                    for (int row = 0; row < channelsTable.getRowCount(); row++) {
                        channelsTable.setValueAt(MirthTriStateCheckBox.CHECKED, row, CHANNELS_SELECTED_COLUMN);
                    }
                    setSaveEnabled(true);
                }
            }
        });

        channelsSelectSeparator = new JLabel("|");

        channelsDeselectAllLabel = new JLabel("<html><u>Deselect All</u></html>");
        channelsDeselectAllLabel.setForeground(Color.BLUE);
        channelsDeselectAllLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        channelsDeselectAllLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                if (evt.getComponent().isEnabled()) {
                    for (int row = 0; row < channelsTable.getRowCount(); row++) {
                        channelsTable.setValueAt(MirthTriStateCheckBox.UNCHECKED, row, CHANNELS_SELECTED_COLUMN);
                    }
                    setSaveEnabled(true);
                }
            }
        });

        channelsTable = new MirthTable();
        channelsTable.setModel(new RefreshTableModel(new String[] { "", "Name", "Id" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == CHANNELS_SELECTED_COLUMN;
            }
        });
        channelsTable.setDragEnabled(false);
        channelsTable.setRowSelectionAllowed(false);
        channelsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        channelsTable.setFocusable(false);
        channelsTable.setOpaque(true);
        channelsTable.getTableHeader().setReorderingAllowed(false);
        channelsTable.setEditable(true);
        channelsTable.setSortable(true);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            channelsTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(channelsTable.getModel());
        rowSorter.setComparator(CHANNELS_SELECTED_COLUMN, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                // 0, 2, 1
                if (Objects.equals(o1, o2)) {
                    return 0;
                } else if (o1 == 0 || (o1 == 2 && o2 == 1)) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        channelsTable.setRowSorter(rowSorter);

        RowFilter<TableModel, Integer> rowFilter = new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
                String name = entry.getStringValue(CHANNELS_NAME_COLUMN);
                return StringUtils.containsIgnoreCase(name, channelFilterField.getText());
            }
        };
        rowSorter.setRowFilter(rowFilter);
        channelsTable.setRowFilter(rowFilter);

        channelsTable.getColumnExt(CHANNELS_SELECTED_COLUMN).setMinWidth(20);
        channelsTable.getColumnExt(CHANNELS_SELECTED_COLUMN).setMaxWidth(20);
        channelsTable.getColumn(CHANNELS_SELECTED_COLUMN).setCellEditor(new TagSelectionCellEditor());
        channelsTable.getColumn(CHANNELS_SELECTED_COLUMN).setCellRenderer(new TagSelectionCellRenderer());

        channelsTable.getColumnExt(CHANNELS_ID_COLUMN).setVisible(false);

        channelsTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent evt) {
                if (!channelsTableAdjusting.get()) {
                    for (int row = 0; row < channelsTable.getModel().getRowCount(); row++) {
                        int state = (int) channelsTable.getModel().getValueAt(row, CHANNELS_SELECTED_COLUMN);
                        String channelId = (String) channelsTable.getModel().getValueAt(row, CHANNELS_ID_COLUMN);

                        if (state == MirthTriStateCheckBox.CHECKED || state == MirthTriStateCheckBox.UNCHECKED) {
                            for (int tagRow : tagsTable.getSelectedModelRows()) {
                                ChannelTag tag = (ChannelTag) tagsTable.getModel().getValueAt(tagRow, TAGS_TAG_COLUMN);

                                if (state == MirthTriStateCheckBox.CHECKED) {
                                    tag.getChannelIds().add(channelId);
                                } else {
                                    tag.getChannelIds().remove(channelId);
                                }

                                tagsTable.getModel().setValueAt(tag.getChannelIds().size(), tagRow, TAGS_CHANNEL_COUNT_COLUMN);
                            }
                        }
                    }
                    setSaveEnabled(true);
                }
            }
        });

        channelsScrollPane = new JScrollPane(channelsTable);
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill"));

        container.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));

        leftPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill", "", "[grow 100][]0[][grow 25]"));
        leftPanel.add(tagsScrollPane, "grow 100, sx");
        leftPanel.add(channelsSeparator, "newline, growx, sx, h 14!");
        leftPanel.add(channelsLabel, "newline");
        leftPanel.add(channelsFilterLabel, "right, split 5");
        leftPanel.add(channelFilterField, "w 100:350");
        leftPanel.add(channelsSelectAllLabel, "gapbefore 12");
        leftPanel.add(channelsSelectSeparator);
        leftPanel.add(channelsDeselectAllLabel);
        leftPanel.add(channelsScrollPane, "newline, grow 25, sx");
        container.add(leftPanel, "grow, push");

        container.add(tagsAddButton, "top, flowy, split 2, w 60!");
        container.add(tagsRemoveButton, "w 60!");

        add(container, "grow");
    }

    private void tagSelectionChanged() {
        Set<String> partialChannelIds = new HashSet<String>();
        Set<String> selectedChannelIds = null;

        int[] selectedModelRows = tagsTable.getSelectedModelRows();

        if (selectedModelRows.length > 0) {
            channelsTable.setEnabled(true);
            tagsRemoveButton.setEnabled(true);
            channelsSelectAllLabel.setEnabled(true);
            channelsDeselectAllLabel.setEnabled(true);

            for (int selectedRow : selectedModelRows) {
                ChannelTag tag = (ChannelTag) tagsTable.getModel().getValueAt(selectedRow, TAGS_TAG_COLUMN);
                partialChannelIds.addAll(tag.getChannelIds());
                if (selectedChannelIds == null) {
                    selectedChannelIds = new HashSet<String>(tag.getChannelIds());
                } else {
                    selectedChannelIds = new HashSet<String>(CollectionUtils.intersection(selectedChannelIds, tag.getChannelIds()));
                }
            }
        } else {
            channelsTable.setEnabled(false);
            tagsRemoveButton.setEnabled(false);
            channelsSelectAllLabel.setEnabled(false);
            channelsDeselectAllLabel.setEnabled(false);
            selectedChannelIds = new HashSet<String>();
        }

        channelsTableAdjusting.set(true);
        try {
            for (int row = 0; row < channelsTable.getModel().getRowCount(); row++) {
                String channelId = (String) channelsTable.getModel().getValueAt(row, CHANNELS_ID_COLUMN);
                if (selectedChannelIds.contains(channelId)) {
                    channelsTable.getModel().setValueAt(MirthTriStateCheckBox.CHECKED, row, CHANNELS_SELECTED_COLUMN);
                } else if (partialChannelIds.contains(channelId)) {
                    channelsTable.getModel().setValueAt(MirthTriStateCheckBox.PARTIAL, row, CHANNELS_SELECTED_COLUMN);
                } else {
                    channelsTable.getModel().setValueAt(MirthTriStateCheckBox.UNCHECKED, row, CHANNELS_SELECTED_COLUMN);
                }
            }
            try {
                channelsTable.updateUI();
            } catch (Exception e) {
                // The tags panel gets refreshed even when not currently shown, so this is okay
            }
        } finally {
            channelsTableAdjusting.set(false);
        }
    }

    private void addTag() {
        String name = getNewTagName();
        ((RefreshTableModel) tagsTable.getModel()).addRow(new Object[] { name,
                ColorUtil.getNewColor(), 0, new ChannelTag(name) });
        tagsTable.setRowSelectionInterval(tagsTable.getRowCount() - 1, tagsTable.getRowCount() - 1);
    }

    private String getNewTagName() {
        String name;
        int num = 1;
        do {
            name = "Tag " + num++;
        } while (tagNameExists(name));

        return name;
    }

    private boolean tagNameExists(String name) {
        for (int row = 0; row < tagsTable.getModel().getRowCount(); row++) {
            if (StringUtils.equalsIgnoreCase((String) tagsTable.getModel().getValueAt(row, TAGS_NAME_COLUMN), name)) {
                return true;
            }
        }
        return false;
    }

    private void removeTag() {
        int[] selectedRows = tagsTable.getSelectedRows();
        int[] selectedModelRows = tagsTable.getSelectedModelRows();
        if (selectedModelRows.length > 0) {
            Arrays.sort(selectedRows);
            Arrays.sort(selectedModelRows);

            for (int i = selectedModelRows.length - 1; i >= 0; i--) {
                ((RefreshTableModel) tagsTable.getModel()).removeRow(selectedModelRows[i]);
            }

            if (selectedRows[0] < tagsTable.getRowCount()) {
                tagsTable.setRowSelectionInterval(selectedRows[0], selectedRows[0]);
            } else if (tagsTable.getRowCount() > 0) {
                tagsTable.setRowSelectionInterval(tagsTable.getRowCount() - 1, tagsTable.getRowCount() - 1);
            }
        }
    }

    private class TagNameCellEditor extends TextFieldCellEditor {

        public TagNameCellEditor() {
            getTextField().setDocument(new MirthFieldConstraints(ChannelTag.MAX_NAME_LENGTH, false, true, true));
        }

        @Override
        public boolean isCellEditable(EventObject evt) {
            return evt != null && evt instanceof MouseEvent && ((MouseEvent) evt).getClickCount() >= 2;
        }

        @Override
        protected boolean valueChanged(String value) {
            if (StringUtils.isBlank(value)) {
                return false;
            }

            for (int row = 0; row < tagsTable.getRowCount(); row++) {
                if (StringUtils.equalsIgnoreCase(value, (String) tagsTable.getValueAt(row, TAGS_NAME_COLUMN))) {
                    return false;
                }
            }

            setSaveEnabled(true);
            return true;
        }
    };

    private class ColorChooserCellEditor extends AbstractCellEditor implements TableCellEditor {
        private Color originalColor;
        private ColorPanel colorPanel;

        public ColorChooserCellEditor() {
            colorPanel = new ColorPanel();
            colorPanel.addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent evt) {
                    Color color = JColorChooser.showDialog(PlatformUI.MIRTH_FRAME, "Edit Background Color", originalColor);
                    changeColor(color);
                    stopCellEditing();
                }
            });
        }

        @Override
        public boolean isCellEditable(EventObject evt) {
            return true;
        }

        @Override
        public Object getCellEditorValue() {
            return originalColor;
        }

        private void changeColor(Color color) {
            if (originalColor == null || (color != null && originalColor != color)) {
                setSaveEnabled(originalColor != null);
                colorPanel.setColor(color);
                originalColor = color;
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            colorPanel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

            if (value instanceof Color) {
                changeColor((Color) value);
            }

            return colorPanel;
        }
    }

    private class ColorPanel extends JPanel {
        private static final int SIZE = 18;
        private Color color;

        public ColorPanel() {}

        public ColorPanel(Color color) {
            this.color = color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (color != null) {
                g.setColor(color);
                g.fillRect((getWidth() / 2) - (SIZE / 2), 1, SIZE, SIZE);
            }
        }
    }

    private class CustomColorCellRenderer extends JPanel implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            ColorPanel colorPanel = new ColorPanel((Color) value);
            colorPanel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return colorPanel;
        }
    }

    private class TagSelectionCellEditor extends DefaultCellEditor {

        private MirthTriStateCheckBox checkBox;
        private JPanel panel;

        public TagSelectionCellEditor() {
            super(new MirthTriStateCheckBox());
            checkBox = (MirthTriStateCheckBox) editorComponent;
            panel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
            panel.add(checkBox, "center");
        }

        @Override
        public Object getCellEditorValue() {
            return checkBox.getState();
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return true;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            super.getTableCellEditorComponent(table, value, isSelected, row, column);
            if (value != null) {
                checkBox.setState((int) value);
            }
            panel.setBackground(row % 2 == 0 ? UIConstants.HIGHLIGHTER_COLOR : UIConstants.BACKGROUND_COLOR);
            checkBox.setBackground(panel.getBackground());
            return panel;
        }
    }

    private class TagSelectionCellRenderer implements TableCellRenderer {

        private MirthTriStateCheckBox checkBox;
        private JPanel panel;

        public TagSelectionCellRenderer() {
            panel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
            checkBox = new MirthTriStateCheckBox();
            panel.add(checkBox, "center");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value != null) {
                checkBox.setState((int) value);
            }
            panel.setBackground(row % 2 == 0 ? UIConstants.HIGHLIGHTER_COLOR : UIConstants.BACKGROUND_COLOR);
            checkBox.setBackground(panel.getBackground());
            return panel;
        }
    }

    private JPanel container;

    private JPanel leftPanel;

    private MirthTable tagsTable;
    private JScrollPane tagsScrollPane;
    private JButton tagsAddButton;
    private JButton tagsRemoveButton;

    private JPanel channelsSeparator;
    private JLabel channelsLabel;
    private JLabel channelsFilterLabel;
    private JTextField channelFilterField;
    private JLabel channelsSelectAllLabel;
    private JLabel channelsSelectSeparator;
    private JLabel channelsDeselectAllLabel;
    private MirthTable channelsTable;
    private JScrollPane channelsScrollPane;
}