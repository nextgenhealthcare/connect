package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PPRPC1 extends Message{	
	public _PPRPC1(){
		segments = new Class[]{_MSH.class, _PID.class, _PV1.class, _PV2.class, _PRB.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _PTH.class, _VAR.class, _OBX.class, _NTE.class, _GOL.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _OBX.class, _NTE.class, _ORC.class, _OBR.class, _NTE.class, _VAR.class, _OBX.class, _NTE.class, _VAR.class};
		repeats = new int[]{0, 0, 0, 0, 0, -1, -1, 0, -1, 0, -1, 0, -1, 0, -1, -1, 0, -1, 0, -1, 0, 0, -1, -1, 0, -1, -1};
		required = new boolean[]{true, true, true, false, true, false, false, true, false, true, false, true, false, true, false, false, true, false, true, false, true, true, false, false, true, false, false};
		groups = new int[][]{{3, 4, 0, 0}, {8, 9, 0, 1}, {10, 11, 0, 1}, {12, 13, 0, 1}, {17, 18, 0, 1}, {19, 20, 0, 1}, {14, 20, 0, 1}, {25, 27, 0, 1}, {22, 27, 0, 0}, {21, 27, 0, 1}, {5, 27, 1, 1}}; 
		description = "PC/ Problem Add";
		name = "PPRPC1";
	}
}
