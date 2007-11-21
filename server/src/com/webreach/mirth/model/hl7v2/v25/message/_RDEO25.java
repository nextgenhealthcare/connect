package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RDEO25 extends Message{	
	public _RDEO25(){
		segments = new Class[]{_MSH.class, _SFT.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _IN1.class, _IN2.class, _IN3.class, _GT1.class, _AL1.class, _ORC.class, _TQ1.class, _TQ2.class, _RXO.class, _NTE.class, _RXR.class, _RXC.class, _NTE.class, _RXE.class, _NTE.class, _TQ1.class, _TQ2.class, _RXR.class, _RXC.class, _OBX.class, _NTE.class, _FT1.class, _BLG.class, _CTI.class};
		repeats = new int[]{0, -1, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0, -1, 0, 0, -1, 0, -1, -1, 0, -1, 0, -1, 0, -1, -1, -1, 0, -1, -1, 0, -1};
		required = new boolean[]{true, false, false, true, false, false, true, false, true, false, false, false, false, true, true, false, true, false, true, true, false, true, false, true, false, true, false, true, false, false, false, false};
		groups = new int[][]{{7, 8, 0, 0}, {9, 11, 0, 1}, {4, 13, 0, 0}, {15, 16, 0, 1}, {20, 21, 0, 1}, {17, 21, 0, 0}, {24, 25, 1, 1}, {28, 29, 0, 1}, {14, 32, 1, 1}}; 
		description = "Pharmacy/Treatment Refill Authorization Request";
		name = "RDEO25";
	}
}
