/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.webreach.mirth.managers.types.MirthProperty;
import com.webreach.mirth.managers.types.MirthPropertyType;
import com.webreach.mirth.managers.types.properties.Component;
import com.webreach.mirth.managers.types.properties.MirthProperties;
import com.webreach.mirth.managers.types.properties.Property;
import com.webreach.mirth.managers.types.properties.Type;

/**
 * PropertyManager maintains a list of Mirth component properties.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 * 
 */
public class PropertyManager {
	protected transient Log logger = LogFactory.getLog(PropertyManager.class);

	public static final String MIRTH_PROP_FILE = ConfigurationManager.CONFIG_FOLDER + "mirth-properties.xml";
	public static final String OPTIONS_DELIMITER = ",";
	public static final String OPTIONS_SUB_DELIMITER = ";";

	private boolean initialized = false;
	private ArrayList<MirthProperty> mirthPropertyList;
	private MirthProperties mirthProperties = null;

	// TODO: get rid of these
	private HashMap<String, String> classNames;
	private HashMap<String, String> transformers;
	private HashMap<String, String> direction;
	private HashMap<String, String> protocol;
	
	// singleton pattern
	private static PropertyManager instance = null;

	private PropertyManager() {}

	public static PropertyManager getInstance() {
		synchronized (PropertyManager.class) {
			if (instance == null)
				instance = new PropertyManager();

			return instance;
		}
	}

	/**
	 * Loads the contents of the properties XML file into the properties list.
	 * 
	 */
	public void initialize() throws ManagerException {
		if (initialized)
			return;

		mirthPropertyList = new ArrayList<MirthProperty>();
		
		// TODO: get rid of these
		classNames = new HashMap<String, String>();
		transformers = new HashMap<String, String>();
		direction = new HashMap<String, String>();
		protocol = new HashMap<String, String>();

		try {
			logger.debug("initializing property manager");

			File mirthPropertiesFile = new File(MIRTH_PROP_FILE);
			JAXBContext mirthPropertiesContext = JAXBContext.newInstance("com.webreach.mirth.managers.types.properties");

			if (mirthPropertiesFile.exists()) {
				logger.debug("loading mirth properties file: " + mirthPropertiesFile.getAbsolutePath());

				Unmarshaller unmarsh = mirthPropertiesContext.createUnmarshaller();
				unmarsh.setValidating(false);
				mirthProperties = (MirthProperties) unmarsh.unmarshal(mirthPropertiesFile);

				if ((mirthProperties != null) && (mirthProperties.getComponent() != null)) {

					for (Iterator componentIterator = mirthProperties.getComponent().iterator(); componentIterator.hasNext();) {
						Component component = (Component) componentIterator.next();

						for (Iterator typeIterator = component.getType().iterator(); typeIterator.hasNext();) {
							Type type = (Type) typeIterator.next();
							
							// TODO: get rid of these
							classNames.put(type.getName(), type.getClassName());
							transformers.put(type.getName(), type.getTransformers());
							direction.put(type.getName(), type.getDirection());
							protocol.put(type.getName(), type.getProtocol());

							for (Iterator propertyIterator = type.getProperty().iterator(); propertyIterator.hasNext();) {
								Property property = (Property) propertyIterator.next();

								MirthProperty mirthProperty = new MirthProperty();
								mirthProperty.setComponent(component.getName());
								mirthProperty.setType(new MirthPropertyType(type.getName(), type.getDisplayName()));
								mirthProperty.setName(property.getName());
								mirthProperty.setDescription(property.getDescription());
								mirthProperty.setDefaultValue(property.getDefaultValue());
								mirthProperty.setDisplayName(property.getDisplayName());
								mirthProperty.setFormType(property.getFormType());
								mirthProperty.setFormOptions(parseOptions(property.getFormOptions()));
								mirthProperty.setFormValidator(property.getFormValidator());
								mirthProperty.setFormValidatorOptions(parseOptions(property.getFormValidatorOptions()));
								mirthProperty.setMuleProperty(property.isMuleProperty());
								mirthProperty.setRequired(property.isRequired());

								addProperty(mirthProperty);
							}
						}
					}

					initialized = true;
				}
			} else {
				logger.error("Could not load Mirth properties file.");
				throw new ManagerException("Could not load Mirth properties file.");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new ManagerException("Could not initialize PropertyManager.", e);
		}
	}

	/**
	 * Parses a comma seperated options string into an array of option values.
	 * 
	 * @param options
	 * @return an array of option values
	 */
	private String[] parseOptions(String options) {
		String[] optionsArray = options.split(OPTIONS_DELIMITER);

		// remove any spaces around each value
		for (int i = 0; i < optionsArray.length; i++) {
			optionsArray[i].trim();
		}

		return optionsArray;
	}

	/**
	 * Adds a new property to the properties list.
	 * 
	 * @param mirthProperty
	 */
	private void addProperty(MirthProperty mirthProperty) {
//		logger.debug("adding property to list: " + mirthProperty.getName());
		mirthPropertyList.add(mirthProperty);
	}

	/**
	 * Returns the value of a specific property.
	 * 
	 * @param component
	 * @param type
	 * @param name
	 * @return
	 */
	public MirthProperty getProperty(String component, String type, String name) {
		for (Iterator iter = mirthPropertyList.iterator(); iter.hasNext();) {
			MirthProperty mirthProperty = (MirthProperty) iter.next();

			if (mirthProperty.getComponent().equals(component) && mirthProperty.getType().getName().equals(type) && mirthProperty.getName().equals(name)) {
				return mirthProperty;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns a list of <code>MirthProperty</code> elements for the given
	 * component and type display name.
	 * 
	 * @param component
	 * @param type
	 * @return a list of <code>MirthProperty</code> elements for the given
	 *         component and type.
	 */
	public ArrayList<MirthProperty> getProperties(String component, String type) {
		ArrayList<MirthProperty> propertyList = new ArrayList<MirthProperty>();

		for (Iterator iter = mirthPropertyList.iterator(); iter.hasNext();) {
			MirthProperty mirthProperty = (MirthProperty) iter.next();

			if (mirthProperty.getComponent().equals(component) && mirthProperty.getType().getName().equals(type)) {
				propertyList.add(mirthProperty);
			}
		}

		return propertyList;
	}

	/**
	 * Returns <code>true</code> if PropertyManager has been initialized.
	 * 
	 * @return <code>true</code> if PropertyManager has been initialized.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Returns an ArrayList of all types for a specified component.
	 * 
	 * @param component
	 * @return an ArrayList of all types for a specified component.
	 */
	public ArrayList<MirthPropertyType> getTypes(String component) {
		ArrayList<MirthPropertyType> typeList = new ArrayList<MirthPropertyType>();

		for (Iterator iter = mirthPropertyList.iterator(); iter.hasNext();) {
			MirthProperty mirthProperty = (MirthProperty) iter.next();

			if (!typeList.contains(mirthProperty.getType())) {
				typeList.add(mirthProperty.getType());
			}
		}

		return typeList;
	}

	/**
	 * Prints a the entire list of properties for debugging purposes.
	 * 
	 */
	public void printProperties() {
		for (Iterator iter = mirthPropertyList.iterator(); iter.hasNext();) {
			MirthProperty property = (MirthProperty) iter.next();
			System.out.println(property.toString());
		}
	}

	/**
	 * Returns the display name for a specific type name, or <code>null</code>
	 * if no property is of the type.
	 * 
	 * @param name
	 * @return the display name for a specific type name, or <code>null</code>
	 *         if no property is of the type.
	 */
	public String getTypeDisplayName(String name) {
		for (Iterator iter = mirthPropertyList.iterator(); iter.hasNext();) {
			MirthProperty property = (MirthProperty) iter.next();

			if (property.getType().getName().equals(name)) {
				return property.getType().getDisplayName();
			}
		}

		return null;
	}

	// TODO: get rid of these
	public String getClassName(String typeName) {
		return classNames.get(typeName);
	}
	
	public String getTransformers(String typeName) {
		return transformers.get(typeName);
	}

	public String getDirection(String typeName) {
		return direction.get(typeName);
	}
	
	public String getProtocol(String typeName) {
		return protocol.get(typeName);
	}
}
