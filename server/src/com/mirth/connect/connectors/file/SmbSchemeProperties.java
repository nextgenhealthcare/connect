package com.mirth.connect.connectors.file;

import java.util.HashMap;
import java.util.Map;

import jcifs.DialectVersion;

public class SmbSchemeProperties extends SchemeProperties {
	private static final String DEFAULT_SMB_MIN_VERSION = DialectVersion.SMB202.toString();
	private static final String DEFAULT_SMB_MAX_VERSION = DialectVersion.SMB311.toString();
	
	private String smbMinVersion;
	private String smbMaxVersion;
	private static final SmbDialectVersion[] supportedVersions = new SmbDialectVersion[] {
			new SmbDialectVersion(DialectVersion.SMB1.toString(), "SMB v1"),
			new SmbDialectVersion(DialectVersion.SMB202.toString(), "SMB v2.0.2"),
			new SmbDialectVersion(DialectVersion.SMB210.toString(), "SMB v2.1"),
			new SmbDialectVersion(DialectVersion.SMB300.toString(), "SMB v3.0"),
			new SmbDialectVersion(DialectVersion.SMB302.toString(), "SMB v3.0.2"),
			new SmbDialectVersion(DialectVersion.SMB311.toString(), "SMB v3.1.1")
		};
	
	public SmbSchemeProperties() {
		smbMinVersion = DEFAULT_SMB_MIN_VERSION;
		smbMaxVersion = DEFAULT_SMB_MAX_VERSION;
	}
	
	public SmbSchemeProperties(SmbSchemeProperties props) {
		smbMinVersion = props.getSmbMinVersion();
		smbMaxVersion = props.getSmbMaxVersion();
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
	
	public String getSmbMinVersion() {
		return smbMinVersion;
	}

	public void setSmbMinVersion(String smbMinVersion) {
		this.smbMinVersion = smbMinVersion;
	}
	
	public String getSmbMaxVersion() {
		return smbMaxVersion;
	}

	public void setSmbMaxVersion(String smbMaxVersion) {
		this.smbMaxVersion = smbMaxVersion;
	}

	@Override
	public Map<String, Object> getPurgedProperties() {
		Map<String, Object> purgedProperties = new HashMap<String, Object>();
		purgedProperties.put("smbMinVersion", smbMinVersion);
		purgedProperties.put("smbMaxVersion", smbMaxVersion);
        return purgedProperties;
	}

	@Override
	public SchemeProperties getFileSchemeProperties() {
		return this;
	}

	@Override
	public String getSummaryText() {
		return "Using " + getReadableVersion(smbMinVersion) + " - " + getReadableVersion(smbMaxVersion);
	}

	@Override
	public String toFormattedString() {
		return "SMB: " + getReadableVersion(smbMinVersion) + "-" + getReadableVersion(smbMaxVersion);
	}

	@Override
	public SchemeProperties clone() {
		return new SmbSchemeProperties(this);
	}
}
