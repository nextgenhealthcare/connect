package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RSPK23 extends Message{	
	public _RSPK23(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _ERR.class, _QAK.class, _QPD.class, _PID.class, _DSC.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, false, true, false, true, true, true, false};
		groups = new int[][]{}; 
		description = "Get Corresponding Identifiers Response";
		name = "RSPK23";
	}
}
