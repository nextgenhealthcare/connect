package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA36 extends Message{	
	public _ADTA36(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _PID.class, _PD1.class, _MRG.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0};
		required = new boolean[]{true, false, true, true, false, true};
		groups = new int[][]{}; 
		description = "Merge Patient Information - Patient ID and Account Number (for Backward Compatibility Only)";
		name = "ADTA36";
	}
}
