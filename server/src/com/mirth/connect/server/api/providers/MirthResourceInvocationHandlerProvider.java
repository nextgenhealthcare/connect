/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.providers;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.spi.internal.ResourceMethodInvocationHandlerProvider;

import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.client.core.api.Param;
import com.mirth.connect.client.core.api.util.OperationUtil;
import com.mirth.connect.server.api.CheckAuthorizedChannelId;
import com.mirth.connect.server.api.CheckAuthorizedUserId;
import com.mirth.connect.server.api.DontCheckAuthorized;
import com.mirth.connect.server.api.MirthServlet;

@Provider
public class MirthResourceInvocationHandlerProvider implements ResourceMethodInvocationHandlerProvider {

    /*
     * This map is used to cache information about specific methods so we don't have to calculate it
     * every time.
     */
    private static Map<Class<? extends MirthServlet>, Map<Method, MethodInfo>> infoMap = new ConcurrentHashMap<Class<? extends MirthServlet>, Map<Method, MethodInfo>>();

    @Override
    public InvocationHandler create(Invocable method) {
        return new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String originalThreadName = Thread.currentThread().getName();

                try {
                    if (proxy instanceof MirthServlet) {
                        try {
                            MirthServlet mirthServlet = (MirthServlet) proxy;

                            Map<Method, MethodInfo> methodMap = infoMap.get(proxy.getClass());
                            if (methodMap == null) {
                                methodMap = new ConcurrentHashMap<Method, MethodInfo>();
                                infoMap.put(mirthServlet.getClass(), methodMap);
                            }

                            MethodInfo methodInfo = methodMap.get(method);
                            if (methodInfo == null) {
                                methodInfo = new MethodInfo();
                                methodMap.put(method, methodInfo);
                            }

                            Operation operation = methodInfo.getOperation();
                            if (operation == null) {
                                /*
                                 * Get the operation from the MirthOperation annotation present on
                                 * the interface method.
                                 */
                                Class<?> clazz = proxy.getClass();

                                while (clazz != null && operation == null) {
                                    for (Class<?> interfaceClass : clazz.getInterfaces()) {
                                        if (BaseServletInterface.class.isAssignableFrom(interfaceClass)) {
                                            operation = OperationUtil.getOperation(interfaceClass, method);
                                            if (operation != null) {
                                                methodInfo.setOperation(operation);
                                                break;
                                            }
                                        }
                                    }

                                    clazz = clazz.getSuperclass();
                                }
                            }
                            mirthServlet.setOperation(operation);

                            // Set thread name based on the servlet class and operation name
                            Thread.currentThread().setName(mirthServlet.getClass().getSimpleName() + " Thread (" + operation.getDisplayName() + ") < " + originalThreadName);

                            /*
                             * If a DontCheckAuthorized annotation is present on the server
                             * implementation method, then no auditing is done now and the servlet
                             * is expected to call checkUserAuthorized. Two other optional
                             * annotations determine whether the channel/user ID should be used in
                             * the authorization check.
                             */
                            Boolean checkAuthorized = methodInfo.getCheckAuthorized();
                            if (checkAuthorized == null) {
                                checkAuthorized = true;

                                Method matchingMethod = mirthServlet.getClass().getMethod(method.getName(), method.getParameterTypes());
                                if (matchingMethod != null) {
                                    checkAuthorized = matchingMethod.getAnnotation(DontCheckAuthorized.class) == null;
                                    methodInfo.setCheckAuthorizedChannelId(matchingMethod.getAnnotation(CheckAuthorizedChannelId.class));
                                    methodInfo.setCheckAuthorizedUserId(matchingMethod.getAnnotation(CheckAuthorizedUserId.class));
                                }

                                methodInfo.setCheckAuthorized(checkAuthorized);
                            }

                            if (checkAuthorized) {
                                /*
                                 * We need to know what parameter index the channel/user ID resides
                                 * at so we can correctly include it with the authorization request.
                                 */
                                Integer channelIdIndex = methodInfo.getChannelIdIndex();
                                Integer userIdIndex = methodInfo.getUserIdIndex();

                                if (args.length > 0) {
                                    List<String> paramNames = methodInfo.getParamNames();
                                    if (paramNames == null) {
                                        paramNames = new ArrayList<String>();
                                        List<Integer> notFoundIndicies = new ArrayList<Integer>();

                                        /*
                                         * The Param annotation lets us know at runtime the name to
                                         * use when adding entries into the parameter map, which
                                         * will eventually be stored in the event logs.
                                         */
                                        int count = 0;
                                        for (Annotation[] paramAnnotations : method.getParameterAnnotations()) {
                                            boolean found = false;
                                            for (Annotation annotation : paramAnnotations) {
                                                if (annotation instanceof Param) {
                                                    Param param = (Param) annotation;
                                                    // Set the name to null if we're not including it in the parameter map
                                                    paramNames.add(param.excludeFromAudit() ? null : param.value());
                                                    found = true;
                                                    break;
                                                }
                                            }
                                            if (!found) {
                                                notFoundIndicies.add(count);
                                                paramNames.add(null);
                                            }
                                            count++;
                                        }

                                        // For each parameter name that wasn't found, replace it with a default name to use in the parameter map
                                        if (CollectionUtils.isNotEmpty(notFoundIndicies)) {
                                            for (Integer index : notFoundIndicies) {
                                                paramNames.set(index, getDefaultParamName(paramNames));
                                            }
                                        }

                                        methodInfo.setParamNames(paramNames);
                                    }

                                    // Add all arguments to the parameter map, except those that had excludeFromAudit enabled.
                                    for (int i = 0; i < args.length; i++) {
                                        String paramName = paramNames.get(i);
                                        if (paramName != null) {
                                            mirthServlet.addToParameterMap(paramNames.get(i), args[i]);
                                        }
                                    }

                                    if (channelIdIndex == null) {
                                        channelIdIndex = -1;
                                        if (methodInfo.getCheckAuthorizedChannelId() != null) {
                                            channelIdIndex = paramNames.indexOf(methodInfo.getCheckAuthorizedChannelId().paramName());
                                        }
                                        methodInfo.setChannelIdIndex(channelIdIndex);
                                    }

                                    if (userIdIndex == null) {
                                        userIdIndex = -1;
                                        if (methodInfo.getCheckAuthorizedUserId() != null) {
                                            userIdIndex = paramNames.indexOf(methodInfo.getCheckAuthorizedUserId().paramName());
                                        }
                                        methodInfo.setUserIdIndex(userIdIndex);
                                    }
                                }

                                // Authorize the request
                                if (channelIdIndex != null && channelIdIndex >= 0) {
                                    mirthServlet.checkUserAuthorized((String) args[channelIdIndex]);
                                } else if (userIdIndex != null && userIdIndex >= 0) {
                                    mirthServlet.checkUserAuthorized((Integer) args[userIdIndex], methodInfo.getCheckAuthorizedUserId().auditCurrentUser());
                                } else {
                                    mirthServlet.checkUserAuthorized();
                                }
                            }
                        } catch (Throwable t) {
                            Throwable converted = convertThrowable(t, new HashSet<Throwable>());
                            if (converted != null) {
                                t = converted;
                            }

                            if (!(t instanceof WebApplicationException)) {
                                t = new MirthApiException(t);
                            }
                            throw new InvocationTargetException(t);
                        }
                    }

                    try {
                        return method.invoke(proxy, args);
                    } catch (InvocationTargetException e) {
                        Throwable converted = convertThrowable(e, new HashSet<Throwable>());
                        if (converted != null && converted instanceof InvocationTargetException) {
                            e = (InvocationTargetException) converted;
                        }
                        throw e;
                    }
                } finally {
                    Thread.currentThread().setName(originalThreadName);
                }
            }
        };
    }

    private Throwable convertThrowable(Throwable t, Set<Throwable> visited) {
        // If the target is null or we've already seen it, ignore
        if (t == null || visited.contains(t)) {
            return null;
        }
        // Add the target to set of visited
        visited.add(t);

        // Recursively convert the causes
        Throwable cause = t.getCause();
        Throwable convertedCause = convertThrowable(cause, visited);

        if (t instanceof PersistenceException) {
            // Always convert this exception
            return new com.mirth.connect.client.core.api.PersistenceException(t.getMessage(), convertedCause != null ? convertedCause : cause);
        } else if (t instanceof WebApplicationException) {
            // This exception may contain an underlying exception stored in a JAX-RS response
            Response response = ((WebApplicationException) t).getResponse();
            Response convertedResponse = null;
            if (response != null && response.hasEntity()) {
                Object entity = response.getEntity();
                if (entity instanceof Throwable) {
                    Object convertedEntity = convertThrowable((Throwable) entity, visited);
                    if (convertedEntity != null) {
                        convertedResponse = Response.fromResponse(response).entity(convertedEntity).build();
                    }
                }
            }

            if (convertedResponse != null) {
                return new MirthApiException(convertedResponse);
            } else if (convertedCause != null) {
                return new MirthApiException(convertedCause);
            }
        } else if (t instanceof InvocationTargetException) {
            // Ensure that this exception always has a cause of WebApplicationException
            if (convertedCause != null) {
                if (!(convertedCause instanceof WebApplicationException)) {
                    convertedCause = new MirthApiException(convertedCause);
                }
                return new InvocationTargetException(convertedCause);
            } else if (cause != null && !(cause instanceof WebApplicationException)) {
                return new InvocationTargetException(new MirthApiException(cause));
            }
        } else if (convertedCause != null) {
            // Any other types, just construct a new instance with the converted cause
            try {
                try {
                    return t.getClass().getConstructor(String.class, Throwable.class).newInstance(t.getMessage(), convertedCause);
                } catch (Throwable t2) {
                    // Ignore and return null
                }
                return t.getClass().getConstructor(Throwable.class).newInstance(convertedCause);
            } catch (Throwable t3) {
                // Ignore and return null
            }
        }

        return null;
    }

    private String getDefaultParamName(List<String> paramNames) {
        int count = 0;
        String name;
        do {
            name = "arg" + count;
            count++;
        } while (paramNames.contains(name));
        return name;
    }

    private class MethodInfo {
        private Operation operation;
        private Boolean checkAuthorized;
        private CheckAuthorizedChannelId checkAuthorizedChannelId;
        private CheckAuthorizedUserId checkAuthorizedUserId;
        private List<String> paramNames;
        private Integer channelIdIndex;
        private Integer userIdIndex;

        public Operation getOperation() {
            return operation;
        }

        public void setOperation(Operation operation) {
            this.operation = operation;
        }

        public Boolean getCheckAuthorized() {
            return checkAuthorized;
        }

        public void setCheckAuthorized(Boolean checkAuthorized) {
            this.checkAuthorized = checkAuthorized;
        }

        public CheckAuthorizedChannelId getCheckAuthorizedChannelId() {
            return checkAuthorizedChannelId;
        }

        public void setCheckAuthorizedChannelId(CheckAuthorizedChannelId checkAuthorizedChannelId) {
            this.checkAuthorizedChannelId = checkAuthorizedChannelId;
        }

        public CheckAuthorizedUserId getCheckAuthorizedUserId() {
            return checkAuthorizedUserId;
        }

        public void setCheckAuthorizedUserId(CheckAuthorizedUserId checkAuthorizedUserId) {
            this.checkAuthorizedUserId = checkAuthorizedUserId;
        }

        public List<String> getParamNames() {
            return paramNames;
        }

        public void setParamNames(List<String> paramNames) {
            this.paramNames = paramNames;
        }

        public Integer getChannelIdIndex() {
            return channelIdIndex;
        }

        public void setChannelIdIndex(Integer channelIdIndex) {
            this.channelIdIndex = channelIdIndex;
        }

        public Integer getUserIdIndex() {
            return userIdIndex;
        }

        public void setUserIdIndex(Integer userIdIndex) {
            this.userIdIndex = userIdIndex;
        }
    }
}