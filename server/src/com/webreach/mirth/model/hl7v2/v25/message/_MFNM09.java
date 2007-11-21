package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM09 extends Message{	
	public _MFNM09(){
		segments = new Class[]{_MSH.class, _SFT.class, _MFI.class, _MFE.class, _OM1.class, _OM3.class, _OM4.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, -1};
		required = new boolean[]{true, false, true, true, true, true, false};
		groups = new int[][]{{6, 7, 0, 0}, {4, 7, 1, 1}}; 
		description = "Test/Observation (categorical) Master File";
		name = "MFNM09";
	}
}
