package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM08 extends Message{	
	public _MFNM08(){
		segments = new Class[]{_MSH.class, _MFI.class, _MFE.class, _OM2.class, _OM3.class, _OM4.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, false, false, false};
		groups = new int[][]{{4, 6, 0, 0}, {3, 6, 1, 1}}; 
		description = "Test/Observation (numeric) Master File";
		name = "MFNM08";
	}
}
