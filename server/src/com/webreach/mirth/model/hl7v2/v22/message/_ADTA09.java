package com.webreach.mirth.model.hl7v2.v22.message;
import com.webreach.mirth.model.hl7v2.v22.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA09 extends Message{	
	public _ADTA09(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PV1.class, _PV2.class, _OBX.class, _DG1.class};
		repeats = new int[]{0, 0, 0, 0, 0, -1, -1};
		required = new boolean[]{true, true, true, true, false, false, false};
		groups = new int[][]{}; 
		description = "Patient Departing";
		name = "ADTA09";
	}
}
