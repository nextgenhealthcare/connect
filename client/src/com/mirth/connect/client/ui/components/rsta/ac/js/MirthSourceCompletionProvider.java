/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.ac.js;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.fife.rsta.ac.js.SourceCompletionProvider;
import org.fife.ui.autocomplete.Completion;

import com.mirth.connect.client.ui.components.rsta.ac.MirthCompletionCacheInterface;

public class MirthSourceCompletionProvider extends SourceCompletionProvider {

    private static final Pattern PARENTHESES_PATTERN = Pattern.compile("\\([\\S\\s]*\\)");

    private MirthCompletionCacheInterface completionCache;
    private String lastCompletionsAtText = null;
    private List<Completion> lastParameterizedCompletionsAt = null;

    public MirthSourceCompletionProvider(boolean xmlSupported) {
        this(null, xmlSupported);
    }

    public MirthSourceCompletionProvider(String javaScriptEngine, boolean xmlSupported) {
        super(javaScriptEngine, xmlSupported);
    }

    public void setCompletionCache(MirthCompletionCacheInterface completionCache) {
        this.completionCache = completionCache;
    }

    @Override
    protected List<Completion> getCompletionsImpl(JTextComponent comp) {
        Set<Completion> set = new HashSet<Completion>(super.getCompletionsImpl(comp));

        String text = StringUtils.trim(getAlreadyEnteredText(comp));
        if (StringUtils.isNotBlank(text)) {
            int dot = text.lastIndexOf('.');

            if (dot > 0) {
                int nextToLastDot = text.lastIndexOf('.', dot - 1);
                String identifier;

                if (nextToLastDot >= 0) {
                    identifier = text.substring(nextToLastDot + 1, dot).trim();
                } else {
                    identifier = text.substring(0, dot).trim();
                }

                identifier = PARENTHESES_PATTERN.matcher(identifier).replaceAll("").trim();

                int index = identifier.lastIndexOf(' ');
                if (index >= 0) {
                    identifier = identifier.substring(index + 1);
                }

                String partialText = text.substring(dot + 1).trim();

                if (StringUtils.isNotBlank(identifier)) {
                    set.addAll(completionCache.getFunctionCompletions(comp, identifier, partialText));
                }
            } else if (text.equals("new") || text.startsWith("new ")) {
                String partialText = text.substring(3).trim();
                set.addAll(completionCache.getConstructorCompletions(comp, partialText));
            } else {
                set.addAll(completionCache.getVariableCompletions(comp, text));
                set.addAll(completionCache.getClassCompletions(comp, text));
                set.addAll(completionCache.getGlobalFunctionCompletions(comp, text));
                set.addAll(completionCache.getCodeCompletions(comp, text));
            }
        } else {
            set.addAll(completionCache.getVariableCompletions(comp, ""));
            set.addAll(completionCache.getClassCompletions(comp, ""));
            set.addAll(completionCache.getGlobalFunctionCompletions(comp, ""));
            set.addAll(completionCache.getCodeCompletions(comp, ""));
        }

        return new ArrayList<Completion>(set);
    }

    @Override
    public List<Completion> getCompletionsAt(JTextComponent tc, Point p) {
        Set<Completion> completions = new HashSet<Completion>();
        List<Completion> parentCompletions = super.getCompletionsAt(tc, p);
        if (CollectionUtils.isNotEmpty(parentCompletions)) {
            completions.addAll(parentCompletions);
        }

        int offset = tc.viewToModel(p);
        if (offset < 0 || offset >= tc.getDocument().getLength()) {
            lastCompletionsAtText = null;
            lastParameterizedCompletionsAt = null;
            return new ArrayList<Completion>(completions);
        }

        Segment s = new Segment();
        Document doc = tc.getDocument();
        Element root = doc.getDefaultRootElement();
        int line = root.getElementIndex(offset);
        Element elem = root.getElement(line);
        int start = elem.getStartOffset();
        int end = elem.getEndOffset() - 1;

        try {

            doc.getText(start, end - start, s);

            // Get the valid chars before the specified offset.
            int startOffs = s.offset + (offset - start) - 1;
            while (startOffs >= s.offset && Character.isLetterOrDigit(s.array[startOffs])) {
                startOffs--;
            }

            // Get the valid chars at and after the specified offset.
            int endOffs = s.offset + (offset - start);
            while (endOffs < s.offset + s.count && Character.isLetterOrDigit(s.array[endOffs])) {
                endOffs++;
            }

            int len = endOffs - startOffs - 1;
            if (len <= 0) {
                lastParameterizedCompletionsAt = null;
                return new ArrayList<Completion>(completions);
            }
            String text = new String(s.array, startOffs + 1, len);

            if (text.equals(lastCompletionsAtText)) {
                if (CollectionUtils.isNotEmpty(lastParameterizedCompletionsAt)) {
                    completions.addAll(lastParameterizedCompletionsAt);
                }
                return new ArrayList<Completion>(completions);
            }

            lastCompletionsAtText = text;
            lastParameterizedCompletionsAt = completionCache.getClassCompletions(tc, text);

            if (CollectionUtils.isNotEmpty(lastParameterizedCompletionsAt)) {
                completions.addAll(lastParameterizedCompletionsAt);
            }
            return new ArrayList<Completion>(completions);

        } catch (BadLocationException ble) {
            ble.printStackTrace(); // Never happens
        }

        lastCompletionsAtText = null;
        lastParameterizedCompletionsAt = null;

        return new ArrayList<Completion>(completions);
    }
}