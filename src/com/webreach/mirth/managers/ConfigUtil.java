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


package com.webreach.mirth.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CMUtil contains various utility methods used by ConfigurationManager.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 * 
 */
public class ConfigUtil {
	private ConfigUtil() {};

	protected static transient Log logger = LogFactory.getLog(ConfigUtil.class);
	private static DESEncrypter encrypter = null;

	/**
	 * Returns the encrypted password.
	 * 
	 * @param password
	 *            the password that will be encrypted
	 * @return the encrypted password.
	 */
	public static String encryptPassword(String password) {
		try {
			if (encrypter == null) {
				encrypter = new DESEncrypter(getEncryptionKey());
			}

			return encrypter.encrypt(password);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Returns the decrypted password.
	 * 
	 * @param password
	 *            the encrypted password that will be decrypted
	 * @return the decrypted password.
	 */
	public static String decryptPassword(String password) {
		try {
			if (encrypter == null) {
				encrypter = new DESEncrypter(getEncryptionKey());
			}

			return encrypter.decrypt(password);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Returns the secret key used for password encryption and decryption.
	 * 
	 * @return the secret key used for password encryption and decryption.
	 */
	private static SecretKey getEncryptionKey() {
		try {
			File keyFile = new File(ConfigurationManager.KEY_FILE);

			if (keyFile.exists()) {
				logger.debug("loading encryption key file: " + keyFile.getAbsolutePath());

				// deserialize the secret key
				FileInputStream fileInput = new FileInputStream(keyFile);
				ObjectInputStream ois = new ObjectInputStream(fileInput);

				return (SecretKey) ois.readObject();
			} else {
				logger.debug("creating new encryption key file: " + keyFile.getAbsolutePath());

				// generate a new secret key
				SecretKey key = KeyGenerator.getInstance(DESEncrypter.DES_ALGORITHM).generateKey();

				// serialize it to a file
				FileOutputStream fileOuput = new FileOutputStream(keyFile);
				ObjectOutputStream oos = new ObjectOutputStream(fileOuput);
				oos.writeObject(key);
				oos.flush();

				return key;
			}
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	/**
	 * Returns an ArrayList of String from a String array.
	 * 
	 * @param stringArray
	 * @return
	 */
	public static ArrayList<String> getArrayListFromStringArray(String[] stringArray) {
		ArrayList<String> arrayList = new ArrayList<String>();

		for (int i = 0; i < stringArray.length; i++) {
			arrayList.add(stringArray[i]);
		}

		return arrayList;
	}

	/**
	 * Returns the current date in the format DATE_MONTH_YEAR_MINUTE_SECOND
	 * 
	 * @return the current date in the format DATE_MONTH_YEAR_MINUTE_SECOND
	 */
	public static String getTimeStamp() {
		return String.valueOf(GregorianCalendar.getInstance().get(GregorianCalendar.DATE)) + "_" + String.valueOf(GregorianCalendar.getInstance().get(GregorianCalendar.MONTH) + 1) + "_" + String.valueOf(GregorianCalendar.getInstance().get(GregorianCalendar.YEAR)) + "_" + String.valueOf(GregorianCalendar.getInstance().get(GregorianCalendar.MINUTE)) + "_" + String.valueOf(GregorianCalendar.getInstance().get(GregorianCalendar.SECOND));
	}

	/**
	 * Returns the name of a file without the extension.
	 * 
	 * @param filename
	 *            the name of the file
	 * @return the name of the file without the extension
	 */
	public static String removeFileExtension(String filename) {
		return filename.substring(0, filename.indexOf("."));
	}
}
