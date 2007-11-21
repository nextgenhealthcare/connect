package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RPII04 extends Message{	
	public _RPII04(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _PRD.class, _CTD.class, _PID.class, _NK1.class, _GT1.class, _IN1.class, _IN2.class, _IN3.class, _NTE.class};
		repeats = new int[]{0, -1, 0, 0, -1, 0, -1, -1, 0, 0, 0, -1};
		required = new boolean[]{true, false, true, true, false, true, false, false, true, false, false, false};
		groups = new int[][]{{4, 5, 1, 1}, {9, 11, 1, 1}, {8, 11, 0, 0}}; 
		description = "Request For Patient Demographic Data - Response";
		name = "RPII04";
	}
}
