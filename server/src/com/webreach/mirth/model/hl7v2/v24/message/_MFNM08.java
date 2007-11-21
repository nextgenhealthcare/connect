package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM08 extends Message{	
	public _MFNM08(){
		segments = new Class[]{_MSH.class, _MFI.class, _MFE.class, _OM1.class, _OM2.class, _OM3.class, _OM4.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, true, false, false, false};
		groups = new int[][]{{3, 7, 1, 1}}; 
		description = "Test/Observation (numeric) Master File";
		name = "MFNM08";
	}
}
