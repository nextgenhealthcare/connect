package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DFTP11 extends Message{	
	public _DFTP11(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _ROL.class, _PV1.class, _PV2.class, _ROL.class, _DB1.class, _ORC.class, _OBR.class, _NTE.class, _OBX.class, _NTE.class, _DG1.class, _DRG.class, _GT1.class, _IN1.class, _IN2.class, _IN3.class, _ROL.class, _ACC.class, _FT1.class, _PR1.class, _ROL.class, _ORC.class, _OBR.class, _NTE.class, _OBX.class, _NTE.class, _DG1.class, _DRG.class, _GT1.class, _IN1.class, _IN2.class, _IN3.class, _ROL.class};
		repeats = new int[]{0, 0, 0, 0, -1, 0, 0, -1, -1, 0, 0, -1, 0, -1, -1, 0, -1, 0, 0, -1, -1, 0, 0, 0, -1, 0, 0, -1, 0, -1, -1, 0, -1, 0, 0, -1, -1};
		required = new boolean[]{true, true, true, false, false, false, false, false, false, false, true, false, true, false, false, false, false, true, false, false, false, false, true, true, false, false, true, false, true, false, false, false, false, true, false, false, false};
		groups = new int[][]{{11, 12, 0, 0}, {13, 14, 0, 1}, {10, 14, 0, 1}, {18, 21, 0, 1}, {24, 25, 0, 1}, {27, 28, 0, 0}, {29, 30, 0, 1}, {26, 30, 0, 1}, {34, 37, 0, 1}, {23, 37, 1, 1}}; 
		description = "Post Detail Financial Transaction";
		name = "DFTP11";
	}
}
