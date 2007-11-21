package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BARP10 extends Message{	
	public _BARP10(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _PID.class, _PV1.class, _DG1.class, _GP1.class, _PR1.class, _GP2.class};
		repeats = new int[]{0, -1, 0, 0, 0, -1, 0, 0, 0};
		required = new boolean[]{true, false, true, true, true, false, true, true, false};
		groups = new int[][]{{8, 9, 0, 1}}; 
		description = "Transmit Ambulatory Payment Classification (APC)";
		name = "BARP10";
	}
}
