package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA62 extends Message{	
	public _ADTA62(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _PV1.class, _ROL.class, _PV2.class};
		repeats = new int[]{0, 0, 0, 0, 0, -1, 0};
		required = new boolean[]{true, true, true, false, true, false, false};
		groups = new int[][]{}; 
		description = "Cancel Change Consulting Doctor";
		name = "ADTA62";
	}
}
