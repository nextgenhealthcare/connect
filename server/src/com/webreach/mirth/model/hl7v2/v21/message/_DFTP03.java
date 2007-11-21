package com.webreach.mirth.model.hl7v2.v21.message;
import com.webreach.mirth.model.hl7v2.v21.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DFTP03 extends Message{	
	public _DFTP03(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PV1.class, _FT1.class};
		repeats = new int[]{0, 0, 0, 0, -1};
		required = new boolean[]{true, true, true, false, false};
		groups = new int[][]{}; 
		description = "Post Detail Financial Transaction";
		name = "DFTP03";
	}
}
