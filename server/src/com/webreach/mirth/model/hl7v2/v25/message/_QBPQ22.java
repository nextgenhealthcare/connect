package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QBPQ22 extends Message{	
	public _QBPQ22(){
		segments = new Class[]{_MSH.class, _SFT.class, _QPD.class, _RCP.class, _DSC.class};
		repeats = new int[]{0, -1, 0, 0, 0};
		required = new boolean[]{true, false, true, true, false};
		groups = new int[][]{}; 
		description = "Find Candidates";
		name = "QBPQ22";
	}
}
