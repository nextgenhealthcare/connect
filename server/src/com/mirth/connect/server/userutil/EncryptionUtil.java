/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import com.mirth.commons.encryption.EncryptionException;
import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.server.controllers.ControllerFactory;

/**
 * This utility class provides some convenience methods for encrypting or decrypting data.
 */
public class EncryptionUtil {

    /**
     * Convenience method for encrypting data. Uses the currently configured encryption settings.
     * 
     * @param data
     *            The data to encrypt, as a String.
     * @return The encrypted data.
     * @throws EncryptionException
     *             If the data cannot be encrypted for any reason.
     */
    public static String encrypt(String data) throws EncryptionException {
        return getEncryptor().encrypt(data);
    }

    /**
     * Convenience method for encrypting data. Uses the currently configured encryption settings.
     * 
     * @param data
     *            The data to encrypt, as a raw byte array.
     * @return An {@link EncryptedData} object containing the header information and encrypted data.
     * @throws EncryptionException
     *             If the data cannot be encrypted for any reason.
     */
    public static EncryptedData encrypt(byte[] data) throws EncryptionException {
        com.mirth.commons.encryption.Encryptor.EncryptedData result = getEncryptor().encrypt(data);
        return new EncryptedData(result.getHeader(), result.getEncryptedData());
    }

    /**
     * Convenience method for decrypting data. Uses the currently configured encryption and fallback
     * settings.
     * 
     * @param data
     *            The data to decrypt, as a String.
     * @return The decrypted data.
     * @throws EncryptionException
     *             If the data cannot be decrypted for any reason.
     */
    public static String decrypt(String data) throws EncryptionException {
        return getEncryptor().decrypt(data);
    }

    /**
     * Convenience method for decrypting data. Uses the currently configured encryption and fallback
     * settings.
     * 
     * @param header
     *            The meta-information about the encrypted data. This is a specially-formatted
     *            string returned from the {@link #encrypt(byte[]) method.
     * @param data
     *            The data to decrypt, as a raw byte array.
     * @return The decrypted data.
     * @throws EncryptionException
     *             If the data cannot be decrypted for any reason.
     */
    public static byte[] decrypt(String header, byte[] data) throws EncryptionException {
        return getEncryptor().decrypt(header, data);
    }

    private static Encryptor getEncryptor() {
        return ControllerFactory.getFactory().createConfigurationController().getEncryptor();
    }
}
