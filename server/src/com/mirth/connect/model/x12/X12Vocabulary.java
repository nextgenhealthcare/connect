/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.x12;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.mirth.connect.model.converters.DataTypeFactory;
import com.mirth.connect.model.util.MessageVocabulary;

public class X12Vocabulary extends MessageVocabulary {
    private Logger logger = Logger.getLogger(X12Vocabulary.class);
    private Map<String, Object> vocab;
    private static final String JAXB_CONTEXT = "com.mirth.connect.model.x12";
    private static final String XML_PATH = "xml";
    private String version = "";
    private String type = "";

    public X12Vocabulary(String version, String type) {
        super(version, type);
        this.version = version;
        this.type = type;
    }

    public String getDescription(String elementId) {
        if (vocab == null) {
            try {
                loadData();
            } catch (Exception e) {
                logger.error("Error loading xml data: " + e.getMessage());
                vocab = new LinkedHashMap<String, Object>();
                return new String();
            }
        }

        // The map can contain a Queue or String
        Object element = vocab.get(elementId);
        if (element instanceof Queue<?>) {
            String description = ((Queue<String>) element).poll();
            if (description == null) {
                description = new String();
            } else {
                // Re-add to the queue to handle looping
                ((Queue<String>) element).add(description);
            }
            return description;
        } else {
            return (String) element;
        }
    }

    private void loadData() throws Exception {
        JAXBContext jc = JAXBContext.newInstance(JAXB_CONTEXT);
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        String fileName = XML_PATH + "/" + type + "." + version + ".xml"; // i.e.
                                                                          // 837.004010X096.xml
        TransactionType collection = ((JAXBElement<TransactionType>) unmarshaller.unmarshal(this.getClass().getResourceAsStream(fileName))).getValue();

        vocab = new LinkedHashMap<String, Object>();
        vocab.put(collection.getId(), collection.getName());
        LoopType loop = collection.getLoop();
        processLoop(loop, vocab);
    }

    private void addEntry(String name, String description, Map<String, Object> mappings) {
        // hack way to format for our ui
        name = name.replaceAll("_LOOP", "").replaceAll("-", "");
        if (name.length() > 5) {
            name = name.substring(0, name.length() - 4) + "." + name.substring(name.length() - 4, name.length() - 2) + "." + name.substring(name.length() - 2);
        } else if (name.length() > 3) {
            name = name.substring(0, name.length() - 2) + "." + name.substring(name.length() - 2);
        }
        name = name.replace('-', '.');

        // First check the map for an existing entry
        if (mappings.containsKey(name)) {

            // We have an existing mapping, if it's a string create a queue,
            // otherwise add to queue
            Object mappingElement = mappings.get(name);
            if (mappingElement instanceof Queue<?>) {
                // Add to the queue
                ((Queue<String>) mappingElement).add(description);

            } else {
                // It's a string so create queue
                Queue<String> queue = new LinkedBlockingQueue<String>();
                // Add the existing entry to the queue
                queue.add((String) mappingElement);
                // Add the new element to the queue
                queue.add(description);
                // Add the queue to the map
                mappings.put(name, queue);
            }
        } else {
            // First time we see element, add to mappings
            mappings.put(name, description);
        }
    }

    private void processLoop(LoopType loop, Map<String, Object> mappings) {
        addEntry(loop.getXid(), loop.getName(), mappings);
        for (Iterator<JAXBElement<?>> iter = loop.getSegmentOrLoopOrRepeat().iterator(); iter.hasNext();) {
            Object element = ((JAXBElement<?>) iter.next()).getValue();
            if (element instanceof SegmentType) {
                SegmentType segment = (SegmentType) element;
                processSegment(segment, mappings);
            } else if (element instanceof LoopType) {
                processLoop((LoopType) element, mappings);
            }
        }
    }

    private void processSegment(SegmentType segment, Map<String, Object> mappings) {
        addEntry(segment.getXid(), segment.getName(), mappings);
        for (Iterator<Object> iter = segment.getElementOrComposite().iterator(); iter.hasNext();) {
            Object component = iter.next();
            if (component instanceof CompositeType) {
                processComposite((CompositeType) component, mappings);
            } else if (component instanceof ElementType) {
                ElementType element = (ElementType) component;
                addEntry(element.getXid(), element.getName(), mappings);
            }
        }
    }

    private void processComposite(CompositeType composite, Map<String, Object> mappings) {
        addEntry(composite.getDataEle(), composite.getName(), mappings);
        for (Iterator<ElementType> iter = composite.getElement().iterator(); iter.hasNext();) {
            ElementType element = iter.next();
            addEntry(element.getXid(), element.getName(), mappings);

        }
    }

    @Override
    public String getDataType() {
        return DataTypeFactory.X12;
    }
}
