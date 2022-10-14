package com.mirth.connect.server.userutil;

import java.security.MessageDigest;
import java.util.Base64;

/**
 * Provides hash utility methods.
 */
public class HashUtil {
    private static final String DEFAULT_ALGORITHM = "SHA-256";
    
    private HashUtil() {}
    
    /**
     * Takes in any object and generates a SHA-256 hash.
     * @param data
     *          The data to hash.
     * @return hash
     *          The generated SHA-256 hash of the data.
     * @throws Exception
     *          If generating a SHA-256 hash fails.
     */
    public static String generate(Object data) throws Exception { 
        if (data instanceof byte[]) {
            return generate((byte[]) data, DEFAULT_ALGORITHM);
        } else {
            return generate(String.valueOf(data).getBytes(), DEFAULT_ALGORITHM); 
        }
    }
    
    /**
     * Takes in a string, an encoding, and a hashing algorithm and generates a hash.
     * @param str
     *          The string to hash.
     * @param encoding
     *          The character encoding to use.
     * @param algorithm
     *          The hashing algorithm to use.
     * @return hash
     *          The generated hash of the string.
     * @throws Exception
     *          If generating a hash of the string fails.
     */
    public static String generate(String str, String encoding, String algorithm) throws Exception { 
        return generate(str.getBytes(encoding), algorithm);
    }

    
    /**
     * Takes in a byte[], an encoding, and a hashing algorithm and generates a hash.
     * @param bytes
     *          The byte[] to hash.
     * @param algorithm
     *          The hashing algorithm to use.
     * @return hash
     *          The generated hash of the byte[].
     * @throws Exception
     *          If generating a hash of the byte[] fails.
     */
    public static String generate(byte[] bytes, String algorithm) throws Exception { 
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        
        messageDigest.update(bytes);
        byte[] hash = messageDigest.digest();
        
        return Base64.getEncoder().encodeToString(hash);
    }
}
