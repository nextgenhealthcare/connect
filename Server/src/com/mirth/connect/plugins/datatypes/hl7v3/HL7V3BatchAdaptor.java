/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v3;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.donkey.server.message.batch.BatchMessageReader;
import com.mirth.connect.donkey.server.message.batch.BatchMessageReceiver;
import com.mirth.connect.plugins.datatypes.hl7v3.HL7V3BatchProperties.SplitType;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.userutil.SourceMap;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class HL7V3BatchAdaptor extends BatchAdaptor {
    private Logger logger = Logger.getLogger(this.getClass());
    private ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();

    private HL7V3BatchProperties batchProperties;
    private BufferedReader bufferedReader;

    public HL7V3BatchAdaptor(BatchAdaptorFactory factory, SourceConnector sourceConnector, BatchRawMessage batchRawMessage) {
        super(factory, sourceConnector, batchRawMessage);
    }

    public HL7V3BatchProperties getBatchProperties() {
        return batchProperties;
    }

    public void setBatchProperties(HL7V3BatchProperties batchProperties) {
        this.batchProperties = batchProperties;
    }

    @Override
    public void cleanup() throws BatchMessageException {}

    @Override
    protected String getNextMessage(int batchSequenceId) throws Exception {
        if (batchRawMessage.getBatchMessageSource() instanceof BatchMessageReader) {
            if (batchSequenceId == 1) {
                BatchMessageReader batchMessageReader = (BatchMessageReader) batchRawMessage.getBatchMessageSource();
                bufferedReader = new BufferedReader(batchMessageReader.getReader());
            }
            return getMessageFromReader();
        } else if (batchRawMessage.getBatchMessageSource() instanceof BatchMessageReceiver) {
            return getMessageFromReceiver((BatchMessageReceiver) batchRawMessage.getBatchMessageSource());
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
            if (StringUtils.isEmpty(batchProperties.getBatchScript())) {
                throw new BatchMessageException("No batch script was set.");
            }

            try {
                final String batchScriptId = ScriptController.getScriptId(ScriptController.BATCH_SCRIPT_KEY, sourceConnector.getChannelId());

                MirthContextFactory contextFactory = contextFactoryController.getContextFactory(sourceConnector.getChannel().getResourceIds());
                if (!factory.getContextFactoryId().equals(contextFactory.getId())) {
                    synchronized (factory) {
                        contextFactory = contextFactoryController.getContextFactory(sourceConnector.getChannel().getResourceIds());
                        if (!factory.getContextFactoryId().equals(contextFactory.getId())) {
                            JavaScriptUtil.recompileGeneratedScript(contextFactory, batchScriptId);
                            factory.setContextFactoryId(contextFactory.getId());
                        }
                    }
                }

                String result = JavaScriptUtil.execute(new JavaScriptTask<String>(contextFactory, "HL7 v3 Batch Adaptor", sourceConnector) {
                    @Override
                    public String doCall() throws Exception {
                        Script compiledScript = CompiledScriptCache.getInstance().getCompiledScript(batchScriptId);

                        if (compiledScript == null) {
                            logger.error("Batch script could not be found in cache");
                            return null;
                        } else {
                            Logger scriptLogger = Logger.getLogger(ScriptController.BATCH_SCRIPT_KEY.toLowerCase());

                            try {
                                Scriptable scope = JavaScriptScopeUtil.getBatchProcessorScope(getContextFactory(), scriptLogger, sourceConnector.getChannelId(), sourceConnector.getChannel().getName(), getScopeObjects(bufferedReader));
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

        scopeObjects.put("sourceMap", new SourceMap(Collections.unmodifiableMap(batchRawMessage.getSourceMap())));

        return scopeObjects;
    }
}
