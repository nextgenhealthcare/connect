package com.mirth.connect.plugins;

import org.syntax.jedit.tokenmarker.TokenMarker;
import org.w3c.dom.Element;

import com.mirth.connect.model.attachments.AttachmentHandlerType;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.model.util.MessageVocabulary;

public abstract class DataTypeClientPlugin extends ClientPlugin {

    public DataTypeClientPlugin(String name) {
        super(name);
    }
    
    /**
     * Get an instance of the data type's serializer with the given properties
     */
    final public IXMLSerializer getSerializer(SerializerProperties properties) {
        return getDataTypeDelegate().getSerializer(properties);
    }
    
    /**
     * Indicates if the data type is in binary format
     */
    final public boolean isBinary() {
        return getDataTypeDelegate().isBinary();
    }
    
    /**
     * Get the default properties of the data type. Must not return null.
     */
    public abstract DataTypeProperties getDefaultProperties();
    
    /**
     * Get the data type delegate that is used for client/server shared methods
     */
    protected abstract DataTypeDelegate getDataTypeDelegate();
    
    /**
     * Get the display name that is shown in the client UI
     */
    public abstract String getDisplayName();
    
    /**
     * Get the default attachment handler type
     */
    public abstract AttachmentHandlerType getDefaultAttachmentHandlerType();
    
    /**
     * Get the token marker used by the client UI
     */
    public abstract TokenMarker getTokenMarker();
    
    /**
     * Get the message vocabulary
     */
    public abstract Class<? extends MessageVocabulary> getVocabulary();
    
    /**
     * If this data type is binary data, use this method to convert the byte data to a string for the template panel.
     * If this data type is not binary, this method is not used.
     */
    public abstract String getTemplateString(byte[] content) throws Exception;
    
    /**
     * Get the minimum tree level before the tree panel will not create an extra node if the field does not have child nodes
     */
    public abstract int getMinTreeLevel();
    
    /**
     * Get the node text for a given element in the tree.
     */
    public String getNodeText(MessageVocabulary vocabulary, Element element) {
        String description = vocabulary.getDescription(element.getNodeName());
        
        String nodeText;
        if (description != null && description.length() > 0) {
            nodeText = element.getNodeName() + " (" + description + ")";
        } else {
            nodeText = element.getNodeName();
        }
        
        return nodeText;
    }
    
}
