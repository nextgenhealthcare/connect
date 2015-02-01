/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.ac.js;

import org.fife.rsta.ac.js.JavaScriptCompletionProvider;
import org.fife.rsta.ac.js.JavaScriptLanguageSupport;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import com.mirth.connect.client.ui.components.rsta.ac.MirthCompletionCacheInterface;
import com.mirth.connect.client.ui.components.rsta.ac.MirthLanguageSupport;

public class MirthJavaScriptLanguageSupport extends JavaScriptLanguageSupport implements MirthLanguageSupport {

    private JavaScriptCompletionProvider provider;
    private MirthJavaScriptShorthandCompletionCache shorthandCompletionCache;

    public MirthJavaScriptLanguageSupport() {
        setXmlAvailable(true);
        setDefaultCompletionCellRenderer(new MirthJavaScriptCellRenderer());
    }

    @Override
    protected void installImpl(RSyntaxTextArea textArea, AutoCompletion ac) {
        super.installImpl(textArea, ac);
        if (provider != null) {
            textArea.setToolTipSupplier(provider);
        }
        ac.setAutoCompleteSingleChoices(false);
    }

    @Override
    protected JavaScriptCompletionProvider createJavaScriptCompletionProvider() {
        MirthSourceCompletionProvider sourceCompletionProvider = new MirthSourceCompletionProvider(isXmlAvailable());
        provider = new MirthJavaScriptCompletionProvider(sourceCompletionProvider, getJarManager(), this);
        shorthandCompletionCache = new MirthJavaScriptShorthandCompletionCache(sourceCompletionProvider, new DefaultCompletionProvider(), isXmlAvailable());
        provider.setShorthandCompletionCache(shorthandCompletionCache);
        sourceCompletionProvider.setCompletionCache(shorthandCompletionCache);
        return provider;
    }

    @Override
    public MirthCompletionCacheInterface getCompletionCache() {
        return shorthandCompletionCache;
    }

    @Override
    public AutoCompletion getAutoCompletionFor(RSyntaxTextArea textArea) {
        return super.getAutoCompletionFor(textArea);
    }
}
