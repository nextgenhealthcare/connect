package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SPQQ08 extends Message{	
	public _SPQQ08(){
		segments = new Class[]{_MSH.class, _SPR.class, _RDF.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{true, true, false, false};
		groups = new int[][]{}; 
		description = "Stored Procedure Request";
		name = "SPQQ08";
	}
}
