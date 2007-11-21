package com.webreach.mirth.model.hl7v2.v22.message;
import com.webreach.mirth.model.hl7v2.v22.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADRA19 extends Message{	
	public _ADRA19(){
		segments = new Class[]{_MSH.class, _MSA.class, _QRD.class, _EVN.class, _PID.class, _PV1.class, _PV2.class, _OBX.class, _AL1.class, _DG1.class, _PR1.class, _GT1.class, _IN1.class, _IN2.class, _IN3.class, _ACC.class, _UB1.class, _UB2.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, false, true, true, false, false, false, false, false, false, true, false, false, false, false, false, false};
		groups = new int[][]{{13, 15, 0, 1}, {4, 18, 1, 1}}; 
		description = "Patient Query - Response to Query";
		name = "ADRA19";
	}
}
