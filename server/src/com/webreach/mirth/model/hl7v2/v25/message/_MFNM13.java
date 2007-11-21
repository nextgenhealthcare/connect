package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM13 extends Message{	
	public _MFNM13(){
		segments = new Class[]{_MSH.class, _SFT.class, _MFI.class, _MFE.class};
		repeats = new int[]{0, -1, 0, -1};
		required = new boolean[]{true, false, true, true};
		groups = new int[][]{}; 
		description = "Master File Notification - General";
		name = "MFNM13";
	}
}
