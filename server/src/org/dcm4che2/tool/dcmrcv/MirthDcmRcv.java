package org.dcm4che2.tool.dcmrcv;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
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
        UMOMessage returnMessage = null;
        try {
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
            BasicDicomObject fmi = new BasicDicomObject();
            fmi.initFileMetaInformation(cuid, iuid, tsuid);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(baos);
            DicomOutputStream dos = new DicomOutputStream(bos);
            dos.writeFileMetaInformation(fmi);
            dataStream.copyTo(dos);
            dos.close();
            String dcmString = new String(new Base64().encode(baos.toByteArray()));
            returnMessage = messageReceiver.routeMessage(new MuleMessage(dcmString), endpoint.isSynchronous());
            // We need to check the message status
            if (returnMessage != null && returnMessage instanceof MuleMessage) {
                Object payload = returnMessage.getPayload();
                if (payload instanceof MessageObject) {
                    MessageObject messageObjectResponse = (MessageObject) payload;
                    postProcessor.doPostProcess(messageObjectResponse);
                }
            }
        } catch (Exception e) {
        } finally {
            // Let the dispose take care of closing the socket
        }
    }

    public boolean isStoreFile() {
        return true;
    }

}
