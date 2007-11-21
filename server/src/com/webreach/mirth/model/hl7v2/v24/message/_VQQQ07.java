package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _VQQQ07 extends Message{	
	public _VQQQ07(){
		segments = new Class[]{_MSH.class, _VTQ.class, _RDF.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{true, true, false, false};
		groups = new int[][]{}; 
		description = "Virtual Table Query";
		name = "VQQQ07";
	}
}
