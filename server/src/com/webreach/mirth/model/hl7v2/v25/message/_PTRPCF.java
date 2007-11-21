package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PTRPCF extends Message{	
	public _PTRPCF(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _ERR.class, _QAK.class, _QRD.class, _PID.class, _PV1.class, _PV2.class, _PTH.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _PRB.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _OBX.class, _NTE.class, _GOL.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _OBX.class, _NTE.class, _ORC.class, _OBR.class, _ANY.class, _NTE.class, _VAR.class, _OBX.class, _NTE.class, _VAR.class};
		repeats = new int[]{0, -1, 0, -1, 0, 0, 0, 0, 0, 0, -1, -1, 0, -1, 0, -1, -1, 0, -1, 0, -1, 0, -1, -1, 0, -1, 0, -1, 0, 0, 0, -1, -1, 0, -1, -1};
		required = new boolean[]{true, false, true, false, false, true, true, true, false, true, false, false, true, false, true, false, false, true, false, true, false, true, false, false, true, false, true, false, true, true, true, false, false, true, false, false};
		groups = new int[][]{{8, 9, 0, 0}, {13, 14, 0, 1}, {18, 19, 0, 1}, {20, 21, 0, 1}, {25, 26, 0, 1}, {27, 28, 0, 1}, {22, 28, 0, 1}, {30, 31, 0, 1}, {34, 36, 0, 1}, {30, 36, 0, 0}, {29, 36, 0, 1}, {15, 36, 0, 1}, {10, 36, 1, 1}, {7, 36, 1, 1}}; 
		description = "PC/ Pathway (problem-oriented) Query Response";
		name = "PTRPCF";
	}
}
