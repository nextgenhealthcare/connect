package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DFTP03 extends Message{	
	public _DFTP03(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _PV1.class, _PV2.class, _DB1.class, _OBX.class, _FT1.class, _PR1.class, _ROL.class, _DG1.class, _DRG.class, _GT1.class, _IN1.class, _IN2.class, _IN3.class, _ACC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, -1, -1, 0, 0, -1, -1, 0, -1, 0, 0, -1, 0};
		required = new boolean[]{true, true, true, false, false, false, false, false, true, true, false, false, false, false, true, false, false, false};
		groups = new int[][]{{10, 11, 0, 1}, {9, 11, 1, 1}, {15, 17, 0, 1}}; 
		description = "Post Detail Financial Transaction";
		name = "DFTP03";
	}
}
