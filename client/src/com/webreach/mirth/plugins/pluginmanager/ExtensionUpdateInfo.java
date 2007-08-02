package com.webreach.mirth.plugins.pluginmanager;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("extensionUpdateInfo")
public class ExtensionUpdateInfo {
	private String id;
	private String name;
	private String version;
	private String mirthVersion;
	private String downloadUrl;
	public String getDownloadUrl() {
		return downloadUrl;
	}
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	public String getMirthVersion() {
		return mirthVersion;
	}
	public void setMirthVersion(String mirthVersion) {
		this.mirthVersion = mirthVersion;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
