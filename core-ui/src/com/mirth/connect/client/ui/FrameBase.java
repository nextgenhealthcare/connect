package com.mirth.connect.client.ui;

import java.awt.Component;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXTaskPane;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.client.ui.FrameBase.ConflictOption;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.PluginMetaData;

public abstract class FrameBase extends JXFrame {
    
    public abstract Client getClient();
    
    public abstract JXTaskPane getOtherPane();
    
    public abstract void updateNotificationTaskName(int notifications);
    
    public abstract int addTask(String callbackMethod, String displayName, String toolTip, String shortcutKey, ImageIcon icon, JXTaskPane pane, JPopupMenu menu);
    
    /**
     * Initializes the bound method call for the task pane actions and adds them to the
     * taskpane/popupmenu.
     */
    public abstract int addTask(String callbackMethod, String displayName, String toolTip, String shortcutKey, ImageIcon icon, JXTaskPane pane, JPopupMenu menu, Object handler);

    public abstract Map<Component, String> getComponentTaskMap();
    
    public abstract void setVisibleTasks(JXTaskPane pane, JPopupMenu menu, int startIndex, int endIndex, boolean visible);
    
    public abstract Map<String, PluginMetaData> getPluginMetaData();
    
    public abstract Map<String, ConnectorMetaData> getConnectorMetaData();
    
    public abstract void setSaveEnabled(boolean enabled);
    
    public abstract void setCanSave(boolean canSave);
    
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
    
    
}
