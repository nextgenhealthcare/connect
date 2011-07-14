package com.mirth.connect.server.util;

import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class Pre22PasswordChecker {
    private static final int SALT_LENGTH = 12;
    private static final String SALT_PREFIX = "SALT_";

    public static void main(String[] args) throws Exception {
        String hash = "NdgB6ojoGb/uFa5amMEyBNG16mE=";
        String salt = "Np+FZYzu4M0=";
        System.out.println(checkPassword("admin", SALT_PREFIX + salt + hash));
    }

    /*
     * The pre-2.2 password has been migrated to the following
     * format:
     * 
     * SALT + 8-bit salt + base64(sha(salt + password))
     * 
     * To compare:
     * 
     * 1. Strip the SALT prefix
     * 2. Get the first 8-bits and Base64 decode it, this the salt
     * 3. Get the remaining bits and Base64 decode it, this is the hash
     * 4. Pass it into the old password checker algorithm
     */
    public static boolean checkPassword(String plainPassword, String encodedPassword) throws Exception {
        String saltHash = StringUtils.substringAfter(encodedPassword, SALT_PREFIX);
        String encodedSalt = StringUtils.substring(saltHash, 0, SALT_LENGTH);
        byte[] decodedSalt = Base64.decodeBase64(encodedSalt);
        byte[] decodedHash = Base64.decodeBase64(StringUtils.substring(saltHash, encodedSalt.length()));
        
        if (Arrays.equals(decodedHash, DigestUtils.sha(ArrayUtils.addAll(decodedSalt, plainPassword.getBytes())))) {
            return true;
        }

        return false;
    }
    
    public static boolean isPre22Hash(String hash) {
        return StringUtils.startsWith(hash, SALT_PREFIX);
    }
}
