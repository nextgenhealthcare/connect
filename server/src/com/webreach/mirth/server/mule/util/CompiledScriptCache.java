package com.webreach.mirth.server.mule.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Script;

public class CompiledScriptCache {
	private Logger logger = Logger.getLogger(this.getClass());
	private Map<String, Script> compiledScripts = new HashMap<String, Script>();

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

	public Script getCompiledScript(String id) {
		return compiledScripts.get(id);
	}

	public void putCompiledScript(String id, Script compiledScript) {
		logger.debug("adding script to cache");
		compiledScripts.put(id, compiledScript);
	}
}
