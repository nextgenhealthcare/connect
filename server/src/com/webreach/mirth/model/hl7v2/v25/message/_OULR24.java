package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OULR24 extends Message{	
	public _OULR24(){
		segments = new Class[]{_MSH.class, _SFT.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _OBR.class, _ORC.class, _NTE.class, _TQ1.class, _TQ2.class, _SPM.class, _OBX.class, _SAC.class, _INV.class, _OBX.class, _TCD.class, _SID.class, _NTE.class, _CTI.class, _DSC.class};
		repeats = new int[]{0, -1, 0, 0, 0, -1, 0, 0, 0, 0, -1, 0, -1, 0, -1, 0, 0, 0, 0, -1, -1, -1, 0};
		required = new boolean[]{true, false, false, true, false, false, true, false, true, false, false, true, false, true, false, true, false, true, false, false, false, false, false};
		groups = new int[][]{{4, 6, 0, 0}, {7, 8, 0, 0}, {12, 13, 0, 1}, {16, 17, 0, 1}, {14, 17, 0, 1}, {18, 21, 0, 1}, {9, 22, 1, 1}}; 
		description = "Unsolicited Order Oriented Observation Message";
		name = "OULR24";
	}
}
