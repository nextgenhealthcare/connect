/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.DomReader;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;

public class MirthDomReader extends DomReader {

    public MirthDomReader(Element rootElement) {
        super(rootElement);
    }

    public MirthDomReader(Document document) {
        super(document);
    }

    public MirthDomReader(Element rootElement, NameCoder nameCoder) {
        super(rootElement, nameCoder);
    }

    public MirthDomReader(Document document, NameCoder nameCoder) {
        super(document, nameCoder);
    }

    public MirthDomReader(Element rootElement, XmlFriendlyReplacer replacer) {
        super(rootElement, replacer);
    }

    public MirthDomReader(Document document, XmlFriendlyReplacer replacer) {
        super(document, replacer);
    }

    protected void reloadCurrentElement() {
        reassignCurrentElement(getCurrent());
    }
}