/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.jdbc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.data.StatisticsUpdater;
import com.mirth.connect.donkey.util.SerializerProvider;

public class JdbcDaoTest {

    // Test with no current attachment data in database
    @Test
    public void testUpdateMessageAttachment1() throws Exception {
        String channelId = "testchannel";
        long messageId = 1;
        Attachment attachment = new Attachment("testattachment", "testing".getBytes(), "text/plain");

        ResultSet segmentCountResult = mock(ResultSet.class);
        PreparedStatement segmentCountStatement = mock(PreparedStatement.class);
        PreparedStatement updateStatement = mock(PreparedStatement.class);
        PreparedStatement insertStatement = mock(PreparedStatement.class);
        PreparedStatement deleteStatement = mock(PreparedStatement.class);
        JdbcDao dao = getDao(channelId, segmentCountResult, segmentCountStatement, updateStatement, insertStatement, deleteStatement);

        when(segmentCountResult.getInt(1)).thenReturn(0);

        dao.updateMessageAttachment(channelId, messageId, attachment);

        verify(segmentCountStatement, times(1)).setString(1, attachment.getId());
        verify(segmentCountStatement, times(1)).setLong(2, messageId);
        verify(segmentCountStatement, times(1)).executeQuery();

        verify(updateStatement, times(1)).setString(1, attachment.getType());
        verify(updateStatement, times(1)).setString(4, attachment.getId());
        verify(updateStatement, times(1)).setLong(5, messageId);

        verify(insertStatement, times(1)).setString(1, attachment.getId());
        verify(insertStatement, times(1)).setLong(2, messageId);
        verify(insertStatement, times(1)).setString(3, attachment.getType());

        verify(insertStatement, times(1)).setInt(4, 1);
        verify(insertStatement, times(1)).setInt(5, attachment.getContent().length);
        verify(insertStatement, times(1)).setBytes(6, attachment.getContent());
        verify(insertStatement, times(1)).executeUpdate();

        verify(updateStatement, times(1)).clearParameters();
        verify(insertStatement, times(1)).clearParameters();

        verify(deleteStatement, times(0)).setString(1, attachment.getId());
        verify(deleteStatement, times(0)).setLong(2, messageId);
        verify(deleteStatement, times(0)).setInt(eq(3), anyInt());
        verify(deleteStatement, times(0)).executeUpdate();
    }

    // Test with attachment data in database
    @Test
    public void testUpdateMessageAttachment2() throws Exception {
        String channelId = "testchannel";
        long messageId = 1;
        Attachment attachment = new Attachment("testattachment", "testing".getBytes(), "text/plain");

        ResultSet segmentCountResult = mock(ResultSet.class);
        PreparedStatement segmentCountStatement = mock(PreparedStatement.class);
        PreparedStatement updateStatement = mock(PreparedStatement.class);
        PreparedStatement insertStatement = mock(PreparedStatement.class);
        PreparedStatement deleteStatement = mock(PreparedStatement.class);
        JdbcDao dao = getDao(channelId, segmentCountResult, segmentCountStatement, updateStatement, insertStatement, deleteStatement);

        when(segmentCountResult.getInt(1)).thenReturn(2);

        dao.updateMessageAttachment(channelId, messageId, attachment);

        verify(segmentCountStatement, times(1)).setString(1, attachment.getId());
        verify(segmentCountStatement, times(1)).setLong(2, messageId);
        verify(segmentCountStatement, times(1)).executeQuery();

        verify(updateStatement, times(1)).setString(1, attachment.getType());
        verify(updateStatement, times(1)).setString(4, attachment.getId());
        verify(updateStatement, times(1)).setLong(5, messageId);

        verify(insertStatement, times(1)).setString(1, attachment.getId());
        verify(insertStatement, times(1)).setLong(2, messageId);
        verify(insertStatement, times(1)).setString(3, attachment.getType());

        verify(updateStatement, times(1)).setInt(6, 1);
        verify(updateStatement, times(1)).setInt(2, attachment.getContent().length);
        verify(updateStatement, times(1)).setBytes(3, attachment.getContent());
        verify(updateStatement, times(1)).executeUpdate();

        verify(updateStatement, times(1)).clearParameters();
        verify(insertStatement, times(1)).clearParameters();

        verify(deleteStatement, times(1)).setString(1, attachment.getId());
        verify(deleteStatement, times(1)).setLong(2, messageId);
        verify(deleteStatement, times(1)).setInt(eq(3), eq(2));
        verify(deleteStatement, times(1)).executeUpdate();
    }

    // Test with large attachment data
    @Test
    public void testUpdateMessageAttachment3() throws Exception {
        String channelId = "testchannel";
        long messageId = 1;
        Attachment attachment = new Attachment("testattachment", StringUtils.repeat("testtest", "", 10 * 1024 * 1024 / 8).getBytes(), "text/plain");

        ResultSet segmentCountResult = mock(ResultSet.class);
        PreparedStatement segmentCountStatement = mock(PreparedStatement.class);
        PreparedStatement updateStatement = mock(PreparedStatement.class);
        PreparedStatement insertStatement = mock(PreparedStatement.class);
        PreparedStatement deleteStatement = mock(PreparedStatement.class);
        JdbcDao dao = getDao(channelId, segmentCountResult, segmentCountStatement, updateStatement, insertStatement, deleteStatement);

        when(segmentCountResult.getInt(1)).thenReturn(1);

        dao.updateMessageAttachment(channelId, messageId, attachment);

        verify(segmentCountStatement, times(1)).setString(1, attachment.getId());
        verify(segmentCountStatement, times(1)).setLong(2, messageId);
        verify(segmentCountStatement, times(1)).executeQuery();

        verify(updateStatement, times(1)).setString(1, attachment.getType());
        verify(updateStatement, times(1)).setString(4, attachment.getId());
        verify(updateStatement, times(1)).setLong(5, messageId);

        verify(insertStatement, times(1)).setString(1, attachment.getId());
        verify(insertStatement, times(1)).setLong(2, messageId);
        verify(insertStatement, times(1)).setString(3, attachment.getType());

        verify(updateStatement, times(1)).setInt(6, 1);
        verify(updateStatement, times(1)).setInt(2, 10000000);
        verify(updateStatement, times(1)).setBytes(3, Arrays.copyOfRange(attachment.getContent(), 0, 10000000));
        verify(updateStatement, times(1)).executeUpdate();

        verify(insertStatement, times(1)).setInt(4, 2);
        verify(insertStatement, times(1)).setInt(5, attachment.getContent().length - 10000000);
        verify(insertStatement, times(1)).setBytes(6, Arrays.copyOfRange(attachment.getContent(), 10000000, attachment.getContent().length));
        verify(insertStatement, times(1)).executeUpdate();

        verify(updateStatement, times(1)).clearParameters();
        verify(insertStatement, times(1)).clearParameters();

        verify(deleteStatement, times(0)).setString(1, attachment.getId());
        verify(deleteStatement, times(0)).setLong(2, messageId);
        verify(deleteStatement, times(0)).setInt(eq(3), anyInt());
        verify(deleteStatement, times(0)).executeUpdate();
    }

    private JdbcDao getDao(String channelId, ResultSet segmentCountResult, PreparedStatement segmentCountStatement, PreparedStatement updateStatement, PreparedStatement insertStatement, PreparedStatement deleteStatement) throws Exception {
        Donkey donkey = mock(Donkey.class);
        Connection connection = mock(Connection.class);
        QuerySource querySource = mock(QuerySource.class);
        PreparedStatementSource statementSource = mock(PreparedStatementSource.class);
        SerializerProvider serializerProvider = mock(SerializerProvider.class);
        boolean encryptData = false;
        boolean decryptData = false;
        StatisticsUpdater statisticsUpdater = mock(StatisticsUpdater.class);
        Statistics currentStats = mock(Statistics.class);
        Statistics totalStats = mock(Statistics.class);
        String statsServerId = "";

        JdbcDao dao = spy(new JdbcDao(donkey, connection, querySource, statementSource, serializerProvider, encryptData, decryptData, statisticsUpdater, currentStats, totalStats, statsServerId));

        when(segmentCountStatement.executeQuery()).thenReturn(segmentCountResult);
        doReturn(segmentCountStatement).when(dao).prepareStatement(eq("selectMessageAttachmentSegmentCount"), eq(channelId));

        doReturn(updateStatement).when(dao).prepareStatement(eq("updateMessageAttachment"), eq(channelId));

        doReturn(insertStatement).when(dao).prepareStatement(eq("insertMessageAttachment"), eq(channelId));

        doReturn(deleteStatement).when(dao).prepareStatement(eq("deleteMessageAttachmentLingeringSegments"), eq(channelId));

        return dao;
    }
}
