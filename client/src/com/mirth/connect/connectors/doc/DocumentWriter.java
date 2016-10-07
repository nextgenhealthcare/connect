/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.doc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthPasswordField;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.panels.connectors.ResponseHandler;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.util.ConnectionTestResponse;

public class DocumentWriter extends ConnectorSettingsPanel {

    private Frame parent;
    private boolean pageSizeUpdating;

    public DocumentWriter() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        initLayout();
    }

    @Override
    public String getConnectorName() {
        return new DocumentDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        DocumentDispatcherProperties properties = new DocumentDispatcherProperties();

        properties.setHost(directoryField.getText().replace('\\', '/'));
        properties.setOutputPattern(fileNameField.getText());

        if (documentTypePDFRadio.isSelected()) {
            properties.setDocumentType(DocumentDispatcherProperties.DOCUMENT_TYPE_PDF);
        } else {
            properties.setDocumentType(DocumentDispatcherProperties.DOCUMENT_TYPE_RTF);
        }

        properties.setEncrypt(encryptedYesRadio.isSelected());

        String writeToOption = "FILE";
        if (outputAttachmentRadioButton.isSelected()) {
            writeToOption = "ATTACHMENT";
        } else if (outputBothRadioButton.isSelected()) {
            writeToOption = "BOTH";
        }

        properties.setOutput(writeToOption);

        properties.setPassword(new String(passwordField.getPassword()));

        properties.setPageWidth(pageSizeWidthField.getText());
        properties.setPageHeight(pageSizeHeightField.getText());
        properties.setPageUnit((Unit) pageSizeUnitComboBox.getSelectedItem());

        properties.setTemplate(templateTextArea.getText());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        DocumentDispatcherProperties props = (DocumentDispatcherProperties) properties;

        directoryField.setText(props.getHost());
        fileNameField.setText(props.getOutputPattern());

        if (props.isEncrypt()) {
            encryptedYesRadio.setSelected(true);
            encryptedYesActionPerformed();
        } else {
            encryptedNoRadio.setSelected(true);
            encryptedNoActionPerformed();
        }

        outputFileRadioButton.setSelected(true);

        String writeToOptions = props.getOutput();
        if (StringUtils.isNotBlank(writeToOptions)) {
            if (writeToOptions.equalsIgnoreCase("BOTH")) {
                outputBothRadioButton.setSelected(true);
            } else if (writeToOptions.equalsIgnoreCase("ATTACHMENT")) {
                outputAttachmentRadioButton.setSelected(true);
            }

            updateFileEnabled(!writeToOptions.equalsIgnoreCase("ATTACHMENT"));
        }

        if (props.getDocumentType().equals(DocumentDispatcherProperties.DOCUMENT_TYPE_PDF)) {
            documentTypePDFRadio.setSelected(true);
            documentTypePDFRadioActionPerformed();
        } else {
            documentTypeRTFRadio.setSelected(true);
            documentTypeRTFRadioActionPerformed();
        }

        passwordField.setText(props.getPassword());

        pageSizeUpdating = true;
        pageSizeWidthField.setText(props.getPageWidth());
        pageSizeWidthField.setCaretPosition(0);
        pageSizeHeightField.setText(props.getPageHeight());
        pageSizeHeightField.setCaretPosition(0);
        pageSizeUnitComboBox.setSelectedItem(props.getPageUnit());
        pageSizeUpdating = false;
        updatePageSizeComboBox();

        templateTextArea.setText(props.getTemplate());
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new DocumentDispatcherProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        DocumentDispatcherProperties props = (DocumentDispatcherProperties) properties;

        boolean valid = true;

        if (!outputAttachmentRadioButton.isSelected() && props.getHost().length() == 0) {
            valid = false;
            if (highlight) {
                directoryField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (!outputAttachmentRadioButton.isSelected() && props.getOutputPattern().length() == 0) {
            valid = false;
            if (highlight) {
                fileNameField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getTemplate().length() == 0) {
            valid = false;
            if (highlight) {
                templateTextArea.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.isEncrypt()) {
            if (props.getPassword().length() == 0) {
                valid = false;
                if (highlight) {
                    passwordField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }
        if (StringUtils.isBlank(props.getPageWidth()) || NumberUtils.toDouble(props.getPageWidth(), 1) <= 0) {
            valid = false;
            if (highlight) {
                pageSizeWidthField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (StringUtils.isBlank(props.getPageHeight()) || NumberUtils.toDouble(props.getPageHeight(), 1) <= 0) {
            valid = false;
            if (highlight) {
                pageSizeHeightField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        directoryField.setBackground(null);
        fileNameField.setBackground(null);
        templateTextArea.setBackground(null);
        passwordField.setBackground(null);
        pageSizeWidthField.setBackground(null);
        pageSizeHeightField.setBackground(null);
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        outputLabel = new JLabel("Output:");
        ButtonGroup outputButtonGroup = new ButtonGroup();

        outputFileRadioButton = new MirthRadioButton("File");
        outputFileRadioButton.setBackground(getBackground());
        outputFileRadioButton.setToolTipText("Write the contents to a file.");
        outputFileRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateFileEnabled(true);
            }
        });
        outputButtonGroup.add(outputFileRadioButton);

        outputAttachmentRadioButton = new MirthRadioButton("Attachment");
        outputAttachmentRadioButton.setBackground(getBackground());
        outputAttachmentRadioButton.setToolTipText("<html>Write the contents to an attachment. The destination's response message will contain the<br>attachment Id and can be used in subsequent connectors to include the attachment.</html>");
        outputAttachmentRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateFileEnabled(false);
            }
        });
        outputButtonGroup.add(outputAttachmentRadioButton);

        outputBothRadioButton = new MirthRadioButton("Both");
        outputBothRadioButton.setBackground(getBackground());
        outputBothRadioButton.setToolTipText("<html>Write the contents to a file and an attachment. The destination's response message will contain<br>the attachment Id and can be used in subsequent connectors to include the attachment.</html>");
        outputBothRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateFileEnabled(true);
            }
        });
        outputButtonGroup.add(outputBothRadioButton);

        directoryLabel = new JLabel("Directory:");

        directoryField = new MirthTextField();
        directoryField.setToolTipText("The directory (folder) where the generated file should be written.");

        testConnectionButton = new JButton("Test Write");
        testConnectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                testConnection();
            }
        });

        fileNameLabel = new JLabel("File Name:");
        fileNameField = new MirthTextField();
        fileNameField.setToolTipText("The file name to give to the generated file.");

        documentTypeLabel = new JLabel("Document Type:");
        ButtonGroup documentTypeButtonGroup = new ButtonGroup();

        documentTypePDFRadio = new MirthRadioButton("PDF");
        documentTypePDFRadio.setBackground(getBackground());
        documentTypePDFRadio.setToolTipText("The type of document to be created for each message.");
        documentTypePDFRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                documentTypePDFRadioActionPerformed();
            }
        });
        documentTypeButtonGroup.add(documentTypePDFRadio);

        documentTypeRTFRadio = new MirthRadioButton("RTF");
        documentTypeRTFRadio.setBackground(getBackground());
        documentTypeRTFRadio.setToolTipText("The type of document to be created for each message.");
        documentTypeRTFRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                documentTypeRTFRadioActionPerformed();
            }
        });
        documentTypeButtonGroup.add(documentTypeRTFRadio);

        encryptedLabel = new JLabel("Encrypted:");
        ButtonGroup encryptedButtonGroup = new ButtonGroup();

        encryptedYesRadio = new MirthRadioButton("Yes");
        encryptedYesRadio.setBackground(getBackground());
        encryptedYesRadio.setToolTipText("If Document Type PDF is selected, generated documents can optionally be encrypted.");
        encryptedYesRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                encryptedYesActionPerformed();
            }
        });
        encryptedButtonGroup.add(encryptedYesRadio);

        encryptedNoRadio = new MirthRadioButton("No");
        encryptedNoRadio.setBackground(getBackground());
        encryptedNoRadio.setToolTipText("If Document Type PDF is selected, generated documents can optionally be encrypted.");
        encryptedNoRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                encryptedNoActionPerformed();
            }
        });
        encryptedButtonGroup.add(encryptedNoRadio);

        passwordLabel = new JLabel("Password:");
        passwordField = new MirthPasswordField();
        passwordField.setToolTipText("If Encrypted Yes is selected, enter the password to be used to later view the document here.");

        pageSizeLabel = new JLabel("Page Size:");
        pageSizeXLabel = new JLabel("×");

        DocumentListener pageSizeDocumentListener = new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent evt) {
                updatePageSizeComboBox();
            }

            @Override
            public void insertUpdate(DocumentEvent evt) {
                updatePageSizeComboBox();
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                updatePageSizeComboBox();
            }
        };

        pageSizeWidthField = new MirthTextField();
        pageSizeWidthField.getDocument().addDocumentListener(pageSizeDocumentListener);
        pageSizeWidthField.setToolTipText("<html>The width of the page. The units for the width<br/>are determined by the drop-down menu to the right.<br/>When rendering PDFs, a minimum of 26mm is enforced.</html>");

        pageSizeHeightField = new MirthTextField();
        pageSizeHeightField.getDocument().addDocumentListener(pageSizeDocumentListener);
        pageSizeHeightField.setToolTipText("<html>The height of the page. The units for the height<br/>are determined by the drop-down menu to the right.<br/>When rendering PDFs, a minimum of 26mm is enforced.</html>");

        pageSizeUnitComboBox = new MirthComboBox<Unit>();
        pageSizeUnitComboBox.setModel(new DefaultComboBoxModel<Unit>(Unit.values()));
        pageSizeUnitComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                updatePageSizeComboBox();
            }
        });
        pageSizeUnitComboBox.setToolTipText("The units to use for the page width and height.");

        pageSizeComboBox = new MirthComboBox<PageSize>();
        pageSizeComboBox.setModel(new DefaultComboBoxModel<PageSize>(ArrayUtils.subarray(PageSize.values(), 0, PageSize.values().length - 1)));
        pageSizeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                pageSizeComboBoxActionPerformed();
            }
        });
        pageSizeComboBox.setToolTipText("Select a standard page size to use, or enter a custom page size.");

        templateLabel = new JLabel("HTML Template:");
        templateTextArea = new MirthRTextScrollPane(ContextType.DESTINATION_DISPATCHER, false, SyntaxConstants.SYNTAX_STYLE_HTML, false);
        templateTextArea.setBorder(BorderFactory.createEtchedBorder());
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, gap 6", "[]12[]", "[]4[]6[]4[]4[]4[]6[]6[]"));

        add(outputLabel, "right");
        add(outputFileRadioButton, "split 3");
        add(outputAttachmentRadioButton);
        add(outputBothRadioButton);
        add(directoryLabel, "newline, right");
        add(directoryField, "w 200!, split 2");
        add(testConnectionButton, "gapbefore 6");
        add(fileNameLabel, "newline, right");
        add(fileNameField, "w 200!");
        add(documentTypeLabel, "newline, right");
        add(documentTypePDFRadio, "split 2");
        add(documentTypeRTFRadio);
        add(encryptedLabel, "newline, right");
        add(encryptedYesRadio, "split 2");
        add(encryptedNoRadio);
        add(passwordLabel, "newline, right");
        add(passwordField, "w 124!");
        add(pageSizeLabel, "newline, right");
        add(pageSizeWidthField, "w 54!, split 5");
        add(pageSizeXLabel);
        add(pageSizeHeightField, "w 54!");
        add(pageSizeUnitComboBox, "gapbefore 6");
        add(pageSizeComboBox, "gapbefore 6");
        add(templateLabel, "newline, right, top");
        add(templateTextArea, "grow, push, w :500, h :150");
    }

    private void testConnection() {
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void handle(Object response) {
                ConnectionTestResponse connectionTestResponse = (ConnectionTestResponse) response;

                if (connectionTestResponse == null) {
                    parent.alertError(parent, "Failed to invoke service.");
                } else if (connectionTestResponse.getType().equals(ConnectionTestResponse.Type.SUCCESS)) {
                    parent.alertInformation(parent, connectionTestResponse.getMessage());
                } else {
                    parent.alertWarning(parent, connectionTestResponse.getMessage());
                }
            }
        };

        try {
            getServlet(DocumentConnectorServletInterface.class, "Testing file write...", "Error testing file write: ", handler).testWrite(getChannelId(), getChannelName(), ((DocumentDispatcherProperties) getProperties()).getHost());
        } catch (ClientException e) {
            // Should not happen
        }
    }

    private void updateFileEnabled(boolean enable) {
        fileNameLabel.setEnabled(enable);
        fileNameField.setEnabled(enable);
        directoryLabel.setEnabled(enable);
        directoryField.setEnabled(enable);
        testConnectionButton.setEnabled(enable);
    }

    private void documentTypePDFRadioActionPerformed() {
        if (encryptedYesRadio.isSelected()) {
            encryptedYesActionPerformed();
        } else {
            encryptedNoActionPerformed();
        }

        encryptedLabel.setEnabled(true);
        encryptedYesRadio.setEnabled(true);
        encryptedNoRadio.setEnabled(true);
    }

    private void documentTypeRTFRadioActionPerformed() {
        encryptedLabel.setEnabled(false);
        encryptedYesRadio.setEnabled(false);
        encryptedNoRadio.setEnabled(false);
        encryptedNoActionPerformed();
    }

    private void encryptedYesActionPerformed() {
        passwordLabel.setEnabled(true);
        passwordField.setEnabled(true);
    }

    private void encryptedNoActionPerformed() {
        passwordLabel.setEnabled(false);
        passwordField.setEnabled(false);
    }

    private void updatePageSizeComboBox() {
        if (pageSizeUpdating) {
            return;
        }
        pageSizeUpdating = true;

        try {
            double width = Double.parseDouble(pageSizeWidthField.getText());
            double height = Double.parseDouble(pageSizeHeightField.getText());
            Unit unit = (Unit) pageSizeUnitComboBox.getSelectedItem();

            PageSize matchingPageSize = null;
            for (PageSize pageSize : PageSize.values()) {
                if (pageSize != PageSize.CUSTOM && pageSize.getWidth(unit) == width && pageSize.getHeight(unit) == height) {
                    matchingPageSize = pageSize;
                    break;
                }
            }

            if (matchingPageSize != null) {
                pageSizeComboBox.setModel(new DefaultComboBoxModel<PageSize>(ArrayUtils.subarray(PageSize.values(), 0, PageSize.values().length - 1)));
                pageSizeComboBox.setSelectedItem(matchingPageSize);
                pageSizeUpdating = false;
                return;
            }
        } catch (Exception e) {
        }

        pageSizeComboBox.setModel(new DefaultComboBoxModel<PageSize>(PageSize.values()));
        pageSizeComboBox.setSelectedItem(PageSize.CUSTOM);
        pageSizeUpdating = false;
    }

    private void pageSizeComboBoxActionPerformed() {
        if (pageSizeUpdating) {
            return;
        }
        pageSizeUpdating = true;

        PageSize pageSize = (PageSize) pageSizeComboBox.getSelectedItem();
        if (pageSize != PageSize.CUSTOM) {
            pageSizeComboBox.setModel(new DefaultComboBoxModel<PageSize>(ArrayUtils.subarray(PageSize.values(), 0, PageSize.values().length - 1)));
            pageSizeComboBox.setSelectedItem(pageSize);
            pageSizeWidthField.setText(String.valueOf(new BigDecimal(pageSize.getWidth()).setScale(2, RoundingMode.DOWN)));
            pageSizeWidthField.setCaretPosition(0);
            pageSizeHeightField.setText(String.valueOf(new BigDecimal(pageSize.getHeight()).setScale(2, RoundingMode.DOWN)));
            pageSizeHeightField.setCaretPosition(0);
            pageSizeUnitComboBox.setSelectedItem(pageSize.getUnit());
        }

        pageSizeUpdating = false;
    }

    private JLabel outputLabel;
    private JRadioButton outputFileRadioButton;
    private JRadioButton outputAttachmentRadioButton;
    private JRadioButton outputBothRadioButton;
    private JLabel directoryLabel;
    private JTextField directoryField;
    private JButton testConnectionButton;
    private JLabel fileNameLabel;
    private JTextField fileNameField;
    private JLabel documentTypeLabel;
    private JRadioButton documentTypePDFRadio;
    private JRadioButton documentTypeRTFRadio;
    private JLabel encryptedLabel;
    private JRadioButton encryptedYesRadio;
    private JRadioButton encryptedNoRadio;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JLabel pageSizeLabel;
    private JTextField pageSizeWidthField;
    private JLabel pageSizeXLabel;
    private JTextField pageSizeHeightField;
    private JComboBox<Unit> pageSizeUnitComboBox;
    private JComboBox<PageSize> pageSizeComboBox;
    private JLabel templateLabel;
    private MirthRTextScrollPane templateTextArea;
}
