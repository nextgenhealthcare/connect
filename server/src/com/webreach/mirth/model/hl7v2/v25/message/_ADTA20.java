package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA20 extends Message{	
	public _ADTA20(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _NPU.class};
		repeats = new int[]{0, -1, 0, 0};
		required = new boolean[]{true, false, true, true};
		groups = new int[][]{}; 
		description = "Bed Status Update";
		name = "ADTA20";
	}
}
