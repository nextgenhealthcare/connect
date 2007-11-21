package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OMNO07 extends Message{	
	public _OMNO07(){
		segments = new Class[]{_MSH.class, _SFT.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _IN1.class, _IN2.class, _IN3.class, _GT1.class, _AL1.class, _ORC.class, _TQ1.class, _TQ2.class, _RQD.class, _RQ1.class, _NTE.class, _OBX.class, _NTE.class, _BLG.class};
		repeats = new int[]{0, -1, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, -1, 0};
		required = new boolean[]{true, false, false, true, false, false, true, false, true, false, false, false, false, true, true, false, true, false, false, true, false, false};
		groups = new int[][]{{7, 8, 0, 0}, {9, 11, 0, 1}, {4, 13, 0, 0}, {15, 16, 0, 1}, {20, 21, 0, 1}, {14, 22, 1, 1}}; 
		description = "Stock Requisition Order";
		name = "OMNO07";
	}
}
