package com.mirth.connect.client.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.util.NotificationUtil;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.notification.Notification;

public class NotificationDialog extends MirthDialog {

    private NotificationModel notificationModel = new NotificationModel();
    private Notification currentNotification;
    private String checkForNotifications = null;
    private String showNotificationPopup = null;
    private boolean checkForNotificationsSetting = false;
    private int unarchivedCount = 0;
    private Set<Integer> archivedNotifications = new HashSet<Integer>();
    Properties userPreferences = new Properties();
    private Color borderColor = new Color(110, 110, 110);

    public NotificationDialog() {
        super(PlatformUI.MIRTH_FRAME, true);
        parent = PlatformUI.MIRTH_FRAME;

        setResizable(false);
        setTitle("Notifications");
        setPreferredSize(new Dimension(750, 625));
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();

        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
            setLocationRelativeTo(null);
        } else {
            setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }
        loadNotifications();
        initLayout();
        pack();
        setVisible(true);
    }

    private void loadNotifications() {
        // Get user preferences
        Set<String> preferenceNames = new HashSet<String>();
        preferenceNames.add("firstlogin");
        preferenceNames.add("checkForNotifications");
        preferenceNames.add("showNotificationPopup");
        preferenceNames.add("archivedNotifications");
        try {
            userPreferences = parent.mirthClient.getUserPreferences(parent.getCurrentUser(parent), preferenceNames);
        } catch (ClientException e) {
            //TODO: Do something about this?
        }
        String archivedNotificationString = userPreferences.getProperty("archivedNotifications");
        if (archivedNotificationString != null) {
            archivedNotifications = ObjectXMLSerializer.getInstance().deserialize(archivedNotificationString, Set.class);
        }
        showNotificationPopup = userPreferences.getProperty("showNotificationPopup");
        checkForNotifications = userPreferences.getProperty("checkForNotifications");

        // Build UI
        initComponents();

        // Pull notifications
        final String workingId = parent.startWorking("Loading notifications...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            List<Notification> notifications = new ArrayList<Notification>();

            public Void doInBackground() {
                notifications = NotificationUtil.getNotifications();
                return null;
            }

            public void done() {
                notificationModel.setData(notifications);

                for (Notification notification : notifications) {
                    if (archivedNotifications.contains(notification.getId())) {
                        notificationModel.setArchived(true, notifications.indexOf(notification));
                    } else {
                        unarchivedCount++;
                    }
                }
                updateUnarchivedCountLabel();
                list.setModel(notificationModel);
                list.setSelectedIndex(0);
                parent.stopWorking(workingId);
            }
        };

        worker.execute();
    }

    private void initComponents() {
        setLayout(new MigLayout("insets 12", "[]", "[fill][]"));

        notificationPanel = new JPanel();
        notificationPanel.setLayout(new MigLayout("insets 0 0 0 0, fill", "[200!][]", "[25!]0[]"));
        notificationPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        archiveAll = new JLabel("Archive All");
        archiveAll.setForeground(java.awt.Color.blue);
        archiveAll.setText("<html><u>Archive All</u></html>");
        archiveAll.setToolTipText("Archive all notifications below.");
        archiveAll.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        newNotificationsLabel = new JLabel();
        newNotificationsLabel.setFont(newNotificationsLabel.getFont().deriveFont(Font.BOLD));
        headerListPanel = new JPanel();
        headerListPanel.setBackground(UIConstants.HIGHLIGHTER_COLOR);
        headerListPanel.setLayout(new MigLayout("insets 2, fill"));
        headerListPanel.setBorder(BorderFactory.createLineBorder(borderColor));

        list = new JList();
        list.setCellRenderer(new NotificationListCellRenderer());
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    currentNotification = (Notification) list.getSelectedValue();
                    if (currentNotification != null) {
                        notificationNameTextField.setText(currentNotification.getName());
                        contentTextPane.setText(currentNotification.getContent());
                        archiveSelected();
                    }
                }
            }
        });
        listScrollPane = new JScrollPane();
        listScrollPane.setBackground(UIConstants.BACKGROUND_COLOR);
        listScrollPane.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, borderColor));
        listScrollPane.setViewportView(list);
        listScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        archiveLabel = new JLabel();
        archiveLabel.setForeground(java.awt.Color.blue);
        archiveLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        notificationNameTextField = new JTextField();
        notificationNameTextField.setFont(notificationNameTextField.getFont().deriveFont(Font.BOLD));
        notificationNameTextField.setEditable(false);
        notificationNameTextField.setBorder(BorderFactory.createEmptyBorder());
        notificationNameTextField.setBackground(UIConstants.HIGHLIGHTER_COLOR);
        DefaultCaret nameCaret = (DefaultCaret) notificationNameTextField.getCaret();
        nameCaret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        headerContentPanel = new JPanel();
        headerContentPanel.setLayout(new MigLayout("insets 2, fill"));
        headerContentPanel.setBorder(BorderFactory.createLineBorder(borderColor));
        headerContentPanel.setBackground(UIConstants.HIGHLIGHTER_COLOR);

        contentTextPane = new JTextPane();
        contentTextPane.setContentType("text/html");
        contentTextPane.setEditable(false);
        contentTextPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent evt) {
                if (evt.getEventType() == EventType.ACTIVATED && Desktop.isDesktopSupported()) {
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(evt.getURL().toURI());
                        } else {
                            BareBonesBrowserLaunch.openURL(evt.getURL().toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        DefaultCaret contentCaret = (DefaultCaret) contentTextPane.getCaret();
        contentCaret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        contentScrollPane = new JScrollPane();
        contentScrollPane.setViewportView(contentTextPane);
        contentScrollPane.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, borderColor));

        archiveLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int index = list.getSelectedIndex();
                if (currentNotification.isArchived()) {
                    notificationModel.setArchived(false, index);
                    unarchivedCount++;
                } else {
                    notificationModel.setArchived(true, index);
                    unarchivedCount--;
                }
                archiveSelected();
                updateUnarchivedCountLabel();
            }
        });

        archiveAll.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                for (int i = 0; i < notificationModel.getSize(); i++) {
                    notificationModel.setArchived(true, i);
                }
                unarchivedCount = 0;
                archiveSelected();
                updateUnarchivedCountLabel();
            }
        });

        notificationCheckBox = new JCheckBox("Show new notifications on login");
        notificationCheckBox.setBackground(UIConstants.BACKGROUND_COLOR);

        if (checkForNotifications == null || BooleanUtils.toBoolean(checkForNotifications)) {
            checkForNotificationsSetting = true;
            if (showNotificationPopup == null || BooleanUtils.toBoolean(showNotificationPopup)) {
                notificationCheckBox.setSelected(true);
            } else {
                notificationCheckBox.setSelected(false);
            }
        } else {
            notificationCheckBox.setSelected(false);
        }

        notificationCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (notificationCheckBox.isSelected() && !checkForNotificationsSetting) {
                    alertSettingsChange();
                }
            }
        });

        closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSave();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                doSave();
            }
        });
    }

    private void initLayout() {
        headerListPanel.add(newNotificationsLabel, "alignx left");
        headerListPanel.add(archiveAll, "alignx right");

        headerContentPanel.add(notificationNameTextField, "alignx left, growx, push");
        headerContentPanel.add(archiveLabel, "alignx right");

        notificationPanel.add(headerListPanel, "grow");
        notificationPanel.add(headerContentPanel, "wrap, grow");
        notificationPanel.add(listScrollPane, "grow");
        notificationPanel.add(contentScrollPane, "grow");

        add(notificationPanel, "grow, push, span");
        add(new JSeparator(), "grow, gaptop 4, span");
        add(notificationCheckBox, "alignx left");
        add(closeButton, "alignx right, width 60, spany 2");
    }

    private void updateUnarchivedCountLabel() {
        if (unarchivedCount == 0) {
            newNotificationsLabel.setText("");
        } else {
            newNotificationsLabel.setText(unarchivedCount + " new");
        }
    }

    private void archiveSelected() {
        if (currentNotification.isArchived()) {
            archiveLabel.setText("<html><u>Unarchive</u></html>");
            archiveLabel.setToolTipText("Unarchive this notification.");
        } else {
            archiveLabel.setText("<html><u>Archive</u></html>");
            archiveLabel.setToolTipText("Archive this notification.");
        }
    }

    private void alertSettingsChange() {
        boolean option = parent.alertOption(this, "<html>Selecting this option will enable checking for notifications on login.<br/>Are you sure you want to continue?</html>");
        if (option) {
            checkForNotificationsSetting = true;
        } else {
            notificationCheckBox.setSelected(false);
        }
    }

    private void doSave() {
        final Properties personPreferences = new Properties();

        if (!StringUtils.equals(checkForNotifications, Boolean.toString(checkForNotificationsSetting))) {
            personPreferences.put("checkForNotifications", Boolean.toString(checkForNotificationsSetting));
        }
        if (!StringUtils.equals(showNotificationPopup, Boolean.toString(notificationCheckBox.isSelected()))) {
            personPreferences.put("showNotificationPopup", Boolean.toString(notificationCheckBox.isSelected()));
        }

        Set<Integer> currentArchivedNotifications = notificationModel.getArchivedNotifications();
        if (!archivedNotifications.equals(currentArchivedNotifications)) {
            personPreferences.put("archivedNotifications", ObjectXMLSerializer.getInstance().serialize(currentArchivedNotifications));
        }

        if (!personPreferences.isEmpty()) {
            final String workingId = parent.startWorking("Saving notifications settings...");

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                public Void doInBackground() {
                    try {
                        parent.mirthClient.setUserPreferences(parent.getCurrentUser(parent), personPreferences);
                    } catch (ClientException e) {
                        parent.alertException(parent, e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                @Override
                public void done() {
                    parent.stopWorking(workingId);
                }
            };

            worker.execute();
        }

        parent.updateNotificationTaskName(unarchivedCount);

        this.dispose();
    }

    private class NotificationListCellRenderer extends DefaultListCellRenderer {
        private JPanel panel;
        private JLabel nameLabel;
        private JLabel dateLabel;
        
        public NotificationListCellRenderer() {
            nameLabel = new JLabel();
            
            dateLabel = new JLabel();
            dateLabel.setFont(list.getFont().deriveFont(10f));
            dateLabel.setForeground(Color.GRAY);
            
            panel = new JPanel();
            panel.setLayout(new MigLayout("insets 2, wrap"));
            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.HIGHLIGHTER_COLOR));
            panel.add(nameLabel);
            panel.add(dateLabel);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Notification notification = ((Notification) value);

            nameLabel.setText(notification.getName());
            dateLabel.setText(notification.getDate());

            if (notification.isArchived()) {
                nameLabel.setFont(list.getFont().deriveFont(Font.PLAIN));
            } else {
                nameLabel.setFont(list.getFont().deriveFont(Font.BOLD));
            }

            if (isSelected) {
                panel.setBackground(list.getSelectionBackground());
                panel.setForeground(list.getSelectionForeground());
            } else {
                panel.setBackground(list.getBackground());
                panel.setForeground(list.getForeground());
            }
            return panel;
        }
    }
    
    private class NotificationModel extends AbstractListModel {
        private List<Notification> notifications = new ArrayList<Notification>();

        @Override
        public Notification getElementAt(int index) {
            return notifications.get(index);
        }

        public void setArchived(boolean archived, int index) {
            getElementAt(index).setArchived(archived);
            fireContentsChanged(this, index, index);
        }

        public Set<Integer> getArchivedNotifications() {
            Set<Integer> archivedNotifications = new HashSet<Integer>();
            for (Notification notification : notifications) {
                if (notification.isArchived()) {
                    archivedNotifications.add(notification.getId());
                }
            }
            return archivedNotifications;
        }

        @Override
        public int getSize() {
            return notifications.size();
        }

        public void setData(List<Notification> notifications) {
            int size = getSize();
            this.notifications.clear();
            fireIntervalRemoved(this, 0, size - 1);

            this.notifications.addAll(notifications);
            fireIntervalAdded(this, 0, getSize() - 1);
        }
    }


    private Frame parent;
    private JPanel notificationPanel;

    // List header panel
    private JPanel headerListPanel;
    private JLabel newNotificationsLabel;
    private JLabel archiveAll;

    // List panel
    private JList list;
    private JScrollPane listScrollPane;

    // Content header panel
    private JPanel headerContentPanel;
    private JTextField notificationNameTextField;
    private JLabel archiveLabel;

    // Content panel
    private JTextPane contentTextPane;
    private JScrollPane contentScrollPane;

    // Dialog footer
    private JCheckBox notificationCheckBox;
    private JButton closeButton;
}
