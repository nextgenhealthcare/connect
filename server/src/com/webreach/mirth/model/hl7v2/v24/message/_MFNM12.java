package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM12 extends Message{	
	public _MFNM12(){
		segments = new Class[]{_MSH.class, _MFI.class, _MFE.class, _OM1.class, _OM7.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, true, false};
		groups = new int[][]{{3, 5, 1, 1}}; 
		description = "Master File Notification Message";
		name = "MFNM12";
	}
}
