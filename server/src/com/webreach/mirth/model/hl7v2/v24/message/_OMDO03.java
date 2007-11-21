package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OMDO03 extends Message{	
	public _OMDO03(){
		segments = new Class[]{_MSH.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _IN1.class, _IN2.class, _IN3.class, _GT1.class, _AL1.class, _ORC.class, _ODS.class, _NTE.class, _OBX.class, _NTE.class, _ORC.class, _ODT.class, _NTE.class};
		repeats = new int[]{0, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0, -1, 0, -1, -1, 0, -1, 0, -1, -1};
		required = new boolean[]{true, false, true, false, false, true, false, true, false, false, false, false, true, true, false, true, false, true, true, false};
		groups = new int[][]{{6, 7, 0, 0}, {8, 10, 0, 1}, {3, 12, 0, 0}, {16, 17, 0, 1}, {14, 17, 0, 0}, {13, 17, 1, 1}, {18, 20, 0, 1}}; 
		description = "Diet Order";
		name = "OMDO03";
	}
}
