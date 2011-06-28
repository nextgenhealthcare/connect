/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.security.MessageDigest;
import java.security.Security;

import com.ibm.crypto.fips.provider.IBMJCEFIPS;
import com.ibm.crypto.fips.provider.SecureRandom;
import com.ibm.misc.BASE64Decoder;
import com.ibm.misc.BASE64Encoder;

public class FIPSEncrypter {
	private static String SHA1_HASH = "SHA1";
	private static FIPSEncrypter instance = null;
	private static BASE64Encoder encoder = new BASE64Encoder();
	private static BASE64Decoder decoder = new BASE64Decoder();

	private FIPSEncrypter() {
		Security.addProvider(new IBMJCEFIPS());
	}

	public static FIPSEncrypter getInstance() {
		synchronized (FIPSEncrypter.class) {
			if (instance == null)
				instance = new FIPSEncrypter();

			return instance;
		}
	}

	public synchronized String getSalt() {
		byte[] salt = new byte[8];
		SecureRandom random = new SecureRandom();
		random.engineNextBytes(salt);
		return encoder.encode(salt);
	}

	public synchronized String getHash(String password, String salt) throws EncryptionException {

		try {
			if ((password == null) || !(password.length() > 0)) {
				throw new EncryptionException("Invalid input.");
			}

			MessageDigest digest = MessageDigest.getInstance(SHA1_HASH, "IBMJCEFIPS");

			// check if the providers is FIPS certified
			IBMJCEFIPS provider = (IBMJCEFIPS) digest.getProvider();

			if (!provider.isFipsCertified()) {
				throw new EncryptionException("Providers not FIPS certified.");
			}

			// reset the digest string
			digest.reset();

			// add the salt to the password
			if (salt != null) {
				digest.update(decoder.decodeBuffer(salt));
			}

			// hash the password
			byte[] hashedPassword = digest.digest(password.getBytes("UTF-8"));

			// convert the hashed byte array to a String
			return encoder.encode(hashedPassword);
		} catch (Exception e) {
			throw new EncryptionException(e);
		}
	}
}