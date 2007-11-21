package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _CRMC07 extends Message{	
	public _CRMC07(){
		segments = new Class[]{_MSH.class, _SFT.class, _PID.class, _PV1.class, _CSR.class, _CSP.class};
		repeats = new int[]{0, -1, 0, 0, 0, -1};
		required = new boolean[]{true, false, true, false, true, false};
		groups = new int[][]{{3, 6, 1, 1}}; 
		description = "Correct/Update Phase Information";
		name = "CRMC07";
	}
}
