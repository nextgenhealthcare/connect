package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA51 extends Message{	
	public _ADTA51(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _PID.class, _PD1.class, _MRG.class, _PV1.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0};
		required = new boolean[]{true, false, true, true, false, true, true};
		groups = new int[][]{}; 
		description = "Change Alternate Visit ID";
		name = "ADTA51";
	}
}
