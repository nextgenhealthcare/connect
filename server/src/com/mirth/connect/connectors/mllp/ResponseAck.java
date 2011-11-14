/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.mllp;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mirth.connect.connectors.mllp.HL7v2XMLQuickParser.HL7v2Header;

public class ResponseAck {
    private Log logger = LogFactory.getLog(this.getClass());

    private String ackMessageString = "";
    private String errorDescription = null;
    private boolean successAck = false;

    public ResponseAck(String ackMessageString) {
        // save the incoming ack message
        this.ackMessageString = ackMessageString;

        try {
            if (ackMessageString.startsWith("<")) {
                HL7v2Header headerData = HL7v2XMLQuickParser.getInstance().processMSA(ackMessageString);
                if (headerData.getParseError() == null) {
                    setStatus(headerData.getAcknowledgmentCode(), headerData.getAckMessageControlId(), headerData.getAckTextMessage(), headerData.getError());
                } else {
                    throw new Exception(headerData.getParseError());
                }
            } else {
                char segmentDelim = '\r';
                char fieldDelim = ackMessageString.charAt(3); // Usually |

                Pattern segmentPattern = Pattern.compile(Pattern.quote(String.valueOf(segmentDelim)));
                Pattern fieldPattern = Pattern.compile(Pattern.quote(String.valueOf(fieldDelim)));

                String msa1 = "";
                String msa2 = "";
                String msa3 = "";
                String err1 = "";
                String[] segments = segmentPattern.split(ackMessageString);
                for (String segment : segments) {
                    String[] fields = fieldPattern.split(segment);

                    if (fields[0].equals("MSA")) {
                        if (fields.length > 1) {
                            msa1 = fields[1];
                        }
                        if (fields.length > 3) {
                            msa3 = fields[3];
                        }
                    } else if (fields[0].equals("ERR")) {
                        if (fields.length > 1) {
                            err1 = fields[1];
                        }
                    }
                }

                setStatus(msa1, msa2, msa3, err1);
            }

            logger.debug("ACK: " + ackMessageString);
        } catch (Exception e) {
            errorDescription = " Message is not a valid ACK";
            errorDescription += "\n" + e + "\n" + ackMessageString;
        }
    }

    private void setStatus(String msa1, String msa2, String msa3, String err1) {
        if (msa1.equals("AA") || msa1.equals("CA")) {
            successAck = true;
            errorDescription = "";
        } else if (msa1.equals("AR") || msa1.equals("CR")) {
            successAck = false;
            errorDescription = " [Application Reject]" + "\n" + msa3 + "\n" + err1;
        } else if (msa1.equals("AE") || msa1.equals("CE")) {
            successAck = false;
            errorDescription = " [Application Error]" + "\n" + msa3 + "\n" + err1;
        } else {
            successAck = false;
            errorDescription = "Unknown response type (" + msa1 + ") \n" + msa3 + "\n" + err1 + "\n\n" + ackMessageString;
        }
    }

    public boolean isSuccessAck() {
        return successAck;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}