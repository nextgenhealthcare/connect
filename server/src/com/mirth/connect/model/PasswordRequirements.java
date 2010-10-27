/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;

public class PasswordRequirements implements Serializable {
	private boolean requireUpper;
	private boolean requireLower;
	private boolean requireNumeric;
	private boolean requireSpecial;
	private int minLength;

	public PasswordRequirements() {
		this.requireUpper = false;
		this.requireLower = false;
		this.requireNumeric = false;
		this.requireSpecial = false;
		this.minLength = 0;
	}

	public PasswordRequirements(boolean requireUpper, boolean requireLower, boolean requireNumeric, boolean requireSpecial, int minLength) {
		this.requireUpper = requireUpper;
		this.requireLower = requireLower;
		this.requireSpecial = requireSpecial;
		this.minLength = minLength;
		this.requireNumeric = requireNumeric;
	}

	public int getMinLength() {
		return minLength;
	}

	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}

	public boolean isRequireLower() {
		return requireLower;
	}

	public void setRequireLower(boolean requireLower) {
		this.requireLower = requireLower;
	}

	public boolean isRequireNumeric() {
		return requireNumeric;
	}

	public void setRequireNumeric(boolean requireNumeric) {
		this.requireNumeric = requireNumeric;
	}

	public boolean isRequireSpecial() {
		return requireSpecial;
	}

	public void setRequireSpecial(boolean requireSpecial) {
		this.requireSpecial = requireSpecial;
	}

	public boolean isRequireUpper() {
		return requireUpper;
	}

	public void setRequireUpper(boolean requireUpper) {
		this.requireUpper = requireUpper;
	}
}
