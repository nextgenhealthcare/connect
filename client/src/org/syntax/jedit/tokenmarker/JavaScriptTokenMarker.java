/*
 * JavaScriptTokenMarker.java - JavaScript token marker Copyright (C) 1999 Slava Pestov
 * 
 * You may use and modify this package for any purpose. Redistribution is permitted, in both source
 * and binary form, provided that this notice remains intact in all source distributions of this
 * package.
 */

package org.syntax.jedit.tokenmarker;

import org.syntax.jedit.KeywordMap;

/**
 * JavaScript token marker.
 * 
 * @author Slava Pestov
 * @version $Id: JavaScriptTokenMarker.java,v 1.3 1999/12/13 03:40:29 sp Exp $
 */
public class JavaScriptTokenMarker extends CTokenMarker {
    public JavaScriptTokenMarker() {
        super(false, getKeywords());
    }

    public static KeywordMap getKeywords() {
        if (javaScriptKeywords == null) {
            javaScriptKeywords = new KeywordMap(false);
            javaScriptKeywords.add("function", Token.KEYWORD3);
            javaScriptKeywords.add("get", Token.KEYWORD3);
            javaScriptKeywords.add("var", Token.KEYWORD1);
            javaScriptKeywords.add("else", Token.KEYWORD1);
            javaScriptKeywords.add("for", Token.KEYWORD1);
            javaScriptKeywords.add("each", Token.KEYWORD1);
            javaScriptKeywords.add("if", Token.KEYWORD1);
            javaScriptKeywords.add("in", Token.KEYWORD1);
            javaScriptKeywords.add("new", Token.KEYWORD1);
            javaScriptKeywords.add("return", Token.KEYWORD1);
            javaScriptKeywords.add("while", Token.KEYWORD1);
            javaScriptKeywords.add("with", Token.KEYWORD1);
            javaScriptKeywords.add("break", Token.KEYWORD1);
            javaScriptKeywords.add("case", Token.KEYWORD1);
            javaScriptKeywords.add("continue", Token.KEYWORD1);
            javaScriptKeywords.add("default", Token.KEYWORD1);
            javaScriptKeywords.add("false", Token.LABEL);
            javaScriptKeywords.add("this", Token.LABEL);
            javaScriptKeywords.add("true", Token.LABEL);
            // Mirth specific
            // TODO: Figure out a consistent coloring scheme
            javaScriptKeywords.add("msg", Token.KEYWORD1);
            javaScriptKeywords.add("tmp", Token.KEYWORD1);
            javaScriptKeywords.add("message", Token.KEYWORD1);
            javaScriptKeywords.add("response", Token.KEYWORD1);
            javaScriptKeywords.add("responseStatus", Token.KEYWORD1);
            javaScriptKeywords.add("responseErrorMessage", Token.KEYWORD1);
            javaScriptKeywords.add("responseStatusMessage", Token.KEYWORD1);
            javaScriptKeywords.add("version", Token.KEYWORD1);
            javaScriptKeywords.add("connectorMap", Token.KEYWORD1);
            javaScriptKeywords.add("channelMap", Token.KEYWORD1);
            javaScriptKeywords.add("sourceMap", Token.KEYWORD1);
            javaScriptKeywords.add("globalMap", Token.KEYWORD1);
            javaScriptKeywords.add("configurationMap", Token.KEYWORD1);
            javaScriptKeywords.add("globalChannelMap", Token.KEYWORD1);
            javaScriptKeywords.add("responseMap", Token.KEYWORD1);
            javaScriptKeywords.add("$co", Token.KEYWORD1);
            javaScriptKeywords.add("$c", Token.KEYWORD1);
            javaScriptKeywords.add("$s", Token.KEYWORD1);
            javaScriptKeywords.add("$gc", Token.KEYWORD1);
            javaScriptKeywords.add("$g", Token.KEYWORD1);
            javaScriptKeywords.add("$cfg", Token.KEYWORD1);
            javaScriptKeywords.add("$r", Token.KEYWORD1);
            javaScriptKeywords.add("logger", Token.KEYWORD3);
            javaScriptKeywords.add("message", Token.KEYWORD3);
            javaScriptKeywords.add("SMTPConnectionFactory", Token.KEYWORD3);
            javaScriptKeywords.add("DatabaseConnectionFactory", Token.KEYWORD3);
            javaScriptKeywords.add("executeCachedQuery", Token.KEYWORD1);
            javaScriptKeywords.add("createDatabaseConnection", Token.KEYWORD1);
            javaScriptKeywords.add("createSMTPConnection", Token.KEYWORD1);
            javaScriptKeywords.add("executeUpdate", Token.KEYWORD1);
            javaScriptKeywords.add("SerializerFactory", Token.KEYWORD3);
            javaScriptKeywords.add("connectorMessage", Token.KEYWORD1);
            javaScriptKeywords.add("alerts", Token.KEYWORD3);
            javaScriptKeywords.add("router", Token.KEYWORD3);
            javaScriptKeywords.add("channelId", Token.KEYWORD1);
            javaScriptKeywords.add("replacer", Token.KEYWORD3);
            javaScriptKeywords.add("FileUtil", Token.KEYWORD3);
            javaScriptKeywords.add("DateUtil", Token.KEYWORD3);

            javaScriptKeywords.add("createSegment", Token.KEYWORD3);
            javaScriptKeywords.add("createSegmentAfter", Token.KEYWORD3);
            javaScriptKeywords.add("addAttachment", Token.KEYWORD3);
            javaScriptKeywords.add("getAttachments", Token.KEYWORD3);
            javaScriptKeywords.add("ABS", Token.KEYWORD3);

            javaScriptKeywords.add("ACC", Token.KEYWORD3);

            javaScriptKeywords.add("ADD", Token.KEYWORD3);

            javaScriptKeywords.add("AFF", Token.KEYWORD3);

            javaScriptKeywords.add("AIG", Token.KEYWORD3);

            javaScriptKeywords.add("AIL", Token.KEYWORD3);

            javaScriptKeywords.add("AIP", Token.KEYWORD3);

            javaScriptKeywords.add("AIS", Token.KEYWORD3);

            javaScriptKeywords.add("AL1", Token.KEYWORD3);

            javaScriptKeywords.add("APR", Token.KEYWORD3);

            javaScriptKeywords.add("ARQ", Token.KEYWORD3);

            javaScriptKeywords.add("AUT", Token.KEYWORD3);

            javaScriptKeywords.add("BHS", Token.KEYWORD3);

            javaScriptKeywords.add("BLC", Token.KEYWORD3);

            javaScriptKeywords.add("BLG", Token.KEYWORD3);

            javaScriptKeywords.add("BPO", Token.KEYWORD3);

            javaScriptKeywords.add("BPX", Token.KEYWORD3);

            javaScriptKeywords.add("BTS", Token.KEYWORD3);

            javaScriptKeywords.add("BTX", Token.KEYWORD3);

            javaScriptKeywords.add("CDM", Token.KEYWORD3);

            javaScriptKeywords.add("CER", Token.KEYWORD3);

            javaScriptKeywords.add("CM0", Token.KEYWORD3);

            javaScriptKeywords.add("CM1", Token.KEYWORD3);

            javaScriptKeywords.add("CM2", Token.KEYWORD3);

            javaScriptKeywords.add("CNS", Token.KEYWORD3);

            javaScriptKeywords.add("CON", Token.KEYWORD3);

            javaScriptKeywords.add("CSP", Token.KEYWORD3);

            javaScriptKeywords.add("CSR", Token.KEYWORD3);

            javaScriptKeywords.add("CSS", Token.KEYWORD3);

            javaScriptKeywords.add("CTD", Token.KEYWORD3);

            javaScriptKeywords.add("CTI", Token.KEYWORD3);

            javaScriptKeywords.add("DB1", Token.KEYWORD3);

            javaScriptKeywords.add("DG1", Token.KEYWORD3);

            javaScriptKeywords.add("DRG", Token.KEYWORD3);

            javaScriptKeywords.add("DSC", Token.KEYWORD3);

            javaScriptKeywords.add("DSP", Token.KEYWORD3);

            javaScriptKeywords.add("ECD", Token.KEYWORD3);

            javaScriptKeywords.add("ECR", Token.KEYWORD3);

            javaScriptKeywords.add("ED ", Token.KEYWORD3);

            javaScriptKeywords.add("EDU", Token.KEYWORD3);

            javaScriptKeywords.add("EQL", Token.KEYWORD3);

            javaScriptKeywords.add("EQP", Token.KEYWORD3);

            javaScriptKeywords.add("EQU", Token.KEYWORD3);

            javaScriptKeywords.add("ERQ", Token.KEYWORD3);

            javaScriptKeywords.add("ERR", Token.KEYWORD3);

            javaScriptKeywords.add("EVN", Token.KEYWORD3);

            javaScriptKeywords.add("FAC", Token.KEYWORD3);

            javaScriptKeywords.add("FHS", Token.KEYWORD3);

            javaScriptKeywords.add("FT1", Token.KEYWORD3);

            javaScriptKeywords.add("FTS", Token.KEYWORD3);

            javaScriptKeywords.add("GOL", Token.KEYWORD3);

            javaScriptKeywords.add("GP1", Token.KEYWORD3);

            javaScriptKeywords.add("GP2", Token.KEYWORD3);

            javaScriptKeywords.add("GT1", Token.KEYWORD3);

            javaScriptKeywords.add("Hxx", Token.KEYWORD3);

            javaScriptKeywords.add("IAM", Token.KEYWORD3);

            javaScriptKeywords.add("IIM", Token.KEYWORD3);

            javaScriptKeywords.add("IN1", Token.KEYWORD3);

            javaScriptKeywords.add("IN2", Token.KEYWORD3);

            javaScriptKeywords.add("IN3", Token.KEYWORD3);

            javaScriptKeywords.add("INV", Token.KEYWORD3);

            javaScriptKeywords.add("IPC", Token.KEYWORD3);

            javaScriptKeywords.add("ISD", Token.KEYWORD3);

            javaScriptKeywords.add("LAN", Token.KEYWORD3);

            javaScriptKeywords.add("LCC", Token.KEYWORD3);

            javaScriptKeywords.add("LCH", Token.KEYWORD3);

            javaScriptKeywords.add("LDP", Token.KEYWORD3);

            javaScriptKeywords.add("LOC", Token.KEYWORD3);

            javaScriptKeywords.add("LRL", Token.KEYWORD3);

            javaScriptKeywords.add("MFA", Token.KEYWORD3);

            javaScriptKeywords.add("MFE", Token.KEYWORD3);

            javaScriptKeywords.add("MFI", Token.KEYWORD3);

            javaScriptKeywords.add("MRG", Token.KEYWORD3);

            javaScriptKeywords.add("MSA", Token.KEYWORD3);

            javaScriptKeywords.add("MSH", Token.KEYWORD3);

            javaScriptKeywords.add("NCK", Token.KEYWORD3);

            javaScriptKeywords.add("NDS", Token.KEYWORD3);

            javaScriptKeywords.add("NK1", Token.KEYWORD3);

            javaScriptKeywords.add("NPU", Token.KEYWORD3);

            javaScriptKeywords.add("NSC", Token.KEYWORD3);

            javaScriptKeywords.add("NST", Token.KEYWORD3);

            javaScriptKeywords.add("NTE", Token.KEYWORD3);

            javaScriptKeywords.add("OBR", Token.KEYWORD3);

            javaScriptKeywords.add("OBX", Token.KEYWORD3);

            javaScriptKeywords.add("ODS", Token.KEYWORD3);

            javaScriptKeywords.add("ODT", Token.KEYWORD3);

            javaScriptKeywords.add("OM1", Token.KEYWORD3);

            javaScriptKeywords.add("OM2", Token.KEYWORD3);

            javaScriptKeywords.add("OM3", Token.KEYWORD3);

            javaScriptKeywords.add("OM4", Token.KEYWORD3);

            javaScriptKeywords.add("OM5", Token.KEYWORD3);

            javaScriptKeywords.add("OM6", Token.KEYWORD3);

            javaScriptKeywords.add("OM7", Token.KEYWORD3);

            javaScriptKeywords.add("ORC", Token.KEYWORD3);

            javaScriptKeywords.add("ORG", Token.KEYWORD3);

            javaScriptKeywords.add("OVR", Token.KEYWORD3);

            javaScriptKeywords.add("PCR", Token.KEYWORD3);

            javaScriptKeywords.add("PD1", Token.KEYWORD3);

            javaScriptKeywords.add("PDA", Token.KEYWORD3);

            javaScriptKeywords.add("PDC", Token.KEYWORD3);

            javaScriptKeywords.add("PEO", Token.KEYWORD3);

            javaScriptKeywords.add("PES", Token.KEYWORD3);

            javaScriptKeywords.add("PID", Token.KEYWORD3);

            javaScriptKeywords.add("PR1", Token.KEYWORD3);

            javaScriptKeywords.add("PRA", Token.KEYWORD3);

            javaScriptKeywords.add("PRB", Token.KEYWORD3);

            javaScriptKeywords.add("PRC", Token.KEYWORD3);

            javaScriptKeywords.add("PRD", Token.KEYWORD3);

            javaScriptKeywords.add("PSH", Token.KEYWORD3);

            javaScriptKeywords.add("PTH", Token.KEYWORD3);

            javaScriptKeywords.add("PV1", Token.KEYWORD3);

            javaScriptKeywords.add("PV2", Token.KEYWORD3);

            javaScriptKeywords.add("QAK", Token.KEYWORD3);

            javaScriptKeywords.add("QID", Token.KEYWORD3);

            javaScriptKeywords.add("QPD", Token.KEYWORD3);

            javaScriptKeywords.add("QRD", Token.KEYWORD3);

            javaScriptKeywords.add("QRF", Token.KEYWORD3);

            javaScriptKeywords.add("QRI", Token.KEYWORD3);

            javaScriptKeywords.add("RCP", Token.KEYWORD3);

            javaScriptKeywords.add("RDF", Token.KEYWORD3);

            javaScriptKeywords.add("RDT", Token.KEYWORD3);

            javaScriptKeywords.add("RF1", Token.KEYWORD3);

            javaScriptKeywords.add("RGS", Token.KEYWORD3);

            javaScriptKeywords.add("RMI", Token.KEYWORD3);

            javaScriptKeywords.add("ROL", Token.KEYWORD3);

            javaScriptKeywords.add("RQ1", Token.KEYWORD3);

            javaScriptKeywords.add("RQD", Token.KEYWORD3);

            javaScriptKeywords.add("RXA", Token.KEYWORD3);

            javaScriptKeywords.add("RXC", Token.KEYWORD3);

            javaScriptKeywords.add("RXD", Token.KEYWORD3);

            javaScriptKeywords.add("RXE", Token.KEYWORD3);

            javaScriptKeywords.add("RXG", Token.KEYWORD3);

            javaScriptKeywords.add("RXO", Token.KEYWORD3);

            javaScriptKeywords.add("RXR", Token.KEYWORD3);

            javaScriptKeywords.add("SAC", Token.KEYWORD3);

            javaScriptKeywords.add("SCH", Token.KEYWORD3);

            javaScriptKeywords.add("SFT", Token.KEYWORD3);

            javaScriptKeywords.add("SID", Token.KEYWORD3);

            javaScriptKeywords.add("SPM", Token.KEYWORD3);

            javaScriptKeywords.add("SPR", Token.KEYWORD3);

            javaScriptKeywords.add("STF", Token.KEYWORD3);

            javaScriptKeywords.add("TCC", Token.KEYWORD3);

            javaScriptKeywords.add("TCD", Token.KEYWORD3);

            javaScriptKeywords.add("TQ1", Token.KEYWORD3);

            javaScriptKeywords.add("TQ2", Token.KEYWORD3);

            javaScriptKeywords.add("TXA", Token.KEYWORD3);

            javaScriptKeywords.add("UB1", Token.KEYWORD3);

            javaScriptKeywords.add("UB2", Token.KEYWORD3);

            javaScriptKeywords.add("URD", Token.KEYWORD3);

            javaScriptKeywords.add("URS", Token.KEYWORD3);

            javaScriptKeywords.add("VAR", Token.KEYWORD3);

            javaScriptKeywords.add("VTQ", Token.KEYWORD3);

            javaScriptKeywords.add("ZL7", Token.KEYWORD3);
            javaScriptKeywords.add("ZCS", Token.KEYWORD3);
            javaScriptKeywords.add("ZFT", Token.KEYWORD3);
            /*
             * javaScriptKeywords.add("send",Token.KEYWORD1);
             * javaScriptKeywords.add("info",Token.KEYWORD1);
             * javaScriptKeywords.add("error",Token.KEYWORD1);
             * javaScriptKeywords.add("close",Token.KEYWORD1);
             * javaScriptKeywords.add("next",Token.KEYWORD1);
             * javaScriptKeywords.add("getInt",Token.KEYWORD1);
             * javaScriptKeywords.add("getString",Token.KEYWORD1);
             * javaScriptKeywords.add("length",Token.KEYWORD1);
             */
            // TODO: Should functions have coloring?
            // TODO: Finish adding variables from Mirth
        }
        return javaScriptKeywords;
    }

    // private members
    private static KeywordMap javaScriptKeywords;
}
