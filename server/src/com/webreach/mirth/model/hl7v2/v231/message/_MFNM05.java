package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM05 extends Message{	
	public _MFNM05(){
		segments = new Class[]{_MSH.class, _MFI.class, _MFE.class, _LOC.class, _LCH.class, _LRL.class, _LDP.class, _LCH.class, _LCC.class};
		repeats = new int[]{0, 0, 0, 0, -1, -1, 0, -1, -1};
		required = new boolean[]{true, true, true, true, false, false, true, false, false};
		groups = new int[][]{{7, 9, 1, 1}, {3, 9, 1, 1}}; 
		description = "Patient Location Master File";
		name = "MFNM05";
	}
}
