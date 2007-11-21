package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA16 extends Message{	
	public _ADTA16(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _PV1.class, _PV2.class, _DB1.class, _OBX.class, _DG1.class, _DRG.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, -1, -1, 0, 0};
		required = new boolean[]{true, true, true, false, true, false, false, false, false, false};
		groups = new int[][]{}; 
		description = "Pending Discharge";
		name = "ADTA16";
	}
}
