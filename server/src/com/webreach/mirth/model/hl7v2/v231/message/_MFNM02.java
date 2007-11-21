package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM02 extends Message{	
	public _MFNM02(){
		segments = new Class[]{_MSH.class, _MFI.class, _MFE.class, _STF.class, _PRA.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, true, false};
		groups = new int[][]{{3, 5, 1, 1}}; 
		description = "Master File - Staff Practitioner";
		name = "MFNM02";
	}
}
