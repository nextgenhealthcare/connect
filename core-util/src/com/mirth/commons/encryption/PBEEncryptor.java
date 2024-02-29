package com.mirth.commons.encryption;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

public class PBEEncryptor extends Encryptor {
    public static final int DEFAULT_SALT_SIZE = 8;
    public static final int DEFAULT_ITERATIONS = 5000;

    private SecretKey key;

    private String algorithm;
    private String password;
    private SecureRandom saltGenerator;
    private int saltSizeBytes = DEFAULT_SALT_SIZE;
    private int iterations = DEFAULT_ITERATIONS;
    private boolean includeSalt = true;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SecureRandom getSaltGenerator() {
        return saltGenerator;
    }

    public void setSaltGenerator(SecureRandom saltGenerator) {
        this.saltGenerator = saltGenerator;
    }

    public int getSaltSizeBytes() {
        return saltSizeBytes;
    }

    public void setSaltSizeBytes(int saltSizeBytes) {
        this.saltSizeBytes = saltSizeBytes;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public boolean isIncludeSalt() {
        return includeSalt;
    }

    public void setIncludeSalt(boolean includeSalt) {
        this.includeSalt = includeSalt;
    }

    @Override
    public synchronized void initialize() throws EncryptionException {
        if (!isInitialized()) {
            try {
                PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
                SecretKeyFactory factory = SecretKeyFactory.getInstance(getAlgorithm(), getProvider());
                key = factory.generateSecret(pbeKeySpec);
                saltGenerator = SecureRandom.getInstance("SHA1PRNG");
            } catch (Exception e) {
                throw new EncryptionException(e);
            }

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
            byte[] encrypted = doEncrypt(message.getBytes());

            if (getFormat() == Output.HEXADECIMAL) {
                return Hex.encodeHexString(encrypted);
            } else {
                return new String(Base64.encodeBase64Chunked(encrypted));
            }
        } catch (Exception e) {
            throw new EncryptionException(e);
        }

    }

    @Override
    public EncryptedData encrypt(final byte[] data) throws EncryptionException {
        throw new UnsupportedOperationException();
    }

    private byte[] doEncrypt(final byte[] message) throws Exception {
        byte[] salt = saltGenerator.generateSeed(saltSizeBytes);
        PBEParameterSpec parameterSpec = new PBEParameterSpec(salt, iterations);
        Cipher cipher = Cipher.getInstance(getAlgorithm(), getProvider());
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
        byte[] encrypted = cipher.doFinal(message);

        if (includeSalt) {
            return ArrayUtils.addAll(salt, encrypted);
        } else {
            return encrypted;
        }
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

    @Override
    public byte[] decrypt(String header, byte[] data) throws EncryptionException {
        throw new UnsupportedOperationException();
    }

    private byte[] decrypt(final byte[] message) throws Exception {
        byte[] salt = new byte[saltSizeBytes];
        System.arraycopy(message, 0, salt, 0, saltSizeBytes);

        byte[] kernel = new byte[message.length - saltSizeBytes];
        System.arraycopy(message, saltSizeBytes, kernel, 0, kernel.length);

        PBEParameterSpec parameterSpec = new PBEParameterSpec(salt, iterations);
        Cipher cipher = Cipher.getInstance(getAlgorithm(), getProvider());
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
        return cipher.doFinal(kernel);
    }
}
