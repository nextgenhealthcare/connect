package com.mirth.connect.connectors.file;

import java.util.HashMap;
import java.util.Map;

import jcifs.DialectVersion;

public class SmbSchemeProperties extends SchemeProperties {
	private static final String DEFAULT_SMB_VERSION = DialectVersion.SMB311.toString();
	
	private String smbVersion;
	private static final SmbDialectVersion[] supportedVersions = new SmbDialectVersion[] {
			new SmbDialectVersion(DialectVersion.SMB1.toString(), "SMB v1"),
			new SmbDialectVersion(DialectVersion.SMB202.toString(), "SMB v2.02"),
			new SmbDialectVersion(DialectVersion.SMB210.toString(), "SMB v2.1"),
			new SmbDialectVersion(DialectVersion.SMB300.toString(), "SMB v3.0"),
			new SmbDialectVersion(DialectVersion.SMB302.toString(), "SMB v3.0.2"),
			new SmbDialectVersion(DialectVersion.SMB311.toString(), "SMB v3.1.1")
		};
	
	public SmbSchemeProperties() {
		smbVersion = DEFAULT_SMB_VERSION;
	}
	
	public SmbSchemeProperties(SmbSchemeProperties props) {
		smbVersion = props.getSmbVersion();
	}
	
	public static SmbDialectVersion[] getSupportedVersions() {
		return supportedVersions;
	}
	
	public static String getReadableVersion(String dialectVersion) {
		for (SmbDialectVersion smbDialectVersion : supportedVersions) {
			if (smbDialectVersion.getVersion().equals(dialectVersion)) {
				return smbDialectVersion.getReadableVersion();
			}
		}
		return null;
	}
	
	public static SmbDialectVersion getSmbDialectVersion(String dialectVersion) {
		for (SmbDialectVersion smbDialectVersion : supportedVersions) {
			if (smbDialectVersion.getVersion().equals(dialectVersion)) {
				return smbDialectVersion;
			}
		}
		return null;
	}
	
	public String getSmbVersion() {
		return smbVersion;
	}

	public void setSmbVersion(String smbVersion) {
		this.smbVersion = smbVersion;
	}

	@Override
	public Map<String, Object> getPurgedProperties() {
		Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("smbVersion", smbVersion);
        return purgedProperties;
	}

	@Override
	public SchemeProperties getFileSchemeProperties() {
		return this;
	}

	@Override
	public String getSummaryText() {
		return "Using " + getReadableVersion(smbVersion);
	}

	@Override
	public String toFormattedString() {
		return smbVersion;
	}

	@Override
	public SchemeProperties clone() {
		return new SmbSchemeProperties(this);
	}
}
