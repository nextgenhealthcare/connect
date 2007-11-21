package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA39 extends Message{	
	public _ADTA39(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _PID.class, _PD1.class, _MRG.class, _PV1.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0};
		required = new boolean[]{true, false, true, true, false, true, false};
		groups = new int[][]{{4, 7, 1, 1}}; 
		description = "Merge Person - Patient ID (for Backward Compatibility Only)";
		name = "ADTA39";
	}
}
