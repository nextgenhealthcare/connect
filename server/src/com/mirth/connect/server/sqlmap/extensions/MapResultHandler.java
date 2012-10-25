/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.sqlmap.extensions;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

public class MapResultHandler<K,V> implements ResultHandler {
	private Map<K, V> map = new LinkedHashMap<K, V>();
	private String key;
	private String value;
	
	public MapResultHandler(String key, String value){
		this.key = key;
		this.value = value;
	}
	
	public Map<K, V> getMap() {
		return map;
	}

	/**
	 * Condenses the mybatis returned map as follows
	 * 
	 * ie) {id='1234', revision=1}  -->  {1234=1}
	 */
	@Override
	public void handleResult(ResultContext context) {
		@SuppressWarnings("unchecked")
		Map<Object, Object> result = (Map<Object, Object>) context.getResultObject();
		
		if(result.containsKey(key.toUpperCase()) && result.containsKey(value.toUpperCase())){
			map.put((K)result.get(key.toUpperCase()), (V)result.get(value.toUpperCase()));
		}
		else if(result.containsKey(key.toLowerCase()) && result.containsKey(value.toLowerCase())){
			map.put((K)result.get(key.toLowerCase()), (V)result.get(value.toLowerCase()));
		}
	}
}