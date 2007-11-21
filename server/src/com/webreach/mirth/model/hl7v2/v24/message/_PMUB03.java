package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PMUB03 extends Message{	
	public _PMUB03(){
		segments = new Class[]{_MSH.class, _EVN.class, _STF.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{true, true, true};
		groups = new int[][]{}; 
		description = "Delete Personnel Re Cord";
		name = "PMUB03";
	}
}
