package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RQII02 extends Message{	
	public _RQII02(){
		segments = new Class[]{_MSH.class, _SFT.class, _PRD.class, _CTD.class, _PID.class, _NK1.class, _GT1.class, _IN1.class, _IN2.class, _IN3.class, _NTE.class};
		repeats = new int[]{0, -1, 0, -1, 0, -1, -1, 0, 0, 0, -1};
		required = new boolean[]{true, false, true, false, true, false, false, true, false, false, false};
		groups = new int[][]{{3, 4, 1, 1}, {8, 10, 1, 1}, {7, 10, 0, 0}}; 
		description = "Request/Receipt of Patient Selection Display List";
		name = "RQII02";
	}
}
