package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RSPK24 extends Message{	
	public _RSPK24(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QAK.class, _QPD.class, _PID.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, false, true, true, false, false};
		groups = new int[][]{}; 
		description = "Allocate Identifiers Response";
		name = "RSPK24";
	}
}
