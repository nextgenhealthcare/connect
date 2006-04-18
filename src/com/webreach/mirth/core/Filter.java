package com.webreach.mirth.core;

public class Filter implements Script {
	private String script;

	public Filter() {
		
	}
	
	public Filter(String script) {
		this.script = script;
	}
	
	public void setScript(String script) {
		this.script = script;
	}
	
	public String getScript() {
		return script;
	}

}
