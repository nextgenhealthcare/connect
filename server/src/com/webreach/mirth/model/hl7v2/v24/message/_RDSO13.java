package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RDSO13 extends Message{	
	public _RDSO13(){
		segments = new Class[]{_MSH.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _AL1.class, _PV1.class, _PV2.class, _ORC.class, _RXO.class, _NTE.class, _RXR.class, _RXC.class, _NTE.class, _RXE.class, _RXR.class, _RXC.class, _RXD.class, _RXR.class, _RXC.class, _OBX.class, _NTE.class, _FT1.class};
		repeats = new int[]{0, -1, 0, 0, -1, -1, 0, 0, 0, 0, -1, -1, -1, -1, 0, -1, -1, 0, -1, -1, 0, -1, -1};
		required = new boolean[]{true, false, true, false, false, false, true, false, true, true, true, true, true, false, true, true, false, true, true, false, true, false, false};
		groups = new int[][]{{7, 8, 0, 0}, {3, 8, 0, 0}, {13, 14, 0, 0}, {11, 14, 0, 0}, {10, 14, 0, 0}, {15, 17, 0, 0}, {21, 22, 0, 1}, {9, 23, 1, 1}}; 
		description = "Pharmacy/Treatment Dispense";
		name = "RDSO13";
	}
}
