package org.dcm4che2.tool.dcmsnd;

import org.dcm4che2.net.Device;
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
}