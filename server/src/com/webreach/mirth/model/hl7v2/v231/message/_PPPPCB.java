package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PPPPCB extends Message{	
	public _PPPPCB(){
		segments = new Class[]{_MSH.class, _PID.class, _PV1.class, _PV2.class, _PTH.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _PRB.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _OBX.class, _NTE.class, _GOL.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _OBX.class, _NTE.class, _ORC.class, _OBR.class, _NTE.class, _VAR.class, _OBX.class, _NTE.class, _VAR.class};
		repeats = new int[]{0, 0, 0, 0, 0, -1, -1, 0, -1, 0, -1, -1, 0, -1, 0, -1, 0, -1, -1, 0, -1, 0, -1, 0, 0, -1, -1, 0, -1, -1};
		required = new boolean[]{true, true, true, false, true, false, false, true, false, true, false, false, true, false, true, false, true, false, false, true, false, true, false, true, true, false, false, true, false, false};
		groups = new int[][]{{3, 4, 0, 0}, {8, 9, 0, 1}, {13, 14, 0, 1}, {15, 16, 0, 1}, {20, 21, 0, 1}, {22, 23, 0, 1}, {17, 23, 0, 1}, {28, 30, 0, 1}, {25, 30, 0, 0}, {24, 30, 0, 1}, {10, 30, 0, 1}, {5, 30, 1, 1}}; 
		description = "PC/ Pathway (problem-oriented) Add";
		name = "PPPPCB";
	}
}
