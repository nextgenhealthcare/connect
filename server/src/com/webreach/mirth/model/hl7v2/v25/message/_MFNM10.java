package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM10 extends Message{	
	public _MFNM10(){
		segments = new Class[]{_MSH.class, _SFT.class, _MFI.class, _MFE.class, _OM1.class, _OM5.class, _OM4.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, -1};
		required = new boolean[]{true, false, true, true, true, true, false};
		groups = new int[][]{{6, 7, 0, 0}, {4, 7, 1, 1}}; 
		description = "Test/Observation Batteries Master File";
		name = "MFNM10";
	}
}
