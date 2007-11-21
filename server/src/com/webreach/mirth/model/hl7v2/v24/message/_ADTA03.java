package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA03 extends Message{	
	public _ADTA03(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _ROL.class, _PV1.class, _PV2.class, _ROL.class, _DB1.class, _DG1.class, _DRG.class, _PR1.class, _ROL.class, _OBX.class, _PDA.class};
		repeats = new int[]{0, 0, 0, 0, -1, 0, 0, -1, -1, -1, 0, 0, -1, -1, 0};
		required = new boolean[]{true, true, true, false, false, true, false, false, false, false, false, true, false, false, false};
		groups = new int[][]{{12, 13, 0, 1}}; 
		description = "Discharge/End Visit";
		name = "ADTA03";
	}
}
