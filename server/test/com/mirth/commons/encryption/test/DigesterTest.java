package com.mirth.commons.encryption.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.Provider;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.mirth.commons.encryption.Digester;
import com.mirth.commons.encryption.Output;

public class DigesterTest {

    private static final String HASH_SHA256_ADMIN = "YzKZIAnbQ5m+3llggrZvNtf5fg69yX7pAplfYg0Dngn/fESH93OktQ==";
    private static final String HASH_PBKDF2_ADMIN = "b8cA3mDkavInMc2JBYa6/C3EGxDp7ppqh7FsoXx0x8+3LWK3Ed3ELg==";
    private static final String HASH_ARGON2ID_ADMIN = "18YvUkCbcCTfjb1yTF8cIUYeS5xtMFpDsqlQSOl3azbu7iKGR7e8Fw==";
    private static final String HASH_ARGON2D_ADMIN = "IyCkfAXvfcvhoI4lMKlQsShWJg/0197QzsTMMPgID4OIcAL95xlDxA==";
    private static final String HASH_ARGON2I_ADMIN = "IHRJ8lHxVv8whJMxFfWLMdpE2gx/0bwDV+oKGS9LquBnl2DFJgM0rA==";

    @Test
    public void testSHA256() throws Exception {
        Digester digester = createDigester(new org.bouncycastle.jce.provider.BouncyCastleProvider(), "SHA256", 1000);

        String input1 = "admin";
        String digest1 = digester.digest(input1);
        assertFalse(StringUtils.isBlank(digest1));
        assertFalse(input1.equals(digest1));
        assertTrue(digester.matches(input1, digest1));

        assertTrue(digester.matches("admin", HASH_SHA256_ADMIN));

        String digest2 = digester.digest(input1);
        assertFalse(StringUtils.isBlank(digest2));
        assertFalse(input1.equals(digest2));
        assertFalse(digest1.equals(digest2));
        assertTrue(digester.matches(input1, digest2));

        digester.setIterations(100000);
        String digest3 = digester.digest(input1);
        assertFalse(StringUtils.isBlank(digest3));
        assertFalse(input1.equals(digest3));
        assertFalse(digest1.equals(digest3));
        assertFalse(digest2.equals(digest3));
        assertTrue(digester.matches(input1, digest3));

        // Hardcoded hash used 1000 iterations
        assertFalse(digester.matches("admin", HASH_SHA256_ADMIN));
    }

    @Test
    public void testPBKDF2() throws Exception {
        Digester digester = createDigester(new org.bouncycastle.jce.provider.BouncyCastleProvider(), "PBKDF2WithHmacSHA256", 600000, true, 256);

        String input1 = "admin";
        String digest1 = digester.digest(input1);
        assertFalse(StringUtils.isBlank(digest1));
        assertFalse(input1.equals(digest1));
        assertTrue(digester.matches(input1, digest1));

        assertTrue(digester.matches("admin", HASH_PBKDF2_ADMIN));

        String digest2 = digester.digest(input1);
        assertFalse(StringUtils.isBlank(digest2));
        assertFalse(input1.equals(digest2));
        assertFalse(digest1.equals(digest2));
        assertTrue(digester.matches(input1, digest2));

        digester.setIterations(1000);
        String digest3 = digester.digest(input1);
        assertFalse(StringUtils.isBlank(digest3));
        assertFalse(input1.equals(digest3));
        assertFalse(digest1.equals(digest3));
        assertFalse(digest2.equals(digest3));
        assertTrue(digester.matches(input1, digest3));

        // Hardcoded hash used 600000 iterations
        assertFalse(digester.matches("admin", HASH_PBKDF2_ADMIN));
    }

    @Test
    public void testArgon2id() throws Exception {
        Digester digester = createDigester(new org.bouncycastle.jce.provider.BouncyCastleProvider(), "Argon2", 3);

        String input1 = "admin";
        String digest1 = digester.digest(input1);
        assertFalse(StringUtils.isBlank(digest1));
        assertFalse(input1.equals(digest1));
        assertTrue(digester.matches(input1, digest1));

        assertTrue(digester.matches("admin", HASH_ARGON2ID_ADMIN));
        assertFalse(digester.matches("admin", HASH_ARGON2D_ADMIN));
        assertFalse(digester.matches("admin", HASH_ARGON2I_ADMIN));

        String digest2 = digester.digest(input1);
        assertFalse(StringUtils.isBlank(digest2));
        assertFalse(input1.equals(digest2));
        assertFalse(digest1.equals(digest2));
        assertTrue(digester.matches(input1, digest2));

        digester.setIterations(2);
        String digest3 = digester.digest(input1);
        assertFalse(StringUtils.isBlank(digest3));
        assertFalse(input1.equals(digest3));
        assertFalse(digest1.equals(digest3));
        assertFalse(digest2.equals(digest3));
        assertTrue(digester.matches(input1, digest3));

        // Hardcoded hash used 3 iterations
        assertFalse(digester.matches("admin", HASH_ARGON2ID_ADMIN));
        assertFalse(digester.matches("admin", HASH_ARGON2D_ADMIN));
        assertFalse(digester.matches("admin", HASH_ARGON2I_ADMIN));
    }

    @Test
    public void testArgon2d() throws Exception {
        Digester digester = createDigester(new org.bouncycastle.jce.provider.BouncyCastleProvider(), "Argon2d", 4);

        String input1 = "admin";
        String digest1 = digester.digest(input1);
        assertFalse(StringUtils.isBlank(digest1));
        assertFalse(input1.equals(digest1));
        assertTrue(digester.matches(input1, digest1));

        assertTrue(digester.matches("admin", HASH_ARGON2D_ADMIN));
        assertFalse(digester.matches("admin", HASH_ARGON2I_ADMIN));
        assertFalse(digester.matches("admin", HASH_ARGON2ID_ADMIN));

        String digest2 = digester.digest(input1);
        assertFalse(StringUtils.isBlank(digest2));
        assertFalse(input1.equals(digest2));
        assertFalse(digest1.equals(digest2));
        assertTrue(digester.matches(input1, digest2));

        digester.setIterations(3);
        String digest3 = digester.digest(input1);
        assertFalse(StringUtils.isBlank(digest3));
        assertFalse(input1.equals(digest3));
        assertFalse(digest1.equals(digest3));
        assertFalse(digest2.equals(digest3));
        assertTrue(digester.matches(input1, digest3));

        // Hardcoded hash used 4 iterations
        assertFalse(digester.matches("admin", HASH_ARGON2D_ADMIN));
        assertFalse(digester.matches("admin", HASH_ARGON2I_ADMIN));
        assertFalse(digester.matches("admin", HASH_ARGON2ID_ADMIN));
    }

    @Test
    public void testArgon2i() throws Exception {
        Digester digester = createDigester(new org.bouncycastle.jce.provider.BouncyCastleProvider(), "Argon2i", 5);

        String input1 = "admin";
        String digest1 = digester.digest(input1);
        assertFalse(StringUtils.isBlank(digest1));
        assertFalse(input1.equals(digest1));
        assertTrue(digester.matches(input1, digest1));

        assertTrue(digester.matches("admin", HASH_ARGON2I_ADMIN));
        assertFalse(digester.matches("admin", HASH_ARGON2D_ADMIN));
        assertFalse(digester.matches("admin", HASH_ARGON2ID_ADMIN));

        String digest2 = digester.digest(input1);
        assertFalse(StringUtils.isBlank(digest2));
        assertFalse(input1.equals(digest2));
        assertFalse(digest1.equals(digest2));
        assertTrue(digester.matches(input1, digest2));

        digester.setIterations(4);
        String digest3 = digester.digest(input1);
        assertFalse(StringUtils.isBlank(digest3));
        assertFalse(input1.equals(digest3));
        assertFalse(digest1.equals(digest3));
        assertFalse(digest2.equals(digest3));
        assertTrue(digester.matches(input1, digest3));

        // Hardcoded hash used 5 iterations
        assertFalse(digester.matches("admin", HASH_ARGON2I_ADMIN));
        assertFalse(digester.matches("admin", HASH_ARGON2D_ADMIN));
        assertFalse(digester.matches("admin", HASH_ARGON2ID_ADMIN));
    }

    @Test
    public void testFallback1() throws Exception {
        Digester digester = createDigester(new org.bouncycastle.jce.provider.BouncyCastleProvider(), "PBKDF2WithHmacSHA256", 600000, true, 256);

        assertFalse(digester.matches("admin", HASH_SHA256_ADMIN));

        digester.setFallbackAlgorithm("SHA256");

        assertTrue(digester.matches("admin", HASH_SHA256_ADMIN));
    }

    @Test
    public void testFallback2() throws Exception {
        Digester digester1 = createDigester(new org.bouncycastle.jce.provider.BouncyCastleProvider(), "Argon2", 3);

        String input1 = "admin";
        String digest1 = digester1.digest(input1);
        assertFalse(StringUtils.isBlank(digest1));
        assertFalse(input1.equals(digest1));
        assertTrue(digester1.matches(input1, digest1));

        Digester digester2 = createDigester(new org.bouncycastle.jce.provider.BouncyCastleProvider(), "PBKDF2WithHmacSHA256", 600000, true, 256);

        assertFalse(digester2.matches(input1, digest1));

        digester2.setFallbackAlgorithm(digester1.getAlgorithm());
        digester2.setFallbackSaltSizeBytes(digester1.getSaltSizeBytes());
        digester2.setFallbackIterations(digester1.getIterations());
        digester2.setFallbackUsePBE(digester1.isUsePBE());
        digester2.setFallbackKeySizeBits(digester1.getKeySizeBits());

        assertTrue(digester2.matches(input1, digest1));
    }

    @Test
    public void testFallback3() throws Exception {
        Digester digester1 = createDigester(new org.bouncycastle.jce.provider.BouncyCastleProvider(), "PBKDF2WithHmacSHA256", 600000, true, 256);

        String input1 = "admin";
        String digest1 = digester1.digest(input1);
        assertFalse(StringUtils.isBlank(digest1));
        assertFalse(input1.equals(digest1));
        assertTrue(digester1.matches(input1, digest1));

        Digester digester2 = createDigester(new org.bouncycastle.jce.provider.BouncyCastleProvider(), "Argon2", 3);

        assertFalse(digester2.matches(input1, digest1));

        digester2.setFallbackAlgorithm(digester1.getAlgorithm());
        digester2.setFallbackSaltSizeBytes(digester1.getSaltSizeBytes());
        digester2.setFallbackIterations(digester1.getIterations());
        digester2.setFallbackUsePBE(digester1.isUsePBE());
        digester2.setFallbackKeySizeBits(digester1.getKeySizeBits());

        assertTrue(digester2.matches(input1, digest1));
    }

    @Test
    public void testFallback4() throws Exception {
        Digester digester1 = createDigester(new org.bouncycastle.jce.provider.BouncyCastleProvider(), "PBKDF2WithHmacSHA256", 2222, true, 128);
        digester1.setSaltSizeBytes(10);

        String input1 = "admin";
        String digest1 = digester1.digest(input1);
        assertFalse(StringUtils.isBlank(digest1));
        assertFalse(input1.equals(digest1));
        assertTrue(digester1.matches(input1, digest1));

        Digester digester2 = createDigester(new org.bouncycastle.jce.provider.BouncyCastleProvider(), "PBKDF2WithHmacSHA256", 600000, true, 256);

        assertFalse(digester2.matches(input1, digest1));

        digester2.setFallbackAlgorithm(digester1.getAlgorithm());
        digester2.setFallbackSaltSizeBytes(digester1.getSaltSizeBytes());
        digester2.setFallbackIterations(digester1.getIterations());
        digester2.setFallbackUsePBE(digester1.isUsePBE());
        digester2.setFallbackKeySizeBits(digester1.getKeySizeBits());

        assertTrue(digester2.matches(input1, digest1));
    }

    private Digester createDigester(Provider provider, String algorithm, int iterations) {
        return createDigester(provider, algorithm, iterations, false, 256);
    }

    private Digester createDigester(Provider provider, String algorithm, int iterations, boolean usePBE, int keySizeBits) {
        Digester digester = new Digester();
        digester.setProvider(provider);
        digester.setAlgorithm(algorithm);
        digester.setIterations(iterations);
        digester.setUsePBE(usePBE);
        digester.setKeySizeBits(keySizeBits);
        digester.setFormat(Output.BASE64);
        return digester;
    }
}
