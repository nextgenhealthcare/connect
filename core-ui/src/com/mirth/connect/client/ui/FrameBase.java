package com.mirth.connect.client.ui;

import java.awt.Component;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.syntax.jedit.JEditTextArea;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.browsers.message.MessageBrowserBase;
import com.mirth.connect.client.ui.codetemplate.CodeTemplatePanelBase;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.User;

public abstract class FrameBase extends JXFrame {
    
    public Client mirthClient;
    
    public abstract Client getClient();
    
    public abstract void setupFrame(Client mirthClient) throws ClientException;
    
    public abstract boolean logout(boolean quit);
    
    public abstract boolean logout(boolean quit, boolean confirmFirst);
    
    /**
     * A prompt to ask the user if he would like to save the changes made before leaving the page.
     */
    public abstract boolean confirmLeave();
    
    public abstract List<DashboardStatus> getCachedDashboardStatuses();
    
    public abstract List<User> getCachedUsers();
    
    public abstract User getCurrentUser(Component parentComponent);
    
    public abstract User getCurrentUser(Component parentComponent, boolean alertOnFailure);
    
    public abstract ChannelSetupBase getChannelSetup();
    
    public abstract ChannelPanelBase getChannelPanel();
    
    public abstract MessageBrowserBase getMessageBrowser();
    
    public abstract CodeTemplatePanelBase getCodeTemplatePanel();
    
    public abstract JPanel getTagsPanel();
    
    public abstract DashboardPanelBase getDashboardPanel();
    
    public abstract JXTaskPaneContainer getTaskPaneContainer();
    
    public abstract Component getCurrentContentPage();
    
    public abstract void setCurrentContentPage(Component contentPageObject);
    
    public abstract JXTaskPane getViewPane();
    
    public abstract JXTaskPane getOtherPane();
    
    public abstract void updateNotificationTaskName(int notifications);
    
    public abstract int addTask(String callbackMethod, String displayName, String toolTip, String shortcutKey, ImageIcon icon, JXTaskPane pane, JPopupMenu menu);
    
    /**
     * Initializes the bound method call for the task pane actions and adds them to the
     * taskpane/popupmenu.
     */
    public abstract int addTask(String callbackMethod, String displayName, String toolTip, String shortcutKey, ImageIcon icon, JXTaskPane pane, JPopupMenu menu, Object handler);

    public abstract Map<Component, String> getComponentTaskMap();

    public abstract void setBold(JXTaskPane pane, int index);
    
    public abstract void setFocus(JXTaskPane pane);
    
    public abstract void setFocus(JXTaskPane[] panes, boolean mirthPane, boolean otherPane);
    
    public abstract void setNonFocusable(JXTaskPane pane);
    
    public abstract void setVisibleTasks(JXTaskPane pane, JPopupMenu menu, int startIndex, int endIndex, boolean visible);
    
    public abstract Map<String, PluginMetaData> getPluginMetaData();
    
    public abstract Map<String, ConnectorMetaData> getConnectorMetaData();
    
    /**
     * Enables the save button for needed page.
     */
    public abstract boolean isSaveEnabled();
    
    public abstract void setSaveEnabled(boolean enabled);
    
    public abstract void setCanSave(boolean canSave);
    
    public abstract void doContextSensitiveSave();
    
    public abstract boolean changesHaveBeenMade();
    
    public abstract void setPanelName(String name);
    
    public abstract String startWorking(final String displayText);
    
    public abstract void stopWorking(final String workingId);
    
    public abstract boolean checkOrUpdateUserPassword(Component parentComponent, final User currentUser, String newPassword);
    
    /**
     * Import a file with the default defined file filter type.
     * 
     * @return
     */
    public abstract String browseForFileString(String fileExtension);
    
    /**
     * Read the bytes from a file with the default defined file filter type.
     * 
     * @return
     */
    public abstract byte[] browseForFileBytes(String fileExtension);
    
    public abstract String readFileToString(File file);
    
    public abstract File browseForFile(String fileExtension);
    
    public abstract File[] browseForFiles(String fileExtension);
    
    /**
     * Creates a File with the default defined file filter type, but does not yet write to it.
     * 
     * @param defaultFileName
     * @param fileExtension
     * @return
     */
    public abstract File createFileForExport(String defaultFileName, String fileExtension);
    
    /**
     * Export a file with the default defined file filter type.
     * 
     * @param fileContents
     * @param fileName
     * @return
     */
    public abstract boolean exportFile(String fileContents, String defaultFileName, String fileExtension, String name);

    public abstract boolean exportFile(String fileContents, File exportFile, String name);
    
    /**
     * Alerts the user with a yes/no option with the passed in 'message'
     */
    public abstract boolean alertOption(Component parentComponent, String message);
    
    /**
     * Alerts the user with a Ok/cancel option with the passed in 'message'
     */
    public abstract boolean alertOkCancel(Component parentComponent, String message);
    
    public enum ConflictOption {
        YES, YES_APPLY_ALL, NO, NO_APPLY_ALL;
    }
    
    /**
     * Alerts the user with a conflict resolution dialog
     */
    public abstract ConflictOption alertConflict(Component parentComponent, String message, int count);
    
    public abstract boolean alertRefresh();
    
    /**
     * Alerts the user with an information dialog with the passed in 'message'
     */
    public abstract void alertInformation(Component parentComponent, String message);
    
    /**
     * Alerts the user with a warning dialog with the passed in 'message'
     */
    public abstract void alertWarning(Component parentComponent, String message);
    
    /**
     * Alerts the user with an error dialog with the passed in 'message'
     */
    public abstract void alertError(Component parentComponent, String message);
    
    /**
     * Alerts the user with an error dialog with the passed in 'message' and a 'question'.
     */
    public abstract void alertCustomError(Component parentComponent, String message, String question);
    
    /**
     * Alerts the user with an exception dialog with the passed in stack trace.
     */
    public abstract void alertThrowable(Component parentComponent, Throwable t);
    
    /**
     * Alerts the user with an exception dialog with the passed in stack trace.
     */
    public abstract void alertThrowable(Component parentComponent, Throwable t, String customMessage);
    
    /**
     * Alerts the user with an exception dialog with the passed in stack trace.
     */
    public abstract void alertThrowable(Component parentComponent, Throwable t, boolean showMessageOnForbidden);
    
    /**
     * Alerts the user with an exception dialog with the passed in stack trace.
     */
    public abstract void alertThrowable(Component parentComponent, Throwable t, String customMessage, boolean showMessageOnForbidden);
    
    /**
     * Alerts the user with an exception dialog with the passed in stack trace.
     */
    public abstract void alertThrowable(Component parentComponent, Throwable t, String customMessage, String safeErrorKey);
    
    /**
     * Alerts the user with an exception dialog with the passed in stack trace.
     */
    public abstract void alertThrowable(Component parentComponent, Throwable t, String customMessage, boolean showMessageOnForbidden, String safeErrorKey);
    
    public abstract void doFind(JEditTextArea text);
    
    public abstract boolean isMultiChannelMessageBrowsingEnabled();
    
    public abstract Set<ChannelTag> getCachedChannelTags();
    
    public abstract Map<String, String> getDataTypeToDisplayNameMap();
    
    public abstract Map<String, String> getDisplayNameToDataTypeMap();
    
    public abstract void setupCharsetEncodingForConnector(JComboBox<?> charsetEncodingCombobox);
    
    /**
     * Creates all the items in the combo box for the connectors.
     * 
     * This method is called from each connector.
     */
    public abstract void setupCharsetEncodingForConnector(JComboBox<?> charsetEncodingCombobox, boolean allowNone);
    
    /**
     * Get the strings which identifies the encoding selected by the user.
     * 
     * This method is called from each connector.
     */
    public abstract String getSelectedEncodingForConnector(JComboBox<?> charsetEncodingCombobox);
    
    public abstract void setPreviousSelectedEncodingForConnector(JComboBox<?> charsetEncodingCombobox, String selectedCharset);

    /**
     * Sets the combobox for the string previously selected. If the server can't support the
     * encoding, the default one is selected. This method is called from each connector.
     */
    public abstract void setPreviousSelectedEncodingForConnector(JComboBox<?> charsetEncodingCombobox, String selectedCharset, boolean allowNone);
    
    public abstract void doShowEvents(String eventNameFilter);
}
