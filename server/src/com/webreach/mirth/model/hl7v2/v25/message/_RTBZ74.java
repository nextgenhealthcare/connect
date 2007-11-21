package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RTBZ74 extends Message{	
	public _RTBZ74(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _SFT.class, _QAK.class, _QPD.class, _RDF.class, _RDT.class, _DSC.class};
		repeats = new int[]{0, 0, -1, -1, 0, 0, 0, -1, 0};
		required = new boolean[]{true, true, false, false, true, true, true, false, false};
		groups = new int[][]{{7, 8, 0, 0}}; 
		description = "Information About Phone Calls (response)";
		name = "RTBZ74";
	}
}
