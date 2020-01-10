/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.servlets;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataParam;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Operation.ExecuteType;
import com.mirth.connect.client.core.Permissions;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;
import com.mirth.connect.client.core.api.providers.MetaDataSearchParamConverterProvider.MetaDataSearch;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.model.MessageImportResult;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.util.messagewriter.EncryptionType;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

@Path("/channels")
@Tag(name = "Messages")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface MessageServletInterface extends BaseServletInterface {

    @POST
    @Path("/{channelId}/messages")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(summary = "Processes a new message through a channel.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "long", ref = "../apiexamples/long_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "long", ref = "../apiexamples/long_json") }) })
    @MirthOperation(name = "processMessages", display = "Process messages", permission = Permissions.MESSAGES_PROCESS, type = ExecuteType.ASYNC)
    public Long processMessage(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("rawData") @Parameter(description = "The raw message data to process.", required = true) String rawData,
            @Param("destinationMetaDataIds") @Parameter(description = "Indicates which destinations to send the message to.") @QueryParam("destinationMetaDataId") Set<Integer> destinationMetaDataIds,
            @Param("sourceMapEntries") @Parameter(description = "These entries will be injected into the source map for the message. Value should be in the format: key=value") @QueryParam("sourceMapEntry") Set<String> sourceMapEntries,
            @Param("overwrite") @Parameter(description = "If true and a valid original message ID is given, this message will overwrite the existing one.") @QueryParam("overwrite") boolean overwrite,
            @Param("imported") @Parameter(description = "If true, marks this message as being imported. If the message is overwriting an existing one, then statistics will not be decremented.") @QueryParam("imported") boolean imported,
            @Param("originalMessageId") @Parameter(description = "The original message ID this message is associated with.") @QueryParam("originalMessageId") Long originalMessageId) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messagesWithObj")
    @Operation(summary = "Processes a new message through a channel, using the RawMessage object.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "long", ref = "../apiexamples/long_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "long", ref = "../apiexamples/long_json") }) })
    @MirthOperation(name = "processMessages", display = "Process messages", permission = Permissions.MESSAGES_PROCESS, type = ExecuteType.ASYNC)
    public Long processMessage(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("rawMessage") @RequestBody(description = "The RawMessage object to process.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "rawMessage", ref = "../apiexamples/raw_message_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "rawMessage", ref = "../apiexamples/raw_message_json") }) }) RawMessage rawMessage) throws ClientException;
    // @formatter:on

    @GET
    @Path("/{channelId}/messages/{messageId}")
    @Operation(summary = "Retrieve a message by ID.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "message", ref = "../apiexamples/message_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "message", ref = "../apiexamples/message_json") }) })
    @MirthOperation(name = "getMessageContent", display = "Get message content", permission = Permissions.MESSAGES_VIEW, type = ExecuteType.ASYNC)
    public Message getMessageContent(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("messageId") @Parameter(description = "The ID of the message.", required = true) @PathParam("messageId") Long messageId,
            @Param("metaDataIds") @Parameter(description = "The metadata IDs of the connectors.") @QueryParam("metaDataId") List<Integer> metaDataIds) throws ClientException;
    // @formatter:on

    @GET
    @Path("/{channelId}/messages/{messageId}/attachments")
    @Operation(summary = "Retrieve a list of attachments by message ID.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "attachmentList", ref = "../apiexamples/attachment_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "attachmentList", ref = "../apiexamples/attachment_list_json") }) })
    @MirthOperation(name = "getAttachmentsByMessageId", display = "Get attachments by message ID", permission = Permissions.MESSAGES_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public List<Attachment> getAttachmentsByMessageId(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("messageId") @Parameter(description = "The ID of the message.", required = true) @PathParam("messageId") Long messageId,
            @Param("includeContent") @Parameter(description = "If false, only the attachment ID and type will be returned.", schema = @Schema(defaultValue = "true")) @QueryParam("includeContent") boolean includeContent) throws ClientException;
    // @formatter:on

    @GET
    @Path("/{channelId}/messages/{messageId}/attachments/{attachmentId}")
    @Operation(summary = "Retrieve a message attachment by ID.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "attachment", ref = "../apiexamples/attachment_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "attachment", ref = "../apiexamples/attachment_json") }) })
    @MirthOperation(name = "getAttachment", display = "Get attachment", permission = Permissions.MESSAGES_VIEW, type = ExecuteType.ASYNC)
    public Attachment getAttachment(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("messageId") @Parameter(description = "The ID of the message.", required = true) @PathParam("messageId") Long messageId,
            @Param("attachmentId") @Parameter(description = "The ID of the attachment.", required = true) @PathParam("attachmentId") String attachmentId) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/{messageId}/_getDICOMMessage")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Given a ConnectorMessage object, reattaches any DICOM attachment data and returns the raw Base64 encoded message data.")
    @MirthOperation(name = "getDICOMMessage", display = "Get DICOM message", permission = Permissions.MESSAGES_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public String getDICOMMessage(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("messageId") @Parameter(description = "The ID of the message.", required = true) @PathParam("messageId") Long messageId,
            @Param("message") @RequestBody(description = "The ConnectorMessage to retrieve DICOM data for.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "connectorMessage", ref = "../apiexamples/connector_message_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "connectorMessage", ref = "../apiexamples/connector_message_json") }) }) ConnectorMessage message) throws ClientException;
    // @formatter:on

    @GET
    @Path("/{channelId}/messages/maxMessageId")
    @Operation(summary = "Returns the maximum message ID for the given channel.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "messageId", ref = "../apiexamples/long_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "messageId", ref = "../apiexamples/long_json") }) })
    @MirthOperation(name = "getMaxMessageId", display = "Get max messageId", permission = Permissions.MESSAGES_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public Long getMaxMessageId(@Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId) throws ClientException;

    @POST
    @Path("/{channelId}/messages/_search")
    @Operation(summary = "Search for messages by specific filter criteria.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "messages", ref = "../apiexamples/message_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "messages", ref = "../apiexamples/message_list_json") }) })
    @MirthOperation(name = "searchMessages", display = "Get messages by page limit", permission = Permissions.MESSAGES_VIEW, abortable = true)
    public List<Message> getMessages(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId, 
            @Param("filter") @RequestBody(description = "The MessageFilter object to use to query messages by.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "filter", ref = "../apiexamples/message_filter_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "filter", ref = "../apiexamples/message_filter_json") }) }) MessageFilter filter, 
            @Param("includeContent") @Parameter(description = "If true, message content will be returned with the results.", schema = @Schema(defaultValue = "false")) @QueryParam("includeContent") Boolean includeContent, 
            @Param("offset") @Parameter(description = "Used for pagination, determines where to start in the search results.", schema = @Schema(defaultValue = "0")) @QueryParam("offset") Integer offset, 
            @Param("limit") @Parameter(description = "Used for pagination, determines the maximum number of results to return.", schema = @Schema(defaultValue = "20")) @QueryParam("limit") Integer limit) throws ClientException;
    // @formatter:on

    @GET
    @Path("/{channelId}/messages")
    @Operation(summary = "Search for messages by specific filter criteria.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "messages", ref = "../apiexamples/message_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "messages", ref = "../apiexamples/message_list_json") }) })
    @MirthOperation(name = "searchMessages", display = "Get messages by page limit", permission = Permissions.MESSAGES_VIEW, abortable = true)
    public List<Message> getMessages(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("minMessageId") @Parameter(description = "The minimum message ID to query.") @QueryParam("minMessageId") Long minMessageId,
            @Param("maxMessageId") @Parameter(description = "The maximum message ID to query.") @QueryParam("maxMessageId") Long maxMessageId,
            @Param("minOriginalId") @Parameter(description = "The minimum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("minOriginalId") Long minOriginalId,
            @Param("maxOriginalId") @Parameter(description = "The maximum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("maxOriginalId") Long maxOriginalId,
            @Param("minImportId") @Parameter(description = "The minimum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("minImportId") Long minImportId,
            @Param("maxImportId") @Parameter(description = "The maximum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("maxImportId") Long maxImportId,
            @Param("startDate") @Parameter(description = "The earliest original received date to query by. Example: 1985-10-26T09:00:00.000-0700") @QueryParam("startDate") Calendar startDate,
            @Param("endDate") @Parameter(description = "The latest original received date to query by. Example: 2015-10-21T07:28:00.000-0700") @QueryParam("endDate") Calendar endDate,
            @Param("textSearch") @Parameter(description = "Searches all message content for this string. This process could take a long time depending on the amount of message content currently stored. Any message content that was encrypted by this channel will not be searchable.") @QueryParam("textSearch") String textSearch,
            @Param("textSearchRegex") @Parameter(description = "If true, text search input will be considered a regular expression pattern to be matched. Only supported by PostgreSQL, MySQL and Oracle databases.") @QueryParam("textSearchRegex") Boolean textSearchRegex,
            @Param("statuses") @Parameter(description = "Determines which message statuses to query by.") @QueryParam("status") Set<Status> statuses,
            @Param("includedMetaDataIds") @Parameter(description = "If present, only connector metadata IDs in this list will be queried.") @QueryParam("includedMetaDataId") Set<Integer> includedMetaDataIds,
            @Param("excludedMetaDataIds") @Parameter(description = "If present, connector metadata IDs in this list will not be queried.") @QueryParam("excludedMetaDataId") Set<Integer> excludedMetaDataIds,
            @Param("serverId") @Parameter(description = "The server ID associated with messages.") @QueryParam("serverId") String serverId,
            @Param("rawContentSearches") @Parameter(description = "Searches the raw content of messages.") @QueryParam("rawContentSearch") Set<String> rawContentSearches,
            @Param("processedRawContentSearches") @Parameter(description = "Searches the processed raw content of messages.") @QueryParam("processedRawContentSearch") Set<String> processedRawContentSearches,
            @Param("transformedContentSearches") @Parameter(description = "Searches the transformed content of messages.") @QueryParam("transformedContentSearch") Set<String> transformedContentSearches,
            @Param("encodedContentSearches") @Parameter(description = "Searches the encoded content of messages.") @QueryParam("encodedContentSearch") Set<String> encodedContentSearches,
            @Param("sentContentSearches") @Parameter(description = "Searches the sent content of messages.") @QueryParam("sentContentSearch") Set<String> sentContentSearches,
            @Param("responseContentSearches") @Parameter(description = "Searches the response content of messages.") @QueryParam("responseContentSearch") Set<String> responseContentSearches,
            @Param("responseTransformedContentSearches") @Parameter(description = "Searches the response transformed content of messages.") @QueryParam("responseTransformedContentSearch") Set<String> responseTransformedContentSearches,
            @Param("processedResponseContentSearches") @Parameter(description = "Searches the processed response content of messages.") @QueryParam("processedResponseContentSearch") Set<String> processedResponseContentSearches,
            @Param("connectorMapContentSearches") @Parameter(description = "Searches the connector map content of messages.") @QueryParam("connectorMapContentSearch") Set<String> connectorMapContentSearches,
            @Param("channelMapContentSearches") @Parameter(description = "Searches the channel map content of messages.") @QueryParam("channelMapContentSearch") Set<String> channelMapContentSearches,
            @Param("sourceMapContentSearches") @Parameter(description = "Searches the source map content of messages.") @QueryParam("sourceMapContentSearch") Set<String> sourceMapContentSearches,
            @Param("responseMapContentSearches") @Parameter(description = "Searches the response map content of messages.") @QueryParam("responseMapContentSearch") Set<String> responseMapContentSearches,
            @Param("processingErrorContentSearches") @Parameter(description = "Searches the processing error content of messages.") @QueryParam("processingErrorContentSearch") Set<String> processingErrorContentSearches,
            @Param("postprocessorErrorContentSearches") @Parameter(description = "Searches the postprocessor error content of messages.") @QueryParam("postprocessorErrorContentSearch") Set<String> postprocessorErrorContentSearches,
            @Param("responseErrorContentSearches") @Parameter(description = "Searches the response error content of messages.") @QueryParam("responseErrorContentSearch") Set<String> responseErrorContentSearches,
            @Param("metaDataSearches") @Parameter(description = "Searches a custom metadata column. Value should be in the form: COLUMN_NAME &lt;operator&gt; value, where operator is one of the following: =, !=, <, <=, >, >=, CONTAINS, DOES NOT CONTAIN, STARTS WITH, DOES NOT START WITH, ENDS WITH, DOES NOT END WITH") @QueryParam("metaDataSearch") Set<MetaDataSearch> metaDataSearches,
            @Param("metaDataCaseInsensitiveSearches") @Parameter(description = "Searches a custom metadata column, ignoring case. Value should be in the form: COLUMN_NAME &lt;operator&gt; value.") @QueryParam("metaDataCaseInsensitiveSearch") Set<MetaDataSearch> metaDataCaseInsensitiveSearches,
            @Param("textSearchMetaDataColumns") @Parameter(description = "When using a text search, these custom metadata columns will also be searched.") @QueryParam("textSearchMetaDataColumn") Set<String> textSearchMetaDataColumns,
            @Param("minSendAttempts") @Parameter(description = "The minimum number of send attempts for connector messages.") @QueryParam("minSendAttempts") Integer minSendAttempts,
            @Param("maxSendAttempts") @Parameter(description = "The maximum number of send attempts for connector messages.") @QueryParam("maxSendAttempts") Integer maxSendAttempts,
            @Param("attachment") @Parameter(description = "If true, only messages with attachments are included in the results.") @QueryParam("attachment") Boolean attachment,
            @Param("error") @Parameter(description = "If true, only messages with errors are included in the results.") @QueryParam("error") Boolean error,
            @Param("includeContent") @Parameter(description = "If true, message content will be returned with the results.", schema = @Schema(defaultValue = "false")) @QueryParam("includeContent") Boolean includeContent,
            @Param("offset") @Parameter(description = "Used for pagination, determines where to start in the search results.", schema = @Schema(defaultValue = "0")) @QueryParam("offset") Integer offset,
            @Param("limit") @Parameter(description = "Used for pagination, determines the maximum number of results to return.", schema = @Schema(defaultValue = "20")) @QueryParam("limit") Integer limit) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/count/_search")
    @Operation(summary = "Count number for messages by specific filter criteria.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "messageCount", ref = "../apiexamples/long_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "messageCount", ref = "../apiexamples/long_json") }) })
    @MirthOperation(name = "getSearchCount", display = "Get search results count", permission = Permissions.MESSAGES_VIEW, abortable = true)
    public Long getMessageCount(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("filter") @RequestBody(description = "The MessageFilter object to use to query messages by.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "filter", ref = "../apiexamples/message_filter_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "filter", ref = "../apiexamples/message_filter_json") }) }) MessageFilter filter) throws ClientException;
    // @formatter:off
    
    @GET
    @Path("/{channelId}/messages/count")
    @Operation(summary="Count number for messages by specific filter criteria.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "messageCount", ref = "../apiexamples/long_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "messageCount", ref = "../apiexamples/long_json") }) })
    @MirthOperation(name = "getSearchCount", display = "Get search results count", permission = Permissions.MESSAGES_VIEW, abortable = true)
    public Long getMessageCount(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("minMessageId") @Parameter(description = "The minimum message ID to query.") @QueryParam("minMessageId") Long minMessageId,
            @Param("maxMessageId") @Parameter(description = "The maximum message ID to query.") @QueryParam("maxMessageId") Long maxMessageId,
            @Param("minOriginalId") @Parameter(description = "The minimum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("minOriginalId") Long minOriginalId,
            @Param("maxOriginalId") @Parameter(description = "The maximum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("maxOriginalId") Long maxOriginalId,
            @Param("minImportId") @Parameter(description = "The minimum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("minImportId") Long minImportId,
            @Param("maxImportId") @Parameter(description = "The maximum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("maxImportId") Long maxImportId,
            @Param("startDate") @Parameter(description = "The earliest original received date to query by. Example: 1985-10-26T09:00:00.000-0700") @QueryParam("startDate") Calendar startDate,
            @Param("endDate") @Parameter(description = "The latest original received date to query by. Example: 2015-10-21T07:28:00.000-0700") @QueryParam("endDate") Calendar endDate,
            @Param("textSearch") @Parameter(description = "Searches all message content for this string. This process could take a long time depending on the amount of message content currently stored. Any message content that was encrypted by this channel will not be searchable.") @QueryParam("textSearch") String textSearch,
            @Param("textSearchRegex") @Parameter(description = "If true, text search input will be considered a regular expression pattern to be matched. Only supported by PostgreSQL, MySQL and Oracle databases.") @QueryParam("textSearchRegex") Boolean textSearchRegex,
            @Param("statuses") @Parameter(description = "Determines which message statuses to query by.") @QueryParam("status") Set<Status> statuses,
            @Param("includedMetaDataIds") @Parameter(description = "If present, only connector metadata IDs in this list will be queried.") @QueryParam("includedMetaDataId") Set<Integer> includedMetaDataIds,
            @Param("excludedMetaDataIds") @Parameter(description = "If present, connector metadata IDs in this list will not be queried.") @QueryParam("excludedMetaDataId") Set<Integer> excludedMetaDataIds,
            @Param("serverId") @Parameter(description = "The server ID associated with messages.") @QueryParam("serverId") String serverId,
            @Param("rawContentSearches") @Parameter(description = "Searches the raw content of messages.") @QueryParam("rawContentSearch") Set<String> rawContentSearches,
            @Param("processedRawContentSearches") @Parameter(description = "Searches the processed raw content of messages.") @QueryParam("processedRawContentSearch") Set<String> processedRawContentSearches,
            @Param("transformedContentSearches") @Parameter(description = "Searches the transformed content of messages.") @QueryParam("transformedContentSearch") Set<String> transformedContentSearches,
            @Param("encodedContentSearches") @Parameter(description = "Searches the encoded content of messages.") @QueryParam("encodedContentSearch") Set<String> encodedContentSearches,
            @Param("sentContentSearches") @Parameter(description = "Searches the sent content of messages.") @QueryParam("sentContentSearch") Set<String> sentContentSearches,
            @Param("responseContentSearches") @Parameter(description = "Searches the response content of messages.") @QueryParam("responseContentSearch") Set<String> responseContentSearches,
            @Param("responseTransformedContentSearches") @Parameter(description = "Searches the response transformed content of messages.") @QueryParam("responseTransformedContentSearch") Set<String> responseTransformedContentSearches,
            @Param("processedResponseContentSearches") @Parameter(description = "Searches the processed response content of messages.") @QueryParam("processedResponseContentSearch") Set<String> processedResponseContentSearches,
            @Param("connectorMapContentSearches") @Parameter(description = "Searches the connector map content of messages.") @QueryParam("connectorMapContentSearch") Set<String> connectorMapContentSearches,
            @Param("channelMapContentSearches") @Parameter(description = "Searches the channel map content of messages.") @QueryParam("channelMapContentSearch") Set<String> channelMapContentSearches,
            @Param("sourceMapContentSearches") @Parameter(description = "Searches the source map content of messages.") @QueryParam("sourceMapContentSearch") Set<String> sourceMapContentSearches,
            @Param("responseMapContentSearches") @Parameter(description = "Searches the response map content of messages.") @QueryParam("responseMapContentSearch") Set<String> responseMapContentSearches,
            @Param("processingErrorContentSearches") @Parameter(description = "Searches the processing error content of messages.") @QueryParam("processingErrorContentSearch") Set<String> processingErrorContentSearches,
            @Param("postprocessorErrorContentSearches") @Parameter(description = "Searches the postprocessor error content of messages.") @QueryParam("postprocessorErrorContentSearch") Set<String> postprocessorErrorContentSearches,
            @Param("responseErrorContentSearches") @Parameter(description = "Searches the response error content of messages.") @QueryParam("responseErrorContentSearch") Set<String> responseErrorContentSearches,
            @Param("metaDataSearches") @Parameter(description = "Searches a custom metadata column. Value should be in the form: COLUMN_NAME &lt;operator&gt; value, where operator is one of the following: =, !=, <, <=, >, >=, CONTAINS, DOES NOT CONTAIN, STARTS WITH, DOES NOT START WITH, ENDS WITH, DOES NOT END WITH") @QueryParam("metaDataSearch") Set<MetaDataSearch> metaDataSearches,
            @Param("metaDataCaseInsensitiveSearches") @Parameter(description = "Searches a custom metadata column, ignoring case. Value should be in the form: COLUMN_NAME &lt;operator&gt; value.") @QueryParam("metaDataCaseInsensitiveSearch") Set<MetaDataSearch> metaDataCaseInsensitiveSearches,
            @Param("textSearchMetaDataColumns") @Parameter(description = "When using a text search, these custom metadata columns will also be searched.") @QueryParam("textSearchMetaDataColumn") Set<String> textSearchMetaDataColumns,
            @Param("minSendAttempts") @Parameter(description = "The minimum number of send attempts for connector messages.") @QueryParam("minSendAttempts") Integer minSendAttempts,
            @Param("maxSendAttempts") @Parameter(description = "The maximum number of send attempts for connector messages.") @QueryParam("maxSendAttempts") Integer maxSendAttempts,
            @Param("attachment") @Parameter(description = "If true, only messages with attachments are included in the results.") @QueryParam("attachment") Boolean attachment,
            @Param("error") @Parameter(description = "If true, only messages with errors are included in the results.") @QueryParam("error") Boolean error) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/_reprocessWithFilter")
    @Operation(summary = "Reprocesses messages through a channel filtering with a MessageFilter.")
    @MirthOperation(name = "reprocessMessages", display = "Reprocess messages", permission = Permissions.MESSAGES_REPROCESS_RESULTS, type = ExecuteType.ASYNC)
    public void reprocessMessages(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("filter") @RequestBody(description = "The MessageFilter object to use to query messages by.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "filter", ref = "../apiexamples/message_filter_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "filter", ref = "../apiexamples/message_filter_json") }) }) MessageFilter filter,
            @Param("replace") @Parameter(description = "If true, the message will overwrite the current one", schema = @Schema(defaultValue = "false")) @QueryParam("replace") boolean replace,
            @Param("filterDestinations") @Parameter(description = "If true, the metaDataId parameter will be used to determine which destinations to reprocess the message through.", schema = @Schema(defaultValue = "false")) @QueryParam("filterDestinations") boolean filterDestinations,
            @Param("reprocessMetaDataIds") @Parameter(description = "Indicates which destinations to send the message to.") @QueryParam("metaDataId") Set<Integer> reprocessMetaDataIds) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/_reprocess")
    @Consumes("")
    @Operation(summary = "Reprocesses messages through a channel by specific filter criteria.")
    @MirthOperation(name = "reprocessMessages", display = "Reprocess messages", permission = Permissions.MESSAGES_REPROCESS_RESULTS, type = ExecuteType.ASYNC)
    public void reprocessMessages(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("minMessageId") @Parameter(description = "The minimum message ID to query.") @QueryParam("minMessageId") Long minMessageId,
            @Param("maxMessageId") @Parameter(description = "The maximum message ID to query.") @QueryParam("maxMessageId") Long maxMessageId,
            @Param("minOriginalId") @Parameter(description = "The minimum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("minOriginalId") Long minOriginalId,
            @Param("maxOriginalId") @Parameter(description = "The maximum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("maxOriginalId") Long maxOriginalId,
            @Param("minImportId") @Parameter(description = "The minimum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("minImportId") Long minImportId,
            @Param("maxImportId") @Parameter(description = "The maximum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("maxImportId") Long maxImportId,
            @Param("startDate") @Parameter(description = "The earliest original received date to query by. Example: 1985-10-26T09:00:00.000-0700") @QueryParam("startDate") Calendar startDate,
            @Param("endDate") @Parameter(description = "The latest original received date to query by. Example: 2015-10-21T07:28:00.000-0700") @QueryParam("endDate") Calendar endDate,
            @Param("textSearch") @Parameter(description = "Searches all message content for this string. This process could take a long time depending on the amount of message content currently stored. Any message content that was encrypted by this channel will not be searchable.") @QueryParam("textSearch") String textSearch,
            @Param("textSearchRegex") @Parameter(description = "If true, text search input will be considered a regular expression pattern to be matched. Only supported by PostgreSQL, MySQL and Oracle databases.") @QueryParam("textSearchRegex") Boolean textSearchRegex,
            @Param("statuses") @Parameter(description = "Determines which message statuses to query by.") @QueryParam("status") Set<Status> statuses,
            @Param("includedMetaDataIds") @Parameter(description = "If present, only connector metadata IDs in this list will be queried.") @QueryParam("includedMetaDataId") Set<Integer> includedMetaDataIds,
            @Param("excludedMetaDataIds") @Parameter(description = "If present, connector metadata IDs in this list will not be queried.") @QueryParam("excludedMetaDataId") Set<Integer> excludedMetaDataIds,
            @Param("serverId") @Parameter(description = "The server ID associated with messages.") @QueryParam("serverId") String serverId,
            @Param("rawContentSearches") @Parameter(description = "Searches the raw content of messages.") @QueryParam("rawContentSearch") Set<String> rawContentSearches,
            @Param("processedRawContentSearches") @Parameter(description = "Searches the processed raw content of messages.") @QueryParam("processedRawContentSearch") Set<String> processedRawContentSearches,
            @Param("transformedContentSearches") @Parameter(description = "Searches the transformed content of messages.") @QueryParam("transformedContentSearch") Set<String> transformedContentSearches,
            @Param("encodedContentSearches") @Parameter(description = "Searches the encoded content of messages.") @QueryParam("encodedContentSearch") Set<String> encodedContentSearches,
            @Param("sentContentSearches") @Parameter(description = "Searches the sent content of messages.") @QueryParam("sentContentSearch") Set<String> sentContentSearches,
            @Param("responseContentSearches") @Parameter(description = "Searches the response content of messages.") @QueryParam("responseContentSearch") Set<String> responseContentSearches,
            @Param("responseTransformedContentSearches") @Parameter(description = "Searches the response transformed content of messages.") @QueryParam("responseTransformedContentSearch") Set<String> responseTransformedContentSearches,
            @Param("processedResponseContentSearches") @Parameter(description = "Searches the processed response content of messages.") @QueryParam("processedResponseContentSearch") Set<String> processedResponseContentSearches,
            @Param("connectorMapContentSearches") @Parameter(description = "Searches the connector map content of messages.") @QueryParam("connectorMapContentSearch") Set<String> connectorMapContentSearches,
            @Param("channelMapContentSearches") @Parameter(description = "Searches the channel map content of messages.") @QueryParam("channelMapContentSearch") Set<String> channelMapContentSearches,
            @Param("sourceMapContentSearches") @Parameter(description = "Searches the source map content of messages.") @QueryParam("sourceMapContentSearch") Set<String> sourceMapContentSearches,
            @Param("responseMapContentSearches") @Parameter(description = "Searches the response map content of messages.") @QueryParam("responseMapContentSearch") Set<String> responseMapContentSearches,
            @Param("processingErrorContentSearches") @Parameter(description = "Searches the processing error content of messages.") @QueryParam("processingErrorContentSearch") Set<String> processingErrorContentSearches,
            @Param("postprocessorErrorContentSearches") @Parameter(description = "Searches the postprocessor error content of messages.") @QueryParam("postprocessorErrorContentSearch") Set<String> postprocessorErrorContentSearches,
            @Param("responseErrorContentSearches") @Parameter(description = "Searches the response error content of messages.") @QueryParam("responseErrorContentSearch") Set<String> responseErrorContentSearches,
            @Param("metaDataSearches") @Parameter(description = "Searches a custom metadata column. Value should be in the form: COLUMN_NAME &lt;operator&gt; value, where operator is one of the following: =, !=, <, <=, >, >=, CONTAINS, DOES NOT CONTAIN, STARTS WITH, DOES NOT START WITH, ENDS WITH, DOES NOT END WITH") @QueryParam("metaDataSearch") Set<MetaDataSearch> metaDataSearches,
            @Param("metaDataCaseInsensitiveSearches") @Parameter(description = "Searches a custom metadata column, ignoring case. Value should be in the form: COLUMN_NAME &lt;operator&gt; value.") @QueryParam("metaDataCaseInsensitiveSearch") Set<MetaDataSearch> metaDataCaseInsensitiveSearches,
            @Param("textSearchMetaDataColumns") @Parameter(description = "When using a text search, these custom metadata columns will also be searched.") @QueryParam("textSearchMetaDataColumn") Set<String> textSearchMetaDataColumns,
            @Param("minSendAttempts") @Parameter(description = "The minimum number of send attempts for connector messages.") @QueryParam("minSendAttempts") Integer minSendAttempts,
            @Param("maxSendAttempts") @Parameter(description = "The maximum number of send attempts for connector messages.") @QueryParam("maxSendAttempts") Integer maxSendAttempts,
            @Param("attachment") @Parameter(description = "If true, only messages with attachments are included in the results.") @QueryParam("attachment") Boolean attachment,
            @Param("error") @Parameter(description = "If true, only messages with errors are included in the results.") @QueryParam("error") Boolean error,
            @Param("replace") @Parameter(description = "If true, the message will overwrite the current one", schema = @Schema(defaultValue = "false")) @QueryParam("replace") boolean replace,
            @Param("filterDestinations") @Parameter(description = "If true, the metaDataId parameter will be used to determine which destinations to reprocess the message through.", schema = @Schema(defaultValue = "false")) @QueryParam("filterDestinations") boolean filterDestinations,
            @Param("reprocessMetaDataIds") @Parameter(description = "Indicates which destinations to send the message to.") @QueryParam("metaDataId") Set<Integer> reprocessMetaDataIds) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/{messageId}/_reprocess")
    @Operation(summary = "Reprocesses and overwrites a single message.")
    @MirthOperation(name = "reprocessMessage", display = "Reprocess messages", permission = Permissions.MESSAGES_REPROCESS, type = ExecuteType.ASYNC)
    public void reprocessMessage(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("messageId") @Parameter(description = "The ID of the message.", required = true) @PathParam("messageId") Long messageId,
            @Param("replace") @Parameter(description = "If true, the message will overwrite the current one", schema = @Schema(defaultValue = "false")) @QueryParam("replace") boolean replace,
            @Param("filterDestinations") @Parameter(description = "If true, the metaDataId parameter will be used to determine which destinations to reprocess the message through.", schema = @Schema(defaultValue = "false")) @QueryParam("filterDestinations") boolean filterDestinations,
            @Param("reprocessMetaDataIds") @Parameter(description = "Indicates which destinations to send the message to.") @QueryParam("metaDataId") Set<Integer> reprocessMetaDataIds) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/_remove")
    @Operation(summary = "Remove messages by specific filter criteria.")
    @MirthOperation(name = "removeMessages", display = "Remove messages", permission = Permissions.MESSAGES_REMOVE_RESULTS, abortable = true)
    public void removeMessages(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("filter") @RequestBody(description = "The MessageFilter object to use to query messages by.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "filter", ref = "../apiexamples/message_filter_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "filter", ref = "../apiexamples/message_filter_json") }) }) MessageFilter filter) throws ClientException;
    // @formatter:on

    @DELETE
    @Path("/{channelId}/messages")
    @Consumes("")
    @Operation(summary = "Remove messages by specific filter criteria.")
    @MirthOperation(name = "removeMessages", display = "Remove messages", permission = Permissions.MESSAGES_REMOVE_RESULTS, abortable = true)
    public void removeMessages(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("minMessageId") @Parameter(description = "The minimum message ID to query.") @QueryParam("minMessageId") Long minMessageId,
            @Param("maxMessageId") @Parameter(description = "The maximum message ID to query.") @QueryParam("maxMessageId") Long maxMessageId,
            @Param("minOriginalId") @Parameter(description = "The minimum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("minOriginalId") Long minOriginalId,
            @Param("maxOriginalId") @Parameter(description = "The maximum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("maxOriginalId") Long maxOriginalId,
            @Param("minImportId") @Parameter(description = "The minimum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("minImportId") Long minImportId,
            @Param("maxImportId") @Parameter(description = "The maximum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("maxImportId") Long maxImportId,
            @Param("startDate") @Parameter(description = "The earliest original received date to query by. Example: 1985-10-26T09:00:00.000-0700") @QueryParam("startDate") Calendar startDate,
            @Param("endDate") @Parameter(description = "The latest original received date to query by. Example: 2015-10-21T07:28:00.000-0700") @QueryParam("endDate") Calendar endDate,
            @Param("textSearch") @Parameter(description = "Searches all message content for this string. This process could take a long time depending on the amount of message content currently stored. Any message content that was encrypted by this channel will not be searchable.") @QueryParam("textSearch") String textSearch,
            @Param("textSearchRegex") @Parameter(description = "If true, text search input will be considered a regular expression pattern to be matched. Only supported by PostgreSQL, MySQL and Oracle databases.") @QueryParam("textSearchRegex") Boolean textSearchRegex,
            @Param("statuses") @Parameter(description = "Determines which message statuses to query by.") @QueryParam("status") Set<Status> statuses,
            @Param("includedMetaDataIds") @Parameter(description = "If present, only connector metadata IDs in this list will be queried.") @QueryParam("includedMetaDataId") Set<Integer> includedMetaDataIds,
            @Param("excludedMetaDataIds") @Parameter(description = "If present, connector metadata IDs in this list will not be queried.") @QueryParam("excludedMetaDataId") Set<Integer> excludedMetaDataIds,
            @Param("serverId") @Parameter(description = "The server ID associated with messages.") @QueryParam("serverId") String serverId,
            @Param("rawContentSearches") @Parameter(description = "Searches the raw content of messages.") @QueryParam("rawContentSearch") Set<String> rawContentSearches,
            @Param("processedRawContentSearches") @Parameter(description = "Searches the processed raw content of messages.") @QueryParam("processedRawContentSearch") Set<String> processedRawContentSearches,
            @Param("transformedContentSearches") @Parameter(description = "Searches the transformed content of messages.") @QueryParam("transformedContentSearch") Set<String> transformedContentSearches,
            @Param("encodedContentSearches") @Parameter(description = "Searches the encoded content of messages.") @QueryParam("encodedContentSearch") Set<String> encodedContentSearches,
            @Param("sentContentSearches") @Parameter(description = "Searches the sent content of messages.") @QueryParam("sentContentSearch") Set<String> sentContentSearches,
            @Param("responseContentSearches") @Parameter(description = "Searches the response content of messages.") @QueryParam("responseContentSearch") Set<String> responseContentSearches,
            @Param("responseTransformedContentSearches") @Parameter(description = "Searches the response transformed content of messages.") @QueryParam("responseTransformedContentSearch") Set<String> responseTransformedContentSearches,
            @Param("processedResponseContentSearches") @Parameter(description = "Searches the processed response content of messages.") @QueryParam("processedResponseContentSearch") Set<String> processedResponseContentSearches,
            @Param("connectorMapContentSearches") @Parameter(description = "Searches the connector map content of messages.") @QueryParam("connectorMapContentSearch") Set<String> connectorMapContentSearches,
            @Param("channelMapContentSearches") @Parameter(description = "Searches the channel map content of messages.") @QueryParam("channelMapContentSearch") Set<String> channelMapContentSearches,
            @Param("sourceMapContentSearches") @Parameter(description = "Searches the source map content of messages.") @QueryParam("sourceMapContentSearch") Set<String> sourceMapContentSearches,
            @Param("responseMapContentSearches") @Parameter(description = "Searches the response map content of messages.") @QueryParam("responseMapContentSearch") Set<String> responseMapContentSearches,
            @Param("processingErrorContentSearches") @Parameter(description = "Searches the processing error content of messages.") @QueryParam("processingErrorContentSearch") Set<String> processingErrorContentSearches,
            @Param("postprocessorErrorContentSearches") @Parameter(description = "Searches the postprocessor error content of messages.") @QueryParam("postprocessorErrorContentSearch") Set<String> postprocessorErrorContentSearches,
            @Param("responseErrorContentSearches") @Parameter(description = "Searches the response error content of messages.") @QueryParam("responseErrorContentSearch") Set<String> responseErrorContentSearches,
            @Param("metaDataSearches") @Parameter(description = "Searches a custom metadata column. Value should be in the form: COLUMN_NAME &lt;operator&gt; value, where operator is one of the following: =, !=, <, <=, >, >=, CONTAINS, DOES NOT CONTAIN, STARTS WITH, DOES NOT START WITH, ENDS WITH, DOES NOT END WITH") @QueryParam("metaDataSearch") Set<MetaDataSearch> metaDataSearches,
            @Param("metaDataCaseInsensitiveSearches") @Parameter(description = "Searches a custom metadata column, ignoring case. Value should be in the form: COLUMN_NAME &lt;operator&gt; value.") @QueryParam("metaDataCaseInsensitiveSearch") Set<MetaDataSearch> metaDataCaseInsensitiveSearches,
            @Param("textSearchMetaDataColumns") @Parameter(description = "When using a text search, these custom metadata columns will also be searched.") @QueryParam("textSearchMetaDataColumn") Set<String> textSearchMetaDataColumns,
            @Param("minSendAttempts") @Parameter(description = "The minimum number of send attempts for connector messages.") @QueryParam("minSendAttempts") Integer minSendAttempts,
            @Param("maxSendAttempts") @Parameter(description = "The maximum number of send attempts for connector messages.") @QueryParam("maxSendAttempts") Integer maxSendAttempts,
            @Param("attachment") @Parameter(description = "If true, only messages with attachments are included in the results.") @QueryParam("attachment") Boolean attachment,
            @Param("error") @Parameter(description = "If true, only messages with errors are included in the results.") @QueryParam("error") Boolean error) throws ClientException;
    // @formatter:on

    @DELETE
    @Path("/{channelId}/messages/{messageId}")
    @Operation(summary = "Remove a single message by ID.")
    @MirthOperation(name = "removeMessage", display = "Remove message", permission = Permissions.MESSAGES_REMOVE, abortable = true)
    public void removeMessage(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("messageId") @Parameter(description = "The ID of the message.", required = true) @PathParam("messageId") Long messageId,
            @Param("metaDataId") @Parameter(description = "If present, only the specific connector message will be removed. If the metadata ID is 0, the entire message will be removed.") @QueryParam("metaDataId") Integer metaDataId) throws ClientException;
    // @formatter:on

    @DELETE
    @Path("/{channelId}/messages/_removeAll")
    @Operation(summary = "Removes all messages for the specified channel.")
    @MirthOperation(name = "removeAllMessages", display = "Remove all messages", permission = Permissions.MESSAGES_REMOVE_ALL)
    public void removeAllMessages(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("restartRunningChannels") @Parameter(description = "If true, currently running channels will be stopped and restarted as part of the remove process. Otherwise, currently running channels will not be included.", schema = @Schema(defaultValue = "false")) @QueryParam("restartRunningChannels") boolean restartRunningChannels,
            @Param("clearStatistics") @Parameter(description = "If true, message statistics will also be cleared.", schema = @Schema(defaultValue = "true")) @QueryParam("clearStatistics") boolean clearStatistics) throws ClientException;
    // @formatter:on

    @DELETE
    @Path("/_removeAllMessages")
    @Operation(summary = "Removes all messages for multiple specified channels.")
    @MirthOperation(name = "removeAllMessages", display = "Remove all messages", permission = Permissions.MESSAGES_REMOVE_ALL)
    public void removeAllMessages(// @formatter:off
            @Param("channelIds") @Parameter(description = "The IDs of the channels.", required = true) @QueryParam("channelId") Set<String> channelIds,
            @Param("restartRunningChannels") @Parameter(description = "If true, currently running channels will be stopped and restarted as part of the remove process. Otherwise, currently running channels will not be included.", schema = @Schema(defaultValue = "false")) @QueryParam("restartRunningChannels") boolean restartRunningChannels,
            @Param("clearStatistics") @Parameter(description = "If true, message statistics will also be cleared.", schema = @Schema(defaultValue = "true")) @QueryParam("clearStatistics") boolean clearStatistics) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_removeAllMessagesPost")
    @Operation(summary = "Removes all messages for multiple specified channels. This is a POST request alternative to DELETE /_removeAllMessages that may be used when there are too many channel IDs to include in the query parameters.")
    @MirthOperation(name = "removeAllMessages", display = "Remove all messages", permission = Permissions.MESSAGES_REMOVE_ALL)
    public void removeAllMessagesPost(// @formatter:off
            @Param("channelIds") @RequestBody(description = "The IDs of the channels.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "channelIds", ref = "../apiexamples/guid_set_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "channelIds", ref = "../apiexamples/guid_set_json") }) }) Set<String> channelIds,
            @Param("restartRunningChannels") @Parameter(description = "If true, currently running channels will be stopped and restarted as part of the remove process. Otherwise, currently running channels will not be included.", schema = @Schema(defaultValue = "false")) @QueryParam("restartRunningChannels") boolean restartRunningChannels,
            @Param("clearStatistics") @Parameter(description = "If true, message statistics will also be cleared.", schema = @Schema(defaultValue = "true")) @QueryParam("clearStatistics") boolean clearStatistics) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/_import")
    @Operation(summary = "Imports a Message object into a channel. The message will not actually be processed through the channel, only imported.")
    @MirthOperation(name = "importMessage", display = "Import message", permission = Permissions.MESSAGES_IMPORT, type = ExecuteType.ASYNC)
    public void importMessage(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("message") @RequestBody(description = "The Message object to import.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "message", ref = "../apiexamples/message_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "message", ref = "../apiexamples/message_json") }) }) Message message) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/_importFromPath")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(summary = "Imports messages into a channel from a path accessible by the server. The messages will not actually be processed through the channel, only imported.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "messageImportResult", ref = "../apiexamples/message_import_result_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "messageImportResult", ref = "../apiexamples/message_import_result_json") }) })
    @MirthOperation(name = "importMessageServer", display = "Import messages on the server", permission = Permissions.MESSAGES_IMPORT, type = ExecuteType.ASYNC)
    public MessageImportResult importMessagesServer(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("path") @RequestBody(description = "The directory path on the server side to import messages from.", required = true, content = {
                    @Content(examples = {@ExampleObject(name = "path", value = "/path/to/message/directory") }) }) String path,
            @Param("includeSubfolders") @Parameter(description = "If true, sub-folders will also be scanned recursively for messages.", schema = @Schema(defaultValue = "false")) @QueryParam("includeSubfolders") boolean includeSubfolders) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/_exportUsingFilter")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Exports messages into a specific directory path accessible by the server. " + SWAGGER_TRY_IT_OUT_DISCLAIMER)
    @MirthOperation(name = "exportMessage", display = "Export message", permission = Permissions.MESSAGES_EXPORT_SERVER, type = ExecuteType.ASYNC)
    public int exportMessagesServer(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("filter") @Parameter(description = "The MessageFilter object to use to query messages by.", required = true) @FormDataParam("filter") MessageFilter filter,
            @Param("pageSize") @Parameter(description = "The maximum number of messages that will be queried at a time.") @QueryParam("pageSize") int pageSize,
            @Param("writerOptions") @Parameter(description = "The MessageWriterOptions object containing various export options.") @FormDataParam("writerOptions") MessageWriterOptions writerOptions) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/_export")
    @Operation(summary = "Exports messages into a specific directory path accessible by the server.")
    @MirthOperation(name = "exportMessage", display = "Export message", permission = Permissions.MESSAGES_EXPORT_SERVER, type = ExecuteType.ASYNC)
    public int exportMessagesServer(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("minMessageId") @Parameter(description = "The minimum message ID to query.") @QueryParam("minMessageId") Long minMessageId,
            @Param("maxMessageId") @Parameter(description = "The maximum message ID to query.") @QueryParam("maxMessageId") Long maxMessageId,
            @Param("minOriginalId") @Parameter(description = "The minimum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("minOriginalId") Long minOriginalId,
            @Param("maxOriginalId") @Parameter(description = "The maximum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("maxOriginalId") Long maxOriginalId,
            @Param("minImportId") @Parameter(description = "The minimum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("minImportId") Long minImportId,
            @Param("maxImportId") @Parameter(description = "The maximum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("maxImportId") Long maxImportId,
            @Param("startDate") @Parameter(description = "The earliest original received date to query by. Example: 1985-10-26T09:00:00.000-0700") @QueryParam("startDate") Calendar startDate,
            @Param("endDate") @Parameter(description = "The latest original received date to query by. Example: 2015-10-21T07:28:00.000-0700") @QueryParam("endDate") Calendar endDate,
            @Param("textSearch") @Parameter(description = "Searches all message content for this string. This process could take a long time depending on the amount of message content currently stored. Any message content that was encrypted by this channel will not be searchable.") @QueryParam("textSearch") String textSearch,
            @Param("textSearchRegex") @Parameter(description = "If true, text search input will be considered a regular expression pattern to be matched. Only supported by PostgreSQL, MySQL and Oracle databases.") @QueryParam("textSearchRegex") Boolean textSearchRegex,
            @Param("statuses") @Parameter(description = "Determines which message statuses to query by.") @QueryParam("status") Set<Status> statuses,
            @Param("includedMetaDataIds") @Parameter(description = "If present, only connector metadata IDs in this list will be queried.") @QueryParam("includedMetaDataId") Set<Integer> includedMetaDataIds,
            @Param("excludedMetaDataIds") @Parameter(description = "If present, connector metadata IDs in this list will not be queried.") @QueryParam("excludedMetaDataId") Set<Integer> excludedMetaDataIds,
            @Param("serverId") @Parameter(description = "The server ID associated with messages.") @QueryParam("serverId") String serverId,
            @Param("rawContentSearches") @Parameter(description = "Searches the raw content of messages.") @QueryParam("rawContentSearch") Set<String> rawContentSearches,
            @Param("processedRawContentSearches") @Parameter(description = "Searches the processed raw content of messages.") @QueryParam("processedRawContentSearch") Set<String> processedRawContentSearches,
            @Param("transformedContentSearches") @Parameter(description = "Searches the transformed content of messages.") @QueryParam("transformedContentSearch") Set<String> transformedContentSearches,
            @Param("encodedContentSearches") @Parameter(description = "Searches the encoded content of messages.") @QueryParam("encodedContentSearch") Set<String> encodedContentSearches,
            @Param("sentContentSearches") @Parameter(description = "Searches the sent content of messages.") @QueryParam("sentContentSearch") Set<String> sentContentSearches,
            @Param("responseContentSearches") @Parameter(description = "Searches the response content of messages.") @QueryParam("responseContentSearch") Set<String> responseContentSearches,
            @Param("responseTransformedContentSearches") @Parameter(description = "Searches the response transformed content of messages.") @QueryParam("responseTransformedContentSearch") Set<String> responseTransformedContentSearches,
            @Param("processedResponseContentSearches") @Parameter(description = "Searches the processed response content of messages.") @QueryParam("processedResponseContentSearch") Set<String> processedResponseContentSearches,
            @Param("connectorMapContentSearches") @Parameter(description = "Searches the connector map content of messages.") @QueryParam("connectorMapContentSearch") Set<String> connectorMapContentSearches,
            @Param("channelMapContentSearches") @Parameter(description = "Searches the channel map content of messages.") @QueryParam("channelMapContentSearch") Set<String> channelMapContentSearches,
            @Param("sourceMapContentSearches") @Parameter(description = "Searches the source map content of messages.") @QueryParam("sourceMapContentSearch") Set<String> sourceMapContentSearches,
            @Param("responseMapContentSearches") @Parameter(description = "Searches the response map content of messages.") @QueryParam("responseMapContentSearch") Set<String> responseMapContentSearches,
            @Param("processingErrorContentSearches") @Parameter(description = "Searches the processing error content of messages.") @QueryParam("processingErrorContentSearch") Set<String> processingErrorContentSearches,
            @Param("postprocessorErrorContentSearches") @Parameter(description = "Searches the postprocessor error content of messages.") @QueryParam("postprocessorErrorContentSearch") Set<String> postprocessorErrorContentSearches,
            @Param("responseErrorContentSearches") @Parameter(description = "Searches the response error content of messages.") @QueryParam("responseErrorContentSearch") Set<String> responseErrorContentSearches,
            @Param("metaDataSearches") @Parameter(description = "Searches a custom metadata column. Value should be in the form: COLUMN_NAME &lt;operator&gt; value, where operator is one of the following: =, !=, <, <=, >, >=, CONTAINS, DOES NOT CONTAIN, STARTS WITH, DOES NOT START WITH, ENDS WITH, DOES NOT END WITH") @QueryParam("metaDataSearch") Set<MetaDataSearch> metaDataSearches,
            @Param("metaDataCaseInsensitiveSearches") @Parameter(description = "Searches a custom metadata column, ignoring case. Value should be in the form: COLUMN_NAME &lt;operator&gt; value.") @QueryParam("metaDataCaseInsensitiveSearch") Set<MetaDataSearch> metaDataCaseInsensitiveSearches,
            @Param("textSearchMetaDataColumns") @Parameter(description = "When using a text search, these custom metadata columns will also be searched.") @QueryParam("textSearchMetaDataColumn") Set<String> textSearchMetaDataColumns,
            @Param("minSendAttempts") @Parameter(description = "The minimum number of send attempts for connector messages.") @QueryParam("minSendAttempts") Integer minSendAttempts,
            @Param("maxSendAttempts") @Parameter(description = "The maximum number of send attempts for connector messages.") @QueryParam("maxSendAttempts") Integer maxSendAttempts,
            @Param("attachment") @Parameter(description = "If true, only messages with attachments are included in the results.") @QueryParam("attachment") Boolean attachment,
            @Param("error") @Parameter(description = "If true, only messages with errors are included in the results.") @QueryParam("error") Boolean error,
            @Param("pageSize") @Parameter(description = "The maximum number of messages that will be queried at a time.") @QueryParam("pageSize") int pageSize,
            @Param("contentType") @Parameter(description = "The ContentType that will be extracted from the message for writing. If null or not provided, the entire message will be written in serialized format.") @QueryParam("contentType") ContentType contentType,
            @Param("destinationContent") @Parameter(description = "If true, the content to write will be extracted from the destination message(s), rather than the source message.", schema = @Schema(defaultValue = "false")) @QueryParam("destinationContent") boolean destinationContent,
            @Param("encrypt") @Parameter(description = "If true, message content will be encrypted before writing.", schema = @Schema(defaultValue = "false")) @QueryParam("encrypt") boolean encrypt,
            @Param("includeAttachments") @Parameter(description = "Determines whether attachments will be included with messages.", schema = @Schema(defaultValue = "false")) @QueryParam("includeAttachments") boolean includeAttachments,
            @Param("baseFolder") @Parameter(description = "The base directory to use when resolving relative paths in the root folder.") @QueryParam("baseFolder") String baseFolder,
            @Param("rootFolder") @Parameter(description = "The root folder to contain the written messages/sub-folders.") @QueryParam("rootFolder") String rootFolder,
            @Param("filePattern") @Parameter(description = "A string defining the folder/filename(s) for writing messages. It may contain variables to be replaced.") @QueryParam("filePattern") String filePattern,
            @Param("archiveFileName") @Parameter(description = "The file name to use for archive exports.") @QueryParam("archiveFileName") String archiveFileName,
            @Param("archiveFormat") @Parameter(description = "The archiver format to use to archive messages/folders that are written to the root folder. Valid values: zip, tar") @QueryParam("archiveFormat") String archiveFormat,
            @Param("compressFormat") @Parameter(description = "The compressor format to use to compress the archive file. Only valid when using the TAR archive format. Valid values: gz, bzip2") @QueryParam("compressFormat") String compressFormat,
            @Param("password") @Parameter(description = "The password used to protect the archive file. Only valid when using the ZIP archive format.") @QueryParam("password") String password,
            @Param("encryptionType") @Parameter(description = "The algorithm used to encrypt the password-protected archive file. Only valid when using the ZIP archive format. Valid values: STANDARD, AES128, AES256") @QueryParam("encryptionType") EncryptionType encryptionType) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/{messageId}/attachments/{attachmentId}/_export")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(summary = "Exports a message attachment into a specific file path accessible by the server.")
    @MirthOperation(name = "exportAttachment", display = "Export Attachment", permission = Permissions.MESSAGES_EXPORT_SERVER, type = ExecuteType.ASYNC)
    public void exportAttachmentServer(// @formatter:off
            @Param("channelId") @Parameter(description = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("messageId") @Parameter(description = "The ID of the message.", required = true) @PathParam("messageId") Long messageId,
            @Param("attachmentId") @Parameter(description = "The ID of the attachment.", required = true) @PathParam("attachmentId") String attachmentId,
            @Param("filePath") @RequestBody(description = "The file path to export the attachment to.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "filePath", value = "/path/to/file") }) }) String filePath,
            @Param("binary") @Parameter(description = "Indicates that the attachment is binary and should be Base64 decoded before writing to file.", schema = @Schema(defaultValue = "false")) @QueryParam("binary") boolean binary) throws ClientException;
    // @formatter:on
}