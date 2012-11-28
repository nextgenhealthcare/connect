/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

public interface AutoResponder {

    /**
     * Returns an appropriate auto-response based on the passed status and
     * content. The connector message is included to allow Velocity replacements
     * using the variable maps.
     * 
     * @param status
     *            - The status the responder should use to determine what
     *            response to send back.
     * @param message
     *            - The source connector message raw content to base the
     *            response on.
     * @param connectorMessage
     *            - The ConnectorMessage object used to facilitate Velocity
     *            template replacements. This could be based on one of three
     *            connector messages:
     * 
     *            1) The source connector message before channel processing
     *            occurs
     *            2) The source connector message after channel processing
     *            occurs
     *            3) The merged connector message after post-processing occurs
     *            (if the response is based on the destination connector
     *            statuses).
     * @return The response that should be sent back to the originating system.
     */
    public Response getResponse(Status status, String message, ConnectorMessage connectorMessage);
}
