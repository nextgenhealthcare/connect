package com.webreach.mirth.model;

public class DriverInfo {
	private String className;
	private String name;

	public DriverInfo() {

	}

	public DriverInfo(String name, String className) {
		this.name = name;
		this.className = className;
	}

	public String getClassName() {
		return this.className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Driver[");
		builder.append("name=" + getName() + ", ");
		builder.append("className=" + getClassName());
		builder.append("]");
		return builder.toString();
	}
}
