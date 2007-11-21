package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM12 extends Message{	
	public _MFNM12(){
		segments = new Class[]{_MSH.class, _SFT.class, _MFI.class, _MFE.class, _OM1.class, _OM7.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0};
		required = new boolean[]{true, false, true, true, true, false};
		groups = new int[][]{{4, 6, 1, 1}}; 
		description = "Master File Notification Message";
		name = "MFNM12";
	}
}
