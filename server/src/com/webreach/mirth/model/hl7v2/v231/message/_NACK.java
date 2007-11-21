package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NACK extends Message{	
	public _NACK(){
		segments = new Class[]{_MSH.class, _ERR.class, _MSA.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{true, false, true};
		groups = new int[][]{}; 
		description = "Negative Acknowledgement";
		name = "NACK";
	}
}
