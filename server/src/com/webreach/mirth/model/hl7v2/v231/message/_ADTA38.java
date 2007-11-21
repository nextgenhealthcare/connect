package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA38 extends Message{	
	public _ADTA38(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _PV1.class, _PV2.class, _DB1.class, _OBX.class, _DG1.class, _DRG.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, -1, -1, -1, 0};
		required = new boolean[]{true, true, true, false, true, false, false, false, false, false};
		groups = new int[][]{}; 
		description = "Cancel Pre-Admit";
		name = "ADTA38";
	}
}
