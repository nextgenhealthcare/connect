package com.webreach.mirth.model.hl7v2.v21.message;
import com.webreach.mirth.model.hl7v2.v21.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA24 extends Message{	
	public _ADTA24(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PV1.class, _PID.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, true, true};
		groups = new int[][]{}; 
		description = "Link Patient Information";
		name = "ADTA24";
	}
}
