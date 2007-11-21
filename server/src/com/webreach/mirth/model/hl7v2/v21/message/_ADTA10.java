package com.webreach.mirth.model.hl7v2.v21.message;
import com.webreach.mirth.model.hl7v2.v21.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA10 extends Message{	
	public _ADTA10(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PV1.class, _DG1.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, true, false};
		groups = new int[][]{}; 
		description = "Patient Arriving";
		name = "ADTA10";
	}
}
