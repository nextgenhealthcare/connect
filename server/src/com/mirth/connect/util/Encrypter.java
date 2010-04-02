/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class implements DES decryption and encryption.
 * 
 */
public class Encrypter {
	public static String DES_ALGORITHM = "DESede";
	public static String UTF8_ENCODING = "UTF8";
	private Cipher ecipher;
	private Cipher dcipher;

	protected transient Log logger = LogFactory.getLog(this.getClass());

	public Encrypter(SecretKey key) {
		try {
			ecipher = Cipher.getInstance(DES_ALGORITHM);
			dcipher = Cipher.getInstance(DES_ALGORITHM);
			ecipher.init(Cipher.ENCRYPT_MODE, key);
			dcipher.init(Cipher.DECRYPT_MODE, key);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Encrypts the string.
	 * 
	 * @param source
	 * @return the encrypted string
	 */
	public String encrypt(String source) throws EncryptionException {
		if (source == null) {
			throw new EncryptionException("Invalid source string: " + source);
		}
		
		try {
			byte[] utf8 = source.getBytes(UTF8_ENCODING);
			byte[] enc = ecipher.doFinal(utf8);

			return new String(new Base64().encode(enc));
		} catch (Exception e) {
			throw new EncryptionException("Could not encrypt string.", e);
		}
	}

	/**
	 * Decrypts the string.
	 * 
	 * @param source
	 * @return the decrypted string.
	 */
	public String decrypt(String source) throws EncryptionException {
		if (source == null) {
			throw new EncryptionException("Invalid source string: " + source);
		}

		try {
			byte[] dec = new Base64().decode(source.getBytes());
			byte[] utf8 = dcipher.doFinal(dec);

			return new String(utf8, UTF8_ENCODING);
		} catch (Exception e) {
			throw new EncryptionException("Could not encrypt string.", e);
		}
	}
}
