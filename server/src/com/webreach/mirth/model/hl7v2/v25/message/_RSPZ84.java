package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RSPZ84 extends Message{	
	public _RSPZ84(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _ERR.class, _QAK.class, _QPD.class, _RDF.class, _RDT.class, _DSC.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0, -1, 0};
		required = new boolean[]{true, false, true, false, true, true, true, false, false};
		groups = new int[][]{{7, 8, 0, 0}}; 
		description = "Who Am I (response)";
		name = "RSPZ84";
	}
}
