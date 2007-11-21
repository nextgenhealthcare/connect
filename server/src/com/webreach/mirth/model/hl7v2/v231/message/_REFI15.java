package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _REFI15 extends Message{	
	public _REFI15(){
		segments = new Class[]{_MSH.class, _RF1.class, _AUT.class, _CTD.class, _PRD.class, _CTD.class, _PID.class, _NK1.class, _GT1.class, _IN1.class, _IN2.class, _IN3.class, _ACC.class, _DG1.class, _DRG.class, _AL1.class, _PR1.class, _AUT.class, _CTD.class, _OBR.class, _NTE.class, _OBX.class, _NTE.class, _PV1.class, _PV2.class, _PV1.class, _PV2.class, _NTE.class};
		repeats = new int[]{0, 0, 0, 0, 0, -1, 0, -1, -1, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, -1, 0, -1, 0, 0, 0, 0, -1};
		required = new boolean[]{true, false, true, false, true, false, true, false, false, true, false, false, false, false, false, false, true, true, false, true, false, true, false, true, false, true, false, false};
		groups = new int[][]{{3, 4, 0, 0}, {5, 6, 1, 1}, {10, 12, 0, 1}, {18, 19, 0, 0}, {17, 19, 0, 1}, {22, 23, 0, 1}, {20, 23, 0, 1}, {24, 25, 0, 0}, {26, 27, 0, 0}}; 
		description = "Request Patient Referral Status";
		name = "REFI15";
	}
}
