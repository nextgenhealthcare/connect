package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFKM06 extends Message{	
	public _MFKM06(){
		segments = new Class[]{_MSH.class, _MSA.class, _MFI.class, _MFA.class};
		repeats = new int[]{0, 0, 0, -1};
		required = new boolean[]{true, true, true, false};
		groups = new int[][]{}; 
		description = "Clinical Study with Phases and Schedules Master File - Response";
		name = "MFKM06";
	}
}
