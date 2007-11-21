package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RSPK21 extends Message{	
	public _RSPK21(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QAK.class, _QPD.class, _PID.class, _PD1.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, false, true, true, true, false, false};
		groups = new int[][]{{6, 7, 0, 0}}; 
		description = "Get Person Demographics Response";
		name = "RSPK21";
	}
}
