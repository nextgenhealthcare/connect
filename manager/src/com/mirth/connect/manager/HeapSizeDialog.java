/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.manager;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;

public class HeapSizeDialog extends JDialog {
    private String heapSize;
    private ManagerController managerController;

    public HeapSizeDialog(String heapSize) {
        super(PlatformUI.MANAGER_DIALOG, true);

        managerController = ManagerController.getInstance();
        getRootPane().registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        DisplayUtil.setResizable(this, false);
        setBackground(Color.white);
        setTitle("Web Start Settings");
        getContentPane().setBackground(Color.white);

        initComponents();

        this.heapSize = StringUtils.isEmpty(heapSize) ? "512m" : heapSize;

        String heapSizeOption = HeapSize.toDisplayName(heapSize);
        if (StringUtils.isBlank(heapSizeOption)) {
            heapSizeOption = this.heapSize;
        }

        // Add any non-default properties to the model
        String property = (String) managerController.getServerProperties().getProperty(ManagerConstants.ADMINISTRATOR_MAX_HEAP_SIZE);
        if (!heapSizeComboboxModel.contains(property) && !heapSizeComboboxModel.contains(HeapSize.toDisplayName(property))) {
            heapSizeComboboxModel.add(formatCustomProperty(property));
        }

        // Resort list by sizes
        List<String> mbList = new ArrayList<String>();
        List<String> gbList = new ArrayList<String>();
        for (String size : heapSizeComboboxModel) {
            if (size.contains("M")) {
                mbList.add(size);
            } else {
                gbList.add(size);
            }
        }

        Collections.sort(mbList);
        Collections.sort(gbList);
        mbList.addAll(gbList);

        heapSizeComboBox = new JComboBox(mbList.toArray());
        heapSizeComboBox.getModel().setSelectedItem(formatCustomProperty(heapSizeOption));

        initLayout();
        pack();
        setLocationRelativeTo(PlatformUI.MANAGER_DIALOG);
        setVisible(true);
    }

    public String getHeapSize() {
        String heapSize = HeapSize.fromDisplayName((String) heapSizeComboBox.getSelectedItem());
        if (StringUtils.isBlank(heapSize)) {
            heapSize = (String) heapSizeComboBox.getSelectedItem();

            String[] formattedHeapSize = heapSize.split(" ");
            heapSize = formattedHeapSize[0] + formattedHeapSize[1].toLowerCase().substring(0, 1);
        }

        return heapSize;
    }

    private void initComponents() {
        heapSizeComboboxModel = new ArrayList<String>();
        Object customComboboxProperties = managerController.getServerProperties().getProperty(ManagerConstants.ADMINISTRATOR_MAX_HEAP_SIZE_OPTIONS);
        if (customComboboxProperties instanceof String) {
            heapSizeComboboxModel.add(formatCustomProperty((String) customComboboxProperties));
        } else if (customComboboxProperties instanceof List) {
            for (String property : (ArrayList<String>) customComboboxProperties) {
                heapSizeComboboxModel.add(formatCustomProperty((String) property));
            }
        }

        if (heapSizeComboboxModel.isEmpty()) {
            heapSizeComboboxModel.add(HeapSize._256MB.getDisplayName());
            heapSizeComboboxModel.add(HeapSize._512MB.getDisplayName());
            heapSizeComboboxModel.add(HeapSize._1GB.getDisplayName());
            heapSizeComboboxModel.add(HeapSize._2GB.getDisplayName());
        }

        warningLabel = new JLabel("<html>Note: The Administrator may fail to start<br>if the max heap size is set too high.</html>");

        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, fill"));
        JPanel panel = new JPanel(new MigLayout("insets 4, novisualpadding, hidemode 3, fill"));
        panel.setBackground(Color.white);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(204, 204, 204)), "Web Start Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        panel.add(new JLabel("Max Heap Size:"), "split");
        panel.add(heapSizeComboBox, "w 75!, left, wrap");

        panel.add(warningLabel, "split");

        add(panel);
        add(new JSeparator(), "newline, growx, sx");
        add(okButton, "newline, h 22!, w 56!, sx, right, split");
        add(cancelButton, "h 22!, w 56!");
    }

    private String formatCustomProperty(String property) { // This should probably try/catch
        String[] prop = property.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        String units = String.valueOf(prop[1]);
        if (StringUtils.isNotBlank(units)) {
            units = units.toLowerCase().contains("m") ? "MB" : "GB";
        }

        return String.valueOf(prop[0]) + " " + units;
    }

    private enum HeapSize {
        _256MB("256 MB", "256m"), _512MB("512 MB", "512m"), _1GB("1 GB", "1g"), _2GB("2 GB", "2g");

        private String displayName;
        private String value;

        private HeapSize(String displayName, String value) {
            this.displayName = displayName;
            this.value = value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getValue() {
            return value;
        }

        public static String fromDisplayName(String displayName) {
            for (HeapSize type : HeapSize.values()) {
                if (type.getDisplayName().equals(displayName)) {
                    return type.value;
                }
            }

            return null;
        }

        public static String toDisplayName(String value) {
            for (HeapSize type : HeapSize.values()) {
                if (type.getValue().equals(value)) {
                    return type.displayName;
                }
            }

            return null;
        }
    }

    private List<String> heapSizeComboboxModel;
    private JComboBox heapSizeComboBox;

    private JLabel warningLabel;
    private JButton okButton;
    private JButton cancelButton;
}
