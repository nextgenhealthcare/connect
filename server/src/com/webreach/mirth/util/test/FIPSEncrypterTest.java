package com.webreach.mirth.util.test;

import junit.framework.TestCase;

import com.webreach.mirth.util.FIPSEncrypter;

public class FIPSEncrypterTest extends TestCase {
	private String plainText = "password";
	private String sampleHashText = "qgg973SXRoGks24Mu1VMRFX1Ye8=";
	private String salt = "OK/HVaQmTMM=";
	
	public void testEncrypt() throws Exception {
		FIPSEncrypter fips = FIPSEncrypter.getInstance();
		String testHashText = fips.getHash(plainText, salt);
		assertEquals(sampleHashText, testHashText);
	}
}
