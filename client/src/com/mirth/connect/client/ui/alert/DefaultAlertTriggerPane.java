package com.mirth.connect.client.ui.alert;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTextArea;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.model.alert.AlertTrigger;
import com.mirth.connect.model.alert.DefaultTrigger;

public class DefaultAlertTriggerPane extends AlertTriggerPane {

    private static int SELECTION_COLUMN_NUMBER = 0;
    private static int ERROR_COLUMN_NUMBER = 1;
    private static int SELECTION_COLUMN_WIDTH = 20;

    public DefaultAlertTriggerPane() {
        initComponents();

        makeErrorTable();
    }

    private void makeErrorTable() {
        Object[][] tableData = new Object[ErrorEventType.values().length][2];

        for (int i = 0; i < ErrorEventType.values().length; i++) {
            ErrorEventType type = ErrorEventType.values()[i];

            tableData[i][0] = false;
            tableData[i][1] = type;
        }

        errorTable.setModel(new RefreshTableModel(tableData, new String[] { "", "Error" }) {

            boolean[] canEdit = { true, false };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        JCheckBox checkBox = new JCheckBox();
        checkBox.setVerticalAlignment(SwingConstants.CENTER);
        checkBox.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultCellEditor editor = new DefaultCellEditor(checkBox);

        editor.addCellEditorListener(new CellEditorListener() {

            @Override
            public void editingStopped(ChangeEvent e) {
                PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
            }

            @Override
            public void editingCanceled(ChangeEvent e) {

            }

        });

        errorTable.getColumnExt(SELECTION_COLUMN_NUMBER).setCellEditor(editor);

        errorTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        errorTable.setRowSelectionAllowed(false);

        errorTable.setRowHeight(UIConstants.ROW_HEIGHT);
        errorTable.setSortable(false);
        errorTable.setOpaque(true);
        errorTable.setDragEnabled(false);
        errorTable.getTableHeader().setReorderingAllowed(false);
        errorTable.setShowGrid(true, true);
        errorTable.setAutoCreateColumnsFromModel(false);

        errorTable.getColumnExt(SELECTION_COLUMN_NUMBER).setMaxWidth(SELECTION_COLUMN_WIDTH);
        errorTable.getColumnExt(SELECTION_COLUMN_NUMBER).setMinWidth(SELECTION_COLUMN_WIDTH);
        errorTable.getColumnExt(SELECTION_COLUMN_NUMBER).setResizable(false);

        errorTable.getColumnExt(ERROR_COLUMN_NUMBER).setMinWidth(UIConstants.MIN_WIDTH);
        errorTable.getColumnExt(ERROR_COLUMN_NUMBER).setResizable(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            errorTable.setHighlighters(highlighter);
        }
    }
    
    @Override
    public List<String> getVariables() {
        List<String> variables = new ArrayList<String>();
        
        variables.add("error");
        variables.add("errorMessage");
        variables.add("channelId");
        variables.add("channelName");
        
        return variables;
    }

    public void updateErrorTable(Set<ErrorEventType> errorAlertTypes) {
        DefaultTableModel tableModel = ((DefaultTableModel) errorTable.getModel());

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            boolean enabled = (Boolean) tableModel.getValueAt(i, 0);
            ErrorEventType type = (ErrorEventType) tableModel.getValueAt(i, 1);

            tableModel.setValueAt(errorAlertTypes.contains(type), i, 0);
        }
    }

    @Override
    public AlertTrigger getTrigger() {
        Set<ErrorEventType> errorAlertTypes = new HashSet<ErrorEventType>();

        DefaultTableModel tableModel = ((DefaultTableModel) errorTable.getModel());

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            boolean enabled = (Boolean) tableModel.getValueAt(i, 0);

            if (enabled) {
                errorAlertTypes.add((ErrorEventType) tableModel.getValueAt(i, 1));
            }
        }

        return new DefaultTrigger(errorAlertTypes, regexTextArea.getText());
    }

    @Override
    public void setTrigger(AlertTrigger trigger) {
        if (trigger instanceof DefaultTrigger) {
            DefaultTrigger defaultTrigger= (DefaultTrigger) trigger;

            updateErrorTable(defaultTrigger.getErrorAlertTypes());

            regexTextArea.setText(defaultTrigger.getRegex());
            
            channelPane.updateChannelTree(defaultTrigger.getChannels());
        }
    }
    
    protected MirthTextArea getRegexTextArea() {
        return regexTextArea;
    }
    
    protected MirthTable getErrorTable() {
        return errorTable;
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);
        setLayout(new MigLayout("insets 0, flowy", "[][grow][]", "[grow]"));

        errorTable = new MirthTable();
        errorScrollPane = new JScrollPane(errorTable);

        errorPane = new JPanel();
        errorPane.setBackground(UIConstants.BACKGROUND_COLOR);
        errorPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Errors (select all that apply)"));
        errorPane.setLayout(new BorderLayout());
        errorPane.add(errorScrollPane);

        regexTextArea = new MirthTextArea();
        regexScrollPane = new JScrollPane(regexTextArea);
        regexPane = new JPanel();
        regexPane.setBackground(UIConstants.BACKGROUND_COLOR);
        regexPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Regex (optional)"));
        regexPane.setLayout(new BorderLayout());
        regexPane.add(regexScrollPane);

        channelPane = new AlertChannelPane();

        add(errorPane, "width 220, growy, wrap");

        add(regexPane, "grow, wrap");

        add(channelPane, "width 300, growy");
    }

    private JPanel errorPane;
    private JScrollPane errorScrollPane;
    private MirthTable errorTable;
    private JPanel regexPane;
    private JScrollPane regexScrollPane;
    private MirthTextArea regexTextArea;
    private AlertChannelPane channelPane;

}
