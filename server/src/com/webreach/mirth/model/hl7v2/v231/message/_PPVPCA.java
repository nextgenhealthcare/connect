package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PPVPCA extends Message{	
	public _PPVPCA(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QAK.class, _QRD.class, _PID.class, _PV1.class, _PV2.class, _GOL.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _PTH.class, _VAR.class, _OBX.class, _NTE.class, _PRB.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _OBX.class, _NTE.class, _ORC.class, _OBR.class, _NTE.class, _VAR.class, _OBX.class, _NTE.class, _VAR.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, -1, 0, -1, 0, -1, 0, -1, -1, 0, -1, 0, -1, 0, 0, -1, -1, 0, -1, -1};
		required = new boolean[]{true, true, false, false, true, true, true, false, true, false, false, true, false, true, false, true, false, true, false, false, true, false, true, false, true, true, false, false, true, false, false};
		groups = new int[][]{{7, 8, 0, 0}, {12, 13, 0, 1}, {14, 15, 0, 1}, {16, 17, 0, 1}, {21, 22, 0, 1}, {23, 24, 0, 1}, {18, 24, 0, 1}, {29, 31, 0, 1}, {26, 31, 0, 0}, {25, 31, 0, 1}, {9, 31, 1, 1}, {6, 31, 1, 1}}; 
		description = "PC/ Goal Response";
		name = "PPVPCA";
	}
}
