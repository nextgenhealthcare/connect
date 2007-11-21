package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM03 extends Message{	
	public _MFNM03(){
		segments = new Class[]{_MSH.class, _SFT.class, _MFI.class, _MFE.class, _OM1.class, _ANY.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0};
		required = new boolean[]{true, false, true, true, true, true};
		groups = new int[][]{{4, 6, 1, 1}}; 
		description = "Master File - Test/Observation (for Backward Compatibility Only)";
		name = "MFNM03";
	}
}
