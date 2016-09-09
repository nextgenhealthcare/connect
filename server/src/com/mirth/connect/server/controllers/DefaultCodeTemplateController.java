/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplateLibrary;
import com.mirth.connect.model.CodeTemplateLibrarySaveResult;
import com.mirth.connect.model.CodeTemplateLibrarySaveResult.CodeTemplateUpdateResult;
import com.mirth.connect.model.CodeTemplateLibrarySaveResult.LibraryUpdateResult;
import com.mirth.connect.model.CodeTemplateSummary;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.plugins.CodeTemplateServerPlugin;
import com.mirth.connect.server.ExtensionLoader;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.JavaScriptContextUtil;

public class DefaultCodeTemplateController extends CodeTemplateController {

    private static CodeTemplateController instance = null;

    private Logger logger = Logger.getLogger(getClass());
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
    private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
    private ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();
    private Cache<CodeTemplateLibrary> libraryCache = new Cache<CodeTemplateLibrary>("Code Template Library", "CodeTemplate.getLibraryRevision", "CodeTemplate.getLibrary");
    private Cache<CodeTemplate> codeTemplateCache = new Cache<CodeTemplate>("Code Template", "CodeTemplate.getCodeTemplateRevision", "CodeTemplate.getCodeTemplate", false);

    private DefaultCodeTemplateController() {}

    public static CodeTemplateController create() {
        synchronized (DefaultCodeTemplateController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(CodeTemplateController.class);

                if (instance == null) {
                    instance = new DefaultCodeTemplateController();
                }
            }

            return instance;
        }
    }

    @Override
    public List<CodeTemplateLibrary> getLibraries(Set<String> libraryIds, boolean includeCodeTemplates) throws ControllerException {
        logger.debug("Getting code template libraries, libraryIds=" + String.valueOf(libraryIds));
        if (CollectionUtils.isEmpty(libraryIds)) {
            libraryIds = null;
        } else {
            libraryIds = new HashSet<String>(libraryIds);
        }

        Map<String, CodeTemplateLibrary> libraryMap = libraryCache.getAllItems();
        List<CodeTemplateLibrary> libraries = new ArrayList<CodeTemplateLibrary>();
        Map<String, CodeTemplate> codeTemplateMap = codeTemplateCache.getAllItems();

        for (CodeTemplateLibrary library : libraryMap.values()) {
            if (libraryIds == null || libraryIds.contains(library.getId())) {
                addCodeTemplatesToLibrary(library, codeTemplateMap);
                if (!includeCodeTemplates) {
                    library.replaceCodeTemplatesWithIds();
                }

                libraries.add(library);

                if (libraryIds != null) {
                    libraryIds.remove(library.getId());
                }
            }

            for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                codeTemplateMap.remove(codeTemplate.getId());
            }
        }

        if (libraryIds != null) {
            for (String libraryId : libraryIds) {
                logger.warn("Cannot find code template library, it may have been removed: " + libraryId);
            }
        }

        return libraries;
    }

    private void addCodeTemplatesToLibrary(CodeTemplateLibrary library, Map<String, CodeTemplate> codeTemplateMap) {
        List<CodeTemplate> codeTemplates = new ArrayList<CodeTemplate>();

        for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
            if (codeTemplateMap.containsKey(codeTemplate.getId())) {
                codeTemplates.add(codeTemplateMap.get(codeTemplate.getId()));
            } else {
                logger.warn("Cannot find code template, it may have been removed: " + codeTemplate.getId());
            }
        }

        library.setCodeTemplates(codeTemplates);
        library.sortCodeTemplates();
    }

    @Override
    public CodeTemplateLibrary getLibraryById(String libraryId) throws ControllerException {
        return libraryCache.getCachedItemById(libraryId);
    }

    @Override
    public CodeTemplateLibrary getLibraryByName(String libraryName) throws ControllerException {
        return libraryCache.getCachedItemByName(libraryName);
    }

    @Override
    public synchronized boolean updateLibraries(List<CodeTemplateLibrary> libraries, ServerEventContext context, boolean override) throws ControllerException {
        Map<String, CodeTemplateLibrary> libraryMap = libraryCache.getAllItems();
        List<CodeTemplateLibrary> librariesToRemove = new ArrayList<CodeTemplateLibrary>(libraryMap.values());
        Map<String, String> codeTemplateMap = new HashMap<String, String>();
        Set<String> libraryNames = new HashSet<String>();
        Set<String> unchangedLibraryIds = new HashSet<String>();

        for (CodeTemplateLibrary library : libraries) {
            for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                // Make sure this library's code templates aren't contained in any other library
                if (codeTemplateMap.put(codeTemplate.getId(), library.getId()) != null) {
                    String errorMessage = "Code Template \"" + codeTemplate.getName() + "\" belongs to more than one library.";
                    logger.error(errorMessage);
                    throw new ControllerException(errorMessage);
                }
            }

            /*
             * Code templates are stored separately in the database. Only the code template ID is
             * needed when storing the library.
             */
            library.replaceCodeTemplatesWithIds();

            // Make sure there isn't another library with the same name
            if (!libraryNames.add(library.getName())) {
                String errorMessage = "There is already a code template library with the name " + library.getName();
                logger.error(errorMessage);
                throw new ControllerException(errorMessage);
            }

            CodeTemplateLibrary matchingLibrary = libraryMap.get(library.getId());

            if (matchingLibrary != null) {
                if (EqualsBuilder.reflectionEquals(library, matchingLibrary, "lastModified", "revision")) {
                    unchangedLibraryIds.add(library.getId());
                } else {
                    /*
                     * If it's not a new library, and its version is different from the one in the
                     * database (in case it has been changed on the server since the client started
                     * modifying it), and override is not enabled, then don't allow the update
                     */
                    if (!library.getRevision().equals(matchingLibrary.getRevision()) && !override) {
                        return false;
                    } else {
                        library.setRevision(matchingLibrary.getRevision() + 1);
                    }
                }

                // Either way, this library is not being removed
                librariesToRemove.remove(matchingLibrary);
            } else {
                // Always start at revision 1 for new libraries
                library.setRevision(1);
            }
        }

        // Remove libraries
        for (CodeTemplateLibrary library : librariesToRemove) {
            try {
                SqlConfig.getSqlSessionManager().delete("CodeTemplate.deleteLibrary", library.getId());

                if (DatabaseUtil.statementExists("CodeTemplate.vacuumLibraryTable")) {
                    SqlConfig.getSqlSessionManager().update("CodeTemplate.vacuumLibraryTable");
                }

                // Invoke the code template plugins
                for (CodeTemplateServerPlugin codeTemplateServerPlugin : extensionController.getCodeTemplateServerPlugins().values()) {
                    codeTemplateServerPlugin.remove(library, context);
                }
            } catch (Exception e) {
                throw new ControllerException(e);
            }
        }

        // Insert or update libraries
        for (CodeTemplateLibrary library : libraries) {
            // Only if it actually changed
            if (!unchangedLibraryIds.contains(library.getId())) {
                try {
                    library.setLastModified(Calendar.getInstance());

                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("id", library.getId());
                    params.put("name", library.getName());
                    params.put("revision", library.getRevision());
                    params.put("library", library);

                    // Put the new library in the database
                    if (getLibraryById(library.getId()) == null) {
                        logger.debug("Inserting code template library");
                        SqlConfig.getSqlSessionManager().insert("CodeTemplate.insertLibrary", params);
                    } else {
                        logger.debug("Updating code template library");
                        SqlConfig.getSqlSessionManager().update("CodeTemplate.updateLibrary", params);
                    }

                    // Invoke the code template plugins
                    for (CodeTemplateServerPlugin codeTemplateServerPlugin : extensionController.getCodeTemplateServerPlugins().values()) {
                        codeTemplateServerPlugin.save(library, context);
                    }
                } catch (Exception e) {
                    throw new ControllerException(e);
                }
            }
        }

        return true;
    }

    @Override
    public List<CodeTemplate> getCodeTemplates(Set<String> codeTemplateIds) throws ControllerException {
        logger.debug("Getting code templates, codeTemplateIds=" + String.valueOf(codeTemplateIds));
        if (CollectionUtils.isEmpty(codeTemplateIds)) {
            codeTemplateIds = null;
        }

        Map<String, CodeTemplate> codeTemplateMap = codeTemplateCache.getAllItems();
        List<CodeTemplate> codeTemplates = new ArrayList<CodeTemplate>();

        if (codeTemplateIds == null) {
            codeTemplates.addAll(codeTemplateMap.values());
        } else {
            for (String codeTemplateId : codeTemplateIds) {
                CodeTemplate codeTemplate = codeTemplateMap.get(codeTemplateId);
                if (codeTemplate == null) {
                    logger.error("Cannot find code template, it may have been removed: " + codeTemplateId);
                } else {
                    codeTemplates.add(codeTemplate);
                }
            }
        }

        return codeTemplates;
    }

    @Override
    public List<CodeTemplateSummary> getCodeTemplateSummary(Map<String, Integer> clientRevisions) throws ControllerException {
        logger.debug("Getting code template summary");
        List<CodeTemplateSummary> codeTemplateSummaries = new ArrayList<CodeTemplateSummary>();

        try {
            Map<String, CodeTemplate> serverCodeTemplates = codeTemplateCache.getAllItems();

            /*
             * Iterate through the cached code template list and check if a code template with the
             * id exists on the server. If it does, and the revision numbers aren't equal, then add
             * the code template to the updated list. Otherwise, if the code template is not found,
             * add it to the deleted list.
             */
            for (Entry<String, Integer> entry : clientRevisions.entrySet()) {
                String cachedCodeTemplateId = entry.getKey();
                CodeTemplateSummary summary = new CodeTemplateSummary(cachedCodeTemplateId);
                boolean addSummary = false;

                if (serverCodeTemplates.containsKey(cachedCodeTemplateId)) {
                    // If the revision numbers aren't equal, add the updated CodeTemplate object
                    CodeTemplate serverCodeTemplate = serverCodeTemplates.get(cachedCodeTemplateId);
                    Integer serverRevision = serverCodeTemplate.getRevision();

                    if (!serverRevision.equals(entry.getValue())) {
                        summary.setCodeTemplate(serverCodeTemplate);
                        addSummary = true;
                    }
                } else {
                    // If a code template with the ID is not found on the server, add it as deleted
                    summary.setDeleted(true);
                    addSummary = true;
                }

                if (addSummary) {
                    codeTemplateSummaries.add(summary);
                }
            }

            /*
             * Add summaries for any entries on the server but not in the client's cache.
             */
            for (Entry<String, CodeTemplate> serverEntry : serverCodeTemplates.entrySet()) {
                if (!clientRevisions.containsKey(serverEntry.getKey())) {
                    codeTemplateSummaries.add(new CodeTemplateSummary(serverEntry.getKey(), serverEntry.getValue()));
                }
            }

            return codeTemplateSummaries;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    @Override
    public CodeTemplate getCodeTemplateById(String codeTemplateId) throws ControllerException {
        return codeTemplateCache.getCachedItemById(codeTemplateId);
    }

    @Override
    public Map<String, Integer> getCodeTemplateRevisionsForChannel(String channelId) throws ControllerException {
        Map<String, Integer> revisions = new HashMap<String, Integer>();

        for (CodeTemplateLibrary library : getLibraries(null, true)) {
            if (library.getEnabledChannelIds().contains(channelId) || (library.isIncludeNewChannels() && !library.getDisabledChannelIds().contains(channelId))) {
                for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                    if (codeTemplate.isAddToScripts()) {
                        revisions.put(codeTemplate.getId(), codeTemplate.getRevision());
                    }
                }
            }
        }

        return revisions;
    }

    @Override
    public synchronized boolean updateCodeTemplate(CodeTemplate codeTemplate, ServerEventContext context, boolean override) throws ControllerException {
        CodeTemplate matchingCodeTemplate = getCodeTemplateById(codeTemplate.getId());
        int currentRevision = 0;

        if (matchingCodeTemplate != null) {
            if (EqualsBuilder.reflectionEquals(codeTemplate, matchingCodeTemplate, "lastModified", "revision")) {
                return true;
            }

            currentRevision = matchingCodeTemplate.getRevision();
        }

        /*
         * If it's not a new code template, and its version is different from the one in the
         * database (in case it has been changed on the server since the client started modifying
         * it), and override is not enabled
         */
        if ((currentRevision > 0) && (currentRevision != codeTemplate.getRevision()) && !override) {
            return false;
        } else {
            codeTemplate.setRevision(currentRevision + 1);
        }

        try {
            for (CodeTemplateLibrary library : getLibraries(null, true)) {
                Set<String> codeTemplateNames = new HashSet<String>();
                boolean found = false;

                for (CodeTemplate template : library.getCodeTemplates()) {
                    if (template.getId().equals(codeTemplate.getId())) {
                        found = true;
                    } else {
                        codeTemplateNames.add(template.getName());
                    }
                }

                if (found && codeTemplateNames.contains(codeTemplate.getName())) {
                    String errorMessage = "There is already a code template with the name " + codeTemplate.getName();
                    logger.error(errorMessage);
                    throw new ControllerException(errorMessage);
                }
            }

            // Check on the server side to ensure that the code template doesn't have syntax errors
            String validationMessage = null;
            Throwable validationCause = null;
            try {
                JavaScriptContextUtil.getGlobalContextForValidation().compileString("function rhinoWrapper() {" + codeTemplate.getCode() + "\n}", UUID.randomUUID().toString(), 1, null);
            } catch (EvaluatorException e) {
                validationMessage = "Error on line " + e.lineNumber() + ": " + e.getMessage() + ".";
                validationCause = e;
            } catch (Exception e) {
                validationMessage = "Unknown error occurred during validation.";
                validationCause = e;
            } finally {
                Context.exit();
            }

            if (validationMessage != null) {
                String errorMessage = "Unable to save code template \"" + codeTemplate.getName() + "\": " + validationMessage;
                logger.error(errorMessage, validationCause);
                throw new ControllerException(errorMessage, validationCause);
            }

            codeTemplate.setLastModified(Calendar.getInstance());

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", codeTemplate.getId());
            params.put("name", codeTemplate.getName());
            params.put("revision", codeTemplate.getRevision());
            params.put("codeTemplate", codeTemplate);

            // Put the new code template in the database
            if (getCodeTemplateById(codeTemplate.getId()) == null) {
                logger.debug("Inserting code template");
                SqlConfig.getSqlSessionManager().insert("CodeTemplate.insertCodeTemplate", params);
            } else {
                logger.debug("Updating code template");
                SqlConfig.getSqlSessionManager().update("CodeTemplate.updateCodeTemplate", params);
            }

            // Invoke the code template plugins
            for (CodeTemplateServerPlugin codeTemplateServerPlugin : extensionController.getCodeTemplateServerPlugins().values()) {
                codeTemplateServerPlugin.save(codeTemplate, context);
            }

            // Re-compile the global scripts
            scriptController.compileGlobalScripts(contextFactoryController.getGlobalScriptContextFactory());

            return true;
        } catch (Exception e) {
            if (e instanceof ControllerException) {
                throw (ControllerException) e;
            }
            throw new ControllerException(e);
        }
    }

    @Override
    public synchronized void removeCodeTemplate(String codeTemplateId, ServerEventContext context) throws ControllerException {
        // Do a lookup to get the latest model object
        CodeTemplate codeTemplate = getCodeTemplateById(codeTemplateId);
        if (codeTemplate == null) {
            return;
        }

        try {
            SqlConfig.getSqlSessionManager().delete("CodeTemplate.deleteCodeTemplate", codeTemplate.getId());

            if (DatabaseUtil.statementExists("CodeTemplate.vacuumCodeTemplateTable")) {
                SqlConfig.getSqlSessionManager().update("CodeTemplate.vacuumCodeTemplateTable");
            }

            // Invoke the code template plugins
            for (CodeTemplateServerPlugin codeTemplateServerPlugin : extensionController.getCodeTemplateServerPlugins().values()) {
                codeTemplateServerPlugin.remove(codeTemplate, context);
            }

            // Re-compile the global scripts
            scriptController.compileGlobalScripts(contextFactoryController.getGlobalScriptContextFactory());
        } catch (Exception e) {
            throw new ControllerException(e);
        }

        // Update any libraries that were using the code template
        List<CodeTemplateLibrary> libraries = new ArrayList<CodeTemplateLibrary>(libraryCache.getAllItems().values());
        boolean changed = false;

        for (CodeTemplateLibrary library : libraries) {
            for (Iterator<CodeTemplate> it = library.getCodeTemplates().iterator(); it.hasNext();) {
                if (it.next().getId().equals(codeTemplate.getId())) {
                    it.remove();
                    changed = true;
                }
            }
        }

        if (changed) {
            updateLibraries(libraries, context, true);
        }
    }

    @Override
    public synchronized CodeTemplateLibrarySaveResult updateLibrariesAndTemplates(List<CodeTemplateLibrary> libraries, Set<String> removedLibraryIds, List<CodeTemplate> updatedCodeTemplates, Set<String> removedCodeTemplateIds, ServerEventContext context, boolean override) {
        // If override is disabled, first check all libraries and templates to make sure they haven't been modified already
        if (!override) {
            Map<String, CodeTemplateLibrary> libraryMap = libraryCache.getAllItems();

            for (CodeTemplateLibrary library : libraries) {
                CodeTemplateLibrary matchingLibrary = libraryMap.get(library.getId());

                if (matchingLibrary != null) {
                    if (!EqualsBuilder.reflectionEquals(library, matchingLibrary, "lastModified", "revision")) {
                        /*
                         * If it's not a new library, and its version is different from the one in
                         * the database (in case it has been changed on the server since the client
                         * started modifying it), and override is not enabled, then don't allow the
                         * update
                         */
                        if (!library.getRevision().equals(matchingLibrary.getRevision())) {
                            return new CodeTemplateLibrarySaveResult(true);
                        }
                    }

                    // If a matching library was found, always remove it from the map
                    libraryMap.remove(library.getId());
                }
            }

            // Remove any libraries that were expected to be removed
            for (String removedLibraryId : removedLibraryIds) {
                libraryMap.remove(removedLibraryId);
            }

            // If any libraries are left, the client is out of sync
            if (!libraryMap.isEmpty()) {
                return new CodeTemplateLibrarySaveResult(true);
            }

            Map<String, CodeTemplate> codeTemplateMap = codeTemplateCache.getAllItems();

            for (CodeTemplate codeTemplate : updatedCodeTemplates) {
                CodeTemplate matchingCodeTemplate = codeTemplateMap.get(codeTemplate.getId());

                if (matchingCodeTemplate != null) {
                    if (!EqualsBuilder.reflectionEquals(codeTemplate, matchingCodeTemplate, "lastModified", "revision")) {
                        /*
                         * If it's not a new code template, and its version is different from the
                         * one in the database (in case it has been changed on the server since the
                         * client started modifying it), and override is not enabled, then don't
                         * allow the update
                         */
                        if (!matchingCodeTemplate.getRevision().equals(codeTemplate.getRevision())) {
                            return new CodeTemplateLibrarySaveResult(true);
                        }
                    }
                }
            }
        }

        CodeTemplateLibrarySaveResult updateSummary = new CodeTemplateLibrarySaveResult();

        try {
            updateSummary.setLibrariesSuccess(updateLibraries(libraries, context, override));

            for (CodeTemplateLibrary library : libraries) {
                LibraryUpdateResult result = new LibraryUpdateResult();
                result.setNewRevision(library.getRevision());
                result.setNewLastModified(library.getLastModified());
                updateSummary.getLibraryResults().put(library.getId(), result);
            }
        } catch (Throwable t) {
            updateSummary.setLibrariesSuccess(false);
            updateSummary.setLibrariesCause(convertUpdateCause(t));

            // If updating the libraries failed, don't go any further
            return updateSummary;
        }

        // Try updating each code template, storing the result in the summary
        for (CodeTemplate codeTemplate : updatedCodeTemplates) {
            CodeTemplateUpdateResult result = new CodeTemplateUpdateResult();

            try {
                result.setSuccess(updateCodeTemplate(codeTemplate, context, override));
                result.setNewRevision(codeTemplate.getRevision());
                result.setNewLastModified(codeTemplate.getLastModified());
            } catch (Throwable t) {
                result.setSuccess(false);
                result.setCause(convertUpdateCause(t));
            }

            updateSummary.getCodeTemplateResults().put(codeTemplate.getId(), result);
        }

        // Try removing each code template, storing the result in the summary
        for (String removedCodeTemplateId : removedCodeTemplateIds) {
            CodeTemplateUpdateResult result = new CodeTemplateUpdateResult();

            try {
                removeCodeTemplate(removedCodeTemplateId, context);
                result.setSuccess(true);
            } catch (Throwable t) {
                result.setSuccess(false);
                result.setCause(convertUpdateCause(t));
            }

            updateSummary.getCodeTemplateResults().put(removedCodeTemplateId, result);
        }

        return updateSummary;
    }

    private Throwable convertUpdateCause(Throwable t) {
        if (t instanceof ControllerException) {
            if (t.getCause() != null) {
                t = t.getCause();
            } else {
                StackTraceElement[] stackTrace = t.getStackTrace();
                t = new Exception(t.getMessage());
                t.setStackTrace(stackTrace);
            }
        }

        return t;
    }
}
