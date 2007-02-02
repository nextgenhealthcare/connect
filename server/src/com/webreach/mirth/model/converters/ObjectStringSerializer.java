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


package com.webreach.mirth.model.converters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectStringSerializer {
	public String serialize(Object source) {
		String data = null;
		ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
		
		try {
			ObjectOutputStream objectOutStream = new ObjectOutputStream(byteOutStream);
			objectOutStream.writeObject(source);
			objectOutStream.flush();
			data = byteOutStream.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				byteOutStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return data;
	}

	public Object deserialize(String source) {
		Object data = null;
		ByteArrayInputStream byteInStream = new ByteArrayInputStream(source.getBytes());

		try {
			ObjectInputStream objectInStream = new ObjectInputStream(byteInStream);
			data = objectInStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				byteInStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return data;
	}
}
