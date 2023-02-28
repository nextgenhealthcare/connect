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

    public static final String HEADER_INDICATOR = "{" + ALGORITHM_PARAM;

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
            EncryptionResult result = doEncrypt(message.getBytes(getCharset()), true);

            // Add header, e.g. {alg=AES/CBC/PKCS5Padding,cs=UTF-8,iv=RrwzKW8JX9qn09im9r5ZpQ==}
            StringBuilder builder = buildHeader(result.iv);
            builder.append((String) result.ciphertext);

            return builder.toString();
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    @Override
    public EncryptedData encrypt(final byte[] data) throws EncryptionException {
        if (data == null) {
            return new EncryptedData(null, null);
        }

        if (!isInitialized()) {
            initialize();
        }

        try {
            EncryptionResult result = doEncrypt(data, false);

            // Add header, e.g. {alg=AES/CBC/PKCS5Padding,cs=UTF-8,iv=RrwzKW8JX9qn09im9r5ZpQ==}
            String header = buildHeader(result.iv).toString();

            return new EncryptedData(header, (byte[]) result.ciphertext);
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    private EncryptionResult doEncrypt(final byte[] message, boolean formatCipherText) throws Exception {
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

            return new EncryptionResult(iv, encrypted, formatCipherText);
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
            // Extract header, e.g. {alg=AES/CBC/PKCS5Padding,cs=UTF-8,iv=RrwzKW8JX9qn09im9r5ZpQ==}
            HeaderExtractionResult result = extractHeader(message);

            if (result.iv != null) {
                return new String(doDecrypt(unformat(StringUtils.substring(message, result.startIndex)), result.algorithm, result.iv), result.charset);
            } else {
                return new String(doDecrypt(unformat(message), result.algorithm, null), result.charset);
            }
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    @Override
    public byte[] decrypt(String header, byte[] data) throws EncryptionException {
        if (data == null) {
            return null;
        }

        if (!isInitialized()) {
            initialize();
        }

        try {
            // Extract header, e.g. {alg=AES/CBC/PKCS5Padding,cs=UTF-8,iv=RrwzKW8JX9qn09im9r5ZpQ==}
            HeaderExtractionResult result = extractHeader(header);
            return doDecrypt(data, result.algorithm, result.iv);
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    private byte[] doDecrypt(final byte[] message, String algorithm, byte[] iv) throws Exception {
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

    private StringBuilder buildHeader(String iv) {
        StringBuilder builder = new StringBuilder("{");
        builder.append(ALGORITHM_PARAM).append(getAlgorithm()).append(',');
        builder.append(CHARSET_PARAM).append(getCharset()).append(',');
        builder.append(IV_PARAM).append(iv).append('}');
        return builder;
    }

    private HeaderExtractionResult extractHeader(String message) throws Exception {
        String algorithm = StringUtils.defaultString(getFallbackAlgorithm(), getAlgorithm());
        String charset = StringUtils.defaultString(getFallbackCharset(), getCharset());
        byte[] iv = null;

        int startIndex = 0;
        if (message != null && message.length() > 0 && message.charAt(0) == '{') {
            startIndex = 1;
            int endIndex = StringUtils.indexOf(message, ALGORITHM_PARAM, 1);
            boolean found = false;

            if (startIndex == endIndex) {
                startIndex += ALGORITHM_PARAM.length();
                endIndex = StringUtils.indexOf(message, ',', startIndex);
                if (endIndex != -1) {
                    algorithm = message.substring(startIndex, endIndex);
                    found = true;
                    startIndex = endIndex + 1;
                }
            }

            if (found) {
                found = false;
                endIndex = StringUtils.indexOf(message, CHARSET_PARAM, startIndex);

                if (startIndex == endIndex) {
                    startIndex += CHARSET_PARAM.length();
                    endIndex = StringUtils.indexOf(message, ',', startIndex);
                    if (endIndex != -1) {
                        charset = message.substring(startIndex, endIndex);
                        found = true;
                        startIndex = endIndex + 1;
                    }
                }
            }

            if (found) {
                found = false;
                endIndex = StringUtils.indexOf(message, IV_PARAM, startIndex);

                if (startIndex == endIndex) {
                    startIndex += IV_PARAM.length();
                    endIndex = StringUtils.indexOf(message, '}', startIndex);
                    if (endIndex != -1) {
                        iv = unformat(message.substring(startIndex, endIndex));
                        found = true;
                        startIndex = endIndex + 1;
                    }
                }
            }

            if (!found) {
                throw new Exception("Encryption header information is malformed.");
            }
        }

        return new HeaderExtractionResult(startIndex, algorithm, charset, iv);
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
        private String iv;
        private Object ciphertext;

        public EncryptionResult(byte[] iv, byte[] ciphertext, boolean formatCipherText) throws UnsupportedEncodingException {
            this.iv = format(iv, false);
            this.ciphertext = formatCipherText ? format(ciphertext, true) : ciphertext;
        }
    }

    private class HeaderExtractionResult {
        private int startIndex;
        private String algorithm;
        private String charset;
        private byte[] iv;

        public HeaderExtractionResult(int startIndex, String algorithm, String charset, byte[] iv) {
            this.startIndex = startIndex;
            this.algorithm = algorithm;
            this.charset = charset;
            this.iv = iv;
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
