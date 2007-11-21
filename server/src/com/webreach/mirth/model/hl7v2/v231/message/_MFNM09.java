package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM09 extends Message{	
	public _MFNM09(){
		segments = new Class[]{_MSH.class, _MFI.class, _MFE.class, _OM3.class, _OM4.class};
		repeats = new int[]{0, 0, 0, 0, -1};
		required = new boolean[]{true, true, true, true, false};
		groups = new int[][]{{4, 5, 0, 0}, {3, 5, 1, 1}}; 
		description = "Test/Observation (categorical) Master File";
		name = "MFNM09";
	}
}
