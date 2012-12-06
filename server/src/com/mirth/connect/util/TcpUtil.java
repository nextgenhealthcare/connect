/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;

public class TcpUtil {

    public static final String DEFAULT_LLP_START_BYTES = "0B"; // <VT>
    public static final String DEFAULT_LLP_END_BYTES = "1C0D"; // <CR><LF>

    public static boolean isValidHexString(String str) {
        return str.matches("^[0-9A-F]*$");
    }

    public static String convertHexToAbbreviation(String str) {
        if (isValidHexString(str)) {
            return getByteAbbreviation(stringToByteArray(str));
        } else {
            return "Invalid Hex";
        }
    }

    public static String getByteAbbreviation(byte[] bytes) {
        String abbreviation = "";

        for (byte b : bytes) {
            //@formatter:off
            switch (b) {
                case 0x00: abbreviation += "<NUL>"; break;
                case 0x01: abbreviation += "<SOH>"; break;
                case 0x02: abbreviation += "<STX>"; break;
                case 0x03: abbreviation += "<ETX>"; break;
                case 0x04: abbreviation += "<EOT>"; break;
                case 0x05: abbreviation += "<ENQ>"; break;
                case 0x06: abbreviation += "<ACK>"; break;
                case 0x07: abbreviation += "<BEL>"; break;
                case 0x08: abbreviation += "<BS>"; break;
                case 0x09: abbreviation += "<TAB>"; break;
                case 0x0A: abbreviation += "<LF>"; break;
                case 0x0B: abbreviation += "<VT>"; break;
                case 0x0C: abbreviation += "<FF>"; break;
                case 0x0D: abbreviation += "<CR>"; break;
                case 0x0E: abbreviation += "<SO>"; break;
                case 0x0F: abbreviation += "<SI>"; break;
                case 0x10: abbreviation += "<DLE>"; break;
                case 0x11: abbreviation += "<DC1>"; break;
                case 0x12: abbreviation += "<DC2>"; break;
                case 0x13: abbreviation += "<DC3>"; break;
                case 0x14: abbreviation += "<DC4>"; break;
                case 0x15: abbreviation += "<NAK>"; break;
                case 0x16: abbreviation += "<SYN>"; break;
                case 0x17: abbreviation += "<ETB>"; break;
                case 0x18: abbreviation += "<CAN>"; break;
                case 0x19: abbreviation += "<EM>"; break;
                case 0x1A: abbreviation += "<SUB>"; break;
                case 0x1B: abbreviation += "<ESC>"; break;
                case 0x1C: abbreviation += "<FS>"; break;
                case 0x1D: abbreviation += "<GS>"; break;
                case 0x1E: abbreviation += "<RS>"; break;
                case 0x1F: abbreviation += "<US>"; break;
                case 0x20: abbreviation += "<Space>"; break;
                case 0x7F: abbreviation += "<DEL>"; break;
                default: abbreviation += new String(new byte[] { b }); break;
            }
            //@formatter:on
        }

        return abbreviation;
    }

    /*
     * Converts a string to an integer. If the string is null or contains no
     * digits, then zero is returned.
     */
    public static int parseInt(String str) {
        if (StringUtils.isBlank(str)) {
            return 0;
        } else {
            String replacedStr = str.replaceAll("[^0-9]", "");
            if (StringUtils.isBlank(replacedStr)) {
                return 0;
            } else {
                return Integer.parseInt(replacedStr, 10);
            }
        }
    }

    public static byte[] stringToByteArray(String str) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        if (StringUtils.isNotBlank(str)) {
            String hexString = str.toUpperCase().replaceAll("[^0-9A-F]", "");

            for (String hexByte : ((hexString.length() % 2 > 0 ? "0" : "") + hexString).split("(?<=\\G..)")) {
                bytes.write((byte) ((Character.digit(hexByte.charAt(0), 16) << 4) + Character.digit(hexByte.charAt(1), 16)));
            }
        }

        return bytes.toByteArray();
    }

    public static String getFixedHost(String host) {
        if (host == null || host.length() == 0) {
            return "localhost";
        }
        return host;
    }
}
