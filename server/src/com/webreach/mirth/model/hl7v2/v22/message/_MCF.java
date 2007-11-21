package com.webreach.mirth.model.hl7v2.v22.message;
import com.webreach.mirth.model.hl7v2.v22.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MCF extends Message{	
	public _MCF(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{true, true, false};
		groups = new int[][]{}; 
		description = "No Description.";
		name = "MCF";
	}
}
