package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA45 extends Message{	
	public _ADTA45(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _PID.class, _PD1.class, _MRG.class, _PV1.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0};
		required = new boolean[]{true, false, true, true, false, true, true};
		groups = new int[][]{{6, 7, 1, 1}}; 
		description = "Move Visit Information - Visit Number";
		name = "ADTA45";
	}
}
