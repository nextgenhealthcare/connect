/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api;

import io.swagger.config.Scanner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mirth.connect.client.core.Version;

public class ScannerFactory {

    private static Scanner DEFAULT_SCANNER;
    private static Map<Version, Scanner> scannerMap = new ConcurrentHashMap<Version, Scanner>();

    public static Scanner getScanner(Version version) {
        if (version == null) {
            return DEFAULT_SCANNER;
        }
        Scanner scanner = scannerMap.get(version);
        return scanner != null ? scanner : DEFAULT_SCANNER;
    }

    public static void setScanner(Version version, Scanner scanner) {
        if (version == null) {
            DEFAULT_SCANNER = scanner;
        } else {
            scannerMap.put(version, scanner);
        }
    }
}