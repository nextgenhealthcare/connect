package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ERPQ08 extends Message{	
	public _ERPQ08(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QAK.class, _ERQ.class, _ERQ.class, _ERQ.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, false, true, true, true, true, false};
		groups = new int[][]{}; 
		description = "Event Replay Response";
		name = "ERPQ08";
	}
}
