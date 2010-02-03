/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model.dicom;

import java.util.HashMap;
import java.util.Map;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.util.MessageVocabulary;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Aug 6, 2007
 * Time: 3:04:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMVocabulary extends MessageVocabulary {
	Map<String, String> vocab = new HashMap<String, String>();
	private DICOMReference reference = null;
	private String version;
	private String type;
	public DICOMVocabulary(String version, String type){
		super(version, type);
		this.version = version;
		this.type = type;
		reference = DICOMReference.getInstance();
	}

	// For now we are going to use the large hashmap

	public String getDescription(String elementId) {
		return reference.getDescription(elementId, version);
	}

	@Override
	public MessageObject.Protocol getProtocol() {
		return MessageObject.Protocol.DICOM;
	}
}
