package org.syntax.jedit.tokenmarker;

import org.syntax.jedit.KeywordMap;

/**
 * HL7 token marker.
 * 
 * @author Chris Lang
 */
public class HL7TokenMarker extends CTokenMarker {
	public HL7TokenMarker() {
		super(true, getKeywords());
	}

	public static KeywordMap getKeywords() {
		if (ccKeywords == null) {
			ccKeywords = new KeywordMap(false);

			ccKeywords.add("ABS", Token.KEYWORD3);

			ccKeywords.add("ACC", Token.KEYWORD3);

			ccKeywords.add("ADD", Token.KEYWORD3);

			ccKeywords.add("AFF", Token.KEYWORD3);

			ccKeywords.add("AIG", Token.KEYWORD3);

			ccKeywords.add("AIL", Token.KEYWORD3);

			ccKeywords.add("AIP", Token.KEYWORD3);

			ccKeywords.add("AIS", Token.KEYWORD3);

			ccKeywords.add("AL1", Token.KEYWORD3);

			ccKeywords.add("APR", Token.KEYWORD3);

			ccKeywords.add("ARQ", Token.KEYWORD3);

			ccKeywords.add("AUT", Token.KEYWORD3);

			ccKeywords.add("BHS", Token.KEYWORD3);

			ccKeywords.add("BLC", Token.KEYWORD3);

			ccKeywords.add("BLG", Token.KEYWORD3);

			ccKeywords.add("BPO", Token.KEYWORD3);

			ccKeywords.add("BPX", Token.KEYWORD3);

			ccKeywords.add("BTS", Token.KEYWORD3);

			ccKeywords.add("BTX", Token.KEYWORD3);

			ccKeywords.add("CDM", Token.KEYWORD3);

			ccKeywords.add("CER", Token.KEYWORD3);

			ccKeywords.add("CM0", Token.KEYWORD3);

			ccKeywords.add("CM1", Token.KEYWORD3);

			ccKeywords.add("CM2", Token.KEYWORD3);

			ccKeywords.add("CNS", Token.KEYWORD3);

			ccKeywords.add("CON", Token.KEYWORD3);

			ccKeywords.add("CSP", Token.KEYWORD3);

			ccKeywords.add("CSR", Token.KEYWORD3);

			ccKeywords.add("CSS", Token.KEYWORD3);

			ccKeywords.add("CTD", Token.KEYWORD3);

			ccKeywords.add("CTI", Token.KEYWORD3);

			ccKeywords.add("DB1", Token.KEYWORD3);

			ccKeywords.add("DG1", Token.KEYWORD3);

			ccKeywords.add("DRG", Token.KEYWORD3);

			ccKeywords.add("DSC", Token.KEYWORD3);

			ccKeywords.add("DSP", Token.KEYWORD3);

			ccKeywords.add("ECD", Token.KEYWORD3);

			ccKeywords.add("ECR", Token.KEYWORD3);

			ccKeywords.add("ED ", Token.KEYWORD3);

			ccKeywords.add("EDU", Token.KEYWORD3);

			ccKeywords.add("EQL", Token.KEYWORD3);

			ccKeywords.add("EQP", Token.KEYWORD3);

			ccKeywords.add("EQU", Token.KEYWORD3);

			ccKeywords.add("ERQ", Token.KEYWORD3);

			ccKeywords.add("ERR", Token.KEYWORD3);

			ccKeywords.add("EVN", Token.KEYWORD3);

			ccKeywords.add("FAC", Token.KEYWORD3);

			ccKeywords.add("FHS", Token.KEYWORD3);

			ccKeywords.add("FT1", Token.KEYWORD3);

			ccKeywords.add("FTS", Token.KEYWORD3);

			ccKeywords.add("GOL", Token.KEYWORD3);

			ccKeywords.add("GP1", Token.KEYWORD3);

			ccKeywords.add("GP2", Token.KEYWORD3);

			ccKeywords.add("GT1", Token.KEYWORD3);

			ccKeywords.add("Hxx", Token.KEYWORD3);

			ccKeywords.add("IAM", Token.KEYWORD3);

			ccKeywords.add("IIM", Token.KEYWORD3);

			ccKeywords.add("IN1", Token.KEYWORD3);

			ccKeywords.add("IN2", Token.KEYWORD3);

			ccKeywords.add("IN3", Token.KEYWORD3);

			ccKeywords.add("INV", Token.KEYWORD3);

			ccKeywords.add("IPC", Token.KEYWORD3);

			ccKeywords.add("ISD", Token.KEYWORD3);

			ccKeywords.add("LAN", Token.KEYWORD3);

			ccKeywords.add("LCC", Token.KEYWORD3);

			ccKeywords.add("LCH", Token.KEYWORD3);

			ccKeywords.add("LDP", Token.KEYWORD3);

			ccKeywords.add("LOC", Token.KEYWORD3);

			ccKeywords.add("LRL", Token.KEYWORD3);

			ccKeywords.add("MFA", Token.KEYWORD3);

			ccKeywords.add("MFE", Token.KEYWORD3);

			ccKeywords.add("MFI", Token.KEYWORD3);

			ccKeywords.add("MRG", Token.KEYWORD3);

			ccKeywords.add("MSA", Token.KEYWORD3);

			ccKeywords.add("MSH", Token.KEYWORD3);

			ccKeywords.add("NCK", Token.KEYWORD3);

			ccKeywords.add("NDS", Token.KEYWORD3);

			ccKeywords.add("NK1", Token.KEYWORD3);

			ccKeywords.add("NPU", Token.KEYWORD3);

			ccKeywords.add("NSC", Token.KEYWORD3);

			ccKeywords.add("NST", Token.KEYWORD3);

			ccKeywords.add("NTE", Token.KEYWORD3);

			ccKeywords.add("OBR", Token.KEYWORD3);

			ccKeywords.add("OBX", Token.KEYWORD3);

			ccKeywords.add("ODS", Token.KEYWORD3);

			ccKeywords.add("ODT", Token.KEYWORD3);

			ccKeywords.add("OM1", Token.KEYWORD3);

			ccKeywords.add("OM2", Token.KEYWORD3);

			ccKeywords.add("OM3", Token.KEYWORD3);

			ccKeywords.add("OM4", Token.KEYWORD3);

			ccKeywords.add("OM5", Token.KEYWORD3);

			ccKeywords.add("OM6", Token.KEYWORD3);

			ccKeywords.add("OM7", Token.KEYWORD3);

			ccKeywords.add("ORC", Token.KEYWORD3);

			ccKeywords.add("ORG", Token.KEYWORD3);

			ccKeywords.add("OVR", Token.KEYWORD3);

			ccKeywords.add("PCR", Token.KEYWORD3);

			ccKeywords.add("PD1", Token.KEYWORD3);

			ccKeywords.add("PDA", Token.KEYWORD3);

			ccKeywords.add("PDC", Token.KEYWORD3);

			ccKeywords.add("PEO", Token.KEYWORD3);

			ccKeywords.add("PES", Token.KEYWORD3);

			ccKeywords.add("PID", Token.KEYWORD3);

			ccKeywords.add("PR1", Token.KEYWORD3);

			ccKeywords.add("PRA", Token.KEYWORD3);

			ccKeywords.add("PRB", Token.KEYWORD3);

			ccKeywords.add("PRC", Token.KEYWORD3);

			ccKeywords.add("PRD", Token.KEYWORD3);

			ccKeywords.add("PSH", Token.KEYWORD3);

			ccKeywords.add("PTH", Token.KEYWORD3);

			ccKeywords.add("PV1", Token.KEYWORD3);

			ccKeywords.add("PV2", Token.KEYWORD3);

			ccKeywords.add("QAK", Token.KEYWORD3);

			ccKeywords.add("QID", Token.KEYWORD3);

			ccKeywords.add("QPD", Token.KEYWORD3);

			ccKeywords.add("QRD", Token.KEYWORD3);

			ccKeywords.add("QRF", Token.KEYWORD3);

			ccKeywords.add("QRI", Token.KEYWORD3);

			ccKeywords.add("RCP", Token.KEYWORD3);

			ccKeywords.add("RDF", Token.KEYWORD3);

			ccKeywords.add("RDT", Token.KEYWORD3);

			ccKeywords.add("RF1", Token.KEYWORD3);

			ccKeywords.add("RGS", Token.KEYWORD3);

			ccKeywords.add("RMI", Token.KEYWORD3);

			ccKeywords.add("ROL", Token.KEYWORD3);

			ccKeywords.add("RQ1", Token.KEYWORD3);

			ccKeywords.add("RQD", Token.KEYWORD3);

			ccKeywords.add("RXA", Token.KEYWORD3);

			ccKeywords.add("RXC", Token.KEYWORD3);

			ccKeywords.add("RXD", Token.KEYWORD3);

			ccKeywords.add("RXE", Token.KEYWORD3);

			ccKeywords.add("RXG", Token.KEYWORD3);

			ccKeywords.add("RXO", Token.KEYWORD3);

			ccKeywords.add("RXR", Token.KEYWORD3);

			ccKeywords.add("SAC", Token.KEYWORD3);

			ccKeywords.add("SCH", Token.KEYWORD3);

			ccKeywords.add("SFT", Token.KEYWORD3);

			ccKeywords.add("SID", Token.KEYWORD3);

			ccKeywords.add("SPM", Token.KEYWORD3);

			ccKeywords.add("SPR", Token.KEYWORD3);

			ccKeywords.add("STF", Token.KEYWORD3);

			ccKeywords.add("TCC", Token.KEYWORD3);

			ccKeywords.add("TCD", Token.KEYWORD3);

			ccKeywords.add("TQ1", Token.KEYWORD3);

			ccKeywords.add("TQ2", Token.KEYWORD3);

			ccKeywords.add("TXA", Token.KEYWORD3);

			ccKeywords.add("UB1", Token.KEYWORD3);

			ccKeywords.add("UB2", Token.KEYWORD3);

			ccKeywords.add("URD", Token.KEYWORD3);

			ccKeywords.add("URS", Token.KEYWORD3);
			
			ccKeywords.add("VAR", Token.KEYWORD3);
			
			ccKeywords.add("VTQ", Token.KEYWORD3);
			
			ccKeywords.add("ZL7", Token.KEYWORD3);
			
			ccKeywords.add("ZCS", Token.KEYWORD3);
			
			ccKeywords.add("ZFT", Token.KEYWORD3);
		}
		return ccKeywords;
	}

	// private members
	private static KeywordMap ccKeywords;
}
