package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _CSUC12 extends Message{	
	public _CSUC12(){
		segments = new Class[]{_MSH.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _CSR.class, _CSP.class, _CSS.class, _ORC.class, _OBR.class, _OBX.class, _ORC.class, _RXA.class, _RXR.class};
		repeats = new int[]{0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0};
		required = new boolean[]{true, true, false, false, true, false, true, false, false, false, true, true, false, true, true};
		groups = new int[][]{{5, 6, 0, 0}, {10, 12, 0, 1}, {14, 15, 1, 1}, {13, 15, 1, 1}, {9, 15, 1, 1}, {8, 15, 1, 1}, {2, 15, 1, 1}}; 
		description = "Update/Correction of Patient Order/Result Information";
		name = "CSUC12";
	}
}
