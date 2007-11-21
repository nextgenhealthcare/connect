package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA50 extends Message{	
	public _ADTA50(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _MRG.class, _PV1.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, false, true, true};
		groups = new int[][]{}; 
		description = "Change Visit Number";
		name = "ADTA50";
	}
}
