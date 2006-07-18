/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.managers.types;


public class MirthProperty {
	// component
	public static final String ENDPOINT = "endpoint";
	
	// type
	public static final String TEXT = "text";
	public static final String SELECT = "select";
	public static final String MULTI_SELECT = "multi_select";
	public static final String YES_NO = "yes_no";
	public static final String TEXTAREA = "textarea";
	public static final String HIDDEN = "hidden";
	public static final String PASSWORD = "password";
	
	// private members
	private String component; // endpoint, filter, channel, etc.
	private MirthPropertyType type; // TCP, HTTP, etc.
	private String name; // host, port, etc.
	private String defaultValue; // defauly value for property
	private String displayName; // name that is displayed in form
	private String description; // description text
	private String formType; // text, selection, yes/no, etc.
	private String[] formOptions; // size, maxlength
	private String formValidator; // intValidator, ipValidator, etc.
	private String[] formValidatorOptions; // range, limit, etc.
	private boolean isMuleProperty; // will be copied over
	private boolean isRequired; // if is a required property
	private boolean isVisible; // if it will appear on the form
	
	public MirthProperty() {
		
	}

	public String getComponent() {
		return this.component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getDefaultValue() {
		return this.defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String[] getFormOptions() {
		return this.formOptions;
	}

	public void setFormOptions(String[] formOptions) {
		this.formOptions = formOptions;
	}

	public String getFormType() {
		return this.formType;
	}

	public void setFormType(String formType) {
		this.formType = formType;
	}

	public String getFormValidator() {
		return this.formValidator;
	}

	public void setFormValidator(String formValidator) {
		this.formValidator = formValidator;
	}

	public String[] getFormValidatorOptions() {
		return this.formValidatorOptions;
	}

	public void setFormValidatorOptions(String[] formValidatorOptions) {
		this.formValidatorOptions = formValidatorOptions;
	}

	public boolean isMuleProperty() {
		return this.isMuleProperty;
	}

	public void setMuleProperty(boolean isMuleProperty) {
		this.isMuleProperty = isMuleProperty;
	}

	public boolean isRequired() {
		return this.isRequired;
	}

	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}

	public boolean isVisible() {
		return this.isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MirthPropertyType getType() {
		return this.type;
	}

	public void setType(MirthPropertyType type) {
		this.type = type;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.getClass().getSimpleName() + "[");
		buffer.append(getComponent() + "/");
		buffer.append(getType().getName() + "/");
		buffer.append(getName() + " (");
		buffer.append(getFormType() + ")");
		buffer.append("]");
		return buffer.toString();
	}
}
