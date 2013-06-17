package com.mirth.connect.server.migration;

public enum Version {
    /*
     * When a new version of Mirth Connect requires migration code, the version must be
     * added to the version list here and the appropriate Migrator implementation must be
     * specified in the switch statement in getMigrator() below. The version list must be kept
     * in historical order.
     */

    // @formatter:off
    
    V0("0"),
    V1("1"),
    V2("2"),
    V3("3"),
    V4("4"),
    V5("5"),
    V6("6"),
    V7("7"),
    V8("8"),
    V9("9"),
    V3_0_0("3.0.0");

    public ServerMigrator getMigrator() {
        switch (this) {
            case V0: return new LegacyMigrator(0);
            case V1: return new LegacyMigrator(1);
            case V2: return new LegacyMigrator(2);
            case V3: return new LegacyMigrator(3);
            case V4: return new LegacyMigrator(4);
            case V5: return new LegacyMigrator(5);
            case V6: return new LegacyMigrator(6);
            case V7: return new Migrate2_0_0();
            case V8: return new LegacyMigrator(8);
            case V9: return new LegacyMigrator(9);
            case V3_0_0: return new Migrate3_0_0();
        }

        return null;
    }
    
    // @formatter:on

    private String versionString;

    private Version(String value) {
        this.versionString = value;
    }

    public boolean nextVersionExists() {
        return ordinal() < getLatest().ordinal();
    }

    public Version getNextVersion() {
        return values()[ordinal() + 1];
    }

    @Override
    public String toString() {
        return versionString;
    }

    public static Version getLatest() {
        Version[] allVersions = values();
        return allVersions[allVersions.length - 1];
    }

    public static Version fromString(String value) {
        for (Version version : values()) {
            if (version.toString().equals(value)) {
                return version;
            }
        }

        return null;
    }
}
