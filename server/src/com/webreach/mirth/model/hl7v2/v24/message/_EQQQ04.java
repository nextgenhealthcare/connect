package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _EQQQ04 extends Message{	
	public _EQQQ04(){
		segments = new Class[]{_MSH.class, _EQL.class, _DSC.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{true, true, false};
		groups = new int[][]{}; 
		description = "Embedded Query Language Query";
		name = "EQQQ04";
	}
}
