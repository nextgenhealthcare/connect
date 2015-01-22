/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.ac.js;

import org.fife.rsta.ac.java.JarManager;
import org.fife.rsta.ac.js.JavaScriptCompletionProvider;
import org.fife.rsta.ac.js.JavaScriptLanguageSupport;
import org.fife.rsta.ac.js.SourceCompletionProvider;

public class MirthJavaScriptCompletionProvider extends JavaScriptCompletionProvider {

    public MirthJavaScriptCompletionProvider(JarManager jarManager, JavaScriptLanguageSupport languageSupport) {
        super(jarManager, languageSupport);
    }

    public MirthJavaScriptCompletionProvider(SourceCompletionProvider provider, JarManager jarManager, JavaScriptLanguageSupport ls) {
        super(provider, jarManager, ls);
    }
}