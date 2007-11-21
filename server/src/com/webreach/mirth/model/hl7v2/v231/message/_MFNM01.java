package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM01 extends Message{	
	public _MFNM01(){
		segments = new Class[]{_MSH.class, _MFI.class, _MFE.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{true, true, true};
		groups = new int[][]{{3, 3, 1, 1}}; 
		description = "Master File not Otherwise Specified (for Backward Compatibility Only)";
		name = "MFNM01";
	}
}
