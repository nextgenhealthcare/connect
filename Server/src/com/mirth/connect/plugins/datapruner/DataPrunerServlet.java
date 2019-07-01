/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datapruner;

import java.util.Calendar;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;

import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.server.api.MirthServlet;

public class DataPrunerServlet extends MirthServlet implements DataPrunerServletInterface {

    private static final DataPrunerController dataPrunerController = DataPrunerController.getInstance();
    private static final Logger logger = Logger.getLogger(DataPrunerServlet.class);

    public DataPrunerServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PLUGIN_POINT);
    }

    @Override
    public Map<String, String> getStatusMap() {
        try {
            return dataPrunerController.getStatusMap();
        } catch (DataPrunerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public Calendar start() {
        try {
            dataPrunerController.startPruner();
            return dataPrunerController.getPrunerStatus().getStartTime();
        } catch (DataPrunerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public void stop() {
        try {
            dataPrunerController.stopPruner();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Stopped waiting for the data pruner to stop, due to a thread interruption.", e);
        } catch (DataPrunerException e) {
            throw new MirthApiException(e);
        }
    }
}