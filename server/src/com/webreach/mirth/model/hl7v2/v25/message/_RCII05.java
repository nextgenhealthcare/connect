package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RCII05 extends Message{	
	public _RCII05(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _QRD.class, _QRF.class, _PRD.class, _CTD.class, _PID.class, _DG1.class, _DRG.class, _AL1.class, _OBR.class, _NTE.class, _OBX.class, _NTE.class, _NTE.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, -1, 0, -1, -1, -1, 0, -1, 0, -1, -1};
		required = new boolean[]{true, false, true, true, false, true, false, true, false, false, false, true, false, true, false, false};
		groups = new int[][]{{6, 7, 1, 1}, {14, 15, 0, 1}, {12, 15, 0, 1}}; 
		description = "Request For Patient Clinical Information - Response";
		name = "RCII05";
	}
}
