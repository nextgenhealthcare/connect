package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OMNO07 extends Message{	
	public _OMNO07(){
		segments = new Class[]{_MSH.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _IN1.class, _IN2.class, _IN3.class, _GT1.class, _AL1.class, _ORC.class, _RQD.class, _RQ1.class, _NTE.class, _OBX.class, _NTE.class, _BLG.class};
		repeats = new int[]{0, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, -1, 0, -1, 0};
		required = new boolean[]{true, false, true, false, false, true, false, true, false, false, false, false, true, true, false, false, true, false, false};
		groups = new int[][]{{6, 7, 0, 0}, {8, 10, 0, 1}, {3, 12, 0, 0}, {17, 18, 0, 1}, {13, 19, 1, 1}}; 
		description = "Non-stock Requisition Order";
		name = "OMNO07";
	}
}
