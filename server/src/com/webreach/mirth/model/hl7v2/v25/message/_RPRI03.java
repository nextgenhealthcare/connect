package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RPRI03 extends Message{	
	public _RPRI03(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _PRD.class, _CTD.class, _PID.class, _NTE.class};
		repeats = new int[]{0, -1, 0, 0, -1, -1, -1};
		required = new boolean[]{true, false, true, true, false, false, false};
		groups = new int[][]{{4, 5, 1, 1}}; 
		description = "Request/Receipt of Patient Selection List - Response";
		name = "RPRI03";
	}
}
