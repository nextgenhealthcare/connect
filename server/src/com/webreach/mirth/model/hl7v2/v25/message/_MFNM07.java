package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM07 extends Message{	
	public _MFNM07(){
		segments = new Class[]{_MSH.class, _SFT.class, _MFI.class, _MFE.class, _CM0.class, _CM2.class};
		repeats = new int[]{0, -1, 0, 0, 0, -1};
		required = new boolean[]{true, false, true, true, true, false};
		groups = new int[][]{{4, 6, 1, 1}}; 
		description = "Clinical Study without Phases but with Schedules Master File";
		name = "MFNM07";
	}
}
