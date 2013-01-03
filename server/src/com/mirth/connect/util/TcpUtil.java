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
        StringBuilder builder = new StringBuilder();

        for (byte b : bytes) {
            //@formatter:off
            switch (b) {
                case 0x00: builder.append("<NUL>"); break;
                case 0x01: builder.append("<SOH>"); break;
                case 0x02: builder.append("<STX>"); break;
                case 0x03: builder.append("<ETX>"); break;
                case 0x04: builder.append("<EOT>"); break;
                case 0x05: builder.append("<ENQ>"); break;
                case 0x06: builder.append("<ACK>"); break;
                case 0x07: builder.append("<BEL>"); break;
                case 0x08: builder.append("<BS>"); break;
                case 0x09: builder.append("<TAB>"); break;
                case 0x0A: builder.append("<LF>"); break;
                case 0x0B: builder.append("<VT>"); break;
                case 0x0C: builder.append("<FF>"); break;
                case 0x0D: builder.append("<CR>"); break;
                case 0x0E: builder.append("<SO>"); break;
                case 0x0F: builder.append("<SI>"); break;
                case 0x10: builder.append("<DLE>"); break;
                case 0x11: builder.append("<DC1>"); break;
                case 0x12: builder.append("<DC2>"); break;
                case 0x13: builder.append("<DC3>"); break;
                case 0x14: builder.append("<DC4>"); break;
                case 0x15: builder.append("<NAK>"); break;
                case 0x16: builder.append("<SYN>"); break;
                case 0x17: builder.append("<ETB>"); break;
                case 0x18: builder.append("<CAN>"); break;
                case 0x19: builder.append("<EM>"); break;
                case 0x1A: builder.append("<SUB>"); break;
                case 0x1B: builder.append("<ESC>"); break;
                case 0x1C: builder.append("<FS>"); break;
                case 0x1D: builder.append("<GS>"); break;
                case 0x1E: builder.append("<RS>"); break;
                case 0x1F: builder.append("<US>"); break;
                case 0x20: builder.append("<Space>"); break;
                case 0x7F: builder.append("<DEL>"); break;
                default: builder.append(new String(new byte[] { b })); break;
            }
            //@formatter:on
        }

        return builder.toString();
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
