package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RQII01 extends Message{	
	public _RQII01(){
		segments = new Class[]{_MSH.class, _PRD.class, _CTD.class, _PID.class, _NK1.class, _GT1.class, _IN1.class, _IN2.class, _IN3.class, _NTE.class};
		repeats = new int[]{0, 0, -1, 0, -1, -1, 0, 0, 0, -1};
		required = new boolean[]{true, true, false, true, false, false, true, false, false, false};
		groups = new int[][]{{2, 3, 1, 1}, {7, 9, 1, 1}, {6, 9, 0, 0}}; 
		description = "Request For Insurance Information";
		name = "RQII01";
	}
}
