package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OMLO35 extends Message{	
	public _OMLO35(){
		segments = new Class[]{_MSH.class, _SFT.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _NK1.class, _PV1.class, _PV2.class, _IN1.class, _IN2.class, _IN3.class, _GT1.class, _AL1.class, _SPM.class, _OBX.class, _SAC.class, _ORC.class, _TQ1.class, _TQ2.class, _OBR.class, _TCD.class, _NTE.class, _DG1.class, _OBX.class, _TCD.class, _NTE.class, _PID.class, _PD1.class, _PV1.class, _PV2.class, _AL1.class, _ORC.class, _OBR.class, _NTE.class, _TQ1.class, _TQ2.class, _OBX.class, _NTE.class, _FT1.class, _CTI.class, _BLG.class};
		repeats = new int[]{0, -1, -1, 0, 0, -1, -1, 0, 0, 0, 0, 0, 0, -1, 0, -1, 0, 0, 0, -1, 0, 0, -1, -1, 0, 0, -1, 0, 0, 0, 0, -1, 0, 0, -1, 0, -1, 0, -1, -1, -1, 0};
		required = new boolean[]{true, false, false, true, false, false, false, true, false, true, false, false, false, false, true, false, true, true, true, false, true, false, false, false, true, false, false, true, false, true, false, false, false, true, false, true, false, true, false, false, false, false};
		groups = new int[][]{{8, 9, 0, 0}, {10, 12, 0, 1}, {4, 14, 0, 0}, {19, 20, 0, 1}, {25, 27, 0, 1}, {28, 29, 0, 0}, {30, 31, 0, 0}, {36, 37, 0, 1}, {38, 39, 1, 1}, {33, 39, 1, 1}, {28, 39, 0, 1}, {21, 39, 0, 0}, {18, 42, 1, 1}, {17, 42, 1, 1}, {15, 42, 1, 1}}; 
		description = "Laboratory Order For Multiple Orders Related to a Single Container of a Specimen";
		name = "OMLO35";
	}
}
