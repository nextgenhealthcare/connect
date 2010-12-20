/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.servlets;

import java.io.IOException;
import java.net.URLDecoder;

import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.ErrorPageHandler;

public class MirthErrorPageHandler extends ErrorPageHandler {

    private static final long serialVersionUID = 1L;

    public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse response) throws HttpException, IOException {
        response.setReason(URLDecoder.decode(response.getReason(), "UTF-8"));
        response.setField("X-Mirth-Error", response.getReason());
        super.handle(pathInContext, pathParams, request, response);
    }
}
