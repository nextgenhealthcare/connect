package com.webreach.mirth.client.ui;

import java.util.Vector;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import com.webreach.mirth.model.PasswordRequirements;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.util.PasswordRequirementsChecker;

public class UserEditPanel extends javax.swing.JPanel {

    private User user;
    private UserDialogInterface dialog;
    private Frame parent;

    /** Creates new form ServerIdentificationPanel */
    public UserEditPanel() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();

        username.setDocument(new MirthFieldConstraints(40, false, false, false));
        password.setDocument(new MirthFieldConstraints(40, false, false, false));
        confirmPassword.setDocument(new MirthFieldConstraints(40, false, false, false));
        firstName.setDocument(new MirthFieldConstraints(40, false, false, false));
        lastName.setDocument(new MirthFieldConstraints(40, false, false, false));
        organization.setDocument(new MirthFieldConstraints(255, false, false, false));
        email.setDocument(new MirthFieldConstraints(255, false, false, false));
        phone.setDocument(new MirthFieldConstraints(40, false, false, false));
        description.setDocument(new MirthFieldConstraints(255, false, false, false));
    }

    public void setUser(UserDialogInterface dialog, User user) {
        this.dialog = dialog;
        this.user = user;

        username.setText(user.getUsername());
        password.setText("");
        confirmPassword.setText("");
        firstName.setText(user.getFirstName());
        lastName.setText(user.getLastName());
        organization.setText(user.getOrganization());
        email.setText(user.getEmail());
        phone.setText(user.getPhoneNumber());
        description.setText(user.getDescription());
    }

    public User getUser() {

        user.setUsername(username.getText());
        user.setFirstName(firstName.getText());
        user.setLastName(lastName.getText());
        user.setOrganization(organization.getText());
        user.setEmail(email.getText());
        user.setPhoneNumber(phone.getText());
        user.setDescription(description.getText());

        return user;
    }

    public String getPassword() {
        return String.valueOf(password.getPassword());
    }

    /**
     * This method checks if the finish button can now be enabled
     */
    public boolean checkIfAbleToFinish(boolean checkPasswords) {
    	if (firstNameAsteriskLabel.isVisible() && firstName.getText().trim().equals("")) {
    		dialog.setFinishButtonEnabled(false);
            return false;
    	} else if (lastNameAsteriskLabel.isVisible() && lastName.getText().trim().equals("")) {
    		dialog.setFinishButtonEnabled(false);
            return false;
    	} else if (emailAsteriskLabel.isVisible() && email.getText().trim().equals("")) {
    		dialog.setFinishButtonEnabled(false);
            return false;
    	} else if (organizationAsteriskLabel.isVisible() && organization.getText().trim().equals("")) {
            dialog.setFinishButtonEnabled(false);
            return false;
    	} else if (checkPasswords && (String.valueOf(password.getPassword()).trim().equals("") || String.valueOf(confirmPassword.getPassword()).trim().equals("") || username.getText().trim().equals(""))) {
        	dialog.setFinishButtonEnabled(false);
            return false;
        } else {
            dialog.setFinishButtonEnabled(true);
            return true;
        }
    }
    
    public void setRequiredFields(boolean firstName, boolean lastName, boolean email, boolean organization) {
    	firstNameAsteriskLabel.setVisible(firstName);
    	lastNameAsteriskLabel.setVisible(lastName);
    	emailAsteriskLabel.setVisible(email);
    	organizationAsteriskLabel.setVisible(organization);
    	
    	checkIfAbleToFinish(true);
    }

    public String validateUser(boolean checkPasswords) {
        if (!checkIfAbleToFinish(checkPasswords)) {
            return "Please fill in all required information.";
        }
        
        // If it's a new user or the username was changed, make sure the username isn't already used.
        if (user.getId() == null || !user.getUsername().equals(username.getText())) {
            for (int i = 0; i < parent.users.size(); i++) {
                if (parent.users.get(i).getUsername().equals(username.getText())) {
                    return "Username already exists.";
                }
            }
        }

        if (checkPasswords && !String.valueOf(password.getPassword()).equals(String.valueOf(confirmPassword.getPassword()))) {
            return "Passwords must be the same.";
        }
        
        try {
            PasswordRequirements requirements = parent.getPasswordRequirements();
            Vector<String> passwordProblems = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(String.valueOf(password.getPassword()), requirements);
            if (passwordProblems != null){
                String retString = "";
                for (String problem : passwordProblems){
                    retString += problem + "\n";
                }
                return retString;
            }
        } catch (ClientException e) {
            return "Unable to retrieve password policy";
        }        
        
        return null;
    }

    private void checkAndTriggerFinishButton(java.awt.event.KeyEvent evt) {
        if (!checkIfAbleToFinish(true)) {
            return;
        } else if (evt.getKeyCode() == evt.VK_ENTER) {
            dialog.triggerFinishButton();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        firstName = new com.webreach.mirth.client.ui.components.MirthTextField();
        firstNameLabel = new javax.swing.JLabel();
        lastNameLabel = new javax.swing.JLabel();
        lastName = new com.webreach.mirth.client.ui.components.MirthTextField();
        organization = new com.webreach.mirth.client.ui.components.MirthTextField();
        organizationLabel = new javax.swing.JLabel();
        emailLabel = new javax.swing.JLabel();
        email = new com.webreach.mirth.client.ui.components.MirthTextField();
        phone = new com.webreach.mirth.client.ui.components.MirthTextField();
        phoneLabel = new javax.swing.JLabel();
        usernameAsteriskLabel = new javax.swing.JLabel();
        passwordAsteriskLabel = new javax.swing.JLabel();
        organizationAsteriskLabel = new javax.swing.JLabel();
        username = new javax.swing.JTextField();
        usernameLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        description = new javax.swing.JTextArea();
        descriptionLabel = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        passwordLabel = new javax.swing.JLabel();
        confirmPasswordLabel = new javax.swing.JLabel();
        confirmPassword = new javax.swing.JPasswordField();
        confirmPasswordAsteriskLabel = new javax.swing.JLabel();
        emailAsteriskLabel = new javax.swing.JLabel();
        firstNameAsteriskLabel = new javax.swing.JLabel();
        lastNameAsteriskLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));

        firstName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                firstNameKeyReleased(evt);
            }
        });

        firstNameLabel.setText("First Name:");

        lastNameLabel.setText("Last Name:");

        lastName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                lastNameKeyReleased(evt);
            }
        });

        organization.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                organizationKeyReleased(evt);
            }
        });

        organizationLabel.setText("Organization:");

        emailLabel.setText("Email:");

        email.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                emailKeyReleased(evt);
            }
        });

        phone.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                phoneKeyReleased(evt);
            }
        });

        phoneLabel.setText("Phone:");

        usernameAsteriskLabel.setForeground(new java.awt.Color(255, 0, 0));
        usernameAsteriskLabel.setText("*");

        passwordAsteriskLabel.setForeground(new java.awt.Color(255, 0, 0));
        passwordAsteriskLabel.setText("*");

        organizationAsteriskLabel.setForeground(new java.awt.Color(255, 0, 0));
        organizationAsteriskLabel.setText("*");

        username.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                usernameKeyReleased(evt);
            }
        });

        usernameLabel.setText("Username:");

        description.setColumns(20);
        description.setFont(new java.awt.Font("Tahoma", 0, 11));
        description.setLineWrap(true);
        description.setRows(4);
        description.setWrapStyleWord(true);
        description.setAutoscrolls(false);
        description.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                descriptionKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(description);

        descriptionLabel.setText("Description:");

        password.setFont(new java.awt.Font("Tahoma", 0, 11));
        password.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                passwordKeyReleased(evt);
            }
        });

        passwordLabel.setText("Password:");

        confirmPasswordLabel.setText("Confirm Password:");

        confirmPassword.setFont(new java.awt.Font("Tahoma", 0, 11));
        confirmPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                confirmPasswordKeyReleased(evt);
            }
        });

        confirmPasswordAsteriskLabel.setForeground(new java.awt.Color(255, 0, 0));
        confirmPasswordAsteriskLabel.setText("*");

        emailAsteriskLabel.setForeground(new java.awt.Color(255, 0, 0));
        emailAsteriskLabel.setText("*");

        firstNameAsteriskLabel.setForeground(new java.awt.Color(255, 0, 0));
        firstNameAsteriskLabel.setText("*");

        lastNameAsteriskLabel.setForeground(new java.awt.Color(255, 0, 0));
        lastNameAsteriskLabel.setText("*");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, usernameLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, passwordLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, confirmPasswordLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, firstNameLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, lastNameLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, organizationLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, emailLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, phoneLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, descriptionLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(confirmPassword, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .add(username, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .add(password, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .add(lastName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .add(organization, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .add(email, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .add(phone, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .add(firstName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .add(jScrollPane1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(lastNameAsteriskLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(emailAsteriskLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(organizationAsteriskLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(passwordAsteriskLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(usernameAsteriskLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(confirmPasswordAsteriskLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(firstNameAsteriskLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(username, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(usernameLabel)
                    .add(usernameAsteriskLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwordLabel)
                    .add(password, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(passwordAsteriskLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(confirmPasswordLabel)
                    .add(confirmPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(confirmPasswordAsteriskLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(firstNameLabel)
                    .add(firstName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(firstNameAsteriskLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lastNameLabel)
                    .add(lastName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lastNameAsteriskLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(organizationLabel)
                    .add(organization, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(organizationAsteriskLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(emailLabel)
                    .add(email, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(emailAsteriskLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(phoneLabel)
                    .add(phone, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(descriptionLabel)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void usernameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_usernameKeyReleased
    checkAndTriggerFinishButton(evt);
}//GEN-LAST:event_usernameKeyReleased

private void passwordKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordKeyReleased
    checkAndTriggerFinishButton(evt);
}//GEN-LAST:event_passwordKeyReleased

private void confirmPasswordKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_confirmPasswordKeyReleased
    checkAndTriggerFinishButton(evt);
}//GEN-LAST:event_confirmPasswordKeyReleased

private void firstNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_firstNameKeyReleased
    checkAndTriggerFinishButton(evt);
}//GEN-LAST:event_firstNameKeyReleased

private void lastNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lastNameKeyReleased
    checkAndTriggerFinishButton(evt);
}//GEN-LAST:event_lastNameKeyReleased

private void organizationKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_organizationKeyReleased
    checkAndTriggerFinishButton(evt);
}//GEN-LAST:event_organizationKeyReleased

private void emailKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_emailKeyReleased
    checkAndTriggerFinishButton(evt);
}//GEN-LAST:event_emailKeyReleased

private void phoneKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_phoneKeyReleased
    checkAndTriggerFinishButton(evt);
}//GEN-LAST:event_phoneKeyReleased

private void descriptionKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_descriptionKeyReleased
    checkAndTriggerFinishButton(evt);
}//GEN-LAST:event_descriptionKeyReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPasswordField confirmPassword;
    private javax.swing.JLabel confirmPasswordAsteriskLabel;
    private javax.swing.JLabel confirmPasswordLabel;
    private javax.swing.JTextArea description;
    private javax.swing.JLabel descriptionLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField email;
    private javax.swing.JLabel emailAsteriskLabel;
    private javax.swing.JLabel emailLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField firstName;
    private javax.swing.JLabel firstNameAsteriskLabel;
    private javax.swing.JLabel firstNameLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private com.webreach.mirth.client.ui.components.MirthTextField lastName;
    private javax.swing.JLabel lastNameAsteriskLabel;
    private javax.swing.JLabel lastNameLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField organization;
    private javax.swing.JLabel organizationAsteriskLabel;
    private javax.swing.JLabel organizationLabel;
    private javax.swing.JPasswordField password;
    private javax.swing.JLabel passwordAsteriskLabel;
    private javax.swing.JLabel passwordLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField phone;
    private javax.swing.JLabel phoneLabel;
    private javax.swing.JTextField username;
    private javax.swing.JLabel usernameAsteriskLabel;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
}
