package com.webreach.mirth.model.hl7v2.v22.message;
import com.webreach.mirth.model.hl7v2.v22.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA34 extends Message{	
	public _ADTA34(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _MRG.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{true, true, true, true};
		groups = new int[][]{}; 
		description = "Merge Patient Information - Patient ID Only";
		name = "ADTA34";
	}
}
