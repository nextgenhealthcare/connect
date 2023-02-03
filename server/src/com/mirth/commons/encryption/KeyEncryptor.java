package com.mirth.commons.encryption;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class KeyEncryptor extends Encryptor {
    private Key key;

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    @Override
    public synchronized void initialize() throws EncryptionException {
        if (!isInitialized()) {
            setInitialized(true);
        }
    }

    @Override
    public String encrypt(final String message) throws EncryptionException {
        if (message == null) {
            return null;
        }

        if (!isInitialized()) {
            initialize();
        }

        try {
            byte[] encrypted = encrypt(message.getBytes());

            if (getFormat() == Output.HEXADECIMAL) {
                return Hex.encodeHexString(encrypted);
            } else {
                return new String(Base64.encodeBase64Chunked(encrypted));
            }
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    private byte[] encrypt(final byte[] message) throws Exception {
        Cipher cipher = Cipher.getInstance(key.getAlgorithm(), getProvider());
        IvParameterSpec parameterSpec = new IvParameterSpec(new byte[cipher.getBlockSize()]);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
        return cipher.doFinal(message);
    }

    @Override
    public String decrypt(final String message) throws EncryptionException {
        if (message == null) {
            return null;
        }

        if (!isInitialized()) {
            initialize();
        }

        try {
            if (getFormat() == Output.HEXADECIMAL) {
                return new String(decrypt(Hex.decodeHex(message.toCharArray())));
            } else {
                return new String(decrypt(Base64.decodeBase64(message)));
            }
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    private byte[] decrypt(final byte[] message) throws Exception {
        Cipher cipher = Cipher.getInstance(key.getAlgorithm(), getProvider());
        IvParameterSpec parameterSpec = new IvParameterSpec(new byte[cipher.getBlockSize()]);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
        return cipher.doFinal(message);
    }
}
