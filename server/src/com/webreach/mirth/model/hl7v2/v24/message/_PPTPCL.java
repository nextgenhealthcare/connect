package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PPTPCL extends Message{	
	public _PPTPCL(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QAK.class, _QRD.class, _PID.class, _PV1.class, _PV2.class, _PTH.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _GOL.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _OBX.class, _NTE.class, _PRB.class, _NTE.class, _VAR.class, _ROL.class, _VAR.class, _OBX.class, _NTE.class, _ORC.class, _OBR.class, _NTE.class, _VAR.class, _OBX.class, _NTE.class, _VAR.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, -1, 0, -1, -1, 0, -1, 0, -1, 0, -1, -1, 0, -1, 0, -1, 0, 0, -1, -1, 0, -1, -1};
		required = new boolean[]{true, true, false, false, true, true, true, false, true, false, false, true, false, true, false, false, true, false, true, false, true, false, false, true, false, true, false, true, true, false, false, true, false, false};
		groups = new int[][]{{7, 8, 0, 0}, {12, 13, 0, 1}, {17, 18, 0, 1}, {19, 20, 0, 1}, {24, 25, 0, 1}, {26, 27, 0, 1}, {21, 27, 0, 1}, {32, 34, 0, 1}, {29, 34, 0, 0}, {28, 34, 0, 1}, {14, 34, 0, 1}, {9, 34, 1, 1}, {6, 34, 1, 1}}; 
		description = "PC/ Pathway (goal-oriented) Query Response";
		name = "PPTPCL";
	}
}
