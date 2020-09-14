package com.mirth.connect.model.converters;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import static org.junit.Assert.assertFalse;

import com.mirth.connect.client.core.Version;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.InvalidChannel;

public class ObjectJSONSerializerTest {

    @BeforeClass
    public static void SetupApi() throws Exception {
        try {
            ObjectXMLSerializer.getInstance().init(Version.getLatest().toString());
        } catch (Exception e) {
            // Ignore if it has already been initialized
        }
    }

    @Test
    public void testDeserializeOldChannel() {
        Channel channel = ObjectJSONSerializer.getInstance().deserialize(GOOD_OLD_CHANNEL, Channel.class);
        assertTrue(channel instanceof Channel);
        assertFalse(channel instanceof InvalidChannel);
        assertEquals(GOOD_OLD_CHANNEL_ID, channel.getId());
        assertTrue(channel.getExportData().getMetadata().getPruningSettings().isArchiveEnabled());
    }

    @Test
    public void testDeserializeInvalidChannel() {
        Channel channel = ObjectJSONSerializer.getInstance().deserialize(INVALID_CHANNEL, Channel.class);
        assertTrue(channel instanceof InvalidChannel);
        assertEquals(INVALID_CHANNEL_ID, channel.getId());
    }

    @Test
    public void testSerializeChannel() throws IOException {
        Channel channel = new Channel();
        channel.setId(GOOD_OLD_CHANNEL_ID);
        String channelId = channel.getId();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectJSONSerializer.getInstance().serialize(channel, baos);

        ByteArrayInputStream iaos = new ByteArrayInputStream(baos.toByteArray());
        JsonReader jsonReader = Json.createReader(iaos);
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        String id = jsonObject.getJsonObject("channel").getString("id");
        assertEquals(channelId, id);
    }

    private static final String GOOD_OLD_CHANNEL_ID = "e084f86d-7743-4df8-a6eb-91d1cdf2ab0f";
    // @formatter:off
    private static final String GOOD_OLD_CHANNEL = 
            "{\n" + 
            "    \"channel\": {\n" + 
            "        \"@version\": \"3.4.2\",\n" + 
            "        \"codeTemplateLibraries\": null,\n" + 
            "        \"deployScript\": \"\\n    // This script executes once when the channel is deployed\\n    // You only have access to the globalMap and globalChannelMap here to persist data\\n  return;\",\n" + 
            "        \"description\": null,\n" + 
            "        \"destinationConnectors\": {\n" + 
            "            \"connector\": {\n" + 
            "                \"@version\": \"3.4.2\",\n" + 
            "                \"enabled\": true,\n" + 
            "                \"filter\": {\n" + 
            "                    \"@version\": \"3.4.2\",\n" + 
            "                    \"rules\": null\n" + 
            "                },\n" + 
            "                \"metaDataId\": 1,\n" + 
            "                \"mode\": \"DESTINATION\",\n" + 
            "                \"name\": \"Destination 1\",\n" + 
            "                \"properties\": {\n" + 
            "                    \"@class\": \"com.mirth.connect.connectors.vm.VmDispatcherProperties\",\n" + 
            "                    \"@version\": \"3.4.2\",\n" + 
            "                    \"channelId\": \"none\",\n" + 
            "                    \"channelTemplate\": \"${message.encodedData}\",\n" + 
            "                    \"destinationConnectorProperties\": {\n" + 
            "                        \"@version\": \"3.4.2\",\n" + 
            "                        \"includeFilterTransformer\": false,\n" + 
            "                        \"queueBufferSize\": 1000,\n" + 
            "                        \"queueEnabled\": false,\n" + 
            "                        \"regenerateTemplate\": false,\n" + 
            "                        \"resourceIds\": {\n" + 
            "                            \"@class\": \"linked-hash-map\",\n" + 
            "                            \"entry\": {\n" + 
            "                                \"string\": [\n" + 
            "                                    \"Default Resource\",\n" + 
            "                                    \"[Default Resource]\"\n" + 
            "                                ]\n" + 
            "                            }\n" + 
            "                        },\n" + 
            "                        \"retryCount\": 0,\n" + 
            "                        \"retryIntervalMillis\": 10000,\n" + 
            "                        \"rotate\": false,\n" + 
            "                        \"sendFirst\": false,\n" + 
            "                        \"threadAssignmentVariable\": null,\n" + 
            "                        \"threadCount\": 1,\n" + 
            "                        \"validateResponse\": false\n" + 
            "                    },\n" + 
            "                    \"mapVariables\": null,\n" + 
            "                    \"pluginProperties\": null\n" + 
            "                },\n" + 
            "                \"responseTransformer\": {\n" + 
            "                    \"@version\": \"3.4.2\",\n" + 
            "                    \"inboundDataType\": \"HL7V2\",\n" + 
            "                    \"inboundProperties\": {\n" + 
            "                        \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\",\n" + 
            "                        \"@version\": \"3.4.2\",\n" + 
            "                        \"batchProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"batchScript\": null,\n" + 
            "                            \"splitType\": \"MSH_Segment\"\n" + 
            "                        },\n" + 
            "                        \"deserializationProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                            \"useStrictParser\": false,\n" + 
            "                            \"useStrictValidation\": false\n" + 
            "                        },\n" + 
            "                        \"responseGenerationProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"dateFormat\": \"yyyyMMddHHmmss.SSS\",\n" + 
            "                            \"errorACKCode\": \"AE\",\n" + 
            "                            \"errorACKMessage\": \"An Error Occurred Processing Message.\",\n" + 
            "                            \"msh15ACKAccept\": false,\n" + 
            "                            \"rejectedACKCode\": \"AR\",\n" + 
            "                            \"rejectedACKMessage\": \"Message Rejected.\",\n" + 
            "                            \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                            \"successfulACKCode\": \"AA\",\n" + 
            "                            \"successfulACKMessage\": null\n" + 
            "                        },\n" + 
            "                        \"responseValidationProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"errorACKCode\": \"AE,CE\",\n" + 
            "                            \"originalIdMapVariable\": null,\n" + 
            "                            \"originalMessageControlId\": \"Destination_Encoded\",\n" + 
            "                            \"rejectedACKCode\": \"AR,CR\",\n" + 
            "                            \"successfulACKCode\": \"AA,CA\",\n" + 
            "                            \"validateMessageControlId\": true\n" + 
            "                        },\n" + 
            "                        \"serializationProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"convertLineBreaks\": true,\n" + 
            "                            \"handleRepetitions\": true,\n" + 
            "                            \"handleSubcomponents\": true,\n" + 
            "                            \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                            \"stripNamespaces\": true,\n" + 
            "                            \"useStrictParser\": false,\n" + 
            "                            \"useStrictValidation\": false\n" + 
            "                        }\n" + 
            "                    },\n" + 
            "                    \"outboundDataType\": \"HL7V2\",\n" + 
            "                    \"outboundProperties\": {\n" + 
            "                        \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\",\n" + 
            "                        \"@version\": \"3.4.2\",\n" + 
            "                        \"batchProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"batchScript\": null,\n" + 
            "                            \"splitType\": \"MSH_Segment\"\n" + 
            "                        },\n" + 
            "                        \"deserializationProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                            \"useStrictParser\": false,\n" + 
            "                            \"useStrictValidation\": false\n" + 
            "                        },\n" + 
            "                        \"responseGenerationProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"dateFormat\": \"yyyyMMddHHmmss.SSS\",\n" + 
            "                            \"errorACKCode\": \"AE\",\n" + 
            "                            \"errorACKMessage\": \"An Error Occurred Processing Message.\",\n" + 
            "                            \"msh15ACKAccept\": false,\n" + 
            "                            \"rejectedACKCode\": \"AR\",\n" + 
            "                            \"rejectedACKMessage\": \"Message Rejected.\",\n" + 
            "                            \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                            \"successfulACKCode\": \"AA\",\n" + 
            "                            \"successfulACKMessage\": null\n" + 
            "                        },\n" + 
            "                        \"responseValidationProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"errorACKCode\": \"AE,CE\",\n" + 
            "                            \"originalIdMapVariable\": null,\n" + 
            "                            \"originalMessageControlId\": \"Destination_Encoded\",\n" + 
            "                            \"rejectedACKCode\": \"AR,CR\",\n" + 
            "                            \"successfulACKCode\": \"AA,CA\",\n" + 
            "                            \"validateMessageControlId\": true\n" + 
            "                        },\n" + 
            "                        \"serializationProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"convertLineBreaks\": true,\n" + 
            "                            \"handleRepetitions\": true,\n" + 
            "                            \"handleSubcomponents\": true,\n" + 
            "                            \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                            \"stripNamespaces\": true,\n" + 
            "                            \"useStrictParser\": false,\n" + 
            "                            \"useStrictValidation\": false\n" + 
            "                        }\n" + 
            "                    },\n" + 
            "                    \"steps\": null\n" + 
            "                },\n" + 
            "                \"transformer\": {\n" + 
            "                    \"@version\": \"3.4.2\",\n" + 
            "                    \"inboundDataType\": \"HL7V2\",\n" + 
            "                    \"inboundProperties\": {\n" + 
            "                        \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\",\n" + 
            "                        \"@version\": \"3.4.2\",\n" + 
            "                        \"batchProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"batchScript\": null,\n" + 
            "                            \"splitType\": \"MSH_Segment\"\n" + 
            "                        },\n" + 
            "                        \"deserializationProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                            \"useStrictParser\": false,\n" + 
            "                            \"useStrictValidation\": false\n" + 
            "                        },\n" + 
            "                        \"responseGenerationProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"dateFormat\": \"yyyyMMddHHmmss.SSS\",\n" + 
            "                            \"errorACKCode\": \"AE\",\n" + 
            "                            \"errorACKMessage\": \"An Error Occurred Processing Message.\",\n" + 
            "                            \"msh15ACKAccept\": false,\n" + 
            "                            \"rejectedACKCode\": \"AR\",\n" + 
            "                            \"rejectedACKMessage\": \"Message Rejected.\",\n" + 
            "                            \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                            \"successfulACKCode\": \"AA\",\n" + 
            "                            \"successfulACKMessage\": null\n" + 
            "                        },\n" + 
            "                        \"responseValidationProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"errorACKCode\": \"AE,CE\",\n" + 
            "                            \"originalIdMapVariable\": null,\n" + 
            "                            \"originalMessageControlId\": \"Destination_Encoded\",\n" + 
            "                            \"rejectedACKCode\": \"AR,CR\",\n" + 
            "                            \"successfulACKCode\": \"AA,CA\",\n" + 
            "                            \"validateMessageControlId\": true\n" + 
            "                        },\n" + 
            "                        \"serializationProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"convertLineBreaks\": true,\n" + 
            "                            \"handleRepetitions\": true,\n" + 
            "                            \"handleSubcomponents\": true,\n" + 
            "                            \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                            \"stripNamespaces\": true,\n" + 
            "                            \"useStrictParser\": false,\n" + 
            "                            \"useStrictValidation\": false\n" + 
            "                        }\n" + 
            "                    },\n" + 
            "                    \"outboundDataType\": \"HL7V2\",\n" + 
            "                    \"outboundProperties\": {\n" + 
            "                        \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\",\n" + 
            "                        \"@version\": \"3.4.2\",\n" + 
            "                        \"batchProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"batchScript\": null,\n" + 
            "                            \"splitType\": \"MSH_Segment\"\n" + 
            "                        },\n" + 
            "                        \"deserializationProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                            \"useStrictParser\": false,\n" + 
            "                            \"useStrictValidation\": false\n" + 
            "                        },\n" + 
            "                        \"responseGenerationProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"dateFormat\": \"yyyyMMddHHmmss.SSS\",\n" + 
            "                            \"errorACKCode\": \"AE\",\n" + 
            "                            \"errorACKMessage\": \"An Error Occurred Processing Message.\",\n" + 
            "                            \"msh15ACKAccept\": false,\n" + 
            "                            \"rejectedACKCode\": \"AR\",\n" + 
            "                            \"rejectedACKMessage\": \"Message Rejected.\",\n" + 
            "                            \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                            \"successfulACKCode\": \"AA\",\n" + 
            "                            \"successfulACKMessage\": null\n" + 
            "                        },\n" + 
            "                        \"responseValidationProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"errorACKCode\": \"AE,CE\",\n" + 
            "                            \"originalIdMapVariable\": null,\n" + 
            "                            \"originalMessageControlId\": \"Destination_Encoded\",\n" + 
            "                            \"rejectedACKCode\": \"AR,CR\",\n" + 
            "                            \"successfulACKCode\": \"AA,CA\",\n" + 
            "                            \"validateMessageControlId\": true\n" + 
            "                        },\n" + 
            "                        \"serializationProperties\": {\n" + 
            "                            \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\",\n" + 
            "                            \"@version\": \"3.4.2\",\n" + 
            "                            \"convertLineBreaks\": true,\n" + 
            "                            \"handleRepetitions\": true,\n" + 
            "                            \"handleSubcomponents\": true,\n" + 
            "                            \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                            \"stripNamespaces\": true,\n" + 
            "                            \"useStrictParser\": false,\n" + 
            "                            \"useStrictValidation\": false\n" + 
            "                        }\n" + 
            "                    },\n" + 
            "                    \"steps\": null\n" + 
            "                },\n" + 
            "                \"transportName\": \"Channel Writer\",\n" + 
            "                \"waitForPrevious\": true\n" + 
            "            }\n" + 
            "        },\n" + 
            "        \"enabled\": true,\n" +  
			"        \"id\": \"" + GOOD_OLD_CHANNEL_ID + "\",\n" +
            "        \"lastModified\": {\n" + 
            "            \"time\": 1574899061916,\n" + 
            "            \"timezone\": \"America/Los_Angeles\"\n" + 
            "        },\n" + 
            "        \"name\": \"test\",\n" + 
            "        \"nextMetaDataId\": 2,\n" + 
            "        \"postprocessingScript\": \"\\n    // This script executes once after a message has been processed\\n    // Responses returned from here will be stored as \\\"Postprocessor\\\" in the response map\\n  return;\",\n" + 
            "        \"preprocessingScript\": \"\\n    // Modify the message variable below to pre process data\\n  return message;\",\n" + 
            "        \"properties\": {\n" + 
            "            \"@version\": \"3.4.2\",\n" + 
            "            \"archiveEnabled\": true,\n" + 
            "            \"attachmentProperties\": {\n" + 
            "                \"@version\": \"3.4.2\",\n" + 
            "                \"properties\": null,\n" + 
            "                \"type\": \"None\"\n" + 
            "            },\n" + 
            "            \"clearGlobalChannelMap\": true,\n" + 
            "            \"encryptData\": false,\n" + 
            "            \"initialState\": \"STARTED\",\n" + 
            "            \"messageStorageMode\": \"DEVELOPMENT\",\n" + 
            "            \"metaDataColumns\": {\n" + 
            "                \"metaDataColumn\": [\n" + 
            "                    {\n" + 
            "                        \"mappingName\": \"mirth_source\",\n" + 
            "                        \"name\": \"SOURCE\",\n" + 
            "                        \"type\": \"STRING\"\n" + 
            "                    },\n" + 
            "                    {\n" + 
            "                        \"mappingName\": \"mirth_type\",\n" + 
            "                        \"name\": \"TYPE\",\n" + 
            "                        \"type\": \"STRING\"\n" + 
            "                    }\n" + 
            "                ]\n" + 
            "            },\n" + 
            "            \"removeAttachmentsOnCompletion\": false,\n" + 
            "            \"removeContentOnCompletion\": false,\n" + 
            "            \"removeOnlyFilteredOnCompletion\": false,\n" + 
            "            \"resourceIds\": {\n" + 
            "                \"@class\": \"linked-hash-map\",\n" + 
            "                \"entry\": {\n" + 
            "                    \"string\": [\n" + 
            "                        \"Default Resource\",\n" + 
            "                        \"[Default Resource]\"\n" + 
            "                    ]\n" + 
            "                }\n" + 
            "            },\n" + 
            "            \"storeAttachments\": false,\n" + 
            "            \"tags\": {\n" + 
            "                \"@class\": \"linked-hash-set\"\n" + 
            "            }\n" + 
            "        },\n" + 
            "        \"revision\": 1,\n" + 
            "        \"sourceConnector\": {\n" + 
            "            \"@version\": \"3.4.2\",\n" + 
            "            \"enabled\": true,\n" + 
            "            \"filter\": {\n" + 
            "                \"@version\": \"3.4.2\",\n" + 
            "                \"rules\": null\n" + 
            "            },\n" + 
            "            \"metaDataId\": 0,\n" + 
            "            \"mode\": \"SOURCE\",\n" + 
            "            \"name\": \"sourceConnector\",\n" + 
            "            \"properties\": {\n" + 
            "                \"@class\": \"com.mirth.connect.connectors.vm.VmReceiverProperties\",\n" + 
            "                \"@version\": \"3.4.2\",\n" + 
            "                \"pluginProperties\": null,\n" + 
            "                \"sourceConnectorProperties\": {\n" + 
            "                    \"@version\": \"3.4.2\",\n" + 
            "                    \"firstResponse\": false,\n" + 
            "                    \"processBatch\": false,\n" + 
            "                    \"processingThreads\": 1,\n" + 
            "                    \"queueBufferSize\": 1000,\n" + 
            "                    \"resourceIds\": {\n" + 
            "                        \"@class\": \"linked-hash-map\",\n" + 
            "                        \"entry\": {\n" + 
            "                            \"string\": [\n" + 
            "                                \"Default Resource\",\n" + 
            "                                \"[Default Resource]\"\n" + 
            "                            ]\n" + 
            "                        }\n" + 
            "                    },\n" + 
            "                    \"respondAfterProcessing\": true,\n" + 
            "                    \"responseVariable\": \"None\"\n" + 
            "                }\n" + 
            "            },\n" + 
            "            \"transformer\": {\n" + 
            "                \"@version\": \"3.4.2\",\n" + 
            "                \"inboundDataType\": \"HL7V2\",\n" + 
            "                \"inboundProperties\": {\n" + 
            "                    \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\",\n" + 
            "                    \"@version\": \"3.4.2\",\n" + 
            "                    \"batchProperties\": {\n" + 
            "                        \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\",\n" + 
            "                        \"@version\": \"3.4.2\",\n" + 
            "                        \"batchScript\": null,\n" + 
            "                        \"splitType\": \"MSH_Segment\"\n" + 
            "                    },\n" + 
            "                    \"deserializationProperties\": {\n" + 
            "                        \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\",\n" + 
            "                        \"@version\": \"3.4.2\",\n" + 
            "                        \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                        \"useStrictParser\": false,\n" + 
            "                        \"useStrictValidation\": false\n" + 
            "                    },\n" + 
            "                    \"responseGenerationProperties\": {\n" + 
            "                        \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\",\n" + 
            "                        \"@version\": \"3.4.2\",\n" + 
            "                        \"dateFormat\": \"yyyyMMddHHmmss.SSS\",\n" + 
            "                        \"errorACKCode\": \"AE\",\n" + 
            "                        \"errorACKMessage\": \"An Error Occurred Processing Message.\",\n" + 
            "                        \"msh15ACKAccept\": false,\n" + 
            "                        \"rejectedACKCode\": \"AR\",\n" + 
            "                        \"rejectedACKMessage\": \"Message Rejected.\",\n" + 
            "                        \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                        \"successfulACKCode\": \"AA\",\n" + 
            "                        \"successfulACKMessage\": null\n" + 
            "                    },\n" + 
            "                    \"responseValidationProperties\": {\n" + 
            "                        \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\",\n" + 
            "                        \"@version\": \"3.4.2\",\n" + 
            "                        \"errorACKCode\": \"AE,CE\",\n" + 
            "                        \"originalIdMapVariable\": null,\n" + 
            "                        \"originalMessageControlId\": \"Destination_Encoded\",\n" + 
            "                        \"rejectedACKCode\": \"AR,CR\",\n" + 
            "                        \"successfulACKCode\": \"AA,CA\",\n" + 
            "                        \"validateMessageControlId\": true\n" + 
            "                    },\n" + 
            "                    \"serializationProperties\": {\n" + 
            "                        \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\",\n" + 
            "                        \"@version\": \"3.4.2\",\n" + 
            "                        \"convertLineBreaks\": true,\n" + 
            "                        \"handleRepetitions\": true,\n" + 
            "                        \"handleSubcomponents\": true,\n" + 
            "                        \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                        \"stripNamespaces\": true,\n" + 
            "                        \"useStrictParser\": false,\n" + 
            "                        \"useStrictValidation\": false\n" + 
            "                    }\n" + 
            "                },\n" + 
            "                \"outboundDataType\": \"HL7V2\",\n" + 
            "                \"outboundProperties\": {\n" + 
            "                    \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\",\n" + 
            "                    \"@version\": \"3.4.2\",\n" + 
            "                    \"batchProperties\": {\n" + 
            "                        \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\",\n" + 
            "                        \"@version\": \"3.4.2\",\n" + 
            "                        \"batchScript\": null,\n" + 
            "                        \"splitType\": \"MSH_Segment\"\n" + 
            "                    },\n" + 
            "                    \"deserializationProperties\": {\n" + 
            "                        \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\",\n" + 
            "                        \"@version\": \"3.4.2\",\n" + 
            "                        \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                        \"useStrictParser\": false,\n" + 
            "                        \"useStrictValidation\": false\n" + 
            "                    },\n" + 
            "                    \"responseGenerationProperties\": {\n" + 
            "                        \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\",\n" + 
            "                        \"@version\": \"3.4.2\",\n" + 
            "                        \"dateFormat\": \"yyyyMMddHHmmss.SSS\",\n" + 
            "                        \"errorACKCode\": \"AE\",\n" + 
            "                        \"errorACKMessage\": \"An Error Occurred Processing Message.\",\n" + 
            "                        \"msh15ACKAccept\": false,\n" + 
            "                        \"rejectedACKCode\": \"AR\",\n" + 
            "                        \"rejectedACKMessage\": \"Message Rejected.\",\n" + 
            "                        \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                        \"successfulACKCode\": \"AA\",\n" + 
            "                        \"successfulACKMessage\": null\n" + 
            "                    },\n" + 
            "                    \"responseValidationProperties\": {\n" + 
            "                        \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\",\n" + 
            "                        \"@version\": \"3.4.2\",\n" + 
            "                        \"errorACKCode\": \"AE,CE\",\n" + 
            "                        \"originalIdMapVariable\": null,\n" + 
            "                        \"originalMessageControlId\": \"Destination_Encoded\",\n" + 
            "                        \"rejectedACKCode\": \"AR,CR\",\n" + 
            "                        \"successfulACKCode\": \"AA,CA\",\n" + 
            "                        \"validateMessageControlId\": true\n" + 
            "                    },\n" + 
            "                    \"serializationProperties\": {\n" + 
            "                        \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\",\n" + 
            "                        \"@version\": \"3.4.2\",\n" + 
            "                        \"convertLineBreaks\": true,\n" + 
            "                        \"handleRepetitions\": true,\n" + 
            "                        \"handleSubcomponents\": true,\n" + 
            "                        \"segmentDelimiter\": \"\\\\r\",\n" + 
            "                        \"stripNamespaces\": true,\n" + 
            "                        \"useStrictParser\": false,\n" + 
            "                        \"useStrictValidation\": false\n" + 
            "                    }\n" + 
            "                },\n" + 
            "                \"steps\": null\n" + 
            "            },\n" + 
            "            \"transportName\": \"Channel Reader\",\n" + 
            "            \"waitForPrevious\": true\n" + 
            "        },\n" + 
            "        \"undeployScript\": \"\\n    // This script executes once when the channel is undeployed\\n    // You only have access to the globalMap and globalChannelMap here to persist data\\n  return;\"\n" + 
            "    }\n" + 
            "}";
    // @formatter:on

    private static final String INVALID_CHANNEL_ID = "b96f0f57-cd27-430d-9059-484211660fb7";
    // @formatter:off
    private static final String INVALID_CHANNEL = 
    		"{\n" + 
    		"  \"channel\": {\n" + 
    		"    \"@version\": \"3.9.0\",\n" + 
    		"    \"id\": \"" + INVALID_CHANNEL_ID + "\",\n" + 
    		"    \"nextMetaDataId\": 4,\n" + 
    		"    \"name\": \"test3\",\n" + 
    		"    \"description\": null,\n" + 
    		"    \"revision\": 1,\n" + 
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
    		"          \"@class\": \"com.mirth.connect.plugins.datatypes.hl7v2.InvalidDataTypePropertiesClass\",\n" + 
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
}
