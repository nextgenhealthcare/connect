/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server;

public interface Encryptor {

    public static final String HEADER_INDICATOR = "{alg=";

    public String encrypt(String text);

    public EncryptedData encrypt(byte[] data);

    public String decrypt(String text);

    public byte[] decrypt(String header, byte[] data);

    public class EncryptedData {
        private String header;
        private byte[] encryptedData;

        public EncryptedData(String header, byte[] encryptedData) {
            this.header = header;
            this.encryptedData = encryptedData;
        }

        public String getHeader() {
            return header;
        }

        public byte[] getEncryptedData() {
            return encryptedData;
        }
    }
}
