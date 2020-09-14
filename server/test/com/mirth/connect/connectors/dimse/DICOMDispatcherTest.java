package com.mirth.connect.connectors.dimse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SequenceDicomElement;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.tool.dcmsnd.CustomDimseRSPHandler;
import org.dcm4che2.tool.dcmsnd.MirthDcmSnd;
import org.dcm4che2.util.StringUtils;
import org.junit.Test;

import com.mirth.connect.connectors.dimse.DICOMDispatcher.CommandDataDimseRSPHandler;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProvider;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;
import com.mirth.connect.server.attachments.dicom.DICOMAttachmentHandlerProvider;
import com.mirth.connect.server.controllers.MessageController;

public class DICOMDispatcherTest {

    @Test
    public void testSendWithStatusCodes() {
        // send message using our custom MirthDcmSnd
        TestDICOMDispatcher dispatcher = new TestDICOMDispatcher();
        dispatcher.configuration = new DefaultDICOMConfiguration();
        DICOMDispatcherProperties props = new DICOMDispatcherProperties();
        props.setHost("host");
        props.setPort("9000");
        ConnectorMessage message = new ConnectorMessage();

        Response response = null;
        Status status = null;
        String statusMessage = null;

        TestMirthDcmSnd.setCommitSucceeded(true);
        TestMirthDcmSnd.setCmdStatus(0);
        response = dispatcher.send(props, message);
        status = response.getStatus();
        statusMessage = response.getStatusMessage();

        // check with 0 status
        assertEquals(Status.SENT, status);
        assertEquals("DICOM message successfully sent", statusMessage);

        // check with 0xB000 || 0xB006 || 0xB007 status
        TestMirthDcmSnd.setCmdStatus(0xB000);
        response = dispatcher.send(props, message);
        status = response.getStatus();
        statusMessage = response.getStatusMessage();
        assertEquals(Status.SENT, status);
        assertEquals("DICOM message successfully sent with warning status code: 0x" + StringUtils.shortToHex(0xB000), statusMessage);

        TestMirthDcmSnd.setCmdStatus(0xB006);
        response = dispatcher.send(props, message);
        status = response.getStatus();
        statusMessage = response.getStatusMessage();
        assertEquals(Status.SENT, status);
        assertEquals("DICOM message successfully sent with warning status code: 0x" + StringUtils.shortToHex(0xB006), statusMessage);

        TestMirthDcmSnd.setCmdStatus(0xB007);
        response = dispatcher.send(props, message);
        status = response.getStatus();
        statusMessage = response.getStatusMessage();
        assertEquals(Status.SENT, status);
        assertEquals("DICOM message successfully sent with warning status code: 0x" + StringUtils.shortToHex(0xB007), statusMessage);

        // check other status == QUEUED
        TestMirthDcmSnd.setCmdStatus(0xB008);
        response = dispatcher.send(props, message);
        status = response.getStatus();
        statusMessage = response.getStatusMessage();
        assertEquals(Status.QUEUED, status);
        assertEquals("Error status code received from DICOM server: 0x" + StringUtils.shortToHex(0xB008), statusMessage);
    }

    @Test
    public void testResponseData() throws DonkeyElementException {
        // send message using our custom MirthDcmSnd
        TestDICOMDispatcher dispatcher = new TestDICOMDispatcher();
        dispatcher.configuration = new DefaultDICOMConfiguration();
        DICOMDispatcherProperties props = new DICOMDispatcherProperties();
        props.setHost("host");
        props.setPort("9000");
        ConnectorMessage message = new ConnectorMessage();

        TestMirthDcmSnd.setCmdStatus(0);
        TestMirthDcmSnd.setCommitSucceeded(true);
        Response response = dispatcher.send(props, message);
        String responseData = response.getMessage();

        String expectedResponseString = "<dicom>\n" + "<tag00000900 len=\"2\" tag=\"00000900\" vr=\"IS\">0</tag00000900>\n" + "</dicom>\n" + "";
        DonkeyElement dicom = new DonkeyElement(expectedResponseString);
        assertEquals(dicom.toXml(), responseData);
    }

    @Test
    public void testStorageCommitment() throws Exception {
        TestDICOMDispatcher dispatcher = new TestDICOMDispatcher();
        dispatcher.configuration = new DefaultDICOMConfiguration();
        DICOMDispatcherProperties props = new DICOMDispatcherProperties();
        props.setHost("host");
        props.setPort("9000");
        props.setStgcmt(true);
        ConnectorMessage message = new ConnectorMessage();

        TestMirthDcmSnd.setCmdStatus(0);
        TestMirthDcmSnd.setCommitSucceeded(false);

        Response response = null;
        Status status = null;
        String statusMessage = null;

        response = dispatcher.send(props, message);
        status = response.getStatus();
        statusMessage = response.getStatusMessage();

        assertEquals(Status.QUEUED, status);
        assertEquals("DICOM message successfully sent but Storage Commitment failed with reason: Unknown", statusMessage);

        // Test the case where the stgcmt request succeeds but contains failed SOP items
        TestMirthDcmSnd.setCommitSucceeded(true);
        TestMirthDcmSnd.setFailedSOP(true);
        TestMirthDcmSnd.setFailureReason(1);

        response = dispatcher.send(props, message);
        status = response.getStatus();
        statusMessage = response.getStatusMessage();

        assertEquals(Status.QUEUED, status);
        assertEquals("DICOM message successfully sent but Storage Commitment failed with reason: 1", statusMessage);

        TestMirthDcmSnd.setCommitSucceeded(false);
        TestMirthDcmSnd.setFailedSOP(false);
        TestMirthDcmSnd.setFailureReason(0);

        // test that a failed storage commitment doesn't cause the message to fail 
        // if the dispatcher isn't configured to care
        props.setStgcmt(false);
        response = dispatcher.send(props, message);
        status = response.getStatus();
        statusMessage = response.getStatusMessage();

        assertEquals(Status.SENT, status);
        assertEquals("DICOM message successfully sent", statusMessage);

        // check with 0xB000 and requesting storage commitment
        props.setStgcmt(true);
        TestMirthDcmSnd.setCmdStatus(0xB000);
        response = dispatcher.send(props, message);
        status = response.getStatus();
        statusMessage = response.getStatusMessage();
        assertEquals(Status.QUEUED, status);
        String expectedMessage = "DICOM message successfully sent with warning status code: 0x" + StringUtils.shortToHex(0xB000) + " but Storage Commitment failed with reason: Unknown";
        assertEquals(expectedMessage, statusMessage);

        // check other status and requesting storage commitment
        TestMirthDcmSnd.setCmdStatus(0xB008);
        response = dispatcher.send(props, message);
        status = response.getStatus();
        statusMessage = response.getStatusMessage();
        assertEquals(Status.QUEUED, status);
        assertEquals("Error status code received from DICOM server: 0x" + StringUtils.shortToHex(0xB008), statusMessage);
    }

    private static class TestMirthDcmSnd extends MirthDcmSnd {
        private static int cmdStatus;
        private static boolean commitSucceeded = true;
        private static boolean failedSOP = false;
        private static int failureReason = 0;

        public TestMirthDcmSnd(DICOMConfiguration configuration) {
            super(configuration);
        }

        public static void setCmdStatus(int status) {
            cmdStatus = status;
        }

        public static void setCommitSucceeded(boolean succeeded) {
            commitSucceeded = succeeded;
        }

        public static void setFailedSOP(boolean failedSOP) {
            TestMirthDcmSnd.failedSOP = failedSOP;
        }

        public static void setFailureReason(int failureReason) {
            TestMirthDcmSnd.failureReason = failureReason;
        }

        @Override
        protected void init() {
            conn = createNetworkConnection();
            remoteConn = createNetworkConnection();
        }

        @Override
        public void start() throws IOException {}

        @Override
        public void open() throws IOException, ConfigurationException, InterruptedException {}

        @Override
        public void close() {}

        @Override
        public void stop() {}

        @Override
        public void addFile(File f) {}

        @Override
        public void send(CustomDimseRSPHandler responseHandler) {
            CommandDataDimseRSPHandler handler = (CommandDataDimseRSPHandler) responseHandler;
            BasicDicomObject cmd = new BasicDicomObject();
            cmd.putInt(Tag.Status, VR.IS, cmdStatus);
            handler.onDimseRSP(null, cmd, null);
        }

        @Override
        public synchronized DicomObject waitForStgCmtResult() throws InterruptedException {
            BasicDicomObject rsp = new BasicDicomObject();
            if (failedSOP) {
                SequenceDicomElement failedSOPSq = (SequenceDicomElement) rsp.putSequence(Tag.FailedSOPSequence);
                BasicDicomObject failedSOPItem = new BasicDicomObject();
                failedSOPItem.putInt(Tag.FailureReason, VR.IS, failureReason);
                failedSOPSq.addDicomObject(failedSOPItem);
            }
            return rsp;
        }

        @Override
        protected NetworkConnection createNetworkConnection() {
            return mock(NetworkConnection.class);
        }

        @Override
        public boolean commit() {
            return commitSucceeded;
        }
    }

    private class TestDICOMDispatcher extends DICOMDispatcher {
        @Override
        protected MirthDcmSnd getDcmSnd(DICOMConfiguration configuration) {
            return new TestMirthDcmSnd(configuration);
        }

        @Override
        protected AttachmentHandlerProvider getAttachmentHandlerProvider() {
            return new TestAttachmentHandlerProvider(null);
        }
    }

    private class TestAttachmentHandlerProvider extends DICOMAttachmentHandlerProvider {
        public TestAttachmentHandlerProvider(MessageController messageController) {
            super(messageController);
        }

        @Override
        public byte[] reAttachMessage(String raw, ConnectorMessage connectorMessage, String charsetEncoding, boolean binary, boolean reattach) {
            return "".getBytes();
        }
    }
}
