/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mirth.connect.client.core.Operation.ExecuteType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MirthOperation {

    /**
     * Unique name of the operation.
     */
    String name();

    /**
     * Name of the operation to display in server events.
     */
    String display();

    /**
     * The name of the permission under which this operation should reside.
     */
    String permission() default "";

    /**
     * The type of client execution to use by default (though it may be overwritten). Only one SYNC
     * request is allowed at a time, while multiple ASYNC requests may execute simultaneously.
     */
    ExecuteType type() default ExecuteType.SYNC;

    /**
     * Determines whether the invocation of this method will generate a server event.
     */
    boolean auditable() default true;

    /**
     * If true, this operation may be aborted if the view is exited or the user logs out.
     */
    boolean abortable() default false;
}