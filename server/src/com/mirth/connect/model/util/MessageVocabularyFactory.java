/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.util;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.client.core.Client;

public class MessageVocabularyFactory {
    private static MessageVocabularyFactory instance = null;
    private Map<String, Class<? extends MessageVocabulary>> vocabs = new HashMap<String, Class<? extends MessageVocabulary>>();

    public static MessageVocabularyFactory getInstance(Client mirthClient, Map<String, Class<? extends MessageVocabulary>> vocabs) {
        synchronized (MessageVocabularyFactory.class) {
            if (instance == null) {
                instance = new MessageVocabularyFactory(mirthClient, vocabs);
            }

            return instance;
        }
    }

    private MessageVocabularyFactory(Client mirthClient, Map<String, Class<? extends MessageVocabulary>> vocabs) {
        this.vocabs = vocabs;
    }

    public MessageVocabulary getVocabulary(String dataType, String version, String type) {
        Class<? extends MessageVocabulary> vocabulary = vocabs.get(dataType);
        MessageVocabulary vocab = null;
        
        if (vocabulary != null) {
            try {
                Constructor<?>[] constructors = vocabulary.getDeclaredConstructors();
                
                for (int i = 0; i < constructors.length; i++) {
                    Class<?> parameters[];
                    parameters = constructors[i].getParameterTypes();
                    // load plugin if the number of parameters is 2.
                    if (parameters.length == 2) {
                        vocab = (MessageVocabulary) constructors[i].newInstance(new Object[] { version, type });
                        i = constructors.length;
                    }
                }

                if (vocab != null) {
                    return vocab;
                } else {
                    // it should never come here.
                    return new DefaultVocabulary(version, type);
                }

            } catch (Exception e) {
                e.printStackTrace();
                return new DefaultVocabulary(version, type);
            }
        } else {
            return new DefaultVocabulary(version, type);
        }
    }
}
