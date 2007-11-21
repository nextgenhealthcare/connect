package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BARP06 extends Message{	
	public _BARP06(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PV1.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{true, true, true, false};
		groups = new int[][]{{3, 4, 1, 1}}; 
		description = "End Account";
		name = "BARP06";
	}
}
