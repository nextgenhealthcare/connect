package org.dcm4che2.tool.dcmsnd;

import java.io.IOException;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.NetworkConnection;

import com.mirth.connect.connectors.dimse.DICOMConfiguration;

public class MirthDcmSnd extends DcmSnd {

    private DICOMConfiguration dicomConfiguration;

    public MirthDcmSnd(DICOMConfiguration dicomConfiguration) {
        super("DCMSND", false);
        this.dicomConfiguration = dicomConfiguration;
        init();
    }

    public Device getDevice() {
        return device;
    }

    public NetworkConnection getNetworkConnection() {
        return conn;
    }

    public NetworkConnection getRemoteNetworkConnection() {
        return remoteConn;
    }

    public NetworkConnection getRemoteStgcmtNetworkConnection() {
        return remoteStgcmtConn;
    }

    @Override
    protected NetworkConnection createNetworkConnection() {
        return dicomConfiguration.createNetworkConnection();
    }

    @Override
    public synchronized DicomObject waitForStgCmtResult() throws InterruptedException {
        return super.waitForStgCmtResult();
    }

    @Override
    public void neventReport(Association as, int pcid, DicomObject rq, DicomObject info) throws DicomServiceException, IOException {
        DicomObject rsp = CommandUtils.mkRSP(rq, CommandUtils.SUCCESS);
        // Make sure to include UID in N-EVENT-REPORT responses
        // See CommandUtils.mkRSP
        if (!CommandUtils.isIncludeUIDinRSP()) {
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            if (cuid == null) {
                cuid = rq.getString(Tag.RequestedSOPClassUID);
            }
            rsp.putString(Tag.AffectedSOPClassUID, VR.UI, cuid);
            String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
            if (iuid == null) {
                iuid = rq.getString(Tag.RequestedSOPInstanceUID);
            }
            if (iuid != null) {
                rsp.putString(Tag.AffectedSOPInstanceUID, VR.UI, iuid);
            }
        }

        onNEventReportRQ(as, pcid, rq, info, rsp);
        as.writeDimseRSP(pcid, rsp);
        onNEventReportRSP(as, pcid, rq, info, rsp);
    }
}