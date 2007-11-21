package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ERPR09 extends Message{	
	public _ERPR09(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QAK.class, _ERQ.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, false, true, true, false};
		groups = new int[][]{}; 
		description = "Event Replay Response";
		name = "ERPR09";
	}
}
