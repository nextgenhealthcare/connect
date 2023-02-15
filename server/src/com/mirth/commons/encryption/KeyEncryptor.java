package com.mirth.commons.encryption;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

public class KeyEncryptor extends Encryptor {

    public static final String IV_HEADER = "{iv}";

    private Key key;
    private String algorithm;
    private String charset = "UTF-8";

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        if (StringUtils.isNotBlank(charset)) {
            this.charset = charset;
        }
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
            byte[] encrypted = encrypt(message.getBytes(getCharset()));

            StringBuilder builder = new StringBuilder(IV_HEADER);

            if (getFormat() == Output.HEXADECIMAL) {
                builder.append(Hex.encodeHexString(encrypted));
            } else {
                builder.append(new String(Base64.encodeBase64Chunked(encrypted), getCharset()));
            }

            return builder.toString();
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    private byte[] encrypt(final byte[] message) throws Exception {
        String algorithm = StringUtils.defaultIfBlank(getAlgorithm(), key.getAlgorithm());
        Cipher cipher = Cipher.getInstance(algorithm, getProvider());

        // Generate random bytes for IV
        byte[] iv = new byte[cipher.getBlockSize()];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec parameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        // Do encryption
        byte[] encrypted = cipher.doFinal(message);

        // Include both IV + encrypted in final byte array
        byte[] finalBytes = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, finalBytes, 0, iv.length);
        System.arraycopy(encrypted, 0, finalBytes, iv.length, encrypted.length);
        return finalBytes;
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
            String msg = message;
            boolean extractIV = StringUtils.startsWith(msg, IV_HEADER);
            if (extractIV) {
                msg = msg.substring(IV_HEADER.length());
            }

            if (getFormat() == Output.HEXADECIMAL) {
                return new String(decrypt(Hex.decodeHex(msg.toCharArray()), extractIV), getCharset());
            } else {
                return new String(decrypt(Base64.decodeBase64(msg), extractIV), getCharset());
            }
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    private byte[] decrypt(final byte[] message, boolean extractIV) throws Exception {
        String algorithm = StringUtils.defaultIfBlank(getAlgorithm(), key.getAlgorithm());
        Cipher cipher = Cipher.getInstance(algorithm, getProvider());

        byte[] iv = new byte[cipher.getBlockSize()];
        byte[] encrypted = message;

        if (extractIV) {
            // Extract the first n bytes of the data as the IV
            encrypted = new byte[message.length - iv.length];
            System.arraycopy(message, 0, iv, 0, iv.length);
            System.arraycopy(message, iv.length, encrypted, 0, message.length - iv.length);
        }

        IvParameterSpec parameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
        return cipher.doFinal(encrypted);
    }
}
