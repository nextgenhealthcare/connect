package com.mirth.connect.donkey.server.data.jdbc;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.data.DonkeyDaoException;
import com.mirth.connect.donkey.server.data.StatisticsUpdater;
import com.mirth.connect.donkey.util.SerializerProvider;

public class OracleJdbcDao extends JdbcDao {

	public OracleJdbcDao(Donkey donkey, Connection connection, QuerySource querySource,
			PreparedStatementSource statementSource, SerializerProvider serializerProvider, boolean encryptData,
			boolean decryptData, StatisticsUpdater statisticsUpdater, Statistics currentStats, Statistics totalStats,
			String statsServerId) {
		super(donkey, connection, querySource, statementSource, serializerProvider, encryptData, decryptData,
				statisticsUpdater, currentStats, totalStats, statsServerId);
	}

	@Override
	public void insertMessage(Message message) {
		getLogger().debug(message.getChannelId() + "/" + message.getMessageId() + ": inserting message");

		PreparedStatement statement = null;
		try {
			statement = prepareStatement("insertMessage", message.getChannelId());
			statement.setLong(1, message.getMessageId());
			statement.setString(2, message.getServerId());
			statement.setTimestamp(3, new Timestamp(message.getReceivedDate().getTimeInMillis()));
			statement.setBoolean(4, message.isProcessed());

			Long originalId = message.getOriginalId();

			if (originalId != null) {
				statement.setLong(5, originalId);
			} else {
				statement.setNull(5, Types.BIGINT);
			}

			Long importId = message.getImportId();

			if (importId != null) {
				statement.setLong(6, importId);
			} else {
				statement.setNull(6, Types.BIGINT);
			}

			String importChannelId = message.getImportChannelId();

			if (importChannelId != null) {
				statement.setString(7, message.getImportChannelId());
			} else {
				statement.setNull(7, Types.VARCHAR);
			}

			statement.executeUpdate();
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	public void updateSendAttempts(ConnectorMessage connectorMessage) {
		getLogger().debug(
				connectorMessage.getChannelId() + "/" + connectorMessage.getMessageId() + ": updating send attempts");

		Calendar sendDate = connectorMessage.getSendDate();
		Calendar responseDate = connectorMessage.getResponseDate();

		PreparedStatement statement = null;
		try {
			statement = prepareStatement("updateSendAttempts", connectorMessage.getChannelId());
			statement.setInt(1, connectorMessage.getSendAttempts());
			statement.setTimestamp(2, sendDate == null ? null : new Timestamp(sendDate.getTimeInMillis()));
			statement.setTimestamp(3, responseDate == null ? null : new Timestamp(responseDate.getTimeInMillis()));
			statement.setInt(4, connectorMessage.getMetaDataId());
			statement.setLong(5, connectorMessage.getMessageId());
			statement.setString(6, connectorMessage.getServerId());
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	public void batchInsertMessageContent(MessageContent messageContent) {
		getLogger().debug(messageContent.getChannelId() + "/" + messageContent.getMessageId() + "/"
				+ messageContent.getMetaDataId() + ": batch inserting message content ("
				+ messageContent.getContentType().toString() + ")");

		PreparedStatement statement = null;
		try {
			String content;
			boolean encrypted;

			// Only encrypt if the content is not already encrypted
			if (isEncryptData() && getEncryptor() != null && !messageContent.isEncrypted()) {
				content = getEncryptor().encrypt(messageContent.getContent());
				encrypted = true;
			} else {
				content = messageContent.getContent();
				encrypted = messageContent.isEncrypted();
			}

			statement = prepareStatement("batchInsertMessageContent", messageContent.getChannelId());
			statement.setInt(1, messageContent.getMetaDataId());
			statement.setLong(2, messageContent.getMessageId());
			statement.setInt(3, messageContent.getContentType().getContentTypeCode());
			statement.setString(4, content);
			statement.setString(5, messageContent.getDataType());
			statement.setBoolean(6, encrypted);

			statement.addBatch();
			statement.clearParameters();
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	public void executeBatchInsertMessageContent(String channelId) {
		getLogger().debug(channelId + ": executing batch message content insert");

		PreparedStatement statement = null;
		try {
			statement = prepareStatement("batchInsertMessageContent", channelId);
			statement.executeBatch();
			statement.clearBatch();
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	protected void insertContent(String channelId, long messageId, int metaDataId, ContentType contentType,
			String content, String dataType, boolean encrypted) {
		PreparedStatement statement = null;
		try {
			// Only encrypt if the content is not already encrypted
			if (isEncryptData() && getEncryptor() != null && !encrypted) {
				content = getEncryptor().encrypt(content);
				encrypted = true;
			}

			statement = prepareStatement("insertMessageContent", channelId);
			statement.setInt(1, metaDataId);
			statement.setLong(2, messageId);
			statement.setInt(3, contentType.getContentTypeCode());
			statement.setString(4, content);
			statement.setString(5, dataType);
			statement.setBoolean(6, encrypted);

			statement.executeUpdate();
			statement.clearParameters();
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	public void storeContent(String channelId, long messageId, int metaDataId, ContentType contentType, String content,
			String dataType, boolean encrypted) {
		PreparedStatement statement = null;
		try {
			// Only encrypt if the content is not already encrypted
			if (isEncryptData() && getEncryptor() != null && !encrypted) {
				content = getEncryptor().encrypt(content);
				encrypted = true;
			}

			statement = prepareStatement("storeMessageContent", channelId);

			if (content == null) {
				statement.setNull(1, Types.LONGVARCHAR);
			} else {
				statement.setString(1, content);
			}

			statement.setString(2, dataType);
			statement.setBoolean(3, encrypted);
			statement.setInt(4, metaDataId);
			statement.setLong(5, messageId);
			statement.setInt(6, contentType.getContentTypeCode());

			int rowCount = statement.executeUpdate();
			statement.clearParameters();

			if (rowCount == 0) {
				// This is the same code as insertContent, without going through
				// the encryption process again
				getLogger().debug(channelId + "/" + messageId + "/" + metaDataId + ": updating message content ("
						+ contentType.toString() + ")");

				close(statement);
				statement = prepareStatement("insertMessageContent", channelId);
				statement.setInt(1, metaDataId);
				statement.setLong(2, messageId);
				statement.setInt(3, contentType.getContentTypeCode());
				statement.setString(4, content);
				statement.setString(5, dataType);
				statement.setBoolean(6, encrypted);

				statement.executeUpdate();
				statement.clearParameters();
			}
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	protected void updateStatistics(String channelId, Integer metaDataId, Map<Status, Long> stats) {
		long received = stats.get(Status.RECEIVED);
		long filtered = stats.get(Status.FILTERED);
		long sent = stats.get(Status.SENT);
		long error = stats.get(Status.ERROR);

		getLogger().debug(channelId + "/" + metaDataId + ": saving statistics");

		PreparedStatement statement = null;

		try {
			/*
			 * Indicates whether case statements are used in the update
			 * statement, in which case the number of bound parameters for each
			 * statistic will double.
			 */
			boolean usingCase = false;

			if (metaDataId == null) {
				if (getQuerySource().queryExists("updateChannelStatisticsWithCase")) {
					usingCase = true;
					statement = prepareStatement("updateChannelStatisticsWithCase", channelId);
				} else {
					statement = prepareStatement("updateChannelStatistics", channelId);
				}
			} else {
				if (getQuerySource().queryExists("updateConnectorStatisticsWithCase")) {
					usingCase = true;
					statement = prepareStatement("updateConnectorStatisticsWithCase", channelId);
				} else {
					statement = prepareStatement("updateConnectorStatistics", channelId);
				}
			}

			// Keep track of the index since it will change depending on whether
			// case statements are used
			int paramIndex = 1;

			if (usingCase) {
				statement.setLong(paramIndex++, received);
				statement.setLong(paramIndex++, received);
				statement.setLong(paramIndex++, received);
				statement.setLong(paramIndex++, received);
				statement.setLong(paramIndex++, filtered);
				statement.setLong(paramIndex++, filtered);
				statement.setLong(paramIndex++, filtered);
				statement.setLong(paramIndex++, filtered);
				statement.setLong(paramIndex++, sent);
				statement.setLong(paramIndex++, sent);
				statement.setLong(paramIndex++, sent);
				statement.setLong(paramIndex++, sent);
				statement.setLong(paramIndex++, error);
				statement.setLong(paramIndex++, error);
				statement.setLong(paramIndex++, error);
				statement.setLong(paramIndex++, error);
			} else {
				statement.setLong(paramIndex++, received);
				statement.setLong(paramIndex++, received);
				statement.setLong(paramIndex++, filtered);
				statement.setLong(paramIndex++, filtered);
				statement.setLong(paramIndex++, sent);
				statement.setLong(paramIndex++, sent);
				statement.setLong(paramIndex++, error);
				statement.setLong(paramIndex++, error);
			}

			if (metaDataId != null) {
				statement.setInt(paramIndex++, metaDataId);
				statement.setString(paramIndex++, getStatsServerId());
			} else {
				statement.setString(paramIndex++, getStatsServerId());
			}

			if (statement.executeUpdate() == 0) {
				close(statement);
				statement = prepareStatement("insertChannelStatistics", channelId);

				if (metaDataId == null) {
					statement.setNull(1, Types.INTEGER);
				} else {
					statement.setInt(1, metaDataId);
				}

				statement.setString(2, getStatsServerId());
				statement.setLong(3, received);
				statement.setLong(4, received);
				statement.setLong(5, filtered);
				statement.setLong(6, filtered);
				statement.setLong(7, sent);
				statement.setLong(8, sent);
				statement.setLong(9, error);
				statement.setLong(10, error);
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	public void insertMessageAttachment(String channelId, long messageId, Attachment attachment) {
		getLogger().debug(channelId + "/" + messageId + ": inserting message attachment");

		PreparedStatement statement = null;
		try {
			statement = prepareStatement("insertMessageAttachment", channelId);
			statement.setString(1, attachment.getId());
			statement.setLong(2, messageId);
			statement.setString(3, attachment.getType());

			// The size of each segment of the attachment.
			int chunkSize = 10000000;

			if (attachment.getContent().length <= chunkSize) {
				// If there is only one segment, just store it
				statement.setInt(4, 1);
				statement.setInt(5, attachment.getContent().length);
				statement.setBytes(6, attachment.getContent());
				statement.executeUpdate();
			} else {
				// Use an input stream on the attachment content to segment the
				// data.
				ByteArrayInputStream inputStream = new ByteArrayInputStream(attachment.getContent());
				// The order of the segment
				int segmentIndex = 1;

				// As long as there are bytes left
				while (inputStream.available() > 0) {
					// Set the segment number
					statement.setInt(4, segmentIndex++);
					// Determine the segment size. If there are more bytes left
					// than the chunk size, the size is the chunk size.
					// Otherwise it is the number of remaining bytes
					int segmentSize = Math.min(chunkSize, inputStream.available());
					// Create a byte array to store the chunk
					byte[] segment = new byte[segmentSize];
					// Read the chunk from the input stream to the byte array
					inputStream.read(segment, 0, segmentSize);
					// Set the segment size
					statement.setInt(5, segmentSize);
					// Set the byte data
					statement.setBytes(6, segment);
					// Perform the insert
					statement.executeUpdate();
				}
			}

			// Clear the parameters because the data held in memory could be
			// quite large.
			statement.clearParameters();
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	public void updateMessageAttachment(String channelId, long messageId, Attachment attachment) {
		getLogger().debug(channelId + "/" + messageId + ": updating message attachment");

		PreparedStatement segmentCountStatement = null;
		ResultSet segmentCountResult = null;
		PreparedStatement updateStatement = null;
		PreparedStatement insertStatement = null;

		try {
			segmentCountStatement = prepareStatement("selectMessageAttachmentSegmentCount", channelId);
			segmentCountStatement.setString(1, attachment.getId());
			segmentCountStatement.setLong(2, messageId);

			segmentCountResult = segmentCountStatement.executeQuery();
			segmentCountResult.next();
			int totalSegmentCount = segmentCountResult.getInt(1);

			updateStatement = prepareStatement("updateMessageAttachment", channelId);
			updateStatement.setString(1, attachment.getType());
			updateStatement.setString(4, attachment.getId());
			updateStatement.setLong(5, messageId);

			insertStatement = prepareStatement("insertMessageAttachment", channelId);
			insertStatement.setString(1, attachment.getId());
			insertStatement.setLong(2, messageId);
			insertStatement.setString(3, attachment.getType());

			// The size of each segment of the attachment.
			int chunkSize = 10000000;

			// Use an input stream on the attachment content to segment the
			// data.
			ByteArrayInputStream inputStream = null;

			boolean done = false;
			int currentSegmentId = 1;

			while (!done) {
				PreparedStatement statement;
				int segmentIdIndex;
				int attachmentSizeIndex;
				int contentIndex;

				if (currentSegmentId <= totalSegmentCount) {
					statement = updateStatement;
					segmentIdIndex = 6;
					attachmentSizeIndex = 2;
					contentIndex = 3;
				} else {
					statement = insertStatement;
					segmentIdIndex = 4;
					attachmentSizeIndex = 5;
					contentIndex = 6;
				}

				// Set the segment number
				statement.setInt(segmentIdIndex, currentSegmentId);

				if (attachment.getContent().length <= chunkSize) {
					// If there is only one segment, just store it
					statement.setInt(attachmentSizeIndex, attachment.getContent().length);
					statement.setBytes(contentIndex, attachment.getContent());
					statement.executeUpdate();
					currentSegmentId++;
					done = true;
				} else {
					if (inputStream == null) {
						inputStream = new ByteArrayInputStream(attachment.getContent());
					}

					// As long as there are bytes left
					if (inputStream.available() > 0) {
						// Determine the segment size. If there are more bytes
						// left than the chunk size, the size is the chunk size.
						// Otherwise it is the number of remaining bytes
						int segmentSize = Math.min(chunkSize, inputStream.available());
						// Create a byte array to store the chunk
						byte[] segment = new byte[segmentSize];
						// Read the chunk from the input stream to the byte
						// array
						inputStream.read(segment, 0, segmentSize);
						// Set the segment size
						statement.setInt(attachmentSizeIndex, segmentSize);
						// Set the byte data
						statement.setBytes(contentIndex, segment);
						// Perform the update
						statement.executeUpdate();
						currentSegmentId++;
					} else {
						done = true;
					}
				}
			}

			// Clear the parameters because the data held in memory could be
			// quite large.
			updateStatement.clearParameters();
			insertStatement.clearParameters();

			// Delete lingering segments
			if (totalSegmentCount >= currentSegmentId) {
				PreparedStatement deleteStatement = prepareStatement("deleteMessageAttachmentLingeringSegments",
						channelId);
				deleteStatement.setString(1, attachment.getId());
				deleteStatement.setLong(2, messageId);
				deleteStatement.setInt(3, currentSegmentId);
				deleteStatement.executeUpdate();
			}
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(segmentCountStatement);
			close(segmentCountResult);
			close(updateStatement);
			close(insertStatement);
		}
	}

	@Override
	public void insertConnectorMessage(ConnectorMessage connectorMessage, boolean storeMaps, boolean updateStats) {
		getLogger().debug(connectorMessage.getChannelId() + "/" + connectorMessage.getMessageId() + "/"
				+ connectorMessage.getMetaDataId() + ": inserting connector message with" + (storeMaps ? "" : "out")
				+ " maps");

		PreparedStatement statement = null;
		try {
			statement = prepareStatement("insertConnectorMessage", connectorMessage.getChannelId());
			statement.setInt(1, connectorMessage.getMetaDataId());
			statement.setLong(2, connectorMessage.getMessageId());
			statement.setString(3, connectorMessage.getServerId());
			statement.setTimestamp(4, new Timestamp(connectorMessage.getReceivedDate().getTimeInMillis()));
			statement.setString(5, Character.toString(connectorMessage.getStatus().getStatusCode()));

			if (connectorMessage.getConnectorName() == null) {
				statement.setNull(6, Types.VARCHAR);
			} else {
				statement.setString(6, connectorMessage.getConnectorName());
			}

			statement.setInt(7, connectorMessage.getSendAttempts());

			if (connectorMessage.getSendDate() == null) {
				statement.setNull(8, Types.TIMESTAMP);
			} else {
				statement.setTimestamp(8, new Timestamp(connectorMessage.getSendDate().getTimeInMillis()));
			}

			if (connectorMessage.getResponseDate() == null) {
				statement.setNull(9, Types.TIMESTAMP);
			} else {
				statement.setTimestamp(9, new Timestamp(connectorMessage.getResponseDate().getTimeInMillis()));
			}

			statement.setInt(10, connectorMessage.getErrorCode());
			statement.setInt(11, connectorMessage.getChainId());
			statement.setInt(12, connectorMessage.getOrderId());
			statement.executeUpdate();

			if (storeMaps) {
				updateSourceMap(connectorMessage);
				updateMaps(connectorMessage);
			}

			updateErrors(connectorMessage);

			if (updateStats) {
				getTransactionStats().update(connectorMessage.getChannelId(), connectorMessage.getMetaDataId(),
						connectorMessage.getStatus(), null);
			}
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	public void deleteMessage(String channelId, long messageId) {
		getLogger().debug(channelId + "/" + messageId + ": deleting message");

		PreparedStatement statement = null;
		try {
			cascadeMessageDelete("deleteMessageCascadeAttachments", messageId, channelId);
			cascadeMessageDelete("deleteMessageCascadeMetadata", messageId, channelId);
			cascadeMessageDelete("deleteMessageCascadeContent", messageId, channelId);
			cascadeMessageDelete("deleteMessageCascadeConnectorMessage", messageId, channelId);

			statement = prepareStatement("deleteMessage", channelId);
			statement.setLong(1, messageId);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	public void deleteConnectorMessages(String channelId, long messageId, Set<Integer> metaDataIds) {
		getLogger().debug(channelId + "/" + messageId + ": deleting connector messages");
		long localChannelId = getLocalChannelId(channelId);

		PreparedStatement statement = null;
		try {
			if (metaDataIds == null) {
				cascadeMessageDelete("deleteMessageCascadeMetadata", messageId, channelId);
				cascadeMessageDelete("deleteMessageCascadeContent", messageId, channelId);

				statement = prepareStatement("deleteConnectorMessages", channelId);
				statement.setLong(1, messageId);
				statement.executeUpdate();
			} else {
				Map<String, Object> values = new HashMap<String, Object>();
				values.put("localChannelId", localChannelId);
				values.put("metaDataIds", StringUtils.join(metaDataIds, ','));

				cascadeMessageDelete("deleteConnectorMessagesByMetaDataIdsCascadeContent", messageId, values);
				cascadeMessageDelete("deleteConnectorMessagesByMetaDataIdsCascadeMetadata", messageId, values);

				statement = null;

				try {
					statement = getConnection().prepareStatement(
							getQuerySource().getQuery("deleteConnectorMessagesByMetaDataIds", values));
					statement.setLong(1, messageId);
					statement.executeUpdate();
				} finally {
					close(statement);
				}
			}
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	public void updateStatus(ConnectorMessage connectorMessage, Status previousStatus) {
		getLogger().debug(connectorMessage.getChannelId() + "/" + connectorMessage.getMessageId() + "/"
				+ connectorMessage.getMetaDataId() + ": updating status from " + previousStatus.getStatusCode() + " to "
				+ connectorMessage.getStatus().getStatusCode());

		PreparedStatement statement = null;
		try {
			// don't decrement the previous status if it was RECEIVED
			if (previousStatus == Status.RECEIVED) {
				previousStatus = null;
			}

			getTransactionStats().update(connectorMessage.getChannelId(), connectorMessage.getMetaDataId(),
					connectorMessage.getStatus(), previousStatus);

			statement = prepareStatement("updateStatus", connectorMessage.getChannelId());
			statement.setString(1, Character.toString(connectorMessage.getStatus().getStatusCode()));
			statement.setInt(2, connectorMessage.getMetaDataId());
			statement.setLong(3, connectorMessage.getMessageId());
			statement.setString(4, connectorMessage.getServerId());

			if (statement.executeUpdate() == 0) {
				throw new DonkeyDaoException(
						"Failed to update connector message status, the connector message was removed from this server.");
			}
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	protected void updateErrorCode(ConnectorMessage connectorMessage) {
		PreparedStatement statement = null;
		try {
			statement = prepareStatement("updateErrorCode", connectorMessage.getChannelId());

			statement.setInt(1, connectorMessage.getErrorCode());
			statement.setInt(2, connectorMessage.getMetaDataId());
			statement.setLong(3, connectorMessage.getMessageId());
			statement.setString(4, connectorMessage.getServerId());
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	public void markAsProcessed(String channelId, long messageId) {
		getLogger().debug(channelId + "/" + messageId + ": marking as processed");

		PreparedStatement statement = null;
		try {
			statement = prepareStatement("markAsProcessed", channelId);
			statement.setLong(1, messageId);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	public void resetMessage(String channelId, long messageId) {
		getLogger().debug(channelId + "/" + messageId + ": resetting message");

		PreparedStatement statement = null;
		try {
			statement = prepareStatement("resetMessage", channelId);
			statement.setLong(1, messageId);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	public void removeChannel(String channelId) {
		if (!getLocalChannelIds().containsKey(channelId)) {
			return;
		}

		getLogger().debug(channelId + ": removing channel");

		setTransactionAlteredChannels(true);

		List<PreparedStatement> statements = new ArrayList<>();
		try {
			statements.add(prepareStatement("dropStatisticsTable", channelId));
			statements.add(prepareStatement("dropAttachmentsTable", channelId));
			statements.add(prepareStatement("dropCustomMetadataTable", channelId));
			statements.add(prepareStatement("dropMessageContentTable", channelId));
			statements.add(prepareStatement("dropMessageMetadataTable", channelId));
			statements.add(prepareStatement("dropMessageSequence", channelId));
			statements.add(prepareStatement("dropMessageTable", channelId));
			statements.add(prepareStatement("deleteChannel", channelId));

			for (PreparedStatement statement : statements) {
				statement.executeUpdate();
			}

			getRemovedChannelIds().add(channelId);
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			for (PreparedStatement statement : statements) {
				close(statement);
			}
		}
	}

	@Override
	public void deleteAllMessages(String channelId) {
		getLogger().debug(channelId + ": deleting all messages");

		List<PreparedStatement> statements = new ArrayList<>();
		try {
			// delete tables without constraints
			cascadeMessageDelete("deleteAllMessagesCascadeAttachments", channelId);
			cascadeMessageDelete("deleteAllMessagesCascadeMetadata", channelId);
			cascadeMessageDelete("deleteAllMessagesCascadeContent", channelId);

			PreparedStatement statement = null;
			// remove constraints so truncate can execute
			if (getQuerySource().queryExists("dropConstraintMessageContentTable")) {
				statement = prepareStatement("dropConstraintMessageContentTable", channelId);
				statements.add(statement);
				statement.executeUpdate();
			}
			if (getQuerySource().queryExists("dropConstraintMessageCustomMetaDataTable")) {
				statement = prepareStatement("dropConstraintMessageCustomMetaDataTable", channelId);
				statements.add(statement);
				statement.executeUpdate();
			}

			// delete
			cascadeMessageDelete("deleteAllMessagesCascadeConnectorMessage", channelId);

			// re-add constraints
			if (getQuerySource().queryExists("addConstraintMessageCustomMetaDataTable")) {
				statement = prepareStatement("addConstraintMessageCustomMetaDataTable", channelId);
				statements.add(statement);
				statement.executeUpdate();
			}
			if (getQuerySource().queryExists("addConstraintMessageContentTable")) {
				statement = prepareStatement("addConstraintMessageContentTable", channelId);
				statements.add(statement);
				statement.executeUpdate();
			}

			// remove constraints so truncate can execute
			if (getQuerySource().queryExists("dropConstraintConnectorMessageTable")) {
				statement = prepareStatement("dropConstraintConnectorMessageTable", channelId);
				statements.add(statement);
				statement.executeUpdate();
			}
			if (getQuerySource().queryExists("dropConstraintAttachmentTable")) {
				statement = prepareStatement("dropConstraintAttachmentTable", channelId);
				statements.add(statement);
				statement.executeUpdate();
			}

			// delete
			statement = prepareStatement("deleteAllMessages", channelId);
			statements.add(statement);
			statement.executeUpdate();

			// re-add constraints
			if (getQuerySource().queryExists("addConstraintAttachmentTable")) {
				statement = prepareStatement("addConstraintAttachmentTable", channelId);
				statements.add(statement);
				statement.executeUpdate();
			}
			if (getQuerySource().queryExists("addConstraintConnectorMessageTable")) {
				statement = prepareStatement("addConstraintConnectorMessageTable", channelId);
				statements.add(statement);
				statement.executeUpdate();
			}

		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			for (PreparedStatement statement : statements) {
				close(statement);
			}
		}
	}

	@Override
	public void deleteMessageContent(String channelId, long messageId) {
		getLogger().debug(channelId + "/" + messageId + ": deleting content");

		PreparedStatement statement = null;
		try {
			statement = prepareStatement("deleteMessageContent", channelId);
			statement.setLong(1, messageId);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	public void deleteMessageContentByMetaDataIds(String channelId, long messageId, Set<Integer> metaDataIds) {
		getLogger().debug(
				channelId + "/" + messageId + ": deleting content by metadata IDs: " + String.valueOf(metaDataIds));

		PreparedStatement statement = null;
		try {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("localChannelId", getLocalChannelId(channelId));
			values.put("metaDataIds", StringUtils.join(metaDataIds, ','));

			statement = getConnection()
					.prepareStatement(getQuerySource().getQuery("deleteMessageContentByMetaDataIds", values));
			statement.setLong(1, messageId);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	protected void deleteMessageContentByMetaDataIdAndContentType(String channelId, long messageId, int metaDataId,
			ContentType contentType) {
		getLogger().debug(channelId + "/" + messageId + ": deleting content");

		PreparedStatement statement = null;
		try {
			statement = prepareStatement("deleteMessageContentByMetaDataIdAndContentType", channelId);
			statement.setLong(1, messageId);
			statement.setInt(2, metaDataId);
			statement.setInt(3, contentType.getContentTypeCode());
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	public void deleteMessageAttachments(String channelId, long messageId) {
		getLogger().debug(channelId + "/" + messageId + ": deleting attachments");

		PreparedStatement statement = null;
		try {
			statement = prepareStatement("deleteMessageAttachments", channelId);
			statement.setLong(1, messageId);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(statement);
		}
	}

	@Override
	public List<Attachment> getMessageAttachment(String channelId, long messageId) {
		ResultSet resultSet = null;
		PreparedStatement statement = null;

		try {
			// Get the total size of each attachment by summing the sizes of its
			// segments
			statement = prepareStatement("selectMessageAttachmentSizeByMessageId", channelId);
			statement.setLong(1, messageId);
			resultSet = statement.executeQuery();

			Map<String, Integer> attachmentSize = new HashMap<String, Integer>();
			while (resultSet.next()) {
				// Store the attachment size in a map with the attachment id as
				// the key
				attachmentSize.put(resultSet.getString("id"), resultSet.getInt("attachment_size"));
			}

			close(resultSet);

			// Get the attachment data
			close(statement);
			statement = prepareStatement("selectMessageAttachmentByMessageId", channelId);
			statement.setLong(1, messageId);
			// Set the number of rows to be fetched into memory at a time. This
			// limits the amount of memory required for the query.
			statement.setFetchSize(1);
			resultSet = statement.executeQuery();

			// Initialize the return object
			List<Attachment> attachments = new ArrayList<Attachment>();
			// The current attachment id that is being stitched together
			String currentAttachmentId = null;
			// The type of the current attachment
			String type = null;
			// Use an byte array to combine the segments
			byte[] content = null;
			int offset = 0;

			while (resultSet.next()) {
				// Get the attachment id of the current segment
				String attachmentId = resultSet.getString("id");

				// Ensure that the attachmentId is in the map we created
				// earlier, otherwise don't return this attachment
				if (attachmentSize.containsKey(attachmentId)) {
					// If starting a new attachment
					if (!attachmentId.equals(currentAttachmentId)) {
						// If there was a previous attachment, we need to finish
						// it.
						if (content != null) {
							// Add the data in the output stream to the list of
							// attachments to return
							attachments.add(new Attachment(currentAttachmentId, content, type));
						}
						currentAttachmentId = attachmentId;
						type = resultSet.getString("type");

						// Initialize the byte array size to the exact size of
						// the attachment. This should minimize the memory
						// requirements if the numbers are correct.
						// Use 0 as a backup in case the size is not in the map.
						// (If trying to return an attachment that no longer
						// exists)
						content = new byte[attachmentSize.get(attachmentId)];
						offset = 0;
					}

					// write the current segment to the output stream buffer
					byte[] segment = resultSet.getBytes("content");
					System.arraycopy(segment, 0, content, offset, segment.length);

					offset += segment.length;
				}
			}

			// Finish the message if one exists by adding it to the list of
			// attachments to return
			if (content != null) {
				attachments.add(new Attachment(currentAttachmentId, content, type));
			}
			content = null;

			return attachments;
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(resultSet);
			close(statement);
		}
	}

	@Override
	public Attachment getMessageAttachment(String channelId, String attachmentId, Long messageId) {
		ResultSet resultSet = null;
		Attachment attachment = new Attachment();
		PreparedStatement statement = null;
		try {
			// Get the total size of each attachment by summing the sizes of its
			// segments
			statement = prepareStatement("selectMessageAttachmentSize", channelId);
			statement.setString(1, attachmentId);
			statement.setLong(2, messageId);
			resultSet = statement.executeQuery();

			int size = 0;
			if (resultSet.next()) {
				// Store the attachment size in a map with the attachment id as
				// the key
				size = resultSet.getInt("attachment_size");
			}

			close(resultSet);

			// Get the attachment data
			close(statement);
			statement = prepareStatement("selectMessageAttachment", channelId);
			statement.setString(1, attachmentId);
			statement.setLong(2, messageId);
			// Set the number of rows to be fetched into memory at a time. This
			// limits the amount of memory required for the query.
			statement.setFetchSize(1);
			resultSet = statement.executeQuery();

			// The type of the current attachment
			String type = null;

			// Initialize the output stream's buffer size to the exact size of
			// the attachment. This should minimize the memory requirements if
			// the numbers are correct.
			byte[] content = null;
			int offset = 0;

			while (resultSet.next()) {
				if (content == null) {
					type = resultSet.getString("type");
					content = new byte[size];
				}

				// write the current segment to the output stream buffer
				byte[] segment = resultSet.getBytes("content");
				System.arraycopy(segment, 0, content, offset, segment.length);

				offset += segment.length;
			}

			// Finish the message if one exists by adding it to the list of
			// attachments to return
			if (content != null) {
				attachment.setId(attachmentId);
				attachment.setContent(content);
				attachment.setType(type);
			}
			content = null;

			return attachment;
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(resultSet);
			close(statement);
		}
	}

	@Override
	public Map<Integer, ConnectorMessage> getConnectorMessages(String channelId, long messageId,
			List<Integer> metaDataIds) {
		ResultSet resultSet = null;
		PreparedStatement statement = null;

		try {
			boolean includeMetaDataIds = CollectionUtils.isNotEmpty(metaDataIds);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("localChannelId", getLocalChannelId(channelId));

			if (includeMetaDataIds) {
				params.put("metaDataIds", StringUtils.join(metaDataIds, ','));
			}

			statement = getConnection().prepareStatement(getQuerySource().getQuery(includeMetaDataIds
					? "getConnectorMessagesByMessageIdAndMetaDataIds" : "getConnectorMessagesByMessageId", params));
			statement.setLong(1, messageId);
			resultSet = statement.executeQuery();

			Map<Integer, ConnectorMessage> connectorMessages = new HashMap<Integer, ConnectorMessage>();

			while (resultSet.next()) {
				ConnectorMessage connectorMessage = getConnectorMessageFromResultSet(channelId, resultSet, true, true);
				connectorMessages.put(connectorMessage.getMetaDataId(), connectorMessage);
			}

			return connectorMessages;
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(resultSet);
			close(statement);
		}
	}

	@Override
	public Map<Integer, Status> getConnectorMessageStatuses(String channelId, long messageId, boolean checkProcessed) {
		ResultSet resultSet = null;
		PreparedStatement statement = null;

		try {
			statement = prepareStatement(
					checkProcessed ? "getConnectorMessageStatusesCheckProcessed" : "getConnectorMessageStatuses",
					channelId);
			statement.setLong(1, messageId);
			resultSet = statement.executeQuery();

			Map<Integer, Status> statusMap = new HashMap<Integer, Status>();

			while (resultSet.next()) {
				statusMap.put(resultSet.getInt(1), Status.fromChar(resultSet.getString(2).charAt(0)));
			}

			return statusMap;
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(resultSet);
			close(statement);
		}
	}

	@Override
	public int getConnectorMessageCount(String channelId, String serverId, int metaDataId, Status status) {
		if (getDonkey().getDeployedChannels().get(channelId) != null || getLocalChannelIds().get(channelId) != null) {
			ResultSet resultSet = null;
			PreparedStatement statement = null;

			try {
				statement = getStatementSource().getPreparedStatement("getConnectorMessageCountByMetaDataIdAndStatus",
						getLocalChannelId(channelId));
				statement.setInt(1, metaDataId);
				statement.setString(2, Character.toString(status.getStatusCode()));
				statement.setString(3, serverId);
				resultSet = statement.executeQuery();
				resultSet.next();
				return resultSet.getInt(1);
			} catch (SQLException e) {
				throw new DonkeyDaoException(e);
			} finally {
				close(resultSet);
				close(statement);
			}
		} else {
			// the channel has never been deployed
			return 0;
		}
	}

	@Override
	public long getConnectorMessageMaxMessageId(String channelId, String serverId, int metaDataId, Status status) {
		ResultSet resultSet = null;
		PreparedStatement statement = null;

		try {
			statement = prepareStatement("getConnectorMessageMaxMessageIdByMetaDataIdAndStatus", channelId);
			statement.setInt(1, metaDataId);
			statement.setString(2, Character.toString(status.getStatusCode()));
			statement.setString(3, serverId);
			resultSet = statement.executeQuery();
			resultSet.next();
			return resultSet.getLong(1);
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(resultSet);
			close(statement);
		}
	}

	@Override
	public void createChannel(String channelId, long localChannelId) {
		getLogger().debug(channelId + ": creating channel");
		Statement initSequenceStatement = null;
		setTransactionAlteredChannels(true);

		PreparedStatement statement = null;
		try {
			statement = prepareStatement("createChannel", null);
			statement.setString(1, channelId);
			statement.setLong(2, localChannelId);
			statement.executeUpdate();

			Map<String, Object> values = new HashMap<String, Object>();
			values.put("localChannelId", localChannelId);

			createTable("createMessageTable", values);
			createTable("createConnectorMessageTable", values);
			createTable("createMessageContentTable", values);
			createTable("createMessageCustomMetaDataTable", values);
			createTable("createMessageAttachmentTable", values);
			createTable("createMessageStatisticsTable", values);
			createTable("createMessageSequence", values);

			if (getQuerySource().queryExists("initMessageSequence")) {
				initSequenceStatement = getConnection().createStatement();
				initSequenceStatement.executeUpdate(getQuerySource().getQuery("initMessageSequence", values));
			}
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(initSequenceStatement);
			close(statement);
		}
	}

	@Override
	protected Statistics getChannelStatistics(String serverId, boolean total) {
		Map<String, Long> channelIds = getLocalChannelIds();
		String queryId = (total) ? "getChannelTotalStatistics" : "getChannelStatistics";
		Statistics statistics = new Statistics(!total);
		ResultSet resultSet = null;

		for (String channelId : channelIds.keySet()) {
			PreparedStatement statement = null;
			try {
				statement = prepareStatement(queryId, channelId);
				statement.setString(1, serverId);
				resultSet = statement.executeQuery();

				while (resultSet.next()) {
					Integer metaDataId = resultSet.getInt("metadata_id");

					if (resultSet.wasNull()) {
						metaDataId = null;
					}

					Map<Status, Long> stats = new HashMap<Status, Long>();
					stats.put(Status.RECEIVED, resultSet.getLong("received"));
					stats.put(Status.FILTERED, resultSet.getLong("filtered"));
					stats.put(Status.SENT, resultSet.getLong("sent"));
					stats.put(Status.ERROR, resultSet.getLong("error"));

					statistics.overwrite(channelId, metaDataId, stats);
				}
			} catch (SQLException e) {
				throw new DonkeyDaoException(e);
			} finally {
				close(resultSet);
				close(statement);
			}
		}

		return statistics;
	}

	protected List<MessageContent> getMessageContent(String channelId, long messageId, int metaDataId) {
		List<MessageContent> messageContents = new ArrayList<MessageContent>();
		ResultSet resultSet = null;

		PreparedStatement statement = null;
		try {
			statement = prepareStatement("getMessageContent", channelId);
			statement.setLong(1, messageId);
			statement.setInt(2, metaDataId);

			resultSet = statement.executeQuery();

			while (resultSet.next()) {
				String content = resultSet.getString("content");
				ContentType contentType = ContentType.fromCode(resultSet.getInt("content_type"));
				String dataType = resultSet.getString("data_type");
				boolean encrypted = resultSet.getBoolean("is_encrypted");

				if ((isDecryptData() || getAlwaysDecrypt().contains(contentType)) && encrypted
						&& getEncryptor() != null) {
					content = getEncryptor().decrypt(content);
					encrypted = false;
				}

				messageContents.add(new MessageContent(channelId, messageId, metaDataId, contentType, content, dataType,
						encrypted));
			}
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(resultSet);
			close(statement);
		}

		return messageContents;
	}

	@Override
	protected List<MessageContent> getDestinationMessageContentFromSource(String channelId, long messageId,
			int metaDataId) {
		List<MessageContent> messageContents = new ArrayList<MessageContent>();
		ResultSet resultSet = null;
		PreparedStatement statement = null;

		try {
			statement = prepareStatement("getDestinationMessageContentFromSource", channelId);
			statement.setLong(1, messageId);

			resultSet = statement.executeQuery();

			while (resultSet.next()) {
				String content = resultSet.getString("content");
				ContentType contentType = ContentType.fromCode(resultSet.getInt("content_type"));
				String dataType = resultSet.getString("data_type");
				boolean encrypted = resultSet.getBoolean("is_encrypted");

				if ((isDecryptData() || getAlwaysDecrypt().contains(contentType)) && encrypted
						&& getEncryptor() != null) {
					content = getEncryptor().decrypt(content);
					encrypted = false;
				}

				if (contentType == ContentType.ENCODED) {
					contentType = ContentType.RAW;
				}

				messageContents.add(new MessageContent(channelId, messageId, metaDataId, contentType, content, dataType,
						encrypted));
			}
		} catch (SQLException e) {
			throw new DonkeyDaoException(e);
		} finally {
			close(resultSet);
			close(statement);
		}

		return messageContents;
	}

}
