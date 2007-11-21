package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QBPZ99 extends Message{	
	public _QBPZ99(){
		segments = new Class[]{_MSH.class, _SFT.class, _QPD.class, _RDF.class, _RCP.class, _DSC.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0};
		required = new boolean[]{true, false, true, false, true, false};
		groups = new int[][]{}; 
		description = "Who Am I";
		name = "QBPZ99";
	}
}
