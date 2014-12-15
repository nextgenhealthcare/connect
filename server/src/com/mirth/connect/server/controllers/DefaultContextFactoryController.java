/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.log4j.Logger;
import org.mozilla.javascript.ContextFactory;

import com.mirth.connect.model.LibraryProperties;
import com.mirth.connect.plugins.LibraryPlugin;
import com.mirth.connect.server.ExtensionLoader;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class DefaultContextFactoryController extends ContextFactoryController {

    private Logger logger = Logger.getLogger(getClass());
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
    private Map<String, LibraryProperties> libraryResources = new ConcurrentHashMap<String, LibraryProperties>();
    private Map<String, List<URL>> libraryCache = new ConcurrentHashMap<String, List<URL>>();
    private volatile Set<String> globalScriptResourceIds = new LinkedHashSet<String>();
    private Map<Set<String>, MirthContextFactory> contextFactoryMap = new ConcurrentHashMap<Set<String>, MirthContextFactory>();

    private static ContextFactoryController instance = null;

    protected DefaultContextFactoryController() {}

    public static ContextFactoryController create() {
        synchronized (DefaultContextFactoryController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(ContextFactoryController.class);

                if (instance == null) {
                    instance = new DefaultContextFactoryController();
                }
            }

            return instance;
        }
    }

    @Override
    public synchronized void initGlobalContextFactory() {
        logger.debug("Initializing global context factory.");
        ContextFactory.initGlobal(new MirthContextFactory(Thread.currentThread().getContextClassLoader(), new HashSet<String>()));
    }

    @Override
    public synchronized void updateResources(List<LibraryProperties> resources) throws Exception {
        logger.debug("Updating resources: " + String.valueOf(resources));
        Set<String> resourceIds = new HashSet<String>();
        Set<String> resourceIdsToReload = new HashSet<String>();
        Set<String> globalScriptResourceIds = new HashSet<String>();

        for (LibraryProperties resource : resources) {
            resourceIds.add(resource.getId());
            LibraryProperties currentResource = libraryResources.get(resource.getId());

            // Only replace the resource if anything changed
            if (!resource.equals(currentResource)) {
                // Only reload the resource libraries if anything REALLY changed
                if (!EqualsBuilder.reflectionEquals(resource, currentResource, new String[] {
                        "name", "description", "includeWithGlobalScripts" })) {
                    logger.debug("Resource " + String.valueOf(resource) + " has changed (besides name / description / global scripts).");
                    resourceIdsToReload.add(resource.getId());
                } else {
                    logger.debug("Resource " + String.valueOf(resource) + " has changed.");
                }

                LibraryPlugin plugin = (LibraryPlugin) extensionController.getResourcePlugins().get(resource.getPluginPointName());
                if (plugin != null) {
                    try {
                        plugin.update(resource);
                    } catch (Exception e) {
                        logger.error("Unable to update libraries: " + e.getMessage(), e);
                    }
                } else {
                    logger.error("Unable to update libraries: Plugin \"" + resource.getPluginPointName() + "\" not found.");
                }

                libraryResources.put(resource.getId(), resource);
            }

            if (resource.isIncludeWithGlobalScripts()) {
                globalScriptResourceIds.add(resource.getId());
            }
        }

        for (LibraryProperties resource : libraryResources.values().toArray(new LibraryProperties[libraryResources.size()])) {
            if (!resourceIds.contains(resource.getId())) {
                logger.debug("Removing resource " + String.valueOf(resource) + ".");
                resourceIdsToReload.add(resource.getId());
                libraryResources.remove(resource.getId());

                LibraryPlugin plugin = (LibraryPlugin) extensionController.getResourcePlugins().get(resource.getPluginPointName());
                if (plugin != null) {
                    try {
                        plugin.remove(resource);
                    } catch (Exception e) {
                        logger.error("Unable to remove library resource: " + e.getMessage(), e);
                    }
                } else {
                    logger.error("Unable to remove library resource: Plugin \"" + resource.getPluginPointName() + "\" not found.");
                }
            }
        }

        if (!this.globalScriptResourceIds.equals(globalScriptResourceIds)) {
            logger.debug("Global script resource IDs have changed: " + String.valueOf(globalScriptResourceIds));
            this.globalScriptResourceIds = globalScriptResourceIds;
        }

        reloadResources(resourceIdsToReload);
    }

    @Override
    public MirthContextFactory getGlobalContextFactory() {
        return (MirthContextFactory) ContextFactory.getGlobal();
    }

    @Override
    public MirthContextFactory getGlobalScriptContextFactory() throws Exception {
        return getContextFactory(globalScriptResourceIds);
    }

    @Override
    public MirthContextFactory getContextFactory(Set<String> libraryResourceIds) throws Exception {
        if (CollectionUtils.isNotEmpty(libraryResourceIds)) {
            redactResourceIds(libraryResourceIds);

            MirthContextFactory contextFactory = contextFactoryMap.get(libraryResourceIds);

            if (contextFactory == null) {
                synchronized (contextFactoryMap) {
                    contextFactory = contextFactoryMap.get(libraryResourceIds);

                    if (contextFactory == null) {
                        resetContextFactory(libraryResourceIds);
                        contextFactory = contextFactoryMap.get(libraryResourceIds);
                    }
                }
            }

            return contextFactory;
        } else {
            return getGlobalContextFactory();
        }
    }

    @Override
    public void reloadResource(String resourceId) throws Exception {
        reloadResources(Collections.singleton(resourceId));
    }

    @Override
    public List<URL> getLibraries(String resourceId) throws Exception {
        return getLibraries(Collections.singleton(resourceId), false);
    }

    private void reloadResources(Set<String> resourceIds) throws Exception {
        for (String resourceId : resourceIds) {
            getLibraries(Collections.singleton(resourceId), true);
        }

        for (Set<String> libraryResourceIds : contextFactoryMap.keySet().toArray(new Set[contextFactoryMap.size()])) {
            Set<String> intersection = new LinkedHashSet<String>(libraryResourceIds);
            if (resourceIds != null) {
                intersection.retainAll(resourceIds);
            }

            if (CollectionUtils.isNotEmpty(intersection)) {
                MirthContextFactory contextFactory = contextFactoryMap.get(libraryResourceIds);

                if (contextFactory != null) {
                    resetContextFactory(libraryResourceIds);
                }
            }
        }
    }

    private void resetContextFactory(Set<String> libraryResourceIds) throws Exception {
        logger.debug("Resetting context factory: libraryResourceIds=" + String.valueOf(libraryResourceIds));
        List<URL> libraries = getLibraries(libraryResourceIds, false);

        MirthContextFactory contextFactory;

        if (CollectionUtils.isNotEmpty(libraries)) {
            // Only create a new context factory if libraries are being used
            contextFactory = new MirthContextFactory(new URLClassLoader(libraries.toArray(new URL[libraries.size()]), Thread.currentThread().getContextClassLoader()), libraryResourceIds);
        } else {
            contextFactory = getGlobalContextFactory();
        }

        contextFactoryMap.put(libraryResourceIds, contextFactory);
    }

    private void redactResourceIds(Set<String> libraryResourceIds) {
        for (Iterator<String> it = libraryResourceIds.iterator(); it.hasNext();) {
            if (!libraryResources.containsKey(it.next())) {
                it.remove();
            }
        }
    }

    private List<URL> getLibraries(Set<String> libraryResourceIds, boolean reload) throws Exception {
        List<URL> libraries = new ArrayList<URL>();

        for (Iterator<String> it = libraryResourceIds.iterator(); it.hasNext();) {
            String resourceId = it.next();
            LibraryProperties properties = libraryResources.get(resourceId);

            if (properties != null) {
                List<URL> cachedLibraries = null;
                if (!reload) {
                    cachedLibraries = libraryCache.get(resourceId);
                }

                if (cachedLibraries != null) {
                    libraries.addAll(cachedLibraries);
                } else {
                    logger.debug("Reloading libraries for resource " + String.valueOf(properties) + ".");
                    LibraryPlugin plugin = (LibraryPlugin) extensionController.getResourcePlugins().get(properties.getPluginPointName());

                    if (plugin != null) {
                        List<URL> loadedLibraries = plugin.getLibraries(properties);
                        libraryCache.put(resourceId, loadedLibraries);
                        libraries.addAll(loadedLibraries);
                        logger.debug("Libraries reloaded for resource " + String.valueOf(properties) + ": " + String.valueOf(loadedLibraries));
                    } else {
                        logger.error("Unable to load libraries: Plugin \"" + properties.getPluginPointName() + "\" not found.");
                        it.remove();
                    }
                }
            } else {
                logger.error("Unable to load libraries: Resource ID " + resourceId + " not found.");
                it.remove();
            }
        }

        return libraries;
    }
}