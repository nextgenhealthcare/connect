/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.ac;

import java.util.List;

import javax.swing.text.JTextComponent;

import org.fife.ui.autocomplete.Completion;

import com.mirth.connect.client.ui.reference.Reference;

public interface MirthCompletionCacheInterface {

    public List<Completion> getClassCompletions(JTextComponent comp, String partialText);

    public List<Completion> getConstructorCompletions(JTextComponent comp, String partialText);

    public List<Completion> getFunctionCompletions(JTextComponent comp, String variableOrClassName, String partialText);

    public List<Completion> getGlobalFunctionCompletions(JTextComponent comp, String partialText);

    public List<Completion> getVariableCompletions(JTextComponent comp, String partialText);

    public List<Completion> getCodeCompletions(JTextComponent comp, String partialText);

    public void removeReferences(List<Reference> references);

    public void addReferences(List<Reference> references);
}