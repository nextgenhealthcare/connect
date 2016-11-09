/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.reference;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.components.rsta.ac.MirthCompletionCacheInterface;
import com.mirth.connect.client.ui.components.rsta.ac.MirthLanguageSupport;
import com.mirth.connect.client.ui.reference.Reference.Type;
import com.mirth.connect.model.Parameters;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateContextSet;
import com.mirth.connect.model.codetemplates.CodeTemplateFunctionDefinition;
import com.mirth.connect.model.codetemplates.CodeTemplateProperties.CodeTemplateType;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.model.util.DefaultMetaData;
import com.mirth.connect.plugins.CodeTemplatePlugin;

public class ReferenceListFactory {

    private static final CodeTemplateContextSet CONTEXT_GLOBAL = CodeTemplateContextSet.getGlobalContextSet();
    private static final CodeTemplateContextSet CONTEXT_CHANNEL = CodeTemplateContextSet.getChannelContextSet();
    private static final CodeTemplateContextSet CONTEXT_CONNECTOR = CodeTemplateContextSet.getConnectorContextSet();
    private static final CodeTemplateContextSet CONTEXT_FILTER_TRANSFORMER = new CodeTemplateContextSet(ContextType.SOURCE_FILTER_TRANSFORMER, ContextType.DESTINATION_FILTER_TRANSFORMER);
    private static final CodeTemplateContextSet CONTEXT_RESPONSE_TRANSFORMER = new CodeTemplateContextSet(ContextType.DESTINATION_RESPONSE_TRANSFORMER);
    private static final CodeTemplateContextSet CONTEXT_ATTACHMENT = CodeTemplateContextSet.getConnectorContextSet().addContext(ContextType.GLOBAL_PREPROCESSOR, ContextType.GLOBAL_POSTPROCESSOR, ContextType.CHANNEL_PREPROCESSOR, ContextType.CHANNEL_POSTPROCESSOR, ContextType.CHANNEL_ATTACHMENT);
    private static final CodeTemplateContextSet CONTEXT_BATCH = new CodeTemplateContextSet(ContextType.CHANNEL_BATCH);
    private static final CodeTemplateContextSet CONTEXT_POSTPROCESSOR = new CodeTemplateContextSet(ContextType.GLOBAL_POSTPROCESSOR, ContextType.CHANNEL_POSTPROCESSOR);
    private static final CodeTemplateContextSet CONTEXT_RESPONSE_MAP = new CodeTemplateContextSet(ContextType.GLOBAL_POSTPROCESSOR, ContextType.CHANNEL_POSTPROCESSOR, ContextType.DESTINATION_DISPATCHER, ContextType.DESTINATION_RESPONSE_TRANSFORMER);

    private static ReferenceListFactory instance = null;

    private Logger logger = Logger.getLogger(getClass());
    private Map<Type, List<Reference>> cache = new HashMap<Type, List<Reference>>();
    private Map<Type, List<Reference>> userCache = new HashMap<Type, List<Reference>>();
    private Map<String, List<CodeTemplate>> codeTemplateMap = new TreeMap<String, List<CodeTemplate>>(new CategoryComparator());
    private Map<String, List<String>> aliasMap = new HashMap<String, List<String>>();
    private boolean pluginReferencesLoaded;
    private boolean afterPluginReferencesLoaded;

    private ReferenceListFactory() {
        initialize();
    }

    public static ReferenceListFactory getInstance() {
        synchronized (ReferenceListFactory.class) {
            if (instance == null) {
                instance = new ReferenceListFactory();
            }
            return instance;
        }
    }

    public synchronized Map<Type, List<Reference>> getReferences() {
        Map<Type, List<Reference>> cache = new HashMap<Type, List<Reference>>();
        for (Entry<Type, List<Reference>> entry : this.cache.entrySet()) {
            cache.put(entry.getKey(), new ArrayList<Reference>(entry.getValue()));
        }
        return cache;
    }

    public synchronized Map<String, List<CodeTemplate>> getCodeTemplateMap() {
        Map<String, List<CodeTemplate>> codeTemplateMap = new TreeMap<String, List<CodeTemplate>>(new CategoryComparator());
        for (Entry<String, List<CodeTemplate>> entry : this.codeTemplateMap.entrySet()) {
            codeTemplateMap.put(entry.getKey(), new ArrayList<CodeTemplate>(entry.getValue()));
        }
        return codeTemplateMap;
    }

    public synchronized List<CodeTemplate> getCodeTemplates(String category, ContextType contextType) {
        List<CodeTemplate> codeTemplates = new ArrayList<CodeTemplate>();

        for (Entry<String, List<CodeTemplate>> entry : codeTemplateMap.entrySet()) {
            if (category == null || entry.getKey().equals(category)) {
                for (CodeTemplate codeTemplate : entry.getValue()) {
                    if (codeTemplate.getContextSet().contains(contextType)) {
                        codeTemplates.add(codeTemplate);
                    }
                }
            }
        }

        return codeTemplates;
    }

    public synchronized void updateUserCodeTemplates() {
        MirthLanguageSupport languageSupport = (MirthLanguageSupport) LanguageSupportFactory.get().getSupportFor(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        MirthCompletionCacheInterface completionCache = languageSupport.getCompletionCache();

        // Remove old references from user cache
        for (List<Reference> references : userCache.values()) {
            completionCache.removeReferences(references);
        }
        userCache.clear();

        List<CodeTemplate> functionList = new ArrayList<CodeTemplate>();
        List<CodeTemplate> codeList = new ArrayList<CodeTemplate>();
        List<Reference> references = new ArrayList<Reference>();

        for (CodeTemplate template : PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplates().values()) {
            Category category = null;

            if (template.getType() == CodeTemplateType.FUNCTION) {
                category = Category.USER_FUNCTIONS;
                functionList.add(template);
            } else if (template.getType() == CodeTemplateType.DRAG_AND_DROP_CODE) {
                category = Category.USER_CODE;
                codeList.add(template);
            }

            if (category != null) {
                Reference reference = convertCodeTemplateToReference(category.toString(), template);
                if (reference != null) {
                    references.add(reference);
                    addUserReference(reference);
                }
            }
        }

        codeTemplateMap.put(Category.USER_FUNCTIONS.toString(), functionList);
        codeTemplateMap.put(Category.USER_CODE.toString(), codeList);
        completionCache.addReferences(references);
    }

    public synchronized void loadPluginReferences() {
        if (pluginReferencesLoaded) {
            return;
        }
        pluginReferencesLoaded = true;

        MirthLanguageSupport languageSupport = (MirthLanguageSupport) LanguageSupportFactory.get().getSupportFor(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        MirthCompletionCacheInterface completionCache = languageSupport.getCompletionCache();

        // Iterate through each loaded code template plugin
        for (Entry<String, CodeTemplatePlugin> codeTemplatePluginEntry : LoadedExtensions.getInstance().getCodeTemplatePlugins().entrySet()) {
            for (Entry<String, List<CodeTemplate>> entry : codeTemplatePluginEntry.getValue().getReferenceItems().entrySet()) {
                String category = entry.getKey();

                if (category != null) {
                    // Add the code template to the map
                    List<CodeTemplate> codeTemplates = codeTemplateMap.get(category);
                    if (codeTemplates == null) {
                        codeTemplates = new ArrayList<CodeTemplate>();
                        codeTemplateMap.put(category, codeTemplates);
                    }
                    codeTemplates.addAll(entry.getValue());

                    // Add each template as a reference as well
                    List<Reference> references = new ArrayList<Reference>();
                    for (CodeTemplate template : entry.getValue()) {
                        Reference reference = convertCodeTemplateToReference(category, template);

                        if (reference != null) {
                            Type type = reference.getType();
                            List<Reference> list = cache.get(type);
                            if (list == null) {
                                list = new ArrayList<Reference>();
                                cache.put(type, list);
                            }
                            list.add(reference);
                            references.add(reference);
                        }
                    }

                    completionCache.addReferences(references);
                }
            }
        }
    }

    public synchronized void loadReferencesAfterPlugins() {
        if (afterPluginReferencesLoaded) {
            return;
        }
        afterPluginReferencesLoaded = true;

        MirthLanguageSupport languageSupport = (MirthLanguageSupport) LanguageSupportFactory.get().getSupportFor(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        MirthCompletionCacheInterface completionCache = languageSupport.getCompletionCache();
        List<Reference> references = new ArrayList<Reference>();
        Reference reference;

        // Conversion reference; this needs the data type plugins to already have been loaded
        reference = new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.CONVERSION.toString(), "Get Serializer", "Creates and returns a data type serializer with the specified serialization and deserialization properties. " + getDataTypesToolTipText(), "var ${dataType} = '${HL7V2}';\nvar ${serializationProperties} = SerializerFactory.getDefaultSerializationProperties(${dataType});\nvar ${deserializationProperties} = SerializerFactory.getDefaultDeserializationProperties(${dataType});\nvar ${serializer} = SerializerFactory.getSerializer(${dataType}, ${serializationProperties}, ${deserializationProperties});");
        references.add(reference);
        addReference(reference, true);

        completionCache.addReferences(references);
    }

    private void addReferences(Collection<Reference> references) {
        if (CollectionUtils.isNotEmpty(references)) {
            for (Reference reference : references) {
                addReference(reference);
            }
        }
    }

    private void addReference(Reference reference) {
        addReference(reference, false);
    }

    private void addReference(Reference reference, boolean start) {
        Type type = reference.getType();

        List<Reference> list = cache.get(type);
        if (list == null) {
            list = new ArrayList<Reference>();
            cache.put(type, list);
        }

        if (start) {
            list.add(0, reference);
        } else {
            list.add(reference);
        }

        // If a category is specified, add the reference as a code template as well
        if (reference.getCategory() != null) {
            List<CodeTemplate> codeTemplates = codeTemplateMap.get(reference.getCategory());
            if (codeTemplates == null) {
                codeTemplates = new ArrayList<CodeTemplate>();
                codeTemplateMap.put(reference.getCategory().toString(), codeTemplates);
            }

            if (start) {
                codeTemplates.add(0, reference.toCodeTemplate());
            } else {
                codeTemplates.add(reference.toCodeTemplate());
            }
        }
    }

    private void addUserReference(Reference reference) {
        Type type = reference.getType();

        List<Reference> list = userCache.get(type);
        if (list == null) {
            list = new ArrayList<Reference>();
            userCache.put(type, list);
        }
        list.add(reference);
    }

    private void initialize() {
        addCodeTemplateReferences();
        addMiscellaneousReferences();
        addUserutilReferences();
        addE4XReferences();
    }

    private Reference convertCodeTemplateToReference(String category, CodeTemplate template) {
        if (template.getType() == CodeTemplateType.FUNCTION) {
            return new FunctionReference(category, template);
        } else {
            return new CodeReference(category, template);
        }
    }

    private void addCodeTemplateReferences() {
        // Logging and alerts references
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.LOGGING_AND_ALERTS.toString(), "Log an Info Statement", "Outputs the message to the system info log.", "logger.info('${message}');"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.LOGGING_AND_ALERTS.toString(), "Log an Error Statement", "Outputs the message to the system error log.", "logger.error('${message}');"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.LOGGING_AND_ALERTS.toString(), "Send an Email", "Sends an alert email using the alert SMTP properties.", "var ${smtpConn} = SMTPConnectionFactory.createSMTPConnection();\n${smtpConn}.send('${to}', '${cc}', '${from}', '${subject}', '${body}', '${charset}');"));
        addReference(new ParameterizedCodeReference(CONTEXT_CHANNEL, Category.LOGGING_AND_ALERTS.toString(), "Trigger an Alert", "Trigger a custom alert for the current channel.", "alerts.sendAlert('${message}');"));

        // Database references
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.DATABASE.toString(), "Perform Database Query", "Performs a database query and returns the rowset.", "var ${dbConn};\nvar ${result};\n\ntry {\n\t${dbConn} = DatabaseConnectionFactory.createDatabaseConnection('${driver}', '${address}', '${username}', '${password}');\n\t${result} = ${dbConn}.executeCachedQuery('${expression}');\n} finally {\n\tif (${dbConn}) {\n\t\t${dbConn}.close();\n\t}\n}"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.DATABASE.toString(), "Perform Parameterized Database Query", "Performs a database query with a (Java) list of parameters.", "var ${dbConn};\nvar ${result};\n\ntry {\n\t${dbConn} = DatabaseConnectionFactory.createDatabaseConnection('${driver}', '${address}', '${username}', '${password}');\n\t${result} = ${dbConn}.executeCachedQuery('${expression}', ${paramList});\n} finally {\n\tif (${dbConn}) {\n\t\t${dbConn}.close();\n\t}\n}"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.DATABASE.toString(), "Perform Database Update", "Performs a database update.", "var ${dbConn};\nvar ${result};\n\ntry {\n\t${dbConn} = DatabaseConnectionFactory.createDatabaseConnection('${driver}', '${address}', '${username}', '${password}');\n\t${result} = ${dbConn}.executeUpdate('${expression}');\n} finally {\n\tif (${dbConn}) {\n\t\t${dbConn}.close();\n\t}\n}"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.DATABASE.toString(), "Perform Parameterized Database Update", "Performs a database update with a (Java) list of parameters.", "var ${dbConn};\nvar ${result};\n\ntry {\n\t${dbConn} = DatabaseConnectionFactory.createDatabaseConnection('${driver}', '${address}', '${username}', '${password}');\n\t${result} = ${dbConn}.executeUpdate('${expression}', ${paramList});\n} finally {\n\tif (${dbConn}) {\n\t\t${dbConn}.close();\n\t}\n}"));
        addReference(new VariableReference(CONTEXT_GLOBAL, Category.DATABASE.toString(), "Postgres Connection Template", "String template for Postgres database connection.", "\"jdbc:postgresql://host:port/dbname\""));
        addReference(new VariableReference(CONTEXT_GLOBAL, Category.DATABASE.toString(), "MySQL Connection Template", "String template for MySQL database connection.", "\"jdbc:mysql://host:port/dbname\""));
        addReference(new VariableReference(CONTEXT_GLOBAL, Category.DATABASE.toString(), "SQL Server Connection Template", "String template for SQL Server database connection.", "\"jdbc:jtds:sqlserver://host:port/dbname\""));
        addReference(new VariableReference(CONTEXT_GLOBAL, Category.DATABASE.toString(), "Oracle Connection Template", "String template for Oracle database connection.", "\"jdbc:oracle:thin:@host:port:dbname\""));
        addReference(new VariableReference(CONTEXT_GLOBAL, Category.DATABASE.toString(), "Postgres Driver", "String used for Postgres database driver.", "\"org.postgresql.Driver\""));
        addReference(new VariableReference(CONTEXT_GLOBAL, Category.DATABASE.toString(), "MySQL Driver", "String used for MySQL database driver.", "\"com.mysql.jdbc.Driver\""));
        addReference(new VariableReference(CONTEXT_GLOBAL, Category.DATABASE.toString(), "SQL Server Driver", "String used for SQL Server database driver.", "\"net.sourceforge.jtds.jdbc.Driver\""));
        addReference(new VariableReference(CONTEXT_GLOBAL, Category.DATABASE.toString(), "Oracle Driver", "String used for Oracle database driver.", "\"oracle.jdbc.OracleDriver\""));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.DATABASE.toString(), "Initialize Driver", "Initialize the specified JDBC driver. (Same as calling Class.forName)", "DatabaseConnectionFactory.initializeDriver('${driver}');"));

        // Message references
        addReference(new CodeReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), "Incoming Message (Raw)", "The original message received.", "connectorMessage.getRawData()"));
        addReference(new CodeReference(CONTEXT_FILTER_TRANSFORMER, Category.MESSAGE.toString(), "Incoming Message (Transformed)", "In a filter/transformer script, this represents the inbound data, serialized to an E4X XML object. If the inbound data type is Raw, this will instead be a string. If the inbound data type is JSON, this will be a JavaScript object.", "msg"));
        addReference(new CodeReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), "Message Source", "The message source (sending facility)", "$('" + DefaultMetaData.SOURCE_VARIABLE_MAPPING + "')"));
        addReference(new CodeReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), "Message Type", "The message type", "$('" + DefaultMetaData.TYPE_VARIABLE_MAPPING + "')"));
        addReference(new CodeReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), "Message Version", "The message version", "$('" + DefaultMetaData.VERSION_VARIABLE_MAPPING + "')"));
        addReference(new CodeReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), "Message ID", "The ID of the overall message being processed", "connectorMessage.getMessageId()"));
        addReference(new CodeReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), "Metadata ID", "The ID of the connector the message is currently being processed through", "connectorMessage.getMetaDataId()"));
        addReference(new CodeReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), "Message Inbound Data Type", "The inbound data type for this connector message", "connectorMessage.getRaw().getDataType()"));
        addReference(new ParameterizedCodeReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), "Iterate Over Segment", "Iterates over a segment that repeats in a message.  Replace SEG with your segment name (i.e. OBX)", "for each (${seg} in ${msg}..${SEG}) {\n\tvar ${sample_value} = ${seg}['${SEG}.1']['${SEG}.1.1'].toString();\n}\n"));
        addReference(new ParameterizedCodeReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), "Iterate Over All Segments", "Iterates over all segments in a message.  The if-statement checks for only segments named \"SEG\".", "for each (${seg} in ${msg}.children()) {\n\tif (${seg}.name().toString() == \"${SEG}\") {\n\t\tvar ${sample_value} = ${seg}['${SEG}.1']['${SEG}.1.1'].toString();\n\t}\n}\n"));
        addReference(new FunctionReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), null, "Create Segment (individual)", "Create a new segment that can be used in any message.", "function createSegment(segmentName) {}", new CodeTemplateFunctionDefinition("createSegment", new Parameters("segmentName", "String", "The name of the segment to create."), "XML", "An E4X XML object representing the created segment.")));
        addReference(new FunctionReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), null, "Create Segment (in message)", "Create a new segment in specified message (msg or tmp)", "function createSegment(segmentName, msg) {}", new CodeTemplateFunctionDefinition("createSegment", new Parameters("segmentName", "String", "The name of the segment to create.").add("msg", "XML", "The parent XML object in which to create the segment."), "XML", "An E4X XML object representing the created segment.")));
        addReference(new FunctionReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), null, "Create Segment (in message, index)", "Create a new segment in specified message (msg or tmp) at segment index i", "function createSegment(segmentName, msg, i) {}", new CodeTemplateFunctionDefinition("createSegment", new Parameters("segmentName", "String", "The name of the segment to create.").add("msg", "XML", "The parent XML object in which to create the segment.").add("i", "Number", "The index at which to insert the segment into the parent XML object."), "XML", "An E4X XML object representing the created segment.")));
        addReference(new FunctionReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), null, "Create Segment After Segment", "Create a new segment and insert it after the target segment", "function createSegmentAfter(insertSegmentName, afterThisSegment) {}", new CodeTemplateFunctionDefinition("createSegmentAfter", new Parameters("insertSegmentName", "String", "The name of the segment to create.").add("afterThisSegment", "XML", "The existing segment that the new segment will be inserted after."), "XML", "An E4X XML object representing the created segment.")));
        addReference(new ParameterizedCodeReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), "Delete Segment", "Delete a segment from the message", "delete ${msg}['${segment}']"));
        addReference(new ParameterizedCodeReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), "Remove one or more from Destination Set", "Stop one or more destinations from being processed for this message. Only available in the preprocessor or source filter/transformer.", "destinationSet.remove([${metaDataIdOrConnectorNames}]);"));
        addReference(new ParameterizedCodeReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), "Remove all except specified from Destination Set", "Stop all except the specified destinations from being processed for this message. Only available in the preprocessor or source filter/transformer.", "destinationSet.removeAllExcept([${metaDataIdOrConnectorNames}]);"));
        addReference(new ParameterizedCodeReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), "Remove all from Destination Set", "Stop all destinations from being processed for this message. Only available in the preprocessor or source filter/transformer.", "destinationSet.removeAll();"));
        addReference(new ParameterizedCodeReference(CONTEXT_CHANNEL, Category.MESSAGE.toString(), "Message Reprocessed", "Get a variable indicating if this message was reprocessed.", "var reprocessed = sourceMap.get('reprocessed') == true;"));
        addReference(new ParameterizedCodeReference(CONTEXT_CHANNEL, Category.MESSAGE.toString(), "Message Replaced", "Get a variable indicating if this message was reprocessed and replaced.", "var replaced = sourceMap.get('replaced') == true;"));
        addReference(new ParameterizedCodeReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), "Validate Input", "Validates an input value and returns a default value instead if empty.", "var ${output} = validate(${input}, ${defaultValue});"));
        addReference(new ParameterizedCodeReference(CONTEXT_CONNECTOR, Category.MESSAGE.toString(), "Validate Input and Replace", "Validates an input value and returns a default value instead if empty, after performing a string replacement with the provided array.", "var ${replacements} = [\n\t[ '${regularExpression}', '${replacement}' ]\n];\nvar ${output} = validate(${input}, ${defaultValue}, ${replacements});"));

        // Response references
        addReference(new CodeReference(CONTEXT_RESPONSE_TRANSFORMER, Category.RESPONSE.toString(), "Set Response Status to SENT", "Indicates message was successfully SENT.", "responseStatus = SENT;"));
        addReference(new CodeReference(CONTEXT_RESPONSE_TRANSFORMER, Category.RESPONSE.toString(), "Set Response Status to QUEUED", "Indicates message should be QUEUED. If queuing is disabled, the message status will be set to ERROR.", "responseStatus = QUEUED;"));
        addReference(new CodeReference(CONTEXT_RESPONSE_TRANSFORMER, Category.RESPONSE.toString(), "Set Response Status to ERROR", "Indicates message should have its status set to ERROR.", "responseStatus = ERROR;"));
        addReference(new ParameterizedCodeReference(CONTEXT_RESPONSE_TRANSFORMER, Category.RESPONSE.toString(), "Set Response Status Message", "Sets the status message of the response.", "responseStatusMessage = '${}';"));
        addReference(new ParameterizedCodeReference(CONTEXT_RESPONSE_TRANSFORMER, Category.RESPONSE.toString(), "Set Response Error Message", "Sets the error message of the response.", "responseErrorMessage = '${}';"));

        // Channel references
        addReference(new VariableReference(CONTEXT_CHANNEL, Category.CHANNEL.toString(), "Channel ID", "The message channel id", "channelId"));
        addReference(new ParameterizedCodeReference(CONTEXT_CHANNEL, Category.CHANNEL.toString(), "Channel Name", "The message channel name", "channelName"));

        // Map references
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.MAP.toString(), "Lookup Value in All Maps", "Returns the value of the key if it exists in any map.", "$('${key}')"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.MAP.toString(), "Get Configuration Variable Map", "The variable map containing server specific settings.", "configurationMap.get('${key}')"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.MAP.toString(), "Get Global Variable Map", "The variable map that persists values between channels.", "globalMap.get('${key}')"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.MAP.toString(), "Put Global Variable Map", "The variable map that persists values between channels.", "globalMap.put('${key}',${'value'})"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.MAP.toString(), "Get Global Channel Variable Map", "The variable map that persists values between messages in a single channel.", "globalChannelMap.get('${key}')"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.MAP.toString(), "Put Global Channel Variable Map", "The variable map that persists values between messages in a single channel.", "globalChannelMap.put('${key}',${'value'})"));
        addReference(new ParameterizedCodeReference(CONTEXT_CONNECTOR, Category.MAP.toString(), "Get Connector Variable Map", "The variable map that will be sent to the connector.", "connectorMap.get('${key}')"));
        addReference(new ParameterizedCodeReference(CONTEXT_CONNECTOR, Category.MAP.toString(), "Put Connector Variable Map", "The variable map that will be sent to the connector.", "connectorMap.put('${key}',${'value'})"));
        addReference(new ParameterizedCodeReference(CONTEXT_CONNECTOR, Category.MAP.toString(), "Get Channel Variable Map", "The variable map that can be used anywhere in the channel.", "channelMap.get('${key}')"));
        addReference(new ParameterizedCodeReference(CONTEXT_CONNECTOR, Category.MAP.toString(), "Put Channel Variable Map", "The variable map that can be used anywhere in the channel.", "channelMap.put('${key}',${'value'})"));
        addReference(new ParameterizedCodeReference(CONTEXT_CONNECTOR, Category.MAP.toString(), "Get Source Variable Map", "The variable map containing metadata about the original message. This map is read-only.", "sourceMap.get('${key}')"));
        addReference(new ParameterizedCodeReference(CONTEXT_RESPONSE_MAP, Category.MAP.toString(), "Get Response Variable Map", "The variable map that stores responses.", "responseMap.get('${key}')"));
        addReference(new ParameterizedCodeReference(CONTEXT_RESPONSE_MAP, Category.MAP.toString(), "Put Sent Response Variable", "Places a successful response in the response variable map.", "responseMap.put('${key}', ResponseFactory.getSentResponse('${message}'))"));
        addReference(new ParameterizedCodeReference(CONTEXT_RESPONSE_MAP, Category.MAP.toString(), "Put Error Response Variable", "Places an unsuccessful response in the response variable map.", "responseMap.put('${key}', ResponseFactory.getErrorResponse('${message}'))"));
        addReference(new ParameterizedCodeReference(CONTEXT_RESPONSE_MAP, Category.MAP.toString(), "Create Sent Response", "Creates a successful response object.", "ResponseFactory.getSentResponse('${message}')"));
        addReference(new ParameterizedCodeReference(CONTEXT_RESPONSE_MAP, Category.MAP.toString(), "Create Error Response", "Creates an unsuccessful response object.", "ResponseFactory.getErrorResponse('${message}')"));

        // Utility references
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Build Map", "Creates a new HashMap and adds an entry to it.", "var ${map} = Maps.map().add('${key}', ${value});"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Build List", "Creates a new ArrayList and adds an element to it.", "var ${list} = Lists.list().append(${element});"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Use Java Class", "Access any Java class in the current classpath", "var ${object} = Packages.${[fully-qualified name]};"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Generate Unique ID", "Generate a Universally Unique Identifier", "var ${uuid} = UUIDGenerator.getUUID();"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Call System Function", "Execute a command on server system. Must have proper security enabled.", "java.lang.Runtime.getRuntime().exec(\"${system_command}\");"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Read File As String", "Read file contents into string", "var ${contents} = FileUtil.read('${filename}');"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Read File As Bytes", "Read file contents into byte array", "var ${contents} = FileUtil.readBytes('${filename}');"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Write String to File", "Write string to file", "FileUtil.write('${filename}', ${append(true/false)}, ${stringData});"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Write Bytes to File", "Write bytes to file", "FileUtil.write('${filename}', ${append(true/false)}, ${byteData});"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "BASE-64 Encode Data", "Encode a byte array to a BASE-64 string", "FileUtil.encode(${data});"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Decode BASE-64 Data", "Decode a BASE-64 string to a byte array", "FileUtil.decode(${data});"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Route Message to Channel", "Sends the specified data to a different channel.", "router.routeMessage('${channelName}', '${message}');"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Route Message by Channel ID", "Sends the specified data to a different channel.", "router.routeMessageByChannelId('${channelId}', '${message}');"));
        addReference(new ParameterizedCodeReference(CONTEXT_CONNECTOR, Category.UTILITY.toString(), "Perform Message Object Value Replacement", "Returns a string that has been run through Velocity replacer with a connectorMessage context", "var ${results} = replacer.replaceValues(${template}, connectorMessage);"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Perform Map Value Replacement", "Returns a string that has been run through Velocity replacer with a map context", "var ${results} = replacer.replaceValues(${template}, ${map});"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Format Overpunch NCPDP Number", "Returns number with decimal points and correct sign", "var ${number} = NCPDPUtil.formatNCPDPNumber('${number}', ${decimalpoints});"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Convert DICOM to Encoded Image as a BASE-64 string", "Converts and returns an image as a BASE-64 string from an uncompressed DICOM image (imagetype: either TIF, JPEG, BMP, PNG, or RAW). If the slice index is left out, it will default to 1.", "DICOMUtil.convertDICOM('${imagetype}', connectorMessage, ${sliceIndex})"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Convert DICOM to Encoded Image as a byte array", "Converts and returns and image as a byte array from an uncompressed DICOM image (imagetype: either TIF,JPEG, BMP, PNG, or RAW). If the slice index is left out, it will default to 1.", "DICOMUtil.convertDICOMToByteArray('${imagetype}', connectorMessage, ${sliceIndex})"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Get DICOM image slice count", "Returns the number of image slices within the uncompressed DICOM image", "DICOMUtil.getSliceCount(connectorMessage)"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Get DICOM message", "Gets the full DICOM messages with image data", "DICOMUtil.getDICOMMessage(connectorMessage)"));
        addReference(new FunctionReference(CONTEXT_ATTACHMENT, Category.UTILITY.toString(), null, "Add Attachment", "Add attachment (String or byte[]) to message", "function addAttachment(data, type) {}", new CodeTemplateFunctionDefinition("addAttachment", new Parameters("data", "Object", "The data to insert as an attachment. May be a string or byte array.").add("type", "String", "The MIME type of the attachment."), "Attachment", "The inserted Attachment object.")));
        addReference(new FunctionReference(CONTEXT_ATTACHMENT, Category.UTILITY.toString(), null, "Get Attachments", "Get List of Attachments associated with this message.  This will get all attachments that have been added in the source and destination(s).", "function getAttachments() {}", new CodeTemplateFunctionDefinition("getAttachments", new Parameters(), "List<Attachment>", "A list of Attachment objects associated with this message.")));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Strip Namespaces", "Remove namespaces from an XML string", "var ${newMessage} = ${message}.replace(/xmlns:?[^=]*=[\"\"][^\"\"]*[\"\"]/g, '');\n"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Parse HTTP Headers", "Takes the string of an HTTP Response and returns it represented as a map for easy access.", "var ${headers} = HTTPUtil.parseHeaders(${header});"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.UTILITY.toString(), "Remove Illegal XML Characters", "Removes illegal XML characters like control characters that cause a parsing error in e4x (\\x00-\\x1F besides TAB, LF, and CR)", "var ${newMessage} = ${message}.replace(/[\\x00-\\x08]|[\\x0B-\\x0C]|[\\x0E-\\x1F]/g, '');\n"));

        // Date references
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.DATE.toString(), "Get Date Object From Pattern", "Parse a date according to specified pattern", "var ${date} = DateUtil.getDate(${pattern}, ${date});"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.DATE.toString(), "Format Date Object", "Formats a date object based on specified format", "var ${dateString} = DateUtil.formatDate(${pattern}, ${date});"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.DATE.toString(), "Convert Date String", "Parse a date and return a newly formatted date", "var ${datestring} = DateUtil.convertDate(${inpattern}, ${outpattern}, ${date});"));
        addReference(new ParameterizedCodeReference(CONTEXT_GLOBAL, Category.DATE.toString(), "Get Current Date", "Returns the current date/time in specified format", "var ${dateString} = DateUtil.getCurrentDate(${pattern});"));

        // Postprocessor references
        addReference(new VariableReference(CONTEXT_POSTPROCESSOR, Category.POSTPROCESSOR.toString(), "Completed Message Object", "The final message object, which contains all processed source and destination connector messages.", "message"));
        addReference(new CodeReference(CONTEXT_POSTPROCESSOR, Category.POSTPROCESSOR.toString(), "Get Merged Connector Message", "Returns a connector message that has a channel map and response map which are merged from all connector messages.\nThis also includes the raw and processed raw content from the source connector.", "message.getMergedConnectorMessage()"));
        addReference(new CodeReference(CONTEXT_POSTPROCESSOR, Category.POSTPROCESSOR.toString(), "Get Source Connector Message", "Returns the source connector message contained in the final message object.", "message.getConnectorMessages().get(0)"));
        addReference(new ParameterizedCodeReference(CONTEXT_POSTPROCESSOR, Category.POSTPROCESSOR.toString(), "Get Destination Connector Message", "Returns a specific destination connector message contained in the final message object.", "message.getConnectorMessages().get(${metaDataId})"));
    }

    private void addMiscellaneousReferences() {
        addReference(new VariableReference(CONTEXT_CONNECTOR, null, "Connector Message", "The current connector's ImmutableConnectorMessage object.", "connectorMessage"));
        addReference(new VariableReference(CONTEXT_FILTER_TRANSFORMER, null, "Outbound Message (Transformed)", "In a filter/transformer script, this represents the outbound template, serialized to an E4X XML object. If the outbound data type is Raw, this will instead be a string. If the outbound data type is JSON, this will be a JavaScript object.", "tmp"));
        addReference(new VariableReference(CONTEXT_RESPONSE_TRANSFORMER, null, "Response Object", "In a response transformer script, this is the ImmutableResponse object associated with the response data.", "response"));
        addReference(new VariableReference(CONTEXT_RESPONSE_TRANSFORMER, null, "Response Status", "In a response transformer script, this is the status which will be used to set the status of the corresponding connector message.", "responseStatus"));
        addReference(new VariableReference(CONTEXT_RESPONSE_TRANSFORMER, null, "Response Error Message", "In a response transformer script, this is the error message which will be used to set the error content of the corresponding connector message.", "responseErrorMessage"));
        addReference(new VariableReference(CONTEXT_RESPONSE_TRANSFORMER, null, "Response Status Message", "In a response transformer script, this is a brief message explaning the reason for the current status.", "responseStatusMessage"));
        addReference(new VariableReference(CONTEXT_CONNECTOR, null, "Connector Map", "The variable map associated with the current connector. Values placed in this map will not persist to other connectors.", "connectorMap"));
        addReference(new VariableReference(CONTEXT_CHANNEL, null, "Channel Map", "The variable map associated with the current message. Values placed in this map will be accessible to downstream connectors and the postprocessor, but will not be accessible to subsequent messages.", "channelMap"));
        addReference(new VariableReference(CONTEXT_CHANNEL, null, "Source Map", "The read-only variable map associated with the current message. Values are placed in this map at the beginning of the message lifecycle (e.g. \"originalFilename\").", "sourceMap"));
        addReference(new VariableReference(CONTEXT_GLOBAL, null, "Global Map", "The global variable map. Values placed in this map will be accessible throughout the entire server.", "globalMap"));
        addReference(new VariableReference(CONTEXT_CHANNEL, null, "Global Channel Map", "The variable map associated with the current channel. Values placed in this map will be accessible throughout the entire channel, and across multiple messages.", "globalChannelMap"));
        addReference(new VariableReference(CONTEXT_GLOBAL, null, "Configuration Map", "The variable map associated with the current server instance. Values placed in this map will be accessible thoughout the entire server. These values can also be edited on the corresponding settings view.", "configurationMap"));
        addReference(new VariableReference(CONTEXT_RESPONSE_MAP, null, "Response Map", "The variable map used to store Response objects for the current message. Values placed in this map may be used to respond to an originating system with. Subsequent destination connectors may also reference these values.", "responseMap"));
        addReference(new VariableReference(CONTEXT_GLOBAL, null, "log4j Logger", "This object can be used to send messages to the server log.", "logger"));
        addReference(new VariableReference(CONTEXT_CONNECTOR, null, "AlertSender", "An instance of AlertSender that can be used to trigger User Defined Transformer events which can be captured by alerts.", "alerts"));
        addReference(new VariableReference(CONTEXT_GLOBAL, null, "VMRouter", "An instance of VMRouter that can be used to dispatch messages to other channels.", "router"));
        addReference(new VariableReference(CONTEXT_CONNECTOR, null, "TemplateValueReplacer", "An instance of TemplateValueReplacer that can be used to perform Velocity template replacement.", "replacer"));
        addReference(new VariableReference(CONTEXT_BATCH, null, "Batch Reader", "In a JavaScript batch script, this BufferedReader object is used to read the incoming data stream.", "reader"));
        addReference(new VariableReference(CONTEXT_GLOBAL, null, "JavaScript Context Factory", "This object can be used to retrieve the resource IDs and classloaders used by the current JavaScript context.", "contextFactory"));

        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Map Value", "Returns the value of the key if it exists in any map.", null, new CodeTemplateFunctionDefinition("$", new Parameters("key", "String", "The key of the entry to retrieve."), "Object", "The value, or null if no value exists.")));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Configuration Map Value", "Get a value from the configuration map.", null, new CodeTemplateFunctionDefinition("$cfg", new Parameters("key", "String", "The key of the entry to retrieve."), "Object", "The value contained in the map, or null if no value exists.")));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Global Map Value", "Get a value from the global map.", null, new CodeTemplateFunctionDefinition("$g", new Parameters("key", "String", "The key of the entry to retrieve."), "Object", "The value contained in the map, or null if no value exists.")));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Put Global Map Value", "Put a value into the global map.", null, new CodeTemplateFunctionDefinition("$g", new Parameters("key", "String", "The key of the entry to retrieve.").add("value", "Object", "The value to put into the map."), "Object", "The previous value associated with the key, or null if there was no mapping.")));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Global Channel Map Value", "Get a value from the global channel map.", null, new CodeTemplateFunctionDefinition("$gc", new Parameters("key", "String", "The key of the entry to retrieve."), "Object", "The value contained in the map, or null if no value exists.")));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Put Global Channel Map Value", "Put a value into the global channel map.", null, new CodeTemplateFunctionDefinition("$gc", new Parameters("key", "String", "The key of the entry to retrieve.").add("value", "Object", "The value to put into the map."), "Object", "The previous value associated with the key, or null if there was no mapping.")));
        addReference(new FunctionReference(CONTEXT_CONNECTOR, null, null, "Get Connector Map Value", "Get a value from the connector map.", null, new CodeTemplateFunctionDefinition("$co", new Parameters("key", "String", "The key of the entry to retrieve."), "Object", "The value contained in the map, or null if no value exists.")));
        addReference(new FunctionReference(CONTEXT_CONNECTOR, null, null, "Put Connector Map Value", "Put a value into the connector map.", null, new CodeTemplateFunctionDefinition("$co", new Parameters("key", "String", "The key of the entry to retrieve.").add("value", "Object", "The value to put into the map."), "Object", "The previous value associated with the key, or null if there was no mapping.")));
        addReference(new FunctionReference(CONTEXT_CONNECTOR, null, null, "Get Channel Map Value", "Get a value from the channel map.", null, new CodeTemplateFunctionDefinition("$c", new Parameters("key", "String", "The key of the entry to retrieve."), "Object", "The value contained in the map, or null if no value exists.")));
        addReference(new FunctionReference(CONTEXT_CONNECTOR, null, null, "Put Channel Map Value", "Put a value into the channel map.", null, new CodeTemplateFunctionDefinition("$c", new Parameters("key", "String", "The key of the entry to retrieve.").add("value", "Object", "The value to put into the map."), "Object", "The previous value associated with the key, or null if there was no mapping.")));
        addReference(new FunctionReference(CONTEXT_CONNECTOR, null, null, "Get Source Map Value", "Get a value from the source map.", null, new CodeTemplateFunctionDefinition("$s", new Parameters("key", "String", "The key of the entry to retrieve."), "Object", "The value contained in the map, or null if no value exists.")));
        addReference(new FunctionReference(CONTEXT_RESPONSE_MAP, null, null, "Get Response Map Value", "Get a value from the response map.", null, new CodeTemplateFunctionDefinition("$r", new Parameters("key", "String", "The key of the entry to retrieve."), "Object", "The value contained in the map, or null if no value exists.")));
        addReference(new FunctionReference(CONTEXT_RESPONSE_MAP, null, null, "Put Response Map Value", "Put a value into the response map.", null, new CodeTemplateFunctionDefinition("$r", new Parameters("key", "String", "The key of the entry to retrieve.").add("value", "Object", "The value to put into the map."), "Object", "The previous value associated with the key, or null if there was no mapping.")));

        addReference(new FunctionReference(CONTEXT_CONNECTOR, null, null, "Validate Input", "Validates an input value and returns a default value instead if empty.", null, new CodeTemplateFunctionDefinition("validate", new Parameters("input", "String", "The input value to validate. If empty, the default value will be used instead.").add("defaultValue", "String", "The default value to use instead when the input value is empty."), "String", "The post-validation result string.")));
        addReference(new FunctionReference(CONTEXT_CONNECTOR, null, null, "Validate Input and Replace", "Validates an input value and returns a default value instead if empty, after performing a string replacement with the provided array.", null, new CodeTemplateFunctionDefinition("validate", new Parameters("input", "String", "The input value to validate. If empty, the default value will be used instead.").add("defaultValue", "String", "The default value to use instead when the input value is empty.").add("replacements", "Array", "A two-dimensional array of string replacements to perform. Each entry is composed of an array of two elements, one for the regular expression, and one for the replacement value."), "String", "The post-validation result string.")));

        List<String> loggerText = Collections.singletonList("logger");
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Log Error Message", "Logs a message with the ERROR level.", null, new CodeTemplateFunctionDefinition("error", new Parameters("message", "String", "The message to log out."), "void", ""), loggerText));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Log Error Message", "Logs a message with the ERROR level.", null, new CodeTemplateFunctionDefinition("error", new Parameters("message", "String", "The message to log out.").add("t", "Throwable", "The exception to log, including its stacktrace."), "void", ""), loggerText));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Log Warning Message", "Logs a message with the WARN level.", null, new CodeTemplateFunctionDefinition("warn", new Parameters("message", "String", "The message to log out."), "void", ""), loggerText));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Log Warning Message", "Logs a message with the WARN level.", null, new CodeTemplateFunctionDefinition("warn", new Parameters("message", "String", "The message to log out.").add("t", "Throwable", "The exception to log, including its stacktrace."), "void", ""), loggerText));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Log Info Message", "Logs a message with the INFO level.", null, new CodeTemplateFunctionDefinition("info", new Parameters("message", "String", "The message to log out."), "void", ""), loggerText));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Log Info Message", "Logs a message with the INFO level.", null, new CodeTemplateFunctionDefinition("info", new Parameters("message", "String", "The message to log out.").add("t", "Throwable", "The exception to log, including its stacktrace."), "void", ""), loggerText));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Log Debug Message", "Logs a message with the DEBUG level.", null, new CodeTemplateFunctionDefinition("debug", new Parameters("message", "String", "The message to log out."), "void", ""), loggerText));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Log Debug Message", "Logs a message with the DEBUG level.", null, new CodeTemplateFunctionDefinition("debug", new Parameters("message", "String", "The message to log out.").add("t", "Throwable", "The exception to log, including its stacktrace."), "void", ""), loggerText));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Log Trace Message", "Logs a message with the TRACE level.", null, new CodeTemplateFunctionDefinition("trace", new Parameters("message", "String", "The message to log out."), "void", ""), loggerText));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Log Trace Message", "Logs a message with the TRACE level.", null, new CodeTemplateFunctionDefinition("trace", new Parameters("message", "String", "The message to log out.").add("t", "Throwable", "The exception to log, including its stacktrace."), "void", ""), loggerText));
    }

    private void addE4XReferences() {
        List<String> beforeDotTextList = new ArrayList<String>();
        beforeDotTextList.add("msg");
        beforeDotTextList.add("tmp");

        addReference(new ConstructorReference(CONTEXT_GLOBAL, null, "XML", "XML", "An E4X XML object. The XML type is an ordered collection of properties with a name, a set of XML attributes, a set of in-scope namespaces and a parent.", null, new CodeTemplateFunctionDefinition("XML", new Parameters(), "XML", "A new E4X XML object.")));
        addReference(new ConstructorReference(CONTEXT_GLOBAL, null, "XML", "XML", "An E4X XML object. The XML type is an ordered collection of properties with a name, a set of XML attributes, a set of in-scope namespaces and a parent.", null, new CodeTemplateFunctionDefinition("XML", new Parameters("value", "String", "The value to construct the XML object with. May be a String or another XML object."), "XML", "A new E4X XML object.")));
        addReference(new ConstructorReference(CONTEXT_GLOBAL, null, "XMLList", "XMLList", "The XMLList type is an ordered collection of properties.", null, new CodeTemplateFunctionDefinition("XMLList", new Parameters(), "XMLList", "A new E4X XMLList object.")));
        addReference(new ConstructorReference(CONTEXT_GLOBAL, null, "XMLList", "XMLList", "The XMLList type is an ordered collection of properties.", null, new CodeTemplateFunctionDefinition("XMLList", new Parameters("value", "String/XMLList", "The value to construct the XMLList object with. May be a String, an XML object, or another XMLList object."), "XMLList", "A new E4X XMLList object.")));
        addReference(new ConstructorReference(CONTEXT_GLOBAL, null, "Namespace", "Namespace", "Namespace objects represent XML namespaces and provide an association between a namespace prefix and a Unique Resource Identifier (URI). The prefix is either the undefined value or a string value that may be used to reference the namespace within the lexical representation of an XML value. When an XML object containing a namespace with an undefined prefix is encoded as XML by the method ToXMLString(), the implementation will automatically generate a prefix. The URI is a string value used to uniquely identify the namespace.", null, new CodeTemplateFunctionDefinition("Namespace", new Parameters(), "Namespace", "A new E4X Namespace object.")));
        addReference(new ConstructorReference(CONTEXT_GLOBAL, null, "Namespace", "Namespace", "Namespace objects represent XML namespaces and provide an association between a namespace prefix and a Unique Resource Identifier (URI). The prefix is either the undefined value or a string value that may be used to reference the namespace within the lexical representation of an XML value. When an XML object containing a namespace with an undefined prefix is encoded as XML by the method ToXMLString(), the implementation will automatically generate a prefix. The URI is a string value used to uniquely identify the namespace.", null, new CodeTemplateFunctionDefinition("Namespace", new Parameters("uriValue", "String", "The URI to reference."), "Namespace", "A new E4X Namespace object.")));
        addReference(new ConstructorReference(CONTEXT_GLOBAL, null, "Namespace", "Namespace", "Namespace objects represent XML namespaces and provide an association between a namespace prefix and a Unique Resource Identifier (URI). The prefix is either the undefined value or a string value that may be used to reference the namespace within the lexical representation of an XML value. When an XML object containing a namespace with an undefined prefix is encoded as XML by the method ToXMLString(), the implementation will automatically generate a prefix. The URI is a string value used to uniquely identify the namespace.", null, new CodeTemplateFunctionDefinition("Namespace", new Parameters("prefixValue", "String", "The bound prefix of the namespace.").add("uriValue", "String", "The URI to reference."), "Namespace", "A new E4X Namespace object.")));

        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Add Namespace", "The addNamespace method adds a namespace declaration to the in scope namespaces for this XML object and returns this XML object. If the in scope namespaces for the XML object already contains a namespace with a prefix matching that of the given parameter, the prefix of the existing namespace is set to undefined.", null, new CodeTemplateFunctionDefinition("addNamespace", new Parameters("namespace", "Namespace", "The namespace to add (Namespace or String)"), "XML", "This XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Append Child", "The appendChild method appends the given child to the end of this XML object’s properties and returns this XML object.", null, new CodeTemplateFunctionDefinition("appendChild", new Parameters("child", "XML", "The child to append."), "XML", "This XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Attribute", "The attribute method returns an XMLList containing zero or one XML attributes associated with this XML object that have the given attributeName.", null, new CodeTemplateFunctionDefinition("attribute", new Parameters("attributeName", "String", "The name of the attribute to search for."), "XMLList", "A list of matching attributes."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Attributes", "The attributes method returns an XMLList containing the XML attributes of this object.", null, new CodeTemplateFunctionDefinition("attributes", new Parameters(), "XMLList", "The list of attributes."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Child", "The child method returns the list of children in this XML object matching the given propertyName. If propertyName is a numeric index, the child method returns a list containing the child at the ordinal position identified by propertyName.", null, new CodeTemplateFunctionDefinition("child", new Parameters("propertyName", "String/Number", "The name or index of the child."), "XML", "The child XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Child Index", "The childIndex method returns a Number representing the ordinal position of this XML object within the context of its parent.", null, new CodeTemplateFunctionDefinition("childIndex", new Parameters(), "Number", "The ordinal position of this XML object within the context of its parent."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Children", "The children method returns an XMLList containing all the properties of this XML object in order.", null, new CodeTemplateFunctionDefinition("children", new Parameters(), "XMLList", "An XMLList of all the children."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Comments", "The comments method returns an XMLList containing the properties of this XML object that represent XML comments.", null, new CodeTemplateFunctionDefinition("comments", new Parameters(), "XMLList", "The list of all comments within this XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Contains", "The contains method returns the result of comparing this XML object with the given value. This treatment intentionally blurs the distinction between a single XML object and an XMLList containing only one value.", null, new CodeTemplateFunctionDefinition("contains", new Parameters("value", "XML/XMLList", "The value to compare this object against."), "boolean", "True if the value is contained within this XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Copy", "The copy method returns a deep copy of this XML object with the internal [[Parent]] property set to null.", null, new CodeTemplateFunctionDefinition("copy", new Parameters(), "XML", "A deep copy of this XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Descendants", "The descendants method returns all the XML valued descendants (children, grandchildren, great-grandchildren, etc.) of this XML object with the given name. If the name parameter is omitted, it returns all descendants of this XML object.", null, new CodeTemplateFunctionDefinition("descendants", new Parameters(), "XMLList", "The list of all descendants."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Descendants", "The descendants method returns all the XML valued descendants (children, grandchildren, great-grandchildren, etc.) of this XML object with the given name. If the name parameter is omitted, it returns all descendants of this XML object.", null, new CodeTemplateFunctionDefinition("descendants", new Parameters("name", "String", "The name to match descendant nodes against."), "XMLList", "The list of matching descendants."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Elements", "When the elements method is called with one parameter name, it returns an XMLList containing all the children of this XML object that are XML elements with the given name. When the elements method is called with no parameters, it returns an XMLList containing all the children of this XML object that are XML elements regardless of their name.", null, new CodeTemplateFunctionDefinition("elements", new Parameters(), "XMLList", "The list of all child XML elements."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Elements", "When the elements method is called with one parameter name, it returns an XMLList containing all the children of this XML object that are XML elements with the given name. When the elements method is called with no parameters, it returns an XMLList containing all the children of this XML object that are XML elements regardless of their name.", null, new CodeTemplateFunctionDefinition("elements", new Parameters("name", "String", "The name to match elements against."), "XMLList", "The list of matching child XML elements."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Has Own Property", "The hasOwnProperty method returns a Boolean value indicating whether this object has the property specified by P. For all XML objects except the XML prototype object, this is the same result returned by the internal method [[HasProperty]]. For the XML prototype object, hasOwnProperty also examines the list of local properties to determine if there is a method property with the given name.", null, new CodeTemplateFunctionDefinition("hasOwnProperty", new Parameters("P", "Object", "The property to search for."), "boolean", "True if the specified property was found."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Has Complex Content", "The hasComplexContent method returns a Boolean value indicating whether this XML object contains complex content. An XML object is considered to contain complex content if it represents an XML element that has child elements. XML objects representing attributes, comments, processing instructions and text nodes do not have complex content. The existence of attributes, comments, processing instructions and text nodes within an XML object is not significant in determining if it has complex content.", null, new CodeTemplateFunctionDefinition("hasComplexContent", new Parameters(), "boolean", "True if this XML object has complex content."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Has Simple Content", "The hasSimpleContent method returns a Boolean value indicating whether this XML object contains simple content. An XML object is considered to contain simple content if it represents a text node, represents an attribute node or if it represents an XML element that has no child elements. XML objects representing comments and processing instructions do not have simple content. The existence of attributes, comments, processing instructions and text nodes within an XML object is not significant in determining if it has simple content.", null, new CodeTemplateFunctionDefinition("hasSimpleContent", new Parameters(), "boolean", "True if this XML object has simple content."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get In-Scope Namespaces", "The inScopeNamespaces method returns an Array of Namespace objects representing the namespaces in scope for this XML object in the context of its parent. If the parent of this XML object is modified, the associated namespace declarations may change. The set of namespaces returned by this method may be a super set of the namespaces used by this value.", null, new CodeTemplateFunctionDefinition("inScopeNamespaces", new Parameters(), "Array", "The array of Namespace objects in-scope for this XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Insert Child After", "The insertChildAfter method inserts the given child2 after the given child1 in this XML object and returns this XML object. If child1 is null, the insertChildAfter method inserts child2 before all children of this XML object (i.e., after none of them). If child1 does not exist in this XML object, it returns without modifying this XML object.", null, new CodeTemplateFunctionDefinition("insertChildAfter", new Parameters("child1", "XML", "The child to insert child2 after.").add("child2", "XML", "The child to insert after child1."), "XML", "This XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Insert Child Before", "The insertChildBefore method inserts the given child2 before the given child1 in this XML object and returns this XML object. If child1 is null, the insertChildBefore method inserts child2 after all children in this XML object (i.e., before none of them). If child1 does not exist in this XML object, it returns without modifying this XML object.", null, new CodeTemplateFunctionDefinition("insertChildBefore", new Parameters("child1", "XML", "The child to insert child2 before.").add("child2", "XML", "The child to insert before child1."), "XML", "This XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Length", "The length method always returns the integer 1 for XML objects. This treatment intentionally blurs the distinction between a single XML object and an XMLList containing only one value.", null, new CodeTemplateFunctionDefinition("length", new Parameters(), "Number", ""), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Local Name", "The localName method returns the local name portion of the qualified name of this XML object.", null, new CodeTemplateFunctionDefinition("localName", new Parameters(), "String", "The local name of this XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Name", "The name method returns the qualified name associated with this XML object.", null, new CodeTemplateFunctionDefinition("name", new Parameters(), "QName", "The qualified name of this XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Namespace", "If no prefix is specified, the namespace method returns the Namespace associated with the qualified name of this XML object.\n\nIf a prefix is specified, the namespace method looks for a namespace in scope for this XML object with the given prefix and, if found, returns it. If no such namespace is found, the namespace method returns undefined.", null, new CodeTemplateFunctionDefinition("namespace", new Parameters(), "Namespace", "The namespace associated with this XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Namespace", "If no prefix is specified, the namespace method returns the Namespace associated with the qualified name of this XML object.\n\nIf a prefix is specified, the namespace method looks for a namespace in scope for this XML object with the given prefix and, if found, returns it. If no such namespace is found, the namespace method returns undefined.", null, new CodeTemplateFunctionDefinition("namespace", new Parameters("prefix", "String", "The prefix of the namespace to match against."), "Namespace", "The namespace associated with this XML object and matching against the specified prefix."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Namespace Declarations", "The namespaceDeclarations method returns an Array of Namespace objects representing the namespace declarations associated with this XML object in the context of its parent. If the parent of this XML object is modified, the associated namespace declarations may change.", null, new CodeTemplateFunctionDefinition("namespaceDeclarations", new Parameters(), "Array", "The array of namespace declarations associated with this XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Node Kind", "The nodeKind method returns a string representing the [[Class]] of this XML object.", null, new CodeTemplateFunctionDefinition("nodeKind", new Parameters(), "String", "The node kind of this XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Normalize", "The normalize method puts all text nodes in this and all descendant XML objects into a normal form by merging adjacent text nodes and eliminating empty text nodes. It returns this XML object.", null, new CodeTemplateFunctionDefinition("normalize", new Parameters(), "XML", "This XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Parent", "The parent method returns the parent of this XML object.", null, new CodeTemplateFunctionDefinition("parent", new Parameters(), "XML", "The parent of this XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Processing Instructions", "When the processingInstructions method is called with one parameter name, it returns an XMLList containing all the children of this XML object that are processing-instructions with the given name. When the processingInstructions method is called with no parameters, it returns an XMLList containing all the children of this XML object that are processing-instructions regardless of their name.", null, new CodeTemplateFunctionDefinition("processingInstructions", new Parameters(), "XMLList", "The list of all processing-instructions children."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Processing Instructions", "When the processingInstructions method is called with one parameter name, it returns an XMLList containing all the children of this XML object that are processing-instructions with the given name. When the processingInstructions method is called with no parameters, it returns an XMLList containing all the children of this XML object that are processing-instructions regardless of their name.", null, new CodeTemplateFunctionDefinition("processingInstructions", new Parameters("name", "String", "The name to match children against."), "XMLList", "The list of matching processing-instructions children."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Prepend Child", "The prependChild method inserts the given child into this object prior to its existing XML properties and returns this XML object.", null, new CodeTemplateFunctionDefinition("prependChild", new Parameters("child", "XML", "The child to insert."), "XML", "This XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Property Is Enumerable", "The propertyIsEnumerable method returns a Boolean value indicating whether the property P will be included in the set of properties iterated over when this XML object is used in a for-in statement. This method returns true when ToString(P) is \"0\"; otherwise, it returns false. This treatment intentionally blurs the distinction between a single XML object and an XMLList containing only one value.", null, new CodeTemplateFunctionDefinition("propertyIsEnumerable", new Parameters("P", "Object", "The property to check."), "boolean", "True if the specified property is enumerable."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Remove Namespace", "The removeNamespace method removes the given namespace from the in scope namespaces of this object and all its descendents, then returns a copy of this XML object. The removeNamespaces method will not remove a namespace from an object where it is referenced by that object’s QName or the QNames of that object's attributes.", null, new CodeTemplateFunctionDefinition("removeNamespace", new Parameters("namespace", "Namespace/String", "The namespace to remove."), "XML", "This XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Replace Node(s)", "The replace method replaces the XML properties of this XML object specified by propertyName with value and returns this XML object. If this XML object contains no properties that match propertyName, the replace method returns without modifying this XML object. The propertyName parameter may be a numeric property name, an unqualified name for a set of XML elements, a qualified name for a set of XML elements or the properties wildcard \"*\". When the propertyName parameter is an unqualified name, it identifies XML elements in the default namespace. The value parameter may be an XML object, XMLList object or any value that may be converted to a String with ToString().", null, new CodeTemplateFunctionDefinition("replace", new Parameters("propertyName", "String/Number", "The name or index of the node to replace.").add("value", "XML", "The value to replace with."), "XML", "This XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Set Children", "The setChildren method replaces the XML properties of this XML object with a new set of XML properties from value. value may be a single XML object or an XMLList. setChildren returns this XML object.", null, new CodeTemplateFunctionDefinition("setChildren", new Parameters("value", "XML/XMLList", "The value to replace the children with."), "XML", "This XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Set Local Name", "The setLocalName method replaces the local name of this XML object with a string constructed from the given name.", null, new CodeTemplateFunctionDefinition("setLocalName", new Parameters("name", "String", "The local name to set."), "void", ""), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Set Name", "The setName method replaces the name of this XML object with a QName or AttributeName constructed from the given name.", null, new CodeTemplateFunctionDefinition("setName", new Parameters("name", "String/QName", "The name to set."), "void", ""), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Set Namespace", "The setNamespace method replaces the namespace associated with the name of this XML object with the given namespace.", null, new CodeTemplateFunctionDefinition("setNamespace", new Parameters("ns", "Namespace/String", "The namespace to set."), "void", ""), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Get Text", "The text method returns an XMLList containing all XML properties of this XML object that represent XML text nodes.", null, new CodeTemplateFunctionDefinition("text", new Parameters(), "XMLList", "A list of all text nodes."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "To String", "The toString method returns a string representation of this XML object.", null, new CodeTemplateFunctionDefinition("toString", new Parameters(), "String", "The string representation of this XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "To XML String", "The toXMLString() method returns an XML encoded string representation of this XML object. Unlike the toString method, toXMLString provides no special treatment for XML objects that contain only XML text nodes (i.e., primitive values). The toXMLString method always includes the start tag, attributes and end tag of the XML object regardless of its content. It is provided for cases when the default XML to string conversion rules are not desired.", null, new CodeTemplateFunctionDefinition("toXMLString", new Parameters(), "String", "The XML-encoded string representation of this XML object."), beforeDotTextList));
        addReference(new FunctionReference(CONTEXT_GLOBAL, null, null, "Value Of", "The valueOf method returns this XML object.", null, new CodeTemplateFunctionDefinition("valueOf", new Parameters(), "XML", "This XML object."), beforeDotTextList));
    }

    private void addUserutilReferences() {
        populateAliases();

        if (Thread.currentThread().getContextClassLoader() instanceof URLClassLoader) {
            URLClassLoader classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
            for (URL url : classLoader.getURLs()) {
                String urlString = url.toString();
                if (StringUtils.endsWithIgnoreCase(urlString, "userutil-sources.jar")) {
                    addUserutilReferences(url);
                }
            }
        }
    }

    private void addUserutilReferences(URL url) {
        InputStream inputStream = null;

        try {
            inputStream = url.openStream();
            ZipInputStream zis = new ZipInputStream(inputStream);
            ZipEntry zipEntry;
            Map<String, InputStream> entryMap = new HashMap<String, InputStream>();

            // Iterate through each entry in the JAR file
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (!zipEntry.isDirectory() && StringUtils.endsWithIgnoreCase(zipEntry.getName(), ".java")) {
                    entryMap.put(zipEntry.getName(), new ByteArrayInputStream(IOUtils.toByteArray(zis)));
                }
            }

            for (Entry<String, InputStream> entry : entryMap.entrySet()) {
                try {
                    // Parse the source file
                    CompilationUnit compilationUnit = JavaParser.parse(entry.getValue());
                    // Determine any runtime aliases for the class
                    List<String> inputTextList = aliasMap.get(entry.getKey().replaceAll("\\.java$", "").replace('/', '.'));
                    // Create and add references for the parsed source file
                    addReferences(ClassVisitor.getReferencesByCompilationUnit(compilationUnit, inputTextList));
                } catch (Exception e) {
                    logger.error("Unable to load references from userutil entry " + entry.getKey(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while scanning for userutil references.", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private void populateAliases() {
        aliasMap.put("com.mirth.connect.userutil.ImmutableConnectorMessage", Collections.singletonList("connectorMessage"));
        aliasMap.put("com.mirth.connect.userutil.ImmutableMessage", Collections.singletonList("message"));
        aliasMap.put("com.mirth.connect.userutil.Status", Collections.singletonList("responseStatus"));
        aliasMap.put("com.mirth.connect.server.userutil.ImmutableResponse", Collections.singletonList("response"));
        aliasMap.put("com.mirth.connect.server.userutil.AlertSender", Collections.singletonList("alerts"));
        aliasMap.put("com.mirth.connect.server.userutil.VMRouter", Collections.singletonList("router"));
        aliasMap.put("com.mirth.connect.server.userutil.DestinationSet", Collections.singletonList("destinationSet"));
        aliasMap.put("com.mirth.connect.server.userutil.ContextFactory", Collections.singletonList("contextFactory"));
        aliasMap.put("java.util.HashMap", Arrays.asList(new String[] { "connectorMap", "channelMap",
                "sourceMap", "globalMap", "configurationMap", "globalChannelMap", "responseMap" }));
    }

    private String getDataTypesToolTipText() {
        StringBuilder builder = new StringBuilder("The available data type keys are:<br/><br/>");
        for (String key : LoadedExtensions.getInstance().getDataTypePlugins().keySet()) {
            builder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            builder.append(key);
            builder.append("<br/>");
        }
        return builder.toString();
    }

    private class CategoryComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            if (o1 == null) {
                if (o2 == null) {
                    return 0;
                } else {
                    return -1;
                }
            } else if (o2 == null) {
                return 1;
            }

            for (Category category : Category.values()) {
                if (category.toString().equals(o1)) {
                    if (category.toString().equals(o2)) {
                        return 0;
                    } else {
                        return -1;
                    }
                } else if (category.toString().equals(o2)) {
                    return 1;
                }
            }

            return o1.compareTo(o2);
        }
    }
}