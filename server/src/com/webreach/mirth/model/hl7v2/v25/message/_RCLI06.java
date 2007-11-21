package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RCLI06 extends Message{	
	public _RCLI06(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _QRD.class, _QRF.class, _PRD.class, _CTD.class, _PID.class, _DG1.class, _DRG.class, _AL1.class, _NTE.class, _DSP.class, _DSC.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, -1, 0, -1, -1, -1, -1, -1, 0};
		required = new boolean[]{true, false, true, true, false, true, false, true, false, false, false, false, false, false};
		groups = new int[][]{{6, 7, 1, 1}}; 
		description = "Request/Receipt of Clinical Data Listing - Response";
		name = "RCLI06";
	}
}
