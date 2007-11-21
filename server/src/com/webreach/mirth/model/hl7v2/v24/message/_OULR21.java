package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OULR21 extends Message{	
	public _OULR21(){
		segments = new Class[]{_MSH.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _SAC.class, _SID.class, _OBX.class, _ORC.class, _OBR.class, _NTE.class, _OBX.class, _TCD.class, _SID.class, _NTE.class, _CTI.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, -1, 0, 0, 0, 0, -1, 0, 0, -1, 0, 0, 0, -1, -1, 0};
		required = new boolean[]{true, false, true, false, false, true, true, true, false, false, false, true, false, false, false, false, false, false, false};
		groups = new int[][]{{3, 5, 0, 0}, {6, 7, 0, 0}, {8, 10, 0, 0}, {14, 17, 1, 1}, {8, 18, 1, 1}}; 
		description = "Unsolicited Laboratory Observation";
		name = "OULR21";
	}
}
