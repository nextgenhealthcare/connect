package com.mirth.commons.encryption;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class KeyEncryptor extends Encryptor {

    public static final String ALGORITHM_PARAM = "alg=";
    public static final String CHARSET_PARAM = "cs=";
    public static final String IV_PARAM = "iv=";

    private Key key;
    private String algorithm;
    private String charset = "UTF-8";

    // Fallbacks to use when decrypting old messages that do not have indicators.
    private String fallbackAlgorithm;
    private String fallbackCharset = "UTF-8";

    private ObjectPool<SecureRandom> randomPool;
    private KeyedObjectPool<String, Cipher> cipherPool;

    public KeyEncryptor() {
        GenericObjectPoolConfig randomConfig = new GenericObjectPoolConfig();
        randomConfig.setMaxTotal(-1);
        randomConfig.setBlockWhenExhausted(false);
        randomPool = new GenericObjectPool<SecureRandom>(new SecureRandomFactory(), randomConfig);

        GenericKeyedObjectPoolConfig cipherConfig = new GenericKeyedObjectPoolConfig();
        cipherConfig.setMaxTotal(-1);
        cipherConfig.setMaxTotalPerKey(-1);
        cipherConfig.setBlockWhenExhausted(false);
        cipherPool = new GenericKeyedObjectPool<String, Cipher>(new CipherFactory(), cipherConfig);
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String getAlgorithm() {
        if (StringUtils.isBlank(algorithm) && key != null) {
            return key.getAlgorithm();
        }
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

    public String getFallbackAlgorithm() {
        return fallbackAlgorithm;
    }

    public void setFallbackAlgorithm(String fallbackAlgorithm) {
        this.fallbackAlgorithm = fallbackAlgorithm;
    }

    public String getFallbackCharset() {
        return fallbackCharset;
    }

    public void setFallbackCharset(String fallbackCharset) {
        if (StringUtils.isNotBlank(fallbackCharset)) {
            this.fallbackCharset = fallbackCharset;
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
            EncryptionResult result = encrypt(message.getBytes(getCharset()));

            // Add header, e.g. {alg=AES/CBC/PKCS5Padding,cs=UTF-8,iv=RrwzKW8JX9qn09im9r5ZpQ==}
            StringBuilder builder = new StringBuilder("{");
            builder.append(ALGORITHM_PARAM).append(getAlgorithm()).append(',');
            builder.append(CHARSET_PARAM).append(getCharset()).append(',');
            builder.append(IV_PARAM).append(format(result.iv, false)).append('}');
            builder.append(format(result.ciphertext, true));

            return builder.toString();
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    private EncryptionResult encrypt(final byte[] message) throws Exception {
        String algorithm = getAlgorithm();
        Cipher borrowedCipher = null;
        SecureRandom borrowedRandom = null;
        try {
            Cipher cipher = borrowedCipher = borrowCipher(algorithm);
            if (cipher == null) {
                cipher = createCipher(algorithm);
            }
            SecureRandom random = borrowedRandom = borrowRandom();
            if (random == null) {
                random = createRandom();
            }

            // Generate random bytes for IV
            byte[] iv = new byte[cipher.getBlockSize()];
            random.nextBytes(iv);
            AlgorithmParameterSpec parameterSpec;
            if (StringUtils.contains(algorithm, "GCM")) {
                parameterSpec = new GCMParameterSpec(128, iv);
            } else {
                parameterSpec = new IvParameterSpec(iv);
            }
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            // Do encryption
            byte[] encrypted = cipher.doFinal(message);

            return new EncryptionResult(iv, encrypted);
        } finally {
            if (borrowedCipher != null) {
                cipherPool.returnObject(algorithm, borrowedCipher);
            }
            if (borrowedRandom != null) {
                randomPool.returnObject(borrowedRandom);
            }
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
            String msg = message;
            String algorithm = StringUtils.defaultString(getFallbackAlgorithm(), getAlgorithm());
            String charset = StringUtils.defaultString(getFallbackCharset(), getCharset());
            byte[] iv = null;

            // Extract header, e.g. {alg=AES/CBC/PKCS5Padding,cs=UTF-8,iv=RrwzKW8JX9qn09im9r5ZpQ==}
            if (StringUtils.startsWith(msg, "{")) {
                msg = StringUtils.removeStart(msg, "{");

                msg = StringUtils.removeStart(msg, ALGORITHM_PARAM);
                int index = StringUtils.indexOf(msg, ',');
                algorithm = StringUtils.substring(msg, 0, index);
                msg = StringUtils.substring(msg, index + 1);

                msg = StringUtils.removeStart(msg, CHARSET_PARAM);
                index = StringUtils.indexOf(msg, ',');
                charset = StringUtils.substring(msg, 0, index);
                msg = StringUtils.substring(msg, index + 1);

                msg = StringUtils.removeStart(msg, IV_PARAM);
                index = StringUtils.indexOf(msg, '}');
                iv = unformat(StringUtils.substring(msg, 0, index));
                msg = StringUtils.substring(msg, index + 1);
            }

            return new String(decrypt(unformat(msg), algorithm, iv), charset);
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    private byte[] decrypt(final byte[] message, String algorithm, byte[] iv) throws Exception {
        Cipher borrowedCipher = null;
        try {
            Cipher cipher = borrowedCipher = borrowCipher(algorithm);
            if (cipher == null) {
                cipher = createCipher(algorithm);
            }

            if (iv == null) {
                iv = new byte[cipher.getBlockSize()];
            }

            AlgorithmParameterSpec parameterSpec;
            if (StringUtils.contains(algorithm, "GCM")) {
                parameterSpec = new GCMParameterSpec(128, iv);
            } else {
                parameterSpec = new IvParameterSpec(iv);
            }

            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
            return cipher.doFinal(message);
        } finally {
            if (borrowedCipher != null) {
                cipherPool.returnObject(algorithm, borrowedCipher);
            }
        }
    }

    private String format(byte[] data, boolean chunked) throws UnsupportedEncodingException {
        if (getFormat() == Output.HEXADECIMAL) {
            return Hex.encodeHexString(data);
        } else {
            if (chunked) {
                return new String(Base64.encodeBase64Chunked(data), getCharset());
            } else {
                return Base64.encodeBase64String(data);
            }
        }
    }

    private byte[] unformat(String data) throws UnsupportedEncodingException, DecoderException {
        if (getFormat() == Output.HEXADECIMAL) {
            return Hex.decodeHex(data.toCharArray());
        } else {
            return Base64.decodeBase64(data);
        }
    }

    private class EncryptionResult {
        private byte[] iv;
        private byte[] ciphertext;

        public EncryptionResult(byte[] iv, byte[] ciphertext) {
            this.iv = iv;
            this.ciphertext = ciphertext;
        }
    }

    private SecureRandom createRandom() {
        return new SecureRandom();
    }

    private SecureRandom borrowRandom() {
        try {
            return randomPool.borrowObject();
        } catch (Exception e) {
            // Ignore and return null, a new object will be created.
            return null;
        }
    }

    private class SecureRandomFactory extends BasePooledObjectFactory<SecureRandom> {
        @Override
        public SecureRandom create() throws Exception {
            return createRandom();
        }

        @Override
        public PooledObject<SecureRandom> wrap(SecureRandom random) {
            return new DefaultPooledObject<SecureRandom>(random);
        }
    }

    private Cipher createCipher(String algorithm) throws Exception {
        return Cipher.getInstance(algorithm, getProvider());
    }

    private Cipher borrowCipher(String algorithm) {
        try {
            return cipherPool.borrowObject(algorithm);
        } catch (Exception e) {
            // Ignore and return null, a new object will be created.
            return null;
        }
    }

    private class CipherFactory extends BaseKeyedPooledObjectFactory<String, Cipher> {
        @Override
        public Cipher create(String algorithm) throws Exception {
            return createCipher(algorithm);
        }

        @Override
        public PooledObject<Cipher> wrap(Cipher random) {
            return new DefaultPooledObject<Cipher>(random);
        }
    }
}
