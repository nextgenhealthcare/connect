package com.webreach.mirth.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Transformer implements Script {
	public enum Type {
		MAP, SCRIPT, XSLT
	};

	public enum Language {
		JAVASCRIPT, PYTHON, TCL
	};

	private Map<String, String> variables;
	private Type type;
	private Language language;

	public Transformer() {
		variables = new HashMap<String, String>();
		setType(Type.SCRIPT);
		setLanguage(Language.JAVASCRIPT);
	}

	public Transformer(Type type) {
		variables = new HashMap<String, String>();
		setType(type);
		setLanguage(Language.JAVASCRIPT);
	}

	public Transformer(Type type, Language language) {
		variables = new HashMap<String, String>();
		setType(type);
		setLanguage(language);
	}

	public Map getVariables() {
		return variables;
	}

	public Language getLanguage() {
		return this.language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public String getScript() {
		StringBuffer script = new StringBuffer();

		if (getType() == Type.MAP) {
			for (Iterator iter = variables.entrySet().iterator(); iter.hasNext();) {
				Entry entry = (Entry) iter.next();
				script.append("map.put('" + entry.getKey().toString() + "', " + entry.getValue().toString() + ");");
				script.append("\n");
			}
		} else if (getType() == Type.SCRIPT) {
			script.append(variables.get("script").toString());
		} else if (getType() == Type.XSLT) {
			script.append(variables.get("xslt").toString());
		}

		return script.toString().trim();
	}
}
