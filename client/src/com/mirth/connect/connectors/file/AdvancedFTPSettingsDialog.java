package com.mirth.connect.connectors.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.util.DisplayUtil;

import net.miginfocom.swing.MigLayout;

public class AdvancedFTPSettingsDialog extends AdvancedSettingsDialog {

    private boolean saved;
    
    public AdvancedFTPSettingsDialog(FTPSchemeProperties schemeProperties) {
        setTitle("Method Settings");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(new Dimension(350, 140));
        DisplayUtil.setResizable(this, false);
        setLayout(new MigLayout("insets 8 8 0 8, novisualpadding, hidemode 3"));
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);

        initComponents();
        initLayout();

        setFileSchemeProperties(schemeProperties != null ? schemeProperties:new FTPSchemeProperties());

        setLocationRelativeTo(PlatformUI.MIRTH_FRAME);
        setVisible(true);
    }
    
    @Override
    public boolean wasSaved() {
        return saved;
    }

    @Override
    public SchemeProperties getSchemeProperties() {
        FTPSchemeProperties props = new FTPSchemeProperties();
        
        props.setInitialCommands(commandStringToList(initialCommandsField.getText()));
        
        return props;
    }
    
    public void setFileSchemeProperties(FTPSchemeProperties schemeProperties) {
        initialCommandsField.setText(commandListToString(schemeProperties.getInitialCommands()));
    }
    
    public boolean validateProperties() {
        return true;
    }
    
    private void initComponents() {
        initialCommandsLabel = new JLabel("Initial Commands:");
        initialCommandsField = new JTextField();
        initialCommandsField.setToolTipText("<html>Commands to run when initializing a FTP connection.<br/>Use commas to separate multiple commands.</html>");
        
        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                okCancelButtonActionPerformed();
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
        JPanel propertiesPanel = new JPanel(new MigLayout("insets 12, novisualpadding, hidemode 3, fillx", "[right][left]"));
        propertiesPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        propertiesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(204, 204, 204)), "FTP Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));
        
        propertiesPanel.add(initialCommandsLabel);
        propertiesPanel.add(initialCommandsField, "w 200!");
        
        add(propertiesPanel, "grow, push, top, wrap");
        
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0 8 8 8, novisualpadding, hidemode 3, fill"));
        buttonPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        buttonPanel.add(new JSeparator(), "growx, sx, wrap");
        buttonPanel.add(okButton, "newline, w 50!, sx, right, split");
        buttonPanel.add(cancelButton, "w 50!");

        add(buttonPanel, "south, span");
    }
    
    private void okCancelButtonActionPerformed() {
        if (!validateProperties()) {
            return;
        }
        
        saved = true;
        PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
        dispose();
    }
    
    private List<String> commandStringToList(String commandString) {
        List<String> commandList = new ArrayList<>();
        String[] commandArray = commandString.split(",");
        for (String command: commandArray) {
            String commandToAdd = command.trim();
            if (StringUtils.isNotBlank(commandToAdd)) {
                commandList.add(commandToAdd);
            }
        }
        return commandList;
    }
    
    private String commandListToString(List<String> commandList) {
        String commandString = null;
        if (CollectionUtils.isNotEmpty(commandList)) {
            commandString = String.join(",", commandList);
        } else {
            commandString = "";
        }
        return commandString;
    }
    
    private JLabel initialCommandsLabel;
    private JTextField initialCommandsField;
    
    private JButton okButton;
    private JButton cancelButton;

}
