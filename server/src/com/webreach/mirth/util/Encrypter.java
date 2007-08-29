/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

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

			return new BASE64Encoder().encode(enc);
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
			byte[] dec = new BASE64Decoder().decodeBuffer(source);
			byte[] utf8 = dcipher.doFinal(dec);

			return new String(utf8, UTF8_ENCODING);
		} catch (Exception e) {
			throw new EncryptionException("Could not encrypt string.", e);
		}
	}
}
