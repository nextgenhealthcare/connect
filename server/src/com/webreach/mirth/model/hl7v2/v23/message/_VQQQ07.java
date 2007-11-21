package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _VQQQ07 extends Message{	
	public _VQQQ07(){
		segments = new Class[]{_MSH.class, _VTQ.class, _RDF.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{true, true, false, false};
		groups = new int[][]{}; 
		description = "Vitual Table Query";
		name = "VQQQ07";
	}
}
