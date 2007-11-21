package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OMLO21 extends Message{	
	public _OMLO21(){
		segments = new Class[]{_MSH.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _IN1.class, _IN2.class, _IN3.class, _GT1.class, _AL1.class, _SAC.class, _OBX.class, _ORC.class, _OBR.class, _SAC.class, _OBX.class, _TCD.class, _NTE.class, _DG1.class, _OBX.class, _TCD.class, _NTE.class, _PID.class, _PD1.class, _PV1.class, _PV2.class, _AL1.class, _ORC.class, _OBR.class, _NTE.class, _OBX.class, _NTE.class, _FT1.class, _CTI.class, _BLG.class};
		repeats = new int[]{0, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0, -1, 0, -1, 0, 0, 0, -1, 0, -1, -1, 0, 0, -1, 0, 0, 0, 0, -1, 0, 0, 0, 0, -1, -1, -1, 0};
		required = new boolean[]{true, false, true, false, false, true, false, true, false, false, false, false, true, false, true, true, true, false, false, false, false, true, false, false, true, false, true, false, false, false, true, false, true, false, false, false, false};
		groups = new int[][]{{6, 7, 0, 0}, {8, 10, 0, 1}, {3, 12, 0, 0}, {13, 14, 0, 0}, {17, 18, 0, 1}, {22, 24, 0, 1}, {25, 26, 0, 0}, {27, 28, 0, 0}, {33, 34, 1, 1}, {30, 34, 1, 1}, {25, 34, 0, 1}, {16, 34, 0, 0}, {15, 37, 1, 1}, {13, 37, 1, 1}}; 
		description = "Laboratory Order";
		name = "OMLO21";
	}
}
