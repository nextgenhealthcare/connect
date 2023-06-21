package com.mirth.commons.encryption;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

public class Digester {
    public static final int DEFAULT_SALT_SIZE = 8;
    public static final int DEFAULT_ITERATIONS = 600000;
    public static final int DEFAULT_KEY_SIZE_BITS = 256;

    private String algorithm = "PBKDF2WithHmacSHA256";
    private Provider provider;
    private Output format = Output.BASE64;
    private boolean initialized = false;

    private SecureRandom saltGenerator;
    private int saltSizeBytes = DEFAULT_SALT_SIZE;
    private int iterations = DEFAULT_ITERATIONS;
    private boolean usePBE = true;
    private int keySizeBits = DEFAULT_KEY_SIZE_BITS;

    // Typically shouldn't need to be changed
    private Charset charset = Charset.forName(System.getProperty("mirth.digester.charset", StandardCharsets.UTF_8.name()));

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

    public boolean isUsePBE() {
        return usePBE;
    }

    public void setUsePBE(boolean usePBE) {
        this.usePBE = usePBE;
    }

    public int getKeySizeBits() {
        return keySizeBits;
    }

    public void setKeySizeBits(int keySizeBits) {
        this.keySizeBits = keySizeBits;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
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
            byte[] digest = digest(message, salt);

            if (format == Output.HEXADECIMAL) {
                return Hex.encodeHexString(digest);
            } else {
                return new String(Base64.encodeBase64Chunked(digest), charset);
            }
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    private byte[] digest(final String message, final byte[] salt) throws Exception {
        byte[] digestBytes;

        if (StringUtils.startsWithIgnoreCase(algorithm, "Argon2")) {
            int mode = Argon2Parameters.ARGON2_id;
            if (StringUtils.equalsIgnoreCase(algorithm, "Argon2i")) {
                mode = Argon2Parameters.ARGON2_i;
            } else if (StringUtils.equalsIgnoreCase(algorithm, "Argon2d")) {
                mode = Argon2Parameters.ARGON2_d;
            }

            Argon2Parameters.Builder builder = (new Argon2Parameters.Builder(mode)).withVersion(Argon2Parameters.ARGON2_VERSION_13).withIterations(iterations).withMemoryAsKB(12288).withParallelism(1).withSalt(salt);
            Argon2BytesGenerator gen = new Argon2BytesGenerator();
            gen.init(builder.build());
            digestBytes = new byte[32];
            gen.generateBytes(message.getBytes(charset), digestBytes);
        } else if (usePBE) {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm, provider);
            KeySpec keySpec = new PBEKeySpec(message.toCharArray(), salt, iterations, keySizeBits);
            digestBytes = keyFactory.generateSecret(keySpec).getEncoded();
        } else {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm, provider);
            messageDigest.reset();
            messageDigest.update(salt);
            messageDigest.update(message.getBytes(charset));

            digestBytes = messageDigest.digest();

            for (int i = 0; i < iterations - 1; i++) {
                messageDigest.reset();
                digestBytes = messageDigest.digest(digestBytes);
            }
        }

        return ArrayUtils.addAll(salt, digestBytes);
    }

    public boolean matches(final String message, final String digest) throws EncryptionException {
        try {
            byte[] digestBytes = null;

            if (format == Output.HEXADECIMAL) {
                digestBytes = Hex.decodeHex(digest.toCharArray());
            } else {
                digestBytes = Base64.decodeBase64(digest.getBytes(charset));
            }

            return matches(message, digestBytes);
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    private boolean matches(final String message, final byte[] digest) throws Exception {
        byte[] salt = new byte[saltSizeBytes];
        System.arraycopy(digest, 0, salt, 0, saltSizeBytes);
        return Arrays.equals(digest(message, salt), digest);
    }
}
