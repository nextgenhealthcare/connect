/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If this annotation is present on a method, when authorization auditing is done an additional
 * check will be done to ensure that either the user has access to manage other users, or if not
 * whether the current user ID is the same as the one passed into the request.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckAuthorizedUserId {

    /**
     * The name of the parameter ({@link com.mirth.connect.client.core.api.Param @Param} annotation
     * on the servlet interface) to use for the user ID.
     */
    String paramName() default "userId";

    boolean auditCurrentUser() default true;
}