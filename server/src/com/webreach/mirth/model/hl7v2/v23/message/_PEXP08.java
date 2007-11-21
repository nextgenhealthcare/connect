package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PEXP08 extends Message{	
	public _PEXP08(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _PES.class, _PEO.class, _PCR.class, _RXE.class, _RXR.class, _RXA.class, _RXR.class, _PRB.class, _OBX.class, _NTE.class, _NK1.class, _RXE.class, _RXR.class, _RXA.class, _RXR.class, _PRB.class, _OBX.class, _CSR.class, _CSP.class};
		repeats = new int[]{0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, -1, 0, 0, -1, -1, -1, 0, 0, -1, 0, 0, -1, -1, 0, -1};
		required = new boolean[]{true, true, true, false, false, true, false, true, true, true, true, false, true, false, false, false, false, true, true, false, true, false, false, false, true, false};
		groups = new int[][]{{6, 7, 0, 0}, {11, 12, 0, 0}, {13, 14, 0, 1}, {19, 20, 0, 0}, {21, 22, 0, 1}, {18, 24, 0, 0}, {25, 26, 0, 1}, {10, 26, 1, 1}, {9, 26, 1, 1}, {8, 26, 1, 1}}; 
		description = "Unsolicited Update Individual Product Experience Report";
		name = "PEXP08";
	}
}
