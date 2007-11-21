package com.webreach.mirth.model.hl7v2.v21.message;
import com.webreach.mirth.model.hl7v2.v21.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NACK extends Message{	
	public _NACK(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{true, true, false};
		groups = new int[][]{}; 
		description = "Negative Acknowledgment";
		name = "NACK";
	}
}
