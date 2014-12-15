/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.libraryresource;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.ResourcePropertiesPanel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.components.MirthTextPane;
import com.mirth.connect.model.ResourceProperties;

public class LibraryResourcePropertiesPanel extends ResourcePropertiesPanel {

    private JLabel directoryLabel;
    private JTextField directoryField;
    private JLabel descriptionLabel;
    private JScrollPane descriptionScrollPane;
    private JTextPane descriptionTextPane;
    private JLabel libraryLabel;
    private MirthTable libraryTable;

    public LibraryResourcePropertiesPanel() {
        initComponents();
    }

    @Override
    public void fillProperties(ResourceProperties properties) {
        LibraryResourceProperties props = (LibraryResourceProperties) properties;
        props.setDirectory(directoryField.getText());
        props.setDescription(descriptionTextPane.getText());
    }

    @Override
    public void setProperties(ResourceProperties properties) {
        final LibraryResourceProperties props = (LibraryResourceProperties) properties;
        directoryField.setText(props.getDirectory());
        descriptionTextPane.setText(props.getDescription());

        final String workingId = PlatformUI.MIRTH_FRAME.startWorking("Loading libraries...");

        SwingWorker<List<String>, Void> worker = new SwingWorker<List<String>, Void>() {

            @Override
            public List<String> doInBackground() throws ClientException {
                return (List<String>) PlatformUI.MIRTH_FRAME.mirthClient.invokePluginMethodAsync(LibraryResourceProperties.PLUGIN_POINT, "getLibraries", props);
            }

            @Override
            public void done() {
                try {
                    List<String> libraries = get();
                    if (libraries == null) {
                        libraries = new ArrayList<String>();
                    }

                    Object[][] data = new Object[libraries.size()][1];
                    int i = 0;

                    for (String library : libraries) {
                        data[i++][0] = library;
                    }

                    ((RefreshTableModel) libraryTable.getModel()).refreshDataVector(data);
                } catch (Throwable t) {
                    if (t instanceof ExecutionException) {
                        t = t.getCause();
                    }
                    PlatformUI.MIRTH_FRAME.alertException(PlatformUI.MIRTH_FRAME, t.getStackTrace(), "Error loading libraries: " + t.toString());
                } finally {
                    PlatformUI.MIRTH_FRAME.stopWorking(workingId);
                }
            }
        };

        worker.execute();
    }

    @Override
    public ResourceProperties getDefaults() {
        return new LibraryResourceProperties();
    }

    @Override
    public String checkProperties() {
        StringBuilder errors = new StringBuilder();

        if (StringUtils.isBlank(directoryField.getText())) {
            directoryField.setBackground(UIConstants.INVALID_COLOR);
            errors.append("Directory cannot be blank.\n");
        }

        return errors.toString();
    }

    @Override
    public void resetInvalidProperties() {
        directoryField.setBackground(null);
    }

    private void initComponents() {
        setLayout(new MigLayout("insets 5, novisualpadding, hidemode 3, fill"));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Library Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        directoryLabel = new JLabel("Directory:");
        add(directoryLabel, "right");

        directoryField = new MirthTextField();
        directoryField.setToolTipText("The directory to load libraries from.");
        add(directoryField, "w 300!");

        descriptionLabel = new JLabel("Description:");
        add(descriptionLabel, "newline, top, right");

        descriptionTextPane = new MirthTextPane();
        descriptionScrollPane = new JScrollPane(descriptionTextPane);
        add(descriptionScrollPane, "grow, sx, push, h 50%");

        libraryLabel = new JLabel("Loaded Libraries:");
        add(libraryLabel, "newline, top, right");

        libraryTable = new MirthTable();
        libraryTable.setModel(new RefreshTableModel(new Object[] { "Library" }, 0));
        libraryTable.setDragEnabled(false);
        libraryTable.setRowSelectionAllowed(false);
        libraryTable.setRowHeight(UIConstants.ROW_HEIGHT);
        libraryTable.setFocusable(false);
        libraryTable.setOpaque(true);
        libraryTable.getTableHeader().setReorderingAllowed(false);
        libraryTable.setEditable(false);
        libraryTable.setSortable(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            libraryTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        add(new JScrollPane(libraryTable), "grow, sx, push, h 50%");
    }
}