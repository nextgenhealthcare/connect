package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _VXUV04 extends Message{	
	public _VXUV04(){
		segments = new Class[]{_MSH.class, _PID.class, _PD1.class, _NK1.class, _PV1.class, _PV2.class, _GT1.class, _IN1.class, _IN2.class, _IN3.class, _ORC.class, _RXA.class, _RXR.class, _OBX.class, _NTE.class};
		repeats = new int[]{0, 0, 0, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, -1};
		required = new boolean[]{true, true, false, false, true, false, false, true, false, false, false, true, false, true, false};
		groups = new int[][]{{5, 6, 0, 0}, {8, 10, 0, 1}, {14, 15, 0, 1}, {11, 15, 0, 1}}; 
		description = "Unsolicited Vaccination Record Update";
		name = "VXUV04";
	}
}
