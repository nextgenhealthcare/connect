package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NMRN01 extends Message{	
	public _NMRN01(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _ERR.class, _QRD.class, _NCK.class, _NTE.class, _NST.class, _NTE.class, _NSC.class, _NTE.class};
		repeats = new int[]{0, -1, 0, -1, 0, 0, -1, 0, -1, 0, -1};
		required = new boolean[]{true, false, true, false, false, false, false, false, false, false, false};
		groups = new int[][]{{6, 11, 1, 1}}; 
		description = "Application Management Query Message - Response";
		name = "NMRN01";
	}
}
