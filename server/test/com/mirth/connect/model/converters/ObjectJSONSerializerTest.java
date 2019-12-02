package com.mirth.connect.model.converters;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
    
    private static final String GOOD_OLD_CHANNEL_ID = "e084f86d-7743-4df8-a6eb-91d1cdf2ab0f";
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
            "        \"id\": \"e084f86d-7743-4df8-a6eb-91d1cdf2ab0f\",\n" + 
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
    
    // @formatter:off
    private static final String GOOD_OLD_XML_CHANNEL = 
            "<channel version=\"3.4.2\">\n" + 
            "    <id>e084f86d-7743-4df8-a6eb-91d1cdf2ab0f</id>\n" + 
            "    <nextMetaDataId>2</nextMetaDataId>\n" + 
            "    <name>test</name>\n" + 
            "    <description></description>\n" + 
            "    <enabled>true</enabled>\n" + 
            "    <lastModified>\n" + 
            "        <time>1574899061916</time>\n" + 
            "        <timezone>America/Los_Angeles</timezone>\n" + 
            "    </lastModified>\n" + 
            "    <revision>1</revision>\n" + 
            "    <sourceConnector version=\"3.4.2\">\n" + 
            "        <metaDataId>0</metaDataId>\n" + 
            "        <name>sourceConnector</name>\n" + 
            "        <properties class=\"com.mirth.connect.connectors.vm.VmReceiverProperties\" version=\"3.4.2\">\n" + 
            "            <pluginProperties/>\n" + 
            "            <sourceConnectorProperties version=\"3.4.2\">\n" + 
            "                <responseVariable>None</responseVariable>\n" + 
            "                <respondAfterProcessing>true</respondAfterProcessing>\n" + 
            "                <processBatch>false</processBatch>\n" + 
            "                <firstResponse>false</firstResponse>\n" + 
            "                <processingThreads>1</processingThreads>\n" + 
            "                <resourceIds class=\"linked-hash-map\">\n" + 
            "                    <entry>\n" + 
            "                        <string>Default Resource</string>\n" + 
            "                        <string>[Default Resource]</string>\n" + 
            "                    </entry>\n" + 
            "                </resourceIds>\n" + 
            "                <queueBufferSize>1000</queueBufferSize>\n" + 
            "            </sourceConnectorProperties>\n" + 
            "        </properties>\n" + 
            "        <transformer version=\"3.4.2\">\n" + 
            "            <steps/>\n" + 
            "            <inboundDataType>HL7V2</inboundDataType>\n" + 
            "            <outboundDataType>HL7V2</outboundDataType>\n" + 
            "            <inboundProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\" version=\"3.4.2\">\n" + 
            "                <serializationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\" version=\"3.4.2\">\n" + 
            "                    <handleRepetitions>true</handleRepetitions>\n" + 
            "                    <handleSubcomponents>true</handleSubcomponents>\n" + 
            "                    <useStrictParser>false</useStrictParser>\n" + 
            "                    <useStrictValidation>false</useStrictValidation>\n" + 
            "                    <stripNamespaces>true</stripNamespaces>\n" + 
            "                    <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                    <convertLineBreaks>true</convertLineBreaks>\n" + 
            "                </serializationProperties>\n" + 
            "                <deserializationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\" version=\"3.4.2\">\n" + 
            "                    <useStrictParser>false</useStrictParser>\n" + 
            "                    <useStrictValidation>false</useStrictValidation>\n" + 
            "                    <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                </deserializationProperties>\n" + 
            "                <batchProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\" version=\"3.4.2\">\n" + 
            "                    <splitType>MSH_Segment</splitType>\n" + 
            "                    <batchScript></batchScript>\n" + 
            "                </batchProperties>\n" + 
            "                <responseGenerationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\" version=\"3.4.2\">\n" + 
            "                    <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                    <successfulACKCode>AA</successfulACKCode>\n" + 
            "                    <successfulACKMessage></successfulACKMessage>\n" + 
            "                    <errorACKCode>AE</errorACKCode>\n" + 
            "                    <errorACKMessage>An Error Occurred Processing Message.</errorACKMessage>\n" + 
            "                    <rejectedACKCode>AR</rejectedACKCode>\n" + 
            "                    <rejectedACKMessage>Message Rejected.</rejectedACKMessage>\n" + 
            "                    <msh15ACKAccept>false</msh15ACKAccept>\n" + 
            "                    <dateFormat>yyyyMMddHHmmss.SSS</dateFormat>\n" + 
            "                </responseGenerationProperties>\n" + 
            "                <responseValidationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\" version=\"3.4.2\">\n" + 
            "                    <successfulACKCode>AA,CA</successfulACKCode>\n" + 
            "                    <errorACKCode>AE,CE</errorACKCode>\n" + 
            "                    <rejectedACKCode>AR,CR</rejectedACKCode>\n" + 
            "                    <validateMessageControlId>true</validateMessageControlId>\n" + 
            "                    <originalMessageControlId>Destination_Encoded</originalMessageControlId>\n" + 
            "                    <originalIdMapVariable></originalIdMapVariable>\n" + 
            "                </responseValidationProperties>\n" + 
            "            </inboundProperties>\n" + 
            "            <outboundProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\" version=\"3.4.2\">\n" + 
            "                <serializationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\" version=\"3.4.2\">\n" + 
            "                    <handleRepetitions>true</handleRepetitions>\n" + 
            "                    <handleSubcomponents>true</handleSubcomponents>\n" + 
            "                    <useStrictParser>false</useStrictParser>\n" + 
            "                    <useStrictValidation>false</useStrictValidation>\n" + 
            "                    <stripNamespaces>true</stripNamespaces>\n" + 
            "                    <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                    <convertLineBreaks>true</convertLineBreaks>\n" + 
            "                </serializationProperties>\n" + 
            "                <deserializationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\" version=\"3.4.2\">\n" + 
            "                    <useStrictParser>false</useStrictParser>\n" + 
            "                    <useStrictValidation>false</useStrictValidation>\n" + 
            "                    <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                </deserializationProperties>\n" + 
            "                <batchProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\" version=\"3.4.2\">\n" + 
            "                    <splitType>MSH_Segment</splitType>\n" + 
            "                    <batchScript></batchScript>\n" + 
            "                </batchProperties>\n" + 
            "                <responseGenerationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\" version=\"3.4.2\">\n" + 
            "                    <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                    <successfulACKCode>AA</successfulACKCode>\n" + 
            "                    <successfulACKMessage></successfulACKMessage>\n" + 
            "                    <errorACKCode>AE</errorACKCode>\n" + 
            "                    <errorACKMessage>An Error Occurred Processing Message.</errorACKMessage>\n" + 
            "                    <rejectedACKCode>AR</rejectedACKCode>\n" + 
            "                    <rejectedACKMessage>Message Rejected.</rejectedACKMessage>\n" + 
            "                    <msh15ACKAccept>false</msh15ACKAccept>\n" + 
            "                    <dateFormat>yyyyMMddHHmmss.SSS</dateFormat>\n" + 
            "                </responseGenerationProperties>\n" + 
            "                <responseValidationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\" version=\"3.4.2\">\n" + 
            "                    <successfulACKCode>AA,CA</successfulACKCode>\n" + 
            "                    <errorACKCode>AE,CE</errorACKCode>\n" + 
            "                    <rejectedACKCode>AR,CR</rejectedACKCode>\n" + 
            "                    <validateMessageControlId>true</validateMessageControlId>\n" + 
            "                    <originalMessageControlId>Destination_Encoded</originalMessageControlId>\n" + 
            "                    <originalIdMapVariable></originalIdMapVariable>\n" + 
            "                </responseValidationProperties>\n" + 
            "            </outboundProperties>\n" + 
            "        </transformer>\n" + 
            "        <filter version=\"3.4.2\">\n" + 
            "            <rules/>\n" + 
            "        </filter>\n" + 
            "        <transportName>Channel Reader</transportName>\n" + 
            "        <mode>SOURCE</mode>\n" + 
            "        <enabled>true</enabled>\n" + 
            "        <waitForPrevious>true</waitForPrevious>\n" + 
            "    </sourceConnector>\n" + 
            "    <destinationConnectors>\n" + 
            "        <connector version=\"3.4.2\">\n" + 
            "            <metaDataId>1</metaDataId>\n" + 
            "            <name>Destination 1</name>\n" + 
            "            <properties class=\"com.mirth.connect.connectors.vm.VmDispatcherProperties\" version=\"3.4.2\">\n" + 
            "                <pluginProperties/>\n" + 
            "                <destinationConnectorProperties version=\"3.4.2\">\n" + 
            "                    <queueEnabled>false</queueEnabled>\n" + 
            "                    <sendFirst>false</sendFirst>\n" + 
            "                    <retryIntervalMillis>10000</retryIntervalMillis>\n" + 
            "                    <regenerateTemplate>false</regenerateTemplate>\n" + 
            "                    <retryCount>0</retryCount>\n" + 
            "                    <rotate>false</rotate>\n" + 
            "                    <includeFilterTransformer>false</includeFilterTransformer>\n" + 
            "                    <threadCount>1</threadCount>\n" + 
            "                    <threadAssignmentVariable></threadAssignmentVariable>\n" + 
            "                    <validateResponse>false</validateResponse>\n" + 
            "                    <resourceIds class=\"linked-hash-map\">\n" + 
            "                        <entry>\n" + 
            "                            <string>Default Resource</string>\n" + 
            "                            <string>[Default Resource]</string>\n" + 
            "                        </entry>\n" + 
            "                    </resourceIds>\n" + 
            "                    <queueBufferSize>1000</queueBufferSize>\n" + 
            "                </destinationConnectorProperties>\n" + 
            "                <channelId>none</channelId>\n" + 
            "                <channelTemplate>${message.encodedData}</channelTemplate>\n" + 
            "                <mapVariables/>\n" + 
            "            </properties>\n" + 
            "            <transformer version=\"3.4.2\">\n" + 
            "                <steps/>\n" + 
            "                <inboundDataType>HL7V2</inboundDataType>\n" + 
            "                <outboundDataType>HL7V2</outboundDataType>\n" + 
            "                <inboundProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\" version=\"3.4.2\">\n" + 
            "                    <serializationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\" version=\"3.4.2\">\n" + 
            "                        <handleRepetitions>true</handleRepetitions>\n" + 
            "                        <handleSubcomponents>true</handleSubcomponents>\n" + 
            "                        <useStrictParser>false</useStrictParser>\n" + 
            "                        <useStrictValidation>false</useStrictValidation>\n" + 
            "                        <stripNamespaces>true</stripNamespaces>\n" + 
            "                        <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                        <convertLineBreaks>true</convertLineBreaks>\n" + 
            "                    </serializationProperties>\n" + 
            "                    <deserializationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\" version=\"3.4.2\">\n" + 
            "                        <useStrictParser>false</useStrictParser>\n" + 
            "                        <useStrictValidation>false</useStrictValidation>\n" + 
            "                        <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                    </deserializationProperties>\n" + 
            "                    <batchProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\" version=\"3.4.2\">\n" + 
            "                        <splitType>MSH_Segment</splitType>\n" + 
            "                        <batchScript></batchScript>\n" + 
            "                    </batchProperties>\n" + 
            "                    <responseGenerationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\" version=\"3.4.2\">\n" + 
            "                        <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                        <successfulACKCode>AA</successfulACKCode>\n" + 
            "                        <successfulACKMessage></successfulACKMessage>\n" + 
            "                        <errorACKCode>AE</errorACKCode>\n" + 
            "                        <errorACKMessage>An Error Occurred Processing Message.</errorACKMessage>\n" + 
            "                        <rejectedACKCode>AR</rejectedACKCode>\n" + 
            "                        <rejectedACKMessage>Message Rejected.</rejectedACKMessage>\n" + 
            "                        <msh15ACKAccept>false</msh15ACKAccept>\n" + 
            "                        <dateFormat>yyyyMMddHHmmss.SSS</dateFormat>\n" + 
            "                    </responseGenerationProperties>\n" + 
            "                    <responseValidationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\" version=\"3.4.2\">\n" + 
            "                        <successfulACKCode>AA,CA</successfulACKCode>\n" + 
            "                        <errorACKCode>AE,CE</errorACKCode>\n" + 
            "                        <rejectedACKCode>AR,CR</rejectedACKCode>\n" + 
            "                        <validateMessageControlId>true</validateMessageControlId>\n" + 
            "                        <originalMessageControlId>Destination_Encoded</originalMessageControlId>\n" + 
            "                        <originalIdMapVariable></originalIdMapVariable>\n" + 
            "                    </responseValidationProperties>\n" + 
            "                </inboundProperties>\n" + 
            "                <outboundProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\" version=\"3.4.2\">\n" + 
            "                    <serializationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\" version=\"3.4.2\">\n" + 
            "                        <handleRepetitions>true</handleRepetitions>\n" + 
            "                        <handleSubcomponents>true</handleSubcomponents>\n" + 
            "                        <useStrictParser>false</useStrictParser>\n" + 
            "                        <useStrictValidation>false</useStrictValidation>\n" + 
            "                        <stripNamespaces>true</stripNamespaces>\n" + 
            "                        <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                        <convertLineBreaks>true</convertLineBreaks>\n" + 
            "                    </serializationProperties>\n" + 
            "                    <deserializationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\" version=\"3.4.2\">\n" + 
            "                        <useStrictParser>false</useStrictParser>\n" + 
            "                        <useStrictValidation>false</useStrictValidation>\n" + 
            "                        <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                    </deserializationProperties>\n" + 
            "                    <batchProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\" version=\"3.4.2\">\n" + 
            "                        <splitType>MSH_Segment</splitType>\n" + 
            "                        <batchScript></batchScript>\n" + 
            "                    </batchProperties>\n" + 
            "                    <responseGenerationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\" version=\"3.4.2\">\n" + 
            "                        <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                        <successfulACKCode>AA</successfulACKCode>\n" + 
            "                        <successfulACKMessage></successfulACKMessage>\n" + 
            "                        <errorACKCode>AE</errorACKCode>\n" + 
            "                        <errorACKMessage>An Error Occurred Processing Message.</errorACKMessage>\n" + 
            "                        <rejectedACKCode>AR</rejectedACKCode>\n" + 
            "                        <rejectedACKMessage>Message Rejected.</rejectedACKMessage>\n" + 
            "                        <msh15ACKAccept>false</msh15ACKAccept>\n" + 
            "                        <dateFormat>yyyyMMddHHmmss.SSS</dateFormat>\n" + 
            "                    </responseGenerationProperties>\n" + 
            "                    <responseValidationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\" version=\"3.4.2\">\n" + 
            "                        <successfulACKCode>AA,CA</successfulACKCode>\n" + 
            "                        <errorACKCode>AE,CE</errorACKCode>\n" + 
            "                        <rejectedACKCode>AR,CR</rejectedACKCode>\n" + 
            "                        <validateMessageControlId>true</validateMessageControlId>\n" + 
            "                        <originalMessageControlId>Destination_Encoded</originalMessageControlId>\n" + 
            "                        <originalIdMapVariable></originalIdMapVariable>\n" + 
            "                    </responseValidationProperties>\n" + 
            "                </outboundProperties>\n" + 
            "            </transformer>\n" + 
            "            <responseTransformer version=\"3.4.2\">\n" + 
            "                <steps/>\n" + 
            "                <inboundDataType>HL7V2</inboundDataType>\n" + 
            "                <outboundDataType>HL7V2</outboundDataType>\n" + 
            "                <inboundProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\" version=\"3.4.2\">\n" + 
            "                    <serializationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\" version=\"3.4.2\">\n" + 
            "                        <handleRepetitions>true</handleRepetitions>\n" + 
            "                        <handleSubcomponents>true</handleSubcomponents>\n" + 
            "                        <useStrictParser>false</useStrictParser>\n" + 
            "                        <useStrictValidation>false</useStrictValidation>\n" + 
            "                        <stripNamespaces>true</stripNamespaces>\n" + 
            "                        <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                        <convertLineBreaks>true</convertLineBreaks>\n" + 
            "                    </serializationProperties>\n" + 
            "                    <deserializationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\" version=\"3.4.2\">\n" + 
            "                        <useStrictParser>false</useStrictParser>\n" + 
            "                        <useStrictValidation>false</useStrictValidation>\n" + 
            "                        <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                    </deserializationProperties>\n" + 
            "                    <batchProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\" version=\"3.4.2\">\n" + 
            "                        <splitType>MSH_Segment</splitType>\n" + 
            "                        <batchScript></batchScript>\n" + 
            "                    </batchProperties>\n" + 
            "                    <responseGenerationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\" version=\"3.4.2\">\n" + 
            "                        <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                        <successfulACKCode>AA</successfulACKCode>\n" + 
            "                        <successfulACKMessage></successfulACKMessage>\n" + 
            "                        <errorACKCode>AE</errorACKCode>\n" + 
            "                        <errorACKMessage>An Error Occurred Processing Message.</errorACKMessage>\n" + 
            "                        <rejectedACKCode>AR</rejectedACKCode>\n" + 
            "                        <rejectedACKMessage>Message Rejected.</rejectedACKMessage>\n" + 
            "                        <msh15ACKAccept>false</msh15ACKAccept>\n" + 
            "                        <dateFormat>yyyyMMddHHmmss.SSS</dateFormat>\n" + 
            "                    </responseGenerationProperties>\n" + 
            "                    <responseValidationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\" version=\"3.4.2\">\n" + 
            "                        <successfulACKCode>AA,CA</successfulACKCode>\n" + 
            "                        <errorACKCode>AE,CE</errorACKCode>\n" + 
            "                        <rejectedACKCode>AR,CR</rejectedACKCode>\n" + 
            "                        <validateMessageControlId>true</validateMessageControlId>\n" + 
            "                        <originalMessageControlId>Destination_Encoded</originalMessageControlId>\n" + 
            "                        <originalIdMapVariable></originalIdMapVariable>\n" + 
            "                    </responseValidationProperties>\n" + 
            "                </inboundProperties>\n" + 
            "                <outboundProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties\" version=\"3.4.2\">\n" + 
            "                    <serializationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties\" version=\"3.4.2\">\n" + 
            "                        <handleRepetitions>true</handleRepetitions>\n" + 
            "                        <handleSubcomponents>true</handleSubcomponents>\n" + 
            "                        <useStrictParser>false</useStrictParser>\n" + 
            "                        <useStrictValidation>false</useStrictValidation>\n" + 
            "                        <stripNamespaces>true</stripNamespaces>\n" + 
            "                        <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                        <convertLineBreaks>true</convertLineBreaks>\n" + 
            "                    </serializationProperties>\n" + 
            "                    <deserializationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties\" version=\"3.4.2\">\n" + 
            "                        <useStrictParser>false</useStrictParser>\n" + 
            "                        <useStrictValidation>false</useStrictValidation>\n" + 
            "                        <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                    </deserializationProperties>\n" + 
            "                    <batchProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties\" version=\"3.4.2\">\n" + 
            "                        <splitType>MSH_Segment</splitType>\n" + 
            "                        <batchScript></batchScript>\n" + 
            "                    </batchProperties>\n" + 
            "                    <responseGenerationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseGenerationProperties\" version=\"3.4.2\">\n" + 
            "                        <segmentDelimiter>\\r</segmentDelimiter>\n" + 
            "                        <successfulACKCode>AA</successfulACKCode>\n" + 
            "                        <successfulACKMessage></successfulACKMessage>\n" + 
            "                        <errorACKCode>AE</errorACKCode>\n" + 
            "                        <errorACKMessage>An Error Occurred Processing Message.</errorACKMessage>\n" + 
            "                        <rejectedACKCode>AR</rejectedACKCode>\n" + 
            "                        <rejectedACKMessage>Message Rejected.</rejectedACKMessage>\n" + 
            "                        <msh15ACKAccept>false</msh15ACKAccept>\n" + 
            "                        <dateFormat>yyyyMMddHHmmss.SSS</dateFormat>\n" + 
            "                    </responseGenerationProperties>\n" + 
            "                    <responseValidationProperties class=\"com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties\" version=\"3.4.2\">\n" + 
            "                        <successfulACKCode>AA,CA</successfulACKCode>\n" + 
            "                        <errorACKCode>AE,CE</errorACKCode>\n" + 
            "                        <rejectedACKCode>AR,CR</rejectedACKCode>\n" + 
            "                        <validateMessageControlId>true</validateMessageControlId>\n" + 
            "                        <originalMessageControlId>Destination_Encoded</originalMessageControlId>\n" + 
            "                        <originalIdMapVariable></originalIdMapVariable>\n" + 
            "                    </responseValidationProperties>\n" + 
            "                </outboundProperties>\n" + 
            "            </responseTransformer>\n" + 
            "            <filter version=\"3.4.2\">\n" + 
            "                <rules/>\n" + 
            "            </filter>\n" + 
            "            <transportName>Channel Writer</transportName>\n" + 
            "            <mode>DESTINATION</mode>\n" + 
            "            <enabled>true</enabled>\n" + 
            "            <waitForPrevious>true</waitForPrevious>\n" + 
            "        </connector>\n" + 
            "    </destinationConnectors>\n" + 
            "    <preprocessingScript>\n" + 
            "    // Modify the message variable below to pre process data\n" + 
            "  return message;</preprocessingScript>\n" + 
            "    <postprocessingScript>\n" + 
            "    // This script executes once after a message has been processed\n" + 
            "    // Responses returned from here will be stored as &quot;Postprocessor&quot; in the response map\n" + 
            "  return;</postprocessingScript>\n" + 
            "    <deployScript>\n" + 
            "    // This script executes once when the channel is deployed\n" + 
            "    // You only have access to the globalMap and globalChannelMap here to persist data\n" + 
            "  return;</deployScript>\n" + 
            "    <undeployScript>\n" + 
            "    // This script executes once when the channel is undeployed\n" + 
            "    // You only have access to the globalMap and globalChannelMap here to persist data\n" + 
            "  return;</undeployScript>\n" + 
            "    <properties version=\"3.4.2\">\n" + 
            "        <clearGlobalChannelMap>true</clearGlobalChannelMap>\n" + 
            "        <messageStorageMode>DEVELOPMENT</messageStorageMode>\n" + 
            "        <encryptData>false</encryptData>\n" + 
            "        <removeContentOnCompletion>false</removeContentOnCompletion>\n" + 
            "        <removeOnlyFilteredOnCompletion>false</removeOnlyFilteredOnCompletion>\n" + 
            "        <removeAttachmentsOnCompletion>false</removeAttachmentsOnCompletion>\n" + 
            "        <initialState>STARTED</initialState>\n" + 
            "        <storeAttachments>false</storeAttachments>\n" + 
            "        <tags class=\"linked-hash-set\"/>\n" + 
            "        <metaDataColumns>\n" + 
            "            <metaDataColumn>\n" + 
            "                <name>SOURCE</name>\n" + 
            "                <type>STRING</type>\n" + 
            "                <mappingName>mirth_source</mappingName>\n" + 
            "            </metaDataColumn>\n" + 
            "            <metaDataColumn>\n" + 
            "                <name>TYPE</name>\n" + 
            "                <type>STRING</type>\n" + 
            "                <mappingName>mirth_type</mappingName>\n" + 
            "            </metaDataColumn>\n" + 
            "        </metaDataColumns>\n" + 
            "        <attachmentProperties version=\"3.4.2\">\n" + 
            "            <type>None</type>\n" + 
            "            <properties/>\n" + 
            "        </attachmentProperties>\n" + 
            "        <archiveEnabled>true</archiveEnabled>\n" + 
            "        <resourceIds class=\"linked-hash-map\">\n" + 
            "            <entry>\n" + 
            "                <string>Default Resource</string>\n" + 
            "                <string>[Default Resource]</string>\n" + 
            "            </entry>\n" + 
            "        </resourceIds>\n" + 
            "    </properties>\n" + 
            "    <codeTemplateLibraries/>\n" + 
            "</channel>";
    // @formatter:on
}
