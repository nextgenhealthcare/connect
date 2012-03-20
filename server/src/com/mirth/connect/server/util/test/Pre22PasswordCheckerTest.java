package com.mirth.connect.server.util.test;

import junit.framework.Assert;

import org.junit.Test;

import com.mirth.connect.server.util.Pre22PasswordChecker;

public class Pre22PasswordCheckerTest {

    @Test
    public void testCheckPassword() throws Exception {
        String salt = "Np+FZYzu4M0=";
        String hash = "NdgB6ojoGb/uFa5amMEyBNG16mE=";
        String migratedSaltHash = "SALT_" + salt + hash;
        
        Assert.assertTrue(Pre22PasswordChecker.checkPassword("admin", migratedSaltHash));
        Assert.assertFalse(Pre22PasswordChecker.checkPassword("foo", migratedSaltHash));
        Assert.assertFalse(Pre22PasswordChecker.checkPassword("", migratedSaltHash));
    }
}
