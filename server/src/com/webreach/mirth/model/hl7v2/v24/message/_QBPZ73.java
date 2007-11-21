package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QBPZ73 extends Message{	
	public _QBPZ73(){
		segments = new Class[]{_MSH.class, _QPD.class, _RCP.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{true, true, true};
		groups = new int[][]{}; 
		description = "Information About Phone Calls";
		name = "QBPZ73";
	}
}
