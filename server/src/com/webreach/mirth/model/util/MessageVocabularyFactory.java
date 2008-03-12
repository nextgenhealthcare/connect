package com.webreach.mirth.model.util;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Constructor;

import com.webreach.mirth.client.core.Client;
import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.ExtensionPoint;
import com.webreach.mirth.model.ExtensionPointDefinition;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.dicom.DICOMVocabulary;
import com.webreach.mirth.model.hl7v2.HL7v2Vocabulary;
import com.webreach.mirth.model.ncpdp.NCPDPVocabulary;
import com.webreach.mirth.model.x12.X12Vocabulary;

public class MessageVocabularyFactory {
	 
	private static MessageVocabularyFactory instance = null;

	private MessageVocabularyFactory() {
		
	}
	
	public static MessageVocabularyFactory getInstance(Client mirthClient) {
		synchronized (MessageVocabularyFactory.class) {
			if (instance == null) {
				instance = new MessageVocabularyFactory(mirthClient);
			}
			return instance;
		}
	}    
    
	private Map<Protocol, Class<? extends MessageVocabulary>> loadedPlugins = new HashMap<Protocol, Class<?extends MessageVocabulary>>();
	
	public MessageVocabularyFactory(Client mirthClient) {
		try {
			setupBuiltInVocab();
			loadPlugins(mirthClient.getPluginMetaData());
		} catch (ClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public MessageVocabularyFactory(Map<String, PluginMetaData> plugins) {
		setupBuiltInVocab();
		loadPlugins(plugins);
	}
	private void setupBuiltInVocab(){
		//Load the default Mirth vocabs
		//TODO: Possibly make these plugins in 1.6.1?
		loadedPlugins.put(Protocol.HL7V2, HL7v2Vocabulary.class);
		loadedPlugins.put(Protocol.X12, X12Vocabulary.class);
		loadedPlugins.put(Protocol.NCPDP, NCPDPVocabulary.class);
        loadedPlugins.put(Protocol.DICOM, DICOMVocabulary.class);
	}

	public MessageVocabulary getVocabulary(Protocol protocol, String version, String type) {
		
		Class<? extends MessageVocabulary> vocabulary = loadedPlugins.get(protocol);
		MessageVocabulary vocab = null;
		if (vocabulary != null) {
			try {
                
                Constructor[] constructors = vocabulary.getDeclaredConstructors();
                for (int i=0; i < constructors.length; i++) {
                    Class parameters[];
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

	// Extension point for ExtensionPoint.Type.CLIENT_VOCABULARY
	@ExtensionPointDefinition(mode = ExtensionPoint.Mode.CLIENT, type = ExtensionPoint.Type.CLIENT_VOCABULARY)
	public void loadPlugins(Map<String, PluginMetaData> plugins) {
		try {
			for (PluginMetaData metaData : plugins.values()) {
				if (metaData.isEnabled()) {
					for (ExtensionPoint extensionPoint : metaData.getExtensionPoints()) {
						try {
							if (extensionPoint.getMode().equals(ExtensionPoint.Mode.CLIENT) && extensionPoint.getType().equals(ExtensionPoint.Type.CLIENT_VOCABULARY) && extensionPoint.getClassName() != null && extensionPoint.getClassName().length() > 0) {
								String pluginName = extensionPoint.getName();
								loadedPlugins.put(Protocol.valueOf(pluginName), Class.forName(extensionPoint.getClassName()).asSubclass(MessageVocabulary.class));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
