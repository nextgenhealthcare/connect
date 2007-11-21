package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM11 extends Message{	
	public _MFNM11(){
		segments = new Class[]{_MSH.class, _SFT.class, _MFI.class, _MFE.class, _OM1.class, _OM6.class, _OM2.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0};
		required = new boolean[]{true, false, true, true, true, true, true};
		groups = new int[][]{{6, 7, 0, 0}, {4, 7, 1, 1}}; 
		description = "Test/Calculated Observations Master File";
		name = "MFNM11";
	}
}
