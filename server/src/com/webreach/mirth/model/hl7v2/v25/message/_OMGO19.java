package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OMGO19 extends Message{	
	public _OMGO19(){
		segments = new Class[]{_MSH.class, _SFT.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _NK1.class, _PV1.class, _PV2.class, _IN1.class, _IN2.class, _IN3.class, _GT1.class, _AL1.class, _ORC.class, _TQ1.class, _TQ2.class, _OBR.class, _NTE.class, _CTD.class, _DG1.class, _OBX.class, _NTE.class, _SPM.class, _OBX.class, _SAC.class, _OBX.class, _PID.class, _PD1.class, _PV1.class, _PV2.class, _AL1.class, _ORC.class, _OBR.class, _TQ1.class, _TQ2.class, _NTE.class, _CTD.class, _OBX.class, _NTE.class, _FT1.class, _CTI.class, _BLG.class};
		repeats = new int[]{0, -1, -1, 0, 0, -1, -1, 0, 0, 0, 0, 0, 0, -1, 0, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, 0, 0, 0, -1, 0, 0, 0, -1, -1, 0, 0, -1, -1, -1, 0};
		required = new boolean[]{true, false, false, true, false, false, false, true, false, true, false, false, false, false, true, true, false, true, false, false, false, true, false, true, false, true, false, true, false, true, false, false, false, true, true, false, false, false, true, false, false, false, false};
		groups = new int[][]{{8, 9, 0, 0}, {10, 12, 0, 1}, {4, 14, 0, 0}, {16, 17, 0, 1}, {22, 23, 0, 1}, {26, 27, 0, 1}, {24, 27, 0, 1}, {28, 29, 0, 0}, {30, 31, 0, 0}, {35, 36, 0, 1}, {39, 40, 1, 1}, {33, 40, 1, 1}, {28, 40, 0, 1}, {15, 43, 1, 1}}; 
		description = "General Clinical Order";
		name = "OMGO19";
	}
}
