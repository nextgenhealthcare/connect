package org.dcm4che2.tool.dcmrcv;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.PDVInputStream;

import com.mirth.connect.connectors.dimse.DICOMConfiguration;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.SourceConnector;

public class MirthDcmRcv extends DcmRcv {
    private Logger logger = Logger.getLogger(this.getClass());
    private SourceConnector sourceConnector;
    private DICOMConfiguration dicomConfiguration;

    public MirthDcmRcv(SourceConnector sourceConnector, DICOMConfiguration dicomConfiguration) {
        super("DCMRCV", false);
        this.sourceConnector = sourceConnector;
        this.dicomConfiguration = dicomConfiguration;
        init();
    }

    public Device getDevice() {
        return device;
    }

    public NetworkConnection getNetworkConnection() {
        return nc;
    }

    @Override
    protected NetworkConnection createNetworkConnection() {
        return dicomConfiguration.createNetworkConnection();
    }

    @Override
    void onCStoreRQ(Association as, int pcid, DicomObject rq, PDVInputStream dataStream, String tsuid, DicomObject rsp) throws IOException, DicomServiceException {
        ByteArrayOutputStream baos = null;
        BufferedOutputStream bos = null;
        DicomOutputStream dos = null;

        String cuid = rq.getString(Tag.AffectedSOPClassUID);
        String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
        BasicDicomObject fileMetaInformation = new BasicDicomObject();
        fileMetaInformation.initFileMetaInformation(cuid, iuid, tsuid);

        String originalThreadName = Thread.currentThread().getName();
        try {
            Thread.currentThread().setName("DICOM Receiver Thread on " + sourceConnector.getChannel().getName() + " (" + sourceConnector.getChannelId() + ") < " + originalThreadName);
            baos = new ByteArrayOutputStream();
            bos = new BufferedOutputStream(baos);
            dos = new DicomOutputStream(bos);
            dos.writeFileMetaInformation(fileMetaInformation);
            dataStream.copyTo(dos);
            // MIRTH-2072: This needs to be closed here
            dos.close();

            byte[] dicomMessage = baos.toByteArray();

            // Allow the stream buffers to be garbage collected before the message is processed.
            bos = null;
            baos = null;
            DispatchResult dispatchResult = null;

            try {
                dispatchResult = sourceConnector.dispatchRawMessage(new RawMessage(dicomMessage));
            } finally {
                sourceConnector.finishDispatch(dispatchResult);
            }
        } catch (ChannelException e) {
        } catch (Exception e) {
            logger.error(e);
        } finally {
            Thread.currentThread().setName(originalThreadName);
            // Let the dispose take care of closing the socket
            IOUtils.closeQuietly(baos);
            IOUtils.closeQuietly(bos);
        }
    }

    @Override
    public boolean isStoreFile() {
        return true;
    }
}
