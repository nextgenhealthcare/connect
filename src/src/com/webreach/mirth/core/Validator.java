package com.webreach.mirth.core;

import java.util.HashMap;
import java.util.Map;

public class Validator {
	private Map<String, String> profiles;
	
	public Validator() {
		profiles = new HashMap<String, String>();
	}
	
	public Map<String, String> getProfiles() {
		return profiles;
	}
}
