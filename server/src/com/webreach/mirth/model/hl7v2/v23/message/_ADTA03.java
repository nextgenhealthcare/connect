package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA03 extends Message{	
	public _ADTA03(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _PV1.class, _PV2.class, _DB1.class, _DG1.class, _DRG.class, _PR1.class, _ROL.class, _OBX.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, -1, -1, 0, 0, -1, -1};
		required = new boolean[]{true, true, true, false, true, false, false, false, false, true, false, false};
		groups = new int[][]{{10, 11, 0, 1}}; 
		description = "Discharge A Patient";
		name = "ADTA03";
	}
}
