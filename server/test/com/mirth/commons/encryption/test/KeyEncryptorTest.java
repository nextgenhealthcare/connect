package com.mirth.commons.encryption.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
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
        encryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());
        testEncryptAndDecrypt(encryptionSettings);
    }

    // Depends on the JRE and whether it supports this key size
//    @Test
//    public void testAESCBC256BC() throws Exception {
//        EncryptionSettings encryptionSettings = new EncryptionSettings();
//        encryptionSettings.setEncryptionAlgorithm("AES/CBC/PKCS5Padding");
//        encryptionSettings.setEncryptionKeyLength(256);
//        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
//        encryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());
//        testEncryptAndDecrypt(encryptionSettings);
//    }

    @Test
    public void testAESCBC128SunJCE() throws Exception {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("AES/CBC/PKCS5Padding");
        encryptionSettings.setEncryptionKeyLength(128);
        encryptionSettings.setSecurityProvider(SunJCE.class.getName());
        encryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());
        testEncryptAndDecrypt(encryptionSettings);
    }

    // Depends on the JRE and whether it supports this key size
//    @Test
//    public void testAESCBC256SunJCE() throws Exception {
//        EncryptionSettings encryptionSettings = new EncryptionSettings();
//        encryptionSettings.setEncryptionAlgorithm("AES/CBC/PKCS5Padding");
//        encryptionSettings.setEncryptionKeyLength(256);
//        encryptionSettings.setSecurityProvider(SunJCE.class.getName());
//        encryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());
//        testEncryptAndDecrypt(encryptionSettings);
//    }

    @Test
    public void testAESGCM128BC() throws Exception {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("AES/GCM/NoPadding");
        encryptionSettings.setEncryptionKeyLength(128);
        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        encryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());
        testEncryptAndDecrypt(encryptionSettings);
    }

    // Depends on the JRE and whether it supports this key size
//    @Test
//    public void testAESGCM256BC() throws Exception {
//        EncryptionSettings encryptionSettings = new EncryptionSettings();
//        encryptionSettings.setEncryptionAlgorithm("AES/GCM/NoPadding");
//        encryptionSettings.setEncryptionKeyLength(256);
//        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
//        encryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());
//        testEncryptAndDecrypt(encryptionSettings);
//    }

    @Test
    public void testAESCBC128BC_AES128BC() throws Exception {
        EncryptionSettings oldEncryptionSettings = new EncryptionSettings();
        oldEncryptionSettings.setEncryptionAlgorithm("AES/CBC/PKCS5Padding");
        oldEncryptionSettings.setEncryptionKeyLength(128);
        oldEncryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        oldEncryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());

        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("AES");
        encryptionSettings.setEncryptionKeyLength(128);
        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        encryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());

        testEncryptAndDecrypt(oldEncryptionSettings, encryptionSettings);
        testEncryptAndDecrypt(encryptionSettings, oldEncryptionSettings, true);
    }

    @Test
    public void testAESGCM128BC_AES128BC() throws Exception {
        EncryptionSettings oldEncryptionSettings = new EncryptionSettings();
        oldEncryptionSettings.setEncryptionAlgorithm("AES/GCM/NoPadding");
        oldEncryptionSettings.setEncryptionKeyLength(128);
        oldEncryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        oldEncryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());

        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("AES");
        encryptionSettings.setEncryptionKeyLength(128);
        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        encryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());

        testEncryptAndDecrypt(oldEncryptionSettings, encryptionSettings);
        testEncryptAndDecrypt(encryptionSettings, oldEncryptionSettings, true);
    }

    @Test
    public void testAESCBC128BC_AESGCM128BC() throws Exception {
        EncryptionSettings oldEncryptionSettings = new EncryptionSettings();
        oldEncryptionSettings.setEncryptionAlgorithm("AES/CBC/PKCS5Padding");
        oldEncryptionSettings.setEncryptionKeyLength(128);
        oldEncryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        oldEncryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());

        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("AES/GCM/NoPadding");
        encryptionSettings.setEncryptionKeyLength(128);
        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        encryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());

        testEncryptAndDecrypt(oldEncryptionSettings, encryptionSettings);
        testEncryptAndDecrypt(encryptionSettings, oldEncryptionSettings);
    }

    @Test
    public void testAESCBC128SunJCE_AESGCM128BC() throws Exception {
        EncryptionSettings oldEncryptionSettings = new EncryptionSettings();
        oldEncryptionSettings.setEncryptionAlgorithm("AES/CBC/PKCS5Padding");
        oldEncryptionSettings.setEncryptionKeyLength(128);
        oldEncryptionSettings.setSecurityProvider(SunJCE.class.getName());
        oldEncryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());

        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("AES/GCM/NoPadding");
        encryptionSettings.setEncryptionKeyLength(128);
        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        encryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());

        testEncryptAndDecrypt(oldEncryptionSettings, encryptionSettings);
        testEncryptAndDecrypt(encryptionSettings, oldEncryptionSettings);
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
        encryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());
        testEncryptAndDecrypt(encryptionSettings, encryptionSettings, true);
    }

    // Depends on the JRE and whether it supports this key size
//    @Test
//    public void testAES256BC() throws Exception {
//        EncryptionSettings encryptionSettings = new EncryptionSettings();
//        encryptionSettings.setEncryptionAlgorithm("AES");
//        encryptionSettings.setEncryptionKeyLength(256);
//        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
//        encryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());
//        testEncryptAndDecrypt(encryptionSettings, encryptionSettings, true);
//    }

    @Test
    public void testDES64BC() throws Exception {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("DES");
        encryptionSettings.setEncryptionKeyLength(64);
        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        encryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());
        testEncryptAndDecrypt(encryptionSettings, encryptionSettings, true);
    }

    @Test
    public void testDESCBC64BC() throws Exception {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("DES/CBC/PKCS5Padding");
        encryptionSettings.setEncryptionKeyLength(64);
        encryptionSettings.setSecurityProvider(BouncyCastleProvider.class.getName());
        encryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());
        testEncryptAndDecrypt(encryptionSettings);
    }

    @Test
    public void testDESCBC56SunJCE() throws Exception {
        EncryptionSettings encryptionSettings = new EncryptionSettings();
        encryptionSettings.setEncryptionAlgorithm("DES/CBC/PKCS5Padding");
        encryptionSettings.setEncryptionKeyLength(56);
        encryptionSettings.setSecurityProvider(SunJCE.class.getName());
        encryptionSettings.setEncryptionCharset(StandardCharsets.UTF_8.name());
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
        assertEquals(encryptor.getCharset(), splitEncrypted(encrypted1, provider).charset);
        String decrypted1 = encryptor.decrypt(encrypted1);
        assertEquals(message1, decrypted1);

        String message2 = new String(getRandomBytes((16 * 4096) - 1), StandardCharsets.UTF_8);
        String encrypted2 = encryptor.encrypt(message2);
        assertEquals(encryptor.getCharset(), splitEncrypted(encrypted2, provider).charset);
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
        assertEquals(encryptor.getCharset(), splitEncrypted(encrypted1, provider).charset);
        decrypted1 = encryptor.decrypt(encrypted1);
        assertEquals(message1, decrypted1);

        // Will be clobbered with windows-1252
        message2 = "I am the Α and the Ω";
        encrypted2 = encryptor.encrypt(message2);
        assertEquals(encryptor.getCharset(), splitEncrypted(encrypted2, provider).charset);
        decrypted2 = encryptor.decrypt(encrypted2);
        assertEquals("I am the ? and the ?", decrypted2);
    }

    @Test
    public void testDifferentCharsets() throws Exception {
        Provider provider = new BouncyCastleProvider();
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", provider);
        keyGenerator.init(128);
        Key key = keyGenerator.generateKey();

        KeyEncryptor encryptor = new KeyEncryptor();
        encryptor.setProvider(provider);
        encryptor.setKey(key);
        encryptor.setAlgorithm("AES/CBC/PKCS5Padding");
        encryptor.setFormat(Output.BASE64);
        encryptor.setCharset(StandardCharsets.UTF_8.name());

        String message1 = "ÂÃÄÅÆÇÈÉÊËÌ";
        String encrypted = encryptor.encrypt(message1);
        assertEquals(encryptor.getCharset(), splitEncrypted(encrypted, provider).charset);
        String decrypted1 = encryptor.decrypt(encrypted);
        assertEquals(message1, decrypted1);

        KeyEncryptor decryptor = new KeyEncryptor();
        decryptor.setProvider(provider);
        decryptor.setKey(key);
        decryptor.setAlgorithm("AES/CBC/PKCS5Padding");
        decryptor.setFormat(Output.BASE64);
        decryptor.setCharset(Charset.forName("windows-1256").name());

        String decrypted2 = decryptor.decrypt(encrypted);
        assertEquals(message1, decrypted2);

        /*
         * UTF-8 encoded ÂÃÄÅÆÇÈÉÊËÌ, Then decoded with windows-1252, Equals Ã‚ÃƒÃ„Ã…Ã†Ã‡ÃˆÃ‰ÃŠÃ‹ÃŒ
         */
        assertTrue(StringUtils.contains(encrypted, ",cs=UTF-8,"));
        encrypted = StringUtils.replace(encrypted, ",cs=UTF-8,", ",cs=windows-1252,");
        String decrypted3 = decryptor.decrypt(encrypted);
        assertEquals("Ã‚ÃƒÃ„Ã…Ã†Ã‡ÃˆÃ‰ÃŠÃ‹ÃŒ", decrypted3);
    }

    private void testEncryptAndDecrypt(EncryptionSettings encryptionSettings) throws Exception {
        testEncryptAndDecrypt(encryptionSettings, encryptionSettings);
    }

    private void testEncryptAndDecrypt(EncryptionSettings oldEncryptionSettings, EncryptionSettings encryptionSettings) throws Exception {
        testEncryptAndDecrypt(oldEncryptionSettings, encryptionSettings, false);
    }

    private void testEncryptAndDecrypt(EncryptionSettings oldEncryptionSettings, EncryptionSettings encryptionSettings, boolean ignoreSameOutput) throws Exception {
        testEncryptAndDecrypt(oldEncryptionSettings, encryptionSettings, ignoreSameOutput, "testing123");
        testEncryptAndDecrypt(oldEncryptionSettings, encryptionSettings, ignoreSameOutput, "testing123456789");
        testEncryptAndDecrypt(oldEncryptionSettings, encryptionSettings, ignoreSameOutput, "testing123456789testing123456789");
        testEncryptAndDecrypt(oldEncryptionSettings, encryptionSettings, ignoreSameOutput, getRandomString(16 * 4096));
        testEncryptAndDecrypt(oldEncryptionSettings, encryptionSettings, ignoreSameOutput, new String(getRandomBytes((16 * 4096) - 1), StandardCharsets.UTF_8));

        String[] messages = new String[100];
        for (int i = 0; i < messages.length; i++) {
            messages[i] = new String(getRandomBytes((16 * 4096) - 1), StandardCharsets.UTF_8);
        }
        testMultithreaded(oldEncryptionSettings, encryptionSettings, messages);
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
    private void testEncryptAndDecrypt(EncryptionSettings oldEncryptionSettings, EncryptionSettings encryptionSettings, boolean ignoreSameOutput, String message) throws Exception {
        Provider oldProvider = (Provider) Class.forName(oldEncryptionSettings.getSecurityProvider()).newInstance();
        KeyGenerator keyGenerator = KeyGenerator.getInstance(oldEncryptionSettings.getEncryptionBaseAlgorithm(), oldProvider);
        keyGenerator.init(oldEncryptionSettings.getEncryptionKeyLength());
        Key key = keyGenerator.generateKey();

        KeyEncryptor oldEncryptor = new KeyEncryptor();
        oldEncryptor.setProvider(oldProvider);
        oldEncryptor.setKey(key);
        oldEncryptor.setAlgorithm(oldEncryptionSettings.getEncryptionAlgorithm());
        oldEncryptor.setCharset(oldEncryptionSettings.getEncryptionCharset());
        oldEncryptor.setFormat(Output.BASE64);

        String encrypted1 = oldEncryptor.encrypt(message);
        String encrypted2 = oldEncryptor.encrypt(message);

        // Should not be equal to the input message
        assertFalse(message.equals(encrypted1));
        assertFalse(message.equals(encrypted2));

        assertTrue(StringUtils.startsWith(encrypted1, "{" + KeyEncryptor.ALGORITHM_PARAM));
        assertTrue(StringUtils.startsWith(encrypted2, "{" + KeyEncryptor.ALGORITHM_PARAM));

        EncryptionParts encryptedParts1 = splitEncrypted(encrypted1, oldProvider);
        EncryptionParts encryptedParts2 = splitEncrypted(encrypted2, oldProvider);

        // The algorithms should be correct
        assertEquals(oldEncryptionSettings.getEncryptionAlgorithm(), encryptedParts1.algorithm);
        assertEquals(oldEncryptionSettings.getEncryptionAlgorithm(), encryptedParts2.algorithm);

        // The charsets should be correct
        assertEquals(oldEncryptionSettings.getEncryptionCharset(), encryptedParts1.charset);
        assertEquals(oldEncryptionSettings.getEncryptionCharset(), encryptedParts2.charset);

        // The IVs should not be equal
        assertFalse(encryptedParts1.iv.equals(encryptedParts2.iv));

        if (!ignoreSameOutput) {
            // The encrypted data also should not be equal when not using default AES
            assertFalse(encryptedParts1.encrypted.equals(encryptedParts2.encrypted));
        }

        Provider provider = (Provider) Class.forName(encryptionSettings.getSecurityProvider()).newInstance();

        KeyEncryptor encryptor = new KeyEncryptor();
        encryptor.setProvider(provider);
        encryptor.setKey(key);
        encryptor.setAlgorithm(encryptionSettings.getEncryptionAlgorithm());
        encryptor.setCharset(encryptionSettings.getEncryptionCharset());
        encryptor.setFormat(Output.BASE64);

        String decrypted1 = encryptor.decrypt(encrypted1);
        String decrypted2 = encryptor.decrypt(encrypted2);

        // Both decrypted messages should be equal to the input
        assertEquals(message, decrypted1);
        assertEquals(message, decrypted2);

        testOldEncryption(encryptionSettings, message);

        testDecryptAndReencrypt(oldEncryptionSettings, encryptionSettings, message);
    }

    /*
     * Ensure that the old-style encrypted messages without the {iv} header can still be decrypted
     * without issues.
     */
    private void testOldEncryption(EncryptionSettings encryptionSettings, String message) throws Exception {
        testOldEncryption(encryptionSettings, message, encryptionSettings.getEncryptionAlgorithm());
        testOldEncryption(encryptionSettings, message, encryptionSettings.getEncryptionBaseAlgorithm());
    }

    private void testOldEncryption(EncryptionSettings encryptionSettings, String message, String oldAlgorithm) throws Exception {
        Provider provider = (Provider) Class.forName(encryptionSettings.getSecurityProvider()).newInstance();
        KeyGenerator keyGenerator = KeyGenerator.getInstance(encryptionSettings.getEncryptionBaseAlgorithm(), provider);
        keyGenerator.init(encryptionSettings.getEncryptionKeyLength());
        Key key = keyGenerator.generateKey();

        if (provider.getName().equals("SunJCE") && oldAlgorithm.equals(encryptionSettings.getEncryptionBaseAlgorithm())) {
            // SunJCE does not allow an IV to be set when it defaults to ECB mode,
            // so just use a full algorithm instead
            oldAlgorithm += "/CBC/PKCS5Padding";
        }

        Cipher cipher = Cipher.getInstance(oldAlgorithm, provider);
        IvParameterSpec parameterSpec = new IvParameterSpec(new byte[cipher.getBlockSize()]);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
        byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        String encrypted1 = new String(Base64.encodeBase64Chunked(encrypted), StandardCharsets.UTF_8);

        KeyEncryptor encryptor = new KeyEncryptor();
        encryptor.setProvider(provider);
        encryptor.setKey(key);
        encryptor.setAlgorithm(encryptionSettings.getEncryptionAlgorithm());
        encryptor.setFallbackAlgorithm(oldAlgorithm);
        encryptor.setFormat(Output.BASE64);

        assertFalse(StringUtils.startsWith(encrypted1, "{"));

        String decrypted1 = encryptor.decrypt(encrypted1);

        // Decrypted message should be equal to the input
        assertEquals(message, decrypted1);
    }

    /*
     * Test EncryptionUtil.decryptAndReencrypt
     */
    private void testDecryptAndReencrypt(EncryptionSettings oldEncryptionSettings, EncryptionSettings encryptionSettings, String message) throws Exception {
        Provider oldProvider = (Provider) Class.forName(oldEncryptionSettings.getSecurityProvider()).newInstance();
        KeyGenerator keyGenerator = KeyGenerator.getInstance(oldEncryptionSettings.getEncryptionBaseAlgorithm(), oldProvider);
        keyGenerator.init(oldEncryptionSettings.getEncryptionKeyLength());
        Key key = keyGenerator.generateKey();

        String oldAlgorithm = oldEncryptionSettings.getEncryptionAlgorithm();

        KeyEncryptor oldEncryptor = new KeyEncryptor();
        oldEncryptor.setProvider(oldProvider);
        oldEncryptor.setKey(key);
        oldEncryptor.setAlgorithm(oldAlgorithm);
        oldEncryptor.setCharset(oldEncryptionSettings.getEncryptionCharset());
        oldEncryptor.setFormat(Output.BASE64);
        String oldEncrypted = oldEncryptor.encrypt(message);

        Provider provider = (Provider) Class.forName(encryptionSettings.getSecurityProvider()).newInstance();

        KeyEncryptor encryptor = new KeyEncryptor();
        encryptor.setProvider(provider);
        encryptor.setKey(key);
        encryptor.setAlgorithm(encryptionSettings.getEncryptionAlgorithm());
        encryptor.setCharset(encryptionSettings.getEncryptionCharset());
        encryptor.setFormat(Output.BASE64);

        String newEncrypted = EncryptionUtil.decryptAndReencrypt(oldEncrypted, encryptor, oldAlgorithm);

        String decrypted = encryptor.decrypt(newEncrypted);

        // Decrypted message should be equal to the input
        assertEquals(message, decrypted);
    }

    /*
     * Encrypt and decrypt multiple messages simultaneously using the same KeyEncryptor instance.
     */
    private void testMultithreaded(EncryptionSettings oldEncryptionSettings, EncryptionSettings encryptionSettings, String... messages) throws Exception {
        Provider oldProvider = (Provider) Class.forName(oldEncryptionSettings.getSecurityProvider()).newInstance();
        KeyGenerator keyGenerator = KeyGenerator.getInstance(oldEncryptionSettings.getEncryptionBaseAlgorithm(), oldProvider);
        keyGenerator.init(oldEncryptionSettings.getEncryptionKeyLength());
        Key key = keyGenerator.generateKey();

        KeyEncryptor oldEncryptor = new KeyEncryptor();
        oldEncryptor.setProvider(oldProvider);
        oldEncryptor.setKey(key);
        oldEncryptor.setAlgorithm(oldEncryptionSettings.getEncryptionAlgorithm());
        oldEncryptor.setCharset(oldEncryptionSettings.getEncryptionCharset());
        oldEncryptor.setFormat(Output.BASE64);

        class EncryptCallable implements Callable<String> {
            private String message;

            public EncryptCallable(String message) {
                this.message = message;
            }

            @Override
            public String call() {
                return oldEncryptor.encrypt(message);
            }
        }

        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<String>> futures = new ArrayList<Future<String>>();
        for (String message : messages) {
            futures.add(executor.submit(new EncryptCallable(message)));
        }

        List<String> encryptedResults = new ArrayList<String>();
        for (Future<String> future : futures) {
            encryptedResults.add(future.get());
        }

        executor.shutdown();

        for (int i = 0; i < messages.length; i++) {
            String message = messages[i];
            String encrypted = encryptedResults.get(i);

            // Should not be equal to the input message
            assertFalse(message.equals(encrypted));

            assertTrue(StringUtils.startsWith(encrypted, "{" + KeyEncryptor.ALGORITHM_PARAM));

            EncryptionParts encryptedParts = splitEncrypted(encrypted, oldProvider);

            // The algorithm should be correct
            assertEquals(oldEncryptionSettings.getEncryptionAlgorithm(), encryptedParts.algorithm);

            // The charset should be correct
            assertEquals(oldEncryptionSettings.getEncryptionCharset(), encryptedParts.charset);
        }

        Provider provider = (Provider) Class.forName(encryptionSettings.getSecurityProvider()).newInstance();

        KeyEncryptor encryptor = new KeyEncryptor();
        encryptor.setProvider(provider);
        encryptor.setKey(key);
        encryptor.setAlgorithm(encryptionSettings.getEncryptionAlgorithm());
        encryptor.setCharset(encryptionSettings.getEncryptionCharset());
        encryptor.setFormat(Output.BASE64);

        class DecryptCallable implements Callable<String> {
            private String encrypted;

            public DecryptCallable(String encrypted) {
                this.encrypted = encrypted;
            }

            @Override
            public String call() {
                return encryptor.decrypt(encrypted);
            }
        }

        executor = Executors.newCachedThreadPool();
        futures = new ArrayList<Future<String>>();
        for (String encrypted : encryptedResults) {
            futures.add(executor.submit(new DecryptCallable(encrypted)));
        }

        List<String> decryptedResults = new ArrayList<String>();
        for (Future<String> future : futures) {
            decryptedResults.add(future.get());
        }

        executor.shutdown();

        for (int i = 0; i < messages.length; i++) {
            String message = messages[i];
            String decrypted = decryptedResults.get(i);

            // Decrypted message should be equal to the input
            assertEquals(message, decrypted);
        }
    }

    private EncryptionParts splitEncrypted(String data, Provider provider) throws Exception {
        data = StringUtils.removeStart(data, "{");

        data = StringUtils.removeStart(data, KeyEncryptor.ALGORITHM_PARAM);
        int index = StringUtils.indexOf(data, ',');
        String algorithm = StringUtils.substring(data, 0, index);
        data = StringUtils.substring(data, index + 1);

        data = StringUtils.removeStart(data, KeyEncryptor.CHARSET_PARAM);
        index = StringUtils.indexOf(data, ',');
        String charset = StringUtils.substring(data, 0, index);
        data = StringUtils.substring(data, index + 1);

        data = StringUtils.removeStart(data, KeyEncryptor.IV_PARAM);
        index = StringUtils.indexOf(data, '}');
        byte[] iv = Base64.decodeBase64(StringUtils.substring(data, 0, index));
        data = StringUtils.substring(data, index + 1);

        byte[] encrypted = Base64.decodeBase64(data);

        String ivBase64 = new String(Base64.encodeBase64Chunked(iv), StandardCharsets.UTF_8);
        String encryptedBase64 = new String(Base64.encodeBase64Chunked(encrypted), StandardCharsets.UTF_8);

        return new EncryptionParts(algorithm, charset, ivBase64, encryptedBase64);
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

    private class EncryptionParts {
        private String algorithm;
        private String charset;
        private String iv;
        private String encrypted;

        public EncryptionParts(String algorithm, String charset, String iv, String encrypted) {
            this.algorithm = algorithm;
            this.charset = charset;
            this.iv = iv;
            this.encrypted = encrypted;
        }
    }
}
