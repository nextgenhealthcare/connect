package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _TBRR08 extends Message{	
	public _TBRR08(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QAK.class, _RDF.class, _RDT.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, false, true, true, true, false};
		groups = new int[][]{}; 
		description = "Tabular Data Response";
		name = "TBRR08";
	}
}
