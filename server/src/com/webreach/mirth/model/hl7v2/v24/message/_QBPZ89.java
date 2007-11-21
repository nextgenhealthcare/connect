package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QBPZ89 extends Message{	
	public _QBPZ89(){
		segments = new Class[]{_MSH.class, _QPD.class, _RCP.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{true, true, true, false};
		groups = new int[][]{}; 
		description = "Lab Results History";
		name = "QBPZ89";
	}
}
