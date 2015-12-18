/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.reflections.Reflections;

import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;

public class OperationUtil {

    public static Operation getOperation(Class<?> servletInterface, Method method) {
        return getOperation(servletInterface, method.getName(), method.getParameterTypes());
    }

    public static Operation getOperation(Class<?> servletInterface, String methodName, Class<?>... parameterTypes) {
        try {
            Method matchingMethod = servletInterface.getMethod(methodName, parameterTypes);
            MirthOperation annotation = matchingMethod.getAnnotation(MirthOperation.class);
            if (annotation != null) {
                return new Operation(annotation.name(), annotation.display(), annotation.type(), annotation.auditable());
            }
        } catch (Exception e) {
        }

        return null;
    }

    public static Set<Operation> getOperations(Class<?> servletInterface) {
        Set<Operation> operations = new HashSet<Operation>();
        for (Method method : servletInterface.getMethods()) {
            MirthOperation annotation = method.getAnnotation(MirthOperation.class);
            if (annotation != null) {
                operations.add(new Operation(annotation.name(), annotation.display(), annotation.type(), annotation.auditable()));
            }
        }
        return operations;
    }

    public static Set<Operation> getAbortableOperations(Class<?> servletInterface) {
        Set<Operation> operations = new HashSet<Operation>();
        for (Method method : servletInterface.getMethods()) {
            MirthOperation annotation = method.getAnnotation(MirthOperation.class);
            if (annotation != null && annotation.abortable()) {
                operations.add(new Operation(annotation.name(), annotation.display(), annotation.type(), annotation.auditable()));
            }
        }
        return operations;
    }

    public static String[] getOperationNamesForPermission(String permissionName) {
        return getOperationNamesForPermission(permissionName, null);
    }

    public static String[] getOperationNamesForPermission(String permissionName, Class<?> servletInterface, String... extraOperationNames) {
        Set<String> operationNames = new HashSet<String>();

        for (Class<?> coreServletInterface : new Reflections("com.mirth.connect.client.core.api.servlets").getSubTypesOf(BaseServletInterface.class)) {
            addOperationName(permissionName, coreServletInterface, operationNames);
        }

        if (servletInterface != null) {
            addOperationName(permissionName, servletInterface, operationNames);
        }

        if (ArrayUtils.isNotEmpty(extraOperationNames)) {
            operationNames.addAll(Arrays.asList(extraOperationNames));
        }

        return operationNames.toArray(new String[operationNames.size()]);
    }

    private static void addOperationName(String permissionName, Class<?> servletInterface, Set<String> operationNames) {
        for (Method method : servletInterface.getMethods()) {
            MirthOperation operationAnnotation = method.getAnnotation(MirthOperation.class);
            if (operationAnnotation != null && operationAnnotation.permission().equals(permissionName)) {
                operationNames.add(operationAnnotation.name());
            }
        }
    }
}