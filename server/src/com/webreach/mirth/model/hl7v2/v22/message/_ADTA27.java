package com.webreach.mirth.model.hl7v2.v22.message;
import com.webreach.mirth.model.hl7v2.v22.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA27 extends Message{	
	public _ADTA27(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _NK1.class, _PV1.class, _PV2.class, _OBX.class};
		repeats = new int[]{0, 0, 0, -1, 0, 0, -1};
		required = new boolean[]{true, true, true, false, true, false, false};
		groups = new int[][]{}; 
		description = "Cancel Pending Admit";
		name = "ADTA27";
	}
}
