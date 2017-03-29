/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.servlets;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

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
@Api("Messages")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface MessageServletInterface extends BaseServletInterface {

    @POST
    @Path("/{channelId}/messages")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation("Processes a new message through a channel.")
    @MirthOperation(name = "processMessages", display = "Process messages", permission = Permissions.MESSAGES_PROCESS, type = ExecuteType.ASYNC)
    public void processMessage(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("rawData") @ApiParam(value = "The raw message data to process.", required = true) String rawData,
            @Param("destinationMetaDataIds") @ApiParam(value = "Indicates which destinations to send the message to.") @QueryParam("destinationMetaDataId") Set<Integer> destinationMetaDataIds,
            @Param("sourceMapEntries") @ApiParam(value = "These entries will be injected into the source map for the message. Value should be in the format: key=value") @QueryParam("sourceMapEntry") Set<String> sourceMapEntries,
            @Param("overwrite") @ApiParam(value = "If true and a valid original message ID is given, this message will overwrite the existing one.") @QueryParam("overwrite") boolean overwrite,
            @Param("imported") @ApiParam(value = "If true, marks this message as being imported. If the message is overwriting an existing one, then statistics will not be decremented.") @QueryParam("imported") boolean imported,
            @Param("originalMessageId") @ApiParam(value = "The original message ID this message is associated with.") @QueryParam("originalMessageId") Long originalMessageId) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages")
    @ApiOperation("Processes a new message through a channel, using the RawMessage object.")
    @MirthOperation(name = "processMessages", display = "Process messages", permission = Permissions.MESSAGES_PROCESS, type = ExecuteType.ASYNC)
    public void processMessage(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("rawMessage") @ApiParam(value = "The RawMessage object to process.", required = true) RawMessage rawMessage) throws ClientException;
    // @formatter:on

    @GET
    @Path("/{channelId}/messages/{messageId}")
    @ApiOperation("Retrieve a message by ID.")
    @MirthOperation(name = "getMessageContent", display = "Get message content", permission = Permissions.MESSAGES_VIEW, type = ExecuteType.ASYNC)
    public Message getMessageContent(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("messageId") @ApiParam(value = "The ID of the message.", required = true) @PathParam("messageId") Long messageId,
            @Param("metaDataIds") @ApiParam(value = "The metadata IDs of the connectors.") @QueryParam("metaDataId") List<Integer> metaDataIds) throws ClientException;
    // @formatter:on

    @GET
    @Path("/{channelId}/messages/{messageId}/attachments")
    @ApiOperation("Retrieve a list of attachments by message ID.")
    @MirthOperation(name = "getAttachmentsByMessageId", display = "Get attachments by message ID", permission = Permissions.MESSAGES_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public List<Attachment> getAttachmentsByMessageId(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("messageId") @ApiParam(value = "The ID of the message.", required = true) @PathParam("messageId") Long messageId,
            @Param("includeContent") @ApiParam(value = "If false, only the attachment ID and type will be returned.", defaultValue = "true") @QueryParam("includeContent") boolean includeContent) throws ClientException;
    // @formatter:on

    @GET
    @Path("/{channelId}/messages/{messageId}/attachments/{attachmentId}")
    @ApiOperation("Retrieve a message attachment by ID.")
    @MirthOperation(name = "getAttachment", display = "Get attachment", permission = Permissions.MESSAGES_VIEW, type = ExecuteType.ASYNC)
    public Attachment getAttachment(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("messageId") @ApiParam(value = "The ID of the message.", required = true) @PathParam("messageId") Long messageId,
            @Param("attachmentId") @ApiParam(value = "The ID of the attachment.", required = true) @PathParam("attachmentId") String attachmentId) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/{messageId}/_getDICOMMessage")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation("Given a ConnectorMessage object, reattaches any DICOM attachment data and returns the raw Base64 encoded message data.")
    @MirthOperation(name = "getDICOMMessage", display = "Get DICOM message", permission = Permissions.MESSAGES_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public String getDICOMMessage(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("messageId") @ApiParam(value = "The ID of the message.", required = true) @PathParam("messageId") Long messageId,
            @Param("message") @ApiParam(value = "The ConnectorMessage to retrieve DICOM data for.", required = true) ConnectorMessage message) throws ClientException;
    // @formatter:on

    @GET
    @Path("/{channelId}/messages/maxMessageId")
    @ApiOperation("Returns the maximum message ID for the given channel.")
    @MirthOperation(name = "getMaxMessageId", display = "Get max messageId", permission = Permissions.MESSAGES_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public Long getMaxMessageId(@Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId) throws ClientException;

    @POST
    @Path("/{channelId}/messages/_search")
    @ApiOperation("Search for messages by specific filter criteria.")
    @MirthOperation(name = "searchMessages", display = "Get messages by page limit", permission = Permissions.MESSAGES_VIEW, abortable = true)
    public List<Message> getMessages(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId, 
            @Param("filter") @ApiParam(value = "The MessageFilter object to use to query messages by.", required = true) MessageFilter filter, 
            @Param("includeContent") @ApiParam(value = "If true, message content will be returned with the results.", defaultValue = "false") @QueryParam("includeContent") Boolean includeContent, 
            @Param("offset") @ApiParam(value = "Used for pagination, determines where to start in the search results.", defaultValue = "0") @QueryParam("offset") Integer offset, 
            @Param("limit") @ApiParam(value = "Used for pagination, determines the maximum number of results to return.", defaultValue = "20") @QueryParam("limit") Integer limit) throws ClientException;
    // @formatter:on

    @GET
    @Path("/{channelId}/messages")
    @ApiOperation("Search for messages by specific filter criteria.")
    @MirthOperation(name = "searchMessages", display = "Get messages by page limit", permission = Permissions.MESSAGES_VIEW, abortable = true)
    public List<Message> getMessages(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("minMessageId") @ApiParam(value = "The minimum message ID to query.") @QueryParam("minMessageId") Long minMessageId,
            @Param("maxMessageId") @ApiParam(value = "The maximum message ID to query.") @QueryParam("maxMessageId") Long maxMessageId,
            @Param("minOriginalId") @ApiParam(value = "The minimum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("minOriginalId") Long minOriginalId,
            @Param("maxOriginalId") @ApiParam(value = "The maximum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("maxOriginalId") Long maxOriginalId,
            @Param("minImportId") @ApiParam(value = "The minimum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("minImportId") Long minImportId,
            @Param("maxImportId") @ApiParam(value = "The maximum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("maxImportId") Long maxImportId,
            @Param("startDate") @ApiParam(value = "The earliest original received date to query by. Example: 1985-10-26T09:00:00.000-0700") @QueryParam("startDate") Calendar startDate,
            @Param("endDate") @ApiParam(value = "The latest original received date to query by. Example: 2015-10-21T07:28:00.000-0700") @QueryParam("endDate") Calendar endDate,
            @Param("textSearch") @ApiParam(value = "Searches all message content for this string. This process could take a long time depending on the amount of message content currently stored. Any message content that was encrypted by this channel will not be searchable.") @QueryParam("textSearch") String textSearch,
            @Param("textSearchRegex") @ApiParam(value = "If true, text search input will be considered a regular expression pattern to be matched. Only supported by PostgreSQL, MySQL and Oracle databases.") @QueryParam("textSearchRegex") Boolean textSearchRegex,
            @Param("statuses") @ApiParam(value = "Determines which message statuses to query by.") @QueryParam("status") Set<Status> statuses,
            @Param("includedMetaDataIds") @ApiParam(value = "If present, only connector metadata IDs in this list will be queried.") @QueryParam("includedMetaDataId") Set<Integer> includedMetaDataIds,
            @Param("excludedMetaDataIds") @ApiParam(value = "If present, connector metadata IDs in this list will not be queried.") @QueryParam("excludedMetaDataId") Set<Integer> excludedMetaDataIds,
            @Param("serverId") @ApiParam(value = "The server ID associated with messages.") @QueryParam("serverId") String serverId,
            @Param("rawContentSearches") @ApiParam(value = "Searches the raw content of messages.") @QueryParam("rawContentSearch") Set<String> rawContentSearches,
            @Param("processedRawContentSearches") @ApiParam(value = "Searches the processed raw content of messages.") @QueryParam("processedRawContentSearch") Set<String> processedRawContentSearches,
            @Param("transformedContentSearches") @ApiParam(value = "Searches the transformed content of messages.") @QueryParam("transformedContentSearch") Set<String> transformedContentSearches,
            @Param("encodedContentSearches") @ApiParam(value = "Searches the encoded content of messages.") @QueryParam("encodedContentSearch") Set<String> encodedContentSearches,
            @Param("sentContentSearches") @ApiParam(value = "Searches the sent content of messages.") @QueryParam("sentContentSearch") Set<String> sentContentSearches,
            @Param("responseContentSearches") @ApiParam(value = "Searches the response content of messages.") @QueryParam("responseContentSearch") Set<String> responseContentSearches,
            @Param("responseTransformedContentSearches") @ApiParam(value = "Searches the response transformed content of messages.") @QueryParam("responseTransformedContentSearch") Set<String> responseTransformedContentSearches,
            @Param("processedResponseContentSearches") @ApiParam(value = "Searches the processed response content of messages.") @QueryParam("processedResponseContentSearch") Set<String> processedResponseContentSearches,
            @Param("connectorMapContentSearches") @ApiParam(value = "Searches the connector map content of messages.") @QueryParam("connectorMapContentSearch") Set<String> connectorMapContentSearches,
            @Param("channelMapContentSearches") @ApiParam(value = "Searches the channel map content of messages.") @QueryParam("channelMapContentSearch") Set<String> channelMapContentSearches,
            @Param("sourceMapContentSearches") @ApiParam(value = "Searches the source map content of messages.") @QueryParam("sourceMapContentSearch") Set<String> sourceMapContentSearches,
            @Param("responseMapContentSearches") @ApiParam(value = "Searches the response map content of messages.") @QueryParam("responseMapContentSearch") Set<String> responseMapContentSearches,
            @Param("processingErrorContentSearches") @ApiParam(value = "Searches the processing error content of messages.") @QueryParam("processingErrorContentSearch") Set<String> processingErrorContentSearches,
            @Param("postprocessorErrorContentSearches") @ApiParam(value = "Searches the postprocessor error content of messages.") @QueryParam("postprocessorErrorContentSearch") Set<String> postprocessorErrorContentSearches,
            @Param("responseErrorContentSearches") @ApiParam(value = "Searches the response error content of messages.") @QueryParam("responseErrorContentSearch") Set<String> responseErrorContentSearches,
            @Param("metaDataSearches") @ApiParam(value = "Searches a custom metadata column. Value should be in the form: COLUMN_NAME &lt;operator&gt; value, where operator is one of the following: =, !=, <, <=, >, >=, CONTAINS, DOES NOT CONTAIN, STARTS WITH, DOES NOT START WITH, ENDS WITH, DOES NOT END WITH") @QueryParam("metaDataSearch") Set<MetaDataSearch> metaDataSearches,
            @Param("metaDataCaseInsensitiveSearches") @ApiParam(value = "Searches a custom metadata column, ignoring case. Value should be in the form: COLUMN_NAME &lt;operator&gt; value.") @QueryParam("metaDataCaseInsensitiveSearch") Set<MetaDataSearch> metaDataCaseInsensitiveSearches,
            @Param("textSearchMetaDataColumns") @ApiParam(value = "When using a text search, these custom metadata columns will also be searched.") @QueryParam("textSearchMetaDataColumn") Set<String> textSearchMetaDataColumns,
            @Param("minSendAttempts") @ApiParam(value = "The minimum number of send attempts for connector messages.") @QueryParam("minSendAttempts") Integer minSendAttempts,
            @Param("maxSendAttempts") @ApiParam(value = "The maximum number of send attempts for connector messages.") @QueryParam("maxSendAttempts") Integer maxSendAttempts,
            @Param("attachment") @ApiParam(value = "If true, only messages with attachments are included in the results.") @QueryParam("attachment") Boolean attachment,
            @Param("error") @ApiParam(value = "If true, only messages with errors are included in the results.") @QueryParam("error") Boolean error,
            @Param("includeContent") @ApiParam(value = "If true, message content will be returned with the results.", defaultValue = "false") @QueryParam("includeContent") Boolean includeContent,
            @Param("offset") @ApiParam(value = "Used for pagination, determines where to start in the search results.", defaultValue = "0") @QueryParam("offset") Integer offset,
            @Param("limit") @ApiParam(value = "Used for pagination, determines the maximum number of results to return.", defaultValue = "20") @QueryParam("limit") Integer limit) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/count/_search")
    @ApiOperation("Count number for messages by specific filter criteria.")
    @MirthOperation(name = "getSearchCount", display = "Get search results count", permission = Permissions.MESSAGES_VIEW, abortable = true)
    public Long getMessageCount(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("filter") @ApiParam(value = "The MessageFilter object to use to query messages by.", required = true) MessageFilter filter) throws ClientException;
    // @formatter:off
    
    @GET
    @Path("/{channelId}/messages/count")
    @ApiOperation("Count number for messages by specific filter criteria.")
    @MirthOperation(name = "getSearchCount", display = "Get search results count", permission = Permissions.MESSAGES_VIEW, abortable = true)
    public Long getMessageCount(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("minMessageId") @ApiParam(value = "The minimum message ID to query.") @QueryParam("minMessageId") Long minMessageId,
            @Param("maxMessageId") @ApiParam(value = "The maximum message ID to query.") @QueryParam("maxMessageId") Long maxMessageId,
            @Param("minOriginalId") @ApiParam(value = "The minimum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("minOriginalId") Long minOriginalId,
            @Param("maxOriginalId") @ApiParam(value = "The maximum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("maxOriginalId") Long maxOriginalId,
            @Param("minImportId") @ApiParam(value = "The minimum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("minImportId") Long minImportId,
            @Param("maxImportId") @ApiParam(value = "The maximum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("maxImportId") Long maxImportId,
            @Param("startDate") @ApiParam(value = "The earliest original received date to query by. Example: 1985-10-26T09:00:00.000-0700") @QueryParam("startDate") Calendar startDate,
            @Param("endDate") @ApiParam(value = "The latest original received date to query by. Example: 2015-10-21T07:28:00.000-0700") @QueryParam("endDate") Calendar endDate,
            @Param("textSearch") @ApiParam(value = "Searches all message content for this string. This process could take a long time depending on the amount of message content currently stored. Any message content that was encrypted by this channel will not be searchable.") @QueryParam("textSearch") String textSearch,
            @Param("textSearchRegex") @ApiParam(value = "If true, text search input will be considered a regular expression pattern to be matched. Only supported by PostgreSQL, MySQL and Oracle databases.") @QueryParam("textSearchRegex") Boolean textSearchRegex,
            @Param("statuses") @ApiParam(value = "Determines which message statuses to query by.") @QueryParam("status") Set<Status> statuses,
            @Param("includedMetaDataIds") @ApiParam(value = "If present, only connector metadata IDs in this list will be queried.") @QueryParam("includedMetaDataId") Set<Integer> includedMetaDataIds,
            @Param("excludedMetaDataIds") @ApiParam(value = "If present, connector metadata IDs in this list will not be queried.") @QueryParam("excludedMetaDataId") Set<Integer> excludedMetaDataIds,
            @Param("serverId") @ApiParam(value = "The server ID associated with messages.") @QueryParam("serverId") String serverId,
            @Param("rawContentSearches") @ApiParam(value = "Searches the raw content of messages.") @QueryParam("rawContentSearch") Set<String> rawContentSearches,
            @Param("processedRawContentSearches") @ApiParam(value = "Searches the processed raw content of messages.") @QueryParam("processedRawContentSearch") Set<String> processedRawContentSearches,
            @Param("transformedContentSearches") @ApiParam(value = "Searches the transformed content of messages.") @QueryParam("transformedContentSearch") Set<String> transformedContentSearches,
            @Param("encodedContentSearches") @ApiParam(value = "Searches the encoded content of messages.") @QueryParam("encodedContentSearch") Set<String> encodedContentSearches,
            @Param("sentContentSearches") @ApiParam(value = "Searches the sent content of messages.") @QueryParam("sentContentSearch") Set<String> sentContentSearches,
            @Param("responseContentSearches") @ApiParam(value = "Searches the response content of messages.") @QueryParam("responseContentSearch") Set<String> responseContentSearches,
            @Param("responseTransformedContentSearches") @ApiParam(value = "Searches the response transformed content of messages.") @QueryParam("responseTransformedContentSearch") Set<String> responseTransformedContentSearches,
            @Param("processedResponseContentSearches") @ApiParam(value = "Searches the processed response content of messages.") @QueryParam("processedResponseContentSearch") Set<String> processedResponseContentSearches,
            @Param("connectorMapContentSearches") @ApiParam(value = "Searches the connector map content of messages.") @QueryParam("connectorMapContentSearch") Set<String> connectorMapContentSearches,
            @Param("channelMapContentSearches") @ApiParam(value = "Searches the channel map content of messages.") @QueryParam("channelMapContentSearch") Set<String> channelMapContentSearches,
            @Param("sourceMapContentSearches") @ApiParam(value = "Searches the source map content of messages.") @QueryParam("sourceMapContentSearch") Set<String> sourceMapContentSearches,
            @Param("responseMapContentSearches") @ApiParam(value = "Searches the response map content of messages.") @QueryParam("responseMapContentSearch") Set<String> responseMapContentSearches,
            @Param("processingErrorContentSearches") @ApiParam(value = "Searches the processing error content of messages.") @QueryParam("processingErrorContentSearch") Set<String> processingErrorContentSearches,
            @Param("postprocessorErrorContentSearches") @ApiParam(value = "Searches the postprocessor error content of messages.") @QueryParam("postprocessorErrorContentSearch") Set<String> postprocessorErrorContentSearches,
            @Param("responseErrorContentSearches") @ApiParam(value = "Searches the response error content of messages.") @QueryParam("responseErrorContentSearch") Set<String> responseErrorContentSearches,
            @Param("metaDataSearches") @ApiParam(value = "Searches a custom metadata column. Value should be in the form: COLUMN_NAME &lt;operator&gt; value, where operator is one of the following: =, !=, <, <=, >, >=, CONTAINS, DOES NOT CONTAIN, STARTS WITH, DOES NOT START WITH, ENDS WITH, DOES NOT END WITH") @QueryParam("metaDataSearch") Set<MetaDataSearch> metaDataSearches,
            @Param("metaDataCaseInsensitiveSearches") @ApiParam(value = "Searches a custom metadata column, ignoring case. Value should be in the form: COLUMN_NAME &lt;operator&gt; value.") @QueryParam("metaDataCaseInsensitiveSearch") Set<MetaDataSearch> metaDataCaseInsensitiveSearches,
            @Param("textSearchMetaDataColumns") @ApiParam(value = "When using a text search, these custom metadata columns will also be searched.") @QueryParam("textSearchMetaDataColumn") Set<String> textSearchMetaDataColumns,
            @Param("minSendAttempts") @ApiParam(value = "The minimum number of send attempts for connector messages.") @QueryParam("minSendAttempts") Integer minSendAttempts,
            @Param("maxSendAttempts") @ApiParam(value = "The maximum number of send attempts for connector messages.") @QueryParam("maxSendAttempts") Integer maxSendAttempts,
            @Param("attachment") @ApiParam(value = "If true, only messages with attachments are included in the results.") @QueryParam("attachment") Boolean attachment,
            @Param("error") @ApiParam(value = "If true, only messages with errors are included in the results.") @QueryParam("error") Boolean error) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/_reprocess")
    @ApiOperation("Reprocesses messages through a channel by specific filter criteria.")
    @MirthOperation(name = "reprocessMessages", display = "Reprocess messages", permission = Permissions.MESSAGES_REPROCESS_RESULTS, type = ExecuteType.ASYNC)
    public void reprocessMessages(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("filter") @ApiParam(value = "The MessageFilter object to use to query messages by.", required = true) MessageFilter filter,
            @Param("replace") @ApiParam(value = "If true, the message will overwrite the current one", defaultValue = "false") @QueryParam("replace") boolean replace,
            @Param("filterDestinations") @ApiParam(value = "If true, the metaDataId parameter will be used to determine which destinations to reprocess the message through.", defaultValue = "false") @QueryParam("filterDestinations") boolean filterDestinations,
            @Param("reprocessMetaDataIds") @ApiParam(value = "Indicates which destinations to send the message to.") @QueryParam("metaDataId") Set<Integer> reprocessMetaDataIds) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/_reprocess")
    @Consumes("")
    @ApiOperation("Reprocesses messages through a channel by specific filter criteria.")
    @MirthOperation(name = "reprocessMessages", display = "Reprocess messages", permission = Permissions.MESSAGES_REPROCESS_RESULTS, type = ExecuteType.ASYNC)
    public void reprocessMessages(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("minMessageId") @ApiParam(value = "The minimum message ID to query.") @QueryParam("minMessageId") Long minMessageId,
            @Param("maxMessageId") @ApiParam(value = "The maximum message ID to query.") @QueryParam("maxMessageId") Long maxMessageId,
            @Param("minOriginalId") @ApiParam(value = "The minimum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("minOriginalId") Long minOriginalId,
            @Param("maxOriginalId") @ApiParam(value = "The maximum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("maxOriginalId") Long maxOriginalId,
            @Param("minImportId") @ApiParam(value = "The minimum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("minImportId") Long minImportId,
            @Param("maxImportId") @ApiParam(value = "The maximum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("maxImportId") Long maxImportId,
            @Param("startDate") @ApiParam(value = "The earliest original received date to query by. Example: 1985-10-26T09:00:00.000-0700") @QueryParam("startDate") Calendar startDate,
            @Param("endDate") @ApiParam(value = "The latest original received date to query by. Example: 2015-10-21T07:28:00.000-0700") @QueryParam("endDate") Calendar endDate,
            @Param("textSearch") @ApiParam(value = "Searches all message content for this string. This process could take a long time depending on the amount of message content currently stored. Any message content that was encrypted by this channel will not be searchable.") @QueryParam("textSearch") String textSearch,
            @Param("textSearchRegex") @ApiParam(value = "If true, text search input will be considered a regular expression pattern to be matched. Only supported by PostgreSQL, MySQL and Oracle databases.") @QueryParam("textSearchRegex") Boolean textSearchRegex,
            @Param("statuses") @ApiParam(value = "Determines which message statuses to query by.") @QueryParam("status") Set<Status> statuses,
            @Param("includedMetaDataIds") @ApiParam(value = "If present, only connector metadata IDs in this list will be queried.") @QueryParam("includedMetaDataId") Set<Integer> includedMetaDataIds,
            @Param("excludedMetaDataIds") @ApiParam(value = "If present, connector metadata IDs in this list will not be queried.") @QueryParam("excludedMetaDataId") Set<Integer> excludedMetaDataIds,
            @Param("serverId") @ApiParam(value = "The server ID associated with messages.") @QueryParam("serverId") String serverId,
            @Param("rawContentSearches") @ApiParam(value = "Searches the raw content of messages.") @QueryParam("rawContentSearch") Set<String> rawContentSearches,
            @Param("processedRawContentSearches") @ApiParam(value = "Searches the processed raw content of messages.") @QueryParam("processedRawContentSearch") Set<String> processedRawContentSearches,
            @Param("transformedContentSearches") @ApiParam(value = "Searches the transformed content of messages.") @QueryParam("transformedContentSearch") Set<String> transformedContentSearches,
            @Param("encodedContentSearches") @ApiParam(value = "Searches the encoded content of messages.") @QueryParam("encodedContentSearch") Set<String> encodedContentSearches,
            @Param("sentContentSearches") @ApiParam(value = "Searches the sent content of messages.") @QueryParam("sentContentSearch") Set<String> sentContentSearches,
            @Param("responseContentSearches") @ApiParam(value = "Searches the response content of messages.") @QueryParam("responseContentSearch") Set<String> responseContentSearches,
            @Param("responseTransformedContentSearches") @ApiParam(value = "Searches the response transformed content of messages.") @QueryParam("responseTransformedContentSearch") Set<String> responseTransformedContentSearches,
            @Param("processedResponseContentSearches") @ApiParam(value = "Searches the processed response content of messages.") @QueryParam("processedResponseContentSearch") Set<String> processedResponseContentSearches,
            @Param("connectorMapContentSearches") @ApiParam(value = "Searches the connector map content of messages.") @QueryParam("connectorMapContentSearch") Set<String> connectorMapContentSearches,
            @Param("channelMapContentSearches") @ApiParam(value = "Searches the channel map content of messages.") @QueryParam("channelMapContentSearch") Set<String> channelMapContentSearches,
            @Param("sourceMapContentSearches") @ApiParam(value = "Searches the source map content of messages.") @QueryParam("sourceMapContentSearch") Set<String> sourceMapContentSearches,
            @Param("responseMapContentSearches") @ApiParam(value = "Searches the response map content of messages.") @QueryParam("responseMapContentSearch") Set<String> responseMapContentSearches,
            @Param("processingErrorContentSearches") @ApiParam(value = "Searches the processing error content of messages.") @QueryParam("processingErrorContentSearch") Set<String> processingErrorContentSearches,
            @Param("postprocessorErrorContentSearches") @ApiParam(value = "Searches the postprocessor error content of messages.") @QueryParam("postprocessorErrorContentSearch") Set<String> postprocessorErrorContentSearches,
            @Param("responseErrorContentSearches") @ApiParam(value = "Searches the response error content of messages.") @QueryParam("responseErrorContentSearch") Set<String> responseErrorContentSearches,
            @Param("metaDataSearches") @ApiParam(value = "Searches a custom metadata column. Value should be in the form: COLUMN_NAME &lt;operator&gt; value, where operator is one of the following: =, !=, <, <=, >, >=, CONTAINS, DOES NOT CONTAIN, STARTS WITH, DOES NOT START WITH, ENDS WITH, DOES NOT END WITH") @QueryParam("metaDataSearch") Set<MetaDataSearch> metaDataSearches,
            @Param("metaDataCaseInsensitiveSearches") @ApiParam(value = "Searches a custom metadata column, ignoring case. Value should be in the form: COLUMN_NAME &lt;operator&gt; value.") @QueryParam("metaDataCaseInsensitiveSearch") Set<MetaDataSearch> metaDataCaseInsensitiveSearches,
            @Param("textSearchMetaDataColumns") @ApiParam(value = "When using a text search, these custom metadata columns will also be searched.") @QueryParam("textSearchMetaDataColumn") Set<String> textSearchMetaDataColumns,
            @Param("minSendAttempts") @ApiParam(value = "The minimum number of send attempts for connector messages.") @QueryParam("minSendAttempts") Integer minSendAttempts,
            @Param("maxSendAttempts") @ApiParam(value = "The maximum number of send attempts for connector messages.") @QueryParam("maxSendAttempts") Integer maxSendAttempts,
            @Param("attachment") @ApiParam(value = "If true, only messages with attachments are included in the results.") @QueryParam("attachment") Boolean attachment,
            @Param("error") @ApiParam(value = "If true, only messages with errors are included in the results.") @QueryParam("error") Boolean error,
            @Param("replace") @ApiParam(value = "If true, the message will overwrite the current one", defaultValue = "false") @QueryParam("replace") boolean replace,
            @Param("filterDestinations") @ApiParam(value = "If true, the metaDataId parameter will be used to determine which destinations to reprocess the message through.", defaultValue = "false") @QueryParam("filterDestinations") boolean filterDestinations,
            @Param("reprocessMetaDataIds") @ApiParam(value = "Indicates which destinations to send the message to.") @QueryParam("metaDataId") Set<Integer> reprocessMetaDataIds) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/{messageId}/_reprocess")
    @ApiOperation("Reprocesses and overwrites a single message.")
    @MirthOperation(name = "reprocessMessage", display = "Reprocess messages", permission = Permissions.MESSAGES_REPROCESS, type = ExecuteType.ASYNC)
    public void reprocessMessage(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("messageId") @ApiParam(value = "The ID of the message.", required = true) @PathParam("messageId") Long messageId,
            @Param("replace") @ApiParam(value = "If true, the message will overwrite the current one", defaultValue = "false") @QueryParam("replace") boolean replace,
            @Param("filterDestinations") @ApiParam(value = "If true, the metaDataId parameter will be used to determine which destinations to reprocess the message through.", defaultValue = "false") @QueryParam("filterDestinations") boolean filterDestinations,
            @Param("reprocessMetaDataIds") @ApiParam(value = "Indicates which destinations to send the message to.") @QueryParam("metaDataId") Set<Integer> reprocessMetaDataIds) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/_remove")
    @ApiOperation("Remove messages by specific filter criteria.")
    @MirthOperation(name = "removeMessages", display = "Remove messages", permission = Permissions.MESSAGES_REMOVE_RESULTS, abortable = true)
    public void removeMessages(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("filter") @ApiParam(value = "The MessageFilter object to use to query messages by.", required = true) MessageFilter filter) throws ClientException;
    // @formatter:on

    @DELETE
    @Path("/{channelId}/messages")
    @Consumes("")
    @ApiOperation("Remove messages by specific filter criteria.")
    @MirthOperation(name = "removeMessages", display = "Remove messages", permission = Permissions.MESSAGES_REMOVE_RESULTS, abortable = true)
    public void removeMessages(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("minMessageId") @ApiParam(value = "The minimum message ID to query.") @QueryParam("minMessageId") Long minMessageId,
            @Param("maxMessageId") @ApiParam(value = "The maximum message ID to query.") @QueryParam("maxMessageId") Long maxMessageId,
            @Param("minOriginalId") @ApiParam(value = "The minimum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("minOriginalId") Long minOriginalId,
            @Param("maxOriginalId") @ApiParam(value = "The maximum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("maxOriginalId") Long maxOriginalId,
            @Param("minImportId") @ApiParam(value = "The minimum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("minImportId") Long minImportId,
            @Param("maxImportId") @ApiParam(value = "The maximum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("maxImportId") Long maxImportId,
            @Param("startDate") @ApiParam(value = "The earliest original received date to query by. Example: 1985-10-26T09:00:00.000-0700") @QueryParam("startDate") Calendar startDate,
            @Param("endDate") @ApiParam(value = "The latest original received date to query by. Example: 2015-10-21T07:28:00.000-0700") @QueryParam("endDate") Calendar endDate,
            @Param("textSearch") @ApiParam(value = "Searches all message content for this string. This process could take a long time depending on the amount of message content currently stored. Any message content that was encrypted by this channel will not be searchable.") @QueryParam("textSearch") String textSearch,
            @Param("textSearchRegex") @ApiParam(value = "If true, text search input will be considered a regular expression pattern to be matched. Only supported by PostgreSQL, MySQL and Oracle databases.") @QueryParam("textSearchRegex") Boolean textSearchRegex,
            @Param("statuses") @ApiParam(value = "Determines which message statuses to query by.") @QueryParam("status") Set<Status> statuses,
            @Param("includedMetaDataIds") @ApiParam(value = "If present, only connector metadata IDs in this list will be queried.") @QueryParam("includedMetaDataId") Set<Integer> includedMetaDataIds,
            @Param("excludedMetaDataIds") @ApiParam(value = "If present, connector metadata IDs in this list will not be queried.") @QueryParam("excludedMetaDataId") Set<Integer> excludedMetaDataIds,
            @Param("serverId") @ApiParam(value = "The server ID associated with messages.") @QueryParam("serverId") String serverId,
            @Param("rawContentSearches") @ApiParam(value = "Searches the raw content of messages.") @QueryParam("rawContentSearch") Set<String> rawContentSearches,
            @Param("processedRawContentSearches") @ApiParam(value = "Searches the processed raw content of messages.") @QueryParam("processedRawContentSearch") Set<String> processedRawContentSearches,
            @Param("transformedContentSearches") @ApiParam(value = "Searches the transformed content of messages.") @QueryParam("transformedContentSearch") Set<String> transformedContentSearches,
            @Param("encodedContentSearches") @ApiParam(value = "Searches the encoded content of messages.") @QueryParam("encodedContentSearch") Set<String> encodedContentSearches,
            @Param("sentContentSearches") @ApiParam(value = "Searches the sent content of messages.") @QueryParam("sentContentSearch") Set<String> sentContentSearches,
            @Param("responseContentSearches") @ApiParam(value = "Searches the response content of messages.") @QueryParam("responseContentSearch") Set<String> responseContentSearches,
            @Param("responseTransformedContentSearches") @ApiParam(value = "Searches the response transformed content of messages.") @QueryParam("responseTransformedContentSearch") Set<String> responseTransformedContentSearches,
            @Param("processedResponseContentSearches") @ApiParam(value = "Searches the processed response content of messages.") @QueryParam("processedResponseContentSearch") Set<String> processedResponseContentSearches,
            @Param("connectorMapContentSearches") @ApiParam(value = "Searches the connector map content of messages.") @QueryParam("connectorMapContentSearch") Set<String> connectorMapContentSearches,
            @Param("channelMapContentSearches") @ApiParam(value = "Searches the channel map content of messages.") @QueryParam("channelMapContentSearch") Set<String> channelMapContentSearches,
            @Param("sourceMapContentSearches") @ApiParam(value = "Searches the source map content of messages.") @QueryParam("sourceMapContentSearch") Set<String> sourceMapContentSearches,
            @Param("responseMapContentSearches") @ApiParam(value = "Searches the response map content of messages.") @QueryParam("responseMapContentSearch") Set<String> responseMapContentSearches,
            @Param("processingErrorContentSearches") @ApiParam(value = "Searches the processing error content of messages.") @QueryParam("processingErrorContentSearch") Set<String> processingErrorContentSearches,
            @Param("postprocessorErrorContentSearches") @ApiParam(value = "Searches the postprocessor error content of messages.") @QueryParam("postprocessorErrorContentSearch") Set<String> postprocessorErrorContentSearches,
            @Param("responseErrorContentSearches") @ApiParam(value = "Searches the response error content of messages.") @QueryParam("responseErrorContentSearch") Set<String> responseErrorContentSearches,
            @Param("metaDataSearches") @ApiParam(value = "Searches a custom metadata column. Value should be in the form: COLUMN_NAME &lt;operator&gt; value, where operator is one of the following: =, !=, <, <=, >, >=, CONTAINS, DOES NOT CONTAIN, STARTS WITH, DOES NOT START WITH, ENDS WITH, DOES NOT END WITH") @QueryParam("metaDataSearch") Set<MetaDataSearch> metaDataSearches,
            @Param("metaDataCaseInsensitiveSearches") @ApiParam(value = "Searches a custom metadata column, ignoring case. Value should be in the form: COLUMN_NAME &lt;operator&gt; value.") @QueryParam("metaDataCaseInsensitiveSearch") Set<MetaDataSearch> metaDataCaseInsensitiveSearches,
            @Param("textSearchMetaDataColumns") @ApiParam(value = "When using a text search, these custom metadata columns will also be searched.") @QueryParam("textSearchMetaDataColumn") Set<String> textSearchMetaDataColumns,
            @Param("minSendAttempts") @ApiParam(value = "The minimum number of send attempts for connector messages.") @QueryParam("minSendAttempts") Integer minSendAttempts,
            @Param("maxSendAttempts") @ApiParam(value = "The maximum number of send attempts for connector messages.") @QueryParam("maxSendAttempts") Integer maxSendAttempts,
            @Param("attachment") @ApiParam(value = "If true, only messages with attachments are included in the results.") @QueryParam("attachment") Boolean attachment,
            @Param("error") @ApiParam(value = "If true, only messages with errors are included in the results.") @QueryParam("error") Boolean error) throws ClientException;
    // @formatter:on

    @DELETE
    @Path("/{channelId}/messages/{messageId}")
    @ApiOperation("Remove a single message by ID.")
    @MirthOperation(name = "removeMessage", display = "Remove message", permission = Permissions.MESSAGES_REMOVE, abortable = true)
    public void removeMessage(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("messageId") @ApiParam(value = "The ID of the message.", required = true) @PathParam("messageId") Long messageId,
            @Param("metaDataId") @ApiParam(value = "If present, only the specific connector message will be removed. If the metadata ID is 0, the entire message will be removed.") @QueryParam("metaDataId") Integer metaDataId) throws ClientException;
    // @formatter:on

    @DELETE
    @Path("/{channelId}/messages/_removeAll")
    @ApiOperation("Removes all messages for the specified channel.")
    @MirthOperation(name = "removeAllMessages", display = "Remove all messages", permission = Permissions.MESSAGES_REMOVE_ALL)
    public void removeAllMessages(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("restartRunningChannels") @ApiParam(value = "If true, currently running channels will be stopped and restarted as part of the remove process. Otherwise, currently running channels will not be included.", defaultValue = "false") @QueryParam("restartRunningChannels") boolean restartRunningChannels,
            @Param("clearStatistics") @ApiParam(value = "If true, message statistics will also be cleared.", defaultValue = "true") @QueryParam("clearStatistics") boolean clearStatistics) throws ClientException;
    // @formatter:on

    @DELETE
    @Path("/_removeAllMessages")
    @ApiOperation("Removes all messages for multiple specified channels.")
    @MirthOperation(name = "removeAllMessages", display = "Remove all messages", permission = Permissions.MESSAGES_REMOVE_ALL)
    public void removeAllMessages(// @formatter:off
            @Param("channelIds") @ApiParam(value = "The IDs of the channels.", required = true) @QueryParam("channelId") Set<String> channelIds,
            @Param("restartRunningChannels") @ApiParam(value = "If true, currently running channels will be stopped and restarted as part of the remove process. Otherwise, currently running channels will not be included.", defaultValue = "false") @QueryParam("restartRunningChannels") boolean restartRunningChannels,
            @Param("clearStatistics") @ApiParam(value = "If true, message statistics will also be cleared.", defaultValue = "true") @QueryParam("clearStatistics") boolean clearStatistics) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/_import")
    @ApiOperation("Imports a Message object into a channel. The message will not actually be processed through the channel, only imported.")
    @MirthOperation(name = "importMessage", display = "Import message", permission = Permissions.MESSAGES_IMPORT, type = ExecuteType.ASYNC)
    public void importMessage(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("message") @ApiParam(value = "The Message object to import.", required = true) Message message) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/_import")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation("Imports messages into a channel from a path accessible by the server. The messages will not actually be processed through the channel, only imported.")
    @MirthOperation(name = "importMessageServer", display = "Import messages on the server", permission = Permissions.MESSAGES_IMPORT, type = ExecuteType.ASYNC)
    public MessageImportResult importMessagesServer(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("path") @ApiParam(value = "The directory path on the server side to import messages from.", required = true) String path,
            @Param("includeSubfolders") @ApiParam(value = "If true, sub-folders will also be scanned recursively for messages.", defaultValue = "false") @QueryParam("includeSubfolders") boolean includeSubfolders) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/_export")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation("Exports messages into a specific directory path accessible by the server.")
    @MirthOperation(name = "exportMessage", display = "Export message", permission = Permissions.MESSAGES_EXPORT_SERVER, type = ExecuteType.ASYNC)
    public int exportMessagesServer(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("filter") @ApiParam(value = "The MessageFilter object to use to query messages by.", required = true) @FormDataParam("filter") MessageFilter filter,
            @Param("pageSize") @ApiParam(value = "The maximum number of messages that will be queried at a time.") @QueryParam("pageSize") int pageSize,
            @Param("writerOptions") @ApiParam(value = "The MessageWriterOptions object containing various export options.") @FormDataParam("writerOptions") MessageWriterOptions writerOptions) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/_export")
    @ApiOperation("Exports messages into a specific directory path accessible by the server.")
    @MirthOperation(name = "exportMessage", display = "Export message", permission = Permissions.MESSAGES_EXPORT_SERVER, type = ExecuteType.ASYNC)
    public int exportMessagesServer(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("minMessageId") @ApiParam(value = "The minimum message ID to query.") @QueryParam("minMessageId") Long minMessageId,
            @Param("maxMessageId") @ApiParam(value = "The maximum message ID to query.") @QueryParam("maxMessageId") Long maxMessageId,
            @Param("minOriginalId") @ApiParam(value = "The minimum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("minOriginalId") Long minOriginalId,
            @Param("maxOriginalId") @ApiParam(value = "The maximum original message ID to query. Messages that have been reprocessed will retain their original message ID.") @QueryParam("maxOriginalId") Long maxOriginalId,
            @Param("minImportId") @ApiParam(value = "The minimum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("minImportId") Long minImportId,
            @Param("maxImportId") @ApiParam(value = "The maximum import message ID to query. Messages that have been imported will retain their original message ID under this value.") @QueryParam("maxImportId") Long maxImportId,
            @Param("startDate") @ApiParam(value = "The earliest original received date to query by. Example: 1985-10-26T09:00:00.000-0700") @QueryParam("startDate") Calendar startDate,
            @Param("endDate") @ApiParam(value = "The latest original received date to query by. Example: 2015-10-21T07:28:00.000-0700") @QueryParam("endDate") Calendar endDate,
            @Param("textSearch") @ApiParam(value = "Searches all message content for this string. This process could take a long time depending on the amount of message content currently stored. Any message content that was encrypted by this channel will not be searchable.") @QueryParam("textSearch") String textSearch,
            @Param("textSearchRegex") @ApiParam(value = "If true, text search input will be considered a regular expression pattern to be matched. Only supported by PostgreSQL, MySQL and Oracle databases.") @QueryParam("textSearchRegex") Boolean textSearchRegex,
            @Param("statuses") @ApiParam(value = "Determines which message statuses to query by.") @QueryParam("status") Set<Status> statuses,
            @Param("includedMetaDataIds") @ApiParam(value = "If present, only connector metadata IDs in this list will be queried.") @QueryParam("includedMetaDataId") Set<Integer> includedMetaDataIds,
            @Param("excludedMetaDataIds") @ApiParam(value = "If present, connector metadata IDs in this list will not be queried.") @QueryParam("excludedMetaDataId") Set<Integer> excludedMetaDataIds,
            @Param("serverId") @ApiParam(value = "The server ID associated with messages.") @QueryParam("serverId") String serverId,
            @Param("rawContentSearches") @ApiParam(value = "Searches the raw content of messages.") @QueryParam("rawContentSearch") Set<String> rawContentSearches,
            @Param("processedRawContentSearches") @ApiParam(value = "Searches the processed raw content of messages.") @QueryParam("processedRawContentSearch") Set<String> processedRawContentSearches,
            @Param("transformedContentSearches") @ApiParam(value = "Searches the transformed content of messages.") @QueryParam("transformedContentSearch") Set<String> transformedContentSearches,
            @Param("encodedContentSearches") @ApiParam(value = "Searches the encoded content of messages.") @QueryParam("encodedContentSearch") Set<String> encodedContentSearches,
            @Param("sentContentSearches") @ApiParam(value = "Searches the sent content of messages.") @QueryParam("sentContentSearch") Set<String> sentContentSearches,
            @Param("responseContentSearches") @ApiParam(value = "Searches the response content of messages.") @QueryParam("responseContentSearch") Set<String> responseContentSearches,
            @Param("responseTransformedContentSearches") @ApiParam(value = "Searches the response transformed content of messages.") @QueryParam("responseTransformedContentSearch") Set<String> responseTransformedContentSearches,
            @Param("processedResponseContentSearches") @ApiParam(value = "Searches the processed response content of messages.") @QueryParam("processedResponseContentSearch") Set<String> processedResponseContentSearches,
            @Param("connectorMapContentSearches") @ApiParam(value = "Searches the connector map content of messages.") @QueryParam("connectorMapContentSearch") Set<String> connectorMapContentSearches,
            @Param("channelMapContentSearches") @ApiParam(value = "Searches the channel map content of messages.") @QueryParam("channelMapContentSearch") Set<String> channelMapContentSearches,
            @Param("sourceMapContentSearches") @ApiParam(value = "Searches the source map content of messages.") @QueryParam("sourceMapContentSearch") Set<String> sourceMapContentSearches,
            @Param("responseMapContentSearches") @ApiParam(value = "Searches the response map content of messages.") @QueryParam("responseMapContentSearch") Set<String> responseMapContentSearches,
            @Param("processingErrorContentSearches") @ApiParam(value = "Searches the processing error content of messages.") @QueryParam("processingErrorContentSearch") Set<String> processingErrorContentSearches,
            @Param("postprocessorErrorContentSearches") @ApiParam(value = "Searches the postprocessor error content of messages.") @QueryParam("postprocessorErrorContentSearch") Set<String> postprocessorErrorContentSearches,
            @Param("responseErrorContentSearches") @ApiParam(value = "Searches the response error content of messages.") @QueryParam("responseErrorContentSearch") Set<String> responseErrorContentSearches,
            @Param("metaDataSearches") @ApiParam(value = "Searches a custom metadata column. Value should be in the form: COLUMN_NAME &lt;operator&gt; value, where operator is one of the following: =, !=, <, <=, >, >=, CONTAINS, DOES NOT CONTAIN, STARTS WITH, DOES NOT START WITH, ENDS WITH, DOES NOT END WITH") @QueryParam("metaDataSearch") Set<MetaDataSearch> metaDataSearches,
            @Param("metaDataCaseInsensitiveSearches") @ApiParam(value = "Searches a custom metadata column, ignoring case. Value should be in the form: COLUMN_NAME &lt;operator&gt; value.") @QueryParam("metaDataCaseInsensitiveSearch") Set<MetaDataSearch> metaDataCaseInsensitiveSearches,
            @Param("textSearchMetaDataColumns") @ApiParam(value = "When using a text search, these custom metadata columns will also be searched.") @QueryParam("textSearchMetaDataColumn") Set<String> textSearchMetaDataColumns,
            @Param("minSendAttempts") @ApiParam(value = "The minimum number of send attempts for connector messages.") @QueryParam("minSendAttempts") Integer minSendAttempts,
            @Param("maxSendAttempts") @ApiParam(value = "The maximum number of send attempts for connector messages.") @QueryParam("maxSendAttempts") Integer maxSendAttempts,
            @Param("attachment") @ApiParam(value = "If true, only messages with attachments are included in the results.") @QueryParam("attachment") Boolean attachment,
            @Param("error") @ApiParam(value = "If true, only messages with errors are included in the results.") @QueryParam("error") Boolean error,
            @Param("pageSize") @ApiParam(value = "The maximum number of messages that will be queried at a time.") @QueryParam("pageSize") int pageSize,
            @Param("contentType") @ApiParam(value = "The ContentType that will be extracted from the message for writing. If null or not provided, the entire message will be written in serialized format.") @QueryParam("contentType") ContentType contentType,
            @Param("destinationContent") @ApiParam(value = "If true, the content to write will be extracted from the destination message(s), rather than the source message.", defaultValue = "false") @QueryParam("destinationContent") boolean destinationContent,
            @Param("encrypt") @ApiParam(value = "If true, message content will be encrypted before writing.", defaultValue = "false") @QueryParam("encrypt") boolean encrypt,
            @Param("includeAttachments") @ApiParam(value = "Determines whether attachments will be included with messages.", defaultValue = "false") @QueryParam("includeAttachments") boolean includeAttachments,
            @Param("baseFolder") @ApiParam(value = "The base directory to use when resolving relative paths in the root folder.") @QueryParam("baseFolder") String baseFolder,
            @Param("rootFolder") @ApiParam(value = "The root folder to contain the written messages/sub-folders.") @QueryParam("rootFolder") String rootFolder,
            @Param("filePattern") @ApiParam(value = "A string defining the folder/filename(s) for writing messages. It may contain variables to be replaced.") @QueryParam("filePattern") String filePattern,
            @Param("archiveFileName") @ApiParam(value = "The file name to use for archive exports.") @QueryParam("archiveFileName") String archiveFileName,
            @Param("archiveFormat") @ApiParam(value = "The archiver format to use to archive messages/folders that are written to the root folder. Valid values: zip, tar") @QueryParam("archiveFormat") String archiveFormat,
            @Param("compressFormat") @ApiParam(value = "The compressor format to use to compress the archive file. Only valid when using the TAR archive format. Valid values: gz, bzip2") @QueryParam("compressFormat") String compressFormat,
            @Param("password") @ApiParam(value = "The password used to protect the archive file. Only valid when using the ZIP archive format.") @QueryParam("password") String password,
            @Param("encryptionType") @ApiParam(value = "The algorithm used to encrypt the password-protected archive file. Only valid when using the ZIP archive format. Valid values: STANDARD, AES128, AES256") @QueryParam("encryptionType") EncryptionType encryptionType) throws ClientException;
    // @formatter:on

    @POST
    @Path("/{channelId}/messages/{messageId}/attachments/{attachmentId}/_export")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation("Exports a message attachment into a specific file path accessible by the server.")
    @MirthOperation(name = "exportAttachment", display = "Export Attachment", permission = Permissions.MESSAGES_EXPORT_SERVER, type = ExecuteType.ASYNC)
    public void exportAttachmentServer(// @formatter:off
            @Param("channelId") @ApiParam(value = "The ID of the channel.", required = true) @PathParam("channelId") String channelId,
            @Param("messageId") @ApiParam(value = "The ID of the message.", required = true) @PathParam("messageId") Long messageId,
            @Param("attachmentId") @ApiParam(value = "The ID of the attachment.", required = true) @PathParam("attachmentId") String attachmentId,
            @Param("filePath") @ApiParam(value = "The file path to export the attachment to.", required = true) String filePath,
            @Param("binary") @ApiParam(value = "Indicates that the attachment is binary and should be Base64 decoded before writing to file.", defaultValue = "false") @QueryParam("binary") boolean binary) throws ClientException;
    // @formatter:on
}