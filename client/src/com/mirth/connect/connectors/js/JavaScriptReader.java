/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.js;

import java.util.Properties;
import java.util.UUID;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;

import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.connectors.ConnectorClass;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class JavaScriptReader extends ConnectorClass {

    private static SyntaxDocument jsMappingDoc;

    public JavaScriptReader() {
        name = JavaScriptReaderProperties.name;

        initComponents();

        jsMappingDoc = new SyntaxDocument();
        jsMappingDoc.setTokenMarker(new JavaScriptTokenMarker());

        javascriptTextPane.setDocument(jsMappingDoc);
        pollingFrequency.setDocument(new MirthFieldConstraints(0, false, false, true));
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(JavaScriptReaderProperties.DATATYPE, name);
        properties.put(JavaScriptWriterProperties.JAVASCRIPT_HOST, "sink");
        properties.put(JavaScriptReaderProperties.JAVASCRIPT_SCRIPT, javascriptTextPane.getText());

        if (pollingIntervalButton.isSelected()) {
            properties.put(JavaScriptReaderProperties.JAVASCRIPT_POLLING_TYPE, "interval");
            properties.put(JavaScriptReaderProperties.JAVASCRIPT_POLLING_FREQUENCY, pollingFrequency.getText());
        } else {
            properties.put(JavaScriptReaderProperties.JAVASCRIPT_POLLING_TYPE, "time");
            properties.put(JavaScriptReaderProperties.JAVASCRIPT_POLLING_TIME, pollingTime.getDate());
        }

        return properties;
    }

    public void setProperties(Properties props) {
        resetInvalidProperties();

        javascriptTextPane.setText((String) props.get(JavaScriptReaderProperties.JAVASCRIPT_SCRIPT));

        if (((String) props.get(JavaScriptReaderProperties.JAVASCRIPT_POLLING_TYPE)).equalsIgnoreCase("interval")) {
            pollingIntervalButton.setSelected(true);
            pollingIntervalButtonActionPerformed(null);
            pollingFrequency.setText((String) props.get(JavaScriptReaderProperties.JAVASCRIPT_POLLING_FREQUENCY));
        } else {
            pollingTimeButton.setSelected(true);
            pollingTimeButtonActionPerformed(null);
            pollingTime.setDate((String) props.get(JavaScriptReaderProperties.JAVASCRIPT_POLLING_TIME));
        }
    }

    public Properties getDefaults() {
        return new JavaScriptReaderProperties().getDefaults();
    }

    public boolean checkProperties(Properties props, boolean highlight) {
        resetInvalidProperties();
        boolean valid = true;

        if (((String) props.get(JavaScriptReaderProperties.JAVASCRIPT_POLLING_TYPE)).equalsIgnoreCase("interval") && ((String) props.get(JavaScriptReaderProperties.JAVASCRIPT_POLLING_FREQUENCY)).length() == 0) {
            valid = false;
            if (highlight) {
                pollingFrequency.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(JavaScriptReaderProperties.JAVASCRIPT_POLLING_TYPE)).equalsIgnoreCase("time") && ((String) props.get(JavaScriptReaderProperties.JAVASCRIPT_POLLING_TIME)).length() == 0) {
            valid = false;
            if (highlight) {
                pollingTime.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(JavaScriptReaderProperties.JAVASCRIPT_SCRIPT)).length() == 0) {
            valid = false;
            if (highlight) {
                javascriptTextPane.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    private void resetInvalidProperties() {
        pollingFrequency.setBackground(null);
        pollingTime.setBackground(null);
        javascriptTextPane.setBackground(null);
    }

    public String doValidate(Properties props, boolean highlight) {
        String error = null;

        if (!checkProperties(props, highlight)) {
            error = "Error in the form for connector \"" + getName() + "\".\n\n";
        }

        String script = ((String) props.get(JavaScriptReaderProperties.JAVASCRIPT_SCRIPT));

        if (script.length() != 0) {
            Context context = Context.enter();
            try {
                context.compileString("function rhinoWrapper() {" + script + "\n}", UUID.randomUUID().toString(), 1, null);
            } catch (EvaluatorException e) {
                if (error == null) {
                    error = "";
                }
                error += "Error in connector \"" + getName() + "\" at Javascript:\nError on line " + e.lineNumber() + ": " + e.getMessage() + ".\n\n";
            } catch (Exception e) {
                if (error == null) {
                    error = "";
                }
                error += "Error in connector \"" + getName() + "\" at Javascript:\nUnknown error occurred during validation.";
            }

            Context.exit();
        }

        return error;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jsLabel = new javax.swing.JLabel();
        pollingFrequencyLabel = new javax.swing.JLabel();
        pollingFrequency = new com.mirth.connect.client.ui.components.MirthTextField();
        javascriptTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea(true,false);
        pollingTimeButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        pollingIntervalButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel5 = new javax.swing.JLabel();
        pollingTimeLabel = new javax.swing.JLabel();
        pollingTime = new com.mirth.connect.client.ui.components.MirthTimePicker();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jsLabel.setText("JavaScript:");

        pollingFrequencyLabel.setText("Polling Frequency (ms):");

        pollingFrequency.setToolTipText("<html>If the \"Interval\" Polling Type is selected, the number of milliseconds between polls must be entered here.<br>Avoid extremely small values because polling can be a somewhat time-consuming operation.</html>");

        javascriptTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        pollingTimeButton.setBackground(new java.awt.Color(255, 255, 255));
        pollingTimeButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(pollingTimeButton);
        pollingTimeButton.setText("Time");
        pollingTimeButton.setToolTipText("<html>A JavaScript reader polls to determine when new messages have arrived.<br>It can be configured when to poll in one of two different ways.<br>If \"Interval\" is selected, the connector will poll every n milliseconds, where n is specified in the Polling Frequency control.<br>If \"Time\" is selected, the connector will poll once a day at the time specified in the Polling Time control.</html>");
        pollingTimeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pollingTimeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pollingTimeButtonActionPerformed(evt);
            }
        });

        pollingIntervalButton.setBackground(new java.awt.Color(255, 255, 255));
        pollingIntervalButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(pollingIntervalButton);
        pollingIntervalButton.setText("Interval");
        pollingIntervalButton.setToolTipText("<html>A JavaScript reader polls to determine when new messages have arrived.<br>It can be configured when to poll in one of two different ways.<br>If \"Interval\" is selected, the connector will poll every n milliseconds, where n is specified in the Polling Frequency control.<br>If \"Time\" is selected, the connector will poll once a day at the time specified in the Polling Time control.</html>");
        pollingIntervalButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pollingIntervalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pollingIntervalButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("Polling Type:");

        pollingTimeLabel.setText("Polling Time (daily):");

        pollingTime.setToolTipText("If the \"Time\" Polling Type is selected, the time of day to poll must be entered here.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jsLabel)
                    .addComponent(pollingTimeLabel)
                    .addComponent(pollingFrequencyLabel)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pollingIntervalButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pollingTimeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pollingFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pollingTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(javascriptTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(pollingIntervalButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pollingTimeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pollingFrequencyLabel)
                    .addComponent(pollingFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pollingTimeLabel)
                    .addComponent(pollingTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jsLabel)
                    .addComponent(javascriptTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void pollingIntervalButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_pollingIntervalButtonActionPerformed
    {//GEN-HEADEREND:event_pollingIntervalButtonActionPerformed
        pollingFrequencyLabel.setEnabled(true);
        pollingTimeLabel.setEnabled(false);
        pollingFrequency.setEnabled(true);
        pollingTime.setEnabled(false);
    }//GEN-LAST:event_pollingIntervalButtonActionPerformed

    private void pollingTimeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_pollingTimeButtonActionPerformed
    {//GEN-HEADEREND:event_pollingTimeButtonActionPerformed
        pollingFrequencyLabel.setEnabled(false);
        pollingTimeLabel.setEnabled(true);
        pollingFrequency.setEnabled(false);
        pollingTime.setEnabled(true);
    }//GEN-LAST:event_pollingTimeButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JLabel jLabel5;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea javascriptTextPane;
    private javax.swing.JLabel jsLabel;
    private com.mirth.connect.client.ui.components.MirthTextField pollingFrequency;
    private javax.swing.JLabel pollingFrequencyLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton pollingIntervalButton;
    private com.mirth.connect.client.ui.components.MirthTimePicker pollingTime;
    private com.mirth.connect.client.ui.components.MirthRadioButton pollingTimeButton;
    private javax.swing.JLabel pollingTimeLabel;
    // End of variables declaration//GEN-END:variables
}
