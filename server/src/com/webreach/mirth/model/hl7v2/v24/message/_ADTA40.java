package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA40 extends Message{	
	public _ADTA40(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _MRG.class, _PV1.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, false, true, false};
		groups = new int[][]{{3, 6, 1, 1}}; 
		description = "Merge Patient - Internal ID";
		name = "ADTA40";
	}
}
