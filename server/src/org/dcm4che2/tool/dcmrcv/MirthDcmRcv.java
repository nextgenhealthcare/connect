package org.dcm4che2.tool.dcmrcv;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.PDVInputStream;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.server.mule.transformers.JavaScriptPostprocessor;

public class MirthDcmRcv extends DcmRcv {
    private Logger logger = Logger.getLogger(this.getClass());
    private AbstractMessageReceiver messageReceiver;
    private JavaScriptPostprocessor postProcessor;
    private UMOEndpoint endpoint;

    public MirthDcmRcv(AbstractMessageReceiver messageReceiver, JavaScriptPostprocessor postProcessor, UMOEndpoint endpoint) {
        super("DCMRCV");
        this.messageReceiver = messageReceiver;
        this.postProcessor = postProcessor;
        this.endpoint = endpoint;
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

        try {
            baos = new ByteArrayOutputStream();
            bos = new BufferedOutputStream(baos);
            dos = new DicomOutputStream(bos);
            dos.writeFileMetaInformation(fileMetaInformation);
            dataStream.copyTo(dos);
            // MIRTH-2072: This needs to be closed here
            dos.close();

            String dicomMessage = Base64.encodeBase64String(baos.toByteArray());
            UMOMessage response = messageReceiver.routeMessage(new MuleMessage(dicomMessage), endpoint.isSynchronous());

            // We need to check the message status
            if ((response != null) && (response instanceof MuleMessage)) {
                Object payload = response.getPayload();

                if (payload instanceof MessageObject) {
                    postProcessor.doPostProcess((MessageObject) payload);
                }
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
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
