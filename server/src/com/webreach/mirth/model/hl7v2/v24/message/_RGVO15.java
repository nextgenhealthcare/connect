package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RGVO15 extends Message{	
	public _RGVO15(){
		segments = new Class[]{_MSH.class, _NTE.class, _PID.class, _NTE.class, _AL1.class, _PV1.class, _PV2.class, _ORC.class, _RXO.class, _NTE.class, _RXR.class, _RXC.class, _NTE.class, _RXE.class, _RXR.class, _RXC.class, _RXG.class, _RXR.class, _RXC.class, _OBX.class, _NTE.class};
		repeats = new int[]{0, -1, 0, -1, -1, 0, 0, 0, 0, -1, -1, -1, -1, 0, -1, -1, 0, -1, -1, 0, -1};
		required = new boolean[]{true, false, true, false, false, true, false, true, true, true, true, true, false, true, true, false, true, true, false, false, false};
		groups = new int[][]{{6, 7, 0, 0}, {3, 7, 0, 0}, {12, 13, 0, 0}, {10, 13, 0, 0}, {9, 13, 0, 0}, {14, 16, 0, 0}, {20, 21, 1, 1}, {17, 21, 1, 1}, {8, 21, 1, 1}}; 
		description = "Pharmacy/Treatment Give";
		name = "RGVO15";
	}
}
