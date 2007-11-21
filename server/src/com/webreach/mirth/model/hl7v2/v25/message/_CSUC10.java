package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _CSUC10 extends Message{	
	public _CSUC10(){
		segments = new Class[]{_MSH.class, _SFT.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _CSR.class, _CSP.class, _CSS.class, _ORC.class, _OBR.class, _TQ1.class, _TQ2.class, _OBX.class, _ORC.class, _RXA.class, _RXR.class};
		repeats = new int[]{0, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0};
		required = new boolean[]{true, false, true, false, false, true, false, true, false, false, false, true, true, false, true, false, true, true};
		groups = new int[][]{{6, 7, 0, 0}, {13, 14, 0, 1}, {11, 15, 1, 1}, {17, 18, 1, 1}, {16, 18, 1, 1}, {10, 18, 1, 1}, {9, 18, 1, 1}, {3, 18, 1, 1}}; 
		description = "Patient Completes the Clinical Trial";
		name = "CSUC10";
	}
}
