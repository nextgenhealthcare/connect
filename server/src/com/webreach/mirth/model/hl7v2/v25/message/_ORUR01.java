package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORUR01 extends Message{	
	public _ORUR01(){
		segments = new Class[]{_MSH.class, _SFT.class, _PID.class, _PD1.class, _NTE.class, _NK1.class, _PV1.class, _PV2.class, _ORC.class, _OBR.class, _NTE.class, _TQ1.class, _TQ2.class, _CTD.class, _OBX.class, _NTE.class, _FT1.class, _CTI.class, _SPM.class, _OBX.class, _DSC.class};
		repeats = new int[]{0, -1, 0, 0, -1, -1, 0, 0, 0, 0, -1, 0, -1, 0, 0, -1, -1, -1, 0, -1, 0};
		required = new boolean[]{true, false, true, false, false, false, true, false, false, true, false, true, false, false, true, false, false, false, true, false, false};
		groups = new int[][]{{7, 8, 0, 0}, {3, 8, 0, 0}, {12, 13, 0, 1}, {15, 16, 0, 1}, {19, 20, 0, 1}, {9, 20, 1, 1}, {3, 20, 1, 1}}; 
		description = "ORU Subscription (response)";
		name = "ORUR01";
	}
}
