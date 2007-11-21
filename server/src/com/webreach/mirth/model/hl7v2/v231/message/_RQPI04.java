package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RQPI04 extends Message{	
	public _RQPI04(){
		segments = new Class[]{_MSH.class, _PRD.class, _CTD.class, _PID.class, _NK1.class, _GT1.class, _NTE.class};
		repeats = new int[]{0, 0, -1, 0, -1, -1, -1};
		required = new boolean[]{true, true, false, true, false, false, false};
		groups = new int[][]{{2, 3, 1, 1}}; 
		description = "Request For Patient Demographic Data";
		name = "RQPI04";
	}
}
