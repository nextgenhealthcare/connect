/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.jdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.Serializer;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoException;
import com.mirth.connect.donkey.server.event.Event;

public class JdbcDao implements DonkeyDao {
    private Connection connection;
    private QuerySource querySource;
    private PreparedStatementSource statementSource;
    private Serializer serializer;
    private Statistics currentStats;
    private Statistics totalStats;
    private Statistics transactionStats = new Statistics();
    private Map<String, Map<Integer, Set<Status>>> resetStats = new HashMap<String, Map<Integer, Set<Status>>>();
    private List<String> removedChannelIds = new ArrayList<String>();
    private ChannelController channelController = ChannelController.getInstance();
    private String asyncCommitCommand;
    private Logger logger = Logger.getLogger(this.getClass());

    protected JdbcDao(Connection connection, QuerySource querySource, PreparedStatementSource statementSource, Serializer serializer) {
        this.connection = connection;
        this.querySource = querySource;
        this.statementSource = statementSource;
        this.serializer = serializer;
        
        currentStats = channelController.getStatistics();
        totalStats = channelController.getTotalStatistics();
    }

    @Override
    public void insertMessage(Message message) {
        try {
            PreparedStatement statement = statementSource.getPreparedStatement("insertMessage", channelController.getLocalChannelId(message.getChannelId()));
            statement.setLong(1, message.getMessageId());
            statement.setString(2, message.getServerId());
            statement.setTimestamp(3, new Timestamp(message.getDateCreated().getTimeInMillis()));
            statement.setBoolean(4, message.isProcessed());
            
            Long importId = message.getImportId();
            
            if (importId != null) {
                statement.setLong(5, message.getImportId());
            } else {
                statement.setNull(5, Types.BIGINT);
            }
            
            String importChannelId = message.getImportChannelId();
            
            if (importChannelId != null) {
                statement.setString(6, message.getImportChannelId());
            } else {
                statement.setNull(6, Types.VARCHAR);
            }
            
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public void insertMessageContent(MessageContent messageContent) {
        try {
            PreparedStatement statement = statementSource.getPreparedStatement("insertMessageContent", channelController.getLocalChannelId(messageContent.getChannelId()));
            statement.setInt(1, messageContent.getMetaDataId());
            statement.setLong(2, messageContent.getMessageId());
            statement.setString(3, Character.toString(messageContent.getContentType().getContentTypeCode()));
            
            String encryptedContent = messageContent.getEncryptedContent();
            
            if (encryptedContent != null) {
                statement.setString(4, encryptedContent);
                statement.setBoolean(5, true);
            } else {
                statement.setString(4, messageContent.getContent());
                statement.setBoolean(5, false);
            }
            
            statement.executeUpdate();
            statement.clearParameters();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public void batchInsertMessageContent(MessageContent messageContent) {
        try {
            PreparedStatement statement = statementSource.getPreparedStatement("insertMessageContent", channelController.getLocalChannelId(messageContent.getChannelId()));
            statement.setInt(1, messageContent.getMetaDataId());
            statement.setLong(2, messageContent.getMessageId());
            statement.setString(3, Character.toString(messageContent.getContentType().getContentTypeCode()));
            
            String encryptedContent = messageContent.getEncryptedContent();
            
            if (encryptedContent != null) {
                statement.setString(4, encryptedContent);
                statement.setBoolean(5, true);
            } else {
                statement.setString(4, messageContent.getContent());
                statement.setBoolean(5, false);
            }
            
            statement.addBatch();
            statement.clearParameters();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public void executeBatchInsertMessageContent(String channelId) {
        try {
            PreparedStatement statement = statementSource.getPreparedStatement("insertMessageContent", channelController.getLocalChannelId(channelId));
            statement.executeBatch();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public void storeMessageContent(MessageContent messageContent) {
        try {
            PreparedStatement statement = statementSource.getPreparedStatement("storeMessageContent", channelController.getLocalChannelId(messageContent.getChannelId()));

            String encryptedContent = messageContent.getEncryptedContent();
            
            if (encryptedContent != null) {
                statement.setString(1, encryptedContent);
                statement.setBoolean(2, true);
            } else {
                String content = messageContent.getContent();
    
                if (content == null) {
                    statement.setNull(1, Types.LONGVARCHAR);
                } else {
                    statement.setString(1, content);
                }
                
                statement.setBoolean(2, false);
            }

            statement.setInt(3, messageContent.getMetaDataId());
            statement.setLong(4, messageContent.getMessageId());
            statement.setString(5, Character.toString(messageContent.getContentType().getContentTypeCode()));

            if (statement.executeUpdate() == 0) {
                insertMessageContent(messageContent);
            }
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }
    
    @Override
    public void addChannelStatistics(Statistics statistics) {
        for (Entry<String, Map<Integer, Map<Status, Long>>> channelEntry : statistics.getStats().entrySet()) {
            String channelId = channelEntry.getKey();
            long localChannelId = channelController.getLocalChannelId(channelId);
            
            try {
                PreparedStatement channelStatement = statementSource.getPreparedStatement("updateChannelStatistics", localChannelId);
                PreparedStatement connectorStatement = statementSource.getPreparedStatement("updateConnectorStatistics", localChannelId);
                
                for (Entry<Integer, Map<Status, Long>> connectorEntry : channelEntry.getValue().entrySet()) {
                    Integer metaDataId = connectorEntry.getKey();
                    Map<Status, Long> connectorStats = connectorEntry.getValue();
                    
                    PreparedStatement statement = (metaDataId == null) ? channelStatement : connectorStatement;
                    statement.setLong(1, connectorStats.get(Status.RECEIVED));
                    statement.setLong(2, connectorStats.get(Status.RECEIVED));
                    statement.setLong(3, connectorStats.get(Status.FILTERED));
                    statement.setLong(4, connectorStats.get(Status.FILTERED));
                    statement.setLong(5, connectorStats.get(Status.TRANSFORMED));
                    statement.setLong(6, connectorStats.get(Status.TRANSFORMED));
                    statement.setLong(7, connectorStats.get(Status.PENDING));
                    statement.setLong(8, connectorStats.get(Status.PENDING));
                    statement.setLong(9, connectorStats.get(Status.SENT));
                    statement.setLong(10, connectorStats.get(Status.SENT));
                    statement.setLong(11, connectorStats.get(Status.ERROR));
                    statement.setLong(12, connectorStats.get(Status.ERROR));
        
                    if (metaDataId != null) {
                        statement.setInt(13, metaDataId);
                    }
        
                    if (statement.executeUpdate() == 0) {
                        statement = statementSource.getPreparedStatement("insertChannelStatistics", localChannelId);
        
                        if (metaDataId == null) {
                            statement.setNull(1, Types.INTEGER);
                        } else {
                            statement.setInt(1, metaDataId);
                        }
        
                        statement.setLong(2, connectorStats.get(Status.RECEIVED));
                        statement.setLong(3, connectorStats.get(Status.RECEIVED));
                        statement.setLong(4, connectorStats.get(Status.FILTERED));
                        statement.setLong(5, connectorStats.get(Status.FILTERED));
                        statement.setLong(6, connectorStats.get(Status.TRANSFORMED));
                        statement.setLong(7, connectorStats.get(Status.TRANSFORMED));
                        statement.setLong(8, connectorStats.get(Status.PENDING));
                        statement.setLong(9, connectorStats.get(Status.PENDING));
                        statement.setLong(10, connectorStats.get(Status.SENT));
                        statement.setLong(11, connectorStats.get(Status.SENT));
                        statement.setLong(12, connectorStats.get(Status.ERROR));
                        statement.setLong(13, connectorStats.get(Status.ERROR));
                        statement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                throw new DonkeyDaoException(e);
            }
        }
    }

    @Override
    public void insertMessageAttachment(String channelId, long messageId, Attachment attachment) {
        try {
            PreparedStatement statement = statementSource.getPreparedStatement("insertMessageAttachment", channelController.getLocalChannelId(channelId));
            statement.setLong(1, messageId);
            statement.setString(2, attachment.getId());
            statement.setBytes(3, attachment.getContent());
            statement.setString(4, attachment.getType());
            statement.executeUpdate();
            statement.clearParameters();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public void insertMetaData(ConnectorMessage connectorMessage, List<MetaDataColumn> metaDataColumns) {
        try {
            List<String> metaDataColumnNames = new ArrayList<String>();

            for (MetaDataColumn metaDataColumn : metaDataColumns) {
                metaDataColumnNames.add(metaDataColumn.getName());
            }

            Map<String, Object> values = new HashMap<String, Object>();
            values.put("localChannelId", channelController.getLocalChannelId(connectorMessage.getChannelId()));
            values.put("metaDataColumnNames", StringUtils.join(metaDataColumnNames, ','));
            values.put("metaDataColumnPlaceholders", "?" + StringUtils.repeat(", ?", metaDataColumnNames.size() - 1));

            PreparedStatement statement = connection.prepareStatement(querySource.getQuery("insertMetaData", values));
            statement.setInt(1, connectorMessage.getMetaDataId());
            statement.setLong(2, connectorMessage.getMessageId());
            int n = 3;

            Map<String, Object> metaDataMap = connectorMessage.getMetaDataMap();

            for (MetaDataColumn metaDataColumn : metaDataColumns) {
                Object value = metaDataMap.get(metaDataColumn.getName());

                // @formatter:off
                if (value == null) {
                    switch (metaDataColumn.getType()) {
                        case STRING: statement.setNull(n, Types.VARCHAR); break;
                        case LONG: statement.setNull(n, Types.BIGINT); break;
                        case DOUBLE: statement.setNull(n, Types.DOUBLE); break;
                        case BOOLEAN: statement.setNull(n, Types.BOOLEAN); break;
                        case DATE: statement.setNull(n, Types.DATE); break;
                        case TIME: statement.setNull(n, Types.TIME); break;
                        case TIMESTAMP: statement.setNull(n, Types.TIMESTAMP); break;
                    }
                } else {                
                    switch (metaDataColumn.getType()) {
                        case STRING: statement.setString(n, (String) value); break;
                        case LONG:
                            if (value instanceof Integer) {
                                statement.setLong(n, ((Integer) value).longValue());
                            } else {
                                statement.setLong(n, (Long) value);
                            }

                            break;
                            
                        case DOUBLE: statement.setDouble(n, (Double) value); break;
                        case BOOLEAN: statement.setBoolean(n, (Boolean) value); break;
                        case DATE: statement.setDate(n, new Date(((Calendar)value).getTimeInMillis())); break;
                        case TIME: statement.setTime(n, new Time(((Calendar)value).getTimeInMillis())); break;
                        case TIMESTAMP: statement.setTimestamp(n, new Timestamp(((Calendar)value).getTimeInMillis())); break;
                    }
                }
                // @formatter:on

                n++;
            }

            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DonkeyDaoException("Failed to insert connector message meta data", e);
        }
    }

    @Override
    public void insertConnectorMessage(ConnectorMessage connectorMessage, boolean storeMaps) {
        try {
            PreparedStatement statement = statementSource.getPreparedStatement("insertConnectorMessage", channelController.getLocalChannelId(connectorMessage.getChannelId()));
            statement.setInt(1, connectorMessage.getMetaDataId());
            statement.setLong(2, connectorMessage.getMessageId());
            statement.setTimestamp(3, new Timestamp(connectorMessage.getDateCreated().getTimeInMillis()));
            statement.setString(4, Character.toString(connectorMessage.getStatus().getStatusCode()));
            // TODO insert send attempts

            Map<String, Object> connectorMap;
            Map<String, Object> channelMap;
            Map<String, Response> responseMap;

            if (storeMaps) {
                connectorMap = connectorMessage.getConnectorMap();
                channelMap = connectorMessage.getChannelMap();
                responseMap = connectorMessage.getResponseMap();
            } else {
                connectorMap = null;
                channelMap = null;
                responseMap = null;
            }

            if (connectorMap == null) {
                statement.setNull(5, Types.LONGVARCHAR);
            } else {
                statement.setString(5, serializer.serialize((HashMap<String, Object>) connectorMap));
            }

            if (channelMap == null) {
                statement.setNull(6, Types.LONGVARCHAR);
            } else {
                statement.setString(6, serializer.serialize((HashMap<String, Object>) channelMap));
            }

            if (responseMap == null) {
                statement.setNull(7, Types.LONGVARCHAR);
            } else {
                statement.setString(7, serializer.serialize((HashMap<String, Response>) responseMap));
            }

            String errors = connectorMessage.getErrors();

            if (errors == null) {
                statement.setNull(8, Types.LONGVARCHAR);
            } else {
                statement.setString(8, serializer.serialize(errors));
            }

            statement.executeUpdate();

            transactionStats.update(connectorMessage.getChannelId(), connectorMessage.getMetaDataId(), connectorMessage.getStatus(), null);
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public void deleteMessage(String channelId, long messageId, boolean deleteStatistics) {
        try {
            long localChannelId = channelController.getLocalChannelId(channelId);

            if (deleteStatistics) {
                deleteMessageStatistics(channelId, getConnectorMessages(channelId, messageId).values());
            }

            PreparedStatement statement = statementSource.getPreparedStatement("deleteMessage", localChannelId);
            statement.setLong(1, messageId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public void deleteConnectorMessages(String channelId, long messageId, List<Integer> metaDataIds, boolean deleteStatistics) {
        try {
            long localChannelId = channelController.getLocalChannelId(channelId);
            PreparedStatement statement = null;

            if (metaDataIds == null) {
                if (deleteStatistics) {
                    deleteMessageStatistics(channelId, getConnectorMessages(channelId, messageId).values());
                }

                statement = statementSource.getPreparedStatement("deleteConnectorMessage", localChannelId);
                statement.setLong(1, messageId);
                statement.executeUpdate();
            } else {
                List<ConnectorMessage> connectorMessages = new ArrayList<ConnectorMessage>();

                for (Entry<Integer, ConnectorMessage> connectorMessageEntry : getConnectorMessages(channelId, messageId).entrySet()) {
                    if (metaDataIds.contains(connectorMessageEntry.getKey())) {
                        connectorMessages.add(connectorMessageEntry.getValue());
                    }
                }

                if (deleteStatistics) {
                    deleteMessageStatistics(channelId, connectorMessages);
                }

                String metaDataIdList = StringUtils.join(metaDataIds, ',');

                Map<String, Object> values = new HashMap<String, Object>();
                values.put("localChannelId", localChannelId);
                values.put("metaDataIds", metaDataIdList);

                statement = connection.prepareStatement(querySource.getQuery("deleteConnectorMessageByMetaDataIds", values));
                statement.setLong(1, messageId);
                statement.executeUpdate();
                statement.close();
            }
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public void updateStatus(ConnectorMessage connectorMessage, Status previousStatus) {
        try {
            // don't decrement the previous status if it was RECEIVED
            if (previousStatus == Status.RECEIVED) {
                previousStatus = null;
            }

            transactionStats.update(connectorMessage.getChannelId(), connectorMessage.getMetaDataId(), connectorMessage.getStatus(), previousStatus);

            PreparedStatement statement = statementSource.getPreparedStatement("updateStatus", channelController.getLocalChannelId(connectorMessage.getChannelId()));
            statement.setString(1, Character.toString(connectorMessage.getStatus().getStatusCode()));
            statement.setInt(2, connectorMessage.getSendAttempts());
            statement.setInt(3, connectorMessage.getMetaDataId());
            statement.setLong(4, connectorMessage.getMessageId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public void updateErrors(ConnectorMessage connectorMessage) {
        try {
            PreparedStatement statement = statementSource.getPreparedStatement("updateErrors", channelController.getLocalChannelId(connectorMessage.getChannelId()));
            statement.setString(1, connectorMessage.getErrors());
            statement.setInt(2, connectorMessage.getMetaDataId());
            statement.setLong(3, connectorMessage.getMessageId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public void updateMaps(ConnectorMessage connectorMessage) {
        try {
            PreparedStatement statement = statementSource.getPreparedStatement("updateMaps", channelController.getLocalChannelId(connectorMessage.getChannelId()));
            Map<String, Object> connectorMap = connectorMessage.getConnectorMap();
            Map<String, Object> channelMap = connectorMessage.getChannelMap();
            Map<String, Response> responseMap = connectorMessage.getResponseMap();

            if (connectorMap == null) {
                statement.setNull(1, Types.LONGVARCHAR);
            } else {
                statement.setString(1, serializer.serialize((HashMap<String, Object>) connectorMap));
            }

            if (channelMap == null) {
                statement.setNull(2, Types.LONGVARCHAR);
            } else {
                statement.setString(2, serializer.serialize((HashMap<String, Object>) channelMap));
            }

            if (responseMap == null) {
                statement.setNull(3, Types.LONGVARCHAR);
            } else {
                statement.setString(3, serializer.serialize((HashMap<String, Response>) responseMap));
            }

            statement.setInt(4, connectorMessage.getMetaDataId());
            statement.setLong(5, connectorMessage.getMessageId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public void updateResponseMap(ConnectorMessage connectorMessage) {
        try {
            PreparedStatement statement = statementSource.getPreparedStatement("updateResponseMap", channelController.getLocalChannelId(connectorMessage.getChannelId()));
            Map<String, Response> responseMap = connectorMessage.getResponseMap();

            if (responseMap == null) {
                statement.setNull(1, Types.LONGVARCHAR);
            } else {
                statement.setString(1, serializer.serialize((HashMap<String, Response>) responseMap));
            }

            statement.setInt(2, connectorMessage.getMetaDataId());
            statement.setLong(3, connectorMessage.getMessageId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public void markAsProcessed(String channelId, long messageId) {
        try {
            PreparedStatement statement = statementSource.getPreparedStatement("markAsProcessed", channelController.getLocalChannelId(channelId));
            statement.setLong(1, messageId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public Map<String, Long> getLocalChannelIds() {
        ResultSet resultSet = null;

        try {
            Map<String, Long> localChannelIds = new HashMap<String, Long>();
            resultSet = statementSource.getPreparedStatement("getLocalChannelIds", null).executeQuery();

            while (resultSet.next()) {
                localChannelIds.put(resultSet.getString(1), resultSet.getLong(2));
            }

            return localChannelIds;
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            close(resultSet);
        }
    }

    @Override
    public void removeChannel(String channelId) {
        Statement statement = null;

        try {
            Map<String, Object> values = new HashMap<String, Object>();
            values.put("localChannelId", channelController.getLocalChannelId(channelId));

            statement = connection.createStatement();
            statement.executeUpdate(querySource.getQuery("removeChannel", values));

            removedChannelIds.add(channelId);
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            close(statement);
        }
    }

    @Override
    public Long selectMaxLocalChannelId() {
        ResultSet resultSet = null;

        try {
            Long maxLocalChannelId = 0L;
            resultSet = statementSource.getPreparedStatement("selectMaxLocalChannelId", null).executeQuery();

            if (resultSet.next()) {
                maxLocalChannelId = resultSet.getLong(1);
            }

            resultSet.close();
            return maxLocalChannelId;
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            close(resultSet);
        }
    }

    @Override
    public void deleteAllMessages(String channelId) {
        Statement statement = null;

        try {
            Map<String, Object> values = new HashMap<String, Object>();
            values.put("localChannelId", channelController.getLocalChannelId(channelId));

            statement = connection.createStatement();
            statement.executeUpdate(querySource.getQuery("deleteAllMessages", values));
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            close(statement);
        }
    }

    @Override
    public void deleteAllContent(String channelId, long messageId) {
        try {
            PreparedStatement statement = statementSource.getPreparedStatement("deleteAllContent", channelController.getLocalChannelId(channelId));
            statement.setLong(1, messageId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public List<MetaDataColumn> getMetaDataColumns(String channelId) {
        ResultSet columns = null;

        try {
            List<MetaDataColumn> metaDataColumns = new ArrayList<MetaDataColumn>();
            Long localChannelId = channelController.getLocalChannelId(channelId);
            columns = connection.getMetaData().getColumns(connection.getCatalog(), null, "d_mcm" + localChannelId, null);

            while (columns.next()) {
                String name = columns.getString("COLUMN_NAME");

                if (!name.equals("metadata_id") && !name.equals("message_id")) {
                    switch (columns.getInt("DATA_TYPE")) {
                        case Types.VARCHAR:
                        case Types.NVARCHAR:
                        case Types.LONGVARCHAR:
                        case Types.LONGNVARCHAR:
                            metaDataColumns.add(new MetaDataColumn(name, MetaDataColumnType.STRING, null));
                            break;

                        case Types.BIGINT:
                        case Types.INTEGER:
                        case Types.SMALLINT:
                        case Types.TINYINT:
                            metaDataColumns.add(new MetaDataColumn(name, MetaDataColumnType.LONG, null));
                            break;

                        case Types.DOUBLE:
                        case Types.FLOAT:
                        case Types.DECIMAL:
                        case Types.REAL:
                        case Types.NUMERIC:
                            metaDataColumns.add(new MetaDataColumn(name, MetaDataColumnType.DOUBLE, null));
                            break;

                        case Types.BOOLEAN:
                        case Types.BIT:
                            metaDataColumns.add(new MetaDataColumn(name, MetaDataColumnType.BOOLEAN, null));
                            break;

                        case Types.DATE:
                            metaDataColumns.add(new MetaDataColumn(name, MetaDataColumnType.DATE, null));
                            break;

                        case Types.TIME:
                            metaDataColumns.add(new MetaDataColumn(name, MetaDataColumnType.TIME, null));
                            break;

                        case Types.TIMESTAMP:
                            metaDataColumns.add(new MetaDataColumn(name, MetaDataColumnType.TIMESTAMP, null));
                            break;

                        default:
                            throw new SQLException("Invalid custom metadata column: " + name + " (type " + columns.getInt("DATA_TYPE") + ").");
                    }
                }
            }

            return metaDataColumns;
        } catch (SQLException e) {
            throw new DonkeyDaoException("Failed to retrieve meta data columns", e);
        } finally {
            close(columns);
        }
    }

    @Override
    public void removeMetaDataColumn(String channelId, String columnName) {
        Statement statement = null;

        try {
            Map<String, Object> values = new HashMap<String, Object>();
            values.put("localChannelId", channelController.getLocalChannelId(channelId));
            values.put("columnName", columnName);

            statement = connection.createStatement();
            statement.executeUpdate(querySource.getQuery("removeMetaDataColumn", values));
        } catch (SQLException e) {
            throw new DonkeyDaoException("Failed to remove meta-data column", e);
        } finally {
            close(statement);
        }
    }

    @Override
    public long getMaxMessageId(String channelId) {
        ResultSet resultSet = null;

        try {
            resultSet = statementSource.getPreparedStatement("getMaxMessageId", channelController.getLocalChannelId(channelId)).executeQuery();
            return (resultSet.next()) ? resultSet.getLong(1) : null;
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            close(resultSet);
        }
    }

    @Override
    public long getNextMessageId(String channelId) {
        ResultSet resultSet = null;

        try {
            resultSet = statementSource.getPreparedStatement("getNextMessageId", channelController.getLocalChannelId(channelId)).executeQuery();
            return (resultSet.next()) ? resultSet.getLong(1) : null;
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            close(resultSet);
        }
    }

    @Override
    public List<ConnectorMessage> getConnectorMessages(String channelId, int metaDataId, Status status) {
        ResultSet resultSet = null;

        try {
            PreparedStatement statement = statementSource.getPreparedStatement("getConnectorMessagesByMetaDataIdAndStatus", channelController.getLocalChannelId(channelId));
            statement.setInt(1, metaDataId);
            statement.setString(2, Character.toString(status.getStatusCode()));
            resultSet = statement.executeQuery();

            List<ConnectorMessage> connectorMessages = new ArrayList<ConnectorMessage>();

            while (resultSet.next()) {
                connectorMessages.add(getConnectorMessageFromResultSet(channelId, resultSet, true, true));
            }

            return connectorMessages;
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            close(resultSet);
        }
    }

    @Override
    public List<ConnectorMessage> getConnectorMessages(String channelId, int metaDataId, Status status, int offset, int limit, Long minMessageId, Long maxMessageId) {
        ResultSet resultSet = null;

        try {
        	PreparedStatement statement;
        	
        	if (minMessageId == null || maxMessageId == null) {
        		statement = statementSource.getPreparedStatement("getConnectorMessagesByMetaDataIdAndStatusWithLimit", channelController.getLocalChannelId(channelId));
                statement.setInt(1, metaDataId);
                statement.setString(2, Character.toString(status.getStatusCode()));
                statement.setInt(3, offset);
                statement.setInt(4, limit);
        	} else {
        		statement = statementSource.getPreparedStatement("getConnectorMessagesByMetaDataIdAndStatusWithLimitAndRange", channelController.getLocalChannelId(channelId));
                statement.setInt(1, metaDataId);
                statement.setString(2, Character.toString(status.getStatusCode()));
                statement.setLong(3, minMessageId);
                statement.setLong(4, maxMessageId);
                statement.setInt(5, offset);
                statement.setInt(6, limit);
        	}

            resultSet = statement.executeQuery();

            List<ConnectorMessage> connectorMessages = new ArrayList<ConnectorMessage>();

            while (resultSet.next()) {
                connectorMessages.add(getConnectorMessageFromResultSet(channelId, resultSet, true, true));
            }

            return connectorMessages;
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            close(resultSet);
        }
    }

    @Override
    public Map<Integer, ConnectorMessage> getConnectorMessages(String channelId, long messageId) {
        ResultSet resultSet = null;

        try {
            PreparedStatement statement = statementSource.getPreparedStatement("getConnectorMessagesByMessageId", channelController.getLocalChannelId(channelId));
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
        }
    }

    @Override
    public List<Message> getUnfinishedMessages(String channelId, String serverId) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            Map<Long, Message> messageMap = new HashMap<Long, Message>();
            List<Message> messageList = new ArrayList<Message>();
            long localChannelId = channelController.getLocalChannelId(channelId);

            statement = statementSource.getPreparedStatement("getUnfinishedMessages", localChannelId);
            statement.setString(1, serverId);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Message message = getMessageFromResultSet(channelId, resultSet);
                messageMap.put(message.getMessageId(), message);
                messageList.add(message);
            }

            close(resultSet);
            close(statement);

            statement = statementSource.getPreparedStatement("getUnfinishedConnectorMessages", localChannelId);
            statement.setString(1, serverId);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ConnectorMessage connectorMessage = getConnectorMessageFromResultSet(channelId, resultSet, true, true);
                messageMap.get(connectorMessage.getMessageId()).getConnectorMessages().put(connectorMessage.getMetaDataId(), connectorMessage);
            }

            return messageList;
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            close(resultSet);
            close(statement);
        }
    }

    @Override
    public int getConnectorMessageCount(String channelId, int metaDataId, Status status) {
        ResultSet resultSet = null;

        try {
            PreparedStatement statement = statementSource.getPreparedStatement("getConnectorMessageCountByMetaDataIdAndStatus", channelController.getLocalChannelId(channelId));
            statement.setInt(1, metaDataId);
            statement.setString(2, Character.toString(status.getStatusCode()));
            resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            close(resultSet);
        }
    }
    
    @Override
    public long getConnectorMessageMaxMessageId(String channelId, int metaDataId, Status status) {
        ResultSet resultSet = null;

        try {
            PreparedStatement statement = statementSource.getPreparedStatement("getConnectorMessageMaxMessageIdByMetaDataIdAndStatus", channelController.getLocalChannelId(channelId));
            statement.setInt(1, metaDataId);
            statement.setString(2, Character.toString(status.getStatusCode()));
            resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            close(resultSet);
        }
    }

    @Override
    public void insertEvent(Event event) {
        try {
            PreparedStatement statement = statementSource.getPreparedStatement("insertEvent", null);
            Long messageId = event.getMessageId();
            Integer metaDataId = event.getMetaDataId();
            Status status = event.getMessageStatus();

            statement.setInt(1, event.getEventType().getEventCode());
            statement.setString(2, event.getChannelId());

            if (metaDataId == null) {
                statement.setNull(3, Types.INTEGER);
            } else {
                statement.setInt(3, event.getMetaDataId());
            }

            if (messageId == null) {
                statement.setNull(4, Types.BIGINT);
            } else {
                statement.setLong(4, event.getMessageId());
            }

            if (status == null) {
                statement.setNull(5, Types.CHAR);
            } else {
                statement.setString(5, Character.toString(status.getStatusCode()));
            }

            statement.setTimestamp(6, new Timestamp(event.getEventDate().getTimeInMillis()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public void addMetaDataColumn(String channelId, MetaDataColumn metaDataColumn) {
        Statement statement = null;

        try {
            Map<String, Object> values = new HashMap<String, Object>();
            values.put("localChannelId", channelController.getLocalChannelId(channelId));
            values.put("columnName", metaDataColumn.getName());

            statement = connection.createStatement();
            statement.executeUpdate(querySource.getQuery("addMetaDataColumn" + StringUtils.capitalize(StringUtils.lowerCase(metaDataColumn.getType().toString())), values));
        } catch (SQLException e) {
            throw new DonkeyDaoException("Failed to add meta-data column", e);
        } finally {
            close(statement);
        }
    }

    @Override
    public void createChannel(String channelId, long localChannelId) {
        Statement createTableStatement = null;

        try {
            PreparedStatement statement = statementSource.getPreparedStatement("createChannel", null);
            statement.setString(1, channelId);
            statement.setLong(2, localChannelId);
            statement.executeUpdate();

            Map<String, Object> values = new HashMap<String, Object>();
            values.put("localChannelId", localChannelId);

            createTableStatement = connection.createStatement();
            createTableStatement.executeUpdate(querySource.getQuery("createMessageTable", values));
            createTableStatement.executeUpdate(querySource.getQuery("createConnectorMessageTable", values));
            createTableStatement.executeUpdate(querySource.getQuery("createMessageContentTable", values));
            createTableStatement.executeUpdate(querySource.getQuery("createMessageCustomMetaDataTable", values));
            createTableStatement.executeUpdate(querySource.getQuery("createMessageAttachmentTable", values));
            createTableStatement.executeUpdate(querySource.getQuery("createMessageStatisticsTable", values));
            createTableStatement.executeUpdate(querySource.getQuery("createMessageSequence", values));
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            close(createTableStatement);
        }
    }

    @Override
    public void resetStatistics(String channelId, Integer metaDataId, Set<Status> statuses) {
        try {
            if (statuses == null || statuses.size() == 0) {
                return;
            }

            Map<String, Object> values = new HashMap<String, Object>();

            int count = 0;
            StringBuilder builder = new StringBuilder();
            for (Status status : statuses) {
                count++;

                if (count > 1) {
                    builder.append(",");
                }
                builder.append(status.toString() + " = 0");

                if (transactionStats.getChannelStats(channelId).containsKey(metaDataId)) {
                    //TODO Currently this will never get called becauses stats are never reset in a transaction that has transactional stat changes. 
                    // Decide whether to remove this or not.
                    transactionStats.getChannelStats(channelId).get(metaDataId).remove(status);
                }
            }
            values.put("statuses", builder.toString());

            String queryName = (metaDataId == null) ? "resetChannelStatistics" : "resetConnectorStatistics";
            PreparedStatement statement = statementSource.getPreparedStatement(queryName, channelController.getLocalChannelId(channelId), values);

            if (metaDataId != null) {
                statement.setInt(1, metaDataId);
            }

            statement.executeUpdate();

            if (!resetStats.containsKey(channelId)) {
                resetStats.put(channelId, new HashMap<Integer, Set<Status>>());
            }

            Map<Integer, Set<Status>> metaDataIds = resetStats.get(channelId);

            if (!metaDataIds.containsKey(metaDataId)) {
                metaDataIds.put(metaDataId, statuses);
            }

        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public Statistics getChannelStatistics() {
        return getChannelStatistics(false);
    }

    @Override
    public Statistics getChannelTotalStatistics() {
        return getChannelStatistics(true);
    }

    private Statistics getChannelStatistics(boolean total) {
        // FIXME: commented out use of channel controller since there is a problem with the unit tests, for some reason channelController.getLocalChannelIds() returns a channel id for a channel that has been removed
//        Map<String, Long> channelIds = channelController.getLocalChannelIds();
        Map<String, Long> channelIds = getLocalChannelIds();
        String queryId = (total) ? "getChannelTotalStatistics" : "getChannelStatistics";
        Statistics statistics = new Statistics();
        ResultSet resultSet = null;

        for (Entry<String, Long> channelIdEntry : channelIds.entrySet()) {
            String channelId = channelIdEntry.getKey();
            Long localChannelId = channelIdEntry.getValue();

            try {
                PreparedStatement statement = statementSource.getPreparedStatement(queryId, localChannelId);
                resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    Integer metaDataId = resultSet.getInt("metadata_id");

                    if (resultSet.wasNull()) {
                        metaDataId = null;
                    }

                    Map<Status, Long> stats = new HashMap<Status, Long>();
                    stats.put(Status.RECEIVED, resultSet.getLong("received"));
                    stats.put(Status.FILTERED, resultSet.getLong("filtered"));
                    stats.put(Status.TRANSFORMED, resultSet.getLong("transformed"));
                    stats.put(Status.PENDING, resultSet.getLong("pending"));
                    stats.put(Status.SENT, resultSet.getLong("sent"));
                    stats.put(Status.ERROR, resultSet.getLong("error"));

                    statistics.getChannelStats(channelId).put(metaDataId, stats);
                }
            } catch (SQLException e) {
                throw new DonkeyDaoException(e);
            } finally {
                close(resultSet);
            }
        }

        return statistics;
    }
    
    @Override
    public void commit() {
        commit(true);
    }
    
    @Override
    public void commit(boolean durable) {
        addChannelStatistics(transactionStats);

        try {
            if (!durable && asyncCommitCommand != null) {
                Statement statement = null;
                
                try {
                    statement = connection.createStatement();
                    statement.execute(asyncCommitCommand);
                } finally {
                    close(statement);
                }
            } else {
                connection.commit();
            }
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }

        // TODO: need to test for any thread synchronization problems with updating stats across multiple threads
        
        if (currentStats != null) {
            // reset stats for any connectors that need to be reset
            for (Entry<String, Map<Integer, Set<Status>>> entry : resetStats.entrySet()) {
                String channelId = entry.getKey();
                Map<Integer, Set<Status>> metaDataIds = entry.getValue();
    
                for (Entry<Integer, Set<Status>> metaDataEntry : metaDataIds.entrySet()) {
                    Integer metaDataId = metaDataEntry.getKey();
                    Set<Status> statuses = metaDataEntry.getValue();
    
                    for (Status status : statuses) {
                        currentStats.getChannelStats(channelId).get(metaDataId).put(status, 0L);
                    }
                }
            }

            // update the in-memory stats with the stats we just saved in storage
            currentStats.update(transactionStats);
            
            // remove the in-memory stats for any channels that were removed
            for (String channelId : removedChannelIds) {
                currentStats.getStats().remove(channelId);
            }
        }
        
        if (totalStats != null) {
            // update the in-memory total stats with the stats we just saved in storage
            totalStats.update(transactionStats);
            
            // remove the in-memory total stats for any channels that were removed
            for (String channelId : removedChannelIds) {
                totalStats.getStats().remove(channelId);
            }
        }

        transactionStats.getStats().clear();
    }

    @Override
    public void rollback() {
        try {
            connection.rollback();
            transactionStats.getStats().clear();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (!connection.isClosed()) {
                connection.rollback();
            }

            connection.close();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    @Override
    public boolean isClosed() {
        try {
            return connection.isClosed();
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    public boolean initTableStructure() {
        boolean channelsTableExists = tableExists("d_channels");
        boolean eventsTableExists = tableExists("d_events");
        Statement statement = null;

        try {
            if (!channelsTableExists) {
                statement = connection.createStatement();
                statement.executeUpdate(querySource.getQuery("createChannelsTable"));
                close(statement);
            }

            if (!eventsTableExists) {
                statement = connection.createStatement();
                statement.executeUpdate(querySource.getQuery("createEventsTable"));
            }

            return (!channelsTableExists || !eventsTableExists);
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            close(statement);
        }
    }

    private boolean tableExists(String tableName) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.prepareStatement(querySource.getQuery("tableExists"));
            statement.setString(1, tableName);
            resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getBoolean(1);
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            close(resultSet);
            close(statement);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public String getAsyncCommitCommand() {
        return asyncCommitCommand;
    }

    public void setAsyncCommitCommand(String asyncCommitCommand) {
        this.asyncCommitCommand = asyncCommitCommand;
    }

    private Message getMessageFromResultSet(String channelId, ResultSet resultSet) {
        try {
            Message message = new Message();
            long messageId = resultSet.getLong("id");
            Calendar dateCreated = Calendar.getInstance();
            dateCreated.setTimeInMillis(resultSet.getTimestamp("date_created").getTime());

            message.setMessageId(messageId);
            message.setChannelId(channelId);
            message.setDateCreated(dateCreated);
            message.setProcessed(resultSet.getBoolean("processed"));
            message.setServerId(resultSet.getString("server_id"));

            return message;
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    private ConnectorMessage getConnectorMessageFromResultSet(String channelId, ResultSet resultSet, boolean includeContent, boolean includeMetaDataMap) {
        try {
            ConnectorMessage connectorMessage = new ConnectorMessage();
            long messageId = resultSet.getLong("message_id");
            int metaDataId = resultSet.getInt("id");
            Calendar dateCreated = Calendar.getInstance();
            dateCreated.setTimeInMillis(resultSet.getTimestamp("date_created").getTime());

            connectorMessage.setMessageId(messageId);
            connectorMessage.setMetaDataId(metaDataId);
            connectorMessage.setChannelId(channelId);
            connectorMessage.setServerId(resultSet.getString("server_id"));
            connectorMessage.setDateCreated(dateCreated);
            connectorMessage.setStatus(Status.fromChar(resultSet.getString("status").charAt(0)));
            connectorMessage.setConnectorMap((HashMap<String, Object>) serializer.deserialize(resultSet.getString("connector_map")));
            connectorMessage.setChannelMap((HashMap<String, Object>) serializer.deserialize(resultSet.getString("channel_map")));
            connectorMessage.setResponseMap((HashMap<String, Response>) serializer.deserialize(resultSet.getString("response_map")));
            connectorMessage.setErrors(resultSet.getString("errors"));
            connectorMessage.setSendAttempts(resultSet.getInt("send_attempts"));

            if (includeContent) {
                MessageContent rawContent = getMessageContent(channelId, messageId, 0, (metaDataId == 0) ? ContentType.RAW : ContentType.ENCODED);

                if (rawContent != null && metaDataId > 0) {
                    rawContent.setMetaDataId(metaDataId);
                    rawContent.setContentType(ContentType.RAW);
                }

                connectorMessage.setRaw(rawContent);
                connectorMessage.setProcessedRaw(getMessageContent(channelId, messageId, metaDataId, ContentType.PROCESSED_RAW));
                connectorMessage.setTransformed(getMessageContent(channelId, messageId, metaDataId, ContentType.TRANSFORMED));
                connectorMessage.setEncoded(getMessageContent(channelId, messageId, metaDataId, ContentType.ENCODED));
                connectorMessage.setSent(getMessageContent(channelId, messageId, metaDataId, ContentType.SENT));
                connectorMessage.setResponse(getMessageContent(channelId, messageId, metaDataId, ContentType.RESPONSE));
                connectorMessage.setProcessedResponse(getMessageContent(channelId, messageId, metaDataId, ContentType.PROCESSED_RESPONSE));
            }

            if (includeMetaDataMap) {
                connectorMessage.setMetaDataMap(getMetaDataMap(channelId, messageId, metaDataId));
            }

            return connectorMessage;
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    private MessageContent getMessageContentFromResultSet(String channelId, ResultSet resultSet) {
        try {
            MessageContent messageContent = new MessageContent();
            long messageId = resultSet.getLong("message_id");
            int metaDataId = resultSet.getInt("metadata_id");
            Calendar dateCreated = Calendar.getInstance();
            dateCreated.setTimeInMillis(resultSet.getTimestamp("date_created").getTime());

            messageContent.setChannelId(channelId);
            messageContent.setMessageId(messageId);
            messageContent.setMetaDataId(metaDataId);
            messageContent.setContentType(ContentType.fromChar(resultSet.getString("content_type").charAt(0)));
            
            if (resultSet.getBoolean("is_encrypted")) {
                messageContent.setEncryptedContent(resultSet.getString("content"));
            } else {
                messageContent.setContent(resultSet.getString("content"));
            }
            
            return messageContent;
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        }
    }

    private MessageContent getMessageContent(String channelId, long messageId, int metaDataId, ContentType contentType) {
        ResultSet resultSet = null;

        try {
            PreparedStatement statement = statementSource.getPreparedStatement("getMessageContent", channelController.getLocalChannelId(channelId));
            statement.setLong(1, messageId);
            statement.setInt(2, metaDataId);
            statement.setString(3, Character.toString(contentType.getContentTypeCode()));

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String content = null;
                String encryptedContent = null;
                
                if (resultSet.getBoolean("is_encrypted")) {
                    encryptedContent = resultSet.getString("content");
                } else {
                    content = resultSet.getString("content");
                }
                
                return new MessageContent(channelId, messageId, metaDataId, contentType, content, encryptedContent);
            }

            return null;
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            close(resultSet);
        }
    }

    private Map<String, Object> getMetaDataMap(String channelId, long messageId, int metaDataId) {
        ResultSet resultSet = null;

        try {
            Map<String, Object> values = new HashMap<String, Object>();
            values.put("localChannelId", channelController.getLocalChannelId(channelId));

            // do not cache this statement since metadata columns may be added/removed
            PreparedStatement statement = connection.prepareStatement(querySource.getQuery("getMetaDataMap", values));
            statement.setLong(1, messageId);
            statement.setInt(2, metaDataId);

            Map<String, Object> metaDataMap = new HashMap<String, Object>();
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int columnCount = resultSetMetaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    metaDataMap.put(resultSetMetaData.getColumnName(i), resultSet.getObject(i));
                }
            }

            return metaDataMap;
        } catch (SQLException e) {
            throw new DonkeyDaoException(e);
        } finally {
            close(resultSet);
        }
    }

    private void deleteMessageStatistics(String channelId, Collection<ConnectorMessage> connectorMessages) {
        for (ConnectorMessage connectorMessage : connectorMessages) {
            Integer metaDataId = connectorMessage.getMetaDataId();
            Status status = connectorMessage.getStatus();

            Map<Status, Long> statsDiff = new HashMap<Status, Long>();
            statsDiff.put(Status.RECEIVED, -1L);
            statsDiff.put(status, -1L);

            transactionStats.update(channelId, metaDataId, statsDiff);
        }
    }

    private void close(Statement statement) {
        try {
            DbUtils.close(statement);
        } catch (SQLException e) {
            logger.error("Failed to close JDBC statement", e);
        }
    }

    private void close(ResultSet resultSet) {
        try {
            DbUtils.close(resultSet);
        } catch (SQLException e) {
            logger.error("Failed to close JDBC result set", e);
        }
    }
}
