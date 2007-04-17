package com.webreach.mirth.util.test;

import junit.framework.TestCase;

import com.webreach.mirth.util.FIPSEncrypter;

public class FIPSEncrypterTest extends TestCase {
	private String plainText = "password";
	private String hashText = "W6ph5Mm5Pz8GgiULbPgzG37mj9g=";
	
	public void testEncrypt() throws Exception {
		FIPSEncrypter fips = FIPSEncrypter.getInstance();
		assertEquals(fips.getHash(plainText, null), hashText);
	}
}
