package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM08 extends Message{	
	public _MFNM08(){
		segments = new Class[]{_MSH.class, _SFT.class, _MFI.class, _MFE.class, _OM1.class, _OM2.class, _OM3.class, _OM4.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, false, true, true, true, false, false, false};
		groups = new int[][]{{4, 8, 1, 1}}; 
		description = "Test/Observation (numeric) Master File";
		name = "MFNM08";
	}
}
