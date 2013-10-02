package com.mirth.connect.model.hl7v2;

import java.util.HashMap;
import java.util.Map;

public class Component {
	private static final String DATATYPE_SUFFIX = "]";
	private static final String DATATYPE_PREFIX = " [";
	private static final String COMPOSITE_PREFIX = ".composite._";
	private static final String MESSAGE_PREFIX = ".message._";
	private static final String SEGMENT_PREFIX = ".segment._";
	private static final String MODEL_CLASSPATH = "com.mirth.connect.model.hl7v2.v";
	protected String description = "";
	protected String name = "";
	protected static Map<Class, Component> cacheMap = new HashMap<Class, Component>();
	protected static String[] versions = new String[] { "21", "22", "23", "231", "24", "25" };

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public Component getComponent(int index) {
		// overridden
		return null;
	}

	public String getComponentDescription(int index) {
		// overridden
		return null;
	}

	public static Component getCachedComponent(Class componentClass) throws InstantiationException, IllegalAccessException {
		Component component = cacheMap.get(componentClass);
		if (component == null) {
			component = (Component) componentClass.newInstance();
			cacheMap.put(componentClass, component);
		}
		return component;
	}

	public static Component getCachedComponent(String componentName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class clazz = Class.forName(componentName);
		return getCachedComponent(clazz);
	}

	// Returns the description for a message type (ex: ADTA01 -> Admit Patient)
	public static String getMessageDescription(String version, String messageName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		messageName = messageName.replaceAll("\\^", "").replaceAll("-", "");
		Component message = getCachedComponent(MODEL_CLASSPATH + version + MESSAGE_PREFIX + messageName);
		return message.getDescription();
	}

	// Returns the description for a message type (ex: ADTA01 -> Admit Patient)
	public static String getMessageDescription(String messageName) {
		String description = "";
		for (int x = versions.length - 1; x >= 0; x--) {
			try {
				description = getMessageDescription(versions[x], messageName);
			} catch (Exception e) {

			}
			if (description != null && description.length() > 0) {
				break;
			}
		}
		return description;
	}

	// Returns the description for a segment (ex: PID -> Patient Identification)
	public static String getSegmentDescription(String version, String segmentName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Component segment = getCachedComponent(MODEL_CLASSPATH + version + SEGMENT_PREFIX + segmentName);
		return segment.getDescription();
	}

	// Returns the description for a segment (ex: PID -> Patient Identification)
	public static String getSegmentDescription(String segmentName) {
		String description = "";
		for (int x = versions.length - 1; x >= 0; x--) {
			try {
				description = getSegmentDescription(versions[x], segmentName);
			} catch (Exception e) {

			}
			if (description != null && description.length() > 0) {
				break;
			}
		}
		return description;
	}

	// Returns the description of a field in a segment (ex: PID.5 -> Patient
	// Name)
	// if includeDataType is true, then (ex: PID.5 -> Patient Name (XPN))
	public static String getSegmentFieldDescription(String version, String segmentName, int index, boolean includeDataType) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return getComponentFieldDescription(version, segmentName, SEGMENT_PREFIX, index, includeDataType);
	}

	// Returns the description of a field in a segment (ex: PID.5 -> Patient
	// Name)
	// if includeDataType is true, then (ex: PID.5 -> Patient Name (XPN))
	public static String getSegmentFieldDescription(String version, String segmentField, boolean includeDataType) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return getComponentFieldDescription(version, segmentField, SEGMENT_PREFIX, includeDataType);
	}

	public static String getSegmentorCompositeFieldDescription(String segmentOrCompositeField, boolean includeDataType) {
		String description = "";
		for (int x = versions.length - 1; x >= 0; x--) {
			try {
				description = getSegmentFieldDescription(versions[x], segmentOrCompositeField, includeDataType);
			} catch (Exception e) {

			}
			if (description != null && description.length() > 0) {
				return description;
			}
		}
		for (int x = versions.length - 1; x >= 0; x--) {
			try {
				description = getCompositeFieldDescription(versions[x], segmentOrCompositeField, includeDataType);
			} catch (Exception e) {

			}
			if (description != null && description.length() > 0) {
				return description;
			}
		}
		return description;
	}

	// Returns the description of a composite (ex: XPN -> Extended Person Name);
	public static String getCompositeDescription(String version, String compositeName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Component composite = getCachedComponent(MODEL_CLASSPATH + version + COMPOSITE_PREFIX + compositeName);
		return composite.getDescription();
	}

	// Returns the description of a composite (ex: XPN.1 -> Family Last Name);
	// if includeDataType is true, then (ex: XPN.1 -> Family Last Name (FN))
	public static String getCompositeFieldDescription(String version, String compositeName, int index, boolean includeDataType) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return getComponentFieldDescription(version, compositeName, COMPOSITE_PREFIX, index, includeDataType);
	}

	// Returns the description of a composite (ex: XPN.1 -> Family Last Name);
	// if includeDataType is true, then (ex: XPN.1 -> Family Last Name (FN))
	public static String getCompositeFieldDescription(String version, String compositeName, boolean includeDataType) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return getComponentFieldDescription(version, compositeName, COMPOSITE_PREFIX, includeDataType);
	}

	// Returns the description of a composite based on segment path (ex: PID.5.1
	// -> Family Last Name)
	// if includeDataType is true, then (ex: PID.5.1 -> Family Last Name (FN))
	public static String getCompositeFieldDescriptionWithSegment(String version, String segmentAndComposite, boolean includeDataType) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String[] parts = segmentAndComposite.split("\\.");
		String segmentName = parts[0];
		int fieldIndex = Integer.parseInt(parts[1]) - 1; // HL7 is 1-index
		// based
		int subFieldIndex = Integer.parseInt(parts[2]) - 1;
		Component segment = getCachedComponent(MODEL_CLASSPATH + version + SEGMENT_PREFIX + segmentName);
		Component field = segment.getComponent(fieldIndex);
		if (includeDataType) {
			Component subfield = field.getComponent(subFieldIndex);
			return field.getComponentDescription(subFieldIndex) + DATATYPE_PREFIX + subfield.getName() + DATATYPE_SUFFIX;
		} else {
			return field.getComponentDescription(subFieldIndex);
		}
	}

	// Returns the description of a composite based on segment path (ex: PID.5.1
	// -> Family Last Name)
	// if includeDataType is true, then (ex: PID.5.1 -> Family Last Name (FN))
	public static String getCompositeFieldDescriptionWithSegment(String segmentAndComposite, boolean includeDataType)  {
		String description = "";
		for (int x = versions.length - 1; x >= 0; x--) {
			try {
				description = getCompositeFieldDescriptionWithSegment(versions[x],segmentAndComposite, includeDataType);
			} catch (Exception e) {

			}
			if (description != null && description.length() > 0) {
				break;
			}
		}
		return description;
	}

	// General purpose helpers
	private static String getComponentFieldDescription(String version, String componentField, String prefix, boolean includeDataType) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String[] parts = componentField.split("\\.");
		String componentName = parts[0];
		int index = Integer.parseInt(parts[1]);
		return getComponentFieldDescription(version, componentName, prefix, index, includeDataType);
	}

	private static String getComponentFieldDescription(String version, String name, String prefix, int index, boolean includeDataType) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Component component = getCachedComponent(MODEL_CLASSPATH + version + prefix + name);
		// HL7 is 1-index based
		index = index - 1;
		if (includeDataType) {
			Component field = component.getComponent(index);
			return component.getComponentDescription(index) + DATATYPE_PREFIX + field.getName() + DATATYPE_SUFFIX;
		} else {
			return component.getComponentDescription(index);
		}
	}

}
