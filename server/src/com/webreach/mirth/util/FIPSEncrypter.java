package com.webreach.mirth.util;

import java.security.MessageDigest;
import java.security.Security;

import com.ibm.crypto.fips.provider.IBMJCEFIPS;
import com.ibm.crypto.fips.provider.SecureRandom;
import com.ibm.misc.BASE64Encoder;

public class FIPSEncrypter {
	private static String SHA1_HASH = "SHA1";
	private static FIPSEncrypter instance = null;

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

	public synchronized String getHash(String plainText) throws EncryptionException {
		try {
			if ((plainText == null) || !(plainText.length() > 0)) {
				throw new EncryptionException("Invalid input.");
			}
			
			MessageDigest digest = MessageDigest.getInstance(SHA1_HASH, "IBMJCEFIPS");
			IBMJCEFIPS provider = (IBMJCEFIPS) digest.getProvider();
			
			if (!provider.isFipsCertified()) {
				throw new EncryptionException("Providers not FIPS certified.");
			}

			// salt hash
			SecureRandom random = new SecureRandom();
			byte[] salt = new byte[8];
			random.engineNextBytes(salt);
			digest.update(salt);
			
			digest.update(plainText.getBytes("UTF-8"));
			byte raw[] = digest.digest();
			String hash = (new BASE64Encoder()).encode(raw);
			return hash;
		} catch (Exception e) {
			throw new EncryptionException(e);
		}
	}
}