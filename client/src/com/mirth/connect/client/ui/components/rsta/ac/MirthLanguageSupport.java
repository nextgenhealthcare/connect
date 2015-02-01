/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.ac;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public interface MirthLanguageSupport extends LanguageSupport {

    public MirthCompletionCacheInterface getCompletionCache();

    public AutoCompletion getAutoCompletionFor(RSyntaxTextArea textArea);
}