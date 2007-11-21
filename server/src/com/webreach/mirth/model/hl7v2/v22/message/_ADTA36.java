package com.webreach.mirth.model.hl7v2.v22.message;
import com.webreach.mirth.model.hl7v2.v22.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA36 extends Message{	
	public _ADTA36(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _MRG.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{true, true, true, true};
		groups = new int[][]{}; 
		description = "Merge Patient Information - Patient ID and Account";
		name = "ADTA36";
	}
}
