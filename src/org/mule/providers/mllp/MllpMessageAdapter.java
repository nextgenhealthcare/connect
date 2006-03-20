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
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package org.mule.providers.mllp;

import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;

public class MllpMessageAdapter extends AbstractMessageAdapter {
	private byte[] message;

	public MllpMessageAdapter(Object message) throws MessagingException {
		if (message instanceof byte[]) {
			this.message = (byte[]) message;
		} else {
			throw new MessageTypeNotSupportedException(message, getClass());
		}
	}

	public String getPayloadAsString() throws Exception {
		return new String(message);
	}

	public byte[] getPayloadAsBytes() throws Exception {
		return message;
	}

	public Object getPayload() {
		return message;
	}
}
