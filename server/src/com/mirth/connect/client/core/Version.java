/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("version")
public enum Version {
    // @formatter:off

    /*
     * When a new version of Mirth Connect is released, do the following:
     * 1) Add the new version to the end of the list below (the list must be kept in historical order)
     * 2) Specify a Migrator class for the new version in the ServerMigrator class
     * 3) Add migration code/classes for any plugins that need to be migrated
     */

    V0("0"),
    V1("1"),
    V2("2"),
    V3("3"),
    V4("4"),
    V5("5"),
    V6("6"),
    V7("7"),
    V8("8"),
    V9("9", "2.2.0"),
    V3_0_0("3.0.0"),
    V3_0_1("3.0.1"),
    V3_0_2("3.0.2"),
    V3_0_3("3.0.3"),
    V3_1_0("3.1.0"),
    V3_1_1("3.1.1"),
    V3_2_0("3.2.0"),
    V3_2_1("3.2.1"),
    V3_2_2("3.2.2"),
    V3_3_0("3.3.0"),
    V3_3_1("3.3.1"),
    V3_3_2("3.3.2"),
    V3_4_0("3.4.0"),
    V3_4_1("3.4.1"),
    V3_4_2("3.4.2"),
    V3_5_0("3.5.0"),
    V3_5_1("3.5.1");
    
    // @formatter:on

    private String schemaVersion;
    private String versionString;

    private Version(String schemaVersion) {
        this.schemaVersion = schemaVersion;
        this.versionString = schemaVersion;
    }

    private Version(String schemaVersion, String versionString) {
        this.schemaVersion = schemaVersion;
        this.versionString = versionString;
    }

    public Version getNextVersion() {
        if (ordinal() < getLatest().ordinal()) {
            return values()[ordinal() + 1];
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return versionString;
    }

    public String toPackageString() {
        return "v" + versionString.replaceAll("\\.", "");
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public static Version getApiEarliest() {
        return V3_4_0;
    }

    public static Version getLatest() {
        Version[] allVersions = values();
        return allVersions[allVersions.length - 1];
    }

    public static Version fromString(String value) {
        for (Version version : values()) {
            if (version.getSchemaVersion().equals(value)) {
                return version;
            }
        }

        return null;
    }
}
