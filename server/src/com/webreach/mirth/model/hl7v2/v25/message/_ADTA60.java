package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA60 extends Message{	
	public _ADTA60(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _PID.class, _PV1.class, _PV2.class, _IAM.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, -1};
		required = new boolean[]{true, false, true, true, false, false, false};
		groups = new int[][]{}; 
		description = "Update Allergy Information";
		name = "ADTA60";
	}
}
