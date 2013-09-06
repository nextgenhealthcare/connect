/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.panels.export;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthButton;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.components.MirthTextPane;
import com.mirth.connect.client.ui.components.MirthVariableList;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class MessageExportPanel extends JPanel {
    private final static String ARCHIVER_MODE_PATTERN = "[timestamp]/[channel id]";
    private final static String EXPORT_MODE_PATTERN = "[timestamp]";
    private final static String XML_EXPORT_FORMAT = "XML serialized message";
    private final static String NO_COMPRESSION = "none";

    private Preferences userPreferences;
    private boolean archiverMode;
    private boolean initialized;
    private Component[] archiveComponents;
    
    private MirthVariableList varList = new MirthVariableList();
    private JScrollPane varListScrollPane = new JScrollPane();
    private JPanel varListPanel = new JPanel();
    
    private ButtonGroup archiveButtonGroup = new ButtonGroup();
    private JLabel archiveLabel = new JLabel("Enable Archiving:");
    private JRadioButton archiveYes = new MirthRadioButton("Yes");
    private JRadioButton archiveNo = new MirthRadioButton("No");
    private JLabel contentLabel = new JLabel("Content:");
    private JComboBox contentComboBox = new MirthComboBox();
    private JCheckBox encryptCheckBox = new MirthCheckBox("Encrypt");
    private JLabel attachmentsLabel = new JLabel();
//    private JCheckBox attachmentsCheckBox = new MirthCheckBox("Include Attachments");
    private JLabel compressLabel = new JLabel("Compression:");
    private JComboBox compressComboBox = new MirthComboBox();
    private JLabel exportToLabel = new JLabel("Export To:");
    private ButtonGroup exportButtonGroup = new ButtonGroup();
    private JRadioButton exportServerRadio = new MirthRadioButton("Server");
    private JRadioButton exportLocalRadio = new MirthRadioButton("My Computer");
    private JButton browseButton = new MirthButton("Browse...");
    private JLabel rootPathLabel = new JLabel("Root Path:");
    private JTextField rootPathTextField = new MirthTextField();
    private JLabel filePatternLabel = new JLabel("File Pattern:");
    private JScrollPane filePatternScrollPane = new JScrollPane();
    private JTextPane filePatternTextPane = new MirthTextPane();
    private JLabel rootPathExtLabel = new JLabel();

    /**
     * Construct a message export panel.
     * 
     * @param parent
     *            The parent swing component.
     * @param userPreferences
     *            User preferences to store/retrieve the last browsed directory on the local
     *            file-system
     * @param archiver
     *            If true, enables the Archive yes/no radio buttons that enable/disable all the
     *            other components
     * @param allowLocalExport
     *            If true, enables components that allow the user to select a local file-system
     *            folder to export to
     */
    public MessageExportPanel(Preferences userPreferences, boolean archiverMode, boolean allowLocalExport) {
        this.userPreferences = userPreferences;
        this.archiverMode = archiverMode;
        
        initComponents();
        initLayout(allowLocalExport);
        initialized = true;
    }

    public boolean isArchiveEnabled() {
        return archiveYes.isSelected();
    }

    public void setArchiveEnabled(boolean archiveEnabled) {
        if (archiveEnabled) {
            archiveYes.setSelected(true);
            archiveNo.setSelected(false);
        } else {
            archiveYes.setSelected(false);
            archiveNo.setSelected(true);
        }

        archiveChanged();
    }

    public boolean isIncludeAttachments() {
        return false;
//        return attachmentsCheckBox.isSelected();
    }

    public void setIncludeAttachments(boolean includeAttachments) {
//        attachmentsCheckBox.setSelected(includeAttachments);
    }

    public boolean isExportLocal() {
        return exportLocalRadio.isSelected();
    }

    public void setExportLocal(boolean exportLocal) {
        exportLocalRadio.setSelected(exportLocal);
        exportServerRadio.setSelected(!exportLocal);
        exportDestinationChanged();
    }

    public MessageWriterOptions getMessageWriterOptions() {
        MessageWriterOptions options = new MessageWriterOptions();

        if (contentComboBox.getSelectedItem() instanceof ExportFormat) {
            ExportFormat exportFormat = (ExportFormat) contentComboBox.getSelectedItem();
            options.setContentType(exportFormat.getContentType());
            options.setDestinationContent(exportFormat.isDestination());
        }

        options.setEncrypt(encryptCheckBox.isSelected());

        if (compressComboBox.getSelectedItem() instanceof ArchiveFormat) {
            ArchiveFormat archiveFormat = (ArchiveFormat) compressComboBox.getSelectedItem();
            options.setArchiveFormat(archiveFormat.getArchiver());
            options.setCompressFormat(archiveFormat.getCompressor());
        }

        options.setRootFolder(rootPathTextField.getText());
        options.setFilePattern(filePatternTextPane.getText());

        return options;
    }

    public void setMessageWriterOptions(MessageWriterOptions options) {
        if (options.getContentType() == null) {
            contentComboBox.setSelectedItem(XML_EXPORT_FORMAT);
        } else {
            DefaultComboBoxModel model = (DefaultComboBoxModel) contentComboBox.getModel();

            for (int i = 0; i < model.getSize(); i++) {
                Object element = model.getElementAt(i);

                if (element instanceof ExportFormat) {
                    ExportFormat exportFormat = (ExportFormat) element;

                    if (exportFormat.getContentType().equals(options.getContentType()) && exportFormat.isDestination() == options.isDestinationContent()) {
                        contentComboBox.setSelectedItem(exportFormat);
                    }
                }
            }
        }

        encryptCheckBox.setSelected(options.isEncrypt());

        ArchiveFormat archiveFormat = ArchiveFormat.lookup(options.getArchiveFormat(), options.getCompressFormat());

        if (archiveFormat == null) {
            compressComboBox.setSelectedItem(NO_COMPRESSION);
        } else {
            compressComboBox.setSelectedItem(archiveFormat);
        }

        compressComboBoxChanged();

        rootPathTextField.setText(options.getRootFolder());
        filePatternTextPane.setText(options.getFilePattern());

        repaint();
    }

    /**
     * Overrides JPanel.setBackground() so that it also sets the background for the radio and
     * checkbox components in the panel
     */
    @Override
    public void setBackground(Color color) {
        super.setBackground(color);

        if (initialized) {
            archiveYes.setBackground(color);
            archiveNo.setBackground(color);
            encryptCheckBox.setBackground(color);
//            attachmentsCheckBox.setBackground(color);
            exportServerRadio.setBackground(color);
            exportLocalRadio.setBackground(color);
        }
    }

    public boolean validate(boolean highlight) {
        resetInvalidProperties();
        
        if (!isEnabled() || archiveNo.isSelected()) {
            return true;
        }

        boolean valid = true;
        
        if (StringUtils.isBlank(rootPathTextField.getText())) {
            valid = false;

            if (highlight) {
                rootPathTextField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        
        if (StringUtils.isBlank(filePatternTextPane.getText())) {
            valid = false;

            if (highlight) {
                filePatternTextPane.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    public void resetInvalidProperties() {
        rootPathTextField.setBackground(getBackground());
        filePatternTextPane.setBackground(getBackground());
    }

    private void initComponents() {
        contentComboBox.setToolTipText("<html>The content that will be exported: Either the entire message serialized into XML, or a specific content type<br />from either the source connector message or the destination connector messages.</html>");
        encryptCheckBox.setToolTipText("<html>If checked, the exported message content will be encrypted.</html>");
        compressComboBox.setToolTipText("<html>When compression is enabled, the files/folders created according to the<br />File Pattern will be put into a compressed file in the Root Path.</html>");
        exportServerRadio.setToolTipText("<html>Store exported files on the Mirth Connect Server, in the Root Path specified below.</html>");
        exportLocalRadio.setToolTipText("<html>Store exported files on this computer, in the Root Path specified below.</html>");
        rootPathTextField.setToolTipText("<html>The root path to store the exported files/folders or compressed file.</html>");
        filePatternTextPane.setToolTipText("<html>The file/folder pattern in which to write the exported message files.<br />Variables from the Variables list to the right may be used in the pattern.</html>");
        
        attachmentsLabel.setText("<html><i>Note: attachments will not be included with " + (archiverMode ? "archived" : "exported") + " messages</i></html>");

        archiveYes = new MirthRadioButton("Yes");
        archiveNo = new MirthRadioButton("No");

        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(XML_EXPORT_FORMAT);
        model.addElement(new ExportFormat(false, ContentType.RAW));
        model.addElement(new ExportFormat(false, ContentType.PROCESSED_RAW));
        model.addElement(new ExportFormat(false, ContentType.TRANSFORMED));
        model.addElement(new ExportFormat(false, ContentType.ENCODED));
        model.addElement(new ExportFormat(false, ContentType.RESPONSE));
        model.addElement(new ExportFormat(true, ContentType.TRANSFORMED));
        model.addElement(new ExportFormat(true, ContentType.ENCODED));
        model.addElement(new ExportFormat(true, ContentType.RESPONSE));
        model.addElement(new ExportFormat(true, ContentType.PROCESSED_RESPONSE));
        contentComboBox.setModel(model);

        model = new DefaultComboBoxModel();
        model.addElement(NO_COMPRESSION);

        for (ArchiveFormat archiveFormat : ArchiveFormat.values()) {
            model.addElement(archiveFormat);
        }

        compressComboBox.setModel(model);

        ArrayList<String> variables = new ArrayList<String>();
        variables.add("Message ID");
        variables.add("Server ID");
        variables.add("Channel ID");
        variables.add("Original File Name");
        variables.add("Formatted Message Date");
        variables.add("Formatted Current Date");
        variables.add("Timestamp");
        variables.add("Unique ID");
        variables.add("Count");
        
        varListScrollPane.setBackground(UIConstants.BACKGROUND_COLOR);
        varListPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        varListPanel.setBorder(BorderFactory.createEmptyBorder());
        varListPanel.setLayout(new BorderLayout());
        varListPanel.add(varListScrollPane);
        varListScrollPane.setViewportView(varList);
        varList.setListData(variables.toArray());

        archiveButtonGroup.add(archiveYes);
        archiveButtonGroup.add(archiveNo);
        archiveYes.setSelected(true);
        
        if (archiverMode) {
            rootPathExtLabel.setText("/" + ARCHIVER_MODE_PATTERN + "/");
        } else {
            rootPathExtLabel.setVisible(false);
        }

        exportButtonGroup.add(exportServerRadio);
        exportButtonGroup.add(exportLocalRadio);
        exportServerRadio.setSelected(true);
        browseButton.setEnabled(false);

        filePatternTextPane.setText("message_${message.messageId}.xml");
        filePatternScrollPane.setViewportView(filePatternTextPane);

        // this is the list of components that will be disabled when the archive radio "No" is selected, see archiveChanged()
        archiveComponents = new Component[] { contentLabel, contentComboBox, encryptCheckBox,
                varList, varListScrollPane, varListPanel, compressLabel, compressComboBox, exportToLabel, exportServerRadio,
                exportLocalRadio, browseButton, rootPathLabel, rootPathTextField, rootPathExtLabel,
                filePatternLabel, filePatternScrollPane, filePatternTextPane, attachmentsLabel };

        // @formatter:off
        archiveYes.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { archiveChanged(); }
        });
        
        archiveNo.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { archiveChanged(); }
        });
        
        compressComboBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { compressComboBoxChanged(); }
        });

        browseButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { browseSelected(); }
        });

        exportServerRadio.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { exportDestinationChanged(); }
        });

        exportLocalRadio.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { exportDestinationChanged(); }
        });
        // @formatter:on
    }

    private void browseSelected() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (userPreferences != null) {
            File currentDir = new File(userPreferences.get("currentDirectory", ""));

            if (currentDir.exists()) {
                chooser.setCurrentDirectory(currentDir);
            }
        }

        if (chooser.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
            if (userPreferences != null) {
                userPreferences.put("currentDirectory", chooser.getCurrentDirectory().getPath());
            }

            rootPathTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void archiveChanged() {
        if (archiveYes.isSelected()) {
            for (Component component : archiveComponents) {
                component.setEnabled(true);
            }
        } else {
            resetInvalidProperties();
            
            for (Component component : archiveComponents) {
                component.setEnabled(false);
            }
        }
    }

    private void exportDestinationChanged() {
        if (exportServerRadio.isSelected()) {
            rootPathTextField.setText(null);
            browseButton.setEnabled(false);
        } else {
            rootPathTextField.setText(null);
            browseButton.setEnabled(true);
        }
    }

    private void compressComboBoxChanged() {
        if (compressComboBox.getSelectedItem() instanceof ArchiveFormat) {
            ArchiveFormat archiveFormat = (ArchiveFormat) compressComboBox.getSelectedItem();
            rootPathExtLabel.setText("/" + ((archiverMode) ? ARCHIVER_MODE_PATTERN : EXPORT_MODE_PATTERN) + "." + archiveFormat);
            rootPathExtLabel.setVisible(true);
        } else if (archiverMode) {
            rootPathExtLabel.setText("/" + ARCHIVER_MODE_PATTERN + "/");
        } else {
            rootPathExtLabel.setVisible(false);
        }
    }

    private void initLayout(boolean allowLocalExport) {
        String rowGap = "2"; // TODO find a better way in mig layout to set the row gap for all rows, given that # of rows can change

        setLayout(new MigLayout("insets 0 0 0 0, wrap, fillx, hidemode 3", "[right]12[left, grow][170!]", ""));

        if (archiverMode) {
            add(archiveLabel);
            add(archiveYes, "split 2");
            add(archiveNo, "gapbottom " + rowGap);

            add(varListPanel, "spany 5, growy, width 170!");
        }

        add(contentLabel);
        add(contentComboBox, "split 2, gapbottom " + rowGap);
        add(encryptCheckBox, "gapleft 8");
//        add(attachmentsCheckBox, "gapleft 8");

        if (!archiverMode) {
            add(varListPanel, "spany 5, growy, width 170!");
        }

        add(compressLabel);
        add(compressComboBox, "gapbottom " + rowGap);

        if (allowLocalExport) {
            add(exportToLabel);
            add(exportServerRadio, "split 3");
            add(exportLocalRadio);
            add(browseButton, "gapbottom " + rowGap);
        }

        add(rootPathLabel);
        add(rootPathTextField, "grow, split 2, height 22!, gapbottom " + rowGap);
        add(rootPathExtLabel, "hidemode 2");

        add(filePatternLabel, "aligny top");
        add(filePatternScrollPane, "grow, push, split 2");
        add(new JLabel(), "gapbottom " + rowGap);
        
        add(new JLabel());
        add(attachmentsLabel);
    }

    /**
     * Launches the panel in a test frame
     */
//    public static void main(String[] args) {
//        Mirth.initUIManager();
//        PlatformUI.MIRTH_FRAME = new Frame() {
//            public void setSaveEnabled(boolean enabled) {}
//        };
//
//        final JFrame frame = new JFrame();
//
//        MessageExportPanel panel = new MessageExportPanel(null, false, true);
//        panel.setBackground(new Color(255, 255, 255));
//
//        frame.setSize(800, 300);
//        frame.getContentPane().setBackground(new Color(255, 255, 255));
//        frame.setLayout(new MigLayout("fill, insets dialog", "", ""));
//        frame.add(panel, "grow");
//        frame.setLocationRelativeTo(null);
//        frame.setVisible(true);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.getRootPane().registerKeyboardAction(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent event) {
//                frame.dispose();
//            }
//        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
//    }
}
