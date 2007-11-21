package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RPLI02 extends Message{	
	public _RPLI02(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _PRD.class, _CTD.class, _NTE.class, _DSP.class, _DSC.class};
		repeats = new int[]{0, -1, 0, 0, -1, -1, -1, 0};
		required = new boolean[]{true, false, true, true, false, false, false, false};
		groups = new int[][]{{4, 5, 1, 1}}; 
		description = "Request/Receipt of Patient Selection Display List - Response";
		name = "RPLI02";
	}
}
