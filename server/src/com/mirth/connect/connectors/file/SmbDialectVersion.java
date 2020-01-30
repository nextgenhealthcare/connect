package com.mirth.connect.connectors.file;

public class SmbDialectVersion {
	private String version;
	private String readableVersion;
	
	public SmbDialectVersion(String version, String readableVersion) {
		this.setVersion(version);
		this.setReadableVersion(readableVersion);
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getReadableVersion() {
		return readableVersion;
	}

	public void setReadableVersion(String readableVersion) {
		this.readableVersion = readableVersion;
	}
	
	@Override
	public String toString() {
		return readableVersion;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof SmbDialectVersion) {
			SmbDialectVersion otherDialectVersion = (SmbDialectVersion) obj;
			return version.equals(otherDialectVersion.getVersion());
		}
		return false;
	}
}
