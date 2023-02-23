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
     *            The data to encrypt.
     * @return The encrypted data.
     * @throws EncryptionException
     *             If the data cannot be encrypted for any reason.
     */
    public static String encrypt(String data) throws EncryptionException {
        return getEncryptor().encrypt(data);
    }

    /**
     * Convenience method for decrypting data. Uses the currently configured encryption and fallback
     * settings.
     * 
     * @param data
     *            The data to decrypt.
     * @return The decrypted data.
     * @throws EncryptionException
     *             If the data cannot be decrypted for any reason.
     */
    public static String decrypt(String data) throws EncryptionException {
        return getEncryptor().encrypt(data);
    }

    private static Encryptor getEncryptor() {
        return ControllerFactory.getFactory().createConfigurationController().getEncryptor();
    }
}
