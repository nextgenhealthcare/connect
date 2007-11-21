package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RGVO15 extends Message{	
	public _RGVO15(){
		segments = new Class[]{_MSH.class, _SFT.class, _NTE.class, _PID.class, _NTE.class, _AL1.class, _PV1.class, _PV2.class, _ORC.class, _TQ1.class, _TQ2.class, _RXO.class, _NTE.class, _RXR.class, _RXC.class, _NTE.class, _RXE.class, _TQ1.class, _TQ2.class, _RXR.class, _RXC.class, _RXG.class, _TQ1.class, _TQ2.class, _RXR.class, _RXC.class, _OBX.class, _NTE.class};
		repeats = new int[]{0, -1, -1, 0, -1, -1, 0, 0, 0, 0, -1, 0, -1, -1, 0, -1, 0, 0, -1, -1, -1, 0, 0, -1, -1, -1, 0, -1};
		required = new boolean[]{true, false, false, true, false, false, true, false, true, true, false, true, true, true, true, false, true, true, false, true, false, true, true, false, true, false, false, false};
		groups = new int[][]{{7, 8, 0, 0}, {4, 8, 0, 0}, {10, 11, 0, 1}, {15, 16, 0, 1}, {13, 16, 0, 0}, {12, 16, 0, 0}, {18, 19, 1, 1}, {17, 21, 0, 0}, {23, 24, 1, 1}, {27, 28, 1, 1}, {22, 28, 1, 1}, {9, 28, 1, 1}}; 
		description = "Pharmacy/Treatment Give";
		name = "RGVO15";
	}
}
