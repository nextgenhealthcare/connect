/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.util.javascript;

import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.util.ThreadUtils;

public abstract class JavaScriptTask<T> implements Callable<T> {

    private Logger logger = Logger.getLogger(JavaScriptTask.class);
    private MirthContextFactory contextFactory;
    private String threadName;
    private Context context;
    private boolean contextCreated = false;

    public JavaScriptTask(MirthContextFactory contextFactory, String name) {
        this(contextFactory, name, null, null);
    }

    public JavaScriptTask(MirthContextFactory contextFactory, String name, String channelId, String channelName) {
        this(contextFactory, name, channelId, channelName, null, null);
    }

    public JavaScriptTask(MirthContextFactory contextFactory, SourceConnector sourceConnector) {
        this(contextFactory, sourceConnector.getConnectorProperties().getName(), sourceConnector);
    }

    public JavaScriptTask(MirthContextFactory contextFactory, String name, SourceConnector sourceConnector) {
        this(contextFactory, name, sourceConnector.getChannelId(), sourceConnector.getChannel().getName(), sourceConnector.getMetaDataId(), null);
    }

    public JavaScriptTask(MirthContextFactory contextFactory, DestinationConnector destinationConnector) {
        this(contextFactory, destinationConnector.getConnectorProperties().getName(), destinationConnector.getChannelId(), destinationConnector.getChannel().getName(), destinationConnector.getMetaDataId(), destinationConnector.getDestinationName());
    }

    public JavaScriptTask(MirthContextFactory contextFactory, Connector connector) {
        this(contextFactory, connector.getConnectorProperties().getName(), connector);
    }

    public JavaScriptTask(MirthContextFactory contextFactory, String name, Connector connector) {
        this(contextFactory);
        if (connector instanceof SourceConnector) {
            init(name, connector.getChannelId(), connector.getChannel().getName(), connector.getMetaDataId(), null);
        } else {
            init(name, connector.getChannelId(), connector.getChannel().getName(), connector.getMetaDataId(), ((DestinationConnector) connector).getDestinationName());
        }
    }

    private JavaScriptTask(MirthContextFactory contextFactory, String name, String channelId, String channelName, Integer metaDataId, String destinationName) {
        this(contextFactory);
        init(name, channelId, channelName, metaDataId, destinationName);
    }

    private JavaScriptTask(MirthContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    private void init(String name, String channelId, String channelName, Integer metaDataId, String destinationName) {
        StringBuilder builder = new StringBuilder(name).append(" JavaScript Task");
        if (StringUtils.isNotEmpty(channelName)) {
            builder.append(" on ").append(channelName);
            if (StringUtils.isNotEmpty(channelId)) {
                builder.append(" (").append(channelId).append(')');
            }

            if (metaDataId != null && metaDataId > 0) {
                builder.append(',');
                if (StringUtils.isNotEmpty(destinationName)) {
                    builder.append(' ').append(destinationName);
                }
                builder.append(" (").append(metaDataId).append(')');
            }
        }
        threadName = builder.toString();
    }

    public MirthContextFactory getContextFactory() {
        return contextFactory;
    }

    public void setContextFactory(MirthContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    protected Context getContext() {
        return context;
    }

    public abstract T doCall() throws Exception;

    @Override
    public final T call() throws Exception {
        String originalThreadName = Thread.currentThread().getName();
        try {
            Thread.currentThread().setName(threadName + " < " + originalThreadName);
            return doCall();
        } finally {
            Thread.currentThread().setName(originalThreadName);
        }
    }

    public Object executeScript(Script compiledScript, Scriptable scope) throws InterruptedException {
        Thread currentThread = Thread.currentThread();

        try {
            // if the executor is halting this task, we don't want to initialize the context yet
            synchronized (this) {
                ThreadUtils.checkInterruptedStatus();
                context = Context.getCurrentContext();
                currentThread.setContextClassLoader(contextFactory.getApplicationClassLoader());
                logger.debug(StringUtils.defaultString(StringUtils.trimToNull(getClass().getSimpleName()), getClass().getName()) + " using context factory: " + contextFactory.hashCode());

                /*
                 * This should never be called but exists in case executeScript is called from a
                 * different thread than the one that entered the context.
                 */
                if (context == null) {
                    contextCreated = true;
                    context = JavaScriptScopeUtil.getContext(contextFactory);
                }

                if (context instanceof MirthContext) {
                    ((MirthContext) context).setRunning(true);
                }
            }

            if (currentThread instanceof MirthJavaScriptThread) {
                MirthJavaScriptThread mirthThread = (MirthJavaScriptThread) currentThread;
                mirthThread.setContext(context);
                mirthThread.setScope(scope);
            }

            return compiledScript.exec(context, scope);
        } finally {
            if (contextCreated) {
                Context.exit();
                contextCreated = false;
            }

            if (currentThread instanceof MirthJavaScriptThread) {
                MirthJavaScriptThread mirthThread = (MirthJavaScriptThread) currentThread;
                mirthThread.setContext(null);
                mirthThread.setScope(null);
            }
        }
    }
}
