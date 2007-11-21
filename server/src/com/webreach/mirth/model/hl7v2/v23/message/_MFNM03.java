package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM03 extends Message{	
	public _MFNM03(){
		segments = new Class[]{_MSH.class, _MFI.class, _MFE.class, _MFE.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{true, true, true, false};
		groups = new int[][]{{3, 4, 1, 1}}; 
		description = "Master File - Test Observation";
		name = "MFNM03";
	}
}
