package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFKM04 extends Message{	
	public _MFKM04(){
		segments = new Class[]{_MSH.class, _MSA.class, _MFI.class, _MFA.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{true, true, true, false};
		groups = new int[][]{}; 
		description = "Master Files Charge Description - Response";
		name = "MFKM04";
	}
}
