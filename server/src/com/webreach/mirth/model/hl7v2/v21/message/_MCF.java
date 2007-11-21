package com.webreach.mirth.model.hl7v2.v21.message;
import com.webreach.mirth.model.hl7v2.v21.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MCF extends Message{	
	public _MCF(){
		segments = new Class[]{_MSH.class, _MSA.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{true, true};
		groups = new int[][]{}; 
		description = "No Description.";
		name = "MCF";
	}
}
