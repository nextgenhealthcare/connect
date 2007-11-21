package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA18 extends Message{	
	public _ADTA18(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _MRG.class, _PV1.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, false, false, true};
		groups = new int[][]{}; 
		description = "Merge Patient Information (Backwards Compatibility Only)";
		name = "ADTA18";
	}
}
