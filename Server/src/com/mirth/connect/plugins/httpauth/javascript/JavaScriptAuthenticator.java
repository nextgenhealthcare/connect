/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth.javascript;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import com.mirth.connect.plugins.httpauth.AuthenticationResult;
import com.mirth.connect.plugins.httpauth.Authenticator;
import com.mirth.connect.plugins.httpauth.RequestInfo;
import com.mirth.connect.plugins.httpauth.userutil.AuthStatus;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.userutil.SourceMap;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.javascript.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;

public class JavaScriptAuthenticator extends Authenticator {

    private static final CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();

    private Logger scriptLogger = Logger.getLogger("js-connector");
    private JavaScriptAuthenticatorProvider provider;

    public JavaScriptAuthenticator(JavaScriptAuthenticatorProvider provider) {
        this.provider = provider;
    }

    @Override
    public AuthenticationResult authenticate(RequestInfo request) throws Exception {
        return JavaScriptUtil.execute(new JavaScriptAuthenticatorTask(request));
    }

    private class JavaScriptAuthenticatorTask extends JavaScriptTask<AuthenticationResult> {

        private RequestInfo request;

        public JavaScriptAuthenticatorTask(RequestInfo request) throws Exception {
            super(provider.getContextFactory(), provider.getConnector().getConnectorProperties().getName() + " Authenticator", provider.getConnector());
            this.request = request;
        }

        @Override
        public AuthenticationResult doCall() throws Exception {
            Script compiledScript = compiledScriptCache.getCompiledScript(provider.getScriptId());

            if (compiledScript == null) {
                throw new Exception("Script not found in cache");
            } else {
                try {
                    Scriptable scope = JavaScriptScopeUtil.getMessageReceiverScope(getContextFactory(), scriptLogger, provider.getConnector().getChannelId(), provider.getConnector().getChannel().getName());

                    Map<String, Object> sourceMap = new HashMap<String, Object>();
                    request.populateMap(sourceMap);
                    scope.put("sourceMap", scope, Context.javaToJS(new SourceMap(sourceMap), scope));

                    for (AuthStatus status : AuthStatus.values()) {
                        scope.put(status.toString(), scope, Context.javaToJS(status, scope));
                    }

                    Object result = executeScript(compiledScript, scope);

                    if (result != null && !(result instanceof Undefined)) {
                        if (result instanceof NativeJavaObject) {
                            Object object = ((NativeJavaObject) result).unwrap();

                            if (object instanceof AuthenticationResult) {
                                return (AuthenticationResult) object;
                            } else if (object instanceof com.mirth.connect.plugins.httpauth.userutil.AuthenticationResult) {
                                return new AuthenticationResult((com.mirth.connect.plugins.httpauth.userutil.AuthenticationResult) object);
                            } else if (object instanceof Boolean && (Boolean) object) {
                                return AuthenticationResult.Success();
                            }
                        } else if (result instanceof AuthenticationResult) {
                            return (AuthenticationResult) result;
                        } else if (result instanceof com.mirth.connect.plugins.httpauth.userutil.AuthenticationResult) {
                            return new AuthenticationResult((com.mirth.connect.plugins.httpauth.userutil.AuthenticationResult) result);
                        } else if (result instanceof Boolean && (Boolean) result) {
                            return AuthenticationResult.Success();
                        } else {
                            try {
                                if ((Boolean) Context.jsToJava(result, java.lang.Boolean.class)) {
                                    return AuthenticationResult.Success();
                                }
                            } catch (EvaluatorException e) {
                            }
                        }
                    }
                } catch (Exception e) {
                    if (e instanceof RhinoException) {
                        try {
                            String script = CompiledScriptCache.getInstance().getSourceScript(provider.getScriptId());
                            int linenumber = ((RhinoException) e).lineNumber();
                            String errorReport = JavaScriptUtil.getSourceCode(script, linenumber, 0);
                            e = new MirthJavascriptTransformerException((RhinoException) e, provider.getConnector().getChannelId(), "Source", 0, provider.getConnector().getConnectorProperties().getName(), errorReport);
                        } catch (Exception ee) {
                            e = new MirthJavascriptTransformerException((RhinoException) e, provider.getConnector().getChannelId(), "Source", 0, provider.getConnector().getConnectorProperties().getName(), null);
                        }
                    }
                    throw e;
                } finally {
                    Context.exit();
                }
            }

            return AuthenticationResult.Failure();
        }
    }
}