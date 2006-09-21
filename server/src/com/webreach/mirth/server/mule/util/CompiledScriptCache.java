package com.webreach.mirth.server.mule.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Script;

public class CompiledScriptCache {
	private Logger logger = Logger.getLogger(this.getClass());
	private Map<String, Script> compiledFilterScripts = new HashMap<String, Script>();
	private Map<String, Script> compiledTransformerScripts = new HashMap<String, Script>();

	// singleton pattern
	private static CompiledScriptCache instance = null;

	private CompiledScriptCache() {}

	public static CompiledScriptCache getInstance() {
		synchronized (CompiledScriptCache.class) {
			if (instance == null)
				instance = new CompiledScriptCache();

			return instance;
		}
	}

	public Script getCompiledFilterScript(String id) {
		return compiledFilterScripts.get(id);
	}

	public void putCompiledFilterScript(String id, Script compiledScript) {
		logger.debug("adding filter script to cache");
		compiledFilterScripts.put(id, compiledScript);
	}

	public Script getCompiledTransformerScript(String id) {
		return compiledTransformerScripts.get(id);
	}

	public void putCompiledTransformerScript(String id, Script compiledScript) {
		logger.debug("adding transformer script to cache");
		compiledTransformerScripts.put(id, compiledScript);
	}
}
