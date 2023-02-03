package com.mirth.commons.encryption;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

public class Digester {
    public static final int DEFAULT_SALT_SIZE = 8;
    public static final int DEFAULT_ITERATIONS = 1000;

    private String algorithm = "MD5";
    private Provider provider;
    private Output format = Output.BASE64;
    private boolean initialized = false;

    private SecureRandom saltGenerator;
    private int saltSizeBytes = DEFAULT_SALT_SIZE;
    private int iterations = DEFAULT_ITERATIONS;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Output getFormat() {
        return format;
    }

    public void setFormat(Output format) {
        this.format = format;
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

    public boolean isInitialized() {
        return initialized;
    }

    protected void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public void initialize() throws EncryptionException {
        if (!isInitialized()) {
            try {
                saltGenerator = SecureRandom.getInstance("SHA1PRNG");
            } catch (NoSuchAlgorithmException e) {
                throw new EncryptionException(e);
            }

            setInitialized(true);
        }
    }

    public String digest(final String message) throws EncryptionException {
        if (message == null) {
            return null;
        }

        try {
            if (!isInitialized()) {
                initialize();
            }

            byte[] salt = saltGenerator.generateSeed(saltSizeBytes);
            byte[] digest = digest(message.getBytes(), salt);

            if (format == Output.HEXADECIMAL) {
                return Hex.encodeHexString(digest);
            } else {
                return new String(Base64.encodeBase64Chunked(digest));
            }
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    private byte[] digest(final byte[] message, final byte[] salt) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm, provider);
        messageDigest.reset();
        messageDigest.update(salt);
        messageDigest.update(message);

        byte[] digestBytes = messageDigest.digest();

        for (int i = 0; i < iterations - 1; i++) {
            messageDigest.reset();
            digestBytes = messageDigest.digest(digestBytes);
        }

        return ArrayUtils.addAll(salt, digestBytes);
    }

    public boolean matches(final String message, final String digest) throws EncryptionException {
        try {
            byte[] digestBytes = null;

            if (format == Output.HEXADECIMAL) {
                digestBytes = Hex.decodeHex(digest.toCharArray());
            } else {
                digestBytes = Base64.decodeBase64(digest.getBytes());
            }

            return matches(message.getBytes(), digestBytes);
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    private boolean matches(final byte[] message, final byte[] digest) throws Exception {
        byte[] salt = new byte[saltSizeBytes];
        System.arraycopy(digest, 0, salt, 0, saltSizeBytes);
        return Arrays.equals(digest(message, salt), digest);
    }
}
