/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta;

import org.fife.ui.rsyntaxtextarea.EOLPreservingRSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;

import com.mirth.connect.client.ui.components.rsta.token.MirthTokenMakerFactory;

public class MirthRSyntaxDocument extends EOLPreservingRSyntaxDocument {

    static {
        System.setProperty(TokenMakerFactory.PROPERTY_DEFAULT_TOKEN_MAKER_FACTORY, MirthTokenMakerFactory.class.getName());
    }

    public MirthRSyntaxDocument(String syntaxStyle) {
        super(syntaxStyle, true);
    }
}