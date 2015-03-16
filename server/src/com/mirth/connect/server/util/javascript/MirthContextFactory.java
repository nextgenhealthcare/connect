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

import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ControllerFactory;

public class MirthContextFactory extends ContextFactory {

    private String id;
    private Set<String> resourceIds;
    private ScriptableObject sealedSharedScope;
    private ObjectXMLSerializer serializer;

    public MirthContextFactory(ClassLoader classLoader, Set<String> resourceIds) {
        this.id = UUID.randomUUID().toString();
        this.resourceIds = resourceIds;
        initApplicationClassLoader(classLoader);
        sealedSharedScope = JavaScriptScopeUtil.createSealedSharedScope(this);
        serializer = new ObjectXMLSerializer(classLoader);
        try {
            serializer.init(ControllerFactory.getFactory().createConfigurationController().getServerVersion());
        } catch (Exception e) {
        }
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

    public Serializer getSerializer() {
        return serializer;
    }

    @Override
    protected Context makeContext() {
        return new MirthContext(this);
    }
}