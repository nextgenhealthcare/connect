package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OMGO19 extends Message{	
	public _OMGO19(){
		segments = new Class[]{_MSH.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _IN1.class, _IN2.class, _IN3.class, _GT1.class, _AL1.class, _ORC.class, _OBR.class, _NTE.class, _CTD.class, _DG1.class, _OBX.class, _NTE.class, _PID.class, _PD1.class, _PV1.class, _PV2.class, _AL1.class, _ORC.class, _OBR.class, _NTE.class, _CTD.class, _OBX.class, _NTE.class, _FT1.class, _CTI.class, _BLG.class};
		repeats = new int[]{0, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0, -1, 0, 0, -1, 0, -1, 0, -1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, -1, -1, -1, 0};
		required = new boolean[]{true, false, true, false, false, true, false, true, false, false, false, false, true, true, false, false, false, true, false, true, false, true, false, false, false, true, false, false, true, false, false, false, false};
		groups = new int[][]{{6, 7, 0, 0}, {8, 10, 0, 1}, {3, 12, 0, 0}, {18, 19, 0, 1}, {20, 21, 0, 0}, {22, 23, 0, 0}, {29, 30, 1, 1}, {25, 30, 1, 1}, {20, 30, 0, 1}, {13, 33, 1, 1}}; 
		description = "General Clinical Order";
		name = "OMGO19";
	}
}
