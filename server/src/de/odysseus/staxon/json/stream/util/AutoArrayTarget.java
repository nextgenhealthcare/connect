/*
 * Copyright 2011, 2012 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.odysseus.staxon.json.stream.util;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Stack;

import de.odysseus.staxon.json.JsonXMLStreamWriter;
import de.odysseus.staxon.json.stream.JsonStreamTarget;
import de.odysseus.staxon.json.stream.JsonStreamToken;

/**
 * Target filter to auto-insert array boundaries.
 * 
 * Note: this class caches all events and flushes to the
 * underlying target after receiving the last close-object
 * event, which may cause memory issues for large documents.
 * Also, auto-recognition of array boundaries never creates
 * arrays with a single element.
 * 
 * It is recommended to handle array boundaries via the
 * {@link JsonXMLStreamWriter#writeStartArray(String)} and
 * {@link JsonXMLStreamWriter#writeEndArray()} methods
 * or by producing <code>&lt;?xml-muliple ...?&gt;</code>
 * processing instructions.
 */
public class AutoArrayTarget implements JsonStreamTarget {
    /**
     * Event type
     */
    protected static interface Event {
        JsonStreamToken token();
        void write(JsonStreamTarget target) throws IOException;
    }
    
    protected static final Event START_OBJECT = new Event() {
        @Override
        public void write(JsonStreamTarget target) throws IOException {
            target.startObject();
        }
        @Override
        public JsonStreamToken token() {
            return JsonStreamToken.START_OBJECT;
        }
        @Override
        public String toString() {
            return token().name();
        }
    };

    protected static final Event END_OBJECT = new Event() {
        @Override
        public void write(JsonStreamTarget target) throws IOException {
            target.endObject();
        }
        @Override
        public JsonStreamToken token() {
            return JsonStreamToken.END_OBJECT;
        }
        @Override
        public String toString() {
            return token().name();
        }
    };

    protected static final Event END_ARRAY = new Event() {
        @Override
        public void write(JsonStreamTarget target) throws IOException {
            target.endArray();
        }
        @Override
        public JsonStreamToken token() {
            return JsonStreamToken.END_ARRAY;
        }
        @Override
        public String toString() {
            return token().name();
        }
    };

    protected static final class NameEvent implements Event {
        final String name;
        boolean array;
        
        public NameEvent(String name) {
            this.name = name;
        }
        @Override
        public void write(JsonStreamTarget target) throws IOException {
            target.name(name);
            if (array) {
                target.startArray();
            }
        }
        @Override
        public JsonStreamToken token() {
            return JsonStreamToken.NAME;
        }
        public String name() {
            return name;
        }
        public boolean isArray() {
            return array;
        }
        public void setArray(boolean array) {
            this.array = array;
        }
        @Override
        public String toString() {
            if (array) {
                return token().name() + " = " + name + " " + JsonStreamToken.START_ARRAY;
            } else {
                return token().name() + " = " + name;
            }
        }
    }
    
    protected static final class ValueEvent implements Event {
        final Object value;
        
        ValueEvent(Object value) {
            this.value = value;
        }
        @Override
        public void write(JsonStreamTarget target) throws IOException {
            target.value(value);
        }
        @Override
        public JsonStreamToken token() {
            return JsonStreamToken.VALUE;
        }
        @Override
        public String toString() {
            return token().name() + " = " + value;
        }
    }

    /*
     * delegate target
     */
    protected final JsonStreamTarget delegate;
    
    /*
     * Event queue 
     */
    protected final Deque<Event> events = new LinkedList<Event>();

    /*
     * Field stack
     */
    protected final Stack<NameEvent> fields = new Stack<NameEvent>();
    
    public AutoArrayTarget(JsonStreamTarget delegate) {
        this.delegate = delegate;
    }

    protected void pushField(String name) {
        events.add(fields.push(new NameEvent(name)));
    }

    protected void popField() {
        if (fields.pop().isArray()) {
            events.add(END_ARRAY);
        }
    }
    
    @Override
    public void name(String name) throws IOException {
        if (events.peekLast().token() == JsonStreamToken.START_OBJECT) {
            pushField(name);
        } else {
            if (name.equals(fields.peek().name())) {
                fields.peek().setArray(true);
            } else {
                popField();
                pushField(name);
            }
        }
    }

    @Override
    public void value(Object value) throws IOException {
        events.add(new ValueEvent(value));
    }

    @Override
    public void startObject() throws IOException {
        events.add(START_OBJECT);
    }

    @Override
    public void endObject() throws IOException {
        if (events.peekLast().token() != JsonStreamToken.START_OBJECT) {
            popField();
        }
        events.add(END_OBJECT);
        if (fields.isEmpty()) {
            while (!events.isEmpty()) {
                events.pollFirst().write(delegate);
            }
        }
    }

    @Override
    public void startArray() throws IOException {
        if (fields.peek().isArray()) {
            throw new IllegalStateException();
        }
        fields.peek().setArray(true);
    }

    @Override
    public void endArray() throws IOException {
        if (!fields.peek().isArray()) {
            throw new IllegalStateException();
        }
        // array will be closed automatically
    }

    @Override
    public void close() throws IOException {
        while (!events.isEmpty()) {
            events.pollFirst().write(delegate);
        }
        delegate.close();
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }
}
