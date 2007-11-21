package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA36 extends Message{	
	public _ADTA36(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _MRG.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, false, true};
		groups = new int[][]{}; 
		description = "Merge Patient Information - Patient ID and Account Number";
		name = "ADTA36";
	}
}
