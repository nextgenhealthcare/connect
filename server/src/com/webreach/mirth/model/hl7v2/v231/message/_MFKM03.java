package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFKM03 extends Message{	
	public _MFKM03(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _MFI.class, _MFA.class};
		repeats = new int[]{0, 0, 0, 0, -1};
		required = new boolean[]{true, true, false, true, false};
		groups = new int[][]{}; 
		description = "Test/Cbservation (for Backward Compatibility Only) - Response";
		name = "MFKM03";
	}
}
