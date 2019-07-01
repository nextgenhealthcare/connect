/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

/**
 * Utility class to create unique identifiers.
 */
public class UUIDGenerator {
    private UUIDGenerator() {}

    /**
     * Returns a type 4 (pseudo randomly generated) UUID. The UUID is generated using a
     * cryptographically strong pseudo random number generator.
     * 
     * @return The UUID string.
     */
    public static String getUUID() {
        return com.mirth.connect.server.util.ServerUUIDGenerator.getUUID();
    }
}
