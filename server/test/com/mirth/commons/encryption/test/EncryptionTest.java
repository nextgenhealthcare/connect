package com.mirth.commons.encryption.test;

import java.security.Key;
import java.security.Provider;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;

import junit.framework.Assert;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Test;

import com.mirth.commons.encryption.Digester;
import com.mirth.commons.encryption.KeyEncryptor;
import com.mirth.commons.encryption.PBEEncryptor;

public class EncryptionTest {
    private String message1 = "Hello world!";
    private String message2 = "Goodbye cruel world!";

    @Before
    public void setUp() throws Exception {

    }

//    @Test
//    public void testKeyEncrypt() throws Exception {
//        Provider fipsProv = (Provider) Class.forName("com.ibm.crypto.fips.provider.IBMJCEFIPS").newInstance();
//        SecureRandom rng = SecureRandom.getInstance("IBMSecureRandom", fipsProv);
//
//        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", fipsProv);
//        keyGenerator.init(rng);
//        Key key = keyGenerator.generateKey();
//
//        KeyEncryptor encryptor = new KeyEncryptor();
//        encryptor.setProvider(fipsProv);
//        encryptor.setKey(key);
//
//        String encrypted = encryptor.encrypt(message1);
//        Assert.assertEquals(message1, encryptor.decrypt(encrypted));
//        Assert.assertNotSame(message2, encryptor.decrypt(encrypted));
//        Assert.assertNull(encryptor.encrypt(null));
//        Assert.assertNull(encryptor.decrypt(null));
//    }

    @Test
    public void testPBEEncrypt() throws Exception {
        Provider bcProv = new BouncyCastleProvider();

        PBEEncryptor encryptor = new PBEEncryptor();
        encryptor.setProvider(bcProv);
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        encryptor.setPassword("password");

        String encrypted = encryptor.encrypt(message1);
        Assert.assertEquals(message1, encryptor.decrypt(encrypted));
        Assert.assertNotSame(message2, encryptor.decrypt(encrypted));
    }

    @Test
    public void testDigest() throws Exception {
        Provider bcProv = new BouncyCastleProvider();
        
        Digester digester = new Digester();
        digester.setProvider(bcProv);
        digester.setAlgorithm("MD5");

        String digest = digester.digest(message1);
        Assert.assertTrue(digester.matches(message1, digest));
        Assert.assertFalse(digester.matches(message2, digest));
        Assert.assertNull(digester.digest(null));
    }
}
