package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RSPK21 extends Message{	
	public _RSPK21(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _ERR.class, _QAK.class, _QPD.class, _PID.class, _PD1.class, _NK1.class, _QRI.class, _DSC.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0, 0, -1, 0, 0};
		required = new boolean[]{true, false, true, false, true, true, true, false, false, true, false};
		groups = new int[][]{{7, 10, 0, 1}}; 
		description = "Get Person Demographics Response";
		name = "RSPK21";
	}
}
