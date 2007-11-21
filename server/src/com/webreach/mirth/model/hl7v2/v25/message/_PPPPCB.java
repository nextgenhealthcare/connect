package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PPPPCB extends Message{	
	public _PPPPCB(){
		segments = new Class[]{_MSH.class, _SFT.class, _PID.class, _PV1.class, _PV2.class, _PTH.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _PRB.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _OBX.class, _NTE.class, _GOL.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _OBX.class, _NTE.class, _ORC.class, _OBR.class, _ANY.class, _NTE.class, _VAR.class, _OBX.class, _NTE.class, _VAR.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, -1, -1, 0, -1, 0, -1, -1, 0, -1, 0, -1, 0, -1, -1, 0, -1, 0, -1, 0, 0, 0, -1, -1, 0, -1, -1};
		required = new boolean[]{true, false, true, true, false, true, false, false, true, false, true, false, false, true, false, true, false, true, false, false, true, false, true, false, true, true, true, false, false, true, false, false};
		groups = new int[][]{{4, 5, 0, 0}, {9, 10, 0, 1}, {14, 15, 0, 1}, {16, 17, 0, 1}, {21, 22, 0, 1}, {23, 24, 0, 1}, {18, 24, 0, 1}, {26, 27, 0, 1}, {30, 32, 0, 1}, {26, 32, 0, 0}, {25, 32, 0, 1}, {11, 32, 0, 1}, {6, 32, 1, 1}}; 
		description = "PC/ Pathway (problem-oriented) Add";
		name = "PPPPCB";
	}
}
