package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA16 extends Message{	
	public _ADTA16(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _PID.class, _PD1.class, _ROL.class, _NK1.class, _PV1.class, _PV2.class, _ROL.class, _DB1.class, _OBX.class, _AL1.class, _DG1.class, _DRG.class, _PR1.class, _ROL.class, _GT1.class, _IN1.class, _IN2.class, _IN3.class, _ROL.class, _ACC.class};
		repeats = new int[]{0, -1, 0, 0, 0, -1, -1, 0, 0, -1, -1, -1, -1, -1, 0, 0, -1, -1, 0, 0, -1, -1, 0};
		required = new boolean[]{true, false, true, true, false, false, false, true, false, false, false, false, false, false, false, true, false, false, true, false, false, false, false};
		groups = new int[][]{{16, 17, 0, 1}, {19, 22, 0, 1}}; 
		description = "Pending Discharge";
		name = "ADTA16";
	}
}
