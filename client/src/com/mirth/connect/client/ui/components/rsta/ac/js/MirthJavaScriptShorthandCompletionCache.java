/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.ac.js;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.JTextComponent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.fife.rsta.ac.js.JavaScriptShorthandCompletionCache;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;

import com.mirth.connect.client.ui.components.rsta.MirthRSyntaxTextArea;
import com.mirth.connect.client.ui.components.rsta.ac.MirthCompletion;
import com.mirth.connect.client.ui.components.rsta.ac.MirthCompletionCacheInterface;
import com.mirth.connect.client.ui.reference.ClassReference;
import com.mirth.connect.client.ui.reference.CodeReference;
import com.mirth.connect.client.ui.reference.ConstructorReference;
import com.mirth.connect.client.ui.reference.FunctionReference;
import com.mirth.connect.client.ui.reference.ParameterizedCodeReference;
import com.mirth.connect.client.ui.reference.Reference;
import com.mirth.connect.client.ui.reference.ReferenceListFactory;
import com.mirth.connect.client.ui.reference.VariableReference;
import com.mirth.connect.model.codetemplates.ContextType;

public class MirthJavaScriptShorthandCompletionCache extends JavaScriptShorthandCompletionCache implements MirthCompletionCacheInterface {

    private PartialHashMap<Completion> variableCompletionMap = new PartialHashMap<Completion>();
    private PartialHashMap<Completion> classCompletionMap = new PartialHashMap<Completion>();
    private PartialHashMap<Completion> constructorCompletionMap = new PartialHashMap<Completion>();
    private Map<String, PartialHashMap<Completion>> functionCompletionMap = new HashMap<String, PartialHashMap<Completion>>();
    private PartialHashMap<Completion> globalFunctionCompletionMap = new PartialHashMap<Completion>();
    private PartialHashMap<Completion> codeCompletionMap = new PartialHashMap<Completion>();

    public MirthJavaScriptShorthandCompletionCache(DefaultCompletionProvider templateProvider, DefaultCompletionProvider commentsProvider, boolean e4xSupport) {
        super(templateProvider, commentsProvider, e4xSupport);
        for (List<Reference> list : ReferenceListFactory.getInstance().getReferences().values()) {
            addReferences(list);
        }
    }

    @Override
    public List<Completion> getClassCompletions(JTextComponent comp, String partialText) {
        return getCompletions(comp, classCompletionMap, partialText);
    }

    @Override
    public List<Completion> getConstructorCompletions(JTextComponent comp, String partialText) {
        return getCompletions(comp, constructorCompletionMap, partialText);
    }

    @Override
    public List<Completion> getFunctionCompletions(JTextComponent comp, String variableOrClassName, String partialText) {
        PartialHashMap<Completion> map = functionCompletionMap.get(variableOrClassName);
        if (MapUtils.isNotEmpty(map)) {
            return getCompletions(comp, map, partialText);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Completion> getGlobalFunctionCompletions(JTextComponent comp, String partialText) {
        return getCompletions(comp, globalFunctionCompletionMap, partialText);
    }

    @Override
    public List<Completion> getVariableCompletions(JTextComponent comp, String partialText) {
        return getCompletions(comp, variableCompletionMap, partialText);
    }

    @Override
    public List<Completion> getCodeCompletions(JTextComponent comp, String partialText) {
        return getCompletions(comp, codeCompletionMap, partialText);
    }

    private List<Completion> getCompletions(JTextComponent comp, PartialHashMap<Completion> map, String partialText) {
        List<Completion> completions = map.getPartial(partialText);
        if (completions == null) {
            completions = Collections.emptyList();
        }

        ContextType contextType = null;
        if (comp instanceof MirthRSyntaxTextArea) {
            contextType = ((MirthRSyntaxTextArea) comp).getContextType();
        }

        if (contextType != null) {
            for (Iterator<Completion> it = completions.iterator(); it.hasNext();) {
                Completion completion = it.next();
                if (completion instanceof MirthCompletion && !((MirthCompletion) completion).getContextSet().contains(contextType)) {
                    it.remove();
                }
            }
        }

        return completions;
    }

    @Override
    public void removeReferences(List<Reference> references) {
        Set<String> referenceIds = new HashSet<String>();
        for (Reference reference : references) {
            referenceIds.add(reference.getId());
        }

        // Remove completions from the parent class
        for (Iterator<Completion> it = getShorthandCompletions().iterator(); it.hasNext();) {
            Completion completion = it.next();
            if (completion instanceof MirthCompletion && referenceIds.contains(((MirthCompletion) completion).getId())) {
                it.remove();
            }
        }

        // Remove completions in our custom maps
        for (Reference reference : references) {
            switch (reference.getType()) {
                case CLASS:
                    ClassReference classReference = (ClassReference) reference;
                    classCompletionMap.removeValue(classReference.getName(), classReference);
                    if (CollectionUtils.isNotEmpty(classReference.getAliases())) {
                        for (String alias : classReference.getAliases()) {
                            classCompletionMap.removeValue(alias, classReference);
                        }
                    }
                    break;
                case FUNCTION:
                    FunctionReference functionReference = (FunctionReference) reference;
                    if (functionReference.getFunctionDefinition() != null) {
                        String name = functionReference.getFunctionDefinition().getName();
                        String className = functionReference.getClassName();
                        List<String> beforeDotTextList = functionReference.getBeforeDotTextList();

                        PartialHashMap<Completion> map = functionCompletionMap.get(className);
                        if (map != null) {
                            map.removeValue(name, functionReference);
                        }

                        if (CollectionUtils.isNotEmpty(beforeDotTextList)) {
                            for (String beforeDotText : beforeDotTextList) {
                                map = functionCompletionMap.get(beforeDotText);
                                if (map != null) {
                                    map.removeValue(name, functionReference);
                                }
                            }
                        }

                        globalFunctionCompletionMap.removeValue(name, functionReference);
                    }
                    break;
                case VARIABLE:
                    variableCompletionMap.removeValue(reference.getName(), reference);
                    break;
                case CODE:
                    codeCompletionMap.removeValue(reference.getName(), reference);
                    break;
            }
        }
    }

    @Override
    public void addReferences(List<Reference> references) {
        if (CollectionUtils.isEmpty(references)) {
            return;
        }
        Completion completion;

        for (Reference reference : references) {
            switch (reference.getType()) {
                case CLASS:
                    ClassReference classReference = (ClassReference) reference;
                    completion = new MirthJavaScriptClassCompletion(getTemplateProvider(), (ClassReference) reference);
                    addShorthandCompletion(completion);
                    classCompletionMap.put(classReference.getName(), completion);

                    if (CollectionUtils.isNotEmpty(classReference.getAliases())) {
                        for (String alias : classReference.getAliases()) {
                            addShorthandCompletion(new MirthJavaScriptClassCompletion(getTemplateProvider(), (ClassReference) reference, alias));
                            classCompletionMap.put(alias, completion);
                        }
                    }
                    break;

                case FUNCTION:
                    FunctionReference functionReference = (FunctionReference) reference;
                    if (functionReference.getFunctionDefinition() != null) {
                        completion = new MirthJavaScriptFunctionCompletion(getTemplateProvider(), functionReference);

                        if (CollectionUtils.isNotEmpty(functionReference.getBeforeDotTextList())) {
                            for (String beforeDotText : functionReference.getBeforeDotTextList()) {
                                if (StringUtils.isBlank(beforeDotText)) {
                                    addShorthandCompletion(completion);
                                } else {
                                    PartialHashMap<Completion> map = functionCompletionMap.get(beforeDotText);
                                    if (map == null) {
                                        map = new PartialHashMap<Completion>();
                                        functionCompletionMap.put(beforeDotText, map);
                                    }

                                    map.put(functionReference.getFunctionDefinition().getName(), completion);
                                }
                            }
                        } else if (!(functionReference instanceof ConstructorReference) && functionReference.getClassName() == null) {
                            addShorthandCompletion(completion);
                            globalFunctionCompletionMap.put(functionReference.getFunctionDefinition().getName(), completion);
                        }

                        if (functionReference.getClassName() != null) {
                            PartialHashMap<Completion> map = functionCompletionMap.get(functionReference.getClassName());
                            if (map == null) {
                                map = new PartialHashMap<Completion>();
                                functionCompletionMap.put(functionReference.getClassName(), map);
                            }

                            map.put(functionReference.getFunctionDefinition().getName(), completion);
                        }

                        if (functionReference instanceof ConstructorReference) {
                            ConstructorReference constructorReference = (ConstructorReference) functionReference;
                            List<Completion> list = constructorCompletionMap.get(constructorReference.getClassName());
                            if (list == null) {
                                list = new ArrayList<Completion>();
                                constructorCompletionMap.put(constructorReference.getClassName(), list);
                            }

                            list.add(completion);
                        }
                    }
                    break;

                case VARIABLE:
                    completion = new MirthJavaScriptBasicCompletion(getTemplateProvider(), (VariableReference) reference);
                    addShorthandCompletion(completion);
                    variableCompletionMap.put(reference.getName(), completion);
                    break;

                case CODE:
                    if (reference instanceof ParameterizedCodeReference) {
                        completion = new MirthJavaScriptTemplateCompletion(getTemplateProvider(), (ParameterizedCodeReference) reference);
                    } else {
                        completion = new MirthJavaScriptBasicCompletion(getTemplateProvider(), (CodeReference) reference);
                    }

                    addShorthandCompletion(completion);
                    codeCompletionMap.put(reference.getName(), completion);
                    break;
            }
        }
    }
}