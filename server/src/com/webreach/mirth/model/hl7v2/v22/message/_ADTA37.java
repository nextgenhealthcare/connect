package com.webreach.mirth.model.hl7v2.v22.message;
import com.webreach.mirth.model.hl7v2.v22.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA37 extends Message{	
	public _ADTA37(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PV1.class, _PID.class, _PV1.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, false, true, false};
		groups = new int[][]{}; 
		description = "Unlink Patient Information";
		name = "ADTA37";
	}
}
