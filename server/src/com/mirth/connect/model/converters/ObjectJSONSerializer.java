package com.mirth.connect.model.converters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.mirth.connect.util.JsonXmlUtil;

public class ObjectJSONSerializer {

    private static final ObjectJSONSerializer instance = new ObjectJSONSerializer();
    
    private Logger logger = Logger.getLogger(getClass());
    
    public static ObjectJSONSerializer getInstance() {
        return instance;
    }
    
    // Object -> XML -> JSON
    public void serialize(Object object, OutputStream outputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter xmlWriter = new OutputStreamWriter(baos, "UTF-8");
        ObjectXMLSerializer.getInstance().serialize(object, xmlWriter);
        
        String xmlString = baos.toString();
        String jsonString = "";
        
        // Our xml util xml to json
        try {
            jsonString = JsonXmlUtil.xmlToJson(xmlString);
        } catch (Exception e) {
            logger.error(e);
            throw new SerializerException(e);
        }
        
        outputStream.write(jsonString.getBytes());
    }
    
    /* Converts a source JSON string to XML then calls ObjectXMLSerializer.deserialize(...)
     * JSON -> XML -> Object
     */
    public <T> T deserialize(String serializedObject, Class<T> expectedClass) {
        String xmlSerializedObject = "";
        
        // Our json -> xml
        try {
            xmlSerializedObject = JsonXmlUtil.jsonToXml(serializedObject);
        } catch (Exception e) {
            logger.error(e);
            throw new SerializerException(e);
        }
        
        return ObjectXMLSerializer.getInstance().deserialize(xmlSerializedObject, expectedClass);
    }
    
    /**
     * Converts a source JSON string to XML then calls ObjectXMLSerializer.deserializeList(...).
     * JSON -> XML -> Object
     */
    public <T> List<T> deserializeList(String serializedObject, Class<T> expectedListItemClass) {
        String xmlSerializedObject = "";
        
        // Our json -> xml
        try {
            xmlSerializedObject = JsonXmlUtil.jsonToXml(serializedObject);
        } catch (Exception e) {
            logger.error(e);
            throw new SerializerException(e);
        }
        
        return ObjectXMLSerializer.getInstance().deserializeList(xmlSerializedObject, expectedListItemClass);
    }
}
