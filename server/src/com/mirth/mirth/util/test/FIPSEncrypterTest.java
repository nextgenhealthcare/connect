/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

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
