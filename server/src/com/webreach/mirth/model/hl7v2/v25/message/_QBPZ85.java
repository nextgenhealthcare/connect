package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QBPZ85 extends Message{	
	public _QBPZ85(){
		segments = new Class[]{_MSH.class, _SFT.class, _QPD.class, _RCP.class, _DSC.class};
		repeats = new int[]{0, -1, 0, 0, 0};
		required = new boolean[]{true, false, true, true, false};
		groups = new int[][]{}; 
		description = "Pharmacy Information Comprehensive";
		name = "QBPZ85";
	}
}
