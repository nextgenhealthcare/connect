/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.ac.js;

import javax.swing.Icon;

import org.apache.commons.lang3.StringUtils;
import org.fife.rsta.ac.js.IconFactory;
import org.fife.rsta.ac.js.completion.JavascriptBasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

import com.mirth.connect.client.ui.components.rsta.ac.MirthCompletion;
import com.mirth.connect.client.ui.reference.ClassReference;
import com.mirth.connect.client.ui.reference.CodeReference;
import com.mirth.connect.client.ui.reference.Reference;
import com.mirth.connect.client.ui.reference.VariableReference;
import com.mirth.connect.model.codetemplates.CodeTemplateContextSet;

public class MirthJavaScriptBasicCompletion extends JavascriptBasicCompletion implements MirthCompletion {

    protected String id;
    protected CodeTemplateContextSet contextSet;
    protected String summary;
    protected String inputText;
    protected String iconName;

    public MirthJavaScriptBasicCompletion(CompletionProvider provider, VariableReference reference) {
        this(provider, reference.getId(), reference.getContextSet(), reference.getReplacementCode(), reference.getReplacementCode(), reference.getName(), reference.getSummary(), reference.getIconName());
    }

    public MirthJavaScriptBasicCompletion(CompletionProvider provider, CodeReference reference) {
        this(provider, reference.getId(), reference.getContextSet(), reference.getName(), reference.getReplacementCode(), null, reference.getSummary(), reference.getIconName());
    }

    public MirthJavaScriptBasicCompletion(CompletionProvider provider, ClassReference reference) {
        this(provider, reference.getId(), reference.getContextSet(), reference.getName(), reference.getName(), null, reference.getSummary(), reference.getIconName());
    }

    public MirthJavaScriptBasicCompletion(CompletionProvider provider, ClassReference reference, String alias) {
        this(provider, reference.getId(), reference.getContextSet(), alias, alias, reference.getName(), reference.getSummary(), reference.getIconName());
    }

    private MirthJavaScriptBasicCompletion(CompletionProvider provider, String id, CodeTemplateContextSet contextSet, String inputText, String replacementText, String shortDesc, String summary, String iconName) {
        super(provider, replacementText, shortDesc, summary);
        this.id = id;
        this.contextSet = contextSet;
        this.summary = summary;
        this.inputText = inputText;
        this.iconName = iconName;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public CodeTemplateContextSet getContextSet() {
        return contextSet;
    }

    @Override
    public String getInputText() {
        return StringUtils.defaultString(inputText, super.getInputText());
    }

    @Override
    public Icon getIcon() {
        if (StringUtils.isNotBlank(iconName)) {
            return IconFactory.getIcon(iconName);
        }
        return IconFactory.getIcon(IconFactory.getEmptyIcon());
    }

    @Override
    public String getToolTipText() {
        String summary = getSummary();
        if (StringUtils.isNotBlank(summary)) {
            return summary.replaceAll("<a\\s*(\\s[^>]*)\\s*(?<!/)>", "<span>").replaceAll("<a\\s*(\\s[^>]*)\\s*/\\s*>", "<span/>");
        }

        return null;
    }

    @Override
    public int getRelevance() {
        return 75;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MirthCompletion) {
            return ((MirthCompletion) obj).getId().equals(id);
        } else if (obj instanceof Reference) {
            return ((Reference) obj).getId().equals(id);
        }
        return false;
    }
}