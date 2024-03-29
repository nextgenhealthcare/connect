/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.ui.util.DisplayUtil;
import com.mirth.connect.model.User;

public class ChangePasswordDialog extends MirthDialog {

    private FrameBase parent;
    private User currentUser;

    public ChangePasswordDialog(User currentUser, String message) {
        super(PlatformUI.MIRTH_FRAME);
        this.currentUser = currentUser;
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        DisplayUtil.setResizable(this, false);

        mirthHeadingLabel.setForeground(UIConstants.HEADER_TITLE_TEXT_COLOR);

        passwordTextArea.setText(message);
        passwordTextArea.setBackground(Color.WHITE);
        passwordTextArea.setDisabledTextColor(Color.RED);

        finishButton.setEnabled(false);

        setModal(true);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                cancelButtonActionPerformed(null);
            }
        });

        pack();
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();

        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
            setLocationRelativeTo(null);
        } else {
            setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }

        setVisible(true);
    }

    public void setFinishButtonEnabled(boolean enabled) {
        finishButton.setEnabled(enabled);
    }

    private void checkAndTriggerFinishButton(java.awt.event.KeyEvent evt) {
        if (StringUtils.isBlank(String.valueOf(password.getPassword())) || StringUtils.isBlank(String.valueOf(confirmPassword.getPassword()))) {
            setFinishButtonEnabled(false);
        } else {
            setFinishButtonEnabled(true);

            if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                finishButtonActionPerformed(null);
            }
        }
    }

    // @formatter:off
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        channelOverview = new javax.swing.JPanel();
        mirthHeadingPanel1 = new com.mirth.connect.client.ui.MirthHeadingPanel();
        mirthHeadingLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        confirmPasswordLabel = new javax.swing.JLabel();
        confirmPassword = new javax.swing.JPasswordField();
        jSeparator1 = new javax.swing.JSeparator();
        cancelButton = new javax.swing.JButton();
        finishButton = new javax.swing.JButton();
        passwordPane = new javax.swing.JScrollPane();
        passwordTextArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Change Password");

        channelOverview.setBackground(new java.awt.Color(255, 255, 255));
        channelOverview.setName(""); // NOI18N

        mirthHeadingLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        mirthHeadingLabel.setForeground(new java.awt.Color(255, 255, 255));
        mirthHeadingLabel.setText("Change Password");

        javax.swing.GroupLayout mirthHeadingPanel1Layout = new javax.swing.GroupLayout(mirthHeadingPanel1);
        mirthHeadingPanel1.setLayout(mirthHeadingPanel1Layout);
        mirthHeadingPanel1Layout.setHorizontalGroup(
            mirthHeadingPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mirthHeadingPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mirthHeadingLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                .addContainerGap())
        );
        mirthHeadingPanel1Layout.setVerticalGroup(
            mirthHeadingPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mirthHeadingPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mirthHeadingLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                .addContainerGap())
        );

        mirthHeadingLabel.getAccessibleContext().setAccessibleName("Change Password");

        passwordLabel.setText("New Password:");

        password.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                passwordKeyReleased(evt);
            }
        });

        confirmPasswordLabel.setText("Confirm New Password:");

        confirmPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                confirmPasswordKeyReleased(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        finishButton.setText("Finish");
        finishButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finishButtonActionPerformed(evt);
            }
        });

        passwordPane.setBorder(null);
        passwordPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        passwordPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        passwordTextArea.setColumns(20);
        passwordTextArea.setEditable(false);
        passwordTextArea.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        passwordTextArea.setLineWrap(true);
        passwordTextArea.setRows(2);
        passwordTextArea.setText("Your password has expired. You are required to change your password in the next 14 days and 23 hours.");
        passwordTextArea.setWrapStyleWord(true);
        passwordTextArea.setEnabled(false);
        passwordPane.setViewportView(passwordTextArea);

        javax.swing.GroupLayout channelOverviewLayout = new javax.swing.GroupLayout(channelOverview);
        channelOverview.setLayout(channelOverviewLayout);
        channelOverviewLayout.setHorizontalGroup(
            channelOverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, channelOverviewLayout.createSequentialGroup()
                .addContainerGap(234, Short.MAX_VALUE)
                .addComponent(cancelButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(finishButton)
                .addGap(9, 9, 9))
            .addComponent(mirthHeadingPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, channelOverviewLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(channelOverviewLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(channelOverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(passwordLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(confirmPasswordLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(channelOverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(confirmPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(45, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, channelOverviewLayout.createSequentialGroup()
                .addContainerGap(34, Short.MAX_VALUE)
                .addComponent(passwordPane, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
        );
        channelOverviewLayout.setVerticalGroup(
            channelOverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, channelOverviewLayout.createSequentialGroup()
                .addComponent(mirthHeadingPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(channelOverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(channelOverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(confirmPasswordLabel)
                    .addComponent(confirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(passwordPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(channelOverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(finishButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(channelOverview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(channelOverview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // @formatter:on

    /**
     * An action for when the finish button is pressed. Checks and saves all of the information.
     */
    private void finishButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finishButtonActionPerformed
        password.requestFocusInWindow();

        if (!String.valueOf(password.getPassword()).equals(String.valueOf(confirmPassword.getPassword()))) {
            parent.alertError(this, "The passwords you entered do not match.");
            return;
        } else if (!parent.checkOrUpdateUserPassword(this, currentUser, String.valueOf(password.getPassword()))) {
            return;
        }

        this.dispose();
    }//GEN-LAST:event_finishButtonActionPerformed

    private void passwordKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordKeyReleased
        checkAndTriggerFinishButton(evt);
    }//GEN-LAST:event_passwordKeyReleased

    private void confirmPasswordKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_confirmPasswordKeyReleased
        checkAndTriggerFinishButton(evt);
    }//GEN-LAST:event_confirmPasswordKeyReleased

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel channelOverview;
    private javax.swing.JPasswordField confirmPassword;
    private javax.swing.JLabel confirmPasswordLabel;
    private javax.swing.JButton finishButton;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel mirthHeadingLabel;
    private com.mirth.connect.client.ui.MirthHeadingPanel mirthHeadingPanel1;
    private javax.swing.JPasswordField password;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JScrollPane passwordPane;
    private javax.swing.JTextArea passwordTextArea;
    // End of variables declaration//GEN-END:variables
}
