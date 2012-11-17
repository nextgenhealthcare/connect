/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.vm;

import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ListenConnector;
import com.mirth.connect.donkey.server.channel.DispatchResult;

public class VmListener extends ListenConnector {

    @Override
    public void onDeploy() throws DeployException {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUndeploy() throws UndeployException {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStart() throws StartException {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStop() throws StopException {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleRecoveredResponse(DispatchResult messageResponse) {
        // TODO Auto-generated method stub
    }

}
