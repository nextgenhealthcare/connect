/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Script;

public class CompiledScriptCache {
    private Logger logger = Logger.getLogger(this.getClass());
    private Map<String, Script> compiledScripts = new ConcurrentHashMap<String, Script>();
    private Map<String, String> sourceScripts = new ConcurrentHashMap<String, String>();

    // singleton pattern
    private static CompiledScriptCache instance = null;

    private CompiledScriptCache() {

    }

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

    public String getSourceScript(String id) {
        return sourceScripts.get(id);
    }

    public void putCompiledScript(String id, Script compiledScript, String sourceScript) {
        logger.debug("adding script to cache");
        compiledScripts.put(id, compiledScript);
        sourceScripts.put(id, sourceScript);
    }

    public void removeCompiledScript(String id) {
        logger.debug("removing script from cache");
        compiledScripts.remove(id);
        sourceScripts.remove(id);
    }
}
