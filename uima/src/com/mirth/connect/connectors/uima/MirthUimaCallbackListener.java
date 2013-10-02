/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.uima;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.uima.aae.client.UimaASProcessStatus;
import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.StringArrayFS;
import org.apache.uima.cas.impl.XCASSerializer;
import org.apache.uima.collection.EntityProcessStatus;
import org.xml.sax.SAXException;

import com.mirth.connect.server.util.VMRouter;

public class MirthUimaCallbackListener extends UimaAsBaseCallbackListener {

    private UimaConnector connector;

    public MirthUimaCallbackListener(UimaConnector connector) {
        this.connector = connector;
    }

    @Override
    public void initializationComplete(EntityProcessStatus aStatus) {}

    @Override
    public void collectionProcessComplete(EntityProcessStatus aStatus) {}

    @Override
    public void onBeforeMessageSend(UimaASProcessStatus status) {}

    @Override
    public void entityProcessComplete(CAS aCas, EntityProcessStatus aStatus) {
        if (aStatus instanceof UimaASProcessStatus) {
            // String casReferenceId = ((UimaASProcessStatus)aStatus).getCasReferenceId();
            
            if(aStatus.isException()) {
                if ((connector.getErrorResponseChannelId() != null) && !connector.getErrorResponseChannelId().equals("sink")) {
                    aCas = addExceptionsToCas(aCas, aStatus);
                    
                    new VMRouter().routeMessageByChannelId(connector.getErrorResponseChannelId(), serializeCas(aCas), true);
                }
            } else {
                // send the success response to the respective channel
                if ((connector.getSuccessResponseChannelId() != null) && !connector.getSuccessResponseChannelId().equals("sink")) {
                    new VMRouter().routeMessageByChannelId(connector.getSuccessResponseChannelId(), serializeCas(aCas), true);
                }
            }

        } else {

            // there was something wrong with this CAS, so send it to the error
            // handler
            if ((connector.getErrorResponseChannelId() != null) && !connector.getErrorResponseChannelId().equals("sink")) {
                aCas = addExceptionsToCas(aCas, aStatus);
                
                new VMRouter().routeMessageByChannelId(connector.getErrorResponseChannelId(), serializeCas(aCas), true);
            }

        }
    }

    private CAS addExceptionsToCas(CAS aCas, EntityProcessStatus aStatus) {
        if(aStatus.isException()) {
            List<Exception> casExceptions = aStatus.getExceptions();
            
            
            if(casExceptions.size()>0) {
                
                // create a temp feature set to store the exceptions
                StringArrayFS exceptionFeatures = aCas.createStringArrayFS(casExceptions.size()+1);
                exceptionFeatures.set(0, "EXCEPTIONS");
                int exceptionIndex = 1;
                for (Exception exception : casExceptions) {
                    exceptionFeatures.set(exceptionIndex, exception.toString());
                    exceptionIndex++;
                }
                aCas.addFsToIndexes(exceptionFeatures);
            }
        }
        return aCas;
    }
    
    protected String serializeCas(CAS cas) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            XCASSerializer.serialize(cas, bos, true);
            return bos.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
