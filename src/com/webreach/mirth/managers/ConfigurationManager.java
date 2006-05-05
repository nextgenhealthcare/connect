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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import com.webreach.mirth.MirthUtil;
import com.webreach.mirth.managers.types.mirth.Channel;
import com.webreach.mirth.managers.types.mirth.Endpoint;
import com.webreach.mirth.managers.types.mirth.Filter;
import com.webreach.mirth.managers.types.mirth.Mirth;
import com.webreach.mirth.managers.types.mirth.Property;
import com.webreach.mirth.managers.types.mirth.Script;
import com.webreach.mirth.managers.types.mirth.Transformer;
import com.webreach.mirth.managers.types.mirth.User;
import com.webreach.mirth.managers.types.mule.ConnectorType;
import com.webreach.mirth.managers.types.mule.EndpointType;
import com.webreach.mirth.managers.types.mule.FilterType;
import com.webreach.mirth.managers.types.mule.InboundRouterType;
import com.webreach.mirth.managers.types.mule.MuleConfiguration;
import com.webreach.mirth.managers.types.mule.MuleDescriptorType;
import com.webreach.mirth.managers.types.mule.OutboundRouterType;
import com.webreach.mirth.managers.types.mule.PropertiesType;
import com.webreach.mirth.managers.types.mule.RouterType;

/**
 * The <code>ConfigurationManager</code> class controls the serialization of
 * Mirth and Mule configuration files.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 */
@SuppressWarnings("unchecked")
public class ConfigurationManager {
	protected transient Log logger = LogFactory.getLog(ConfigurationManager.class);

	// configuration file names
	public static final String CONFIGURATION = "configuration";
	public static final String CONFIG_FOLDER = CONFIGURATION + File.separator;
	public static final String MIRTH_CONFIG_FILE = CONFIG_FOLDER + "mirth-config.xml";
	public static final String MULE_CONFIG_FILE = CONFIG_FOLDER + "mule-config.xml";
	public static final String MULE_BOOT_FILE = CONFIG_FOLDER + "mule-boot.xml";
	public static final String ACTIVEMQ_CONFIG_FILE = CONFIG_FOLDER + "activemq-config.xml";
	public static final String JETTY_CONFIG_FILE = CONFIG_FOLDER + "jetty-config.xml";
	public static final String KEY_FILE = CONFIG_FOLDER + "key.dat";
	public static final String BUILD_NUMBER = "build.number";
	public static final String BUILD_PROPERTIES = "build.properties";

	// endpoint types
	public static final String ENDPOINT_TCP = "tcp";
	public static final String ENDPOINT_HTTP = "http";
	public static final String ENDPOINT_JMS = "jms";
	public static final String ENDPOINT_JDBC = "jdbc";
	public static final String ENDPOINT_AXIS = "axis";
	public static final String ENDPOINT_MLLP = "mllp";
	public static final String ENDPOINT_POP3 = "pop3";
	public static final String ENDPOINT_SMTP = "smtp";
	public static final String ENDPOINT_FILE = "file";

	// script types
	public static final String SCRIPT_ECMA = "JavaScript";
	public static final String SCRIPT_PYTHON = "Python";
	public static final String SCRIPT_JAVA = "Java";

	// delimeter used for configuration ID sequence (filters, tranformers, etc.)
	private static final String ID_SEQ_DELIMETER = " ";
	// delimeter used to seperate ID from channel name in Mule configuration
	public static final String ID_NAME_DELIMETER = "_";

	// configuration manager status
	private boolean initialized = false;
	private ChangeManager changeManager = ChangeManager.getInstance();
	private PropertyManager propertyManager = PropertyManager.getInstance();

	// JAXB objects
	private Mirth mirth = null;
	private MuleConfiguration mule = null;
	private com.webreach.mirth.managers.types.mirth.ObjectFactory mirthFactory = null;
	private com.webreach.mirth.managers.types.mule.ObjectFactory muleFactory = null;
	private JAXBContext mirthContext = null;
	private JAXBContext muleContext = null;

	// Mirth lists
	private List<Transformer> transformerList = null;
	private List<Channel> channelList = null;
	private List<Endpoint> endpointList = null;
	private List<Filter> filterList = null;
	private List<User> userList = null;

	// singleton pattern
	private static ConfigurationManager instance = null;

	private ConfigurationManager() {}

	public static ConfigurationManager getInstance() {
		synchronized (ConfigurationManager.class) {
			if (instance == null)
				instance = new ConfigurationManager();

			return instance;
		}
	}

	/**
	 * Initializes the ConfigurationManager. If the XML configuration file
	 * exists, the contents are read in. Otherwise, a new XML file is created.
	 * 
	 * @throws ManagerException
	 */
	public void initialize() throws ManagerException {
		if (initialized)
			return;

		try {
			// create the configuration directory if it doesnt exist
			File configFolder = new File(CONFIGURATION);

			if (!configFolder.exists()) {
				logger.debug("creating configuration folder: " + CONFIG_FOLDER);
				configFolder.mkdir();
			}

			logger.debug("initializing configuration manager");
			unmarshallMirth();
			unmarshallMule();

			initialized = true;
		} catch (Exception e) {
			throw new ManagerException("Could not initialize ConfigurationManager.", e);
		}
	}
	
	private void unmarshallMirth() {
		try {
			File mirthConfigFile = new File(MIRTH_CONFIG_FILE);
			mirthContext = JAXBContext.newInstance("com.webreach.mirth.managers.types.mirth");
			mirthFactory = new com.webreach.mirth.managers.types.mirth.ObjectFactory();

			if (mirthConfigFile.exists()) {
				logger.debug("unmarshalling existing mirth configuration file: " + mirthConfigFile.getAbsolutePath());
				Unmarshaller unmarsh = mirthContext.createUnmarshaller();
				unmarsh.setValidating(false);
				
				// disable schema validation completely
				unmarsh.setEventHandler(new ValidationEventHandler() {
					public boolean handleEvent(ValidationEvent validationEvent) {
						return false;
					}
				});
				
				mirth = (Mirth) unmarsh.unmarshal(mirthConfigFile);

				if (mirth.getComponents() != null) {
					if (mirth.getComponents().getChannels() != null) {
						channelList = mirth.getComponents().getChannels().getChannel();
					}

					if (mirth.getComponents().getTransformers() != null) {
						transformerList = mirth.getComponents().getTransformers().getTransformer();
					}

					if (mirth.getComponents().getFilters() != null) {
						filterList = mirth.getComponents().getFilters().getFilter();
					}

					if (mirth.getComponents().getEndpoints() != null) {
						endpointList = mirth.getComponents().getEndpoints().getEndpoint();
					}
				}

				// load the users list
				if (mirth.getConfiguration() != null) {
					if (mirth.getConfiguration().getUsers().getUser() != null) {
						userList = mirth.getConfiguration().getUsers().getUser();
					}
				}
			} else {
				logger.debug("creating new mirth configuration file: " + mirthConfigFile.getAbsolutePath());
				mirth = mirthFactory.createMirth();
				mirth.setLastId(0);

				mirth.setComponents(mirthFactory.createComponents());
				mirth.setConfiguration(mirthFactory.createConfiguration());

				mirth.getComponents().setChannels(mirthFactory.createChannels());
				mirth.getComponents().setTransformers(mirthFactory.createTransformers());
				mirth.getComponents().setFilters(mirthFactory.createFilters());
				mirth.getComponents().setEndpoints(mirthFactory.createEndpoints());
				mirth.getConfiguration().setUsers(mirthFactory.createUsers());

				channelList = mirth.getComponents().getChannels().getChannel();
				transformerList = mirth.getComponents().getTransformers().getTransformer();
				filterList = mirth.getComponents().getFilters().getFilter();
				endpointList = mirth.getComponents().getEndpoints().getEndpoint();
				userList = mirth.getConfiguration().getUsers().getUser();

				// add the default admin account
				User admin = mirthFactory.createUser();
				admin.setLogin("admin");
				admin.setPassword(ConfigUtil.encryptPassword("password"));
				addUser(admin);

				marshallMirth();
			}
		} catch (Exception e) {
			throw new ManagerException("Could not load Mirth configuration file.", e);
		}
	}
	
	private void unmarshallMule() {
		try {
			File muleBootFile = new File(MULE_BOOT_FILE);
			muleContext = JAXBContext.newInstance("com.webreach.mirth.managers.types.mule");
			muleFactory = new com.webreach.mirth.managers.types.mule.ObjectFactory();

			if (muleBootFile.exists()) {
				logger.debug("unmarshalling mule boot-strap configuration file: " + muleBootFile.getAbsolutePath());
				Unmarshaller unmarsh = muleContext.createUnmarshaller();
				unmarsh.setValidating(false);
				
				// disable schema validation completely
				unmarsh.setEventHandler(new ValidationEventHandler() {
					public boolean handleEvent(ValidationEvent validationEvent) {
						return false;
					}
				});

				mule = (MuleConfiguration) unmarsh.unmarshal(muleBootFile);
			} else {
				logger.debug("could not load mule boot-strap configuration file: " + muleBootFile.getAbsolutePath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void marshallMirth() throws ManagerException {
		try {
			logger.debug("writing to mirth configuration file");
			Marshaller marsh = mirthContext.createMarshaller();
			marsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			// format for CDATA elements is "namespace^element"
			XMLSerializer serializer = MirthUtil.getXMLSerializer(new FileOutputStream(MIRTH_CONFIG_FILE), new String[] { "^script" });
			marsh.marshal(mirth, serializer.asContentHandler());

			changeManager.setConfigurationChanged(true);
		} catch (Exception e) {
			throw new ManagerException("Could not update Mirth configuration file.", e);
		}
	}

	/*
	 * ------------------------------ Channel ------------------------------
	 */

	/**
	 * Constructs a <code>Channel</code> object.
	 * 
	 * @throws ManagerException
	 *             if the <code>Channel</code> could not be constructed.
	 */
	public Channel createChannel() throws ManagerException {
		try {
			return mirthFactory.createChannel();
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Returns a channel with the specified <code>channelId</code> in the
	 * channel list.
	 * 
	 * @param channelId
	 *            the id of the channel
	 * @return the channel with the specified channel id in the channel list.
	 * @throws ManagerException
	 *             if the channel list is <code>null</code>.
	 */
	public Channel getChannel(int channelId) throws ManagerException {
		try {
			for (int i = 0; i < channelList.size(); i++) {
				Channel currentChannel = channelList.get(i);

				if (currentChannel.getId() == channelId) {
					return currentChannel;
				}
			}

			return null;
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Returns an unmodifiable view of the channel list sorted by name.
	 * 
	 * @return the channel list
	 * @throws ManagerException
	 *             if the channel list is <code>null</code>.
	 */
	public List getChannelList() throws ManagerException {
		if (channelList == null) {
			throw new ManagerException("Channel list is null.");
		} else {
			Collections.sort(channelList, new Comparator() {
				public int compare(Object o1, Object o2) {
					Channel c1 = (Channel) o1;
					Channel c2 = (Channel) o2;

					return c1.getName().toLowerCase().compareTo(c2.getName().toLowerCase());
				}
			});

			return Collections.unmodifiableList(channelList);
		}
	}

	/**
	 * Inserts a channel into the channel list and returns the assigned
	 * <code>channelId</code>.
	 * 
	 * @param channel
	 * @return the assgined channel id
	 * @throws ManagerException
	 *             if the channel list is <code>null</code>.
	 */
	public int addChannel(Channel channel) throws ManagerException {
		try {
			channel.setId(getNewId());
			channelList.add(channel);
		} catch (Exception e) {
			throw new ManagerException(e);
		}

		marshallMirth();
		changeManager.changeChannel(channel.getId());
		return channel.getId();
	}

	/**
	 * Updates a channel in the channel list. The outdated channel is removed
	 * based on <code>channelId</code> and the new channel is inserted into
	 * the list.
	 * 
	 * @param channel
	 *            the updated channel.
	 * @throws ManagerException
	 *             if the channel list is <code>null</code>.
	 */
	public void updateChannel(Channel channel) throws ManagerException {
		try {
			for (int i = 0; i < channelList.size(); i++) {
				Channel currentChannel = channelList.get(i);

				if (currentChannel.getId() == channel.getId()) {
					channelList.remove(i);
					channelList.add(channel);
				}
			}

			marshallMirth();
			changeManager.changeChannel(channel.getId());
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Removes a channel from the channel list.
	 * 
	 * @param channelId
	 *            the id of the channel
	 * @throws ManagerException
	 *             if the channel list is <code>null</code>.
	 */
	public void removeChannel(int channelId) throws ManagerException {
		try {
			for (int i = 0; i < channelList.size(); i++) {
				Channel currentChannel = channelList.get(i);

				if (currentChannel.getId() == channelId) {
					channelList.remove(i);
				}
			}

			marshallMirth();
			changeManager.changeChannel(channelId);
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/*
	 * ------------------------------ Endpoint ------------------------------
	 */

	/**
	 * Constructs a <code>Endpoint</code> object.
	 * 
	 * @throws ManagerException
	 *             if the <code>Endpoint</code> could not be constructed.
	 */
	public Endpoint createEndpoint() throws ManagerException {
		try {
			Endpoint endpoint = mirthFactory.createEndpoint();
			endpoint.setProperties(mirthFactory.createProperties());

			return endpoint;
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Returns a endpoint with the specified <code>endpointId</code> in the
	 * endpoint list.
	 * 
	 * @param endpointId
	 *            the id of the endpoint
	 * @return the endpoint with the specified endpoint id in the endpoint list.
	 * @throws ManagerException
	 *             if the endpoint list is <code>null</code>.
	 */
	public Endpoint getEndpoint(int endpointId) throws ManagerException {
		try {
			for (int i = 0; i < endpointList.size(); i++) {
				Endpoint currentEndpoint = endpointList.get(i);

				if (currentEndpoint.getId() == endpointId) {
					return currentEndpoint;
				}
			}

			return null;
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Returns an unmodifiable view of the endpoint list sorted by name.
	 * 
	 * @return the endpoint list
	 * @throws ManagerException
	 *             if the endpoint list is <code>null</code>.
	 */
	public List getEndpointList() throws ManagerException {
		if (endpointList == null) {
			throw new ManagerException("Endpoint list is null.");
		} else {
			Collections.sort(endpointList, new Comparator() {
				public int compare(Object o1, Object o2) {
					Endpoint e1 = (Endpoint) o1;
					Endpoint e2 = (Endpoint) o2;

					return e1.getName().toLowerCase().compareTo(e2.getName().toLowerCase());
				}
			});

			return Collections.unmodifiableList(endpointList);
		}
	}

	/**
	 * Inserts a endpoint into the endpoint list and returns the assigned
	 * <code>endpointId</code>.
	 * 
	 * @param endpoint
	 * @return the assgined endpoint id
	 * @throws ManagerException
	 *             if the endpoint list is <code>null</code>.
	 */
	public int addEndpoint(Endpoint endpoint) throws ManagerException {
		try {
			endpoint.setId(getNewId());
			endpointList.add(endpoint);
		} catch (Exception e) {
			throw new ManagerException(e);
		}

		marshallMirth();
		return endpoint.getId();
	}

	/**
	 * Updates a endpoint in the endpoint list. The outdated endpoint is removed
	 * based on <code>endpointId</code> and the new endpoint is inserted into
	 * the list.
	 * 
	 * @param endpoint
	 *            the updated endpoint.
	 * @throws ManagerException
	 *             if the endpoint list is <code>null</code>.
	 */
	public void updateEndpoint(Endpoint endpoint) throws ManagerException {
		try {
			for (int i = 0; i < endpointList.size(); i++) {
				Endpoint currentEndpoint = endpointList.get(i);

				if (currentEndpoint.getId() == endpoint.getId()) {
					endpointList.remove(i);
					endpointList.add(endpoint);
				}
			}

			marshallMirth();
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Removes a endpoint from the endpoint list.
	 * 
	 * @param endpointId
	 *            the id of the endpoint
	 * @throws ManagerException
	 *             if the endpoint list is <code>null</code>.
	 */
	public void removeEndpoint(int endpointId) throws ManagerException {
		try {
			for (int i = 0; i < endpointList.size(); i++) {
				Endpoint currentEndpoint = endpointList.get(i);

				if (currentEndpoint.getId() == endpointId) {
					endpointList.remove(i);
				}
			}

			marshallMirth();
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/*
	 * ------------------------------ Transformer ------------------------------
	 */

	/**
	 * Constructs a <code>Transformer</code> object.
	 * 
	 * @throws ManagerException
	 *             if the <code>Transformer</code> could not be constructed.
	 */
	public Transformer createTransformer() throws ManagerException {
		try {
			return mirthFactory.createTransformer();
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Returns a transformer with the specified <code>transformerId</code> in
	 * the transformer list.
	 * 
	 * @param transformerId
	 *            the id of the transformer
	 * @return the transformer with the specified transformer id in the
	 *         transformer list.
	 * @throws ManagerException
	 *             if the transformer list is <code>null</code>.
	 */
	public Transformer getTransformer(int transformerId) throws ManagerException {
		try {
			for (int i = 0; i < transformerList.size(); i++) {
				Transformer currentTransformer = transformerList.get(i);

				if (currentTransformer.getId() == transformerId) {
					return currentTransformer;
				}
			}

			return null;
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Returns an unmodifiable view of the transformer list sorted by name.
	 * 
	 * @return the transformer list
	 * @throws ManagerException
	 *             if the transformer list is <code>null</code>.
	 */
	public List getTransformerList() throws ManagerException {
		if (transformerList == null) {
			throw new ManagerException("Transformer list is null.");
		} else {
			Collections.sort(transformerList, new Comparator() {
				public int compare(Object o1, Object o2) {
					Transformer t1 = (Transformer) o1;
					Transformer t2 = (Transformer) o2;

					return t1.getName().toLowerCase().compareTo(t2.getName().toLowerCase());
				}
			});

			return Collections.unmodifiableList(transformerList);
		}
	}

	/**
	 * Inserts a transformer into the transformer list and returns the assigned
	 * <code>transformerId</code>.
	 * 
	 * @param transformer
	 * @return the assgined transformer id
	 * @throws ManagerException
	 *             if the transformer list is <code>null</code>.
	 */
	public int addTransformer(Transformer transformer) throws ManagerException {
		try {
			transformer.setId(getNewId());
			transformerList.add(transformer);
		} catch (Exception e) {
			throw new ManagerException(e);
		}

		marshallMirth();
		return transformer.getId();
	}

	/**
	 * Updates a transformer in the transformer list. The outdated transformer
	 * is removed based on <code>transformerId</code> and the new transformer
	 * is inserted into the list.
	 * 
	 * @param transformer
	 *            the updated transformer.
	 * @throws ManagerException
	 *             if the transformer list is <code>null</code>.
	 */
	public void updateTransformer(Transformer transformer) throws ManagerException {
		try {
			for (int i = 0; i < transformerList.size(); i++) {
				Transformer currentTransformer = transformerList.get(i);

				if (currentTransformer.getId() == transformer.getId()) {
					transformerList.remove(i);
					transformerList.add(transformer);
				}
			}

			marshallMirth();
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Removes a transformer from the transformer list.
	 * 
	 * @param transformerId
	 *            the id of the transformer
	 * @throws ManagerException
	 *             if the transformer list is <code>null</code>.
	 */
	public void removeTransformer(int transformerId) throws ManagerException {
		try {
			for (int i = 0; i < transformerList.size(); i++) {
				Transformer currentTransformer = transformerList.get(i);

				if (currentTransformer.getId() == transformerId) {
					transformerList.remove(i);
				}
			}

			marshallMirth();
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/*
	 * ------------------------------ Filter ------------------------------
	 */

	/**
	 * Constructs a <code>Filter</code> object.
	 * 
	 * @throws ManagerException
	 *             if the <code>Filter</code> could not be constructed.
	 */
	public Filter createFilter() throws ManagerException {
		try {
			return mirthFactory.createFilter();
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Returns a filter with the specified <code>filterId</code> in the filter
	 * list.
	 * 
	 * @param filterId
	 *            the id of the filter
	 * @return the filter with the specified filter id in the filter list.
	 * @throws ManagerException
	 *             if the filter list is <code>null</code>.
	 */
	public Filter getFilter(int filterId) throws ManagerException {
		try {
			for (int i = 0; i < filterList.size(); i++) {
				Filter currentFilter = filterList.get(i);

				if (currentFilter.getId() == filterId) {
					return currentFilter;
				}
			}

			return null;
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Returns an unmodifiable view of the filter list sorted by name.
	 * 
	 * @return the filter list
	 * @throws ManagerException
	 *             if the filter list is <code>null</code>.
	 */
	public List getFilterList() throws ManagerException {
		if (filterList == null) {
			throw new ManagerException("Filter list is null.");
		} else {
			Collections.sort(filterList, new Comparator() {
				public int compare(Object o1, Object o2) {
					Filter f1 = (Filter) o1;
					Filter f2 = (Filter) o2;

					return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
				}
			});

			return Collections.unmodifiableList(filterList);
		}
	}

	/**
	 * Inserts a filter into the filter list and returns the assigned
	 * <code>filterId</code>.
	 * 
	 * @param filter
	 * @return the assgined filter id
	 * @throws ManagerException
	 *             if the filter list is <code>null</code>.
	 */
	public int addFilter(Filter filter) throws ManagerException {
		try {
			filter.setId(getNewId());
			filterList.add(filter);
		} catch (Exception e) {
			throw new ManagerException(e);
		}

		marshallMirth();
		return filter.getId();
	}

	/**
	 * Updates a filter in the filter list. The outdated filter is removed based
	 * on <code>filterId</code> and the new filter is inserted into the list.
	 * 
	 * @param filter
	 *            the updated filter.
	 * @throws ManagerException
	 *             if the filter list is <code>null</code>.
	 */
	public void updateFilter(Filter filter) throws ManagerException {
		try {
			for (int i = 0; i < filterList.size(); i++) {
				Filter currentFilter = filterList.get(i);

				if (currentFilter.getId() == filter.getId()) {
					filterList.remove(i);
					filterList.add(filter);
				}
			}

			marshallMirth();
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Removes a filter from the filter list.
	 * 
	 * @param filterId
	 *            the id of the filter
	 * @throws ManagerException
	 *             if the filter list is <code>null</code>.
	 */
	public void removeFilter(int filterId) throws ManagerException {
		try {
			for (int i = 0; i < filterList.size(); i++) {
				Filter currentFilter = filterList.get(i);

				if (currentFilter.getId() == filterId) {
					filterList.remove(i);
				}
			}

			marshallMirth();
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/*
	 * ------------------------------ User ------------------------------
	 */

	/**
	 * Constructs a <code>User</code> object.
	 * 
	 * @throws ManagerException
	 *             if the <code>User</code> could not be constructed.
	 */
	public User createUser() throws ManagerException {
		try {
			return mirthFactory.createUser();
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Returns a user with the specified <code>userId</code> in the user list.
	 * 
	 * @param userId
	 *            the id of the user
	 * @return the user with the specified user id in the user list.
	 * @throws ManagerException
	 *             if the user list is <code>null</code>.
	 */
	public User getUser(int userId) throws ManagerException {
		try {
			for (int i = 0; i < userList.size(); i++) {
				User currentUser = userList.get(i);

				if (currentUser.getId() == userId) {
					return currentUser;
				}
			}

			return null;
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Returns an unmodifiable view of the user list sorted by name.
	 * 
	 * @return the user list
	 * @throws ManagerException
	 *             if the user list is <code>null</code>.
	 */
	public List getUserList() throws ManagerException {
		if (userList == null) {
			throw new ManagerException("User list is null.");
		} else {
			Collections.sort(userList, new Comparator() {
				public int compare(Object o1, Object o2) {
					User f1 = (User) o1;
					User f2 = (User) o2;

					return f1.getLogin().toLowerCase().compareTo(f2.getLogin().toLowerCase());
				}
			});

			return Collections.unmodifiableList(userList);
		}
	}

	/**
	 * Inserts a user into the user list and returns the assigned
	 * <code>userId</code>.
	 * 
	 * @param user
	 * @return the assgined user id
	 * @throws ManagerException
	 *             if the user list is <code>null</code>.
	 */
	public int addUser(User user) throws ManagerException {
		try {
			user.setId(getNewId());
			userList.add(user);
		} catch (Exception e) {
			throw new ManagerException(e);
		}

		marshallMirth();
		return user.getId();
	}

	/**
	 * Updates a user in the user list. The outdated user is removed based on
	 * <code>userId</code> and the new user is inserted into the list.
	 * 
	 * @param user
	 *            the updated user.
	 * @throws ManagerException
	 *             if the user list is <code>null</code>.
	 */
	public void updateUser(User user) throws ManagerException {
		try {
			for (int i = 0; i < userList.size(); i++) {
				User currentUser = userList.get(i);

				if (currentUser.getId() == user.getId()) {
					userList.remove(i);
					userList.add(user);
				}
			}

			marshallMirth();
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Removes a user from the user list.
	 * 
	 * @param userId
	 *            the id of the user
	 * @throws ManagerException
	 *             if the user list is <code>null</code>.
	 */
	public void removeUser(int userId) throws ManagerException {
		try {
			for (int i = 0; i < userList.size(); i++) {
				User currentUser = userList.get(i);

				if (currentUser.getId() == userId) {
					userList.remove(i);
				}
			}

			marshallMirth();
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Generates the Mule configuration file.
	 * 
	 * @throws ManagerException
	 *             if the Mule configuration file could not be genereated.
	 */
	public void marshallMule() throws ManagerException {
		// reload the boot-strap configuration
		unmarshallMule();
		
		// open Mule configuration file
		File muleConfigFile = new File(MULE_CONFIG_FILE);

		try {
			if (muleConfigFile.exists()) {
				// create a backup of the existing file
				logger.debug("mule configuration file already exists: " + muleConfigFile.getAbsolutePath());
				muleConfigFile.renameTo(new File(ConfigUtil.removeFileExtension(MULE_CONFIG_FILE) + "_" + ConfigUtil.getTimeStamp() + ".xml"));
				muleConfigFile = new File(MULE_CONFIG_FILE);
			}

			createMuleConnectors();
			createMuleDescriptors();

			// write the file to XML
			logger.debug("writing to mule configuration file");
			Marshaller marsh = muleContext.createMarshaller();
			marsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			OutputFormat of = new OutputFormat();
//			of.setCDataElements(elements);
			of.setIndenting(true);
			of.setLineSeparator("\n");
//			of.setDoctype("-//SymphonySoft //DTD mule-configuration XML V1.0//EN", "http://www.symphonysoft.com/dtds/mule/mule-configuration.dtd");
			of.setOmitDocumentType(true);
			XMLSerializer serializer = new XMLSerializer(of);
			serializer.setOutputByteStream(new FileOutputStream(MULE_CONFIG_FILE));
			
			marsh.marshal(mule, serializer);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new ManagerException("Could not generate Mule configuration file.", e);
		}
	}

	private void createMuleConnectors() throws ManagerException {
		logger.debug("creating mule connectors");

		try {
			List<ConnectorType> muleConnectorList = mule.getConnector();
			
			for (Iterator<Endpoint> endpointIter = getEndpointList().iterator(); endpointIter.hasNext();) {
				Endpoint endpoint = endpointIter.next();
				ConnectorType connector = muleFactory.createConnectorType();
				connector.setName(removeSpaces(endpoint.getName()));
				connector.setClassName(propertyManager.getClassName(endpoint.getType()));

				// get the list of endpoint properties
				List<Property> endpointPropertyList = endpoint.getProperties().getProperty();

				// genereate the list of connector proerties
				connector.setProperties(muleFactory.createPropertiesType());

				// query map for jdbc connector
				PropertiesType.Map queries = null;
				
				// insert all endpoint properties into connector properties list
				for (Iterator<Property> propertyIter = endpointPropertyList.iterator(); propertyIter.hasNext();) {
					Property endpointProperty = propertyIter.next();
					
					// this is a Mule special case for queries and acks so that they are placed in their
					// own map with properties
					if (endpointProperty.getName().equals("query") || endpointProperty.getName().equals("statement") || endpointProperty.getName().equals("ack")) {
						if (queries == null) {
							queries = muleFactory.createPropertiesTypeMap();
							queries.setName("queries");
						}
						
						addMuleProperty(queries.getPropertyOrFactoryPropertyOrContainerProperty(), endpointProperty.getName(), endpointProperty.getValue());
					} else if (propertyManager.getProperty("endpoint", endpoint.getType(), endpointProperty.getName()).isMuleProperty()) {
						// if the property is also a Mule property, add it to the property list
						addMuleProperty(connector.getProperties().getPropertyOrFactoryPropertyOrContainerProperty(), endpointProperty.getName(), endpointProperty.getValue());	
					}
				}

				if (queries != null) {
					connector.getProperties().getPropertyOrFactoryPropertyOrContainerProperty().add(queries);	
				}

				// add the connector to the list of mule connectors
				muleConnectorList.add(connector);
			}
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	private void createMuleDescriptors() throws ManagerException {
		logger.debug("generating mule descriptors");
		
		try {
			for (Iterator<Channel> iter = channelList.iterator(); iter.hasNext();) {
				Channel channel = iter.next();
				
				// only generate "deployed" (enabled) channels
				if (channel.isEnabled()) {
					createDescriptor(channel);	
				}
			}
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	private void createDescriptor(Channel channel) throws ManagerException {
		try {
			logger.debug("generating channel descriptor: " + channel.getName());

			MuleDescriptorType muleDescriptor = muleFactory.createMuleDescriptorType();
			muleDescriptor.setName(channel.getId() + ID_NAME_DELIMETER + channel.getName());
			muleDescriptor.setInitialState("stopped");

			// generate the inbound-router
			InboundRouterType inboundRouter = muleFactory.createInboundRouterType();
			InboundRouterType.Endpoint inEndpoint = muleFactory.createInboundRouterTypeEndpoint();

			// determine which transformer to use
			// if the name of the inbound endpoint has the word "outbound" in it, use the outbound transformer
			// otherwise use the default inbound transformer
			// inbound = Connector -> HL7 -> XML -> [Transformer] -> HashMap -> Connector 
			// outbound = Connector -> Object (ResultSet, ArrayList, etc.) -> XML -> [Transformer] -> HL7 -> Connector
			boolean isOutbound = false;
			
			if (propertyManager.getDirection(getEndpoint(channel.getSourceEndpointId()).getType()).equals("outbound")) {
				isOutbound = true;
			}
			
			if (isOutbound) {
				muleDescriptor.setImplementation("com.webreach.mirth.transformers.OutboundECMAScriptTransformer");	
			} else {
				muleDescriptor.setImplementation("com.webreach.mirth.transformers.InboundECMAScriptTransformer");
			}

			String inboundHost = getPropertyValue(getEndpoint(channel.getSourceEndpointId()).getProperties().getProperty(), "host");

			if (inboundHost == null) {
				inboundHost = getPropertyValue(getEndpoint(channel.getSourceEndpointId()).getProperties().getProperty(), "hostname");
			}
			
			if (inboundHost == null) {
				inboundHost = "";
			}
			
			String inboundPort = getPropertyValue(getEndpoint(channel.getSourceEndpointId()).getProperties().getProperty(), "port");
			String inboundProtocol = propertyManager.getProtocol(getEndpoint(channel.getSourceEndpointId()).getType());
			
			if ((inboundPort != null) && (!inboundHost.equals(""))) {
				inEndpoint.setAddress(inboundProtocol + "://" + inboundHost + ":" + inboundPort);	
			} else {
				inEndpoint.setAddress(inboundProtocol + "://" + inboundHost);
			}
			
			inEndpoint.setConnector(removeSpaces(getEndpoint(channel.getSourceEndpointId()).getName()));
			
			// set the transformers
			String inTransformers = propertyManager.getTransformers(getEndpoint(channel.getSourceEndpointId()).getType());
			
			if (!inTransformers.equals("")) {
				inEndpoint.setTransformers(inTransformers);	
			}
			
			
			inboundRouter.getEndpoint().add(inEndpoint);

			// generate the router
			InboundRouterType.Router inRouter = muleFactory.createInboundRouterTypeRouter();
			inRouter.setClassName("org.mule.routing.inbound.SelectiveConsumer");

			// generate the filter
			FilterType filter = muleFactory.createFilterType();

			// determine which filter component to use
			if (getFilter(Integer.parseInt(channel.getFilterId().split(ID_SEQ_DELIMETER)[0])).getScript().getType().equals(SCRIPT_ECMA)) {
				filter.setClassName("com.webreach.mirth.filters.ECMAScriptFilter");
			} else if (getFilter(Integer.parseInt(channel.getFilterId().split(ID_SEQ_DELIMETER)[0])).getScript().getType().equals(SCRIPT_PYTHON)) {
				filter.setClassName("com.webreach.mirth.filters.PythonFilter");
			}

			// set the filter script property
			filter.setProperties(muleFactory.createPropertiesType());
			addMuleProperty(filter.getProperties().getPropertyOrFactoryPropertyOrContainerProperty(), "script", getFilter(Integer.parseInt(channel.getFilterId().split(ID_SEQ_DELIMETER)[0])).getScript().getValue());

			// set the filter for the router
			inRouter.setFilter(filter);	
			
			// add the router to the inbound-router list
			// TODO: still filter but different for outbound
			if (!isOutbound) {
				inboundRouter.getRouter().add(inRouter);	
			}
			
			// add the inbound-router to the inbound descriptor
			muleDescriptor.setInboundRouter(inboundRouter);
			
			// generate the outbound-router
			OutboundRouterType outboundRouter = muleFactory.createOutboundRouterType();
			List<RouterType> outRouterList = outboundRouter.getRouter();

			// generate a new router for the outbound-router
			RouterType outRouter = muleFactory.createRouterType();
			outRouter.setClassName("org.mule.routing.outbound.MulticastingRouter");
			List<EndpointType> outRouterEndpointList = outRouter.getEndpoint();

			// string array of destination endpoint ids
			String destinationEndpointIdList[] = channel.getDestinationEndpointId().split(" ");
			
			for (int i = 0; i < destinationEndpointIdList.length; i++) {
				Endpoint endpoint = getEndpoint(Integer.parseInt(destinationEndpointIdList[i]));
			
				// generate the outbound endpoint for the router
				EndpointType outEndpoint = muleFactory.createEndpointType();
				String outboundHost = getPropertyValue(endpoint.getProperties().getProperty(), "host");
				
				if (outboundHost == null) {
					outboundHost = getPropertyValue(endpoint.getProperties().getProperty(), "hostname");
				}
				
				if (outboundHost == null) {
					outboundHost = "";
				}
				
				String outboundPort = getPropertyValue(endpoint.getProperties().getProperty(), "port");
				String outboundProtocol = propertyManager.getProtocol(endpoint.getType());
				
				if ((outboundPort != null) && (!outboundHost.equals(""))) {
					outEndpoint.setAddress(outboundProtocol + "://" + outboundHost + ":" + outboundPort);	
				} else {
					outEndpoint.setAddress(outboundProtocol + "://" + outboundHost);
				}
	
				outEndpoint.setConnector(removeSpaces(endpoint.getName()));
	
				// set the transformers
				String outTransformers = propertyManager.getTransformers(endpoint.getType());
				
				if (!outTransformers.equals("")) {
					outEndpoint.setTransformers(outTransformers);	
				}
				
				// add the endpoint to the router
				outRouterEndpointList.add(outEndpoint);
			}
				
			// add the ACK endpoint
//			EndpointType ackEndpoint = muleFactory.createEndpointType();
//			ackEndpoint.setAddress("vm://ackgenerator");
//			outRouterEndpointList.add(ackEndpoint);
			
			// add the router to the list
			outRouterList.add(outRouter);
			// add the outbound-router to the outbound descriptor
			muleDescriptor.setOutboundRouter(outboundRouter);

			// generate the descriptor properties list
			muleDescriptor.setProperties(muleFactory.createPropertiesType());

			// set the properties for the transformer
			if (channel.isOutbound()) {
				addMuleProperty(muleDescriptor.getProperties().getPropertyOrFactoryPropertyOrContainerProperty(), "script", getTransformer(Integer.parseInt(channel.getTransformerId().split(ID_SEQ_DELIMETER)[0])).getScript().getValue());
			} else {
				addMuleProperty(muleDescriptor.getProperties().getPropertyOrFactoryPropertyOrContainerProperty(), "script", getTransformer(Integer.parseInt(channel.getTransformerId().split(ID_SEQ_DELIMETER)[0])).getScript().getValue());
//				addMuleProperty(muleDescriptor.getProperties().getPropertyOrFactoryPropertyOrContainerProperty(), "returnClass", getTransformer(Integer.parseInt(channel.getTransformerId().split(ID_SEQ_DELIMETER)[0])).getScript().getReturnClass());
			}
			
			// add the inbound descriptor to the model descriptor list
			mule.getModel().getMuleDescriptor().add(muleDescriptor);
		} catch (Exception e) {
//			throw new ManagerException(e);
			e.printStackTrace();
		}
	}

	/**
	 * Constructs a <code>Script</code> object.
	 * 
	 * @throws ManagerException
	 *             if the <code>Script</code> could not be constructed.
	 */
	public Script createScript() throws ManagerException {
		try {
			return mirthFactory.createScript();
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Constructs a <code>Property</code> object.
	 * 
	 * @throws ManagerException
	 *             if the <code>Property</code> could not be constructed.
	 */
	public Property createProperty() throws ManagerException {
		try {
			return mirthFactory.createProperty();
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Returns the version of ConfigurationManager.
	 * 
	 * @return the version of ConfigurationManager.
	 */
	public String getVersion() {
		Properties properties = new Properties();

		try {
			properties.load(new FileInputStream(BUILD_PROPERTIES));
			return properties.getProperty("build.version");
		} catch (IOException e) {
			logger.warn(e);

			return "0.0";
		}
	}

	/**
	 * Returns the Mirth build number.
	 * 
	 * @return the Mirth build number.
	 */
	public String getBuildNumber() {
		Properties properties = new Properties();

		try {
			properties.load(new FileInputStream(BUILD_NUMBER));
			return properties.getProperty("build.number");
		} catch (IOException e) {
			logger.warn(e);

			return "0";
		}
	}

	// returns a new id based on the last used id
	private int getNewId() {
		mirth.setLastId(mirth.getLastId() + 1);
		return mirth.getLastId();
	}

	/**
	 * Returns the value of a <code>Property</code> with the given name in the
	 * given Property list.
	 * 
	 * @return the value of a <code>Property</code> with the given name in the
	 *         given Property list.
	 */
	public String getPropertyValue(List<Property> propertyList, String name) {
		for (Iterator<Property> iter = propertyList.iterator(); iter.hasNext();) {
			Property property = iter.next();

			if (property.getName().equals(name)) {
				return property.getValue();
			}
		}
		
		return null;
	}

	/**
	 * Creates a new Mirth <code>Property</code> with the given name and value
	 * and inserts it into the the given <code>Property</code> list. If the
	 * property with the given name is already in the list, its value will be
	 * updated.
	 * 
	 */
	public void addMirthProperty(List<Property> propertyList, String name, String value) throws ManagerException {
		try {
			// if the property is already in the list
			for (int i = 0; i < propertyList.size(); i++) {
				if (propertyList.get(i).getName().equals(name)) {
					// update the property value
					propertyList.get(i).setValue(value);

					return;
				}
			}

			Property property = mirthFactory.createProperty();
			property.setName(name);
			property.setValue(value);
			propertyList.add(property);
		} catch (JAXBException e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Creates a new Mule <code>Property</code> with the given name and
	 * value and inserts it into the the given <code>Property</code> list.
	 * If the property with the given name is already in the list, its value
	 * will be updated.
	 * 
	 */
	private void addMuleProperty(List<PropertiesType.Property> propertyList, String name, String value) throws ManagerException {
		try {
			// if the property is already in the list
			for (int i = 0; i < propertyList.size(); i++) {
				if (propertyList.get(i).getName().equals(name)) {
					// update the property value
					propertyList.get(i).setValue(value);

					return;
				}
			}

			PropertiesType.Property property = muleFactory.createPropertiesTypeProperty();
			property.setName(name);
			property.setValue(value);
			
			System.out.println("adding property: " + name + " = " + value);
			
			propertyList.add(property);
		} catch (JAXBException e) {
//			throw new ManagerException(e);
			e.printStackTrace();
		}
	}

	/**
	 * Clears the properties list for a specified endpoint.
	 * 
	 * @param endpoint
	 */
	public void clearProperties(Endpoint endpoint) {
		endpoint.getProperties().getProperty().clear();
	}

	/**
	 * Returns <code>true</code> if ConfigurationManager has been initialized.
	 * 
	 * @return <code>true</code> if ConfigurationManager has been initialized.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Returns the Mule XML configuration id.
	 * 
	 * @return the Mule XML configuration id.
	 * @throws ManagerException
	 */
	public String getConfigurationId() throws ManagerException {
		// TODO: fix this!
//		if (mule == null) {
//			throw new ManagerException("Could not retrieve configuration id from Mule configuration file.");
//		}
//
//		return mule.getId();
		
		return "MirthConfiguration";
	}
	
	private String removeSpaces(String string) {
		return string.replaceAll(" ", "_");
	}
}
