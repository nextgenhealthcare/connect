package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA43 extends Message{	
	public _ADTA43(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _PID.class, _PD1.class, _MRG.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0};
		required = new boolean[]{true, false, true, true, false, true};
		groups = new int[][]{{4, 6, 1, 1}}; 
		description = "Move Patient Information - Patient Identifier List";
		name = "ADTA43";
	}
}
