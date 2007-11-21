package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DFTP11 extends Message{	
	public _DFTP11(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _PID.class, _PD1.class, _ROL.class, _PV1.class, _PV2.class, _ROL.class, _DB1.class, _ORC.class, _TQ1.class, _TQ2.class, _OBR.class, _NTE.class, _OBX.class, _NTE.class, _DG1.class, _DRG.class, _GT1.class, _IN1.class, _IN2.class, _IN3.class, _ROL.class, _ACC.class, _FT1.class, _PR1.class, _ROL.class, _ORC.class, _TQ1.class, _TQ2.class, _OBR.class, _NTE.class, _OBX.class, _NTE.class, _DG1.class, _DRG.class, _GT1.class, _IN1.class, _IN2.class, _IN3.class, _ROL.class};
		repeats = new int[]{0, -1, 0, 0, 0, -1, 0, 0, -1, -1, 0, 0, -1, 0, -1, 0, -1, -1, 0, -1, 0, 0, -1, -1, 0, 0, 0, -1, 0, 0, -1, 0, -1, 0, -1, -1, 0, -1, 0, 0, -1, -1};
		required = new boolean[]{true, false, true, true, false, false, false, false, false, false, false, true, false, true, false, true, false, false, false, false, true, false, false, false, false, true, true, false, false, true, false, true, false, true, false, false, false, false, true, false, false, false};
		groups = new int[][]{{12, 13, 0, 1}, {14, 15, 0, 0}, {16, 17, 0, 1}, {11, 17, 0, 1}, {21, 24, 0, 1}, {27, 28, 0, 1}, {30, 31, 0, 1}, {32, 33, 0, 0}, {34, 35, 0, 1}, {29, 35, 0, 1}, {39, 42, 0, 1}, {26, 42, 1, 1}}; 
		description = "Post Detail Financial Transactions - New";
		name = "DFTP11";
	}
}
