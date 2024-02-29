package de.odysseus.staxon.json.stream.util;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;

import de.odysseus.staxon.json.stream.JsonStreamTarget;
import de.odysseus.staxon.json.stream.JsonStreamToken;

public class MirthArrayTarget implements JsonStreamTarget {
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
    private final Stack< Map<String, List<Object>> > levelEventStack = new Stack<>(); 
    private final Stack<Integer> nameEventCounts = new Stack<>();

    /*
     * Field stack
     */
    protected final Stack<NameEvent> fields = new Stack<NameEvent>();
    
    private boolean alwaysArray = false;
    
    
    public MirthArrayTarget(JsonStreamTarget delegate) {
        this(delegate, false);
    }
    
    public MirthArrayTarget(JsonStreamTarget delegate, boolean alwaysArray) {
    	this.delegate = delegate;
    	this.alwaysArray = alwaysArray;
    }
    
    @Override
    public void name(String name) throws IOException {
    	Map<String, List<Object>> levelEventMap = levelEventStack.peek();
		if (levelEventMap != null) {
			List<Object> levelObjects = levelEventMap.get(name);
			if (levelObjects == null) {
				levelObjects = new LinkedList<>();
				levelEventMap.put(name, levelObjects);
			}
			fields.push(new NameEvent(name));
		}
		nameEventCounts.push(nameEventCounts.pop() + 1);
    }
    
    private boolean isSpecialField(String name) {
        return StringUtils.equals(name, "@xmlns") || StringUtils.equals(name, "@xmlnsprefix") || StringUtils.startsWith(name, "@xmlns:") || StringUtils.equals(name, "$");
    }

    @Override
    public void value(Object value) throws IOException {
    	NameEvent nameEvent = fields.peek();
    	Map<String, List<Object>> levelEventMap = levelEventStack.peek();
		if (levelEventMap != null) {
			List<Object> levelEvents = levelEventMap.get(nameEvent.name);
			if (levelEvents == null) {
				levelEvents = new LinkedList<>();
				levelEventMap.put(nameEvent.name, levelEvents);
			}
			levelEvents.add(new ValueEvent(value));
		}
    }

    @Override
    public void startObject() throws IOException {
    	levelEventStack.push(new LinkedHashMap<>());
    	nameEventCounts.push(0);
    }

    @Override
    public void endObject() throws IOException {
    	// Pop from the fields stack, so that we have the NameEvent corresponding to this object on top
    	Integer nameEvents = nameEventCounts.pop();
    	for (int i = 0; i < nameEvents; i++) {
    		fields.pop();
    	}
    	
    	// If we've reached the end of the XML events, write to the delegate
		if (fields.isEmpty()) {
			// create events list
			Deque<Event> events = generateEventList(levelEventStack.peek(), true);
			
			//write out events
			while (!events.isEmpty()) {
				events.pollFirst().write(delegate);
			}
		} else {
			NameEvent currentName = fields.peek();
	    	Map<String, List<Object>> levelEventMap = levelEventStack.pop();
	    	Map<String, List<Object>> currentLevelEventMap = levelEventStack.peek();
	    	currentLevelEventMap.get(currentName.name).add(levelEventMap);
		}
    }
    
    private Deque<Event> generateEventList(Map<String, List<Object>> levelEventMap, boolean root) {
    	Deque<Event> eventList = new LinkedList<>();
    	
    	eventList.add(START_OBJECT);
    	
    	for (Entry<String, List<Object>> element : levelEventMap.entrySet()) {
    		NameEvent event = new NameEvent(element.getKey());
    		if (alwaysArray) {
    			if (!root && !isSpecialField(event.name)) {
    				event.setArray(true);
    			}
    		} else if (element.getValue().size() > 1) {
    			event.setArray(true);
    		}
    		// add event to list
    		eventList.add(event);
    		
    		// loop through values
    		for (Object obj : element.getValue()) {
        		// if value is a map recursive call
    			if (obj instanceof Map) {
    				eventList.addAll(generateEventList((Map<String, List<Object>>)obj, false));
    			} else { // otherwise add event to list
    				eventList.add((Event)obj);
    			}
    		}
    		
    		if (event.isArray()) {
    			eventList.add(END_ARRAY);
    		}
    	}
    	
    	eventList.add(END_OBJECT);
    	return eventList;
    }
    
    @Override
    public void startArray() throws IOException {
    }

    @Override
    public void endArray() throws IOException {
    }

    @Override
    public void close() throws IOException {
    	Deque<Event> events = generateEventList(levelEventStack.peek(), true);
        while (!events.isEmpty()) {
			//write out events
            events.pollFirst().write(delegate);
        }
        delegate.close();
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

}
