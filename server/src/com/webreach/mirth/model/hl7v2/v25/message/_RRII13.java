package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RRII13 extends Message{	
	public _RRII13(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _RF1.class, _AUT.class, _CTD.class, _PRD.class, _CTD.class, _PID.class, _ACC.class, _DG1.class, _DRG.class, _AL1.class, _PR1.class, _AUT.class, _CTD.class, _OBR.class, _NTE.class, _OBX.class, _NTE.class, _PV1.class, _PV2.class, _NTE.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0, -1, 0, 0, -1, -1, -1, 0, 0, 0, 0, -1, 0, -1, 0, 0, -1};
		required = new boolean[]{true, false, false, false, true, false, true, false, true, false, false, false, false, true, true, false, true, false, true, false, true, false, false};
		groups = new int[][]{{5, 6, 0, 0}, {7, 8, 1, 1}, {15, 16, 0, 0}, {14, 16, 0, 1}, {19, 20, 0, 1}, {17, 20, 0, 1}, {21, 22, 0, 0}}; 
		description = "Modify Patient Referral - Response";
		name = "RRII13";
	}
}
