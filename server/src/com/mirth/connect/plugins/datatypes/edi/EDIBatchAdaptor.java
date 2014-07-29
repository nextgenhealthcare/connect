/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.edi;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.donkey.server.message.batch.BatchMessageReader;
import com.mirth.connect.donkey.server.message.batch.BatchMessageReceiver;
import com.mirth.connect.donkey.server.message.batch.BatchMessageSource;
import com.mirth.connect.plugins.datatypes.edi.EDIBatchProperties.SplitType;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;

public class EDIBatchAdaptor extends BatchAdaptor {
    private Logger logger = Logger.getLogger(this.getClass());

    private EDIBatchProperties batchProperties;
    private BufferedReader bufferedReader;

    public EDIBatchAdaptor(SourceConnector sourceConnector, BatchMessageSource batchMessageSource) {
        super(sourceConnector, batchMessageSource);
    }

    public EDIBatchProperties getBatchProperties() {
        return batchProperties;
    }

    public void setBatchProperties(EDIBatchProperties batchProperties) {
        this.batchProperties = batchProperties;
    }

    @Override
    public void cleanup() throws BatchMessageException {}

    @Override
    protected String getNextMessage(int batchSequenceId) throws Exception {
        if (batchMessageSource instanceof BatchMessageReader) {
            if (batchSequenceId == 1) {
                BatchMessageReader batchMessageReader = (BatchMessageReader) batchMessageSource;
                bufferedReader = new BufferedReader(batchMessageReader.getReader());
            }
            return getMessageFromReader();
        } else if (batchMessageSource instanceof BatchMessageReceiver) {
            return getMessageFromReceiver((BatchMessageReceiver) batchMessageSource);
        }

        return null;
    }

    private String getMessageFromReceiver(BatchMessageReceiver batchMessageReceiver) throws Exception {
        byte[] bytes = null;

        if (batchMessageReceiver.canRead()) {
            try {
                bytes = batchMessageReceiver.readBytes();
            } finally {
                batchMessageReceiver.readCompleted();
            }

            if (bytes != null) {
                return batchMessageReceiver.getStringFromBytes(bytes);
            }
        }
        return null;
    }

    private String getMessageFromReader() throws Exception {
        SplitType splitType = batchProperties.getSplitType();

        if (splitType == SplitType.JavaScript) {
            try {
                String result = JavaScriptUtil.execute(new JavaScriptTask<String>() {
                    @Override
                    public String call() throws Exception {
                        String batchScriptId = ScriptController.getScriptId(ScriptController.BATCH_SCRIPT_KEY, sourceConnector.getChannelId());
                        Script compiledScript = CompiledScriptCache.getInstance().getCompiledScript(batchScriptId);

                        if (compiledScript == null) {
                            logger.error("Batch script could not be found in cache");
                            return null;
                        } else {
                            Logger scriptLogger = Logger.getLogger(ScriptController.BATCH_SCRIPT_KEY.toLowerCase());

                            try {
                                Scriptable scope = JavaScriptScopeUtil.getBatchProcessorScope(scriptLogger, batchScriptId, getScopeObjects(bufferedReader));
                                return (String) Context.jsToJava(executeScript(compiledScript, scope), String.class);
                            } finally {
                                Context.exit();
                            }
                        }
                    }
                });

                if (StringUtils.isEmpty(result)) {
                    return null;
                } else {
                    return result;
                }
            } catch (JavaScriptExecutorException e) {
                logger.error(e.getCause());
            } catch (Throwable e) {
                logger.error(e);
            }
        } else {
            throw new BatchMessageException("No valid batch splitting method configured");
        }

        return null;
    }

    private Map<String, Object> getScopeObjects(Reader in) {
        Map<String, Object> scopeObjects = new HashMap<String, Object>();

        // Provide the reader in the scope
        scopeObjects.put("reader", in);

        return scopeObjects;
    }
}
