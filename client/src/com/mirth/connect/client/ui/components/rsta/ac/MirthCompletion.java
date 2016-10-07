/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.ac;

import org.fife.ui.autocomplete.Completion;

import com.mirth.connect.model.codetemplates.CodeTemplateContextSet;

public interface MirthCompletion extends Completion {

    public String getId();

    public CodeTemplateContextSet getContextSet();

    public boolean equals(Object obj);
}