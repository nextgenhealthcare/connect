package com.webreach.mirth.model.hl7v2.v21.message;
import com.webreach.mirth.model.hl7v2.v21.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA17 extends Message{	
	public _ADTA17(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PV1.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{true, true, true, true};
		groups = new int[][]{{3, 4, 1, 1}}; 
		description = "Swap Patients";
		name = "ADTA17";
	}
}
