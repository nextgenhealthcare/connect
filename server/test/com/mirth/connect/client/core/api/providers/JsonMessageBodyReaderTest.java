package com.mirth.connect.client.core.api.providers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.client.core.Version;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.converters.ObjectJSONSerializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class JsonMessageBodyReaderTest {
    TestJsonMessageBodyReader reader;
    
    @Before
    public void Setup() {
        reader = new TestJsonMessageBodyReader();
    }
    
    @BeforeClass
    public static void SetupApi() throws Exception {
        try {
            ObjectXMLSerializer.getInstance().init(Version.getLatest().toString());
        } catch (Exception e) {
            // Ignore if it has already been initialized
        }
    }
    
    @Test
    public void testReadFromSingleObject() throws WebApplicationException, IOException {
        InputStream entityStream = new ByteArrayInputStream(GOOD_CHANNEL.getBytes("UTF-8"));
        MediaType mediaType = new MediaType("application", "json");
        
        @SuppressWarnings("unchecked")
        Class<Object>clazz = (Class<Object>) ((Class<?>) Channel.class);
        
        Type genericType = (Type) Channel.class; 
        
        Channel obj = (Channel) reader.readFrom(clazz, genericType, null, mediaType, null, entityStream);
        
        assertEquals(GOOD_CHANNEL_ID, obj.getId());
        
        ObjectJSONSerializer spySerializer = reader.getObjectJsonSerializer();
        verify(spySerializer, times(1)).deserialize(any(), any());
        verify(spySerializer, times(0)).deserializeList(any(), any());
    }
    
    @Test
    public void testReadFromList() throws WebApplicationException, IOException, ClassNotFoundException {
        InputStream entityStream = new ByteArrayInputStream("{\"list\":{\"string\":[\"hello\",\"world\"]}}".getBytes("UTF-8"));
        MediaType mediaType = new MediaType("application", "json");
        
        @SuppressWarnings("unchecked")
        Class<Object>clazz = (Class<Object>) ((Class<?>) List.class);
        
        ParameterizedType genericType = TypeUtils.parameterize(List.class, String.class);
        
        @SuppressWarnings("unchecked")
        List<String> obj = (List<String>) reader.readFrom(clazz, genericType, null, mediaType, null, entityStream);
        assertEquals("hello", obj.get(0));
        assertEquals("world", obj.get(1));
        
        ObjectJSONSerializer spySerializer = reader.getObjectJsonSerializer();
        verify(spySerializer, times(1)).deserializeList(any(), any());
        verify(spySerializer, times(0)).deserialize(any(), any());
    }
        
    private static final String GOOD_CHANNEL_ID = "340f6381-7259-4a4d-baf3-694432697ae6";
    
    // @formatter:off
    private static final String GOOD_CHANNEL = 
            "{\n" + 
            "  \"channel\": {\n" + 
            "    \"@version\": \"3.9.0\",\n" + 
            "    \"id\": \"" + GOOD_CHANNEL_ID + "\",\n" + 
            "    \"nextMetaDataId\": 2,\n" + 
            "    \"name\": \"test1\",\n" + 
            "    \"description\": null,\n" + 
            "    \"revision\": 2,\n" + 
            "    \"sourceConnector\": {\n" + 
            "      \"@version\": \"3.9.0\",\n" + 
            "      \"metaDataId\": 0,\n" + 
            "      \"name\": \"sourceConnector\",\n" + 
            "      \"properties\": {\n" + 
            "        \"@class\": \"com.mirth.connect.connectors.vm.VmReceiverProperties\",\n" + 
            "        \"@version\": \"3.9.0\",\n" + 
            "        \"pluginProperties\": null,\n" + 
            "        \"sourceConnectorProperties\": {\n" + 
            "          \"@version\": \"3.9.0\",\n" + 
            "          \"responseVariable\": \"None\",\n" + 
            "          \"respondAfterProcessing\": true,\n" + 
            "          \"processBatch\": false,\n" + 
            "          \"firstResponse\": false,\n" + 
            "          \"processingThreads\": 1,\n" + 
            "          \"resourceIds\": {\n" + 
            "            \"@class\": \"linked-hash-map\",\n" + 
            "            \"entry\": {\n" + 
            "              \"string\": [\n" + 
            "                \"Default Resource\",\n" + 
            "                \"[Default Resource]\"\n" + 
            "              ]\n" + 
            "            }\n" + 
            "          },\n" + 
            "          \"queueBufferSize\": 1000\n" + 
            "        }\n" + 
            "      },\n" + 
            "      \"transformer\": {\n" + 
            "        \"@version\": \"3.9.0\",\n" + 
            "        \"elements\": null,\n" + 
            "        \"inboundDataType\": \"HL7V2\",\n" + 
            "        \"outboundDataType\": \"HL7V2\",\n" + 
            "        \"inboundProperties\": {\n" + 
            "          \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\",\n" + 
            "          \"@version\": \"3.9.0\",\n" + 
            "          \"serializationProperties\": {\n" + 
            "            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\",\n" + 
            "            \"@version\": \"3.9.0\",\n" + 
            "            \"handleRepetitions\": true,\n" + 
            "            \"handleSubcomponents\": true,\n" + 
            "            \"useStrictParser\": false,\n" + 
            "            \"useStrictValidation\": false,\n" + 
            "            \"stripNamespaces\": true,\n" + 
            "            \"segmentDelimiter\": \"\\\\r\",\n" + 
            "            \"convertLineBreaks\": true\n" + 
            "          },\n" + 
            "          \"deserializationProperties\": {\n" + 
            "            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\",\n" + 
            "            \"@version\": \"3.9.0\",\n" + 
            "            \"useStrictParser\": false,\n" + 
            "            \"useStrictValidation\": false,\n" + 
            "            \"segmentDelimiter\": \"\\\\r\"\n" + 
            "          },\n" + 
            "          \"batchProperties\": {\n" + 
            "            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\",\n" + 
            "            \"@version\": \"3.9.0\",\n" + 
            "            \"splitType\": \"MSH_Segment\",\n" + 
            "            \"batchScript\": null\n" + 
            "          },\n" + 
            "          \"responseGenerationProperties\": {\n" + 
            "            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\",\n" + 
            "            \"@version\": \"3.9.0\",\n" + 
            "            \"segmentDelimiter\": \"\\\\r\",\n" + 
            "            \"successfulACKCode\": \"AA\",\n" + 
            "            \"successfulACKMessage\": null,\n" + 
            "            \"errorACKCode\": \"AE\",\n" + 
            "            \"errorACKMessage\": \"An Error Occurred Processing Message.\",\n" + 
            "            \"rejectedACKCode\": \"AR\",\n" + 
            "            \"rejectedACKMessage\": \"Message Rejected.\",\n" + 
            "            \"msh15ACKAccept\": false,\n" + 
            "            \"dateFormat\": \"yyyyMMddHHmmss.SSS\"\n" + 
            "          },\n" + 
            "          \"responseValidationProperties\": {\n" + 
            "            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\",\n" + 
            "            \"@version\": \"3.9.0\",\n" + 
            "            \"successfulACKCode\": \"AA,CA\",\n" + 
            "            \"errorACKCode\": \"AE,CE\",\n" + 
            "            \"rejectedACKCode\": \"AR,CR\",\n" + 
            "            \"validateMessageControlId\": true,\n" + 
            "            \"originalMessageControlId\": \"Destination_Encoded\",\n" + 
            "            \"originalIdMapVariable\": null\n" + 
            "          }\n" + 
            "        },\n" + 
            "        \"outboundProperties\": {\n" + 
            "          \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\",\n" + 
            "          \"@version\": \"3.9.0\",\n" + 
            "          \"serializationProperties\": {\n" + 
            "            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\",\n" + 
            "            \"@version\": \"3.9.0\",\n" + 
            "            \"handleRepetitions\": true,\n" + 
            "            \"handleSubcomponents\": true,\n" + 
            "            \"useStrictParser\": false,\n" + 
            "            \"useStrictValidation\": false,\n" + 
            "            \"stripNamespaces\": true,\n" + 
            "            \"segmentDelimiter\": \"\\\\r\",\n" + 
            "            \"convertLineBreaks\": true\n" + 
            "          },\n" + 
            "          \"deserializationProperties\": {\n" + 
            "            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\",\n" + 
            "            \"@version\": \"3.9.0\",\n" + 
            "            \"useStrictParser\": false,\n" + 
            "            \"useStrictValidation\": false,\n" + 
            "            \"segmentDelimiter\": \"\\\\r\"\n" + 
            "          },\n" + 
            "          \"batchProperties\": {\n" + 
            "            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\",\n" + 
            "            \"@version\": \"3.9.0\",\n" + 
            "            \"splitType\": \"MSH_Segment\",\n" + 
            "            \"batchScript\": null\n" + 
            "          },\n" + 
            "          \"responseGenerationProperties\": {\n" + 
            "            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\",\n" + 
            "            \"@version\": \"3.9.0\",\n" + 
            "            \"segmentDelimiter\": \"\\\\r\",\n" + 
            "            \"successfulACKCode\": \"AA\",\n" + 
            "            \"successfulACKMessage\": null,\n" + 
            "            \"errorACKCode\": \"AE\",\n" + 
            "            \"errorACKMessage\": \"An Error Occurred Processing Message.\",\n" + 
            "            \"rejectedACKCode\": \"AR\",\n" + 
            "            \"rejectedACKMessage\": \"Message Rejected.\",\n" + 
            "            \"msh15ACKAccept\": false,\n" + 
            "            \"dateFormat\": \"yyyyMMddHHmmss.SSS\"\n" + 
            "          },\n" + 
            "          \"responseValidationProperties\": {\n" + 
            "            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\",\n" + 
            "            \"@version\": \"3.9.0\",\n" + 
            "            \"successfulACKCode\": \"AA,CA\",\n" + 
            "            \"errorACKCode\": \"AE,CE\",\n" + 
            "            \"rejectedACKCode\": \"AR,CR\",\n" + 
            "            \"validateMessageControlId\": true,\n" + 
            "            \"originalMessageControlId\": \"Destination_Encoded\",\n" + 
            "            \"originalIdMapVariable\": null\n" + 
            "          }\n" + 
            "        }\n" + 
            "      },\n" + 
            "      \"filter\": {\n" + 
            "        \"@version\": \"3.9.0\",\n" + 
            "        \"elements\": null\n" + 
            "      },\n" + 
            "      \"transportName\": \"Channel Reader\",\n" + 
            "      \"mode\": \"SOURCE\",\n" + 
            "      \"enabled\": true,\n" + 
            "      \"waitForPrevious\": true\n" + 
            "    },\n" + 
            "    \"destinationConnectors\": {\n" + 
            "      \"connector\": {\n" + 
            "        \"@version\": \"3.9.0\",\n" + 
            "        \"metaDataId\": 1,\n" + 
            "        \"name\": \"Destination 1\",\n" + 
            "        \"properties\": {\n" + 
            "          \"@class\": \"com.mirth.connect.connectors.vm.VmDispatcherProperties\",\n" + 
            "          \"@version\": \"3.9.0\",\n" + 
            "          \"pluginProperties\": null,\n" + 
            "          \"destinationConnectorProperties\": {\n" + 
            "            \"@version\": \"3.9.0\",\n" + 
            "            \"queueEnabled\": false,\n" + 
            "            \"sendFirst\": false,\n" + 
            "            \"retryIntervalMillis\": 10000,\n" + 
            "            \"regenerateTemplate\": false,\n" + 
            "            \"retryCount\": 0,\n" + 
            "            \"rotate\": false,\n" + 
            "            \"includeFilterTransformer\": false,\n" + 
            "            \"threadCount\": 1,\n" + 
            "            \"threadAssignmentVariable\": null,\n" + 
            "            \"validateResponse\": false,\n" + 
            "            \"resourceIds\": {\n" + 
            "              \"@class\": \"linked-hash-map\",\n" + 
            "              \"entry\": {\n" + 
            "                \"string\": [\n" + 
            "                  \"Default Resource\",\n" + 
            "                  \"[Default Resource]\"\n" + 
            "                ]\n" + 
            "              }\n" + 
            "            },\n" + 
            "            \"queueBufferSize\": 1000,\n" + 
            "            \"reattachAttachments\": true\n" + 
            "          },\n" + 
            "          \"channelId\": \"none\",\n" + 
            "          \"channelTemplate\": \"${message.encodedData}\",\n" + 
            "          \"mapVariables\": null\n" + 
            "        },\n" + 
            "        \"transformer\": {\n" + 
            "          \"@version\": \"3.9.0\",\n" + 
            "          \"elements\": null,\n" + 
            "          \"inboundDataType\": \"HL7V2\",\n" + 
            "          \"outboundDataType\": \"HL7V2\",\n" + 
            "          \"inboundProperties\": {\n" + 
            "            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\",\n" + 
            "            \"@version\": \"3.9.0\",\n" + 
            "            \"serializationProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"handleRepetitions\": true,\n" + 
            "              \"handleSubcomponents\": true,\n" + 
            "              \"useStrictParser\": false,\n" + 
            "              \"useStrictValidation\": false,\n" + 
            "              \"stripNamespaces\": true,\n" + 
            "              \"segmentDelimiter\": \"\\\\r\",\n" + 
            "              \"convertLineBreaks\": true\n" + 
            "            },\n" + 
            "            \"deserializationProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"useStrictParser\": false,\n" + 
            "              \"useStrictValidation\": false,\n" + 
            "              \"segmentDelimiter\": \"\\\\r\"\n" + 
            "            },\n" + 
            "            \"batchProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"splitType\": \"MSH_Segment\",\n" + 
            "              \"batchScript\": null\n" + 
            "            },\n" + 
            "            \"responseGenerationProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"segmentDelimiter\": \"\\\\r\",\n" + 
            "              \"successfulACKCode\": \"AA\",\n" + 
            "              \"successfulACKMessage\": null,\n" + 
            "              \"errorACKCode\": \"AE\",\n" + 
            "              \"errorACKMessage\": \"An Error Occurred Processing Message.\",\n" + 
            "              \"rejectedACKCode\": \"AR\",\n" + 
            "              \"rejectedACKMessage\": \"Message Rejected.\",\n" + 
            "              \"msh15ACKAccept\": false,\n" + 
            "              \"dateFormat\": \"yyyyMMddHHmmss.SSS\"\n" + 
            "            },\n" + 
            "            \"responseValidationProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"successfulACKCode\": \"AA,CA\",\n" + 
            "              \"errorACKCode\": \"AE,CE\",\n" + 
            "              \"rejectedACKCode\": \"AR,CR\",\n" + 
            "              \"validateMessageControlId\": true,\n" + 
            "              \"originalMessageControlId\": \"Destination_Encoded\",\n" + 
            "              \"originalIdMapVariable\": null\n" + 
            "            }\n" + 
            "          },\n" + 
            "          \"outboundProperties\": {\n" + 
            "            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\",\n" + 
            "            \"@version\": \"3.9.0\",\n" + 
            "            \"serializationProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"handleRepetitions\": true,\n" + 
            "              \"handleSubcomponents\": true,\n" + 
            "              \"useStrictParser\": false,\n" + 
            "              \"useStrictValidation\": false,\n" + 
            "              \"stripNamespaces\": true,\n" + 
            "              \"segmentDelimiter\": \"\\\\r\",\n" + 
            "              \"convertLineBreaks\": true\n" + 
            "            },\n" + 
            "            \"deserializationProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"useStrictParser\": false,\n" + 
            "              \"useStrictValidation\": false,\n" + 
            "              \"segmentDelimiter\": \"\\\\r\"\n" + 
            "            },\n" + 
            "            \"batchProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"splitType\": \"MSH_Segment\",\n" + 
            "              \"batchScript\": null\n" + 
            "            },\n" + 
            "            \"responseGenerationProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"segmentDelimiter\": \"\\\\r\",\n" + 
            "              \"successfulACKCode\": \"AA\",\n" + 
            "              \"successfulACKMessage\": null,\n" + 
            "              \"errorACKCode\": \"AE\",\n" + 
            "              \"errorACKMessage\": \"An Error Occurred Processing Message.\",\n" + 
            "              \"rejectedACKCode\": \"AR\",\n" + 
            "              \"rejectedACKMessage\": \"Message Rejected.\",\n" + 
            "              \"msh15ACKAccept\": false,\n" + 
            "              \"dateFormat\": \"yyyyMMddHHmmss.SSS\"\n" + 
            "            },\n" + 
            "            \"responseValidationProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"successfulACKCode\": \"AA,CA\",\n" + 
            "              \"errorACKCode\": \"AE,CE\",\n" + 
            "              \"rejectedACKCode\": \"AR,CR\",\n" + 
            "              \"validateMessageControlId\": true,\n" + 
            "              \"originalMessageControlId\": \"Destination_Encoded\",\n" + 
            "              \"originalIdMapVariable\": null\n" + 
            "            }\n" + 
            "          }\n" + 
            "        },\n" + 
            "        \"responseTransformer\": {\n" + 
            "          \"@version\": \"3.9.0\",\n" + 
            "          \"elements\": null,\n" + 
            "          \"inboundDataType\": \"HL7V2\",\n" + 
            "          \"outboundDataType\": \"HL7V2\",\n" + 
            "          \"inboundProperties\": {\n" + 
            "            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\",\n" + 
            "            \"@version\": \"3.9.0\",\n" + 
            "            \"serializationProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"handleRepetitions\": true,\n" + 
            "              \"handleSubcomponents\": true,\n" + 
            "              \"useStrictParser\": false,\n" + 
            "              \"useStrictValidation\": false,\n" + 
            "              \"stripNamespaces\": true,\n" + 
            "              \"segmentDelimiter\": \"\\\\r\",\n" + 
            "              \"convertLineBreaks\": true\n" + 
            "            },\n" + 
            "            \"deserializationProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"useStrictParser\": false,\n" + 
            "              \"useStrictValidation\": false,\n" + 
            "              \"segmentDelimiter\": \"\\\\r\"\n" + 
            "            },\n" + 
            "            \"batchProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"splitType\": \"MSH_Segment\",\n" + 
            "              \"batchScript\": null\n" + 
            "            },\n" + 
            "            \"responseGenerationProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"segmentDelimiter\": \"\\\\r\",\n" + 
            "              \"successfulACKCode\": \"AA\",\n" + 
            "              \"successfulACKMessage\": null,\n" + 
            "              \"errorACKCode\": \"AE\",\n" + 
            "              \"errorACKMessage\": \"An Error Occurred Processing Message.\",\n" + 
            "              \"rejectedACKCode\": \"AR\",\n" + 
            "              \"rejectedACKMessage\": \"Message Rejected.\",\n" + 
            "              \"msh15ACKAccept\": false,\n" + 
            "              \"dateFormat\": \"yyyyMMddHHmmss.SSS\"\n" + 
            "            },\n" + 
            "            \"responseValidationProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"successfulACKCode\": \"AA,CA\",\n" + 
            "              \"errorACKCode\": \"AE,CE\",\n" + 
            "              \"rejectedACKCode\": \"AR,CR\",\n" + 
            "              \"validateMessageControlId\": true,\n" + 
            "              \"originalMessageControlId\": \"Destination_Encoded\",\n" + 
            "              \"originalIdMapVariable\": null\n" + 
            "            }\n" + 
            "          },\n" + 
            "          \"outboundProperties\": {\n" + 
            "            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\",\n" + 
            "            \"@version\": \"3.9.0\",\n" + 
            "            \"serializationProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"handleRepetitions\": true,\n" + 
            "              \"handleSubcomponents\": true,\n" + 
            "              \"useStrictParser\": false,\n" + 
            "              \"useStrictValidation\": false,\n" + 
            "              \"stripNamespaces\": true,\n" + 
            "              \"segmentDelimiter\": \"\\\\r\",\n" + 
            "              \"convertLineBreaks\": true\n" + 
            "            },\n" + 
            "            \"deserializationProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"useStrictParser\": false,\n" + 
            "              \"useStrictValidation\": false,\n" + 
            "              \"segmentDelimiter\": \"\\\\r\"\n" + 
            "            },\n" + 
            "            \"batchProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"splitType\": \"MSH_Segment\",\n" + 
            "              \"batchScript\": null\n" + 
            "            },\n" + 
            "            \"responseGenerationProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"segmentDelimiter\": \"\\\\r\",\n" + 
            "              \"successfulACKCode\": \"AA\",\n" + 
            "              \"successfulACKMessage\": null,\n" + 
            "              \"errorACKCode\": \"AE\",\n" + 
            "              \"errorACKMessage\": \"An Error Occurred Processing Message.\",\n" + 
            "              \"rejectedACKCode\": \"AR\",\n" + 
            "              \"rejectedACKMessage\": \"Message Rejected.\",\n" + 
            "              \"msh15ACKAccept\": false,\n" + 
            "              \"dateFormat\": \"yyyyMMddHHmmss.SSS\"\n" + 
            "            },\n" + 
            "            \"responseValidationProperties\": {\n" + 
            "              \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\",\n" + 
            "              \"@version\": \"3.9.0\",\n" + 
            "              \"successfulACKCode\": \"AA,CA\",\n" + 
            "              \"errorACKCode\": \"AE,CE\",\n" + 
            "              \"rejectedACKCode\": \"AR,CR\",\n" + 
            "              \"validateMessageControlId\": true,\n" + 
            "              \"originalMessageControlId\": \"Destination_Encoded\",\n" + 
            "              \"originalIdMapVariable\": null\n" + 
            "            }\n" + 
            "          }\n" + 
            "        },\n" + 
            "        \"filter\": {\n" + 
            "          \"@version\": \"3.9.0\",\n" + 
            "          \"elements\": null\n" + 
            "        },\n" + 
            "        \"transportName\": \"Channel Writer\",\n" + 
            "        \"mode\": \"DESTINATION\",\n" + 
            "        \"enabled\": true,\n" + 
            "        \"waitForPrevious\": true\n" + 
            "      }\n" + 
            "    },\n" + 
            "    \"preprocessingScript\": \"// Modify the message variable below to pre process data\\nreturn message;\",\n" + 
            "    \"postprocessingScript\": \"// This script executes once after a message has been processed\\n// Responses returned from here will be stored as \\\"Postprocessor\\\" in the response map\\nreturn;\",\n" + 
            "    \"deployScript\": \"// This script executes once when the channel is deployed\\n// You only have access to the globalMap and globalChannelMap here to persist data\\nreturn;\",\n" + 
            "    \"undeployScript\": \"// This script executes once when the channel is undeployed\\n// You only have access to the globalMap and globalChannelMap here to persist data\\nreturn;\",\n" + 
            "    \"properties\": {\n" + 
            "      \"@version\": \"3.9.0\",\n" + 
            "      \"clearGlobalChannelMap\": true,\n" + 
            "      \"messageStorageMode\": \"DEVELOPMENT\",\n" + 
            "      \"encryptData\": false,\n" + 
            "      \"removeContentOnCompletion\": false,\n" + 
            "      \"removeOnlyFilteredOnCompletion\": false,\n" + 
            "      \"removeAttachmentsOnCompletion\": false,\n" + 
            "      \"initialState\": \"STARTED\",\n" + 
            "      \"storeAttachments\": true,\n" + 
            "      \"metaDataColumns\": {\n" + 
            "        \"metaDataColumn\": [\n" + 
            "          {\n" + 
            "            \"name\": \"SOURCE\",\n" + 
            "            \"type\": \"STRING\",\n" + 
            "            \"mappingName\": \"mirth_source\"\n" + 
            "          },\n" + 
            "          {\n" + 
            "            \"name\": \"TYPE\",\n" + 
            "            \"type\": \"STRING\",\n" + 
            "            \"mappingName\": \"mirth_type\"\n" + 
            "          }\n" + 
            "        ]\n" + 
            "      },\n" + 
            "      \"attachmentProperties\": {\n" + 
            "        \"@version\": \"3.9.0\",\n" + 
            "        \"type\": \"None\",\n" + 
            "        \"properties\": null\n" + 
            "      },\n" + 
            "      \"resourceIds\": {\n" + 
            "        \"@class\": \"linked-hash-map\",\n" + 
            "        \"entry\": {\n" + 
            "          \"string\": [\n" + 
            "            \"Default Resource\",\n" + 
            "            \"[Default Resource]\"\n" + 
            "          ]\n" + 
            "        }\n" + 
            "      }\n" + 
            "    }\n" + 
            "  }\n" + 
            "}";
    // @formatter:on
    
    private static class TestJsonMessageBodyReader extends JsonMessageBodyReader {
        ObjectJSONSerializer serializer;
        
        public TestJsonMessageBodyReader() {
            this.serializer = spy(super.getObjectJsonSerializer());
        }
        
        @Override
        protected ObjectJSONSerializer getObjectJsonSerializer() {
            return serializer;
        }
        
    }
    
}
