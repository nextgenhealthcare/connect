package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _REFI14 extends Message{	
	public _REFI14(){
		segments = new Class[]{_MSH.class, _SFT.class, _RF1.class, _AUT.class, _CTD.class, _PRD.class, _CTD.class, _PID.class, _NK1.class, _GT1.class, _IN1.class, _IN2.class, _IN3.class, _ACC.class, _DG1.class, _DRG.class, _AL1.class, _PR1.class, _AUT.class, _CTD.class, _OBR.class, _NTE.class, _OBX.class, _NTE.class, _PV1.class, _PV2.class, _NTE.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, -1, 0, -1, -1, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, -1, 0, -1, 0, 0, -1};
		required = new boolean[]{true, false, false, true, false, true, false, true, false, false, true, false, false, false, false, false, false, true, true, false, true, false, true, false, true, false, false};
		groups = new int[][]{{4, 5, 0, 0}, {6, 7, 1, 1}, {11, 13, 0, 1}, {19, 20, 0, 0}, {18, 20, 0, 1}, {23, 24, 0, 1}, {21, 24, 0, 1}, {25, 26, 0, 0}}; 
		description = "Cancel Patient Referral";
		name = "REFI14";
	}
}
