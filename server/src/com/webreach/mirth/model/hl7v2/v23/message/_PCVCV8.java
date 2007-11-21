package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCVCV8 extends Message{	
	public _PCVCV8(){
		segments = new Class[]{_MSH.class, _EVN.class, _PCI.class, _PCS.class, _PCV.class};
		repeats = new int[]{0, 0, 0, -1, -1};
		required = new boolean[]{true, true, true, false, false};
		groups = new int[][]{}; 
		description = "Quality Issue Notification";
		name = "PCVCV8";
	}
}
