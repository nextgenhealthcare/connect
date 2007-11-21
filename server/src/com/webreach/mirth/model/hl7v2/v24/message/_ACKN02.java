package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ACKN02 extends Message{	
	public _ACKN02(){
		segments = new Class[]{_MSH.class, _MSA.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{true, true};
		groups = new int[][]{}; 
		description = "Application Management Data Message (unsolicited) - Response";
		name = "ACKN02";
	}
}
