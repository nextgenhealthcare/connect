package com.mirth.connect.plugins.messagepruner;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.DonkeyConfiguration;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.controllers.MessageController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.tests.TestUtils;

public class MessagePrunerTest {
    private final static String TEST_CHANNEL_ID = "prunerTestChannel";
    private final static String TEST_SERVER_ID = "testServerId";
    private final static String TEST_MESSAGE_CONTENT = TestUtils.TEST_HL7_MESSAGE;
    private final static int TEST_POWER = 7;

    private static DefaultMessagePruner pruner;
    private static TestArchiver archiver;
    private static Calendar messageDateThreshold;
    private static Calendar contentDateThreshold;

    @BeforeClass
    public final static void init() throws Exception {
        ConfigurationController configurationController = ConfigurationController.getInstance();
        configurationController.initializeSecuritySettings();
        configurationController.initializeDatabaseSettings();

        Donkey donkey = Donkey.getInstance();
        donkey.startEngine(new DonkeyConfiguration(configurationController.getApplicationDataDir(), configurationController.getDatabaseSettings().getProperties()));

        archiver = new TestArchiver();

        messageDateThreshold = Calendar.getInstance();
        messageDateThreshold.set(Calendar.DAY_OF_MONTH, messageDateThreshold.get(Calendar.DAY_OF_MONTH) - 90);

        contentDateThreshold = Calendar.getInstance();
        contentDateThreshold.set(Calendar.DAY_OF_MONTH, contentDateThreshold.get(Calendar.DAY_OF_MONTH) - 45);

        pruner = new DefaultMessagePruner();
        pruner.setRetryCount(3);
        pruner.setSkipIncomplete(true);
        pruner.setSkipStatuses(Arrays.asList(new Status[] { Status.ERROR, Status.QUEUED }));
        pruner.setMessageArchiver(archiver);
    }

    @Before
    public final void before() {
        if (archiver != null) {
            archiver.reset();
        }
    }

    @Test
    public final void testPruneMetadataAndContent() throws Exception {
        prepareTestMessages(true, true, true, Status.SENT);
        pruner.executePruner(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold);
        assertMessagesPruned();
        assertContentPruned();
        assertMessagesArchived();
    }

    @Test
    public final void testPruneContentOnly() throws Exception {
        prepareTestMessages(false, true, true, Status.SENT);
        pruner.executePruner(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold);
        assertMessagesNotPruned();
        assertContentPruned();
        assertMessagesArchived();
    }

    @Test
    public final void testPruneNone() throws Exception {
        prepareTestMessages(false, false, true, Status.SENT);
        pruner.executePruner(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold);
        assertMessagesNotPruned();
        assertContentNotPruned();
        assertMessagesNotArchived();
    }

    @Test
    public final void testPruneSkipIncomplete() throws Exception {
        prepareTestMessages(true, true, false, Status.SENT);
        pruner.executePruner(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold);
        assertMessagesNotPruned();
        assertContentNotPruned();
        assertMessagesNotArchived();
    }

    @Test
    public final void testPruneIncomplete() throws Exception {
        if (pruner instanceof DefaultMessagePruner) {
            ((DefaultMessagePruner) pruner).setSkipIncomplete(false);
        } else {
            System.err.println("Skipping testPruneIncomplete()");
            return;
        }

        prepareTestMessages(true, true, false, Status.SENT);
        pruner.executePruner(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold);
        assertMessagesPruned();
        assertContentPruned();
        assertMessagesArchived();
    }

    @Test
    public final void testPruneQueued() throws Exception {
        prepareTestMessages(true, true, true, Status.QUEUED);
        pruner.executePruner(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold);
        assertMessagesNotPruned();
        assertContentNotPruned();
        assertMessagesNotArchived();
    }

    @Test
    public final void testPruneErrored() throws Exception {
        prepareTestMessages(true, true, true, Status.ERROR);
        pruner.executePruner(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold);
        assertMessagesNotPruned();
        assertContentNotPruned();
        assertMessagesNotArchived();
    }

    @Test
    @Ignore
    public final void testPrunePerformance() throws Exception {
        final int power = 14;
        final int numPrunable = (int) Math.pow(2, power);

        prepareTestMessages(false, false, true, Status.SENT, power);

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = TestUtils.getConnection();
            long localChannelId = ChannelController.getInstance().getLocalChannelId(TEST_CHANNEL_ID);

            Calendar dateCreated = Calendar.getInstance();
            dateCreated.setTime(messageDateThreshold.getTime());
            dateCreated.set(Calendar.DAY_OF_MONTH, dateCreated.get(Calendar.DAY_OF_MONTH) - 1);

            statement = connection.prepareStatement("UPDATE d_m" + localChannelId + " SET date_created = ? WHERE id = any(array(SELECT id FROM d_m" + localChannelId + " LIMIT ?))");
            statement.setTimestamp(1, new Timestamp(dateCreated.getTimeInMillis()));
            statement.setInt(2, numPrunable);

            System.out.print("Making " + numPrunable + " messages prunable...");
            statement.executeUpdate();
            connection.commit();
            System.out.println("done");
        } finally {
            DbUtils.close(statement);
            DbUtils.close(connection);
        }

        System.out.print("Running performance test...");
        long startTime = System.currentTimeMillis();

        pruner.executePruner(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        int testSize = (int) Math.pow(2, power);

        assertEquals(testSize - numPrunable, TestUtils.getNumMessages(TEST_CHANNEL_ID));
        assertEquals(testSize - numPrunable, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));

        System.out.println("Pruned " + numPrunable + " messages in " + duration + "ms, " + TestUtils.getPerSecondRate(numPrunable, duration, 1) + " messages per second");
    }

    private void prepareTestMessages(boolean messagesPrunable, Boolean contentPrunable, boolean processed, Status destinationStatus) throws Exception {
        prepareTestMessages(messagesPrunable, contentPrunable, processed, destinationStatus, TEST_POWER);
    }

    private void prepareTestMessages(boolean messagesPrunable, Boolean contentPrunable, boolean processed, Status destinationStatus, int power) throws Exception {
        Calendar dateThreshold;

        if (messagesPrunable) {
            dateThreshold = messageDateThreshold;
        } else if (contentPrunable) {
            dateThreshold = contentDateThreshold;
        } else {
            dateThreshold = Calendar.getInstance();
        }

        Calendar dateCreated = Calendar.getInstance();
        dateCreated.setTime(dateThreshold.getTime());
        dateCreated.set(Calendar.DAY_OF_MONTH, dateCreated.get(Calendar.DAY_OF_MONTH) - 1);

        Message message = MessageController.getInstance().createNewMessage(TEST_CHANNEL_ID, TEST_SERVER_ID);
        message.setDateCreated(dateCreated);
        message.setProcessed(processed);

        ConnectorMessage sourceMessage = new ConnectorMessage(TEST_CHANNEL_ID, message.getMessageId(), 0, TEST_SERVER_ID, message.getDateCreated(), Status.RECEIVED);
        message.getConnectorMessages().put(0, sourceMessage);

        ConnectorMessage destinationMessage = new ConnectorMessage(TEST_CHANNEL_ID, message.getMessageId(), 1, TEST_SERVER_ID, message.getDateCreated(), destinationStatus);
        message.getConnectorMessages().put(1, destinationMessage);

        if (contentPrunable != null) {
            sourceMessage.setRaw(new MessageContent(TEST_CHANNEL_ID, message.getMessageId(), 0, ContentType.RAW, TEST_MESSAGE_CONTENT, null));
            destinationMessage.setRaw(new MessageContent(TEST_CHANNEL_ID, message.getMessageId(), 1, ContentType.RAW, TEST_MESSAGE_CONTENT, null));
        }

        TestUtils.createTestMessagesFast(TEST_CHANNEL_ID, message, power);

        int testSize = (int) Math.pow(2, power);
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID));

        if (contentPrunable != null) {
            assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
        } else {
            assertEquals(0, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
        }
    }

    private void assertMessagesPruned() throws Exception {
        assertEquals(0, TestUtils.getNumMessages(TEST_CHANNEL_ID));
    }

    private void assertMessagesNotPruned() throws Exception {
        assertEquals((int) Math.pow(2, TEST_POWER), TestUtils.getNumMessages(TEST_CHANNEL_ID));
    }

    private void assertContentPruned() throws Exception {
        assertEquals(0, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
    }

    private void assertContentNotPruned() throws Exception {
        assertEquals((int) Math.pow(2, TEST_POWER), TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
    }

    private void assertMessagesArchived() {
        if (archiver != null) {
            assertEquals((int) Math.pow(2, TEST_POWER), archiver.getNumMessages());
        }
    }

    private void assertMessagesNotArchived() {
        if (archiver != null) {
            assertEquals(0, archiver.getNumMessages());
        }
    }

    private static class TestArchiver implements MessageArchiver {
        private Set<Long> archivedMessageIds = new HashSet<Long>();

        @Override
        public void archiveMessage(Message message) {
            archivedMessageIds.add(message.getMessageId());
        }

        @Override
        public boolean isArchived(long messageId) {
            return archivedMessageIds.contains(messageId);
        }

        public int getNumMessages() {
            return archivedMessageIds.size();
        }

        public void reset() {
            archivedMessageIds.clear();
        }
    }
}
