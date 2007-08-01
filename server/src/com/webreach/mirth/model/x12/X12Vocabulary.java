package com.webreach.mirth.model.x12;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.converters.ObjectCloner;
import com.webreach.mirth.model.util.MessageVocabulary;
import com.webreach.mirth.server.util.StackTracePrinter;

public class X12Vocabulary extends MessageVocabulary {
	Map<String, String> vocab;
	Logger logger = Logger.getLogger(ObjectCloner.class);
	private static final String JAXB_CONTEXT = "com.webreach.mirth.model.x12";
	private static final String XML_PATH = "xml";
	private String version = "";
	private String type = "";
	
	public X12Vocabulary(String version, String type){
		super(version, type);
		this.version = version;
		this.type = type;
	}
	public String getDescription(String elementId) {
		if (vocab == null){
			try{
				loadData();
			}catch (Exception e){
				logger.error("Error loading xml data: " + e.getMessage());
				vocab = new HashMap<String, String>();
				return new String();
			}
		}
		return vocab.get(elementId);
	}
	
	private void loadData() throws Exception{
			JAXBContext jc = JAXBContext.newInstance(JAXB_CONTEXT);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			
			String fileName = XML_PATH + "/" + type + "." + version + ".xml"; // i.e. 837.004010X096.xml
			TransactionType collection = (TransactionType) ((JAXBElement<TransactionType>) unmarshaller.unmarshal(this.getClass().getResourceAsStream(fileName))).getValue();

			vocab = new LinkedHashMap<String, String>();
			vocab.put(collection.getId(), collection.getName());
			LoopType loop = collection.getLoop();
			processLoop(loop, vocab);
	}
	
	private String trimLeadingZero(String name) {
		if (name.startsWith("0")) {
			return name.substring(1);
		} else {
			return name;
		}
	}
	
	private void addEntry(String name, String description, Map<String, String> mappings) {
		// hack way to format for our ui
		name = name.replaceAll("_LOOP", "").replaceAll("-", "");
		if (name.length() > 5) {
			name = name.substring(0, name.length() - 4) + "." + name.substring(name.length() - 4, name.length() - 2) + "." + name.substring(name.length() - 2);
		} else if (name.length() > 3) {
			name = name.substring(0, name.length() - 2) + "." + name.substring(name.length() - 2);
		}
		mappings.put(name.replace('-', '.'), description);
	}


	private void processLoop(LoopType loop, Map<String, String> mappings) {
		addEntry(loop.getXid(), loop.getName(), mappings);
		for (Iterator iter = loop.getSegmentOrLoopOrRepeat().iterator(); iter.hasNext();) {
			Object element = ((JAXBElement<?>) iter.next()).getValue();
			if (element instanceof SegmentType) {
				SegmentType segment = (SegmentType) element;
				processSegment(segment, mappings);
			} else if (element instanceof LoopType) {
				processLoop((LoopType) element, mappings);
			}
		}
	}

	private void processSegment(SegmentType segment, Map<String, String> mappings) {
		addEntry(segment.getXid(), segment.getName(), mappings);
		for (Iterator iter = segment.getElementOrComposite().iterator(); iter.hasNext();) {
			Object component = iter.next();
			if (component instanceof CompositeType) {
				processComposite((CompositeType) component, mappings);
			} else if (component instanceof ElementType) {
				ElementType element = (ElementType) component;
				addEntry(element.getXid(), element.getName(), mappings);
			}
		}
	}

	private void processComposite(CompositeType composite, Map<String, String> mappings) {
		addEntry(composite.getDataEle(), composite.getName(), mappings);
		for (Iterator<ElementType> iter = composite.getElement().iterator(); iter.hasNext();) {
			ElementType element = iter.next();
			addEntry(element.getXid(), element.getName(), mappings);

		}
	}
	@Override
	public Protocol getProtocol() {
		return Protocol.X12;
	}
}
