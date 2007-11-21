package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PMUB06 extends Message{	
	public _PMUB06(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _STF.class, _PRA.class, _ORG.class};
		repeats = new int[]{0, -1, 0, 0, -1, -1};
		required = new boolean[]{true, false, true, true, false, false};
		groups = new int[][]{}; 
		description = "Terminate Practicing Person";
		name = "PMUB06";
	}
}
