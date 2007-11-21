package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PMUB03 extends Message{	
	public _PMUB03(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _STF.class};
		repeats = new int[]{0, -1, 0, 0};
		required = new boolean[]{true, false, true, true};
		groups = new int[][]{}; 
		description = "Delete Personnel Record";
		name = "PMUB03";
	}
}
