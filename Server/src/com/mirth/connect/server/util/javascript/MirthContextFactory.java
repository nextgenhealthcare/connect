/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.util.javascript;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;

import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class MirthContextFactory extends ContextFactory {

    private String id;
    private URL[] urls;
    private Set<String> resourceIds;
    private ScriptableObject sealedSharedScope;
    private ObjectXMLSerializer serializer;
    private ClassLoader isolatedClassLoader;
    private int languageVersion = Context.VERSION_DEFAULT;

    public MirthContextFactory(URL[] urls, Set<String> resourceIds) {
        this.id = UUID.randomUUID().toString();
        this.urls = urls;
        this.resourceIds = resourceIds;

        ClassLoader classLoader = null;
        if (ArrayUtils.isNotEmpty(urls)) {
            classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        } else {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        initApplicationClassLoader(classLoader);

        ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

        Integer rhinoLanguageVersion = configurationController.getRhinoLanguageVersion();
        if (rhinoLanguageVersion != null) {
            try {
                Context.checkLanguageVersion(rhinoLanguageVersion);
                languageVersion = rhinoLanguageVersion;
            } catch (Exception e) {
                // Ignore
            }
        }

        sealedSharedScope = JavaScriptScopeUtil.createSealedSharedScope(this);
        serializer = new ObjectXMLSerializer(classLoader);
        try {
            serializer.init(configurationController.getServerVersion());
        } catch (Exception e) {
        }
        serializer.processAnnotations(ObjectXMLSerializer.getExtraAnnotatedClasses());
    }

    public String getId() {
        return id;
    }

    public Set<String> getResourceIds() {
        return resourceIds;
    }

    public ClassLoader getIsolatedClassLoader() {
        if (isolatedClassLoader == null && ArrayUtils.isNotEmpty(urls)) {
            synchronized (this) {
                if (isolatedClassLoader == null) {
                    isolatedClassLoader = new URLClassLoader(urls, null);
                }
            }
        }
        return isolatedClassLoader;
    }

    public ScriptableObject getSealedSharedScope() {
        return sealedSharedScope;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    @Override
    protected Context makeContext() {
        return new MirthContext(this, languageVersion);
    }
}