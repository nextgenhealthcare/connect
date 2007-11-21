package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RPAI10 extends Message{	
	public _RPAI10(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _RF1.class, _AUT.class, _CTD.class, _PRD.class, _CTD.class, _PID.class, _NK1.class, _GT1.class, _IN1.class, _IN2.class, _IN3.class, _ACC.class, _DG1.class, _DRG.class, _AL1.class, _PR1.class, _AUT.class, _CTD.class, _OBR.class, _NTE.class, _OBX.class, _NTE.class, _PV1.class, _PV2.class, _NTE.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0, -1, 0, -1, -1, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, -1, 0, -1, 0, 0, -1};
		required = new boolean[]{true, false, true, false, true, false, true, false, true, false, false, true, false, false, false, false, false, false, true, true, false, true, false, true, false, true, false, false};
		groups = new int[][]{{5, 6, 0, 0}, {7, 8, 1, 1}, {12, 14, 0, 1}, {20, 21, 0, 0}, {19, 21, 1, 1}, {24, 25, 0, 1}, {22, 25, 0, 1}, {26, 27, 0, 0}}; 
		description = "Request For Resubmission of An Authorization - Response";
		name = "RPAI10";
	}
}
