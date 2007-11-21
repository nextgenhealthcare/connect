package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PEXP08 extends Message{	
	public _PEXP08(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _PES.class, _PEO.class, _PCR.class, _RXE.class, _TQ1.class, _TQ2.class, _RXR.class, _RXA.class, _RXR.class, _PRB.class, _OBX.class, _NTE.class, _NK1.class, _RXE.class, _TQ1.class, _TQ2.class, _RXR.class, _RXA.class, _RXR.class, _PRB.class, _OBX.class, _CSR.class, _CSP.class};
		repeats = new int[]{0, -1, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, -1, -1, -1, 0, 0, 0, -1, -1, 0, 0, -1, -1, 0, -1};
		required = new boolean[]{true, false, true, true, false, false, true, false, true, true, true, true, true, false, false, true, false, false, false, false, true, true, true, false, false, true, false, false, false, true, false};
		groups = new int[][]{{7, 8, 0, 0}, {13, 14, 1, 1}, {12, 15, 0, 0}, {16, 17, 0, 1}, {23, 24, 1, 1}, {22, 25, 0, 0}, {26, 27, 0, 1}, {21, 29, 0, 0}, {30, 31, 0, 1}, {11, 31, 1, 1}, {10, 31, 1, 1}, {9, 31, 1, 1}}; 
		description = "Unsolicited Update Individual Product Experience Report";
		name = "PEXP08";
	}
}
