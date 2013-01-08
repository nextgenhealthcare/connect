package com.mirth.connect.plugins;

import java.util.Map;

import com.mirth.connect.donkey.model.message.AutoResponder;
import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.model.DataTypeDelegate;
import com.mirth.connect.model.converters.IXMLSerializer;

public abstract class DataTypeServerPlugin implements ServerPlugin {
    
    /**
     * Get an instance of the data type's serializer with the given properties
     */
    final public IXMLSerializer getSerializer(Map<?, ?> properties) {
        return getDataTypeDelegate().getSerializer(properties);
    }
    
    /**
     * Get the default properties of the data type
     */
    final public Map<String, String> getDefaultProperties() {
        return getDataTypeDelegate().getDefaultProperties();
    }
    
    /** 
     * Indicates if the data type is in XML format
     */
    final public boolean isXml() {
        return getDataTypeDelegate().isXml();
    }
    
    /**
     * Indicates if the data type is in binary format
     */
    final public boolean isBinary() {
        return getDataTypeDelegate().isXml();
    }
    
    /**
     * Get the data type delegate that is used for client/server shared methods
     */
    protected abstract DataTypeDelegate getDataTypeDelegate();
    
    /**
     * Get the auto responder for the data type
     */
    public abstract AutoResponder getAutoResponder(Map<?, ?> properties);
    
    /**
     * Returns whether namespaces should be stripped for the given properties
     */
    public abstract boolean isStripNamespaces(Map<?, ?> properties);
    
    /**
     * Get the batch script from a serializer.
     * Returns null if no script exists.
     */
    public String getBatchScript(XmlSerializer serializer) {
        return null;
    }
}
