package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFRM02 extends Message{	
	public _MFRM02(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QRD.class, _QRF.class, _MFI.class, _MFE.class, _MFE.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, false, true, false, true, true, false, false};
		groups = new int[][]{{7, 8, 1, 1}}; 
		description = "Master Files Query Response";
		name = "MFRM02";
	}
}
