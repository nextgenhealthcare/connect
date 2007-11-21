package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NMRN01 extends Message{	
	public _NMRN01(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QRD.class, _NCK.class, _NTE.class, _NST.class, _NTE.class, _NSC.class, _NTE.class};
		repeats = new int[]{0, 0, 0, 0, 0, -1, 0, -1, 0, -1};
		required = new boolean[]{true, true, false, false, true, false, true, false, true, false};
		groups = new int[][]{{5, 6, 0, 0}, {7, 8, 0, 0}, {9, 10, 0, 0}, {5, 10, 1, 1}}; 
		description = "Application Management Query Message - Response";
		name = "NMRN01";
	}
}
