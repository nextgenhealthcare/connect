package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA02 extends Message{	
	public _ADTA02(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _PID.class, _PD1.class, _ROL.class, _PV1.class, _PV2.class, _ROL.class, _DB1.class, _OBX.class, _PDA.class};
		repeats = new int[]{0, -1, 0, 0, 0, -1, 0, 0, -1, -1, -1, 0};
		required = new boolean[]{true, false, true, true, false, false, true, false, false, false, false, false};
		groups = new int[][]{}; 
		description = "Transfer a Patient";
		name = "ADTA02";
	}
}
