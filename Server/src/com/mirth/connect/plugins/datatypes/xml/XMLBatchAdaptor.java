/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.xml;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.donkey.server.message.batch.BatchMessageReader;
import com.mirth.connect.donkey.server.message.batch.BatchMessageReceiver;
import com.mirth.connect.plugins.datatypes.xml.XMLBatchProperties.SplitType;
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

public class XMLBatchAdaptor extends BatchAdaptor {
    private Logger logger = Logger.getLogger(this.getClass());
    private ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();

    private BufferedReader bufferedReader;
    private XPathFactory xPathFactory = XPathFactory.newInstance();
    private XMLBatchProperties batchProperties;
    private NodeList nodeList;
    private int currentNode = 0;

    public XMLBatchAdaptor(BatchAdaptorFactory factory, SourceConnector sourceConnector, BatchRawMessage batchRawMessage) {
        super(factory, sourceConnector, batchRawMessage);
    }

    public XMLBatchProperties getBatchProperties() {
        return batchProperties;
    }

    public void setBatchProperties(XMLBatchProperties batchProperties) {
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

        if (splitType == SplitType.Element_Name || splitType == SplitType.Level || splitType == SplitType.XPath_Query) {
            if (nodeList == null) {
                StringBuilder query = new StringBuilder();
                if (splitType == SplitType.Element_Name) {
                    query.append("//*[local-name()='");
                    query.append(batchProperties.getElementName());
                    query.append("']");
                } else if (splitType == SplitType.Level) {
                    query.append("/*");

                    for (int i = 0; i < batchProperties.getLevel(); i++) {
                        query.append("/*");
                    }
                } else if (splitType == SplitType.XPath_Query) {
                    query.append(batchProperties.getQuery());
                }

                XPath xpath = xPathFactory.newXPath();

                nodeList = (NodeList) xpath.evaluate(query.toString(), new InputSource(bufferedReader), XPathConstants.NODESET);
            }

            if (currentNode < nodeList.getLength()) {
                Node node = nodeList.item(currentNode++);

                if (node != null) {
                    return toXML(node);
                }
            }
        } else if (splitType == SplitType.JavaScript) {
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

                String result = JavaScriptUtil.execute(new JavaScriptTask<String>(contextFactory, "XML Batch Adaptor", sourceConnector) {
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

    private String toXML(Node node) throws Exception {
        Writer writer = new StringWriter();

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.transform(new DOMSource(node), new StreamResult(writer));

        return writer.toString();
    }

    private Map<String, Object> getScopeObjects(Reader in) {
        Map<String, Object> scopeObjects = new HashMap<String, Object>();

        // Provide the reader in the scope
        scopeObjects.put("reader", in);

        scopeObjects.put("sourceMap", new SourceMap(Collections.unmodifiableMap(batchRawMessage.getSourceMap())));

        return scopeObjects;
    }
}
