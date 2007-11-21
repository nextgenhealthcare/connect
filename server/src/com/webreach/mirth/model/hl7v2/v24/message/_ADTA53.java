package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA53 extends Message{	
	public _ADTA53(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _PV1.class, _PV2.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, false, true, false};
		groups = new int[][]{}; 
		description = "Cancel Patient Returns From a Leave of Absence";
		name = "ADTA53";
	}
}
