package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OULR21 extends Message{	
	public _OULR21(){
		segments = new Class[]{_MSH.class, _SFT.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _SAC.class, _SID.class, _ORC.class, _OBR.class, _NTE.class, _TQ1.class, _TQ2.class, _OBX.class, _TCD.class, _SID.class, _NTE.class, _CTI.class, _DSC.class};
		repeats = new int[]{0, -1, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, -1, 0, -1, 0, 0, -1, -1, -1, 0};
		required = new boolean[]{true, false, false, true, false, false, true, false, true, false, false, true, false, true, false, false, false, false, false, false, false};
		groups = new int[][]{{4, 6, 0, 0}, {7, 8, 0, 0}, {9, 10, 0, 0}, {14, 15, 0, 1}, {16, 19, 1, 1}, {9, 20, 1, 1}}; 
		description = "Unsolicited Laboratory Observation";
		name = "OULR21";
	}
}
