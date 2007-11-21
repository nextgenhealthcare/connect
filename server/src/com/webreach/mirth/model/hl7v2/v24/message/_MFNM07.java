package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM07 extends Message{	
	public _MFNM07(){
		segments = new Class[]{_MSH.class, _MFI.class, _MFE.class, _CM0.class, _CM2.class};
		repeats = new int[]{0, 0, 0, 0, -1};
		required = new boolean[]{true, true, true, true, false};
		groups = new int[][]{{3, 5, 1, 1}}; 
		description = "Clinical Study Without Phases But with Schedules Master File";
		name = "MFNM07";
	}
}
