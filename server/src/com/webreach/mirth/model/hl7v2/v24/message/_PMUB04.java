package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PMUB04 extends Message{	
	public _PMUB04(){
		segments = new Class[]{_MSH.class, _EVN.class, _STF.class, _PRA.class, _ORG.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, false, false};
		groups = new int[][]{}; 
		description = "Active Practicing Person";
		name = "PMUB04";
	}
}
