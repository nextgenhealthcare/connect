package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NMR extends Message{	
	public _NMR(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QRD.class, _NCK.class, _NTE.class, _NST.class, _NTE.class, _NSC.class, _NTE.class};
		repeats = new int[]{0, 0, 0, 0, 0, -1, 0, -1, 0, -1};
		required = new boolean[]{true, true, false, false, false, false, false, false, false, false};
		groups = new int[][]{{5, 10, 1, 1}}; 
		description = "Network Management Response";
		name = "NMR";
	}
}
