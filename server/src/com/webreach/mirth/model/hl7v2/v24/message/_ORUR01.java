package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORUR01 extends Message{	
	public _ORUR01(){
		segments = new Class[]{_MSH.class, _PID.class, _PD1.class, _NK1.class, _NTE.class, _PV1.class, _PV2.class, _ORC.class, _OBR.class, _NTE.class, _CTD.class, _OBX.class, _NTE.class, _FT1.class, _CTI.class, _DSC.class};
		repeats = new int[]{0, 0, 0, -1, -1, 0, 0, 0, 0, -1, 0, 0, -1, -1, -1, 0};
		required = new boolean[]{true, true, false, false, false, true, false, false, true, false, false, false, false, false, false, false};
		groups = new int[][]{{6, 7, 0, 0}, {2, 7, 0, 0}, {12, 13, 1, 1}, {8, 15, 1, 1}, {2, 15, 1, 1}}; 
		description = "Unsolicited Transmission of An Observation Message";
		name = "ORUR01";
	}
}
