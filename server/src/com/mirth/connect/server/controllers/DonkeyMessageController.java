/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.channel.ChannelState;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.DataType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.server.util.AttachmentUtil;
import com.mirth.connect.server.util.DICOMUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.MessageEncryptionUtil;
import com.mirth.connect.util.export.MessageExportOptions;
import com.mirth.connect.util.export.MessageExporter;
import com.mirth.connect.util.export.MessageExporter.MessageExporterException;
import com.mirth.connect.util.export.MessageRetriever;

public class DonkeyMessageController extends MessageController {
    private static DonkeyMessageController instance = null;

    public static MessageController create() {
        synchronized (DonkeyMessageController.class) {
            if (instance == null) {
                instance = new DonkeyMessageController();
            }

            return instance;
        }
    }

    private Logger logger = Logger.getLogger(this.getClass());

    private DonkeyMessageController() {}

    private Map<String, Object> getParameters(MessageFilter filter, String channelId, Integer offset, Integer limit) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("messageIdUpper", filter.getMessageIdUpper());
        params.put("messageIdLower", filter.getMessageIdLower());
        params.put("importIdUpper", filter.getImportIdUpper());
        params.put("importIdLower", filter.getImportIdLower());
        params.put("startDate", filter.getStartDate());
        params.put("endDate", filter.getEndDate());
        params.put("quickSearch", filter.getQuickSearch());
        params.put("statuses", filter.getStatuses());
        params.put("sendAttemptsLower", filter.getSendAttemptsLower());
        params.put("sendAttemptsUpper", filter.getSendAttemptsUpper());
        params.put("type", filter.getType());
        params.put("source", filter.getSource());
        params.put("rawSearch", filter.getContentSearch().get(ContentType.RAW));
        params.put("processedRawSearch", filter.getContentSearch().get(ContentType.PROCESSED_RAW));
        params.put("transformedSearch", filter.getContentSearch().get(ContentType.TRANSFORMED));
        params.put("encodedSearch", filter.getContentSearch().get(ContentType.ENCODED));
        params.put("sentSearch", filter.getContentSearch().get(ContentType.SENT));
        params.put("responseSearch", filter.getContentSearch().get(ContentType.RESPONSE));
        params.put("processedResponseSearch", filter.getContentSearch().get(ContentType.PROCESSED_RESPONSE));
        params.put("metaDataIds", filter.getMetaDataIds());
        params.put("serverId", filter.getServerId());
        params.put("maxMessageId", filter.getMaxMessageId());
        params.put("metaDataSearch", filter.getMetaDataSearch());
        params.put("attachment", filter.getAttachment());

        return params;
    }

    @Override
    public long getMaxMessageId(String channelId) {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();
        
        try {
            return dao.getMaxMessageId(channelId);
        } finally {
            dao.close();
        }
    }

    @Override
    public Long getMessageCount(MessageFilter filter, Channel channel) {
        if (filter.getMetaDataIds() != null && filter.getMetaDataIds().isEmpty()) {
            return 0L;
        }

        return SqlConfig.getSqlSessionManager().selectOne("Message.searchMessagesCount", getParameters(filter, channel.getChannelId(), null, null));
    }

    private Set<Integer> getMetaDataIdsFromString(String metaDataIds) {
        if (metaDataIds == null) {
            return null;
        }

        String[] pieces = StringUtils.split(metaDataIds, ',');
        Set<Integer> list = new HashSet<Integer>();

        for (String piece : pieces) {
            list.add(Integer.parseInt(piece));
        }

        return list;
    }

    @Override
    public List<Message> getMessages(MessageFilter filter, Channel channel, Boolean includeContent, Integer offset, Integer limit) {
        List<Message> messages = new ArrayList<Message>();
        SqlSession session = SqlConfig.getSqlSessionManager();
        String channelId = channel.getChannelId();

        if (filter.getMetaDataIds() != null && filter.getMetaDataIds().isEmpty()) {
            return messages;
        }

        Map<String, Object> params = getParameters(filter, channelId, offset, limit);
        List<Map<String, Object>> rows = session.selectList("Message.searchMessages", params);

        Long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);

        for (Map<String, Object> row : rows) {
            Calendar dateCreated = Calendar.getInstance();
            dateCreated.setTimeInMillis(((Timestamp) row.get("date_created")).getTime());

            Message message = new Message();
            message.setMessageId((Long) row.get("message_id"));
            message.setChannelId(channelId);
            message.setDateCreated(dateCreated);
            message.setServerId((String) row.get("server_id"));
            message.setProcessed((Boolean) row.get("processed"));
            message.setImportId((Long) row.get("import_id"));
            message.setImportChannelId((String) row.get("import_channel_id"));
            message.setAttemptedResponse((Boolean) row.get("attempted_response"));
            message.setResponseError((String) row.get("response_error"));

            Set<Integer> metaDataIds = getMetaDataIdsFromString((String) row.get("metadata_ids"));

            params = new HashMap<String, Object>();
            params.put("localChannelId", localChannelId);
            params.put("messageId", message.getMessageId());
            params.put("metaDataIds", metaDataIds);
            List<ConnectorMessage> connectorMessages = session.selectList("Message.selectConnectorMessagesByIds", params);

            for (ConnectorMessage connectorMessage : connectorMessages) {
                connectorMessage.setChannelId(channelId);
                
                message.getConnectorMessages().put(connectorMessage.getMetaDataId(), connectorMessage);
            }
            
            if (includeContent) {
                List<MessageContent> contentList = session.selectList("Message.selectMessageContent", params);
                
                for (MessageContent messageContent : contentList) {
                    messageContent.setChannelId(channel.getChannelId());
                    message.getConnectorMessages().get(messageContent.getMetaDataId()).setContent(messageContent);
                }
            }

            messages.add(message);
        }

        return messages;
    }

    @Override
    public Message getMessageContent(String channelId, Long messageId) {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();
        
        try {
        	Map<String, Object> params = new HashMap<String, Object>();
        	params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));
        	params.put("messageId", messageId);
        	
            Message message = new Message();
            message.setChannelId(channelId);
            message.setMessageId(messageId);
            
            Map<String, Object> row = SqlConfig.getSqlSessionManager().selectOne("Message.selectMessageById", params);
            
            if (row != null) {
	            Calendar dateCreated = Calendar.getInstance();
	            dateCreated.setTimeInMillis(((Timestamp) row.get("date_created")).getTime());
	            
	            message.setDateCreated(dateCreated);
	            message.setServerId((String) row.get("server_id"));
	            message.setProcessed((Boolean) row.get("processed"));
	            message.setImportId((Long) row.get("import_id"));
	            message.setImportChannelId((String) row.get("import_channel_id"));
	            message.setAttemptedResponse((Boolean) row.get("attempted_response"));
	            message.setResponseError((String) row.get("response_error"));
            }
            
            Map<Integer, ConnectorMessage> connectorMessages = dao.getConnectorMessages(channelId, messageId);
            Encryptor encryptor = ConfigurationController.getInstance().getEncryptor();
    
            for (Entry<Integer, ConnectorMessage> connectorMessageEntry : connectorMessages.entrySet()) {
                Integer metaDataId = connectorMessageEntry.getKey();
                ConnectorMessage connectorMessage = connectorMessageEntry.getValue();
                
                MessageEncryptionUtil.decryptConnectorMessage(connectorMessage, encryptor);
                message.getConnectorMessages().put(metaDataId, connectorMessage);
            }
    
            return message;
        } finally {
            dao.close();
        }
    }

    @Override
    public List<Attachment> getMessageAttachmentIds(String channelId, Long messageId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));
        params.put("messageId", messageId);

        return SqlConfig.getSqlSessionManager().selectList("Message.selectMessageAttachmentIds", params);
    }

    @Override
    public Attachment getMessageAttachment(String channelId, String attachmentId) {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            return dao.getMessageAttachment(channelId, attachmentId);
        } finally {
            dao.close();
        }
    }

    @Override
    public List<Attachment> getMessageAttachment(String channelId, Long messageId) {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            return dao.getMessageAttachment(channelId, messageId);
        } finally {
            dao.close();
        }
    }

    @Override
    public int removeMessages(String channelId, MessageFilter filter) {
    	// Perform the deletes in batches rather than all in one transaction.
    	//TODO Tune the limit to use for each batch in the delete.
        Map<String, Object> params = getParameters(filter, channelId, null, 100000);
        
        Channel channel = ControllerFactory.getFactory().createEngineController().getDeployedChannel(channelId);
        if (channel != null) {
	        List<Map<String, Object>> rows = null;
	        Long maxMessageId = filter.getMaxMessageId();
	        do {
	        	// Prevent the delete from occurring at the same time as the channel being started. 
		        synchronized (channel) {
		        	params.put("maxMessageId", maxMessageId);
		        	
			        // Perform a search using the message filter parameters
			        rows = SqlConfig.getSqlSessionManager().selectList("Message.searchMessages", params);
			        Map<Long, Set<Integer>> messages = new HashMap<Long, Set<Integer>>();

			        // For each message that was retrieved
			        for (Map<String, Object> row : rows) {
			            Long messageId = (Long) row.get("message_id");
			            Set<Integer> metaDataIds = getMetaDataIdsFromString((String) row.get("metadata_ids"));
			            Boolean processed = (Boolean) row.get("processed");
			            
			            if (maxMessageId == null || maxMessageId >= messageId) {
			            	maxMessageId = messageId - 1;
			            }
			
			            // Allow unprocessed messages to be deleted only if the channel is stopped.
			            if (channel.getCurrentState() == ChannelState.STOPPED || processed) {
				            if (metaDataIds.contains(0)) {
				                // Delete the entire message if the source connector message is to be deleted
				                messages.put(messageId, null);
				            } else {
				                // Otherwise only deleted the destination connector message
				                messages.put(messageId, metaDataIds);
				            }
			            }
			        }
			
			        com.mirth.connect.donkey.server.controllers.MessageController.getInstance().deleteMessages(channelId, messages, false);
		        }
	        } while (rows != null && rows.size() > 0);
	        
	        // Invalidate the queue buffer to ensure stats are updated.
            channel.invalidateQueues();
        }
        //TODO Decide what to return
        return 0;
    }

    @Override
    public boolean clearMessages(String channelId) throws ControllerException {
        logger.debug("clearing messages: channelId=" + channelId);
        Channel channel = ControllerFactory.getFactory().createEngineController().getDeployedChannel(channelId);
        boolean cleared = false;
        if (channel != null) {
        	// Prevent the delete from occurring at the same time as the channel being started. 
        	synchronized (channel) {
        		// Only allow the messages to be cleared if the channel is stopped.
        		if (channel.getCurrentState() == ChannelState.STOPPED) {
		        	DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();
			        try {
			            dao.deleteAllMessages(channelId);
			            dao.commit();
			            cleared = true;
			            
		            	// Invalidate the queue buffer to ensure stats are updated.
		                channel.invalidateQueues();
			        } finally {
			            dao.close();
			        }
        		} else {
        			logger.warn("Cannot remove all messages for channel " + channel.getName() + " (" + channel.getChannelId() + ") because the channel is not stopped.");
                }
        	}
        }
        
        return cleared;
    }

    @Override
    public void reprocessMessages(String channelId, MessageFilter filter, boolean replace, List<Integer> reprocessMetaDataIds) {
        EngineController engineController = ControllerFactory.getFactory().createEngineController();
        DataType dataType = engineController.getDeployedChannel(channelId).getSourceConnector().getInboundDataType();
        Encryptor encryptor = ConfigurationController.getInstance().getEncryptor();
        
        Map<String, Object> params = getParameters(filter, channelId, null, null);
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));
        
        List<Long> messageIds = SqlConfig.getSqlSessionManager().selectList("Message.selectMessageIdsForReprocessing", params);
        
        params.clear();
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));
        
        for (Long messageId : messageIds) {
            params.put("messageId", messageId);
            Map<String, Object> messageResult = SqlConfig.getSqlSessionManager().selectOne("Message.selectMessageForReprocessing", params);
            
            String rawContent = (String) messageResult.get("content");
            String encryptedRawContent = null;
            RawMessage rawMessage = null;

            if ((Boolean) messageResult.get("is_encrypted")) {
                encryptedRawContent = rawContent;
                rawContent = encryptor.decrypt(encryptedRawContent);
            }
            
            ConnectorMessage connectorMessage = new ConnectorMessage();
            connectorMessage.setChannelId(channelId);
            connectorMessage.setMessageId(messageId);
            connectorMessage.setMetaDataId(0);
            connectorMessage.setRaw(new MessageContent(channelId, messageId, 0, ContentType.RAW, rawContent, dataType.getType(), encryptedRawContent));
            
            if (ExtensionController.getInstance().getDataTypePlugins().get(dataType.getType()).isBinary()) {
                rawMessage = new RawMessage(DICOMUtil.getDICOMRawBytes(connectorMessage));
            } else {
                rawMessage = new RawMessage(org.apache.commons.codec.binary.StringUtils.newString(AttachmentUtil.reAttachMessage(rawContent, connectorMessage, Constants.ATTACHMENT_CHARSET, false), Constants.ATTACHMENT_CHARSET));
            }

            if (replace) {
                rawMessage.setMessageIdToOverwrite(messageId);
            }
            
            rawMessage.setDestinationMetaDataIds(reprocessMetaDataIds);

            try {
                engineController.dispatchRawMessage(channelId, rawMessage);
            } catch (Throwable e) {
                logger.error(e);
            }
        }
    }

    @Override
    public int exportMessages(MessageExportOptions options) throws MessageExporterException {
        final MessageController messageController = this;
        final EngineController engineController = ControllerFactory.getFactory().createEngineController();
        
        MessageExporter messageExporter = new MessageExporter();
        messageExporter.setOptions(options);
        messageExporter.setSerializer(new ObjectXMLSerializer());
        messageExporter.setEncryptor(ConfigurationController.getInstance().getEncryptor());
        messageExporter.setMessageRetriever(new MessageRetriever() {
            @Override
            public List<Message> getMessages(String channelId, MessageFilter filter, boolean includeContent, Integer offset, Integer limit) {
                return messageController.getMessages(filter, engineController.getDeployedChannel(channelId), includeContent, offset, limit);
            }
        });
        
        return messageExporter.export();
    }
    
    @Override
    public void importMessage(String channelId, Message message) throws MessageImportException {
        try {
            MessageEncryptionUtil.decryptMessage(message, ConfigurationController.getInstance().getEncryptor());
            com.mirth.connect.donkey.server.controllers.MessageController.getInstance().importMessage(channelId, message);
        } catch (DonkeyException e) {
            throw new MessageImportException(e);
        }
    }

    @Override
    public int pruneMessages(List<String> channelIds, int limit) throws MessagePrunerException {
        // TODO Auto-generated method stub
        return 0;
    }
}
