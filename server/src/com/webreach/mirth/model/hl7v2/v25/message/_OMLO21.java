package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OMLO21 extends Message{	
	public _OMLO21(){
		segments = new Class[]{_MSH.class, _SFT.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _NK1.class, _PV1.class, _PV2.class, _IN1.class, _IN2.class, _IN3.class, _GT1.class, _AL1.class, _ORC.class, _TQ1.class, _TQ2.class, _OBR.class, _TCD.class, _NTE.class, _CTD.class, _DG1.class, _OBX.class, _TCD.class, _NTE.class, _SPM.class, _OBX.class, _SAC.class, _OBX.class, _PID.class, _PD1.class, _PV1.class, _PV2.class, _AL1.class, _ORC.class, _OBR.class, _NTE.class, _TQ1.class, _TQ2.class, _OBX.class, _NTE.class, _FT1.class, _CTI.class, _BLG.class};
		repeats = new int[]{0, -1, -1, 0, 0, -1, -1, 0, 0, 0, 0, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, -1, 0, 0, -1, 0, -1, 0, -1, 0, 0, 0, 0, -1, 0, 0, -1, 0, -1, 0, -1, -1, -1, 0};
		required = new boolean[]{true, false, false, true, false, false, false, true, false, true, false, false, false, false, true, true, false, true, false, false, false, false, true, false, false, true, false, true, false, true, false, true, false, false, false, true, false, true, false, true, false, false, false, false};
		groups = new int[][]{{8, 9, 0, 0}, {10, 12, 0, 1}, {4, 14, 0, 0}, {16, 17, 0, 1}, {23, 25, 0, 1}, {28, 29, 0, 1}, {26, 29, 0, 1}, {30, 31, 0, 0}, {32, 33, 0, 0}, {38, 39, 0, 1}, {40, 41, 1, 1}, {35, 41, 1, 1}, {30, 41, 0, 1}, {18, 41, 0, 0}, {15, 44, 1, 1}}; 
		description = "Laboratory Order";
		name = "OMLO21";
	}
}
