package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORMO01 extends Message{	
	public _ORMO01(){
		segments = new Class[]{_MSH.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _IN1.class, _IN2.class, _IN3.class, _GT1.class, _AL1.class, _ORC.class, _OBR.class, _RQD.class, _RQ1.class, _RXO.class, _ODS.class, _ODT.class, _NTE.class, _CTD.class, _DG1.class, _OBX.class, _NTE.class, _FT1.class, _CTI.class, _BLG.class};
		repeats = new int[]{0, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, -1, 0, -1, 0, -1, -1, -1, 0};
		required = new boolean[]{true, false, true, false, false, true, false, true, false, false, false, false, true, true, true, true, true, true, true, false, false, false, true, false, false, false, false};
		groups = new int[][]{{6, 7, 0, 0}, {8, 10, 0, 1}, {3, 12, 0, 0}, {14, 19, 0, 1}, {23, 24, 0, 1}, {14, 24, 0, 0}, {13, 27, 1, 1}}; 
		description = "Order Message";
		name = "ORMO01";
	}
}
