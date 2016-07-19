/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.token.js;

import javax.swing.Action;

import org.fife.ui.rsyntaxtextarea.TokenMap;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.fife.ui.rsyntaxtextarea.modes.JavaScriptTokenMaker;

public class MirthJavaScriptTokenMaker extends JavaScriptTokenMaker {

    private static TokenMap extraTokens;

    static {
        extraTokens = new TokenMap();

        int blueTokenType = TokenTypes.MARKUP_TAG_NAME;
        int purpleTokenType = TokenTypes.LITERAL_BOOLEAN;
        int functionTokenType = TokenTypes.FUNCTION;

        extraTokens.put("msg", blueTokenType);
        extraTokens.put("tmp", blueTokenType);
        extraTokens.put("message", blueTokenType);
        extraTokens.put("response", blueTokenType);
        extraTokens.put("responseStatus", blueTokenType);
        extraTokens.put("responseErrorMessage", blueTokenType);
        extraTokens.put("responseStatusMessage", blueTokenType);
        extraTokens.put("connectorMap", blueTokenType);
        extraTokens.put("channelMap", blueTokenType);
        extraTokens.put("sourceMap", blueTokenType);
        extraTokens.put("globalMap", blueTokenType);
        extraTokens.put("configurationMap", blueTokenType);
        extraTokens.put("globalChannelMap", blueTokenType);
        extraTokens.put("responseMap", blueTokenType);
        extraTokens.put("$co", blueTokenType);
        extraTokens.put("$c", blueTokenType);
        extraTokens.put("$s", blueTokenType);
        extraTokens.put("$gc", blueTokenType);
        extraTokens.put("$g", blueTokenType);
        extraTokens.put("$cfg", blueTokenType);
        extraTokens.put("$r", blueTokenType);
        extraTokens.put("logger", purpleTokenType);
        extraTokens.put("destinationSet", purpleTokenType);

        extraTokens.put("SMTPConnectionFactory", purpleTokenType);
        extraTokens.put("DatabaseConnectionFactory", purpleTokenType);
        extraTokens.put("SerializerFactory", purpleTokenType);
        extraTokens.put("FileUtil", purpleTokenType);
        extraTokens.put("DateUtil", purpleTokenType);

        extraTokens.put("executeCachedQuery", blueTokenType);
        extraTokens.put("createDatabaseConnection", blueTokenType);
        extraTokens.put("createSMTPConnection", blueTokenType);
        extraTokens.put("executeUpdate", blueTokenType);
        extraTokens.put("connectorMessage", blueTokenType);
        extraTokens.put("alerts", purpleTokenType);
        extraTokens.put("router", purpleTokenType);
        extraTokens.put("channelId", blueTokenType);
        extraTokens.put("channelName", blueTokenType);
        extraTokens.put("replacer", purpleTokenType);
        extraTokens.put("contextFactory", blueTokenType);

        extraTokens.put("createSegment", functionTokenType);
        extraTokens.put("createSegmentAfter", functionTokenType);
        extraTokens.put("addAttachment", functionTokenType);
        extraTokens.put("getAttachments", functionTokenType);

        extraTokens.put("ABS", purpleTokenType);
        extraTokens.put("ACC", purpleTokenType);
        extraTokens.put("ADD", purpleTokenType);
        extraTokens.put("AFF", purpleTokenType);
        extraTokens.put("AIG", purpleTokenType);
        extraTokens.put("AIL", purpleTokenType);
        extraTokens.put("AIP", purpleTokenType);
        extraTokens.put("AIS", purpleTokenType);
        extraTokens.put("AL1", purpleTokenType);
        extraTokens.put("APR", purpleTokenType);
        extraTokens.put("ARQ", purpleTokenType);
        extraTokens.put("AUT", purpleTokenType);
        extraTokens.put("BHS", purpleTokenType);
        extraTokens.put("BLC", purpleTokenType);
        extraTokens.put("BLG", purpleTokenType);
        extraTokens.put("BPO", purpleTokenType);
        extraTokens.put("BPX", purpleTokenType);
        extraTokens.put("BTS", purpleTokenType);
        extraTokens.put("BTX", purpleTokenType);
        extraTokens.put("CDM", purpleTokenType);
        extraTokens.put("CER", purpleTokenType);
        extraTokens.put("CM0", purpleTokenType);
        extraTokens.put("CM1", purpleTokenType);
        extraTokens.put("CM2", purpleTokenType);
        extraTokens.put("CNS", purpleTokenType);
        extraTokens.put("CON", purpleTokenType);
        extraTokens.put("CSP", purpleTokenType);
        extraTokens.put("CSR", purpleTokenType);
        extraTokens.put("CSS", purpleTokenType);
        extraTokens.put("CTD", purpleTokenType);
        extraTokens.put("CTI", purpleTokenType);
        extraTokens.put("DB1", purpleTokenType);
        extraTokens.put("DG1", purpleTokenType);
        extraTokens.put("DRG", purpleTokenType);
        extraTokens.put("DSC", purpleTokenType);
        extraTokens.put("DSP", purpleTokenType);
        extraTokens.put("ECD", purpleTokenType);
        extraTokens.put("ECR", purpleTokenType);
        extraTokens.put("ED ", purpleTokenType);
        extraTokens.put("EDU", purpleTokenType);
        extraTokens.put("EQL", purpleTokenType);
        extraTokens.put("EQP", purpleTokenType);
        extraTokens.put("EQU", purpleTokenType);
        extraTokens.put("ERQ", purpleTokenType);
        extraTokens.put("ERR", purpleTokenType);
        extraTokens.put("EVN", purpleTokenType);
        extraTokens.put("FAC", purpleTokenType);
        extraTokens.put("FHS", purpleTokenType);
        extraTokens.put("FT1", purpleTokenType);
        extraTokens.put("FTS", purpleTokenType);
        extraTokens.put("GOL", purpleTokenType);
        extraTokens.put("GP1", purpleTokenType);
        extraTokens.put("GP2", purpleTokenType);
        extraTokens.put("GT1", purpleTokenType);
        extraTokens.put("Hxx", purpleTokenType);
        extraTokens.put("IAM", purpleTokenType);
        extraTokens.put("IIM", purpleTokenType);
        extraTokens.put("IN1", purpleTokenType);
        extraTokens.put("IN2", purpleTokenType);
        extraTokens.put("IN3", purpleTokenType);
        extraTokens.put("INV", purpleTokenType);
        extraTokens.put("IPC", purpleTokenType);
        extraTokens.put("ISD", purpleTokenType);
        extraTokens.put("LAN", purpleTokenType);
        extraTokens.put("LCC", purpleTokenType);
        extraTokens.put("LCH", purpleTokenType);
        extraTokens.put("LDP", purpleTokenType);
        extraTokens.put("LOC", purpleTokenType);
        extraTokens.put("LRL", purpleTokenType);
        extraTokens.put("MFA", purpleTokenType);
        extraTokens.put("MFE", purpleTokenType);
        extraTokens.put("MFI", purpleTokenType);
        extraTokens.put("MRG", purpleTokenType);
        extraTokens.put("MSA", purpleTokenType);
        extraTokens.put("MSH", purpleTokenType);
        extraTokens.put("NCK", purpleTokenType);
        extraTokens.put("NDS", purpleTokenType);
        extraTokens.put("NK1", purpleTokenType);
        extraTokens.put("NPU", purpleTokenType);
        extraTokens.put("NSC", purpleTokenType);
        extraTokens.put("NST", purpleTokenType);
        extraTokens.put("NTE", purpleTokenType);
        extraTokens.put("OBR", purpleTokenType);
        extraTokens.put("OBX", purpleTokenType);
        extraTokens.put("ODS", purpleTokenType);
        extraTokens.put("ODT", purpleTokenType);
        extraTokens.put("OM1", purpleTokenType);
        extraTokens.put("OM2", purpleTokenType);
        extraTokens.put("OM3", purpleTokenType);
        extraTokens.put("OM4", purpleTokenType);
        extraTokens.put("OM5", purpleTokenType);
        extraTokens.put("OM6", purpleTokenType);
        extraTokens.put("OM7", purpleTokenType);
        extraTokens.put("ORC", purpleTokenType);
        extraTokens.put("ORG", purpleTokenType);
        extraTokens.put("OVR", purpleTokenType);
        extraTokens.put("PCR", purpleTokenType);
        extraTokens.put("PD1", purpleTokenType);
        extraTokens.put("PDA", purpleTokenType);
        extraTokens.put("PDC", purpleTokenType);
        extraTokens.put("PEO", purpleTokenType);
        extraTokens.put("PES", purpleTokenType);
        extraTokens.put("PID", purpleTokenType);
        extraTokens.put("PR1", purpleTokenType);
        extraTokens.put("PRA", purpleTokenType);
        extraTokens.put("PRB", purpleTokenType);
        extraTokens.put("PRC", purpleTokenType);
        extraTokens.put("PRD", purpleTokenType);
        extraTokens.put("PSH", purpleTokenType);
        extraTokens.put("PTH", purpleTokenType);
        extraTokens.put("PV1", purpleTokenType);
        extraTokens.put("PV2", purpleTokenType);
        extraTokens.put("QAK", purpleTokenType);
        extraTokens.put("QID", purpleTokenType);
        extraTokens.put("QPD", purpleTokenType);
        extraTokens.put("QRD", purpleTokenType);
        extraTokens.put("QRF", purpleTokenType);
        extraTokens.put("QRI", purpleTokenType);
        extraTokens.put("RCP", purpleTokenType);
        extraTokens.put("RDF", purpleTokenType);
        extraTokens.put("RDT", purpleTokenType);
        extraTokens.put("RF1", purpleTokenType);
        extraTokens.put("RGS", purpleTokenType);
        extraTokens.put("RMI", purpleTokenType);
        extraTokens.put("ROL", purpleTokenType);
        extraTokens.put("RQ1", purpleTokenType);
        extraTokens.put("RQD", purpleTokenType);
        extraTokens.put("RXA", purpleTokenType);
        extraTokens.put("RXC", purpleTokenType);
        extraTokens.put("RXD", purpleTokenType);
        extraTokens.put("RXE", purpleTokenType);
        extraTokens.put("RXG", purpleTokenType);
        extraTokens.put("RXO", purpleTokenType);
        extraTokens.put("RXR", purpleTokenType);
        extraTokens.put("SAC", purpleTokenType);
        extraTokens.put("SCH", purpleTokenType);
        extraTokens.put("SFT", purpleTokenType);
        extraTokens.put("SID", purpleTokenType);
        extraTokens.put("SPM", purpleTokenType);
        extraTokens.put("SPR", purpleTokenType);
        extraTokens.put("STF", purpleTokenType);
        extraTokens.put("TCC", purpleTokenType);
        extraTokens.put("TCD", purpleTokenType);
        extraTokens.put("TQ1", purpleTokenType);
        extraTokens.put("TQ2", purpleTokenType);
        extraTokens.put("TXA", purpleTokenType);
        extraTokens.put("UB1", purpleTokenType);
        extraTokens.put("UB2", purpleTokenType);
        extraTokens.put("URD", purpleTokenType);
        extraTokens.put("URS", purpleTokenType);
        extraTokens.put("VAR", purpleTokenType);
        extraTokens.put("VTQ", purpleTokenType);
        extraTokens.put("ZL7", purpleTokenType);
        extraTokens.put("ZCS", purpleTokenType);
        extraTokens.put("ZFT", purpleTokenType);
    }

    @Override
    public void addToken(char[] array, int start, int end, int tokenType, int startOffset, boolean hyperlink) {
        if (tokenType == TokenTypes.IDENTIFIER) {
            int newType = extraTokens.get(array, start, end);
            if (newType > -1) {
                tokenType = newType;
            }
        }
        super.addToken(array, start, end, tokenType, startOffset, hyperlink);
    }

    @Override
    public Action getInsertBreakAction() {
        return null;
    }
}