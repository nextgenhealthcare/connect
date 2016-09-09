/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.codetemplate;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.TaskConstants;
import com.mirth.connect.client.ui.AbstractFramePanel;
import com.mirth.connect.client.ui.AbstractSortableTreeTableNode;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.OffsetRowSorter;
import com.mirth.connect.client.ui.QueuingSwingWorker;
import com.mirth.connect.client.ui.QueuingSwingWorkerTask;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.SortableTreeTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.ChannelInfo;
import com.mirth.connect.client.ui.components.ChannelsTableCellEditor;
import com.mirth.connect.client.ui.components.ChannelsTableCellRenderer;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTreeTable;
import com.mirth.connect.client.ui.components.MirthTriStateCheckBox;
import com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane;
import com.mirth.connect.client.ui.reference.ReferenceListFactory;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplate.CodeTemplateType;
import com.mirth.connect.model.CodeTemplateContextSet;
import com.mirth.connect.model.CodeTemplateLibrary;
import com.mirth.connect.model.CodeTemplateLibrarySaveResult;
import com.mirth.connect.model.CodeTemplateLibrarySaveResult.CodeTemplateUpdateResult;
import com.mirth.connect.model.CodeTemplateLibrarySaveResult.LibraryUpdateResult;
import com.mirth.connect.model.CodeTemplateSummary;
import com.mirth.connect.model.ContextType;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.util.CodeTemplateUtil;
import com.mirth.connect.util.JavaScriptContextUtil;

public class CodeTemplatePanel extends AbstractFramePanel {

    public static final String OPTION_ONLY_SINGLE_CODE_TEMPLATES = "onlySingleCodeTemplates";
    public static final String OPTION_ONLY_SINGLE_LIBRARIES = "onlySingleLibraries";

    public static final int TEMPLATE_NAME_COLUMN = 0;
    public static final int TEMPLATE_ID_COLUMN = 1;
    public static final int TEMPLATE_TYPE_COLUMN = 2;
    public static final int TEMPLATE_DESCRIPTION_COLUMN = 3;
    public static final int TEMPLATE_REVISION_COLUMN = 4;
    public static final int TEMPLATE_LAST_MODIFIED_COLUMN = 5;

    public static final String NEW_CHANNELS = "[New Channels]";

    static final int TEMPLATE_NUM_COLUMNS = 6;

    private static final int LIBRARY_CHANNELS_NAME_COLUMN = 0;
    private static final int LIBRARY_CHANNELS_ID_COLUMN = 1;

    private static final int TASK_CODE_TEMPLATE_REFRESH = 0;
    private static final int TASK_CODE_TEMPLATE_SAVE = 1;
    private static final int TASK_CODE_TEMPLATE_NEW = 2;
    private static final int TASK_CODE_TEMPLATE_LIBRARY_NEW = 3;
    private static final int TASK_CODE_TEMPLATE_IMPORT = 4;
    private static final int TASK_CODE_TEMPLATE_LIBRARY_IMPORT = 5;
    private static final int TASK_CODE_TEMPLATE_EXPORT = 6;
    private static final int TASK_CODE_TEMPLATE_LIBRARY_EXPORT = 7;
    private static final int TASK_CODE_TEMPLATE_LIBRARY_EXPORT_ALL = 8;
    private static final int TASK_CODE_TEMPLATE_DELETE = 9;
    private static final int TASK_CODE_TEMPLATE_LIBRARY_DELETE = 10;
    private static final int TASK_CODE_TEMPLATE_VALIDATE = 11;

    private Frame parent;
    private Logger logger = Logger.getLogger(getClass());
    private boolean firstLoad = true;
    private Map<String, CodeTemplateLibrary> codeTemplateLibraries = new LinkedHashMap<String, CodeTemplateLibrary>();
    private Map<String, CodeTemplate> codeTemplates = new LinkedHashMap<String, CodeTemplate>();
    private AtomicBoolean libraryComboBoxAdjusting = new AtomicBoolean(false);
    private AtomicBoolean saveAdjusting = new AtomicBoolean(false);
    private AtomicBoolean updateCurrentNode = new AtomicBoolean(true);
    private CodeTemplateTreeTableModel fullModel;
    private int currentSelectedRow = -1;
    private CodeChangeWorker codeChangeWorker;

    private List<Pair<Component, Component>> singleLibraryTaskComponents = new ArrayList<Pair<Component, Component>>();
    private List<Pair<Component, Component>> singleCodeTemplateTaskComponents = new ArrayList<Pair<Component, Component>>();

    public CodeTemplatePanel(Frame parent) {
        this.parent = parent;
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        setBackground(UIConstants.BACKGROUND_COLOR);
        initComponents();
        initToolTips();
        initLayout();

        codeTemplateTasks = new JXTaskPane();
        codeTemplateTasks.setTitle("Code Template Tasks");
        codeTemplateTasks.setName(TaskConstants.CODE_TEMPLATE_KEY);
        codeTemplateTasks.setFocusable(false);

        codeTemplatePopupMenu = new JPopupMenu();
        templateTreeTableScrollPane.setComponentPopupMenu(codeTemplatePopupMenu);

        parent.addTask(TaskConstants.CODE_TEMPLATE_REFRESH, "Refresh", "Refresh the list of code templates.", "", new ImageIcon(Frame.class.getResource("images/arrow_refresh.png")), codeTemplateTasks, codeTemplatePopupMenu, this);
        parent.addTask(TaskConstants.CODE_TEMPLATE_SAVE, "Save Changes", "Save all changes made to all libraries and code templates.", "", new ImageIcon(Frame.class.getResource("images/disk.png")), codeTemplateTasks, codeTemplatePopupMenu, this);
        parent.addTask(TaskConstants.CODE_TEMPLATE_NEW, "New Code Template", "Create a new code template.", "N", new ImageIcon(Frame.class.getResource("images/add.png")), codeTemplateTasks, codeTemplatePopupMenu, this);
        parent.addTask(TaskConstants.CODE_TEMPLATE_LIBRARY_NEW, "New Library", "Create a new code template library.", "", new ImageIcon(Frame.class.getResource("images/add.png")), codeTemplateTasks, codeTemplatePopupMenu, this);
        parent.addTask(TaskConstants.CODE_TEMPLATE_IMPORT, "Import Code Templates", "Import list of code templates from an XML file.", "", new ImageIcon(Frame.class.getResource("images/report_go.png")), codeTemplateTasks, codeTemplatePopupMenu, this);
        parent.addTask(TaskConstants.CODE_TEMPLATE_LIBRARY_IMPORT, "Import Libraries", "Import list of code template libraries from an XML file.", "", new ImageIcon(Frame.class.getResource("images/report_go.png")), codeTemplateTasks, codeTemplatePopupMenu, this);
        parent.addTask(TaskConstants.CODE_TEMPLATE_EXPORT, "Export Code Template", "Export the selected code template to an XML file.", "", new ImageIcon(Frame.class.getResource("images/report_disk.png")), codeTemplateTasks, codeTemplatePopupMenu, this);
        parent.addTask(TaskConstants.CODE_TEMPLATE_LIBRARY_EXPORT, "Export Library", "Export the selected code template library to an XML file.", "", new ImageIcon(Frame.class.getResource("images/report_disk.png")), codeTemplateTasks, codeTemplatePopupMenu, this);
        parent.addTask(TaskConstants.CODE_TEMPLATE_LIBRARY_EXPORT_ALL, "Export All Libraries", "Export all libraries to XML files.", "", new ImageIcon(Frame.class.getResource("images/report_disk.png")), codeTemplateTasks, codeTemplatePopupMenu, this);
        parent.addTask(TaskConstants.CODE_TEMPLATE_DELETE, "Delete Code Template", "Delete the currently selected code template.", "L", new ImageIcon(Frame.class.getResource("images/delete.png")), codeTemplateTasks, codeTemplatePopupMenu, this);
        parent.addTask(TaskConstants.CODE_TEMPLATE_LIBRARY_DELETE, "Delete Library", "Delete the currently selected code template library.", "", new ImageIcon(Frame.class.getResource("images/delete.png")), codeTemplateTasks, codeTemplatePopupMenu, this);
        parent.addTask(TaskConstants.CODE_TEMPLATE_VALIDATE, "Validate Script", "Validate the currently viewed script.", "", new ImageIcon(Frame.class.getResource("images/accept.png")), codeTemplateTasks, codeTemplatePopupMenu, this);

        parent.setNonFocusable(codeTemplateTasks);
        parent.taskPaneContainer.add(codeTemplateTasks, parent.taskPaneContainer.getComponentCount() - 1);

        setTaskVisible(TASK_CODE_TEMPLATE_REFRESH);
        setTaskVisible(TASK_CODE_TEMPLATE_NEW);
        setTaskVisible(TASK_CODE_TEMPLATE_IMPORT);
        setTaskVisible(TASK_CODE_TEMPLATE_LIBRARY_NEW);
        setTaskVisible(TASK_CODE_TEMPLATE_LIBRARY_IMPORT);
        setTaskVisible(TASK_CODE_TEMPLATE_LIBRARY_EXPORT_ALL);

        setTaskInvisible(TASK_CODE_TEMPLATE_SAVE);
        setTaskInvisible(TASK_CODE_TEMPLATE_EXPORT);
        setTaskInvisible(TASK_CODE_TEMPLATE_LIBRARY_EXPORT);
        setTaskInvisible(TASK_CODE_TEMPLATE_DELETE);
        setTaskInvisible(TASK_CODE_TEMPLATE_LIBRARY_DELETE);
        setTaskInvisible(TASK_CODE_TEMPLATE_VALIDATE);
    }

    private void setTaskVisible(int task) {
        setTaskVisibility(task, true);
    }

    private void setTaskInvisible(int task) {
        setTaskVisibility(task, false);
    }

    private void setTaskVisibility(int task, boolean visible) {
        parent.setVisibleTasks(codeTemplateTasks, codeTemplatePopupMenu, task, task, visible);
    }

    MirthTreeTable getTreeTable() {
        return templateTreeTable;
    }

    CodeTemplateTreeTableModel getFullModel() {
        return fullModel;
    }

    @Override
    public void switchPanel() {
        Object[][] data = new Object[parent.channelPanel.getCachedChannelStatuses().size() + 1][2];
        data[0][0] = new ChannelInfo(NEW_CHANNELS, false);
        data[0][1] = NEW_CHANNELS;
        int row = 1;

        for (ChannelStatus channelStatus : parent.channelPanel.getCachedChannelStatuses().values()) {
            data[row][0] = new ChannelInfo(channelStatus.getChannel().getName(), false);
            data[row][1] = channelStatus.getChannel().getId();
            row++;
        }

        ((RefreshTableModel) libraryChannelsTable.getModel()).refreshDataVector(data);

        doRefreshCodeTemplates();
        parent.setBold(parent.viewPane, UIConstants.ERROR_CONSTANT);
        parent.setPanelName("Code Templates");
        parent.setCurrentContentPage(CodeTemplatePanel.this);
        parent.setFocus(codeTemplateTasks);
        setSaveEnabled(false);
    }

    @Override
    public boolean isSaveEnabled() {
        return codeTemplateTasks.getContentPane().getComponent(TASK_CODE_TEMPLATE_SAVE).isVisible();
    }

    @Override
    public void setSaveEnabled(boolean enabled) {
        if (!enabled || !saveAdjusting.get()) {
            setTaskVisibility(TASK_CODE_TEMPLATE_SAVE, enabled);
        }
    }

    @Override
    public boolean changesHaveBeenMade() {
        return isSaveEnabled();
    }

    @Override
    public void doContextSensitiveSave() {
        if (isSaveEnabled()) {
            doSaveCodeTemplates();
        }
    }

    @Override
    public boolean confirmLeave() {
        return promptSave(false);
    }

    public boolean promptSave(boolean force) {
        stopTableEditing();
        int option;

        if (force) {
            option = JOptionPane.showConfirmDialog(parent, "You must save the code template changes before continuing. Would you like to save now?");
        } else {
            option = JOptionPane.showConfirmDialog(parent, "Would you like to save the code templates?");
        }

        if (option == JOptionPane.YES_OPTION) {
            CodeTemplateLibrarySaveResult updateSummary = doSaveCodeTemplates(false);

            if (updateSummary == null || updateSummary.isOverrideNeeded() || !updateSummary.isLibrariesSuccess()) {
                return false;
            } else {
                for (CodeTemplateUpdateResult result : updateSummary.getCodeTemplateResults().values()) {
                    if (!result.isSuccess()) {
                        return false;
                    }
                }
            }
        } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION || (option == JOptionPane.NO_OPTION && force)) {
            return false;
        }

        return true;
    }

    @Override
    protected Component addAction(Action action, Set<String> options) {
        Component taskComponent = codeTemplateTasks.add(action);
        Component popupComponent = codeTemplatePopupMenu.add(action);

        if (options.contains(OPTION_ONLY_SINGLE_LIBRARIES)) {
            singleLibraryTaskComponents.add(new ImmutablePair<Component, Component>(taskComponent, popupComponent));
        } else if (options.contains(OPTION_ONLY_SINGLE_CODE_TEMPLATES)) {
            singleCodeTemplateTaskComponents.add(new ImmutablePair<Component, Component>(taskComponent, popupComponent));
        }

        return taskComponent;
    }

    public Map<String, CodeTemplateLibrary> getCachedCodeTemplateLibraries() {
        return codeTemplateLibraries;
    }

    public Map<String, CodeTemplate> getCachedCodeTemplates() {
        return codeTemplates;
    }

    public String getCurrentSelectedId() {
        int selectedRow = templateTreeTable.getSelectedRow();
        if (selectedRow >= 0) {
            TreePath selectedPath = templateTreeTable.getPathForRow(selectedRow);
            if (selectedPath != null) {
                return (String) ((TreeTableNode) selectedPath.getLastPathComponent()).getValueAt(TEMPLATE_ID_COLUMN);
            }
        }

        return null;
    }

    public void doRefreshCodeTemplates() {
        doRefreshCodeTemplates(true);
    }

    public void doRefreshCodeTemplates(boolean showMessageOnForbidden) {
        doRefreshCodeTemplates(null, showMessageOnForbidden);
    }

    public void doRefreshCodeTemplates(ActionListener actionListener) {
        doRefreshCodeTemplates(actionListener, true);
    }

    public void doRefreshCodeTemplates(final ActionListener actionListener, final boolean showMessageOnForbidden) {
        if (isSaveEnabled() && !confirmLeave()) {
            return;
        }

        final Map<String, Integer> codeTemplateRevisions = getCodeTemplateRevisions();
        int selectedRow = templateTreeTable.getSelectedRow();
        final TreeTableNode selectedNode = selectedRow >= 0 ? (TreeTableNode) templateTreeTable.getPathForRow(selectedRow).getLastPathComponent() : null;
        final Set<String> expandedLibraryIds = getExpandedLibraryIds();

        QueuingSwingWorkerTask<Void, Void> task = new QueuingSwingWorkerTask<Void, Void>("doRefreshCodeTemplates", "Loading code templates...") {
            private List<CodeTemplateSummary> codeTemplateSummaries;
            private List<CodeTemplateLibrary> codeTemplateLibraries;

            @Override
            public Void doInBackground() throws ClientException {
                codeTemplateSummaries = parent.mirthClient.getCodeTemplateSummary(codeTemplateRevisions);
                codeTemplateLibraries = parent.mirthClient.getCodeTemplateLibraries(null, false);
                return null;
            }

            @Override
            public void done() {
                try {
                    get();

                    for (CodeTemplateSummary codeTemplateSummary : codeTemplateSummaries) {
                        String codeTemplateId = codeTemplateSummary.getCodeTemplateId();

                        if (codeTemplateSummary.isDeleted()) {
                            codeTemplates.remove(codeTemplateId);
                        } else {
                            codeTemplates.put(codeTemplateId, codeTemplateSummary.getCodeTemplate());
                        }
                    }

                    Set<String> assignedCodeTemplateIds = new HashSet<String>();
                    List<String> libraryNames = new ArrayList<String>();

                    CodeTemplatePanel.this.codeTemplateLibraries.clear();
                    for (CodeTemplateLibrary library : codeTemplateLibraries) {
                        CodeTemplatePanel.this.codeTemplateLibraries.put(library.getId(), library);

                        for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                            assignedCodeTemplateIds.add(codeTemplate.getId());
                        }

                        libraryNames.add(library.getName());
                    }

                    ReferenceListFactory.getInstance().updateUserCodeTemplates();
                    saveAdjusting.set(true);
                    updateCurrentNode.set(false);
                    updateTasks();
                    final TreePath selectedPath = updateCodeTemplateTable(selectedNode);
                    updateLibrariesComboBox();
                    setSaveEnabled(false);

                    if (selectedNode != null && !codeTemplates.containsKey((String) selectedNode.getValueAt(TEMPLATE_ID_COLUMN))) {
                        switchSplitPaneComponent(blankPanel);
                    }

                    if (firstLoad) {
                        templateTreeTable.expandAll();
                        firstLoad = false;
                    } else {
                        expandLibraryNodes(expandedLibraryIds);
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (selectedPath != null) {
                                selectTemplatePath(selectedPath);
                            }
                            saveAdjusting.set(false);
                            updateCurrentNode.set(true);
                        }
                    });
                } catch (Exception e) {
                    Throwable cause = e;
                    if (cause instanceof ExecutionException) {
                        cause = e.getCause();
                    }
                    parent.alertThrowable(parent, e.getCause(), showMessageOnForbidden);
                }

                if (actionListener != null) {
                    actionListener.actionPerformed(null);
                }
            }
        };

        new QueuingSwingWorker<Void, Void>(task, false).executeDelegate();
    }

    private Set<String> getExpandedLibraryIds() {
        Set<String> expandedLibraryIds = new HashSet<String>();

        for (Enumeration<? extends TreeTableNode> libraryNodes = ((TreeTableNode) templateTreeTable.getTreeTableModel().getRoot()).children(); libraryNodes.hasMoreElements();) {
            TreeTableNode libraryNode = libraryNodes.nextElement();
            if (templateTreeTable.isExpanded(new TreePath(((CodeTemplateTreeTableModel) templateTreeTable.getTreeTableModel()).getPathToRoot(libraryNode)))) {
                expandedLibraryIds.add((String) libraryNode.getValueAt(TEMPLATE_ID_COLUMN));
            }
        }

        return expandedLibraryIds;
    }

    private void expandLibraryNodes(Set<String> expandedLibraryIds) {
        if (expandedLibraryIds != null) {
            for (Enumeration<? extends TreeTableNode> libraryNodes = ((TreeTableNode) templateTreeTable.getTreeTableModel().getRoot()).children(); libraryNodes.hasMoreElements();) {
                TreeTableNode libraryNode = libraryNodes.nextElement();
                if (expandedLibraryIds.contains((String) libraryNode.getValueAt(TEMPLATE_ID_COLUMN))) {
                    templateTreeTable.expandPath(new TreePath(((CodeTemplateTreeTableModel) templateTreeTable.getTreeTableModel()).getPathToRoot(libraryNode)));
                }
            }
        }
    }

    private Map<String, Integer> getCodeTemplateRevisions() {
        Map<String, Integer> codeTemplateRevisions = new HashMap<String, Integer>();
        for (CodeTemplate codeTemplate : codeTemplates.values()) {
            codeTemplateRevisions.put(codeTemplate.getId(), codeTemplate.getRevision());
        }
        return codeTemplateRevisions;
    }

    private TreePath updateCodeTemplateTable(TreeTableNode selectedNode) {
        CodeTemplateTreeTableModel model = (CodeTemplateTreeTableModel) templateTreeTable.getTreeTableModel();
        CodeTemplateRootTreeTableNode root = new CodeTemplateRootTreeTableNode();

        for (CodeTemplateLibrary library : codeTemplateLibraries.values()) {
            addLibraryNode(root, library);
        }

        fullModel.setRoot(root);
        fullModel.sort();
        if (StringUtils.isNotBlank(templateFilterField.getText())) {
            root = getFilteredRootNode(root);
        }
        model.setRoot(root);
        model.sort();
        updateFilterNotification();

        return selectPathFromNodeId(selectedNode, root);
    }

    private TreePath selectPathFromNodeId(TreeTableNode selectedNode, CodeTemplateRootTreeTableNode root) {
        TreePath selectedPath = null;
        if (selectedNode != null) {
            CodeTemplateTreeTableModel model = (CodeTemplateTreeTableModel) templateTreeTable.getTreeTableModel();

            for (Enumeration<? extends MutableTreeTableNode> libraries = root.children(); libraries.hasMoreElements();) {
                CodeTemplateLibraryTreeTableNode libraryNode = (CodeTemplateLibraryTreeTableNode) libraries.nextElement();

                if (selectedNode instanceof CodeTemplateLibraryTreeTableNode && ((CodeTemplateLibraryTreeTableNode) selectedNode).getLibraryId().equals(libraryNode.getLibraryId())) {
                    selectedPath = new TreePath(model.getPathToRoot(libraryNode));
                    break;
                }

                for (Enumeration<? extends MutableTreeTableNode> codeTemplates = libraryNode.children(); codeTemplates.hasMoreElements();) {
                    CodeTemplateTreeTableNode codeTemplateNode = (CodeTemplateTreeTableNode) codeTemplates.nextElement();

                    if (selectedNode instanceof CodeTemplateTreeTableNode && ((CodeTemplateTreeTableNode) selectedNode).getCodeTemplateId().equals(codeTemplateNode.getCodeTemplateId())) {
                        selectedPath = new TreePath(model.getPathToRoot(codeTemplateNode));
                        break;
                    }
                }

                if (selectedPath != null) {
                    break;
                }
            }

            if (selectedPath != null) {
                selectTemplatePath(selectedPath);
            }
        }

        return selectedPath;
    }

    private CodeTemplateLibraryTreeTableNode addLibraryNode(AbstractMutableTreeTableNode root, CodeTemplateLibrary library) {
        CodeTemplateLibraryTreeTableNode libraryNode = new CodeTemplateLibraryTreeTableNode(library);
        for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
            CodeTemplate cachedCodeTemplate = codeTemplates.get(codeTemplate.getId());
            if (cachedCodeTemplate != null) {
                libraryNode.add(new CodeTemplateTreeTableNode(cachedCodeTemplate));
            }
        }
        root.add(libraryNode);
        return libraryNode;
    }

    private CodeTemplateRootTreeTableNode getFilteredRootNode(CodeTemplateRootTreeTableNode root) {
        CodeTemplateRootTreeTableNode newRoot = new CodeTemplateRootTreeTableNode();
        String filter = StringUtils.trim(templateFilterField.getText());

        for (Enumeration<? extends MutableTreeTableNode> libraryNodes = root.children(); libraryNodes.hasMoreElements();) {
            CodeTemplateLibraryTreeTableNode libraryNode = (CodeTemplateLibraryTreeTableNode) libraryNodes.nextElement();

            CodeTemplateLibraryTreeTableNode newLibraryNode = new CodeTemplateLibraryTreeTableNode(libraryNode.getLibrary());

            for (Enumeration<? extends MutableTreeTableNode> codeTemplateNodes = libraryNode.children(); codeTemplateNodes.hasMoreElements();) {
                CodeTemplateTreeTableNode codeTemplateNode = (CodeTemplateTreeTableNode) codeTemplateNodes.nextElement();

                if (StringUtils.containsIgnoreCase((String) codeTemplateNode.getValueAt(TEMPLATE_NAME_COLUMN), filter)) {
                    CodeTemplateTreeTableNode newCodeTemplateNode = new CodeTemplateTreeTableNode(codeTemplateNode.getCodeTemplate());
                    newLibraryNode.add(newCodeTemplateNode);
                }
            }

            if (newLibraryNode.getChildCount() > 0 || StringUtils.containsIgnoreCase((String) libraryNode.getValueAt(TEMPLATE_NAME_COLUMN), filter)) {
                newRoot.add(newLibraryNode);
            }
        }

        return newRoot;
    }

    public void doSaveCodeTemplates() {
        doSaveCodeTemplates(true);
    }

    private CodeTemplateLibrarySaveResult doSaveCodeTemplates(boolean asynchronous) {
        stopTableEditing();
        updateCurrentNode();

        if (!doValidateCodeTemplate(false)) {
            return null;
        }

        Map<String, CodeTemplateLibrary> codeTemplateLibraries = new LinkedHashMap<String, CodeTemplateLibrary>();
        Map<String, CodeTemplateLibrary> removedCodeTemplateLibraries = new LinkedHashMap<String, CodeTemplateLibrary>();
        Map<String, CodeTemplate> updatedCodeTemplates = new LinkedHashMap<String, CodeTemplate>();
        // Add all cached code templates to the remove map first
        Map<String, CodeTemplate> removedCodeTemplates = new LinkedHashMap<String, CodeTemplate>(codeTemplates);

        for (Enumeration<? extends MutableTreeTableNode> libraryNodes = ((AbstractSortableTreeTableNode) fullModel.getRoot()).children(); libraryNodes.hasMoreElements();) {
            CodeTemplateLibraryTreeTableNode libraryNode = (CodeTemplateLibraryTreeTableNode) libraryNodes.nextElement();

            CodeTemplateLibrary library = libraryNode.getLibrary();
            List<CodeTemplate> libraryTemplates = new ArrayList<CodeTemplate>();

            for (Enumeration<? extends MutableTreeTableNode> codeTemplateNodes = libraryNode.children(); codeTemplateNodes.hasMoreElements();) {
                CodeTemplateTreeTableNode codeTemplateNode = (CodeTemplateTreeTableNode) codeTemplateNodes.nextElement();
                CodeTemplate codeTemplate = codeTemplateNode.getCodeTemplate();

                // Only update the code template if it changed
                if (!codeTemplates.containsKey(codeTemplate.getId()) || !codeTemplate.equals(codeTemplates.get(codeTemplate.getId()))) {
                    updatedCodeTemplates.put(codeTemplate.getId(), codeTemplate);
                }

                // Always remove the entry from the remove map
                removedCodeTemplates.remove(codeTemplate.getId());

                libraryTemplates.add(new CodeTemplate(codeTemplate.getId()));
            }

            library.setCodeTemplates(libraryTemplates);
            codeTemplateLibraries.put(library.getId(), library);
        }

        for (CodeTemplateLibrary library : this.codeTemplateLibraries.values()) {
            if (!codeTemplateLibraries.containsKey(library.getId())) {
                removedCodeTemplateLibraries.put(library.getId(), library);
            }
        }

        int selectedRow = templateTreeTable.getSelectedRow();
        TreeTableNode selectedNode = selectedRow >= 0 ? (TreeTableNode) templateTreeTable.getPathForRow(selectedRow).getLastPathComponent() : null;
        Set<String> expandedLibraryIds = getExpandedLibraryIds();

        if (asynchronous) {
            new UpdateSwingWorker(codeTemplateLibraries, removedCodeTemplateLibraries, updatedCodeTemplates, removedCodeTemplates, false, selectedNode, expandedLibraryIds).execute();
            return null;
        } else {
            return attemptUpdate(codeTemplateLibraries, removedCodeTemplateLibraries, updatedCodeTemplates, removedCodeTemplates, false, selectedNode, expandedLibraryIds);
        }
    }

    public CodeTemplateLibrarySaveResult attemptUpdate(Map<String, CodeTemplateLibrary> libraries, Map<String, CodeTemplateLibrary> removedLibraries, Map<String, CodeTemplate> updatedCodeTemplates, Map<String, CodeTemplate> removedCodeTemplates, boolean override, TreeTableNode selectedNode, Set<String> expandedLibraryIds) {
        CodeTemplateLibrarySaveResult updateSummary = null;
        boolean tryAgain = false;

        try {
            updateSummary = updateLibrariesAndTemplates(libraries, removedLibraries, updatedCodeTemplates, removedCodeTemplates, override);

            if (updateSummary.isOverrideNeeded()) {
                if (!override) {
                    if (parent.alertOption(parent, "One or more code templates or libraries have been modified since you last refreshed.\nDo you want to overwrite the changes?")) {
                        tryAgain = true;
                    }
                } else {
                    parent.alertError(parent, "Unable to save code templates or libraries.");
                }
            } else {
                handleUpdateSummary(libraries, updatedCodeTemplates, removedCodeTemplates, override, selectedNode, expandedLibraryIds, updateSummary);
            }
        } catch (Exception e) {
            Throwable cause = e;
            if (cause instanceof ExecutionException) {
                cause = e.getCause();
            }
            parent.alertThrowable(parent, cause, "Unable to save code templates or libraries: " + cause.getMessage());
        }

        if (tryAgain && !override) {
            return attemptUpdate(libraries, removedLibraries, updatedCodeTemplates, removedCodeTemplates, true, selectedNode, expandedLibraryIds);
        }

        return updateSummary;
    }

    private CodeTemplateLibrarySaveResult updateLibrariesAndTemplates(Map<String, CodeTemplateLibrary> libraries, Map<String, CodeTemplateLibrary> removedLibraries, Map<String, CodeTemplate> updatedCodeTemplates, Map<String, CodeTemplate> removedCodeTemplates, boolean override) throws Exception {
        return parent.mirthClient.updateLibrariesAndTemplates(new ArrayList<CodeTemplateLibrary>(libraries.values()), new HashSet<String>(removedLibraries.keySet()), new ArrayList<CodeTemplate>(updatedCodeTemplates.values()), new HashSet<String>(removedCodeTemplates.keySet()), override);
    }

    private void handleUpdateSummary(Map<String, CodeTemplateLibrary> libraries, Map<String, CodeTemplate> updatedCodeTemplates, Map<String, CodeTemplate> removedCodeTemplates, boolean override, TreeTableNode selectedNode, Set<String> expandedLibraryIds, CodeTemplateLibrarySaveResult updateSummary) {
        try {
            if (!updateSummary.isOverrideNeeded()) {
                if (updateSummary.isLibrariesSuccess()) {
                    Set<String> assignedCodeTemplateIds = new HashSet<String>();
                    List<String> libraryNames = new ArrayList<String>();

                    // Replace the cached libraries
                    codeTemplateLibraries.clear();
                    for (CodeTemplateLibrary library : libraries.values()) {
                        LibraryUpdateResult result = updateSummary.getLibraryResults().get(library.getId());
                        library.setRevision(result.getNewRevision());
                        library.setLastModified(result.getNewLastModified());
                        codeTemplateLibraries.put(library.getId(), library);

                        for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                            assignedCodeTemplateIds.add(codeTemplate.getId());
                        }

                        libraryNames.add(library.getName());
                    }

                    int numFailed = 0;
                    Throwable firstCause = null;
                    Set<String> successfulIds = new HashSet<String>();

                    // Update any cached code templates that were successfully saved
                    for (Entry<String, CodeTemplateUpdateResult> entry : updateSummary.getCodeTemplateResults().entrySet()) {
                        String codeTemplateId = entry.getKey();
                        CodeTemplateUpdateResult result = entry.getValue();

                        if (result.isSuccess()) {
                            CodeTemplate updatedCodeTemplate = updatedCodeTemplates.get(codeTemplateId);

                            if (updatedCodeTemplate != null) {
                                updatedCodeTemplate.setRevision(result.getNewRevision());
                                updatedCodeTemplate.setLastModified(result.getNewLastModified());
                                codeTemplates.put(codeTemplateId, updatedCodeTemplate);
                            } else {
                                codeTemplates.remove(codeTemplateId);
                            }

                            successfulIds.add(codeTemplateId);
                        } else {
                            numFailed++;
                            if (firstCause == null && result.getCause() != null) {
                                firstCause = result.getCause();
                            }
                        }
                    }

                    // Create a new table model
                    CodeTemplateRootTreeTableNode root = new CodeTemplateRootTreeTableNode();

                    // Create each library node
                    for (CodeTemplateLibrary library : codeTemplateLibraries.values()) {
                        CodeTemplateLibraryTreeTableNode libraryNode = new CodeTemplateLibraryTreeTableNode(library);

                        for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                            String codeTemplateId = codeTemplate.getId();

                            if (successfulIds.contains(codeTemplateId)) {
                                // If the update was successful, add the new code template. If the remove was successful, don't add anything.
                                if (updatedCodeTemplates.containsKey(codeTemplateId)) {
                                    CodeTemplateTreeTableNode newCodeTemplateNode = new CodeTemplateTreeTableNode(codeTemplates.get(codeTemplate.getId()));
                                    libraryNode.add(newCodeTemplateNode);
                                }
                            } else {
                                // The update or removal wasn't successful, so add the old template
                                CodeTemplateTreeTableNode codeTemplateNode = getCodeTemplateNodeById(codeTemplateId);
                                if (codeTemplateNode != null) {
                                    CodeTemplateTreeTableNode newCodeTemplateNode = new CodeTemplateTreeTableNode(codeTemplateNode.getCodeTemplate());
                                    libraryNode.add(newCodeTemplateNode);
                                }
                            }
                        }

                        root.add(libraryNode);
                    }

                    // Update the actual table model
                    CodeTemplateTreeTableModel model = (CodeTemplateTreeTableModel) templateTreeTable.getTreeTableModel();

                    updateCurrentNode.set(false);
                    fullModel.setRoot(root);
                    fullModel.sort();
                    if (StringUtils.isNotBlank(templateFilterField.getText())) {
                        root = getFilteredRootNode(root);
                    }
                    model.setRoot(root);
                    model.sort();
                    updateFilterNotification();

                    saveAdjusting.set(true);
                    ReferenceListFactory.getInstance().updateUserCodeTemplates();
                    updateTasks();
                    updateLibrariesComboBox();
                    expandLibraryNodes(expandedLibraryIds);

                    // Re-select the previously selected node if applicable
                    TreePath selectedPath = null;
                    if (selectedNode != null) {

                        for (Enumeration<? extends MutableTreeTableNode> libraryNodes = root.children(); libraryNodes.hasMoreElements();) {
                            CodeTemplateLibraryTreeTableNode libraryNode = (CodeTemplateLibraryTreeTableNode) libraryNodes.nextElement();

                            if (selectedNode instanceof CodeTemplateLibraryTreeTableNode && ((CodeTemplateLibraryTreeTableNode) selectedNode).getLibraryId().equals(libraryNode.getLibraryId())) {
                                selectedPath = new TreePath(model.getPathToRoot(libraryNode));
                                break;
                            }

                            for (Enumeration<? extends MutableTreeTableNode> codeTemplates = libraryNode.children(); codeTemplates.hasMoreElements();) {
                                CodeTemplateTreeTableNode codeTemplateNode = (CodeTemplateTreeTableNode) codeTemplates.nextElement();

                                if (selectedNode instanceof CodeTemplateTreeTableNode && ((CodeTemplateTreeTableNode) selectedNode).getCodeTemplateId().equals(codeTemplateNode.getCodeTemplateId())) {
                                    selectedPath = new TreePath(model.getPathToRoot(codeTemplateNode));
                                    break;
                                }
                            }

                            if (selectedPath != null) {
                                break;
                            }
                        }

                        if (selectedPath != null) {
                            selectTemplatePath(selectedPath);
                        }
                    }

                    if (numFailed > 0) {
                        if (firstCause != null) {
                            parent.alertThrowable(parent, firstCause, numFailed + " code templates failed to be updated or removed. First cause: " + firstCause.getMessage());
                        } else {
                            parent.alertError(parent, numFailed + " code templates failed to be updated or removed.");
                        }
                    } else {
                        setSaveEnabled(false);
                    }

                    final TreePath finalPath = selectedPath;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (finalPath != null) {
                                selectTemplatePath(finalPath);
                            }
                            saveAdjusting.set(false);
                            updateCurrentNode.set(true);
                        }
                    });
                } else {
                    if (updateSummary.getLibrariesCause() != null) {
                        parent.alertThrowable(parent, updateSummary.getLibrariesCause(), "Unable to save code template libraries: " + updateSummary.getLibrariesCause().getMessage());
                    } else {
                        parent.alertError(parent, "Unable to save code template libraries.");
                    }
                }
            }
        } catch (Exception e) {
            Throwable cause = e;
            if (cause instanceof ExecutionException) {
                cause = e.getCause();
            }
            parent.alertThrowable(parent, cause, "Unable to save code templates or libraries: " + cause.getMessage());
        }
    }

    public UpdateSwingWorker getSwingWorker(Map<String, CodeTemplateLibrary> libraries, Map<String, CodeTemplateLibrary> removedLibraries, Map<String, CodeTemplate> updatedCodeTemplates, Map<String, CodeTemplate> removedCodeTemplates, boolean override) {
        return new UpdateSwingWorker(libraries, removedLibraries, updatedCodeTemplates, removedCodeTemplates, override, null, null);
    }

    public class UpdateSwingWorker extends SwingWorker<CodeTemplateLibrarySaveResult, Void> {

        private Map<String, CodeTemplateLibrary> libraries;
        private Map<String, CodeTemplateLibrary> removedLibraries;
        private Map<String, CodeTemplate> updatedCodeTemplates;
        private Map<String, CodeTemplate> removedCodeTemplates;
        private boolean override;
        private TreeTableNode selectedNode;
        private Set<String> expandedLibraryIds;
        private String workingId;
        private ActionListener actionListener;

        public UpdateSwingWorker(Map<String, CodeTemplateLibrary> libraries, Map<String, CodeTemplateLibrary> removedLibraries, Map<String, CodeTemplate> updatedCodeTemplates, Map<String, CodeTemplate> removedCodeTemplates, boolean override, TreeTableNode selectedNode, Set<String> expandedLibraryIds) {
            this.libraries = libraries;
            this.removedLibraries = removedLibraries;
            this.updatedCodeTemplates = updatedCodeTemplates;
            this.removedCodeTemplates = removedCodeTemplates;
            this.override = override;
            this.selectedNode = selectedNode;
            this.expandedLibraryIds = expandedLibraryIds;
            workingId = parent.startWorking("Saving code templates and libraries...");
        }

        public void setActionListener(ActionListener actionListener) {
            this.actionListener = actionListener;
        }

        @Override
        protected CodeTemplateLibrarySaveResult doInBackground() throws Exception {
            return updateLibrariesAndTemplates(libraries, removedLibraries, updatedCodeTemplates, removedCodeTemplates, override);
        }

        @Override
        protected void done() {
            boolean tryAgain = false;

            try {
                CodeTemplateLibrarySaveResult updateSummary = get();

                if (updateSummary.isOverrideNeeded()) {
                    if (!override) {
                        if (parent.alertOption(parent, "One or more code templates or libraries have been modified since you last refreshed.\nDo you want to overwrite the changes?")) {
                            tryAgain = true;
                        }
                    } else {
                        parent.alertError(parent, "Unable to save code templates or libraries.");
                    }
                } else {
                    handleUpdateSummary(libraries, updatedCodeTemplates, removedCodeTemplates, override, selectedNode, expandedLibraryIds, updateSummary);

                    if (updateSummary.isLibrariesSuccess() && actionListener != null) {
                        actionListener.actionPerformed(null);
                    }
                }
            } catch (Exception e) {
                Throwable cause = e;
                if (cause instanceof ExecutionException) {
                    cause = e.getCause();
                }
                parent.alertThrowable(parent, cause, "Unable to save code templates or libraries: " + cause.getMessage());
            } finally {
                parent.stopWorking(workingId);

                if (tryAgain && !override) {
                    new UpdateSwingWorker(libraries, removedLibraries, updatedCodeTemplates, removedCodeTemplates, true, selectedNode, expandedLibraryIds).execute();
                }
            }
        }
    }

    private CodeTemplateTreeTableNode getCodeTemplateNodeById(String codeTemplateId) {
        for (Enumeration<? extends MutableTreeTableNode> libraryNodes = ((AbstractSortableTreeTableNode) fullModel.getRoot()).children(); libraryNodes.hasMoreElements();) {
            for (Enumeration<? extends MutableTreeTableNode> codeTemplateNodes = libraryNodes.nextElement().children(); codeTemplateNodes.hasMoreElements();) {
                CodeTemplateTreeTableNode codeTemplateNode = (CodeTemplateTreeTableNode) codeTemplateNodes.nextElement();
                if (codeTemplateNode.getCodeTemplateId().equals(codeTemplateId)) {
                    return codeTemplateNode;
                }
            }
        }
        return null;
    }

    private AbstractSortableTreeTableNode findFullNode(AbstractSortableTreeTableNode node) {
        String id = (String) node.getValueAt(TEMPLATE_ID_COLUMN);

        for (Enumeration<? extends MutableTreeTableNode> libraryNodes = ((AbstractSortableTreeTableNode) fullModel.getRoot()).children(); libraryNodes.hasMoreElements();) {
            CodeTemplateLibraryTreeTableNode libraryNode = (CodeTemplateLibraryTreeTableNode) libraryNodes.nextElement();
            if (id.equals(libraryNode.getValueAt(TEMPLATE_ID_COLUMN))) {
                return libraryNode;
            }

            for (Enumeration<? extends MutableTreeTableNode> codeTemplateNodes = libraryNode.children(); codeTemplateNodes.hasMoreElements();) {
                CodeTemplateTreeTableNode codeTemplateNode = (CodeTemplateTreeTableNode) codeTemplateNodes.nextElement();
                if (id.equals(codeTemplateNode.getValueAt(TEMPLATE_ID_COLUMN))) {
                    return codeTemplateNode;
                }
            }
        }

        return null;
    }

    public void doNewCodeTemplate() {
        stopTableEditing();
        updateCurrentNode();
        updateCurrentNode.set(false);
        setSaveEnabled(true);
        AbstractSortableTreeTableNode parentNode;

        int selectedRow = templateTreeTable.getSelectedRow();
        if (selectedRow >= 0) {
            parentNode = (AbstractSortableTreeTableNode) templateTreeTable.getPathForRow(selectedRow).getLastPathComponent();
            if (parentNode instanceof CodeTemplateTreeTableNode) {
                parentNode = (AbstractSortableTreeTableNode) parentNode.getParent();
            }
        } else {
            return;
        }

        String name;
        int index = 1;
        do {
            name = "Template " + index++;
        } while (!checkCodeTemplateName(name));

        CodeTemplate codeTemplate = CodeTemplate.getDefaultCodeTemplate(name);
        CodeTemplateTreeTableNode codeTemplateNode = new CodeTemplateTreeTableNode(codeTemplate);

        CodeTemplateTreeTableModel model = (CodeTemplateTreeTableModel) templateTreeTable.getTreeTableModel();
        model.insertNodeInto(codeTemplateNode, parentNode);

        if (model.getRoot() != fullModel.getRoot()) {
            fullModel.insertNodeInto(new CodeTemplateTreeTableNode(codeTemplate), findFullNode(parentNode));
        }

        TreePath selectedPath = new TreePath(model.getPathToRoot(codeTemplateNode));
        selectTemplatePath(selectedPath);
        updateFilterNotification();
        updateCurrentNode.set(true);
    }

    public void doNewLibrary() {
        stopTableEditing();
        updateCurrentNode();
        updateCurrentNode.set(false);
        setSaveEnabled(true);

        String name;
        int index = 1;
        do {
            name = "Library " + index++;
        } while (!checkLibraryName(name));

        CodeTemplateLibrary library = new CodeTemplateLibrary();
        library.setName(name);
        CodeTemplateLibraryTreeTableNode libraryNode = new CodeTemplateLibraryTreeTableNode(library);

        CodeTemplateTreeTableModel model = (CodeTemplateTreeTableModel) templateTreeTable.getTreeTableModel();
        model.insertNodeInto(libraryNode, (AbstractSortableTreeTableNode) model.getRoot());

        if (model.getRoot() != fullModel.getRoot()) {
            fullModel.insertNodeInto(new CodeTemplateLibraryTreeTableNode(library), (AbstractSortableTreeTableNode) fullModel.getRoot());
        }

        final TreePath selectedPath = new TreePath(model.getPathToRoot(libraryNode));
        selectTemplatePath(selectedPath);

        updateLibrariesComboBox();
        updateFilterNotification();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                selectTemplatePath(selectedPath);
                updateCurrentNode.set(true);
            }
        });
    }

    private boolean checkLibraryName(String name) {
        for (Enumeration<? extends MutableTreeTableNode> libraries = ((AbstractSortableTreeTableNode) fullModel.getRoot()).children(); libraries.hasMoreElements();) {
            if (((CodeTemplateLibraryTreeTableNode) libraries.nextElement()).getLibrary().getName().equals(name)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkCodeTemplateName(String name) {
        for (Enumeration<? extends MutableTreeTableNode> libraries = ((AbstractSortableTreeTableNode) fullModel.getRoot()).children(); libraries.hasMoreElements();) {
            for (Enumeration<? extends MutableTreeTableNode> codeTemplates = libraries.nextElement().children(); codeTemplates.hasMoreElements();) {
                if (((CodeTemplateTreeTableNode) codeTemplates.nextElement()).getCodeTemplate().getName().equals(name)) {
                    return false;
                }
            }
        }

        return true;
    }

    public void doImportCodeTemplates() {
        stopTableEditing();
        updateCurrentNode();

        if (changesHaveBeenMade() && !promptSave(true)) {
            return;
        }

        if (codeTemplateLibraries.size() == 0) {
            parent.alertError(parent, "Cannot import code templates without an existing library.");
            return;
        }

        String content = parent.browseForFileString("XML");
        if (content == null) {
            return;
        }

        List<CodeTemplate> importCodeTemplates;
        try {
            importCodeTemplates = ObjectXMLSerializer.getInstance().deserializeList(content, CodeTemplate.class);
        } catch (Exception e) {
            parent.alertThrowable(this, e, "Invalid code template file: " + e.getMessage());
            return;
        }

        parent.removeInvalidItems(importCodeTemplates, CodeTemplate.class);
        if (CollectionUtils.isEmpty(importCodeTemplates)) {
            parent.alertError(parent, "No code templates found in the file.");
            return;
        }

        CodeTemplateLibrary unassignedLibrary = new CodeTemplateLibrary();
        unassignedLibrary.setCodeTemplates(importCodeTemplates);
        showImportDialog(Collections.singletonList(unassignedLibrary), true);
    }

    public void doImportLibraries() {
        stopTableEditing();
        updateCurrentNode();

        if (changesHaveBeenMade() && !promptSave(true)) {
            return;
        }

        String content = parent.browseForFileString("XML");
        if (content == null) {
            return;
        }

        List<CodeTemplateLibrary> importLibraries;
        try {
            importLibraries = ObjectXMLSerializer.getInstance().deserializeList(content, CodeTemplateLibrary.class);
        } catch (Exception e) {
            parent.alertThrowable(this, e, "Invalid code template library file: " + e.getMessage());
            return;
        }

        parent.removeInvalidItems(importLibraries, CodeTemplateLibrary.class);
        if (CollectionUtils.isEmpty(importLibraries)) {
            parent.alertError(parent, "No code template libraries found in the file.");
            return;
        }

        showImportDialog(importLibraries, false);
    }

    private void showImportDialog(List<CodeTemplateLibrary> importLibraries, boolean unassignedCodeTemplates) {
        CodeTemplateImportDialog dialog = new CodeTemplateImportDialog(parent, importLibraries, unassignedCodeTemplates);

        if (dialog.wasSaved()) {
            int selectedRow = templateTreeTable.getSelectedRow();
            TreeTableNode selectedNode = selectedRow >= 0 ? (TreeTableNode) templateTreeTable.getPathForRow(selectedRow).getLastPathComponent() : null;
            new UpdateSwingWorker(dialog.getUpdatedLibraries(), new HashMap<String, CodeTemplateLibrary>(), dialog.getUpdatedCodeTemplates(), new HashMap<String, CodeTemplate>(), true, selectedNode, getExpandedLibraryIds()).execute();
            doRefreshCodeTemplates();
        }
    }

    public void doExportCodeTemplate() {
        stopTableEditing();
        updateCurrentNode();

        if (changesHaveBeenMade() && !promptSave(true)) {
            return;
        }

        TreePath selectedPath = templateTreeTable.getTreeSelectionModel().getSelectionPath();
        if (selectedPath != null) {
            CodeTemplate codeTemplate = ((CodeTemplateTreeTableNode) selectedPath.getLastPathComponent()).getCodeTemplate();
            ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
            try {
                String codeTemplateXML = serializer.serialize(codeTemplate);
                parent.exportFile(codeTemplateXML, codeTemplate.getName() + ".xml", "XML", "Code template export");
            } catch (Throwable t) {
                parent.alertThrowable(parent, t, "Unable to export: " + t.getMessage());
            }
        }
    }

    public void doExportLibrary() {
        stopTableEditing();
        updateCurrentNode();

        if (changesHaveBeenMade() && !promptSave(true)) {
            return;
        }

        TreePath selectedPath = templateTreeTable.getTreeSelectionModel().getSelectionPath();
        if (selectedPath != null) {
            CodeTemplateLibrary library = getLibraryWithTemplates((CodeTemplateLibraryTreeTableNode) selectedPath.getLastPathComponent());
            ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
            try {
                String libraryXml = serializer.serialize(library);
                parent.exportFile(libraryXml, library.getName() + ".xml", "XML", "Code template library export");
            } catch (Throwable t) {
                parent.alertThrowable(parent, t, "Unable to export: " + t.getMessage());
            }
        }
    }

    public void doExportAllLibraries() {
        stopTableEditing();
        updateCurrentNode();

        if (changesHaveBeenMade() && !promptSave(true)) {
            return;
        }

        List<CodeTemplateLibrary> libraries = new ArrayList<CodeTemplateLibrary>();
        for (Enumeration<? extends MutableTreeTableNode> libraryNodes = ((AbstractSortableTreeTableNode) fullModel.getRoot()).children(); libraryNodes.hasMoreElements();) {
            libraries.add(getLibraryWithTemplates((CodeTemplateLibraryTreeTableNode) libraryNodes.nextElement()));
        }

        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        try {
            String librariesXml = serializer.serialize(libraries);
            parent.exportFile(librariesXml, null, "XML", "Code template libraries export");
        } catch (Throwable t) {
            parent.alertThrowable(parent, t, "Unable to export: " + t.getMessage());
        }
    }

    private CodeTemplateLibrary getLibraryWithTemplates(CodeTemplateLibraryTreeTableNode libraryNode) {
        CodeTemplateLibrary library = new CodeTemplateLibrary(libraryNode.getLibrary());

        List<CodeTemplate> codeTemplates = new ArrayList<CodeTemplate>();
        for (Enumeration<? extends MutableTreeTableNode> codeTemplateNodes = findFullNode(libraryNode).children(); codeTemplateNodes.hasMoreElements();) {
            codeTemplates.add(new CodeTemplate(((CodeTemplateTreeTableNode) codeTemplateNodes.nextElement()).getCodeTemplate()));
        }
        library.setCodeTemplates(codeTemplates);

        return library;
    }

    public void doDeleteCodeTemplate() {
        deleteSelectedNode(true);
    }

    public void doDeleteLibrary() {
        deleteSelectedNode(false);
        updateLibrariesComboBox();
    }

    private void deleteSelectedNode(boolean codeTemplate) {
        stopTableEditing();
        setSaveEnabled(true);
        TreePath selectedPath = templateTreeTable.getTreeSelectionModel().getSelectionPath();

        if (selectedPath != null) {
            CodeTemplateTreeTableModel model = (CodeTemplateTreeTableModel) templateTreeTable.getTreeTableModel();
            MutableTreeTableNode selectedNode = (MutableTreeTableNode) selectedPath.getLastPathComponent();
            MutableTreeTableNode parent = (MutableTreeTableNode) selectedNode.getParent();
            int selectedNodeIndex = parent.getIndex(selectedNode);
            MutableTreeTableNode newSelectedNode = null;

            if (!codeTemplate && selectedNode.getChildCount() > 0) {
                if (!this.parent.alertOkCancel(this.parent, "The selected library contains " + selectedNode.getChildCount() + " code templates. If you delete the library, the code templates will be deleted as well. Are you sure you wish to continue?")) {
                    return;
                }

                for (MutableTreeTableNode codeTemplateNode : Collections.list(selectedNode.children())) {
                    model.removeNodeFromParent(codeTemplateNode);

                    if (model.getRoot() != fullModel.getRoot()) {
                        AbstractSortableTreeTableNode fullCodeTemplateNode = findFullNode((AbstractSortableTreeTableNode) codeTemplateNode);
                        fullModel.removeNodeFromParent(fullCodeTemplateNode);
                    }
                }
            }

            updateCurrentNode.set(false);
            selectedNode = (MutableTreeTableNode) selectedPath.getLastPathComponent();
            model.removeNodeFromParent(selectedNode);

            if (model.getRoot() != fullModel.getRoot()) {
                fullModel.removeNodeFromParent(findFullNode((AbstractSortableTreeTableNode) selectedNode));
            }

            if (selectedNodeIndex < parent.getChildCount()) {
                newSelectedNode = (MutableTreeTableNode) parent.getChildAt(selectedNodeIndex);
            } else if (parent.getChildCount() > 0) {
                newSelectedNode = (MutableTreeTableNode) parent.getChildAt(parent.getChildCount() - 1);
            } else if (codeTemplate) {
                newSelectedNode = parent;
            }

            if (newSelectedNode != null) {
                final TreePath newSelectedPath = new TreePath(((CodeTemplateTreeTableModel) templateTreeTable.getTreeTableModel()).getPathToRoot(newSelectedNode));
                selectTemplatePath(newSelectedPath);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        selectTemplatePath(newSelectedPath);
                        updateCurrentNode.set(true);
                    }
                });
            } else {
                switchSplitPaneComponent(blankPanel);
                updateCurrentNode.set(true);
            }

            updateFilterNotification();
        }
    }

    public void doValidateCodeTemplate() {
        doValidateCodeTemplate(true);
    }

    private boolean doValidateCodeTemplate(boolean showSuccessMessage) {
        stopTableEditing();
        String validationMessage = null;

        try {
            JavaScriptContextUtil.getGlobalContextForValidation().compileString("function rhinoWrapper() {" + templateCodeTextArea.getText() + "\n}", UUID.randomUUID().toString(), 1, null);
        } catch (EvaluatorException e) {
            validationMessage = "Error on line " + e.lineNumber() + ": " + e.getMessage() + ".";
        } catch (Exception e) {
            validationMessage = "Unknown error occurred during validation.";
        } finally {
            Context.exit();
        }

        if (validationMessage == null) {
            if (showSuccessMessage) {
                parent.alertInformation(this, "Validation successful.");
            }
            return true;
        } else {
            parent.alertInformation(this, validationMessage);
            return false;
        }
    }

    private void initComponents() {
        splitPane = new JSplitPane();
        splitPane.setBackground(getBackground());
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(Preferences.userNodeForPackage(Mirth.class).getInt("height", UIConstants.MIRTH_HEIGHT) / 3);
        splitPane.setResizeWeight(0.5);

        topPanel = new JPanel();
        topPanel.setBackground(UIConstants.COMBO_BOX_BACKGROUND);

        final CodeTemplateTreeTableCellEditor templateCellEditor = new CodeTemplateTreeTableCellEditor(this);

        templateTreeTable = new MirthTreeTable("CodeTemplate", new HashSet<String>(Arrays.asList(new String[] {
                "Name", "Description", "Revision", "Last Modified" }))) {

            private TreeTableNode selectedNode;

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == TEMPLATE_NAME_COLUMN;
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (isHierarchical(column)) {
                    return templateCellEditor;
                } else {
                    return super.getCellEditor(row, column);
                }
            }

            @Override
            protected void beforeSort() {
                updateCurrentNode();
                updateCurrentNode.set(false);

                int selectedRow = templateTreeTable.getSelectedRow();
                selectedNode = selectedRow >= 0 ? (TreeTableNode) templateTreeTable.getPathForRow(selectedRow).getLastPathComponent() : null;
            }

            @Override
            protected void afterSort() {
                final TreePath selectedPath = selectPathFromNodeId(selectedNode, (CodeTemplateRootTreeTableNode) templateTreeTable.getTreeTableModel().getRoot());
                if (selectedPath != null) {
                    selectTemplatePath(selectedPath);
                }

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (selectedPath != null) {
                            selectTemplatePath(selectedPath);
                        }
                        updateCurrentNode.set(true);
                    }
                });
            }
        };

        DefaultTreeTableModel model = new CodeTemplateTreeTableModel();
        model.setColumnIdentifiers(Arrays.asList(new String[] { "Name", "Id", "Type", "Description",
                "Revision", "Last Modified" }));

        CodeTemplateRootTreeTableNode rootNode = new CodeTemplateRootTreeTableNode();
        model.setRoot(rootNode);

        fullModel = new CodeTemplateTreeTableModel();
        fullModel.setColumnIdentifiers(Arrays.asList(new String[] { "Name", "Id", "Type",
                "Description", "Revision", "Last Modified" }));

        CodeTemplateRootTreeTableNode fullRootNode = new CodeTemplateRootTreeTableNode();
        fullModel.setRoot(fullRootNode);

        templateTreeTable.setColumnFactory(new CodeTemplateTableColumnFactory());
        templateTreeTable.setTreeTableModel(model);
        templateTreeTable.setOpenIcon(null);
        templateTreeTable.setClosedIcon(null);
        templateTreeTable.setLeafIcon(null);
        templateTreeTable.setRootVisible(false);
        templateTreeTable.setDoubleBuffered(true);
        templateTreeTable.setDragEnabled(false);
        templateTreeTable.setRowSelectionAllowed(true);
        templateTreeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateTreeTable.setRowHeight(UIConstants.ROW_HEIGHT);
        templateTreeTable.setFocusable(true);
        templateTreeTable.setOpaque(true);
        templateTreeTable.getTableHeader().setReorderingAllowed(true);
        templateTreeTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        templateTreeTable.setEditable(true);
        templateTreeTable.setSortable(true);
        templateTreeTable.setAutoCreateColumnsFromModel(false);
        templateTreeTable.setShowGrid(true, true);
        templateTreeTable.restoreColumnPreferences();
        templateTreeTable.setMirthColumnControlEnabled(true);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            templateTreeTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        templateTreeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                checkSelection(evt);
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                checkSelection(evt);
            }

            private void checkSelection(MouseEvent evt) {
                int row = templateTreeTable.rowAtPoint(new Point(evt.getX(), evt.getY()));

                if (row < 0) {
                    templateTreeTable.clearSelection();
                }

                if (evt.isPopupTrigger()) {
                    if (row != -1) {
                        if (!templateTreeTable.isRowSelected(row)) {
                            templateTreeTable.setRowSelectionInterval(row, row);
                        }
                    }
                    codeTemplatePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

        templateTreeTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    TreePath selectedPath = templateTreeTable.getTreeSelectionModel().getSelectionPath();
                    if (selectedPath != null) {
                        MutableTreeTableNode selectedNode = (MutableTreeTableNode) selectedPath.getLastPathComponent();
                        if (selectedNode instanceof CodeTemplateLibraryTreeTableNode && codeTemplateTasks.getContentPane().getComponent(TASK_CODE_TEMPLATE_LIBRARY_DELETE).isVisible()) {
                            doDeleteLibrary();
                        } else if (selectedNode instanceof CodeTemplateTreeTableNode && codeTemplateTasks.getContentPane().getComponent(TASK_CODE_TEMPLATE_DELETE).isVisible()) {
                            doDeleteCodeTemplate();
                        }
                    }
                }
            }
        });

        templateTreeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (!evt.getValueIsAdjusting() && !templateTreeTable.getSelectionModel().getValueIsAdjusting()) {
                    int selectedRow = templateTreeTable.getSelectedRow();

                    boolean saveEnabled = isSaveEnabled();
                    boolean adjusting = saveAdjusting.getAndSet(true);

                    printTreeTable();

                    updateCurrentNode();
                    currentSelectedRow = selectedRow;

                    if (selectedRow < 0) {
                        if (!adjusting) {
                            switchSplitPaneComponent(blankPanel);
                        }
                    } else {
                        TreePath path = templateTreeTable.getPathForRow(selectedRow);
                        if (path != null) {
                            TreeTableNode node = (TreeTableNode) path.getLastPathComponent();

                            if (node instanceof CodeTemplateLibraryTreeTableNode) {
                                setLibraryProperties((CodeTemplateLibraryTreeTableNode) node);
                                switchSplitPaneComponent(libraryPanel);
                            } else if (node instanceof CodeTemplateTreeTableNode) {
                                setCodeTemplateProperties((CodeTemplateTreeTableNode) node);
                                switchSplitPaneComponent(templatePanel);
                            }
                        }
                    }

                    updateTasks();

                    setSaveEnabled(saveEnabled);
                    if (!adjusting) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                saveAdjusting.set(false);
                            }
                        });
                    }
                }
            }
        });

        templateTreeTable.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                treeExpansionChanged();
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                treeExpansionChanged();
            }

            private void treeExpansionChanged() {
                updateCurrentNode();
                updateCurrentNode.set(false);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateCurrentNode.set(true);
                    }
                });
            }
        });

        templateTreeTableScrollPane = new JScrollPane(templateTreeTable);
        templateTreeTableScrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x6E6E6E)));

        templateFilterNotificationLabel = new JLabel();

        templateFilterLabel = new JLabel("Filter:");

        templateFilterField = new JTextField();
        templateFilterField.setToolTipText("Filters (by name) the code templates and libraries that show up in the table above.");

        templateFilterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent evt) {
                filterChanged(evt);
            }

            @Override
            public void insertUpdate(DocumentEvent evt) {
                filterChanged(evt);
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                filterChanged(evt);
            }

            private void filterChanged(DocumentEvent evt) {
                try {
                    updateTemplateFilter(evt.getDocument().getText(0, evt.getLength()));
                } catch (BadLocationException e) {
                }
            }
        });

        templateFilterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                updateTemplateFilter(templateFilterField.getText());
            }
        });

        blankPanel = new JPanel();

        libraryPanel = new JPanel();
        libraryPanel.setBackground(splitPane.getBackground());

        libraryLeftPanel = new JPanel();
        libraryLeftPanel.setBackground(libraryPanel.getBackground());

        librarySummaryLabel = new JLabel("Summary:");
        librarySummaryValue = new JLabel();

        libraryDescriptionLabel = new JLabel("Description:");
        libraryDescriptionScrollPane = new MirthRTextScrollPane(null, false, SyntaxConstants.SYNTAX_STYLE_NONE, false);

        DocumentListener codeChangeListener = new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent evt) {
                codeChanged();
            }

            @Override
            public void insertUpdate(DocumentEvent evt) {
                codeChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                codeChanged();
            }

            private void codeChanged() {
                if (codeChangeWorker != null) {
                    codeChangeWorker.cancel(true);
                }

                int selectedRow = templateTreeTable.getSelectedRow();
                if (selectedRow >= 0) {
                    TreePath selectedPath = templateTreeTable.getPathForRow(selectedRow);
                    if (selectedPath != null) {
                        codeChangeWorker = new CodeChangeWorker((String) ((TreeTableNode) selectedPath.getLastPathComponent()).getValueAt(TEMPLATE_ID_COLUMN));
                        codeChangeWorker.execute();
                    }
                }
            }
        };
        libraryDescriptionScrollPane.getDocument().addDocumentListener(codeChangeListener);

        libraryRightPanel = new JPanel();
        libraryRightPanel.setBackground(libraryPanel.getBackground());

        libraryChannelsSelectPanel = new JPanel();
        libraryChannelsSelectPanel.setBackground(libraryRightPanel.getBackground());

        libraryChannelsLabel = new JLabel("<html><b>Channels</b></html>");
        libraryChannelsLabel.setForeground(new Color(64, 64, 64));

        libraryChannelsSelectAllLabel = new JLabel("<html><u>Select All</u></html>");
        libraryChannelsSelectAllLabel.setForeground(Color.BLUE);
        libraryChannelsSelectAllLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        libraryChannelsSelectAllLabel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent evt) {
                if (evt.getComponent().isEnabled()) {
                    for (int row = 0; row < libraryChannelsTable.getRowCount(); row++) {
                        ChannelInfo channelInfo = (ChannelInfo) libraryChannelsTable.getValueAt(row, LIBRARY_CHANNELS_NAME_COLUMN);
                        channelInfo.setEnabled(true);
                        libraryChannelsTable.setValueAt(channelInfo, row, LIBRARY_CHANNELS_NAME_COLUMN);
                    }
                    setSaveEnabled(true);
                }
            }
        });

        libraryChannelsDeselectAllLabel = new JLabel("<html><u>Deselect All</u></html>");
        libraryChannelsDeselectAllLabel.setForeground(Color.BLUE);
        libraryChannelsDeselectAllLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        libraryChannelsDeselectAllLabel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent evt) {
                if (evt.getComponent().isEnabled()) {
                    for (int row = 0; row < libraryChannelsTable.getRowCount(); row++) {
                        ChannelInfo channelInfo = (ChannelInfo) libraryChannelsTable.getValueAt(row, LIBRARY_CHANNELS_NAME_COLUMN);
                        channelInfo.setEnabled(false);
                        libraryChannelsTable.setValueAt(channelInfo, row, LIBRARY_CHANNELS_NAME_COLUMN);
                    }
                    setSaveEnabled(true);
                }
            }
        });

        libraryChannelsFilterLabel = new JLabel("Filter:");
        libraryChannelsFilterField = new JTextField();
        libraryChannelsFilterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent evt) {
                libraryChannelsTable.getRowSorter().allRowsChanged();
            }

            @Override
            public void insertUpdate(DocumentEvent evt) {
                libraryChannelsTable.getRowSorter().allRowsChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                libraryChannelsTable.getRowSorter().allRowsChanged();
            }
        });

        libraryChannelsTable = new MirthTable();
        libraryChannelsTable.setModel(new RefreshTableModel(new Object[] { "Name", "Id" }, 0));
        libraryChannelsTable.setDragEnabled(false);
        libraryChannelsTable.setRowSelectionAllowed(false);
        libraryChannelsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        libraryChannelsTable.setFocusable(false);
        libraryChannelsTable.setOpaque(true);
        libraryChannelsTable.getTableHeader().setReorderingAllowed(false);
        libraryChannelsTable.setEditable(true);

        OffsetRowSorter libraryChannelsRowSorter = new OffsetRowSorter(libraryChannelsTable.getModel(), 1);
        libraryChannelsRowSorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                String name = entry.getStringValue(LIBRARY_CHANNELS_NAME_COLUMN);
                return name.equals(NEW_CHANNELS) || StringUtils.containsIgnoreCase(name, StringUtils.trim(libraryChannelsFilterField.getText()));
            }
        });
        libraryChannelsTable.setRowSorter(libraryChannelsRowSorter);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            libraryChannelsTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        libraryChannelsTable.getColumnExt(LIBRARY_CHANNELS_NAME_COLUMN).setCellRenderer(new ChannelsTableCellRenderer());
        libraryChannelsTable.getColumnExt(LIBRARY_CHANNELS_NAME_COLUMN).setCellEditor(new ChannelsTableCellEditor());

        // Hide ID column
        libraryChannelsTable.getColumnExt(LIBRARY_CHANNELS_ID_COLUMN).setVisible(false);

        libraryChannelsScrollPane = new JScrollPane(libraryChannelsTable);

        templatePanel = new JPanel();
        templatePanel.setBackground(splitPane.getBackground());

        templateLeftPanel = new JPanel();
        templateLeftPanel.setBackground(templatePanel.getBackground());

        templateLibraryLabel = new JLabel("Library:");
        templateLibraryComboBox = new JComboBox<String>();
        templateLibraryComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                libraryComboBoxActionPerformed();
            }
        });

        templateTypeLabel = new JLabel("Type:");
        templateTypeComboBox = new JComboBox<CodeTemplateType>(CodeTemplateType.values());
        templateTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setSaveEnabled(true);
            }
        });

        templateCodeLabel = new JLabel("Code:");
        templateCodeTextArea = new MirthRTextScrollPane(ContextType.GLOBAL_DEPLOY);
        templateCodeTextArea.getDocument().addDocumentListener(codeChangeListener);

        templateAutoGenerateDocumentationButton = new JButton("Update JSDoc");
        templateAutoGenerateDocumentationButton.setToolTipText("<html>Generates/updates a JSDoc at the beginning of your<br/>code, with parameter/return annotations as needed.</html>");
        templateAutoGenerateDocumentationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String currentText = templateCodeTextArea.getText();
                String newText = CodeTemplateUtil.updateCode(templateCodeTextArea.getText());
                templateCodeTextArea.setText(newText, false);
                if (!currentText.equals(newText)) {
                    setSaveEnabled(true);
                }
            }
        });

        templateRightPanel = new JPanel();
        templateRightPanel.setBackground(templatePanel.getBackground());

        templateContextSelectPanel = new JPanel();
        templateContextSelectPanel.setBackground(templateRightPanel.getBackground());

        templateContextLabel = new JLabel("<html><b>Context</b></html>");
        templateContextLabel.setForeground(new Color(64, 64, 64));

        templateContextSelectAllLabel = new JLabel("<html><u>Select All</u></html>");
        templateContextSelectAllLabel.setForeground(Color.BLUE);
        templateContextSelectAllLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        templateContextSelectAllLabel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent evt) {
                TreeTableNode root = (TreeTableNode) templateContextTreeTable.getTreeTableModel().getRoot();
                for (Enumeration<? extends TreeTableNode> groups = root.children(); groups.hasMoreElements();) {
                    TreeTableNode group = groups.nextElement();
                    ((MutablePair<Integer, String>) group.getUserObject()).setLeft(MirthTriStateCheckBox.CHECKED);
                    for (Enumeration<? extends TreeTableNode> children = group.children(); children.hasMoreElements();) {
                        ((MutablePair<Integer, String>) children.nextElement().getUserObject()).setLeft(MirthTriStateCheckBox.CHECKED);
                    }
                }
                templateContextTreeTable.updateUI();
                setSaveEnabled(true);
            }
        });

        templateContextDeselectAllLabel = new JLabel("<html><u>Deselect All</u></html>");
        templateContextDeselectAllLabel.setForeground(Color.BLUE);
        templateContextDeselectAllLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        templateContextDeselectAllLabel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent evt) {
                TreeTableNode root = (TreeTableNode) templateContextTreeTable.getTreeTableModel().getRoot();
                for (Enumeration<? extends TreeTableNode> groups = root.children(); groups.hasMoreElements();) {
                    TreeTableNode group = groups.nextElement();
                    ((MutablePair<Integer, String>) group.getUserObject()).setLeft(MirthTriStateCheckBox.UNCHECKED);
                    for (Enumeration<? extends TreeTableNode> children = group.children(); children.hasMoreElements();) {
                        ((MutablePair<Integer, String>) children.nextElement().getUserObject()).setLeft(MirthTriStateCheckBox.UNCHECKED);
                    }
                }
                templateContextTreeTable.updateUI();
                setSaveEnabled(true);
            }
        });

        final TableCellEditor contextCellEditor = new ContextTreeTableCellEditor(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setSaveEnabled(true);
            }
        });

        templateContextTreeTable = new MirthTreeTable() {
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (isHierarchical(column)) {
                    return contextCellEditor;
                } else {
                    return super.getCellEditor(row, column);
                }
            }
        };

        DefaultMutableTreeTableNode rootContextNode = new DefaultMutableTreeTableNode();
        DefaultMutableTreeTableNode globalScriptsNode = new DefaultMutableTreeTableNode(new MutablePair<Integer, String>(MirthTriStateCheckBox.CHECKED, "Global Scripts"));
        globalScriptsNode.add(new DefaultMutableTreeTableNode(new MutablePair<Integer, ContextType>(MirthTriStateCheckBox.CHECKED, ContextType.GLOBAL_DEPLOY)));
        globalScriptsNode.add(new DefaultMutableTreeTableNode(new MutablePair<Integer, ContextType>(MirthTriStateCheckBox.CHECKED, ContextType.GLOBAL_UNDEPLOY)));
        globalScriptsNode.add(new DefaultMutableTreeTableNode(new MutablePair<Integer, ContextType>(MirthTriStateCheckBox.CHECKED, ContextType.GLOBAL_PREPROCESSOR)));
        globalScriptsNode.add(new DefaultMutableTreeTableNode(new MutablePair<Integer, ContextType>(MirthTriStateCheckBox.CHECKED, ContextType.GLOBAL_POSTPROCESSOR)));
        rootContextNode.add(globalScriptsNode);
        DefaultMutableTreeTableNode channelScriptsNode = new DefaultMutableTreeTableNode(new MutablePair<Integer, String>(MirthTriStateCheckBox.CHECKED, "Channel Scripts"));
        channelScriptsNode.add(new DefaultMutableTreeTableNode(new MutablePair<Integer, ContextType>(MirthTriStateCheckBox.CHECKED, ContextType.CHANNEL_DEPLOY)));
        channelScriptsNode.add(new DefaultMutableTreeTableNode(new MutablePair<Integer, ContextType>(MirthTriStateCheckBox.CHECKED, ContextType.CHANNEL_UNDEPLOY)));
        channelScriptsNode.add(new DefaultMutableTreeTableNode(new MutablePair<Integer, ContextType>(MirthTriStateCheckBox.CHECKED, ContextType.CHANNEL_PREPROCESSOR)));
        channelScriptsNode.add(new DefaultMutableTreeTableNode(new MutablePair<Integer, ContextType>(MirthTriStateCheckBox.CHECKED, ContextType.CHANNEL_POSTPROCESSOR)));
        channelScriptsNode.add(new DefaultMutableTreeTableNode(new MutablePair<Integer, ContextType>(MirthTriStateCheckBox.CHECKED, ContextType.CHANNEL_ATTACHMENT)));
        channelScriptsNode.add(new DefaultMutableTreeTableNode(new MutablePair<Integer, ContextType>(MirthTriStateCheckBox.CHECKED, ContextType.CHANNEL_BATCH)));
        rootContextNode.add(channelScriptsNode);
        DefaultMutableTreeTableNode sourceConnectorNode = new DefaultMutableTreeTableNode(new MutablePair<Integer, String>(MirthTriStateCheckBox.CHECKED, "Source Connector"));
        sourceConnectorNode.add(new DefaultMutableTreeTableNode(new MutablePair<Integer, ContextType>(MirthTriStateCheckBox.CHECKED, ContextType.SOURCE_RECEIVER)));
        sourceConnectorNode.add(new DefaultMutableTreeTableNode(new MutablePair<Integer, ContextType>(MirthTriStateCheckBox.CHECKED, ContextType.SOURCE_FILTER_TRANSFORMER)));
        rootContextNode.add(sourceConnectorNode);
        DefaultMutableTreeTableNode destinationConnectorNode = new DefaultMutableTreeTableNode(new MutablePair<Integer, String>(MirthTriStateCheckBox.CHECKED, "Destination Connector"));
        destinationConnectorNode.add(new DefaultMutableTreeTableNode(new MutablePair<Integer, ContextType>(MirthTriStateCheckBox.CHECKED, ContextType.DESTINATION_FILTER_TRANSFORMER)));
        destinationConnectorNode.add(new DefaultMutableTreeTableNode(new MutablePair<Integer, ContextType>(MirthTriStateCheckBox.CHECKED, ContextType.DESTINATION_DISPATCHER)));
        destinationConnectorNode.add(new DefaultMutableTreeTableNode(new MutablePair<Integer, ContextType>(MirthTriStateCheckBox.CHECKED, ContextType.DESTINATION_RESPONSE_TRANSFORMER)));
        rootContextNode.add(destinationConnectorNode);

        DefaultTreeTableModel contextModel = new SortableTreeTableModel(rootContextNode);
        contextModel.setColumnIdentifiers(Arrays.asList(new String[] { "Context" }));
        templateContextTreeTable.setTreeTableModel(contextModel);

        templateContextTreeTable.setRootVisible(false);
        templateContextTreeTable.setDragEnabled(false);
        templateContextTreeTable.setRowSelectionAllowed(false);
        templateContextTreeTable.setRowHeight(UIConstants.ROW_HEIGHT);
        templateContextTreeTable.setFocusable(false);
        templateContextTreeTable.setOpaque(true);
        templateContextTreeTable.getTableHeader().setReorderingAllowed(false);
        templateContextTreeTable.setEditable(true);
        templateContextTreeTable.setSortable(false);
        templateContextTreeTable.setShowGrid(true, true);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            templateContextTreeTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        templateContextTreeTable.setTreeCellRenderer(new ContextTreeTableCellRenderer());
        templateContextTreeTable.setOpenIcon(null);
        templateContextTreeTable.setClosedIcon(null);
        templateContextTreeTable.setLeafIcon(null);

        templateContextTreeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                checkSelection(evt);
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                checkSelection(evt);
            }

            private void checkSelection(MouseEvent evt) {
                if (templateContextTreeTable.rowAtPoint(new Point(evt.getX(), evt.getY())) < 0) {
                    templateContextTreeTable.clearSelection();
                }
            }
        });

        templateContextTreeTable.getTreeTableModel().addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent evt) {
                if (ArrayUtils.isNotEmpty(evt.getChildren())) {
                    TreeTableNode node = (TreeTableNode) evt.getChildren()[0];

                    if (evt.getTreePath().getPathCount() == 2) {
                        boolean allChildren = true;
                        boolean noChildren = true;
                        for (Enumeration<? extends TreeTableNode> children = node.getParent().children(); children.hasMoreElements();) {
                            TreeTableNode child = children.nextElement();
                            if (((Pair<Integer, ContextType>) child.getUserObject()).getLeft() == MirthTriStateCheckBox.UNCHECKED) {
                                allChildren = false;
                            } else {
                                noChildren = false;
                            }
                        }

                        int value;
                        if (allChildren) {
                            value = MirthTriStateCheckBox.CHECKED;
                        } else if (noChildren) {
                            value = MirthTriStateCheckBox.UNCHECKED;
                        } else {
                            value = MirthTriStateCheckBox.PARTIAL;
                        }

                        ((MutablePair<Integer, String>) node.getParent().getUserObject()).setLeft(value);
                    } else if (evt.getTreePath().getPathCount() == 1) {
                        int value = ((Pair<Integer, String>) node.getUserObject()).getLeft();

                        for (Enumeration<? extends TreeTableNode> children = node.children(); children.hasMoreElements();) {
                            ((MutablePair<Integer, ContextType>) children.nextElement().getUserObject()).setLeft(value);
                        }
                    }
                }
            }

            @Override
            public void treeNodesInserted(TreeModelEvent evt) {}

            @Override
            public void treeNodesRemoved(TreeModelEvent evt) {}

            @Override
            public void treeStructureChanged(TreeModelEvent evt) {}
        });

        templateContextTreeTableScrollPane = new JScrollPane(templateContextTreeTable);
    }

    private void initToolTips() {
        libraryChannelsFilterField.setToolTipText("Filters the channels that show up in the table below.");
        String toolTipText = "<html>Select the channels to include this library in. If " + NEW_CHANNELS + "<br/>is selected, any new channels that are created or imported will<br/>automatically have the code templates within this library included.</html>";
        libraryChannelsTable.getTableHeader().setToolTipText(toolTipText);
        libraryChannelsTable.setToolTipText(toolTipText);
        templateLibraryComboBox.setToolTipText("<html>The parent library that this code template belongs to.</html>");
        templateTypeComboBox.setToolTipText("<html>The type of code template to create.<br/><b>&nbsp;&nbsp;&nbsp;&nbsp;- " + CodeTemplateType.FUNCTION + ":</b> The template will be compiled in with scripts, and the drag-and-drop will include the function signature.<br/><b>&nbsp;&nbsp;&nbsp;&nbsp;- " + CodeTemplateType.DRAG_AND_DROP_CODE + ":</b> The template will not be compiled in with scripts, and the drag-and-drop will<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;include the entire code block verbatim (except for the initial documentation block).<br/><b>&nbsp;&nbsp;&nbsp;&nbsp;- " + CodeTemplateType.COMPILED_CODE + ":</b> The template will be compiled in with scripts, but drag-and-drop will not be available at all.</html>");
        toolTipText = "Select which scripts should have access to this code template.";
        templateContextTreeTable.setToolTipText(toolTipText);
        templateContextTreeTable.getColumnExt(0).setToolTipText(toolTipText);
    }

    private void initLayout() {
        topPanel.setLayout(new MigLayout("insets 0 0 5 0, novisualpadding, hidemode 3, fill"));
        topPanel.add(templateTreeTableScrollPane, "grow, sx, push");
        topPanel.add(templateFilterNotificationLabel, "newline, gapbefore 13");
        topPanel.add(templateFilterLabel, "right, split");
        topPanel.add(templateFilterField, "right, w :300, gapafter 5");
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(blankPanel);

        libraryPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));

        libraryLeftPanel.setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill", "[]13[]", "[]8[]"));
        libraryLeftPanel.add(librarySummaryLabel, "right");
        libraryLeftPanel.add(librarySummaryValue, "grow");
        libraryLeftPanel.add(libraryDescriptionLabel, "newline, top, right");
        libraryLeftPanel.add(libraryDescriptionScrollPane, "grow, push, w :400, h 100:100");
        libraryPanel.add(libraryLeftPanel, "grow, push");

        libraryRightPanel.setLayout(new MigLayout("insets 12 0 12 12, novisualpadding, hidemode 3, fill", "", "[][][grow]"));
        libraryRightPanel.add(libraryChannelsLabel, "left");

        libraryChannelsSelectPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        libraryChannelsSelectPanel.add(libraryChannelsSelectAllLabel);
        libraryChannelsSelectPanel.add(new JLabel("|"));
        libraryChannelsSelectPanel.add(libraryChannelsDeselectAllLabel);
        libraryRightPanel.add(libraryChannelsSelectPanel, "right");

        libraryRightPanel.add(libraryChannelsFilterLabel, "newline, split 2, sx");
        libraryRightPanel.add(libraryChannelsFilterField, "grow");

        libraryRightPanel.add(libraryChannelsScrollPane, "newline, grow, h 100:100, sx");

        libraryPanel.add(libraryRightPanel, "grow, w 220!");

        templatePanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));

        templateLeftPanel.setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill", "[]13[grow]", "[][][grow][]"));
        templateLeftPanel.add(templateLibraryLabel, "right");
        templateLeftPanel.add(templateLibraryComboBox, "w 200:");
        templateLeftPanel.add(templateTypeLabel, "newline, right");
        templateLeftPanel.add(templateTypeComboBox);
        templateLeftPanel.add(templateCodeLabel, "newline, top, right");
        templateLeftPanel.add(templateCodeTextArea, "grow, sx, w :400, h 127:127");
        templateLeftPanel.add(templateAutoGenerateDocumentationButton, "sx, right");
        templatePanel.add(templateLeftPanel, "grow, push");

        templateRightPanel.setLayout(new MigLayout("insets 12 0 12 12, novisualpadding, hidemode 3, fill"));
        templateRightPanel.add(templateContextLabel, "left");

        templateContextSelectPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        templateContextSelectPanel.add(templateContextSelectAllLabel);
        templateContextSelectPanel.add(new JLabel("|"));
        templateContextSelectPanel.add(templateContextDeselectAllLabel);
        templateRightPanel.add(templateContextSelectPanel, "right");

        templateRightPanel.add(templateContextTreeTableScrollPane, "newline, grow, sx, push");
        templatePanel.add(templateRightPanel, "grow, w 220!, h 100:100");

        add(splitPane, "grow");
    }

    private void switchSplitPaneComponent(Component c) {
        int dividerLocation = splitPane.getDividerLocation();
        splitPane.setRightComponent(c);
        splitPane.setDividerLocation(dividerLocation);
    }

    private void setLibraryProperties(CodeTemplateLibraryTreeTableNode libraryNode) {
        CodeTemplateLibrary library = libraryNode.getLibrary();

        List<CodeTemplate> codeTemplates = new ArrayList<CodeTemplate>();
        for (Enumeration<? extends MutableTreeTableNode> en = libraryNode.children(); en.hasMoreElements();) {
            codeTemplates.add(((CodeTemplateTreeTableNode) en.nextElement()).getCodeTemplate());
        }

        Map<CodeTemplateType, Integer> typeMap = new HashMap<CodeTemplateType, Integer>();
        for (CodeTemplateType type : CodeTemplateType.values()) {
            typeMap.put(type, 0);
        }
        for (CodeTemplate codeTemplate : codeTemplates) {
            typeMap.put(codeTemplate.getType(), typeMap.get(codeTemplate.getType()) + 1);
        }
        StringBuilder summary = new StringBuilder();
        for (CodeTemplateType type : CodeTemplateType.values()) {
            summary.append(typeMap.get(type)).append(' ').append(type);
            if (typeMap.get(type) != 1) {
                summary.append('s');
            }
            if (type.ordinal() < CodeTemplateType.values().length - 1) {
                summary.append(", ");
            }
        }
        librarySummaryValue.setText(summary.toString());

        libraryDescriptionScrollPane.setText(library.getDescription());

        setLibraryChannels(library.isIncludeNewChannels(), library.getEnabledChannelIds(), library.getDisabledChannelIds());
    }

    private void setLibraryChannels(boolean includeNewChannels, Set<String> enabledChannelIds, Set<String> disabledChannelIds) {
        for (int row = 0; row < libraryChannelsTable.getModel().getRowCount(); row++) {
            ChannelInfo channelInfo = (ChannelInfo) libraryChannelsTable.getModel().getValueAt(row, LIBRARY_CHANNELS_NAME_COLUMN);
            String channelId = (String) libraryChannelsTable.getModel().getValueAt(row, LIBRARY_CHANNELS_ID_COLUMN);

            if (channelId.equals(NEW_CHANNELS)) {
                channelInfo.setEnabled(includeNewChannels);
            } else {
                channelInfo.setEnabled(enabledChannelIds.contains(channelId) || (!disabledChannelIds.contains(channelId) && includeNewChannels));
            }

            libraryChannelsTable.getModel().setValueAt(channelInfo, row, LIBRARY_CHANNELS_NAME_COLUMN);
        }
    }

    private void updateCurrentNode() {
        if (currentSelectedRow >= 0 && updateCurrentNode.get()) {
            TreePath selectedPath = templateTreeTable.getPathForRow(currentSelectedRow);
            if (selectedPath != null) {
                TreeTableNode selectedNode = (TreeTableNode) selectedPath.getLastPathComponent();

                if (selectedNode instanceof CodeTemplateLibraryTreeTableNode) {
                    CodeTemplateLibraryTreeTableNode libraryNode = (CodeTemplateLibraryTreeTableNode) selectedNode;
                    updateLibraryNode(libraryNode);

                    if (templateTreeTable.getTreeTableModel().getRoot() != fullModel.getRoot()) {
                        updateLibraryNode((CodeTemplateLibraryTreeTableNode) findFullNode(libraryNode));
                    }
                } else {
                    CodeTemplateTreeTableNode codeTemplateNode = (CodeTemplateTreeTableNode) selectedNode;
                    updateCodeTemplateNode(codeTemplateNode);

                    if (templateTreeTable.getTreeTableModel().getRoot() != fullModel.getRoot()) {
                        CodeTemplateTreeTableNode fullCodeTemplateNode = (CodeTemplateTreeTableNode) findFullNode(codeTemplateNode);
                        fullCodeTemplateNode.setValueAt(codeTemplateNode.getValueAt(TEMPLATE_NAME_COLUMN), TEMPLATE_NAME_COLUMN);
                        updateCodeTemplateNode(fullCodeTemplateNode);
                    }
                }
            }
        }
    }

    private void updateLibraryNode(CodeTemplateLibraryTreeTableNode libraryNode) {
        if (libraryNode != null) {
            libraryNode.setValueAt(libraryDescriptionScrollPane.getText(), TEMPLATE_DESCRIPTION_COLUMN);

            Set<String> enabledChannelIds = new HashSet<String>();
            Set<String> disabledChannelIds = new HashSet<String>();

            for (int row = 0; row < libraryChannelsTable.getModel().getRowCount(); row++) {
                ChannelInfo channelInfo = (ChannelInfo) libraryChannelsTable.getModel().getValueAt(row, LIBRARY_CHANNELS_NAME_COLUMN);
                String channelId = (String) libraryChannelsTable.getModel().getValueAt(row, LIBRARY_CHANNELS_ID_COLUMN);

                if (channelId.equals(NEW_CHANNELS)) {
                    libraryNode.getLibrary().setIncludeNewChannels(channelInfo.isEnabled());
                } else if (channelInfo.isEnabled()) {
                    enabledChannelIds.add(channelId);
                } else {
                    disabledChannelIds.add(channelId);
                }
            }

            libraryNode.getLibrary().setEnabledChannelIds(enabledChannelIds);
            libraryNode.getLibrary().setDisabledChannelIds(disabledChannelIds);
        }
    }

    private void updateCodeTemplateNode(CodeTemplateTreeTableNode codeTemplateNode) {
        if (codeTemplateNode != null) {
            codeTemplateNode.setValueAt(templateTypeComboBox.getSelectedItem(), TEMPLATE_TYPE_COLUMN);
            codeTemplateNode.getCodeTemplate().setCode(templateCodeTextArea.getText());

            CodeTemplateContextSet contextSet = new CodeTemplateContextSet();
            for (Enumeration<? extends MutableTreeTableNode> groups = ((MutableTreeTableNode) templateContextTreeTable.getTreeTableModel().getRoot()).children(); groups.hasMoreElements();) {
                MutableTreeTableNode group = groups.nextElement();
                for (Enumeration<? extends MutableTreeTableNode> children = group.children(); children.hasMoreElements();) {
                    Pair<Integer, ContextType> pair = (Pair<Integer, ContextType>) children.nextElement().getUserObject();
                    if (pair.getLeft() == MirthTriStateCheckBox.CHECKED) {
                        try {
                            contextSet.add(pair.getRight());
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            codeTemplateNode.getCodeTemplate().setContextSet(contextSet);
        }
    }

    private void setCodeTemplateProperties(CodeTemplateTreeTableNode codeTemplateNode) {
        CodeTemplateLibraryTreeTableNode libraryNode = (CodeTemplateLibraryTreeTableNode) codeTemplateNode.getParent();
        CodeTemplate codeTemplate = codeTemplateNode.getCodeTemplate();

        libraryComboBoxAdjusting.set(true);
        templateLibraryComboBox.setSelectedItem(libraryNode.getLibrary().getName());
        templateTypeComboBox.setSelectedItem(codeTemplate.getType());
        templateCodeTextArea.setText(codeTemplate.getCode());
        updateContextTable(codeTemplate.getContextSet());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                libraryComboBoxAdjusting.set(false);
            }
        });
    }

    private void updateContextTable(CodeTemplateContextSet context) {
        DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode) templateContextTreeTable.getTreeTableModel().getRoot();

        for (Enumeration<? extends MutableTreeTableNode> groups = root.children(); groups.hasMoreElements();) {
            MutableTreeTableNode group = groups.nextElement();
            MutablePair<Integer, String> groupPair = (MutablePair<Integer, String>) group.getUserObject();
            boolean allChildren = true;
            boolean noChildren = true;

            for (Enumeration<? extends MutableTreeTableNode> children = group.children(); children.hasMoreElements();) {
                MutableTreeTableNode child = children.nextElement();
                MutablePair<Integer, ContextType> childPair = (MutablePair<Integer, ContextType>) child.getUserObject();

                if (context.contains(childPair.getRight())) {
                    childPair.setLeft(MirthTriStateCheckBox.CHECKED);
                    noChildren = false;
                } else {
                    childPair.setLeft(MirthTriStateCheckBox.UNCHECKED);
                    allChildren = false;
                }
            }

            if (allChildren) {
                groupPair.setLeft(MirthTriStateCheckBox.CHECKED);
            } else if (noChildren) {
                groupPair.setLeft(MirthTriStateCheckBox.UNCHECKED);
            } else {
                groupPair.setLeft(MirthTriStateCheckBox.PARTIAL);
            }
        }

        templateContextTreeTable.expandAll();
    }

    private void updateTasks() {
        int selectedRow = templateTreeTable.getSelectedRow();

        for (Pair<Component, Component> task : singleLibraryTaskComponents) {
            task.getLeft().setVisible(false);
            task.getRight().setVisible(false);
        }
        for (Pair<Component, Component> task : singleCodeTemplateTaskComponents) {
            task.getLeft().setVisible(false);
            task.getRight().setVisible(false);
        }

        if (selectedRow >= 0) {
            TreeTableNode selectedNode = (TreeTableNode) templateTreeTable.getPathForRow(selectedRow).getLastPathComponent();
            setTaskVisible(TASK_CODE_TEMPLATE_NEW);

            if (selectedNode instanceof CodeTemplateLibraryTreeTableNode) {
                setTaskVisible(TASK_CODE_TEMPLATE_LIBRARY_EXPORT);
                setTaskVisible(TASK_CODE_TEMPLATE_LIBRARY_DELETE);

                setTaskInvisible(TASK_CODE_TEMPLATE_EXPORT);
                setTaskInvisible(TASK_CODE_TEMPLATE_DELETE);
                setTaskInvisible(TASK_CODE_TEMPLATE_VALIDATE);

                for (Pair<Component, Component> task : singleLibraryTaskComponents) {
                    task.getLeft().setVisible(true);
                    task.getRight().setVisible(true);
                }
            } else if (selectedNode instanceof CodeTemplateTreeTableNode) {
                setTaskVisible(TASK_CODE_TEMPLATE_EXPORT);
                setTaskVisible(TASK_CODE_TEMPLATE_DELETE);
                setTaskVisible(TASK_CODE_TEMPLATE_VALIDATE);

                setTaskInvisible(TASK_CODE_TEMPLATE_LIBRARY_EXPORT);
                setTaskInvisible(TASK_CODE_TEMPLATE_LIBRARY_DELETE);

                for (Pair<Component, Component> task : singleCodeTemplateTaskComponents) {
                    task.getLeft().setVisible(true);
                    task.getRight().setVisible(true);
                }
            }
        } else {
            setTaskInvisible(TASK_CODE_TEMPLATE_NEW);
            setTaskInvisible(TASK_CODE_TEMPLATE_EXPORT);
            setTaskInvisible(TASK_CODE_TEMPLATE_DELETE);
            setTaskInvisible(TASK_CODE_TEMPLATE_VALIDATE);
            setTaskInvisible(TASK_CODE_TEMPLATE_LIBRARY_EXPORT);
            setTaskInvisible(TASK_CODE_TEMPLATE_LIBRARY_DELETE);
        }

        if (fullModel.getRoot().getChildCount() > 0) {
            setTaskVisible(TASK_CODE_TEMPLATE_IMPORT);
        } else {
            setTaskInvisible(TASK_CODE_TEMPLATE_IMPORT);
        }

        setTaskVisibility(TASK_CODE_TEMPLATE_LIBRARY_EXPORT_ALL, ((MutableTreeTableNode) fullModel.getRoot()).getChildCount() > 1);
    }

    void updateLibrariesComboBox() {
        MutableTreeTableNode root = (MutableTreeTableNode) fullModel.getRoot();
        List<String> libraryNames = new ArrayList<String>();
        for (Enumeration<? extends MutableTreeTableNode> libraries = root.children(); libraries.hasMoreElements();) {
            libraryNames.add(((CodeTemplateLibraryTreeTableNode) libraries.nextElement()).getLibrary().getName());
        }

        String selectedName = (String) templateLibraryComboBox.getSelectedItem();
        templateLibraryComboBox.setModel(new DefaultComboBoxModel<String>(libraryNames.toArray(new String[libraryNames.size()])));
        if (libraryNames.contains(selectedName)) {
            templateLibraryComboBox.setSelectedItem(selectedName);
        }
    }

    private void libraryComboBoxActionPerformed() {
        if (!libraryComboBoxAdjusting.get()) {
            setSaveEnabled(true);

            TreePath selectedPath = templateTreeTable.getTreeSelectionModel().getSelectionPath();
            if (selectedPath != null) {
                TreeTableNode selectedNode = (TreeTableNode) selectedPath.getLastPathComponent();
                if (selectedNode instanceof CodeTemplateTreeTableNode) {
                    CodeTemplateTreeTableNode codeTemplateNode = (CodeTemplateTreeTableNode) selectedNode;
                    CodeTemplateTreeTableModel model = (CodeTemplateTreeTableModel) templateTreeTable.getTreeTableModel();
                    CodeTemplateLibraryTreeTableNode currentParent = (CodeTemplateLibraryTreeTableNode) codeTemplateNode.getParent();

                    String libraryName = (String) templateLibraryComboBox.getSelectedItem();

                    CodeTemplateLibraryTreeTableNode libraryNode = null;
                    for (Enumeration<? extends MutableTreeTableNode> libraries = ((MutableTreeTableNode) model.getRoot()).children(); libraries.hasMoreElements();) {
                        CodeTemplateLibraryTreeTableNode library = (CodeTemplateLibraryTreeTableNode) libraries.nextElement();
                        if (library.getLibrary().getName().equals(libraryName)) {
                            libraryNode = library;
                            break;
                        }
                    }

                    CodeTemplateLibraryTreeTableNode fullLibraryNode = null;
                    for (Enumeration<? extends MutableTreeTableNode> libraries = ((MutableTreeTableNode) fullModel.getRoot()).children(); libraries.hasMoreElements();) {
                        CodeTemplateLibraryTreeTableNode library = (CodeTemplateLibraryTreeTableNode) libraries.nextElement();
                        if (library.getLibrary().getName().equals(libraryName)) {
                            fullLibraryNode = library;
                            break;
                        }
                    }

                    if (!currentParent.getLibraryId().equals(fullLibraryNode.getLibraryId())) {
                        updateCurrentNode();
                        updateCurrentNode.set(false);
                        model.removeNodeFromParent(codeTemplateNode);

                        if (libraryNode != null) {
                            model.insertNodeInto(codeTemplateNode, libraryNode);
                            selectTemplatePath(new TreePath(model.getPathToRoot(codeTemplateNode)));
                        } else {
                            selectTemplatePath(new TreePath(model.getPathToRoot(model.getRoot())));
                        }

                        if (model.getRoot() != fullModel.getRoot()) {
                            AbstractSortableTreeTableNode fullCodeTemplateNode = findFullNode(codeTemplateNode);
                            fullModel.removeNodeFromParent(fullCodeTemplateNode);
                            fullModel.insertNodeInto(fullCodeTemplateNode, fullLibraryNode);
                        }
                        updateCurrentNode.set(true);
                    }
                }
            }
        }
    }

    private void selectTemplatePath(TreePath path) {
        templateTreeTable.scrollPathToVisible(path);
        int selectedRow = templateTreeTable.getRowForPath(path);
        templateTreeTable.getTreeSelectionModel().setSelectionPath(path);
        templateTreeTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
        templateTreeTable.scrollRowToVisible(selectedRow);
    }

    private void stopTableEditing() {
        if (templateTreeTable.isEditing()) {
            templateTreeTable.getCellEditor().stopCellEditing();
        }
    }

    private class CodeChangeWorker extends SwingWorker<Void, Void> {

        private String selectedId;

        public CodeChangeWorker(String selectedId) {
            this.selectedId = selectedId;
        }

        @Override
        protected Void doInBackground() throws Exception {
            Thread.sleep(100);
            return null;
        }

        @Override
        protected void done() {
            try {
                get();

                int selectedRow = templateTreeTable.getSelectedRow();
                if (selectedRow >= 0) {
                    TreePath selectedPath = templateTreeTable.getPathForRow(selectedRow);
                    if (selectedPath != null) {
                        TreeTableNode selectedNode = (TreeTableNode) selectedPath.getLastPathComponent();
                        if (selectedId.equals(selectedNode.getValueAt(TEMPLATE_ID_COLUMN))) {
                            CodeTemplateTreeTableModel model = (CodeTemplateTreeTableModel) templateTreeTable.getTreeTableModel();

                            if (selectedNode instanceof CodeTemplateLibraryTreeTableNode) {
                                CodeTemplateLibraryTreeTableNode libraryNode = (CodeTemplateLibraryTreeTableNode) selectedNode;
                                model.setValueAt(libraryDescriptionScrollPane.getText(), libraryNode, TEMPLATE_DESCRIPTION_COLUMN);
                                fullModel.setValueAt(libraryDescriptionScrollPane.getText(), findFullNode(libraryNode), TEMPLATE_DESCRIPTION_COLUMN);
                            } else if (selectedNode instanceof CodeTemplateTreeTableNode) {
                                CodeTemplateTreeTableNode codeTemplateNode = (CodeTemplateTreeTableNode) selectedNode;
                                codeTemplateNode.getCodeTemplate().setCode(templateCodeTextArea.getText());
                                model.setValueAt(codeTemplateNode.getCodeTemplate().getDescription(), codeTemplateNode, TEMPLATE_DESCRIPTION_COLUMN);

                                CodeTemplateTreeTableNode fullCodeTemplateNode = (CodeTemplateTreeTableNode) findFullNode(codeTemplateNode);
                                fullCodeTemplateNode.getCodeTemplate().setCode(templateCodeTextArea.getText());
                                fullModel.setValueAt(fullCodeTemplateNode.getCodeTemplate().getDescription(), fullCodeTemplateNode, TEMPLATE_DESCRIPTION_COLUMN);

                                if (StringUtils.startsWith(StringUtils.trim(codeTemplateNode.getCodeTemplate().getCode()), "/**")) {
                                    templateAutoGenerateDocumentationButton.setText("Update JSDoc");
                                } else {
                                    templateAutoGenerateDocumentationButton.setText("Generate JSDoc");
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private void updateTemplateFilter(String filter) {
        stopTableEditing();
        updateCurrentNode();
        updateCurrentNode.set(false);

        CodeTemplateTreeTableModel model = (CodeTemplateTreeTableModel) templateTreeTable.getTreeTableModel();
        CodeTemplateRootTreeTableNode root = (CodeTemplateRootTreeTableNode) fullModel.getRoot();

        if (StringUtils.isNotBlank(filter)) {
            root = getFilteredRootNode(root);
        }

        model.setRoot(root);
        model.sort();
        updateFilterNotification();

        templateTreeTable.expandAll();
        templateTreeTable.clearSelection();
        switchSplitPaneComponent(blankPanel);
        updateCurrentNode.set(true);
    }

    private void updateFilterNotification() {
        int totalLibraries = 0;
        int totalCodeTemplates = 0;
        for (Enumeration<? extends MutableTreeTableNode> libraryNodes = ((MutableTreeTableNode) fullModel.getRoot()).children(); libraryNodes.hasMoreElements();) {
            totalLibraries++;
            totalCodeTemplates += libraryNodes.nextElement().getChildCount();
        }

        int tableLibraries = 0;
        int tableCodeTemplates = 0;
        for (Enumeration<? extends MutableTreeTableNode> libraryNodes = ((MutableTreeTableNode) templateTreeTable.getTreeTableModel().getRoot()).children(); libraryNodes.hasMoreElements();) {
            tableLibraries++;
            tableCodeTemplates += libraryNodes.nextElement().getChildCount();
        }

        StringBuilder builder = new StringBuilder();
        if (totalLibraries == tableLibraries) {
            builder.append(String.valueOf(tableLibraries)).append(" Librar");
            if (tableLibraries == 1) {
                builder.append('y');
            } else {
                builder.append("ies");
            }
            builder.append(", ");
        } else {
            builder.append(String.valueOf(tableLibraries)).append(" of ").append(String.valueOf(totalLibraries)).append(" Librar");
            if (totalLibraries == 1) {
                builder.append('y');
            } else {
                builder.append("ies");
            }
            builder.append(" (").append(String.valueOf(totalLibraries - tableLibraries)).append(" filtered), ");
        }
        if (totalCodeTemplates == tableCodeTemplates) {
            builder.append(String.valueOf(tableCodeTemplates)).append(" Code Template");
            if (tableCodeTemplates != 1) {
                builder.append('s');
            }
        } else {
            builder.append(String.valueOf(tableCodeTemplates)).append(" of ").append(String.valueOf(totalCodeTemplates)).append(" Code Template");
            if (totalCodeTemplates != 1) {
                builder.append('s');
            }
            builder.append(" (").append(String.valueOf(totalCodeTemplates - tableCodeTemplates)).append(" filtered)");
        }

        templateFilterNotificationLabel.setText(builder.toString());
    }

    private void printTreeTable() {
        if (logger.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder();
            for (Enumeration<? extends MutableTreeTableNode> children = ((MutableTreeTableNode) templateTreeTable.getTreeTableModel().getRoot()).children(); children.hasMoreElements();) {
                printTreeTable(children.nextElement(), builder, 0);
            }
            logger.debug(builder.toString());

            if (templateTreeTable.getTreeTableModel().getRoot() != fullModel.getRoot()) {
                builder = new StringBuilder();
                for (Enumeration<? extends MutableTreeTableNode> children = ((MutableTreeTableNode) fullModel.getRoot()).children(); children.hasMoreElements();) {
                    printTreeTable(children.nextElement(), builder, 0);
                }
                logger.debug(builder.toString());
            }
        }
    }

    private void printTreeTable(MutableTreeTableNode node, StringBuilder builder, int depth) {
        builder.append(StringUtils.repeat('\t', depth));
        if (node instanceof CodeTemplateLibraryTreeTableNode) {
            CodeTemplateLibraryTreeTableNode libraryNode = (CodeTemplateLibraryTreeTableNode) node;
            builder.append(libraryNode.getLibrary().getName()).append("\t\t\t\t").append(libraryNode.getLibrary().getDescription().replaceAll("\r\n|\r|\n", " "));
        } else if (node instanceof CodeTemplateTreeTableNode) {
            CodeTemplateTreeTableNode codeTemplateNode = (CodeTemplateTreeTableNode) node;
            builder.append(codeTemplateNode.getCodeTemplate().getName()).append("\t\t\t\t").append(StringUtils.defaultString(codeTemplateNode.getCodeTemplate().getDescription()).replaceAll("\r\n|\r|\n", " "));
        }
        builder.append('\n');

        for (Enumeration<? extends MutableTreeTableNode> children = node.children(); children.hasMoreElements();) {
            printTreeTable(children.nextElement(), builder, depth + 1);
        }
    }

    private JXTaskPane codeTemplateTasks;
    private JPopupMenu codeTemplatePopupMenu;

    private JSplitPane splitPane;
    private JPanel topPanel;
    private MirthTreeTable templateTreeTable;
    private JScrollPane templateTreeTableScrollPane;
    private JLabel templateFilterNotificationLabel;
    private JLabel templateFilterLabel;
    private JTextField templateFilterField;
    private JPanel blankPanel;

    // Library Panel
    private JPanel libraryPanel;

    private JPanel libraryLeftPanel;
    private JLabel librarySummaryLabel;
    private JLabel librarySummaryValue;
    private JLabel libraryDescriptionLabel;
    private MirthRTextScrollPane libraryDescriptionScrollPane;

    private JPanel libraryRightPanel;
    private JPanel libraryChannelsSelectPanel;
    private JLabel libraryChannelsLabel;
    private JLabel libraryChannelsSelectAllLabel;
    private JLabel libraryChannelsDeselectAllLabel;
    private JLabel libraryChannelsFilterLabel;
    private JTextField libraryChannelsFilterField;
    private MirthTable libraryChannelsTable;
    private JScrollPane libraryChannelsScrollPane;

    // Template Panel
    private JPanel templatePanel;

    private JPanel templateLeftPanel;
    private JLabel templateLibraryLabel;
    private JComboBox<String> templateLibraryComboBox;
    private JLabel templateTypeLabel;
    private JComboBox<CodeTemplateType> templateTypeComboBox;
    private JLabel templateCodeLabel;
    private MirthRTextScrollPane templateCodeTextArea;
    private JButton templateAutoGenerateDocumentationButton;

    private JPanel templateRightPanel;
    private JPanel templateContextSelectPanel;
    private JLabel templateContextLabel;
    private JLabel templateContextSelectAllLabel;
    private JLabel templateContextDeselectAllLabel;
    private MirthTreeTable templateContextTreeTable;
    private JScrollPane templateContextTreeTableScrollPane;
}