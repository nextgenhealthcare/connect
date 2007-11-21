package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM11 extends Message{	
	public _MFNM11(){
		segments = new Class[]{_MSH.class, _MFI.class, _MFE.class, _OM6.class, _OM2.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, true, true};
		groups = new int[][]{{4, 5, 0, 0}, {3, 5, 1, 1}}; 
		description = "Test/Calculated Observations Master File";
		name = "MFNM11";
	}
}
