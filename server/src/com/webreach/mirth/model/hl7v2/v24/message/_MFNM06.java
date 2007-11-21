package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM06 extends Message{	
	public _MFNM06(){
		segments = new Class[]{_MSH.class, _MFI.class, _MFE.class, _CM0.class, _CM1.class, _CM2.class};
		repeats = new int[]{0, 0, 0, 0, 0, -1};
		required = new boolean[]{true, true, true, true, true, false};
		groups = new int[][]{{5, 6, 0, 1}, {3, 6, 1, 1}}; 
		description = "Clinical Study with Phases and Schedules Master File";
		name = "MFNM06";
	}
}
