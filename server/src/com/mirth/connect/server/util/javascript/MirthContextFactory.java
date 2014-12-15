/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.util.javascript;

import java.util.Set;
import java.util.UUID;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;

public class MirthContextFactory extends ContextFactory {

    private String id;
    private Set<String> resourceIds;
    private ScriptableObject sealedSharedScope;

    public MirthContextFactory(ClassLoader classLoader, Set<String> resourceIds) {
        this.id = UUID.randomUUID().toString();
        this.resourceIds = resourceIds;
        initApplicationClassLoader(classLoader);
        sealedSharedScope = JavaScriptScopeUtil.createSealedSharedScope(this);
    }

    public String getId() {
        return id;
    }

    public Set<String> getResourceIds() {
        return resourceIds;
    }

    public ScriptableObject getSealedSharedScope() {
        return sealedSharedScope;
    }

    @Override
    protected Context makeContext() {
        return new MirthContext(this);
    }
}