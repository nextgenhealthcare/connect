package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM02 extends Message{	
	public _MFNM02(){
		segments = new Class[]{_MSH.class, _SFT.class, _MFI.class, _MFE.class, _STF.class, _PRA.class, _ORG.class, _AFF.class, _LAN.class, _EDU.class, _CER.class, _NTE.class};
		repeats = new int[]{0, -1, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1};
		required = new boolean[]{true, false, true, true, true, false, false, false, false, false, false, false};
		groups = new int[][]{{4, 12, 1, 1}}; 
		description = "Master File - Staff Practitioner";
		name = "MFNM02";
	}
}
