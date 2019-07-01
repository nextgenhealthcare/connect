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
 * check will be done to ensure the channel ID in question isn't redacted by the authorization
 * controller.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckAuthorizedChannelId {

    /**
     * The name of the parameter ({@link com.mirth.connect.client.core.api.Param @Param} annotation
     * on the servlet interface) to use for the channel ID.
     */
    String paramName() default "channelId";
}