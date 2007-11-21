package com.webreach.mirth.model.hl7v2.v21.message;
import com.webreach.mirth.model.hl7v2.v21.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA05 extends Message{	
	public _ADTA05(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _NK1.class, _PV1.class, _DG1.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, true, true, false};
		groups = new int[][]{}; 
		description = "Preadmit a Patient";
		name = "ADTA05";
	}
}
