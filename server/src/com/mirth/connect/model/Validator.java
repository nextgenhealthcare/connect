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
import java.util.HashMap;
import java.util.Map;

public class Validator implements Serializable {
	private Map<String, String> profiles;

	public Validator() {
		profiles = new HashMap<String, String>();
	}

	public Map<String, String> getProfiles() {
		return profiles;
	}
}
