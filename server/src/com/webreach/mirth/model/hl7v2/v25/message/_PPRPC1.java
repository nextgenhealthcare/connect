package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PPRPC1 extends Message{	
	public _PPRPC1(){
		segments = new Class[]{_MSH.class, _SFT.class, _PID.class, _PV1.class, _PV2.class, _PRB.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _PTH.class, _VAR.class, _OBX.class, _NTE.class, _GOL.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _OBX.class, _NTE.class, _ORC.class, _OBR.class, _ANY.class, _NTE.class, _VAR.class, _OBX.class, _NTE.class, _VAR.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, -1, -1, 0, -1, 0, -1, 0, -1, 0, -1, -1, 0, -1, 0, -1, 0, 0, 0, -1, -1, 0, -1, -1};
		required = new boolean[]{true, false, true, true, false, true, false, false, true, false, true, false, true, false, true, false, false, true, false, true, false, true, true, true, false, false, true, false, false};
		groups = new int[][]{{4, 5, 0, 0}, {9, 10, 0, 1}, {11, 12, 0, 1}, {13, 14, 0, 1}, {18, 19, 0, 1}, {20, 21, 0, 1}, {15, 21, 0, 1}, {23, 24, 0, 1}, {27, 29, 0, 1}, {23, 29, 0, 0}, {22, 29, 0, 1}, {6, 29, 1, 1}}; 
		description = "PC/ Problem Add";
		name = "PPRPC1";
	}
}
