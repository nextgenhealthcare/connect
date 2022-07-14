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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.internet.InternetAddress;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.model.User;

import net.miginfocom.swing.MigLayout;

public class UserEditPanel extends javax.swing.JPanel {

	private static List<String> STATE_TERRITORY_CODES = Arrays.asList("AL", "AK", "AS", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FL", "GA", "GU", "HI", 
    		"ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MP", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", 
    		"ND", "OH", "OK", "OR", "PA", "PR", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "VI", "WA", "WV", "WI", "WY" );
	
	private static List<String> INDUSTRIES = Arrays.asList("ACO",
        "CHC/FQHC",
        "Clinic",
        "HIE",
        "HIT Consulting",
        "HIT Software",
        "Hospital",
        "Lab",
        "Network",
        "Other",
        "Payer",
        "Physicians Group",
        "Private Practice",
        "Public Health Agency",
        "Radiology Center",
        "University");
	
	private static List<String> ROLES = Arrays.asList("Primary Role*",
		"C-Suite",
		"Consultant - Advisor",
		"Consultant - Engineer",
		"Consultant - Implementer",
		"Employee - Engineer",
		"Employee - Manager",
		"Employee - Director",
		"Employee - VP",
		"Independent Contractor",
		"Other");
	
    private User user;
    private UserDialogInterface dialog;
    private Frame parent;
    private final String DEFAULT_OPTION = "--Select an option--";
    Map<String, String> countryMap = new HashMap<String, String>(); 
    private List<String> countryCodes;
    private List<String> countryNames;

    public UserEditPanel() {
        this.parent = PlatformUI.MIRTH_FRAME;
        
        initializeCountryCodes();
        initComponents();
        initLayout();

    }
    
    private void initializeCountryCodes() {
    	PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    	Set<String> countryCodeSet = phoneUtil.getSupportedRegions();
    	
    	// Sort list in alphabetical order
    	countryCodes = new ArrayList<>(countryCodeSet);
    	java.util.Collections.sort(countryCodes);
    	
    	// get country names for pull down and sort in alphabetical order
        for (String item : countryCodeSet) {
            phoneUtil.getCountryCodeForRegion(item);
            Locale obj = new Locale("", item);
            String countryName = obj.getDisplayCountry();
            countryMap.put(item, countryName);
        }
    	countryNames = countryMap.values().stream().collect(Collectors.toCollection(ArrayList :: new));
    	java.util.Collections.sort(countryNames);
    }
    
    protected List<String> getCountryCodes() {
    	return countryCodes;
    }
    
    protected List<String> getCountryNames() {
    	return countryNames;
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
        if (!StringUtils.isBlank(user.getIndustry())) {
            industry.setSelectedItem(user.getIndustry());
        }
        if (!StringUtils.isBlank(user.getCountry())) {
            country.setSelectedItem(user.getCountry());
        }
        if (!StringUtils.isBlank(user.getStateTerritory())) {
            stateTerritory.setSelectedItem(user.getStateTerritory());
        }
        if (!StringUtils.isBlank(user.getRole())) {
            role.setSelectedItem(user.getRole());
        }
    }

    public User getUser() {

        user.setUsername(username.getText());
        user.setFirstName(firstName.getText());
        user.setLastName(lastName.getText());
        user.setOrganization(organization.getText());
        user.setEmail(email.getText());
        user.setPhoneNumber(phone.getText());
        user.setDescription(description.getText());
        if (!country.getSelectedItem().equals(DEFAULT_OPTION)) {
            user.setCountry((String) country.getSelectedItem());
        }
        if (!industry.getSelectedItem().equals(DEFAULT_OPTION)) {
            user.setIndustry((String) industry.getSelectedItem());
        }
        if (!role.getSelectedItem().equals(DEFAULT_OPTION)) {
            user.setRole((String) role.getSelectedItem());
        }
        if (!stateTerritory.getSelectedItem().equals(DEFAULT_OPTION)) {
            user.setStateTerritory((String) stateTerritory.getSelectedItem());
        }

        return user;
    }

    public String getPassword() {
        return String.valueOf(password.getPassword());
    }

    /**
     * This method checks if the finish button can now be enabled
     */
    public boolean checkIfAbleToFinish() {
        boolean finishEnabled = true;
        // Any of the following clauses cause the finish button to be disabled
        // how do we force the state territory??
        if ((StringUtils.isBlank(username.getText())) || 
           	 passwordIsRequired && (StringUtils.isBlank(String.valueOf(password.getPassword())) || 
             confirmPasswordIsRequired && StringUtils.isBlank(String.valueOf(confirmPassword.getPassword())) ||
        	 firstNameIsRequired && StringUtils.isBlank(firstName.getText()) || 
        	 lastNameIsRequired && StringUtils.isBlank(lastName.getText()) || 
        	 emailIsRequired && StringUtils.isBlank(email.getText()) || 
        	 countryIsRequired && country.getSelectedItem().equals(DEFAULT_OPTION) ||
        	 phoneIsRequired && StringUtils.isBlank(phone.getText()) || 
        	 organizationIsRequired && StringUtils.isBlank(organization.getText()) || 
        	 roleIsRequired && role.getSelectedItem().equals(DEFAULT_OPTION) ||
        	 stateTerritoryIsRequired && stateTerritory.getSelectedItem().equals(DEFAULT_OPTION) ||
        	 industryIsRequired && industry.getSelectedItem().equals(DEFAULT_OPTION))) {
            finishEnabled = false;
        }
        dialog.setFinishButtonEnabled(finishEnabled);
        return finishEnabled;
    }

    // allRequired = all fields are required
    // passwordRequired = password fields are required for new users screen and first time login
    // for all users
    // passwordRequired = false when editing a user
    public void setRequiredFields(boolean allRequired, boolean passwordRequired) {
        confirmPasswordIsRequired = passwordRequired;
        countryIsRequired = allRequired;
        emailIsRequired = allRequired;
        firstNameIsRequired = allRequired;
        industryIsRequired = allRequired;
        lastNameIsRequired = allRequired;
        organizationIsRequired = allRequired;
        passwordIsRequired = passwordRequired;
        phoneIsRequired = allRequired;
        roleIsRequired = allRequired;
        // if allRequired then do not show any asterisks
        if (allRequired) {
            passwordAsteriskLabel.setVisible(false);
            confirmPasswordAsteriskLabel.setVisible(false);
        	usernameAsteriskLabel.setVisible(false);
        } else {        	
            passwordAsteriskLabel.setVisible(passwordRequired);
            confirmPasswordAsteriskLabel.setVisible(passwordRequired);
        	usernameAsteriskLabel.setVisible(true);
        }
        checkIfAbleToFinish();
    }

    public String validateUser() {
        if (!checkIfAbleToFinish()) {
            return "Please fill in all required information.";
        }

        // if it's a new user or the username was changed, make sure the username isn't already used.
        if (user.getId() == null || !user.getUsername().equals(username.getText())) {
            for (int i = 0; i < parent.users.size(); i++) {
                if (parent.users.get(i).getUsername().equals(username.getText())) {
                    return "This username already exists. Please choose another one.";
                }
            }
        }

        if (!String.valueOf(password.getPassword()).equals(String.valueOf(confirmPassword.getPassword()))) {
            return "Passwords must be the same.";
        }

        try {
            String emailAddress = email.getText();
            if (StringUtils.isNotBlank(emailAddress)) {
                new InternetAddress(emailAddress).validate();
            }
        } catch (Exception e) {
            return "The email address is invalid: " + e.getMessage();
        }

        if (StringUtils.isNotBlank(phone.getText())) {
        	if (country.getSelectedItem().equals(DEFAULT_OPTION)) {
        		return "Country field is required to validate phone number.";
        	} else {
        		if (!validatePhoneNumber(phone.getText(), getKeyFromValue(countryMap, country.getSelectedItem()).toString())) {
            		return "The phone number is invalid for the given Country and/or State/Territory.";
        		}
        	}
        }
        return null;
    }

    private void checkAndTriggerFinishButton(KeyEvent evt) {
        if (checkIfAbleToFinish() && (evt.getKeyCode() == KeyEvent.VK_ENTER)) {
            dialog.triggerFinishButton();
        }
    }
    
    /***
     * 
     * @param phoneNumber
     * @param countryCode
     * @return Whether the phone number is valid for the given country code
     */
    protected boolean validatePhoneNumber(String phoneNumber, String countryCode) {
    	PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    	try {
    		PhoneNumber parsedPhoneNumber = phoneUtil.parse(phoneNumber, countryCode);
    		return phoneUtil.isValidNumber(parsedPhoneNumber);
		} catch (NumberParseException e) {
			return false;
		}
    }
    
    protected String formatPhoneNumber(String phoneNumber, String countryCode) {
    	PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    	try {
    		String originalPhoneNumber = Long.toString(phoneUtil.parse(phoneNumber, countryCode).getNationalNumber());
    		PhoneNumber parsedPhoneNumber = phoneUtil.parse(originalPhoneNumber, countryCode);
    		return phoneUtil.format(parsedPhoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
		} catch (NumberParseException e) {
			return phoneNumber;
		}
    }
    

    private void initComponents() {
    	setBackground(UIConstants.BACKGROUND_COLOR);
    	
    	usernameAsteriskLabel = new JLabel();
    	usernameAsteriskLabel.setForeground(new Color(255, 0, 0));
    	usernameAsteriskLabel.setText("*");
    	usernameAsteriskLabel.setVisible(false);
    	
    	passwordAsteriskLabel = new JLabel();
    	passwordAsteriskLabel.setForeground(new Color(255, 0, 0));
    	passwordAsteriskLabel.setText("*");
    	passwordAsteriskLabel.setVisible(false);
    	
    	confirmPasswordAsteriskLabel = new JLabel();
    	confirmPasswordAsteriskLabel.setForeground(new Color(255, 0, 0));
    	confirmPasswordAsteriskLabel.setText("*");
    	confirmPasswordAsteriskLabel.setVisible(false);

        usernameLabel = new JLabel("Username:");
        username = new JTextField();
        username.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                usernameKeyReleased(evt);
            }
        });
        
        passwordLabel = new JLabel("New Password:");
        password = new JPasswordField();
        password.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                passwordKeyReleased(evt);
            }
        });
        
        confirmPasswordLabel = new JLabel("Confirm New Password:");
        confirmPassword = new JPasswordField();
        confirmPassword.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                confirmPasswordKeyReleased(evt);
            }
        });
        
        firstNameLabel = new JLabel("First Name:");
        firstName = new MirthTextField();
        firstName.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                firstNameKeyReleased(evt);
            }
        });
        
        lastNameLabel = new JLabel("Last Name:");
        lastName = new MirthTextField();
        lastName.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                lastNameKeyReleased(evt);
            }
        });
        
        emailLabel = new JLabel("Email:");
        email = new MirthTextField();
        email.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                emailKeyReleased(evt);
            }
        });
        
        countryLabel = new JLabel("Country:");
        country = new JComboBox<String>();
        for (String item : getCountryNames()) {
            country.addItem(item);
        }        
        country.getModel().setSelectedItem("United States");
        country.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                countryActionPerformed(evt);
            }
        });  

        stateTerritoryLabel = new JLabel("State/Territory:");
        stateTerritory = new JComboBox<String>();
        for (String item : STATE_TERRITORY_CODES) {
            stateTerritory.addItem(item);
        }
        stateTerritory.getModel().setSelectedItem(DEFAULT_OPTION);
        stateTerritory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	stateTerritoryActionPerformed(evt);
            }
        });  
        
        phoneLabel = new JLabel("Phone:");
        phone = new MirthTextField();
        phone.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                phoneKeyReleased(evt);
            }
        });
        
        organizationLabel = new JLabel("Organization:");
        organization = new MirthTextField();
        organization.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                organizationKeyReleased(evt);
            }
        });
        
        roleLabel = new JLabel("Role:");
        role = new JComboBox<String>();
        for (String item : ROLES) {
        	role.addItem(item);
        }
        role.getModel().setSelectedItem(DEFAULT_OPTION);
        role.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                roleActionPerformed(evt);
            }
        });  
        
        industryLabel = new JLabel("Business:");
        industry = new JComboBox<String>();
        for (String item : INDUSTRIES) {
            industry.addItem(item);
        }
        industry.getModel().setSelectedItem(DEFAULT_OPTION);
        // Disable scroll bar
        industry.setMaximumRowCount(industry.getModel().getSize());
        industry.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                industryActionPerformed(evt);
            }
        });  

        descriptionLabel = new JLabel("Description (Optional):");
        description = new JTextArea();
        description.setColumns(20);
        description.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        description.setLineWrap(true);
        description.setRows(4);
        description.setWrapStyleWord(true);
        description.setAutoscrolls(false);
        jScrollPane1 = new JScrollPane();
        jScrollPane1.setViewportView(description);

    }
    
    private void initLayout() {        
    	JPanel editUserPanel = new JPanel(new MigLayout("novisualpadding, hidemode 0, align center, insets 0 0 0 0, fill", "25[right][fill][]"));
    	editUserPanel.setBackground(UIConstants.BACKGROUND_COLOR);
    	editUserPanel.setBorder(BorderFactory.createEmptyBorder());
    	editUserPanel.setMinimumSize(getMinimumSize());
    	editUserPanel.setMaximumSize(getMaximumSize());
    	editUserPanel.add(usernameLabel);
		editUserPanel.add(username);
    	editUserPanel.add(usernameAsteriskLabel, "wrap");
    	editUserPanel.add(passwordLabel);
    	editUserPanel.add(password);
    	editUserPanel.add(passwordAsteriskLabel, "wrap");
    	editUserPanel.add(confirmPasswordLabel);
    	editUserPanel.add(confirmPassword);
    	editUserPanel.add(confirmPasswordAsteriskLabel, "wrap");
    	editUserPanel.add(firstNameLabel);
    	editUserPanel.add(firstName, "wrap");
    	editUserPanel.add(lastNameLabel);
    	editUserPanel.add(lastName, "wrap");
    	editUserPanel.add(emailLabel);
    	editUserPanel.add(email, "wrap");
    	editUserPanel.add(countryLabel);
    	editUserPanel.add(country, "wrap");
		editUserPanel.add(stateTerritoryLabel);
		editUserPanel.add(stateTerritory, "wrap");
    	editUserPanel.add(phoneLabel);
    	editUserPanel.add(phone, "wrap");
    	editUserPanel.add(organizationLabel);
    	editUserPanel.add(organization, "wrap");
		editUserPanel.add(roleLabel);
		editUserPanel.add(role, "wrap");
    	editUserPanel.add(industryLabel);
    	editUserPanel.add(industry, "wrap");
    	editUserPanel.add(descriptionLabel);
    	editUserPanel.add(jScrollPane1, "wrap");
    	
    	add(editUserPanel);

    }
    
    private void usernameKeyReleased(KeyEvent evt) {
        checkAndTriggerFinishButton(evt);
    }

    private void passwordKeyReleased(KeyEvent evt) {
        checkAndTriggerFinishButton(evt);
    }

    private void confirmPasswordKeyReleased(KeyEvent evt) {
        checkAndTriggerFinishButton(evt);
    }

    private void firstNameKeyReleased(KeyEvent evt) {
        checkAndTriggerFinishButton(evt);
    }

    private void lastNameKeyReleased(KeyEvent evt) {
        checkAndTriggerFinishButton(evt);
    }

    private void organizationKeyReleased(KeyEvent evt) {
        checkAndTriggerFinishButton(evt);
    }

    private void emailKeyReleased(KeyEvent evt) {
        checkAndTriggerFinishButton(evt);
    }

    private void phoneKeyReleased(KeyEvent evt) {
    	// this commented code will add the country code in front of the phone number - like +1 for the US
    	phone.setText(formatPhoneNumber(phone.getText(), getKeyFromValue(countryMap, country.getSelectedItem()).toString()));
        checkAndTriggerFinishButton(evt);
    }

    private void countryActionPerformed(ActionEvent evt) {
        if (dialog != null) {
        	if (country.getSelectedItem() == "United States") {
        		stateTerritory.setEnabled(true);
        		if (firstNameIsRequired) {
        			stateTerritoryIsRequired = true;
        		}
        	} else {
                stateTerritory.getModel().setSelectedItem(DEFAULT_OPTION);
        		stateTerritory.setEnabled(false);
        		stateTerritoryIsRequired = false;
        	}
        	phone.setText(formatPhoneNumber(phone.getText(), getKeyFromValue(countryMap, country.getSelectedItem()).toString()));
            checkIfAbleToFinish();
        }
    }

    private void stateTerritoryActionPerformed(ActionEvent evt) {
        if (dialog != null) {
            checkIfAbleToFinish();
        }
    }

    private void roleActionPerformed(ActionEvent evt) {
        if (dialog != null) {
            checkIfAbleToFinish();
        }
    }
    
    private void industryActionPerformed(ActionEvent evt) {
        if (dialog != null) {
            checkIfAbleToFinish();
        }
    }
    
    public Object getKeyFromValue(Map map, Object value) {
    	for (Object item : map.keySet()) {
    		if (map.get(item).equals(value)) {
    			return item;
    		}
    	}
    	return null;
    }

    private JPasswordField confirmPassword;
    private JLabel confirmPasswordAsteriskLabel;
    private Boolean confirmPasswordIsRequired = false;
    private JLabel confirmPasswordLabel;
    private JComboBox<String> country;
    private Boolean countryIsRequired = false;
    private JLabel countryLabel;
    private JTextArea description;
    private JLabel descriptionLabel;
    private MirthTextField email;
    private Boolean emailIsRequired = false;
    private JLabel emailLabel;
    private MirthTextField firstName;
    private Boolean firstNameIsRequired = false;
    private JLabel firstNameLabel;
    private JComboBox<String> industry;
    private Boolean industryIsRequired = false;
    private JLabel industryLabel;
    private JScrollPane jScrollPane1;
    private MirthTextField lastName;
    private Boolean lastNameIsRequired = false;
    private JLabel lastNameLabel;
    private MirthTextField organization;
    private Boolean organizationIsRequired = false;
    private JLabel organizationLabel;
    private JPasswordField password;
    private JLabel passwordAsteriskLabel;
    private Boolean passwordIsRequired = false;
    private JLabel passwordLabel;
    private MirthTextField phone;
    private Boolean phoneIsRequired = false;
    private JLabel phoneLabel;
    private JComboBox<String> role;
    private Boolean roleIsRequired = false;
    private JLabel roleLabel;
    private JComboBox<String> stateTerritory;
    private Boolean stateTerritoryIsRequired = false;
    private JLabel stateTerritoryLabel;
    private JTextField username;
    private JLabel usernameAsteriskLabel;
    private Boolean usernameIsRequired = false;
    private JLabel usernameLabel;
}
