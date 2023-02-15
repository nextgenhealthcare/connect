package com.mirth.commons.encryption.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.Provider;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

import com.mirth.commons.encryption.KeyEncryptor;
import com.mirth.commons.encryption.Output;
import com.mirth.commons.encryption.util.EncryptionUtil;
import com.mirth.connect.model.EncryptionSettings;
import com.sun.crypto.provider.SunJCE;

public class KeyEncryptorTest {

    @Test
    public void testAESCBC128BC() throws Exception {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("AES/CBC/PKCS5Padding");
        encryptionSettings.setEncryptionKeyLength(128);
        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        testEncryptAndDecrypt(encryptionSettings);
    }

    @Test
    public void testAESCBC256BC() throws Exception {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("AES/CBC/PKCS5Padding");
        encryptionSettings.setEncryptionKeyLength(256);
        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        testEncryptAndDecrypt(encryptionSettings);
    }

    @Test
    public void testAESCBC128SunJCE() throws Exception {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("AES/CBC/PKCS5Padding");
        encryptionSettings.setEncryptionKeyLength(128);
        encryptionSettings.setSecurityProvider(SunJCE.class.getName());
        testEncryptAndDecrypt(encryptionSettings);
    }

    @Test
    public void testAESCBC256SunJCE() throws Exception {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("AES/CBC/PKCS5Padding");
        encryptionSettings.setEncryptionKeyLength(256);
        encryptionSettings.setSecurityProvider(SunJCE.class.getName());
        testEncryptAndDecrypt(encryptionSettings);
    }

    @Test
    public void testAESGCM128BC() throws Exception {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("AES/GCM/NoPadding");
        encryptionSettings.setEncryptionKeyLength(128);
        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        testEncryptAndDecrypt(encryptionSettings);
    }

    @Test
    public void testAESGCM256BC() throws Exception {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("AES/GCM/NoPadding");
        encryptionSettings.setEncryptionKeyLength(256);
        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        testEncryptAndDecrypt(encryptionSettings);
    }

    /*
     * Using just "AES" defaults to ECB mode in both Bouncy Castle and SunJCE.
     * 
     * However, BC still allows the IV to be set even if the mode doesn't use it.
     */
    @Test
    public void testAES128BC() throws Exception {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("AES");
        encryptionSettings.setEncryptionKeyLength(128);
        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        testEncryptAndDecrypt(encryptionSettings, true);
    }

    @Test
    public void testAES256BC() throws Exception {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("AES");
        encryptionSettings.setEncryptionKeyLength(256);
        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        testEncryptAndDecrypt(encryptionSettings, true);
    }

    @Test
    public void testDES64BC() throws Exception {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("DES");
        encryptionSettings.setEncryptionKeyLength(64);
        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        testEncryptAndDecrypt(encryptionSettings, true);
    }

    @Test
    public void testDESCBC64BC() throws Exception {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("DES/CBC/PKCS5Padding");
        encryptionSettings.setEncryptionKeyLength(64);
        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        testEncryptAndDecrypt(encryptionSettings);
    }

    @Test
    public void testDESCBC56SunJCE() throws Exception {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("DES/CBC/PKCS5Padding");
        encryptionSettings.setEncryptionKeyLength(56);
        encryptionSettings.setSecurityProvider(SunJCE.class.getName());
        testEncryptAndDecrypt(encryptionSettings);
    }

    @Test
    public void testCharsets() throws Exception {
        Provider provider = new BouncyCastleProvider();
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", provider);
        keyGenerator.init(128);
        Key key = keyGenerator.generateKey();

        Charset defaultCharset = Charset.defaultCharset();
        assertFalse(defaultCharset.name().equals(Charset.forName("windows-1252").name()));

        // Will default to UTF-8
        KeyEncryptor encryptor = new KeyEncryptor();
        encryptor.setProvider(provider);
        encryptor.setKey(key);
        encryptor.setAlgorithm("AES/CBC/PKCS5Padding");
        encryptor.setFormat(Output.BASE64);

        // Should not be clobbered with UTF-8
        String message1 = "I am the Α and the Ω";
        String encrypted1 = encryptor.encrypt(message1);
        String decrypted1 = encryptor.decrypt(encrypted1);
        assertEquals(message1, decrypted1);

        String message2 = new String(getRandomBytes((16 * 4096) - 1), StandardCharsets.UTF_8);
        String encrypted2 = encryptor.encrypt(message2);
        String decrypted2 = encryptor.decrypt(encrypted2);
        assertEquals(message2, decrypted2);

        // Using windows-1252
        encryptor = new KeyEncryptor();
        encryptor.setProvider(provider);
        encryptor.setKey(key);
        encryptor.setAlgorithm("AES/CBC/PKCS5Padding");
        encryptor.setFormat(Output.BASE64);
        encryptor.setCharset("windows-1252");

        // Regular message
        message1 = "testing123456789testing123456789";
        encrypted1 = encryptor.encrypt(message1);
        decrypted1 = encryptor.decrypt(encrypted1);
        assertEquals(message1, decrypted1);

        // Will be clobbered with windows-1252
        message2 = "I am the Α and the Ω";
        encrypted2 = encryptor.encrypt(message2);
        decrypted2 = encryptor.decrypt(encrypted2);
        assertEquals("I am the ? and the ?", decrypted2);
    }

    private void testEncryptAndDecrypt(EncryptionSettings encryptionSettings) throws Exception {
        testEncryptAndDecrypt(encryptionSettings, false);
    }

    private void testEncryptAndDecrypt(EncryptionSettings encryptionSettings, boolean ignoreSameOutput) throws Exception {
        testEncryptAndDecrypt(encryptionSettings, ignoreSameOutput, "testing123");
        testEncryptAndDecrypt(encryptionSettings, ignoreSameOutput, "testing123456789");
        testEncryptAndDecrypt(encryptionSettings, ignoreSameOutput, "testing123456789testing123456789");
        testEncryptAndDecrypt(encryptionSettings, ignoreSameOutput, getRandomString(16 * 4096));
        testEncryptAndDecrypt(encryptionSettings, ignoreSameOutput, new String(getRandomBytes((16 * 4096) - 1), StandardCharsets.UTF_8));
    }

    /*
     * @formatter:off
     * 1. Encrypt the same message twice
     * 2. Both encrypted messages should not be equal to the input
     * 3. Both encrypted messages should have the {iv} header
     * 4. The IVs for both encrypted messages should be different
     * 5. (Only for non-ECB) The encrypted data minus the IV for both messages should be different
     * 6. Decrypt both messages
     * 7. Both decrypted messages should be identical to the input
     * 8. Test with the old-style encryption (see below)
     * 9. Test with EncryptionUtil.decryptAndReencrypt
     * @formatter:on
     */
    private void testEncryptAndDecrypt(EncryptionSettings encryptionSettings, boolean ignoreSameOutput, String message) throws Exception {
        Provider provider = (Provider) Class.forName(encryptionSettings.getSecurityProvider()).newInstance();
        KeyGenerator keyGenerator = KeyGenerator.getInstance(encryptionSettings.getEncryptionBaseAlgorithm(), provider);
        keyGenerator.init(encryptionSettings.getEncryptionKeyLength());
        Key key = keyGenerator.generateKey();

        KeyEncryptor encryptor = new KeyEncryptor();
        encryptor.setProvider(provider);
        encryptor.setKey(key);
        encryptor.setAlgorithm(encryptionSettings.getEncryptionAlgorithm());
        encryptor.setFormat(Output.BASE64);

        String encrypted1 = encryptor.encrypt(message);
        String encrypted2 = encryptor.encrypt(message);

        // Should not be equal to the input message
        assertFalse(message.equals(encrypted1));
        assertFalse(message.equals(encrypted2));

        assertTrue(StringUtils.startsWith(encrypted1, KeyEncryptor.IV_HEADER));
        assertTrue(StringUtils.startsWith(encrypted2, KeyEncryptor.IV_HEADER));

        Pair<String, String> encryptedPair1 = splitEncrypted(encrypted1, key.getAlgorithm(), provider);
        Pair<String, String> encryptedPair2 = splitEncrypted(encrypted2, key.getAlgorithm(), provider);

        // The IVs should not be equal
        assertFalse(encryptedPair1.getLeft().equals(encryptedPair2.getLeft()));

        if (!ignoreSameOutput) {
            // The encrypted data also should not be equal when not using default AES
            assertFalse(encryptedPair1.getRight().equals(encryptedPair2.getRight()));
        }

        String decrypted1 = encryptor.decrypt(encrypted1);
        String decrypted2 = encryptor.decrypt(encrypted2);

        // Both decrypted messages should be equal to the input
        assertEquals(message, decrypted1);
        assertEquals(message, decrypted2);

        testOldEncryption(encryptionSettings, message);

        testDecryptAndReencrypt(encryptionSettings, message);
    }

    /*
     * Ensure that the old-style encrypted messages without the {iv} header can still be decrypted
     * without issues.
     */
    private void testOldEncryption(EncryptionSettings encryptionSettings, String message) throws Exception {
        Provider provider = (Provider) Class.forName(encryptionSettings.getSecurityProvider()).newInstance();
        KeyGenerator keyGenerator = KeyGenerator.getInstance(encryptionSettings.getEncryptionBaseAlgorithm(), provider);
        keyGenerator.init(encryptionSettings.getEncryptionKeyLength());
        Key key = keyGenerator.generateKey();

        Cipher cipher = Cipher.getInstance(encryptionSettings.getEncryptionAlgorithm(), provider);
        IvParameterSpec parameterSpec = new IvParameterSpec(new byte[cipher.getBlockSize()]);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
        byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        String encrypted1 = new String(Base64.encodeBase64Chunked(encrypted), StandardCharsets.UTF_8);

        KeyEncryptor encryptor = new KeyEncryptor();
        encryptor.setProvider(provider);
        encryptor.setKey(key);
        encryptor.setAlgorithm(encryptionSettings.getEncryptionAlgorithm());
        encryptor.setFormat(Output.BASE64);

        assertFalse(StringUtils.startsWith(encrypted1, KeyEncryptor.IV_HEADER));

        String decrypted1 = encryptor.decrypt(encrypted1);

        // Decrypted message should be equal to the input
        assertEquals(message, decrypted1);
    }

    /*
     * Test EncryptionUtil.decryptAndReencrypt
     */
    private void testDecryptAndReencrypt(EncryptionSettings encryptionSettings, String message) throws Exception {
        Provider provider = (Provider) Class.forName(encryptionSettings.getSecurityProvider()).newInstance();
        KeyGenerator keyGenerator = KeyGenerator.getInstance(encryptionSettings.getEncryptionBaseAlgorithm(), provider);
        keyGenerator.init(encryptionSettings.getEncryptionKeyLength());
        Key key = keyGenerator.generateKey();

        KeyEncryptor encryptor = new KeyEncryptor();
        encryptor.setProvider(provider);
        encryptor.setKey(key);
        encryptor.setAlgorithm(encryptionSettings.getEncryptionAlgorithm());
        encryptor.setFormat(Output.BASE64);

        String oldAlgorithm = encryptionSettings.getEncryptionBaseAlgorithm();
        if (provider.getName().equals("SunJCE")) {
            // SunJCE does not allow an IV to be set when it defaults to ECB mode,
            // so just use a full algorithm instead
            oldAlgorithm += "/CBC/PKCS5Padding";
        }

        KeyEncryptor oldEncryptor = new KeyEncryptor();
        oldEncryptor.setProvider(provider);
        oldEncryptor.setKey(key);
        oldEncryptor.setAlgorithm(oldAlgorithm);
        oldEncryptor.setFormat(Output.BASE64);
        String oldEncrypted = oldEncryptor.encrypt(message);

        String newEncrypted = EncryptionUtil.decryptAndReencrypt(oldEncrypted, encryptor, oldAlgorithm);

        String decrypted = encryptor.decrypt(newEncrypted);

        // Decrypted message should be equal to the input
        assertEquals(message, decrypted);
    }

    private Pair<String, String> splitEncrypted(String data, String algorithm, Provider provider) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm, provider);
        cipher.getBlockSize();

        data = StringUtils.removeStart(data, KeyEncryptor.IV_HEADER);
        byte[] totalBytes = Base64.decodeBase64(data);

        byte[] iv = new byte[cipher.getBlockSize()];
        byte[] encrypted = new byte[totalBytes.length - iv.length];
        System.arraycopy(totalBytes, 0, iv, 0, iv.length);
        System.arraycopy(totalBytes, iv.length, encrypted, 0, totalBytes.length - iv.length);

        String ivBase64 = new String(Base64.encodeBase64Chunked(iv), StandardCharsets.UTF_8);
        String encryptedBase64 = new String(Base64.encodeBase64Chunked(encrypted), StandardCharsets.UTF_8);

        return new ImmutablePair<String, String>(ivBase64, encryptedBase64);
    }

    private byte[] getRandomBytes(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    private String getRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append((char) (' ' + random.nextInt('~' - ' ')));
        }
        return builder.toString();
    }
}
