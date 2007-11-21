package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA45 extends Message{	
	public _ADTA45(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _MRG.class, _PV1.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, false, true, true};
		groups = new int[][]{{5, 6, 1, 1}}; 
		description = "Move Visit Information - Visit Number";
		name = "ADTA45";
	}
}
