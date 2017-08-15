/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.client.core.api.providers.MetaDataSearchParamConverterProvider.MetaDataSearch;
import com.mirth.connect.client.core.api.servlets.MessageServletInterface;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.model.MessageImportResult;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.model.filters.elements.ContentSearchElement;
import com.mirth.connect.model.filters.elements.MetaDataSearchElement;
import com.mirth.connect.server.api.CheckAuthorizedChannelId;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.server.controllers.MessageController;
import com.mirth.connect.server.util.DICOMMessageUtil;
import com.mirth.connect.util.MessageImporter.MessageImportException;
import com.mirth.connect.util.messagewriter.EncryptionType;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class MessageServlet extends MirthServlet implements MessageServletInterface {

    private static final Logger logger = Logger.getLogger(MessageServlet.class);
    private static final MessageController messageController = ControllerFactory.getFactory().createMessageController();
    private static final EngineController engineController = ControllerFactory.getFactory().createEngineController();

    public MessageServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc);
    }

    @Override
    @CheckAuthorizedChannelId
    public void processMessage(final String channelId, String rawData, Set<Integer> destinationMetaDataIds, Set<String> sourceMapEntries, boolean overwrite, boolean imported, Long originalMessageId) {
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        if (CollectionUtils.isNotEmpty(sourceMapEntries)) {
            for (String entry : sourceMapEntries) {
                int index = entry.indexOf('=');
                if (index > 0) {
                    sourceMap.put(entry.substring(0, index).trim(), entry.substring(index + 1).trim());
                }
            }
        }

        final RawMessage rawMessage = new RawMessage(rawData, destinationMetaDataIds, sourceMap);
        rawMessage.setOverwrite(overwrite);
        rawMessage.setImported(imported);
        rawMessage.setOriginalMessageId(originalMessageId);

        Runnable processTask = new Runnable() {
            @Override
            public void run() {
                try {
                    engineController.dispatchRawMessage(channelId, rawMessage, true, true);
                } catch (ChannelException e) {
                    // Do nothing. An error should have been logged.
                } catch (BatchMessageException e) {
                    logger.error("Error processing batch message", e);
                }
            }
        };

        // Process the message on a new thread so the client is not waiting for it to complete.
        new Thread(processTask, "Message Process Thread < " + Thread.currentThread().getName()).start();
    }

    @Override
    @CheckAuthorizedChannelId
    public void processMessage(final String channelId, final RawMessage rawMessage) {
        Runnable processTask = new Runnable() {
            @Override
            public void run() {
                try {
                    engineController.dispatchRawMessage(channelId, rawMessage, true, true);
                } catch (ChannelException e) {
                    // Do nothing. An error should have been logged.
                } catch (BatchMessageException e) {
                    logger.error("Error processing batch message", e);
                }
            }
        };

        // Process the message on a new thread so the client is not waiting for it to complete.
        new Thread(processTask, "Message Process Thread < " + Thread.currentThread().getName()).start();
    }

    @Override
    @CheckAuthorizedChannelId
    public Message getMessageContent(String channelId, Long messageId, List<Integer> metaDataIds) {
        return messageController.getMessageContent(channelId, messageId, metaDataIds);
    }

    @Override
    @CheckAuthorizedChannelId
    public List<Attachment> getAttachmentsByMessageId(String channelId, Long messageId, boolean includeContent) {
        if (includeContent) {
            return messageController.getMessageAttachment(channelId, messageId);
        } else {
            return messageController.getMessageAttachmentIds(channelId, messageId);
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public Attachment getAttachment(String channelId, Long messageId, String attachmentId) {
        return messageController.getMessageAttachment(channelId, attachmentId, messageId);
    }

    @Override
    @CheckAuthorizedChannelId
    public String getDICOMMessage(String channelId, Long messageId, ConnectorMessage message) {
        return DICOMMessageUtil.getDICOMRawData(message);
    }

    @Override
    @CheckAuthorizedChannelId
    public Long getMaxMessageId(String channelId) {
        return messageController.getMaxMessageId(channelId);
    }

    @Override
    @CheckAuthorizedChannelId
    public List<Message> getMessages(String channelId, MessageFilter filter, Boolean includeContent, Integer offset, Integer limit) {
        return messageController.getMessages(filter, channelId, includeContent, offset, limit);
    }

    @Override
    @CheckAuthorizedChannelId
    public List<Message> getMessages(String channelId, Long minMessageId, Long maxMessageId, Long minOriginalId, Long maxOriginalId, Long minImportId, Long maxImportId, Calendar startDate, Calendar endDate, String textSearch, Boolean textSearchRegex, Set<Status> statuses, Set<Integer> includedMetaDataIds, Set<Integer> excludedMetaDataIds, String serverId, Set<String> rawContentSearches, Set<String> processedRawContentSearches, Set<String> transformedContentSearches, Set<String> encodedContentSearches, Set<String> sentContentSearches, Set<String> responseContentSearches, Set<String> responseTransformedContentSearches, Set<String> processedResponseContentSearches, Set<String> connectorMapContentSearches, Set<String> channelMapContentSearches, Set<String> sourceMapContentSearches, Set<String> responseMapContentSearches, Set<String> processingErrorContentSearches, Set<String> postprocessorErrorContentSearches, Set<String> responseErrorContentSearches, Set<MetaDataSearch> metaDataSearches, Set<MetaDataSearch> metaDataCaseInsensitiveSearches, Set<String> textSearchMetaDataColumns, Integer minSendAttempts, Integer maxSendAttempts, Boolean attachment, Boolean error, Boolean includeContent, Integer offset, Integer limit) {
        MessageFilter filter = getMessageFilter(minMessageId, maxMessageId, minOriginalId, maxOriginalId, minImportId, maxImportId, startDate, endDate, textSearch, textSearchRegex, statuses, includedMetaDataIds, excludedMetaDataIds, serverId, rawContentSearches, processedRawContentSearches, transformedContentSearches, encodedContentSearches, sentContentSearches, responseContentSearches, responseTransformedContentSearches, processedResponseContentSearches, connectorMapContentSearches, channelMapContentSearches, sourceMapContentSearches, responseMapContentSearches, processingErrorContentSearches, postprocessorErrorContentSearches, responseErrorContentSearches, metaDataSearches, metaDataCaseInsensitiveSearches, textSearchMetaDataColumns, minSendAttempts, maxSendAttempts, attachment, error);
        return messageController.getMessages(filter, channelId, includeContent, offset, limit);
    }

    @Override
    @CheckAuthorizedChannelId
    public Long getMessageCount(String channelId, MessageFilter filter) {
        return messageController.getMessageCount(filter, channelId);
    }

    @Override
    @CheckAuthorizedChannelId
    public Long getMessageCount(String channelId, Long minMessageId, Long maxMessageId, Long minOriginalId, Long maxOriginalId, Long minImportId, Long maxImportId, Calendar startDate, Calendar endDate, String textSearch, Boolean textSearchRegex, Set<Status> statuses, Set<Integer> includedMetaDataIds, Set<Integer> excludedMetaDataIds, String serverId, Set<String> rawContentSearches, Set<String> processedRawContentSearches, Set<String> transformedContentSearches, Set<String> encodedContentSearches, Set<String> sentContentSearches, Set<String> responseContentSearches, Set<String> responseTransformedContentSearches, Set<String> processedResponseContentSearches, Set<String> connectorMapContentSearches, Set<String> channelMapContentSearches, Set<String> sourceMapContentSearches, Set<String> responseMapContentSearches, Set<String> processingErrorContentSearches, Set<String> postprocessorErrorContentSearches, Set<String> responseErrorContentSearches, Set<MetaDataSearch> metaDataSearches, Set<MetaDataSearch> metaDataCaseInsensitiveSearches, Set<String> textSearchMetaDataColumns, Integer minSendAttempts, Integer maxSendAttempts, Boolean attachment, Boolean error) {
        MessageFilter filter = getMessageFilter(minMessageId, maxMessageId, minOriginalId, maxOriginalId, minImportId, maxImportId, startDate, endDate, textSearch, textSearchRegex, statuses, includedMetaDataIds, excludedMetaDataIds, serverId, rawContentSearches, processedRawContentSearches, transformedContentSearches, encodedContentSearches, sentContentSearches, responseContentSearches, responseTransformedContentSearches, processedResponseContentSearches, connectorMapContentSearches, channelMapContentSearches, sourceMapContentSearches, responseMapContentSearches, processingErrorContentSearches, postprocessorErrorContentSearches, responseErrorContentSearches, metaDataSearches, metaDataCaseInsensitiveSearches, textSearchMetaDataColumns, minSendAttempts, maxSendAttempts, attachment, error);
        return messageController.getMessageCount(filter, channelId);
    }

    @Override
    @CheckAuthorizedChannelId
    public void reprocessMessages(String channelId, MessageFilter filter, boolean replace, boolean filterDestinations, Set<Integer> reprocessMetaDataIds) {
        doReprocessMessages(channelId, filter, replace, filterDestinations, reprocessMetaDataIds);
    }

    @Override
    @CheckAuthorizedChannelId
    public void reprocessMessages(String channelId, Long minMessageId, Long maxMessageId, Long minOriginalId, Long maxOriginalId, Long minImportId, Long maxImportId, Calendar startDate, Calendar endDate, String textSearch, Boolean textSearchRegex, Set<Status> statuses, Set<Integer> includedMetaDataIds, Set<Integer> excludedMetaDataIds, String serverId, Set<String> rawContentSearches, Set<String> processedRawContentSearches, Set<String> transformedContentSearches, Set<String> encodedContentSearches, Set<String> sentContentSearches, Set<String> responseContentSearches, Set<String> responseTransformedContentSearches, Set<String> processedResponseContentSearches, Set<String> connectorMapContentSearches, Set<String> channelMapContentSearches, Set<String> sourceMapContentSearches, Set<String> responseMapContentSearches, Set<String> processingErrorContentSearches, Set<String> postprocessorErrorContentSearches, Set<String> responseErrorContentSearches, Set<MetaDataSearch> metaDataSearches, Set<MetaDataSearch> metaDataCaseInsensitiveSearches, Set<String> textSearchMetaDataColumns, Integer minSendAttempts, Integer maxSendAttempts, Boolean attachment, Boolean error, boolean replace, boolean filterDestinations, Set<Integer> reprocessMetaDataIds) {
        final MessageFilter filter = getMessageFilter(minMessageId, maxMessageId, minOriginalId, maxOriginalId, minImportId, maxImportId, startDate, endDate, textSearch, textSearchRegex, statuses, includedMetaDataIds, excludedMetaDataIds, serverId, rawContentSearches, processedRawContentSearches, transformedContentSearches, encodedContentSearches, sentContentSearches, responseContentSearches, responseTransformedContentSearches, processedResponseContentSearches, connectorMapContentSearches, channelMapContentSearches, sourceMapContentSearches, responseMapContentSearches, processingErrorContentSearches, postprocessorErrorContentSearches, responseErrorContentSearches, metaDataSearches, metaDataCaseInsensitiveSearches, textSearchMetaDataColumns, minSendAttempts, maxSendAttempts, attachment, error);
        doReprocessMessages(channelId, filter, replace, filterDestinations, reprocessMetaDataIds);
    }

    @Override
    @CheckAuthorizedChannelId
    public void reprocessMessage(final String channelId, Long messageId, boolean replace, boolean filterDestinations, Set<Integer> reprocessMetaDataIds) {
        final MessageFilter filter = new MessageFilter();
        filter.setMinMessageId(messageId);
        filter.setMaxMessageId(messageId);
        doReprocessMessages(channelId, filter, replace, filterDestinations, reprocessMetaDataIds);
    }

    private void doReprocessMessages(final String channelId, final MessageFilter filter, final boolean replace, boolean filterDestinations, Set<Integer> reprocessMetaDataIds) {
        final Set<Integer> metaDataIds = filterDestinations ? reprocessMetaDataIds : null;

        Runnable reprocessTask = new Runnable() {
            @Override
            public void run() {
                try {
                    messageController.reprocessMessages(channelId, filter, replace, metaDataIds);
                } catch (ControllerException e) {
                    logger.error("Error reprocessing messages for channel " + channelId + ": " + e.getMessage(), e);
                }
            }
        };

        // Process the message on a new thread so the client is not waiting for it to complete.
        new Thread(reprocessTask, "Message Reprocess Thread < " + Thread.currentThread().getName()).start();
    }

    @Override
    @CheckAuthorizedChannelId
    public void removeMessages(String channelId, MessageFilter filter) {
        messageController.removeMessages(channelId, filter);
    }

    @Override
    @CheckAuthorizedChannelId
    public void removeMessages(String channelId, Long minMessageId, Long maxMessageId, Long minOriginalId, Long maxOriginalId, Long minImportId, Long maxImportId, Calendar startDate, Calendar endDate, String textSearch, Boolean textSearchRegex, Set<Status> statuses, Set<Integer> includedMetaDataIds, Set<Integer> excludedMetaDataIds, String serverId, Set<String> rawContentSearches, Set<String> processedRawContentSearches, Set<String> transformedContentSearches, Set<String> encodedContentSearches, Set<String> sentContentSearches, Set<String> responseContentSearches, Set<String> responseTransformedContentSearches, Set<String> processedResponseContentSearches, Set<String> connectorMapContentSearches, Set<String> channelMapContentSearches, Set<String> sourceMapContentSearches, Set<String> responseMapContentSearches, Set<String> processingErrorContentSearches, Set<String> postprocessorErrorContentSearches, Set<String> responseErrorContentSearches, Set<MetaDataSearch> metaDataSearches, Set<MetaDataSearch> metaDataCaseInsensitiveSearches, Set<String> textSearchMetaDataColumns, Integer minSendAttempts, Integer maxSendAttempts, Boolean attachment, Boolean error) {
        MessageFilter filter = getMessageFilter(minMessageId, maxMessageId, minOriginalId, maxOriginalId, minImportId, maxImportId, startDate, endDate, textSearch, textSearchRegex, statuses, includedMetaDataIds, excludedMetaDataIds, serverId, rawContentSearches, processedRawContentSearches, transformedContentSearches, encodedContentSearches, sentContentSearches, responseContentSearches, responseTransformedContentSearches, processedResponseContentSearches, connectorMapContentSearches, channelMapContentSearches, sourceMapContentSearches, responseMapContentSearches, processingErrorContentSearches, postprocessorErrorContentSearches, responseErrorContentSearches, metaDataSearches, metaDataCaseInsensitiveSearches, textSearchMetaDataColumns, minSendAttempts, maxSendAttempts, attachment, error);
        messageController.removeMessages(channelId, filter);
    }

    @Override
    @CheckAuthorizedChannelId
    public void removeMessage(String channelId, Long messageId, Integer metaDataId) {
        MessageFilter filter = new MessageFilter();
        filter.setMinMessageId(messageId);
        filter.setMaxMessageId(messageId);
        if (metaDataId != null) {
            List<Integer> metaDataIds = new ArrayList<Integer>();
            metaDataIds.add(metaDataId);
            filter.setIncludedMetaDataIds(metaDataIds);
        }
        messageController.removeMessages(channelId, filter);
    }

    @Override
    @CheckAuthorizedChannelId
    public void removeAllMessages(String channelId, boolean restartRunningChannels, boolean clearStatistics) {
        engineController.removeAllMessages(Collections.singleton(channelId), restartRunningChannels, clearStatistics, null);
    }

    @Override
    public void removeAllMessages(Set<String> channelIds, boolean restartRunningChannels, boolean clearStatistics) {
        engineController.removeAllMessages(redactChannelIds(channelIds), restartRunningChannels, clearStatistics, null);
    }

    @Override
    public void removeAllMessagesPost(Set<String> channelIds, boolean restartRunningChannels, boolean clearStatistics) {
        removeAllMessages(channelIds, restartRunningChannels, clearStatistics);
    }

    @Override
    @CheckAuthorizedChannelId
    public void importMessage(String channelId, Message message) {
        try {
            messageController.importMessage(channelId, message);
        } catch (MessageImportException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public MessageImportResult importMessagesServer(String channelId, String path, boolean includeSubfolders) {
        try {
            return messageController.importMessagesServer(channelId, path, includeSubfolders);
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public int exportMessagesServer(String channelId, MessageFilter filter, int pageSize, MessageWriterOptions writerOptions) {
        try {
            return messageController.exportMessages(channelId, filter, pageSize, writerOptions);
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public int exportMessagesServer(String channelId, Long minMessageId, Long maxMessageId, Long minOriginalId, Long maxOriginalId, Long minImportId, Long maxImportId, Calendar startDate, Calendar endDate, String textSearch, Boolean textSearchRegex, Set<Status> statuses, Set<Integer> includedMetaDataIds, Set<Integer> excludedMetaDataIds, String serverId, Set<String> rawContentSearches, Set<String> processedRawContentSearches, Set<String> transformedContentSearches, Set<String> encodedContentSearches, Set<String> sentContentSearches, Set<String> responseContentSearches, Set<String> responseTransformedContentSearches, Set<String> processedResponseContentSearches, Set<String> connectorMapContentSearches, Set<String> channelMapContentSearches, Set<String> sourceMapContentSearches, Set<String> responseMapContentSearches, Set<String> processingErrorContentSearches, Set<String> postprocessorErrorContentSearches, Set<String> responseErrorContentSearches, Set<MetaDataSearch> metaDataSearches, Set<MetaDataSearch> metaDataCaseInsensitiveSearches, Set<String> textSearchMetaDataColumns, Integer minSendAttempts, Integer maxSendAttempts, Boolean attachment, Boolean error, int pageSize, ContentType contentType, boolean destinationContent, boolean encrypt, boolean includeAttachments, String baseFolder, String rootFolder, String filePattern, String archiveFileName, String archiveFormat, String compressFormat, String password, EncryptionType encryptionType) {
        MessageFilter filter = getMessageFilter(minMessageId, maxMessageId, minOriginalId, maxOriginalId, minImportId, maxImportId, startDate, endDate, textSearch, textSearchRegex, statuses, includedMetaDataIds, excludedMetaDataIds, serverId, rawContentSearches, processedRawContentSearches, transformedContentSearches, encodedContentSearches, sentContentSearches, responseContentSearches, responseTransformedContentSearches, processedResponseContentSearches, connectorMapContentSearches, channelMapContentSearches, sourceMapContentSearches, responseMapContentSearches, processingErrorContentSearches, postprocessorErrorContentSearches, responseErrorContentSearches, metaDataSearches, metaDataCaseInsensitiveSearches, textSearchMetaDataColumns, minSendAttempts, maxSendAttempts, attachment, error);
        MessageWriterOptions writerOptions = getMessageWriterOptions(contentType, destinationContent, encrypt, includeAttachments, baseFolder, rootFolder, filePattern, archiveFileName, archiveFormat, compressFormat, password, encryptionType);
        try {
            return messageController.exportMessages(channelId, filter, pageSize, writerOptions);
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public void exportAttachmentServer(String channelId, Long messageId, String attachmentId, String filePath, boolean binary) {
        try {
            messageController.exportAttachment(channelId, attachmentId, messageId, filePath, binary);
        } catch (IOException e) {
            throw new MirthApiException(e);
        }
    }

    private MessageFilter getMessageFilter(Long minMessageId, Long maxMessageId, Long minOriginalId, Long maxOriginalId, Long minImportId, Long maxImportId, Calendar startDate, Calendar endDate, String textSearch, Boolean textSearchRegex, Set<Status> statuses, Set<Integer> includedMetaDataIds, Set<Integer> excludedMetaDataIds, String serverId, Set<String> rawContentSearches, Set<String> processedRawContentSearches, Set<String> transformedContentSearches, Set<String> encodedContentSearches, Set<String> sentContentSearches, Set<String> responseContentSearches, Set<String> responseTransformedContentSearches, Set<String> processedResponseContentSearches, Set<String> connectorMapContentSearches, Set<String> channelMapContentSearches, Set<String> sourceMapContentSearches, Set<String> responseMapContentSearches, Set<String> processingErrorContentSearches, Set<String> postprocessorErrorContentSearches, Set<String> responseErrorContentSearches, Set<MetaDataSearch> metaDataSearches, Set<MetaDataSearch> metaDataCaseInsensitiveSearches, Set<String> textSearchMetaDataColumns, Integer minSendAttempts, Integer maxSendAttempts, Boolean attachment, Boolean error) {
        MessageFilter filter = new MessageFilter();
        filter.setMinMessageId(minMessageId);
        filter.setMaxMessageId(maxMessageId);
        filter.setOriginalIdLower(minOriginalId);
        filter.setOriginalIdUpper(maxOriginalId);
        filter.setImportIdLower(minImportId);
        filter.setImportIdUpper(maxImportId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setTextSearch(textSearch);
        filter.setTextSearchRegex(textSearchRegex);
        if (CollectionUtils.isNotEmpty(statuses)) {
            filter.setStatuses(statuses);
        }
        if (CollectionUtils.isNotEmpty(includedMetaDataIds)) {
            filter.setIncludedMetaDataIds(new ArrayList<Integer>(includedMetaDataIds));
        }
        if (CollectionUtils.isNotEmpty(excludedMetaDataIds)) {
            filter.setExcludedMetaDataIds(new ArrayList<Integer>(excludedMetaDataIds));
        }
        filter.setServerId(serverId);

        List<ContentSearchElement> contentSearchList = new ArrayList<ContentSearchElement>();
        if (CollectionUtils.isNotEmpty(rawContentSearches)) {
            contentSearchList.add(new ContentSearchElement(ContentType.RAW.getContentTypeCode(), new ArrayList<String>(rawContentSearches)));
        }
        if (CollectionUtils.isNotEmpty(processedRawContentSearches)) {
            contentSearchList.add(new ContentSearchElement(ContentType.PROCESSED_RAW.getContentTypeCode(), new ArrayList<String>(processedRawContentSearches)));
        }
        if (CollectionUtils.isNotEmpty(transformedContentSearches)) {
            contentSearchList.add(new ContentSearchElement(ContentType.TRANSFORMED.getContentTypeCode(), new ArrayList<String>(transformedContentSearches)));
        }
        if (CollectionUtils.isNotEmpty(encodedContentSearches)) {
            contentSearchList.add(new ContentSearchElement(ContentType.ENCODED.getContentTypeCode(), new ArrayList<String>(encodedContentSearches)));
        }
        if (CollectionUtils.isNotEmpty(sentContentSearches)) {
            contentSearchList.add(new ContentSearchElement(ContentType.SENT.getContentTypeCode(), new ArrayList<String>(sentContentSearches)));
        }
        if (CollectionUtils.isNotEmpty(responseContentSearches)) {
            contentSearchList.add(new ContentSearchElement(ContentType.RESPONSE.getContentTypeCode(), new ArrayList<String>(responseContentSearches)));
        }
        if (CollectionUtils.isNotEmpty(responseTransformedContentSearches)) {
            contentSearchList.add(new ContentSearchElement(ContentType.RESPONSE_TRANSFORMED.getContentTypeCode(), new ArrayList<String>(responseTransformedContentSearches)));
        }
        if (CollectionUtils.isNotEmpty(processedResponseContentSearches)) {
            contentSearchList.add(new ContentSearchElement(ContentType.PROCESSED_RESPONSE.getContentTypeCode(), new ArrayList<String>(processedResponseContentSearches)));
        }
        if (CollectionUtils.isNotEmpty(connectorMapContentSearches)) {
            contentSearchList.add(new ContentSearchElement(ContentType.CONNECTOR_MAP.getContentTypeCode(), new ArrayList<String>(connectorMapContentSearches)));
        }
        if (CollectionUtils.isNotEmpty(channelMapContentSearches)) {
            contentSearchList.add(new ContentSearchElement(ContentType.CHANNEL_MAP.getContentTypeCode(), new ArrayList<String>(channelMapContentSearches)));
        }
        if (CollectionUtils.isNotEmpty(sourceMapContentSearches)) {
            contentSearchList.add(new ContentSearchElement(ContentType.SOURCE_MAP.getContentTypeCode(), new ArrayList<String>(sourceMapContentSearches)));
        }
        if (CollectionUtils.isNotEmpty(responseMapContentSearches)) {
            contentSearchList.add(new ContentSearchElement(ContentType.RESPONSE_MAP.getContentTypeCode(), new ArrayList<String>(responseMapContentSearches)));
        }
        if (CollectionUtils.isNotEmpty(processingErrorContentSearches)) {
            contentSearchList.add(new ContentSearchElement(ContentType.PROCESSING_ERROR.getContentTypeCode(), new ArrayList<String>(processingErrorContentSearches)));
        }
        if (CollectionUtils.isNotEmpty(postprocessorErrorContentSearches)) {
            contentSearchList.add(new ContentSearchElement(ContentType.POSTPROCESSOR_ERROR.getContentTypeCode(), new ArrayList<String>(postprocessorErrorContentSearches)));
        }
        if (CollectionUtils.isNotEmpty(responseErrorContentSearches)) {
            contentSearchList.add(new ContentSearchElement(ContentType.RESPONSE_ERROR.getContentTypeCode(), new ArrayList<String>(responseErrorContentSearches)));
        }
        filter.setContentSearch(contentSearchList);

        List<MetaDataSearchElement> metaDataSearchList = new ArrayList<MetaDataSearchElement>();
        if (CollectionUtils.isNotEmpty(metaDataSearches)) {
            for (MetaDataSearch search : metaDataSearches) {
                metaDataSearchList.add(new MetaDataSearchElement(search.getColumnName(), search.getOperator().toFullString(), search.getValue(), false));
            }
        }
        if (CollectionUtils.isNotEmpty(metaDataCaseInsensitiveSearches)) {
            for (MetaDataSearch search : metaDataCaseInsensitiveSearches) {
                metaDataSearchList.add(new MetaDataSearchElement(search.getColumnName(), search.getOperator().toFullString(), search.getValue(), true));
            }
        }
        filter.setMetaDataSearch(metaDataSearchList);

        filter.setTextSearchMetaDataColumns(new ArrayList<String>(textSearchMetaDataColumns));
        filter.setSendAttemptsLower(minSendAttempts);
        filter.setSendAttemptsUpper(maxSendAttempts);
        filter.setAttachment(attachment);
        filter.setError(error);

        return filter;
    }

    private MessageWriterOptions getMessageWriterOptions(ContentType contentType, boolean destinationContent, boolean encrypt, boolean includeAttachments, String baseFolder, String rootFolder, String filePattern, String archiveFileName, String archiveFormat, String compressFormat, String password, EncryptionType encryptionType) {
        MessageWriterOptions writerOptions = new MessageWriterOptions();
        writerOptions.setContentType(contentType);
        writerOptions.setDestinationContent(destinationContent);
        writerOptions.setEncrypt(encrypt);
        writerOptions.setIncludeAttachments(includeAttachments);
        if (StringUtils.isNotEmpty(baseFolder)) {
            writerOptions.setBaseFolder(baseFolder);
        } else {
            writerOptions.setBaseFolder(System.getProperty("user.dir"));
        }
        writerOptions.setRootFolder(rootFolder);
        writerOptions.setFilePattern(filePattern);
        writerOptions.setArchiveFileName(archiveFileName);
        writerOptions.setArchiveFormat(archiveFormat);
        writerOptions.setCompressFormat(compressFormat);
        writerOptions.setPassword(password);
        writerOptions.setEncryptionType(encryptionType);

        return writerOptions;
    }
}