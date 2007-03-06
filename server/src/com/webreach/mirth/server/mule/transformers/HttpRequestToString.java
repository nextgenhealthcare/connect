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


package com.webreach.mirth.server.mule.transformers;

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

/**
 * Transforms HTTP request to String object
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 */
public class HttpRequestToString extends AbstractTransformer {
	public HttpRequestToString() {
		super();
		this.registerSourceType(String.class);
		this.registerSourceType(byte[].class);
		setReturnClass(String.class);
	}

    public Object doTransform(Object src) throws TransformerException {
		String param;
		
		if (src instanceof byte[]) {
			param = new String((byte[]) src);
		} else {
			param = src.toString();
		}
		
		//int splitIndex = param.indexOf("=");
	
		//if (splitIndex > -1) {
			// remove the question mark from a GET
			return param;//.substring(splitIndex + 1);
		//} else {
			// throw new TransformerException(org.mule.config.i18n.Message.createStaticMessage("Failed to parse HTTP string: " + param), this);
			//return param;
	//	}
    }
}
